package bely.pinguiprofessions.listeners;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.LanguageManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class ExpListener implements Listener {

    private final PinguiProfessions plugin;

    public ExpListener(PinguiProfessions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUseExpBottle(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() != Material.EXPERIENCE_BOTTLE) return;
        
        if (!item.hasItemMeta()) return;
        
        NamespacedKey key = new NamespacedKey(plugin, "extracted_exp");
        if (item.getItemMeta().getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
            event.setCancelled(true); // Prevenir que se lance la botella vanilla
            
            int xp = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
            
            // Consumir el item
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            
            // Dar XP
            player.giveExp(xp);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
            player.sendMessage(LanguageManager.format("&aHas recuperado &b" + xp + " &apuntos de experiencia."));
        }
    }
}
