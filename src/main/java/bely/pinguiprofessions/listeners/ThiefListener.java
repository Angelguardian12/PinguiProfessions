package bely.pinguiprofessions.listeners;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.ProfessionManager;
import bely.pinguiprofessions.models.PlayerProfile;
import bely.pinguiprofessions.models.Profession;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ThiefListener implements Listener {

    private final PinguiProfessions plugin;
    private final ProfessionManager professionManager;
    private final Map<UUID, Long> smokeBombCooldowns = new HashMap<>();
    private final Random random = new Random();

    public ThiefListener(PinguiProfessions plugin, ProfessionManager professionManager) {
        this.plugin = plugin;
        this.professionManager = professionManager;
    }

    // Paso Sigiloso: Velocidad II al agacharse
    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = professionManager.getProfile(player.getUniqueId());

        if (profile != null && profile.getProfession() == Profession.THIEF && profile.getRank() > 0) {
            if (event.isSneaking()) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1, false, false, true));
            } else {
                player.removePotionEffect(PotionEffectType.SPEED);
            }
        }
    }

    // Bomba de Humo
    @EventHandler
    public void onUseSmokeBomb(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT")) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() == Material.FIREWORK_STAR && item.hasItemMeta() && item.getItemMeta().getDisplayName().contains("Bomba de Humo")) {
            PlayerProfile profile = professionManager.getProfile(player.getUniqueId());
            if (profile != null && profile.getProfession() == Profession.THIEF && profile.getRank() > 0) {
                event.setCancelled(true);
                
                long current = System.currentTimeMillis();
                int cooldownSecs = plugin.getConfig().getInt("professions.thief.smoke_bomb_cooldown", 15);
                
                if (smokeBombCooldowns.containsKey(player.getUniqueId())) {
                    long lastUse = smokeBombCooldowns.get(player.getUniqueId());
                    if (current - lastUse < cooldownSecs * 1000L) {
                        player.sendMessage("§c¡Tu bomba de humo aún no está lista!");
                        return;
                    }
                }
                
                smokeBombCooldowns.put(player.getUniqueId(), current);
                
                // Consumir
                item.setAmount(item.getAmount() - 1);
                
                // Efecto
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.5f);
                player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation().add(0, 1, 0), 200, 3, 3, 3, 0.05);
                
                // Ceguera a los cercanos (excepto al propio ladrón)
                for (Entity e : player.getNearbyEntities(5, 5, 5)) {
                    if (e instanceof LivingEntity && !e.equals(player)) {
                        ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0));
                    }
                }
            } else {
                player.sendMessage("§cNo tienes idea de cómo usar este peligroso artefacto.");
            }
        }
    }

    // Dejar rastro (Pista) al golpear a un jugador
    @EventHandler
    public void onAttackDropClue(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player thief = (Player) event.getDamager();
            checkAndDropClue(thief);
        }
    }

    // Dejar rastro al revisar cofres
    @EventHandler
    public void onOpenChestDropClue(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.CHEST || event.getInventory().getType() == InventoryType.BARREL) {
            checkAndDropClue((Player) event.getPlayer());
        }
    }

    private void checkAndDropClue(Player player) {
        PlayerProfile profile = professionManager.getProfile(player.getUniqueId());
        if (profile != null && profile.getProfession() == Profession.THIEF && profile.getRank() > 0) {
            int chance = plugin.getConfig().getInt("professions.thief.clue_drop_chance", 30);
            if (random.nextInt(100) < chance) {
                dropClue(player);
            }
        }
    }

    private void dropClue(Player thief) {
        ItemStack clueItem = new ItemStack(Material.PAPER);
        ItemMeta meta = clueItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cPista Sospechosa");
            // Usamos PersistentDataContainer para guardar el nombre real del ladrón de forma oculta en el item
            NamespacedKey key = new NamespacedKey(plugin, "thief_identity");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, thief.getName());
            clueItem.setItemMeta(meta);
        }

        // Drop the item naturally
        Item dropped = thief.getWorld().dropItemNaturally(thief.getLocation(), clueItem);
        dropped.setPickupDelay(20);
        
        // Hide the item for everyone EXCEPT investigators
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    PlayerProfile prof = professionManager.getProfile(p.getUniqueId());
                    if (prof == null || prof.getProfession() != Profession.INVESTIGATOR) {
                        p.hideEntity(plugin, dropped);
                    }
                }
            }
        }.runTaskLater(plugin, 1L); // Run next tick to ensure the entity exists fully
        
        // El ladrón no sabe que dejó una pista (silencioso)
    }
}
