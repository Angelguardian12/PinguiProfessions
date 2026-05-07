package bely.pinguiprofessions.managers;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.models.PlayerProfile;
import bely.pinguiprofessions.models.Profession;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class ClanIntegration {

    private final PinguiProfessions plugin;
    private final ProfessionManager professionManager;
    private boolean isEnabled;

    public ClanIntegration(PinguiProfessions plugin, ProfessionManager professionManager) {
        this.plugin = plugin;
        this.professionManager = professionManager;
        this.isEnabled = plugin.getConfig().getBoolean("use-simpleclans", true) && 
                         Bukkit.getPluginManager().getPlugin("SimpleClans") != null;
    }

    /**
     * Comprueba si el jugador puede tomar esta profesión basada en el límite de su clan.
     * Retorna true si tiene permitido, false si ya superó el límite.
     */
    public boolean canTakeProfession(Player player, Profession profession) {
        if (!isEnabled) return true;

        SimpleClans sc = SimpleClans.getInstance();
        if (sc == null || sc.getClanManager() == null) return true;

        ClanPlayer cp = sc.getClanManager().getClanPlayer(player);
        if (cp == null || cp.getClan() == null) {
            // Si el jugador no está en un clan, permitimos que tome la profesión libremente.
            return true;
        }

        Clan clan = cp.getClan();
        return checkClanLimit(clan, profession, player.getUniqueId());
    }

    /**
     * Comprueba si el clan puede admitir a un nuevo jugador con la profesión dada.
     */
    public boolean canClanAcceptProfession(Clan clan, Profession profession) {
        if (!isEnabled) return true;
        return checkClanLimit(clan, profession, null);
    }

    private boolean checkClanLimit(Clan clan, Profession profession, UUID ignoringPlayer) {
        int limit = plugin.getConfig().getInt("clan_limits." + profession.name().toLowerCase(), -1);
        if (limit == -1) return true; // -1 significa sin límite

        int currentCount = 0;
        List<ClanPlayer> members = clan.getMembers();
        
        for (ClanPlayer member : members) {
            UUID memberUUID = member.getUniqueId();
            if (ignoringPlayer != null && memberUUID.equals(ignoringPlayer)) continue;
            
            PlayerProfile profile = professionManager.getProfileOffline(memberUUID);
            if (profile != null && profile.getProfession() == profession) {
                currentCount++;
            }
        }

        return currentCount < limit;
    }
}
