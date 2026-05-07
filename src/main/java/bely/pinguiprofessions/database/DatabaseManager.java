package bely.pinguiprofessions.database;

import bely.pinguiprofessions.PinguiProfessions;
import bely.pinguiprofessions.models.PlayerProfile;
import bely.pinguiprofessions.models.Profession;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DatabaseManager {

    private final PinguiProfessions plugin;
    private Connection connection;

    public DatabaseManager(PinguiProfessions plugin) {
        this.plugin = plugin;
        connect();
        initTable();
    }

    private void connect() {
        try {
            File dataFolder = new File(plugin.getDataFolder(), "database.db");
            if (!dataFolder.exists()) {
                dataFolder.getParentFile().mkdirs();
                dataFolder.createNewFile();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
        } catch (Exception e) {
            plugin.getLogger().severe("No se pudo conectar a SQLite: " + e.getMessage());
        }
    }

    private void initTable() {
        if (connection == null) return;
        String sql = "CREATE TABLE IF NOT EXISTS players (" +
                     "uuid VARCHAR(36) PRIMARY KEY, " +
                     "profession VARCHAR(32) DEFAULT 'NONE', " +
                     "rank INTEGER DEFAULT 0, " +
                     "xp INTEGER DEFAULT 0, " +
                     "completed_blocks TEXT DEFAULT ''" +
                     ");";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
            // Alterar la tabla si ya existía para agregar la nueva columna
            try (PreparedStatement alterStmt = connection.prepareStatement("ALTER TABLE players ADD COLUMN completed_blocks TEXT DEFAULT '';")) {
                alterStmt.execute();
            } catch (SQLException ignored) {}
        } catch (SQLException e) {
            plugin.getLogger().severe("Error al inicializar tabla: " + e.getMessage());
        }
    }

    public PlayerProfile loadPlayer(UUID uuid) {
        if (connection == null) return new PlayerProfile(uuid, Profession.NONE, 0, 0);

        String sql = "SELECT profession, rank, xp, completed_blocks FROM players WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Profession prof = Profession.valueOf(rs.getString("profession").toUpperCase());
                int rank = rs.getInt("rank");
                int xp = rs.getInt("xp");
                
                Set<String> completedBlocks = new HashSet<>();
                String blocksStr = rs.getString("completed_blocks");
                if (blocksStr != null && !blocksStr.isEmpty()) {
                    completedBlocks.addAll(Arrays.asList(blocksStr.split(",")));
                }
                
                return new PlayerProfile(uuid, prof, rank, xp, completedBlocks);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error cargando jugador " + uuid + ": " + e.getMessage());
        }
        return new PlayerProfile(uuid, Profession.NONE, 0, 0);
    }

    public void savePlayer(PlayerProfile profile) {
        if (connection == null) return;

        String sql = "INSERT OR REPLACE INTO players (uuid, profession, rank, xp, completed_blocks) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, profile.getUuid().toString());
            stmt.setString(2, profile.getProfession().name());
            stmt.setInt(3, profile.getRank());
            stmt.setInt(4, profile.getXp());
            stmt.setString(5, String.join(",", profile.getCompletedBlocks()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error guardando jugador " + profile.getUuid() + ": " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
