package bely.pinguiprofessions.listeners;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.ProfessionManager;
import bely.pinguiprofessions.models.PlayerProfile;
import bely.pinguiprofessions.models.Profession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class CombatListener implements Listener {

    private final PinguiProfessions plugin;
    private final ProfessionManager professionManager;

    public CombatListener(PinguiProfessions plugin, ProfessionManager professionManager) {
        this.plugin = plugin;
        this.professionManager = professionManager;
    }

    // Aumento de daño para el Caballero con Espadas/Hachas
    @EventHandler
    public void onKnightAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        PlayerProfile profile = professionManager.getProfile(player.getUniqueId());

        if (profile != null && profile.getProfession() == Profession.KNIGHT && profile.getRank() > 0) {
            ItemStack weapon = player.getInventory().getItemInMainHand();
            String name = weapon.getType().name();
            
            // Si usa Espada o Hacha
            if (name.endsWith("_SWORD") || name.endsWith("_AXE")) {
                double bonus = plugin.getConfig().getDouble("professions.knight.weapon_mastery_bonus", 10.0) / 100.0;
                event.setDamage(event.getDamage() + (event.getDamage() * bonus));
            }
        }
    }

    // Resistencia Mágica para el Caballero
    @EventHandler
    public void onKnightMagicDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        PlayerProfile profile = professionManager.getProfile(player.getUniqueId());

        if (profile != null && profile.getProfession() == Profession.KNIGHT && profile.getRank() > 0) {
            EntityDamageEvent.DamageCause cause = event.getCause();
            
            // Causas consideradas "mágicas" en vanilla (el plugin de magia del server normalmente usa estas causas o DAMAGE_ALL)
            if (cause == EntityDamageEvent.DamageCause.MAGIC || 
                cause == EntityDamageEvent.DamageCause.POISON || 
                cause == EntityDamageEvent.DamageCause.DRAGON_BREATH ||
                cause == EntityDamageEvent.DamageCause.WITHER) {
                
                double resistance = plugin.getConfig().getDouble("professions.knight.magic_resistance", 15.0) / 100.0;
                event.setDamage(event.getDamage() - (event.getDamage() * resistance));
            }
        }
    }

    // Usamos PlayerVelocityEvent para evitar deprecaciones estrictas
    @EventHandler
    public void onKnightKnockback(org.bukkit.event.player.PlayerVelocityEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = professionManager.getProfile(player.getUniqueId());

        if (profile != null && profile.getProfession() == Profession.KNIGHT && profile.getRank() > 0) {
            ItemStack chestplate = player.getInventory().getChestplate();
            
            // Si lleva armadura pesada y el vector Y es positivo (normalmente implica empuje por daño)
            if (chestplate != null) {
                String type = chestplate.getType().name();
                if (type.contains("IRON") || type.contains("DIAMOND") || type.contains("NETHERITE")) {
                    // Reducir la velocidad recibida (Knockback)
                    org.bukkit.util.Vector vel = event.getVelocity();
                    event.setVelocity(vel.multiply(0.5));
                }
            }
        }
    }
}
