package bely.pinguiprofessions.listeners;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.CourseManager;
import bely.pinguiprofessions.managers.LanguageManager;
import bely.pinguiprofessions.managers.ProfessionManager;
import bely.pinguiprofessions.models.PlayerProfile;
import bely.pinguiprofessions.models.Profession;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"deprecation", "unused"})
public class PlayerListener implements Listener {

    private final ProfessionManager professionManager;
    private final CourseManager courseManager;
    private final bely.pinguiprofessions.managers.ClanIntegration clanIntegration;

    public PlayerListener(PinguiProfessions plugin, ProfessionManager professionManager, CourseManager courseManager, bely.pinguiprofessions.managers.ClanIntegration clanIntegration) {
        this.professionManager = professionManager;
        this.courseManager = courseManager;
        this.clanIntegration = clanIntegration;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        professionManager.loadProfile(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        professionManager.unloadProfile(event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        for (List<Location> locs : courseManager.getCourseBlocks().values()) {
            if (locs.contains(loc)) {
                courseManager.removeCourseBlock(loc);
                event.getPlayer().sendMessage(LanguageManager.format("&eHas eliminado un bloque de curso registrado."));
                break;
            }
        }
    }

    @EventHandler
    public void onCraftMedicalKit(CraftItemEvent event) {
        ItemStack result = event.getRecipe().getResult();
        if (result.hasItemMeta() && result.getItemMeta().hasDisplayName() &&
            result.getItemMeta().getDisplayName().contains("Kit Médico")) {
            
            Player player = (Player) event.getWhoClicked();
            PlayerProfile profile = professionManager.getProfile(player.getUniqueId());
            
            if (profile == null || profile.getProfession() != Profession.DOCTOR) {
                event.setCancelled(true);
                player.sendMessage(LanguageManager.format("&cTus manos no poseen el conocimiento antiguo para crear este elemento."));
            }
        }
    }

    @EventHandler
    public void onCourseInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;

        Location clickedLoc = event.getClickedBlock().getLocation();
        Player player = event.getPlayer();
        PlayerProfile profile = professionManager.getProfile(player.getUniqueId());

        if (profile == null) return;

        // Comprobar si el bloque clickeado es un bloque de curso
        for (Map.Entry<Profession, List<Location>> entry : courseManager.getCourseBlocks().entrySet()) {
            Profession courseProf = entry.getKey();
            for (Location loc : entry.getValue()) {
                if (loc.equals(clickedLoc)) {
                    event.setCancelled(true); // Evitar abrir el inventario del yunque/mesa si es curso
                    
                    if (profile.getProfession() == Profession.NONE) {
                        // El jugador comienza el curso (Rango 0)
                        if (!clanIntegration.canTakeProfession(player, courseProf)) {
                            player.sendMessage(LanguageManager.format("&cTu clan ya ha alcanzado el límite de &e" + courseProf.getDisplayName() + "&c permitidos."));
                            return;
                        }
                        profile.setProfession(courseProf);
                        profile.setRank(0);
                        profile.setXp(0);
                        profile.getCompletedBlocks().clear();
                        player.sendMessage(LanguageManager.format("&aHas comenzado tu formación como &e" + courseProf.getDisplayName() + "&a."));
                        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                    } else if (profile.getProfession() != courseProf) {
                        player.sendMessage(LanguageManager.format("&cYa estás estudiando o ejerciendo otra profesión."));
                        return;
                    }

                    // Aumentar el progreso si es Rango 0 (Estudiante)
                    if (profile.getRank() == 0) {
                        String serializedLoc = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
                        if (profile.getCompletedBlocks().contains(serializedLoc)) {
                            player.sendMessage(LanguageManager.format("&cYa completaste esta sección del curso."));
                            return;
                        }

                        profile.getCompletedBlocks().add(serializedLoc);
                        
                        sendRandomProfessionTip(player, courseProf);

                        int totalBlocks = courseManager.getCourseBlocks().get(courseProf).size();
                        int completed = profile.getCompletedBlocks().size();
                        profile.setXp(totalBlocks == 0 ? 0 : (completed * 100) / totalBlocks);

                        if (completed >= totalBlocks && totalBlocks > 0) {
                            if (courseProf == Profession.BLACKSMITH) {
                                if (player.getInventory().contains(Material.ANVIL) && player.getInventory().contains(Material.IRON_SWORD)) {
                                    removeItems(player.getInventory(), Material.ANVIL, 1);
                                    removeItems(player.getInventory(), Material.IRON_SWORD, 1);
                                    player.sendMessage(LanguageManager.format("&aHas entregado tus materiales de graduación."));
                                } else {
                                    player.sendMessage(LanguageManager.format("&cPara graduarte como Herrero debes entregar 1 Yunque y 1 Espada de Hierro."));
                                    profile.getCompletedBlocks().remove(serializedLoc);
                                    profile.setXp(((completed - 1) * 100) / totalBlocks);
                                    return;
                                }
                            }

                            profile.setXp(0);
                            profile.setRank(1);
                            profile.getCompletedBlocks().clear();
                            player.sendMessage(LanguageManager.format("&a¡Felicidades! Has completado tu entrenamiento y ahora eres oficialmente &e" + courseProf.getDisplayName() + "&a."));
                            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                        } else {
                            player.sendMessage(LanguageManager.format("&eProgreso de curso: &b" + profile.getXp() + "%"));
                            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                        }
                    } else {
                        // Si ya es Rango 1+, puede que esto abra una GUI de su profesión
                        player.sendMessage(LanguageManager.format("&aYa eres un profesional de esta área."));
                    }
                    return;
                }
            }
        }
    }

    private void sendRandomProfessionTip(Player player, Profession prof) {
        String[] tips;
        if (prof == Profession.BLACKSMITH) {
            tips = new String[]{
                "&7[&eTip&7] &fCraftea armaduras de Diamante o Netherite para subir de nivel y reducir tu probabilidad de fallo.",
                "&7[&eTip&7] &fAl graduarte, tendrás un 40% de probabilidad de romper ítems al craftear armaduras pesadas.",
                "&7[&eTip&7] &fLos yunques del curso no reparan, pero los normales en el mundo sí, si tienes la XP."
            };
        } else {
            tips = new String[]{
                "&7[&eTip&7] &fCompleta todos los bloques de la zona para graduarte.",
                "&7[&eTip&7] &fCada profesión tiene habilidades y crafteos únicos."
            };
        }
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', tips[new java.util.Random().nextInt(tips.length)]));
    }

    private void removeItems(org.bukkit.inventory.Inventory inv, Material type, int amount) {
        if (amount <= 0) return;
        int size = inv.getSize();
        for (int slot = 0; slot < size; slot++) {
            org.bukkit.inventory.ItemStack is = inv.getItem(slot);
            if (is == null) continue;
            if (is.getType() == type) {
                int newAmount = is.getAmount() - amount;
                if (newAmount > 0) {
                    is.setAmount(newAmount);
                    break;
                } else {
                    inv.setItem(slot, null);
                    amount = -newAmount;
                    if (amount == 0) break;
                }
            }
        }
    }
}
