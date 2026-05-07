package bely.pinguiprofessions.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerProfile {
    private final UUID uuid;
    private Profession profession;
    private int rank;
    private int xp;
    private final Set<String> completedBlocks;

    public PlayerProfile(UUID uuid, Profession profession, int rank, int xp, Set<String> completedBlocks) {
        this.uuid = uuid;
        this.profession = profession;
        this.rank = rank;
        this.xp = xp;
        this.completedBlocks = completedBlocks != null ? completedBlocks : new HashSet<>();
    }

    public PlayerProfile(UUID uuid, Profession profession, int rank, int xp) {
        this(uuid, profession, rank, xp, new HashSet<>());
    }

    public UUID getUuid() { return uuid; }
    public Profession getProfession() { return profession; }
    public void setProfession(Profession profession) { this.profession = profession; }
    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }
    public void addXp(int amount) { this.xp += amount; }
    public Set<String> getCompletedBlocks() { return completedBlocks; }
}
