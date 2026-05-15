package bely.pinguiprofessions.managers;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.database.DatabaseManager;
import bely.pinguiprofessions.models.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class ProfessionManager {

    private final PinguiProfessions plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, PlayerProfile> cache = new HashMap<>();

    public ProfessionManager(PinguiProfessions plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        registerMedicalKitRecipe();
    }

    public void loadProfile(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerProfile profile = databaseManager.loadPlayer(player.getUniqueId());
            Bukkit.getScheduler().runTask(plugin, () -> {
                cache.put(player.getUniqueId(), profile);
                
                // Sincronización con LuckPerms para rangos comprados/administrativos
                if (profile.getProfession() == bely.pinguiprofessions.models.Profession.NONE) {
                    bely.pinguiprofessions.models.Profession detectedProf = null;
                    if (player.hasPermission("group.herrero")) detectedProf = bely.pinguiprofessions.models.Profession.BLACKSMITH;
                    else if (player.hasPermission("group.doctor")) detectedProf = bely.pinguiprofessions.models.Profession.DOCTOR;
                    else if (player.hasPermission("group.alquimista")) detectedProf = bely.pinguiprofessions.models.Profession.ALCHEMIST;
                    else if (player.hasPermission("group.tabernero")) detectedProf = bely.pinguiprofessions.models.Profession.BARKEEP;
                    else if (player.hasPermission("group.caballero")) detectedProf = bely.pinguiprofessions.models.Profession.KNIGHT;
                    else if (player.hasPermission("group.comerciante")) detectedProf = bely.pinguiprofessions.models.Profession.MERCHANT;
                    else if (player.hasPermission("group.ladron")) detectedProf = bely.pinguiprofessions.models.Profession.THIEF;
                    else if (player.hasPermission("group.comisario")) detectedProf = bely.pinguiprofessions.models.Profession.INVESTIGATOR;

                    if (detectedProf != null) {
                        profile.setProfession(detectedProf);
                        profile.setRank(1);
                        profile.setXp(1000); // 1000 XP for immunity against penalties
                        plugin.getLogger().info("Sincronizado jugador VIP " + player.getName() + " con profesión " + detectedProf.name());
                    }
                }
            });
        });
    }

    public void unloadProfile(Player player) {
        PlayerProfile profile = cache.remove(player.getUniqueId());
        if (profile != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> databaseManager.savePlayer(profile));
        }
    }

    public void saveAll() {
        for (PlayerProfile profile : cache.values()) {
            databaseManager.savePlayer(profile);
        }
    }

    public PlayerProfile getProfile(UUID uuid) {
        return cache.get(uuid);
    }

    public PlayerProfile getProfileOffline(UUID uuid) {
        PlayerProfile profile = cache.get(uuid);
        if (profile != null) {
            return profile;
        }
        // Consulta síncrona a la DB para verificación rápida
        return databaseManager.loadPlayer(uuid);
    }

    private void registerMedicalKitRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "kit_medico");
        
        // Evitar registrar dos veces en recargas de servidor
        if (Bukkit.getRecipe(key) == null) {
            ItemStack medicalKit = new ItemStack(Material.KNOWLEDGE_BOOK);
            ItemMeta meta = medicalKit.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(LanguageManager.format("&aKit Médico"));
                List<String> lore = new ArrayList<>();
                lore.add(LanguageManager.format("&7Reanima a un jugador inconsciente"));
                lore.add(LanguageManager.format("&7Se consume al usarlo"));
                meta.setLore(lore);
                medicalKit.setItemMeta(meta);
            }

            ShapedRecipe recipe = new ShapedRecipe(key, medicalKit);
            recipe.shape("SPS", "PPP", "SPS");
            recipe.setIngredient('S', Material.WHEAT_SEEDS);
            recipe.setIngredient('P', Material.PAPER);
            Bukkit.addRecipe(recipe);
            plugin.getLogger().info("Receta de Kit Médico registrada.");
        }

        // Registrar Poción de Maná
        if (plugin.getConfig().getBoolean("use-mana", true)) {
            NamespacedKey manaKey = new NamespacedKey(plugin, "pocion_mana");
            if (Bukkit.getRecipe(manaKey) == null) {
                ItemStack manaPotion = new ItemStack(Material.POTION);
                org.bukkit.inventory.meta.PotionMeta pMeta = (org.bukkit.inventory.meta.PotionMeta) manaPotion.getItemMeta();
                if (pMeta != null) {
                    pMeta.setDisplayName("§b§lPoción de Maná");
                    pMeta.setColor(org.bukkit.Color.AQUA);
                    List<String> lore = new ArrayList<>();
                    lore.add("§7Restaura §b50 puntos de Magia");
                    lore.add("§7Hecho por Alquimistas.");
                    pMeta.setLore(lore);
                    manaPotion.setItemMeta(pMeta);
                }

                ShapedRecipe manaRecipe = new ShapedRecipe(manaKey, manaPotion);
                manaRecipe.shape(" L ", " G ", " W ");
                manaRecipe.setIngredient('L', Material.LAPIS_LAZULI);
                manaRecipe.setIngredient('G', Material.GLOWSTONE_DUST);
                manaRecipe.setIngredient('W', Material.GLASS_BOTTLE);
                Bukkit.addRecipe(manaRecipe);
                plugin.getLogger().info("Receta de Poción de Maná registrada.");
            }
        }
    }
}
