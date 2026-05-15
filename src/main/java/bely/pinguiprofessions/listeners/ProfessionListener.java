package bely.pinguiprofessions.listeners;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.LanguageManager;
import bely.pinguiprofessions.managers.ProfessionManager;
import bely.pinguiprofessions.models.PlayerProfile;
import bely.pinguiprofessions.models.Profession;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class ProfessionListener implements Listener {

    private final PinguiProfessions plugin;
    private final ProfessionManager professionManager;
    private final Random random = new Random();

    public ProfessionListener(PinguiProfessions plugin, ProfessionManager professionManager) {
        this.plugin = plugin;
        this.professionManager = professionManager;
    }

    @EventHandler
    public void onBlacksmithCraft(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        if (result == null || result.getType().isAir()) return;

        // Verificar si es un ítem de Herrería (Armaduras, Espadas, Hachas de Hierro/Diamante/Netherite)
        if (isBlacksmithItem(result.getType())) {
            Player player = (Player) event.getWhoClicked();
            PlayerProfile profile = professionManager.getProfile(player.getUniqueId());

            if (profile == null) return;

            // Si NO es herrero y no tiene bypass, bloquear el crafteo de estos ítems
            if (profile.getProfession() != Profession.BLACKSMITH && !player.hasPermission("pinguiprofessions.admin.bypass")) {
                event.setCancelled(true);
                player.sendMessage(LanguageManager.format("&cSolo un Herrero puede forjar este equipamiento."));
                return;
            }

            // Es herrero, aplicar la probabilidad de fallo dinámica
            int baseFailChance = plugin.getConfig().getInt("professions.blacksmith.fail_chance_apprentice", 40);
            int minFailChance = plugin.getConfig().getInt("professions.blacksmith.fail_chance_master", 0);
            int failChance = Math.max(minFailChance, baseFailChance - (profile.getXp() / 10));
            
            // Si el random es menor al % de fallo, se rompe
            if (failChance > 0 && random.nextInt(100) < failChance) {
                event.setCancelled(true);
                
                // Consumir solo el porcentaje configurado de materiales
                int lossPercentage = plugin.getConfig().getInt("professions.blacksmith.material_loss_percentage", 25);
                ItemStack[] matrix = event.getInventory().getMatrix();
                
                int totalItems = 0;
                for (ItemStack item : matrix) {
                    if (item != null && !item.getType().isAir()) totalItems++;
                }
                
                int itemsToDestroy = Math.max(1, (int) Math.ceil(totalItems * (lossPercentage / 100.0)));
                int destroyed = 0;
                
                for (int i = 0; i < matrix.length; i++) {
                    if (matrix[i] != null && !matrix[i].getType().isAir()) {
                        if (destroyed < itemsToDestroy) {
                            matrix[i].setAmount(matrix[i].getAmount() - 1);
                            if (matrix[i].getAmount() <= 0) {
                                matrix[i] = null;
                            }
                            destroyed++;
                        }
                    }
                }
                event.getInventory().setMatrix(matrix);

                // Efectos visuales de fallo
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1.0f, 0.8f);
                player.spawnParticle(Particle.LARGE_SMOKE, player.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0.05);
                player.sendMessage(LanguageManager.format(plugin.getConfig().getString("lang.blacksmith_craft_fail", "&c¡Tu intento de forja ha fallado! Has perdido algunos materiales. &7(" + failChance + "% prob.)")));
            } else {
                // Éxito: Dar algo de experiencia de profesión
                profile.addXp(5);
                player.sendMessage(LanguageManager.format("&a¡Forja exitosa! &7(+5 XP)"));
            }
        }
    }

    private boolean isBlacksmithItem(Material material) {
        String name = material.name();
        return (name.contains("DIAMOND_") || name.contains("NETHERITE_")) &&
               (name.endsWith("_SWORD") || name.endsWith("_AXE") || name.endsWith("_HELMET") || 
                name.endsWith("_CHESTPLATE") || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS"));
    }
}
