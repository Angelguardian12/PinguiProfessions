package bely.pinguiprofessions.commands;

import bely.pinguiprofessions.PinguiProfessions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportCommand implements CommandExecutor {

    private final PinguiProfessions plugin;

    public ReportCommand(PinguiProfessions plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cSolo jugadores pueden usar este comando.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2) {
            player.sendMessage("§cUso correcto: /reportar <jugador> <motivo...>");
            return true;
        }

        String target = args[0];
        StringBuilder reason = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            reason.append(args[i]).append(" ");
        }

        plugin.getReportManager().addReport(player.getName(), target, reason.toString().trim());
        player.sendMessage("§aTu reporte ha sido enviado a la comisaría. Los comisarios lo investigarán pronto.");
        return true;
    }
}
