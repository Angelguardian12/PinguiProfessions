package bely.pinguiprofessions.listeners;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.managers.ClanIntegration;
import bely.pinguiprofessions.managers.LanguageManager;
import bely.pinguiprofessions.managers.ProfessionManager;
import bely.pinguiprofessions.models.PlayerProfile;
import bely.pinguiprofessions.models.Profession;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.events.PlayerJoinedClanEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ClanListener implements Listener {

    private final PinguiProfessions plugin;
    private final ClanIntegration clanIntegration;
    private final ProfessionManager professionManager;

    public ClanListener(PinguiProfessions plugin, ClanIntegration clanIntegration, ProfessionManager professionManager) {
        this.plugin = plugin;
        this.clanIntegration = clanIntegration;
        this.professionManager = professionManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinClan(PlayerJoinedClanEvent event) {
        if (!plugin.getConfig().getBoolean("use-simpleclans", true)) return;
        
        ClanPlayer cp = event.getClanPlayer();
        if (cp == null || cp.toPlayer() == null) return;
        
        Player player = cp.toPlayer();
        PlayerProfile profile = professionManager.getProfile(player.getUniqueId());
        
        // Si no tiene profesión, da igual a qué clan entre.
        if (profile == null || profile.getProfession() == Profession.NONE) return;
        
        Profession prof = profile.getProfession();
        Clan clan = event.getClan();
        
        // Comprobar si el clan ya tiene el límite de esta profesión
        // (Nota: como el jugador YA entró en este punto, canClanAcceptProfession podría contar a este jugador
        // y dar falso positivo. Por tanto, le pasaremos un método especial o simplemente restaremos 1 si ya está dentro)
        if (!clanIntegration.canTakeProfession(player, prof)) {
            // El límite se superó!
            // En SimpleClans, no se puede cancelar PlayerJoinedClanEvent porque no implementa Cancellable en versiones viejas.
            // Lo que hacemos es sacarlo del clan inmediatamente.
            clan.removePlayerFromClan(player.getUniqueId());
            player.sendMessage(LanguageManager.format("&cNo has podido unirte a " + clan.getName() + " porque ya han alcanzado el límite máximo de &e" + prof.getDisplayName() + "&c."));
        }
    }
}
