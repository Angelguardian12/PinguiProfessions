package bely.pinguiprofessions.managers;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.models.Profession;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class MissionsManager {

    private final PinguiProfessions plugin;
    private File file;
    private FileConfiguration config;
    private final List<Mission> missions = new ArrayList<>();

    public MissionsManager(PinguiProfessions plugin) {
        this.plugin = plugin;
        loadFile();
    }

    private void loadFile() {
        file = new File(plugin.getDataFolder(), "missions.yml");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                // Create default example
                plugin.saveResource("missions.yml", false);
            } catch (Exception e) {
                plugin.getLogger().severe("No se pudo crear missions.yml");
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        loadMissions();
    }

    private void loadMissions() {
        missions.clear();
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section != null) {
                try {
                    Profession prof = Profession.valueOf(section.getString("profession", "NONE").toUpperCase());
                    String type = section.getString("type", "UNKNOWN");
                    int rewardXp = section.getInt("reward_xp", 20);
                    
                    Mission mission = new Mission(key, prof, type, rewardXp, section.getConfigurationSection("requirements"));
                    missions.add(mission);
                } catch (Exception e) {
                    plugin.getLogger().warning("Error cargando misión: " + key);
                }
            }
        }
        plugin.getLogger().info("Cargadas " + missions.size() + " misiones.");
    }

    public List<Mission> getMissions() {
        return missions;
    }

    public static class Mission {
        public final String id;
        public final Profession profession;
        public final String type;
        public final int rewardXp;
        public final ConfigurationSection requirements;

        public Mission(String id, Profession profession, String type, int rewardXp, ConfigurationSection requirements) {
            this.id = id;
            this.profession = profession;
            this.type = type;
            this.rewardXp = rewardXp;
            this.requirements = requirements;
        }
    }
}
