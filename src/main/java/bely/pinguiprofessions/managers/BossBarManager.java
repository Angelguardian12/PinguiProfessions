package bely.pinguiprofessions.managers;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.models.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class BossBarManager {

    private final Map<UUID, BossBar> playerBars = new HashMap<>();

    public BossBarManager(PinguiProfessions plugin) {
    }

    public void updateBossBar(Player player, PlayerProfile profile) {
        // Solo mostrar BossBar si el jugador está en Rango 0 (Estudiante) y tiene una profesión asignada (en curso)
        if (profile.getRank() == 0 && profile.getProfession() != bely.pinguiprofessions.models.Profession.NONE) {
            BossBar bar = playerBars.get(player.getUniqueId());
            
            String title = ChatColor.translateAlternateColorCodes('&', 
                "&e&lCurso: &f" + profile.getProfession().getDisplayName() + " &7| &aProgreso: " + profile.getXp() + "%");
                
            double progress = profile.getXp() / 100.0;
            if (progress > 1.0) progress = 1.0;
            if (progress < 0.0) progress = 0.0;

            if (bar == null) {
                bar = Bukkit.createBossBar(title, BarColor.YELLOW, BarStyle.SOLID);
                bar.addPlayer(player);
                playerBars.put(player.getUniqueId(), bar);
            } else {
                bar.setTitle(title);
            }
            
            bar.setProgress(progress);
        } else {
            removeBossBar(player);
        }
    }

    public void removeBossBar(Player player) {
        BossBar bar = playerBars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removePlayer(player);
        }
    }
    
    public void removeAll() {
        for (BossBar bar : playerBars.values()) {
            bar.removeAll();
        }
        playerBars.clear();
    }
}
