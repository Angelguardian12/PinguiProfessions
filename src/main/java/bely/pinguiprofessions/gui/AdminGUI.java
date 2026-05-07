package bely.pinguiprofessions.gui;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.CourseManager;
import bely.pinguiprofessions.models.Profession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("deprecation")
public class AdminGUI implements Listener {

    private final CourseManager courseManager;
    private final Map<Player, Location> pendingRegistrations = new HashMap<>();

    public AdminGUI(PinguiProfessions plugin, CourseManager courseManager) {
        this.courseManager = courseManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openAdminMenu(Player player) {
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "Debes estar mirando a un bloque (a menos de 5 bloques) para registrarlo.");
            return;
        }

        pendingRegistrations.put(player, targetBlock.getLocation());

        Inventory inv = Bukkit.createInventory(null, 27, ChatColor.DARK_RED + "Registrar Curso");

        inv.setItem(10, createGuiItem(Material.ANVIL, ChatColor.YELLOW + "Herrero", ChatColor.GRAY + "Registrar como curso de herrería"));
        inv.setItem(11, createGuiItem(Material.BREWING_STAND, ChatColor.YELLOW + "Doctor", ChatColor.GRAY + "Registrar como curso médico"));
        inv.setItem(12, createGuiItem(Material.GLASS_BOTTLE, ChatColor.YELLOW + "Alquimista", ChatColor.GRAY + "Registrar como curso de alquimia"));
        inv.setItem(14, createGuiItem(Material.POTION, ChatColor.YELLOW + "Tabernero", ChatColor.GRAY + "Registrar como curso de tabernero"));
        inv.setItem(15, createGuiItem(Material.IRON_SWORD, ChatColor.YELLOW + "Caballero", ChatColor.GRAY + "Registrar como arena de caballero"));
        inv.setItem(16, createGuiItem(Material.CHEST, ChatColor.YELLOW + "Comerciante", ChatColor.GRAY + "Registrar como curso de comerciante"));

        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(java.util.Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_RED + "Registrar Curso")) return;

        event.setCancelled(true); // Evitar llevarse los ítems

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Location targetLocation = pendingRegistrations.get(player);
        if (targetLocation == null) {
            player.closeInventory();
            return;
        }

        Profession selectedProf = null;

        switch (clickedItem.getType()) {
            case ANVIL: selectedProf = Profession.BLACKSMITH; break;
            case BREWING_STAND: selectedProf = Profession.DOCTOR; break;
            case GLASS_BOTTLE: selectedProf = Profession.ALCHEMIST; break;
            case POTION: selectedProf = Profession.BARKEEP; break;
            case IRON_SWORD: selectedProf = Profession.KNIGHT; break;
            case CHEST: selectedProf = Profession.MERCHANT; break;
            default: break;
        }

        if (selectedProf != null) {
            courseManager.saveCourseBlock(selectedProf, targetLocation);
            player.sendMessage(ChatColor.GREEN + "¡Bloque registrado exitosamente para el curso de " + selectedProf.getDisplayName() + "!");
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
            player.closeInventory();
            pendingRegistrations.remove(player);
        }
    }
}
