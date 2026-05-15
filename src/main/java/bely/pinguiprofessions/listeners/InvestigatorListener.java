package bely.pinguiprofessions.listeners;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.ProfessionManager;
import bely.pinguiprofessions.models.PlayerProfile;
import bely.pinguiprofessions.models.Profession;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class InvestigatorListener implements Listener {

    private final PinguiProfessions plugin;
    private final ProfessionManager professionManager;
    private final NamespacedKey thiefKey;

    public InvestigatorListener(PinguiProfessions plugin, ProfessionManager professionManager) {
        this.plugin = plugin;
        this.professionManager = professionManager;
        this.thiefKey = new NamespacedKey(plugin, "thief_identity");
    }

    // Ocultar las pistas existentes a los nuevos jugadores si no son comisarios
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = professionManager.getProfile(player.getUniqueId());
        
        if ((profile == null || profile.getProfession() != Profession.INVESTIGATOR) && !player.hasPermission("pinguiprofessions.admin.bypass")) {
            for (Item item : player.getWorld().getEntitiesByClass(Item.class)) {
                ItemStack itemStack = item.getItemStack();
                if (itemStack.hasItemMeta() && itemStack.getItemMeta().getPersistentDataContainer().has(thiefKey, PersistentDataType.STRING)) {
                    player.hideEntity(plugin, item);
                }
            }
        }
    }

    // Recoger la pista y transformarla en Evidencia encriptada
    @EventHandler
    public void onPickupClue(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        
        Player player = (Player) event.getEntity();
        ItemStack itemStack = event.getItem().getItemStack();
        
        if (itemStack.hasItemMeta() && itemStack.getItemMeta().getPersistentDataContainer().has(thiefKey, PersistentDataType.STRING)) {
            PlayerProfile profile = professionManager.getProfile(player.getUniqueId());
            
            // Si por alguna razón la ve alguien más, cancelar
            if ((profile == null || profile.getProfession() != Profession.INVESTIGATOR) && !player.hasPermission("pinguiprofessions.admin.bypass")) {
                event.setCancelled(true);
                return;
            }

            // Es el comisario. Transformar la pista en "Evidencia"
            String thiefName = itemStack.getItemMeta().getPersistentDataContainer().get(thiefKey, PersistentDataType.STRING);
            String encryption = plugin.getConfig().getString("professions.investigator.encryption_type", "MORSE");
            String encodedName = encode(thiefName, encryption);

            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName("§bEvidencia Recolectada");
            List<String> lore = new ArrayList<>();
            lore.add("§7Identidad sospechosa encriptada:");
            lore.add("§e" + encodedName);
            lore.add("");
            lore.add("§7Cifrado: §f" + encryption);
            lore.add("§7(Sostén esto en tu mano secundaria");
            lore.add("§7y usa una Brújula para rastrearlo)");
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
        }
    }

    // Rastreador (Uso de la brújula)
    @EventHandler
    public void onUseTracker(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT")) return;
        
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        if (mainHand.getType() == Material.COMPASS && offHand.hasItemMeta()) {
            if (offHand.getItemMeta().getPersistentDataContainer().has(thiefKey, PersistentDataType.STRING)) {
                PlayerProfile profile = professionManager.getProfile(player.getUniqueId());
                if ((profile != null && profile.getProfession() == Profession.INVESTIGATOR && profile.getRank() > 0) || player.hasPermission("pinguiprofessions.admin.bypass")) {
                    
                    String thiefName = offHand.getItemMeta().getPersistentDataContainer().get(thiefKey, PersistentDataType.STRING);
                    Player thief = plugin.getServer().getPlayer(thiefName);
                    
                    if (thief != null && thief.isOnline()) {
                        player.setCompassTarget(thief.getLocation());
                        player.sendMessage("§a[!] Brújula sincronizada. Siguiendo el rastro de la evidencia...");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.5f);
                        
                        // Efecto visual en la brújula
                        ItemMeta compassMeta = mainHand.getItemMeta();
                        compassMeta.setDisplayName("§cRastreador Activo");
                        mainHand.setItemMeta(compassMeta);
                    } else {
                        player.sendMessage("§cEl sospechoso asociado a esta evidencia no está cerca o huyó de la ciudad (Offline).");
                    }
                }
            }
        }
    }

    // Helpers de encriptación
    private String encode(String text, String type) {
        if (text == null) return "UNKNOWN";
        text = text.toUpperCase();
        if (type.equalsIgnoreCase("CAESAR")) {
            StringBuilder result = new StringBuilder();
            int shift = 3; // Desplazamiento típico de César
            for (char c : text.toCharArray()) {
                if (c >= 'A' && c <= 'Z') {
                    char shifted = (char) (((c - 'A' + shift) % 26) + 'A');
                    result.append(shifted);
                } else {
                    result.append(c);
                }
            }
            return result.toString();
        } else {
            // MORSE default
            String[] morseAlphabet = { ".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--.." };
            StringBuilder result = new StringBuilder();
            for (char c : text.toCharArray()) {
                if (c >= 'A' && c <= 'Z') {
                    result.append(morseAlphabet[c - 'A']).append(" ");
                } else if (c == ' ') {
                    result.append("/ ");
                } else {
                    result.append(c).append(" ");
                }
            }
            return result.toString().trim();
        }
    }
}
