package bely.pinguiprofessions.listeners;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.LanguageManager;
import bely.pinguiprofessions.managers.ProfessionManager;
import bely.pinguiprofessions.models.PlayerProfile;
import bely.pinguiprofessions.models.Profession;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ShopListener implements Listener {

    private final ProfessionManager professionManager;

    public ShopListener(PinguiProfessions plugin, ProfessionManager professionManager) {
        this.professionManager = professionManager;
    }

    @EventHandler
    public void onShopCreateCommand(PlayerCommandPreprocessEvent event) {
        String cmd = event.getMessage().toLowerCase();
        
        // Comandos de creación de QuickShop
        if (cmd.startsWith("/qs create") || cmd.startsWith("/quickshop create")) {
            Player player = event.getPlayer();
            PlayerProfile profile = professionManager.getProfile(player.getUniqueId());

            // Solo los comerciantes (rango 1+) pueden crear tiendas físicas
            if ((profile == null || profile.getProfession() != Profession.MERCHANT || profile.getRank() == 0) && !player.hasPermission("pinguiprofessions.admin.bypass")) {
                event.setCancelled(true);
                player.sendMessage(LanguageManager.format("&cLas leyes del mercado dictan que solo los &eComerciantes &cgraduados pueden establecer tiendas."));
            }
        }
    }
}
