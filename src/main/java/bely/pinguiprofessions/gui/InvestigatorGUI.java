package bely.pinguiprofessions.gui;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.ReportManager;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("deprecation")
public class InvestigatorGUI implements Listener {

    private final PinguiProfessions plugin;

    public InvestigatorGUI(PinguiProfessions plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.DARK_BLUE + "Buzón de Casos");

        List<ReportManager.Report> reports = plugin.getReportManager().getActiveReports();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        for (int i = 0; i < Math.min(reports.size(), 54); i++) {
            ReportManager.Report report = reports.get(i);
            
            ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§cReporte #" + (i + 1));
                List<String> lore = new ArrayList<>();
                lore.add("§7Acusado: §e" + report.target);
                lore.add("§7Denunciante: §f" + report.reporter);
                lore.add("§7Hora: §8" + sdf.format(new Date(report.timestamp)));
                lore.add("");
                lore.add("§7Motivo:");
                
                // Dividir motivo largo en múltiples líneas
                String reason = report.reason;
                int maxLineLength = 30;
                for (int j = 0; j < reason.length(); j += maxLineLength) {
                    lore.add("§f" + reason.substring(j, Math.min(j + maxLineLength, reason.length())));
                }
                
                lore.add("");
                lore.add("§a[Clic Izquierdo] §7para investigar.");
                lore.add("§c[Clic Derecho] §7para cerrar el caso.");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inv.setItem(i, item);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(ChatColor.DARK_BLUE + "Buzón de Casos")) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        List<ReportManager.Report> reports = plugin.getReportManager().getActiveReports();
        if (slot >= 0 && slot < reports.size()) {
            ReportManager.Report report = reports.get(slot);
            
            if (event.isRightClick()) {
                // Cerrar caso
                plugin.getReportManager().removeReport(report);
                openMenu(player); // Recargar
                player.sendMessage("§aCaso cerrado.");
            } else if (event.isLeftClick()) {
                // Iniciar investigación (por ahora solo notifica)
                player.closeInventory();
                player.sendMessage("§eHas iniciado la investigación contra §c" + report.target + "§e.");
                player.sendMessage("§7Pista: Busca indicios (papeles ensangrentados) donde se le vio por última vez.");
            }
        }
    }
}
