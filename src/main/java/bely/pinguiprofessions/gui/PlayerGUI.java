package bely.pinguiprofessions.gui;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.LanguageManager;
import bely.pinguiprofessions.managers.ProfessionManager;
import bely.pinguiprofessions.models.PlayerProfile;
import bely.pinguiprofessions.models.Profession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

@SuppressWarnings("deprecation")
public class PlayerGUI implements Listener {

    private final PinguiProfessions plugin;
    private final ProfessionManager professionManager;

    public PlayerGUI(PinguiProfessions plugin, ProfessionManager professionManager) {
        this.plugin = plugin;
        this.professionManager = professionManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player) {
        PlayerProfile profile = professionManager.getProfile(player.getUniqueId());
        if (profile == null) return;

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_AQUA + "Tu Profesión");

        // Ítem de información de estado
        if (profile.getProfession() == Profession.NONE) {
            inv.setItem(13, createGuiItem(Material.MAP, ChatColor.YELLOW + "Sin Profesión", 
                ChatColor.GRAY + "Aún no has elegido un camino.",
                ChatColor.GRAY + "Visita la escuela de la ciudad para",
                ChatColor.GRAY + "comenzar un curso interactuando",
                ChatColor.GRAY + "con los bloques holográficos."
            ));
        } else {
            String rankName = profile.getRank() == 0 ? "Estudiante" : "Oficial";
            inv.setItem(13, createGuiItem(Material.NETHER_STAR, ChatColor.GOLD + profile.getProfession().getDisplayName(), 
                ChatColor.GRAY + "Rango: " + ChatColor.WHITE + rankName,
                ChatColor.GRAY + "Experiencia: " + ChatColor.WHITE + profile.getXp() + " XP"
            ));
            
            if (profile.getProfession() == Profession.INVESTIGATOR) {
                inv.setItem(22, createGuiItem(Material.WRITABLE_BOOK, ChatColor.DARK_BLUE + "Buzón de Casos", 
                    ChatColor.GRAY + "Revisa los reportes activos",
                    ChatColor.GRAY + "de la ciudad."
                ));
            }
            
            // Botón de abandonar
            inv.setItem(26, createGuiItem(Material.BARRIER, ChatColor.RED + "Abandonar Profesión", 
                ChatColor.GRAY + "Haz clic aquí para renunciar",
                ChatColor.GRAY + "a tu profesión actual.",
                ChatColor.DARK_RED + "¡Perderás todo tu progreso!"
            ));
        }

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_AQUA + "Tu Profesión")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType().isAir()) return;

        if (clicked.getType() == Material.WRITABLE_BOOK) {
            player.closeInventory();
            plugin.getInvestigatorGUI().openMenu(player);
            return;
        }

        if (clicked.getType() == Material.BARRIER) {
            PlayerProfile profile = professionManager.getProfile(player.getUniqueId());
            if (profile != null && profile.getProfession() != Profession.NONE) {
                profile.setProfession(Profession.NONE);
                profile.setRank(0);
                profile.setXp(0);
                
                player.closeInventory();
                plugin.getBossBarManager().removeBossBar(player);
                
                player.sendMessage(LanguageManager.format(plugin.getConfig().getString("lang.profession_abandoned", "&cHas abandonado tu profesión. Todo tu progreso se ha perdido.")));
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1f, 0.5f);
            }
        }
    }
}
