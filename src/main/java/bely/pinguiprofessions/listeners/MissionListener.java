package bely.pinguiprofessions.listeners;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.LanguageManager;
import bely.pinguiprofessions.managers.MissionsManager;
import bely.pinguiprofessions.managers.ProfessionManager;
import bely.pinguiprofessions.models.PlayerProfile;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

@SuppressWarnings({"deprecation", "unused"})
public class MissionListener implements Listener {

    private final ProfessionManager professionManager;
    private final MissionsManager missionsManager;

    public MissionListener(PinguiProfessions plugin, ProfessionManager professionManager, MissionsManager missionsManager) {
        this.professionManager = professionManager;
        this.missionsManager = missionsManager;
    }

    // Escuchar cuando el jugador toma el resultado de un Yunque
    @EventHandler
    public void onAnvilRename(InventoryClickEvent event) {
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.ANVIL) return;
        if (event.getSlot() != 2) return; // Slot de resultado del yunque

        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType().isAir()) return;

        Player player = (Player) event.getWhoClicked();
        PlayerProfile profile = professionManager.getProfile(player.getUniqueId());

        if (profile == null || profile.getRank() != 0) return; // Solo estudiantes

        // Iterar misiones para ver si cumple alguna de tipo RENAME_ITEM
        for (MissionsManager.Mission mission : missionsManager.getMissions()) {
            if (mission.profession == profile.getProfession() && mission.type.equalsIgnoreCase("RENAME_ITEM")) {
                ConfigurationSection reqs = mission.requirements;
                
                if (reqs == null) continue;

                String reqItem = reqs.getString("item");
                String reqName = reqs.getString("name");
                List<String> reqLore = reqs.getStringList("lore");

                boolean matchItem = reqItem != null && result.getType() == Material.valueOf(reqItem);
                
                ItemMeta meta = result.getItemMeta();
                boolean matchName = true;
                if (reqName != null) {
                    matchName = meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', reqName));
                }

                // Por ahora no verificamos el lore porque el yunque vanilla no puede agregar lore, solo renombrar.
                // Sin embargo, si el item base ya tiene el lore y lo renombra, funcionará.

                if (matchItem && matchName) {
                    // Misión completada
                    profile.addXp(mission.rewardXp);
                    
                    if (profile.getXp() >= 100) {
                        profile.setXp(0);
                        profile.setRank(1);
                        player.sendMessage(LanguageManager.format("&a¡Has completado tu formación final y ahora eres oficialmente &e" + mission.profession.getDisplayName() + "&a!"));
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    } else {
                        player.sendMessage(LanguageManager.format("&a¡Misión completada! &e+" + mission.rewardXp + " XP"));
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    }
                    return; // Misión procesada
                }
            }
        }
    }
}
