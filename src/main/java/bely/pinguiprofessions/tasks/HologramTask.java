package bely.pinguiprofessions.tasks;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.CourseManager;
import bely.pinguiprofessions.managers.ProfessionManager;
import bely.pinguiprofessions.models.PlayerProfile;
import bely.pinguiprofessions.models.Profession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class HologramTask extends BukkitRunnable {

    private final PinguiProfessions plugin;
    private final CourseManager courseManager;
    private final ProfessionManager professionManager;
    
    // Rastrea qué jugador está viendo qué hologramas
    private final Map<UUID, Map<Location, TextDisplay>> personalHolograms = new HashMap<>();

    public HologramTask(PinguiProfessions plugin, CourseManager courseManager, ProfessionManager professionManager) {
        this.plugin = plugin;
        this.courseManager = courseManager;
        this.professionManager = professionManager;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = professionManager.getProfile(player.getUniqueId());
            if (profile == null) continue;

            Profession zoneProf = null;
            if (org.bukkit.Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
                zoneProf = bely.pinguiprofessions.utils.WGUtils.getProfessionZone(player);
            }

            // Fallback si no hay WG: buscar si hay bloques cerca (radio 10 bloques, distanceSquared 100)
            if (zoneProf == null) {
                Location playerLoc = player.getLocation();
                double nearestDist = 100.0;
                for (Map.Entry<Profession, List<Location>> entry : courseManager.getCourseBlocks().entrySet()) {
                    for (Location loc : entry.getValue()) {
                        if (loc.getWorld().equals(playerLoc.getWorld())) {
                            double dist = loc.distanceSquared(playerLoc);
                            if (dist < nearestDist) {
                                nearestDist = dist;
                                zoneProf = entry.getKey();
                            }
                        }
                    }
                }
            }

            Map<Location, TextDisplay> displays = personalHolograms.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

            if (zoneProf != null) {
                List<Location> blocks = courseManager.getCourseBlocks().get(zoneProf);
                if (blocks != null) {
                    // Remover displays de bloques que ya no están en la lista
                    displays.entrySet().removeIf(entry -> {
                        if (!blocks.contains(entry.getKey())) {
                            entry.getValue().remove();
                            return true;
                        }
                        return false;
                    });
                    
                    // Crear/Actualizar displays para los bloques actuales
                    for (Location loc : blocks) {
                        TextDisplay display = displays.get(loc);
                        if (display == null || !display.isValid()) {
                            Location holoLoc = loc.clone().add(0.5, 1.2, 0.5);
                            display = player.getWorld().spawn(holoLoc, TextDisplay.class, d -> {
                                d.setVisibleByDefault(false);
                                d.setBillboard(Display.Billboard.CENTER);
                                d.setDefaultBackground(false);
                                d.setShadowed(true);
                            });
                            player.showEntity(plugin, display);
                            displays.put(loc, display);
                        }
                        updateHologramText(player, display, loc, zoneProf, profile);
                    }
                }
            } else {
                // Eliminar todos si no está en zona
                for (TextDisplay d : displays.values()) {
                    if (d != null) d.remove();
                }
                displays.clear();
            }

            // BossBar
            if (zoneProf != null && (profile.getProfession() == Profession.NONE || profile.getProfession() == zoneProf)) {
                plugin.getBossBarManager().updateBossBar(player, profile);
            } else {
                plugin.getBossBarManager().removeBossBar(player);
            }
        }
    }

    private void updateHologramText(Player player, TextDisplay display, Location loc, Profession courseProf, PlayerProfile profile) {
        String text;
        
        if (profile.getProfession() == courseProf && profile.getRank() > 0) {
            text = ChatColor.translateAlternateColorCodes('&', "&aYa eres " + courseProf.getDisplayName());
        } else if (profile.getProfession() != Profession.NONE && profile.getProfession() != courseProf) {
            text = ChatColor.translateAlternateColorCodes('&', "&cYa estás ejerciendo otra profesión.\n&7(/profesiones abandonar)");
        } else {
            String serializedLoc = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
            boolean completed = profile.getCompletedBlocks().contains(serializedLoc);
            
            String status = completed ? "&a[Completado]" : "&c[Pendiente]";
            int totalBlocks = courseManager.getCourseBlocks().get(courseProf).size();
            int progress = totalBlocks == 0 ? 0 : (profile.getCompletedBlocks().size() * 100) / totalBlocks;
            
            text = ChatColor.translateAlternateColorCodes('&', 
                "&e&l" + courseProf.getDisplayName() + "\n" +
                status + "\n" +
                "&fProgreso: &b" + progress + "% / 100%"
            );
        }
        
        display.setText(text);
    }
}
