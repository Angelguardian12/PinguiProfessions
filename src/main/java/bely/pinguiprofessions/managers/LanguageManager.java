package bely.pinguiprofessions.managers;

import bely.pinguiprofessions.PinguiProfessions;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class LanguageManager {

    private final PinguiProfessions plugin;
    private FileConfiguration langConfig;
    private File langFile;

    public LanguageManager(PinguiProfessions plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    public void loadLanguage() {
        // Save defaults if they don't exist
        plugin.saveResource("lang/es.yml", false);
        plugin.saveResource("lang/en.yml", false);

        String langName = plugin.getConfig().getString("language", "es");
        langFile = new File(plugin.getDataFolder(), "lang/" + langName + ".yml");
        
        if (!langFile.exists()) {
            plugin.getLogger().warning("Idioma " + langName + " no encontrado, usando es.yml");
            langFile = new File(plugin.getDataFolder(), "lang/es.yml");
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getMessage(String path) {
        String message = langConfig.getString(path);
        if (message == null) {
            return "&cMessage not found: " + path;
        }
        
        String prefix = langConfig.getString("prefix", "&8[&bPinguiProfessions&8] &7");
        message = message.replace("%prefix%", prefix);
        
        return format(message);
    }

    public String getMessageWithPrefix(String path) {
        return format(langConfig.getString("prefix", "&8[&bPinguiProfessions&8] &7")) + getMessage(path);
    }

    public static String format(String message) {
        if (message == null) return "";
        // Soporte básico para códigos & de colores (para versiones < 1.16) y hex (1.16+)
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            String hexCode = message.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace("&#", "x");
            
            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder("");
            for (char c : ch) {
                builder.append("&").append(c);
            }
            message = message.replace(hexCode, builder.toString());
            matcher = pattern.matcher(message);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
