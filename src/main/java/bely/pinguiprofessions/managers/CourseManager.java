package bely.pinguiprofessions.managers;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.models.Profession;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseManager {

    private final PinguiProfessions plugin;
    private File file;
    private FileConfiguration config;
    
    // Lista de bloques registrados para cada profesión
    private final Map<Profession, List<Location>> courseBlocks = new HashMap<>();

    public CourseManager(PinguiProfessions plugin) {
        this.plugin = plugin;
        for (Profession prof : Profession.values()) {
            courseBlocks.put(prof, new ArrayList<>());
        }
        loadFile();
    }

    private void loadFile() {
        file = new File(plugin.getDataFolder(), "courses.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("No se pudo crear courses.yml");
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        
        // Cargar locaciones
        ConfigurationSection section = config.getConfigurationSection("locations");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String profStr = section.getString(key + ".profession");
                String worldName = section.getString(key + ".world");
                double x = section.getDouble(key + ".x");
                double y = section.getDouble(key + ".y");
                double z = section.getDouble(key + ".z");
                
                World world = Bukkit.getWorld(worldName);
                if (world != null && profStr != null) {
                    try {
                        Profession prof = Profession.valueOf(profStr.toUpperCase());
                        Location loc = new Location(world, x, y, z);
                        courseBlocks.get(prof).add(loc);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
    }

    public void saveCourseBlock(Profession profession, Location location) {
        List<Location> locs = courseBlocks.get(profession);
        if (!locs.contains(location)) {
            locs.add(location);
            
            // Guardar en archivo
            String key = "locations.loc_" + System.currentTimeMillis();
            config.set(key + ".profession", profession.name());
            config.set(key + ".world", location.getWorld().getName());
            config.set(key + ".x", location.getX());
            config.set(key + ".y", location.getY());
            config.set(key + ".z", location.getZ());
            
            try {
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<Profession, List<Location>> getCourseBlocks() {
        return courseBlocks;
    }

    public void removeCourseBlock(Location location) {
        for (Map.Entry<Profession, List<Location>> entry : courseBlocks.entrySet()) {
            if (entry.getValue().remove(location)) {
                ConfigurationSection section = config.getConfigurationSection("locations");
                if (section != null) {
                    for (String key : section.getKeys(false)) {
                        double x = section.getDouble(key + ".x");
                        double y = section.getDouble(key + ".y");
                        double z = section.getDouble(key + ".z");
                        String worldName = section.getString(key + ".world");
                        if (x == location.getX() && y == location.getY() && z == location.getZ() && location.getWorld().getName().equals(worldName)) {
                            config.set("locations." + key, null);
                            try {
                                config.save(file);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
                break;
            }
        }
    }
}
