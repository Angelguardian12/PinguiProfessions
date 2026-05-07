package bely.pinguiprofessions.commands;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.LanguageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ProfesionesCommand implements CommandExecutor {

    private final PinguiProfessions plugin;
    private final LanguageManager languageManager;

    public ProfesionesCommand(PinguiProfessions plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("pinguiprofessions.admin")) {
                sender.sendMessage(languageManager.getMessageWithPrefix("no_permission"));
                return true;
            }
            
            plugin.reloadConfig();
            languageManager.loadLanguage();
            sender.sendMessage(languageManager.getMessageWithPrefix("reloaded"));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            if (!sender.hasPermission("pinguiprofessions.admin")) {
                sender.sendMessage(languageManager.getMessageWithPrefix("no_permission"));
                return true;
            }
            if (sender instanceof Player) {
                plugin.getAdminGUI().openAdminMenu((Player) sender);
            } else {
                sender.sendMessage("§cEste comando es solo para jugadores.");
            }
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(languageManager.getMessageWithPrefix("only_players"));
            return true;
        }

        Player player = (Player) sender;
        plugin.getPlayerGUI().openMenu(player);

        return true;
    }
}
