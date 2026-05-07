package bely.pinguiprofessions.utils;

import bely.pinguiprofessions.models.PlayerProfile;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Player;

public class WGUtils {

    public static bely.pinguiprofessions.models.Profession getProfessionZone(Player player) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()));

            for (ProtectedRegion region : set) {
                String id = region.getId().toLowerCase();
                for (bely.pinguiprofessions.models.Profession prof : bely.pinguiprofessions.models.Profession.values()) {
                    if (prof != bely.pinguiprofessions.models.Profession.NONE && id.contains(prof.name().toLowerCase())) {
                        return prof;
                    }
                }
            }
        } catch (Exception e) {
            // Ignorar errores si WorldGuard no está cargado correctamente
        }
        return null;
    }
}
