package bely.pinguiprofessions.commands;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.LanguageManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ExpCommand implements CommandExecutor {

    private final PinguiProfessions plugin;

    public ExpCommand(PinguiProfessions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Comando solo para jugadores.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(LanguageManager.format("&cUso: /extraerxp <puntos>"));
            return true;
        }

        int pointsToExtract;
        try {
            pointsToExtract = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(LanguageManager.format("&cLa cantidad debe ser un número válido."));
            return true;
        }

        if (pointsToExtract <= 0) {
            player.sendMessage(LanguageManager.format("&cLa cantidad debe ser mayor a 0."));
            return true;
        }

        int totalExp = getTotalExperience(player);
        if (totalExp < pointsToExtract) {
            player.sendMessage(LanguageManager.format("&cNo tienes suficientes puntos de experiencia."));
            return true;
        }

        // Restar experiencia
        setTotalExperience(player, totalExp - pointsToExtract);

        // Crear la botella custom
        ItemStack expBottle = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = expBottle.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a&lBotella de Experiencia Concentrada"));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', "&7Puntos: &b" + pointsToExtract));
            lore.add(ChatColor.translateAlternateColorCodes('&', "&e¡Úsala para recuperar la XP o dásela a un Herrero!"));
            meta.setLore(lore);

            // Marca de seguridad PDC para evitar falsificaciones
            NamespacedKey key = new NamespacedKey(plugin, "extracted_exp");
            meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, pointsToExtract);

            expBottle.setItemMeta(meta);
        }

        player.getInventory().addItem(expBottle);
        player.sendMessage(LanguageManager.format("&aHas extraído &b" + pointsToExtract + " &apuntos de experiencia."));

        return true;
    }

    // Métodos para calcular la experiencia total con precisión
    private int getTotalExperience(Player player) {
        int level = player.getLevel();
        int exp = 0;
        if (level >= 0 && level <= 15) {
            exp = (int) (Math.pow(level, 2) + 6 * level);
        } else if (level >= 16 && level <= 30) {
            exp = (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360);
        } else if (level >= 31) {
            exp = (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
        }
        return Math.round(exp + player.getExp() * player.getExpToLevel());
    }

    private void setTotalExperience(Player player, int amount) {
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);
        player.giveExp(amount);
    }
}
