package com.diogocorreadcs.bounty.database;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.models.HunterStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HunterStatsRepository {

    private final BountyPlugin plugin;
    private final DatabaseManager databaseManager;

    public HunterStatsRepository(BountyPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void createTable() {
        String statsTable = "CREATE TABLE IF NOT EXISTS dbounty_stats (" +
                "player_uuid VARCHAR(36) PRIMARY KEY, " +
                "player_name VARCHAR(16) NOT NULL, " +
                "bounties_claimed INT NOT NULL DEFAULT 0, " +
                "total_earned DOUBLE NOT NULL DEFAULT 0" +
                ");";

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(statsTable);
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao criar tabela de estatisticas: " + e.getMessage());
        }
    }

    public CompletableFuture<HunterStats> loadHunterStatsAsync(UUID playerUuid, String fallbackName) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT player_name, bounties_claimed, total_earned FROM dbounty_stats WHERE player_uuid = ?;";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, playerUuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new HunterStats(
                                playerUuid,
                                rs.getString("player_name"),
                                rs.getInt("bounties_claimed"),
                                rs.getDouble("total_earned")
                        );
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao carregar stats de " + fallbackName + ": " + e.getMessage());
            }
            return new HunterStats(playerUuid, fallbackName, 0, 0.0D);
        });
    }

    public CompletableFuture<Void> saveHunterStatsAsync(HunterStats stats) {
        return CompletableFuture.runAsync(() -> saveHunterStatsSync(stats));
    }

    public void saveHunterStatsSync(HunterStats stats) {
        String upsert = databaseManager.isMysql()
                ? "INSERT INTO dbounty_stats (player_uuid, player_name, bounties_claimed, total_earned) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE player_name = VALUES(player_name), bounties_claimed = VALUES(bounties_claimed), total_earned = VALUES(total_earned);"
                : "INSERT INTO dbounty_stats (player_uuid, player_name, bounties_claimed, total_earned) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(player_uuid) DO UPDATE SET player_name = excluded.player_name, bounties_claimed = excluded.bounties_claimed, total_earned = excluded.total_earned;";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(upsert)) {
            ps.setString(1, stats.getPlayerUuid().toString());
            ps.setString(2, stats.getPlayerName());
            ps.setInt(3, stats.getBountiesClaimed());
            ps.setDouble(4, stats.getTotalEarned());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar stats de " + stats.getPlayerName() + ": " + e.getMessage());
        }
    }

    public CompletableFuture<List<HunterStats>> getTopHuntersAsync(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<HunterStats> top = new ArrayList<>();
            String query = "SELECT player_uuid, player_name, bounties_claimed, total_earned FROM dbounty_stats ORDER BY total_earned DESC LIMIT ?;";
            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        top.add(new HunterStats(
                                UUID.fromString(rs.getString("player_uuid")),
                                rs.getString("player_name"),
                                rs.getInt("bounties_claimed"),
                                rs.getDouble("total_earned")
                        ));
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Erro ao buscar ranking de cacadores: " + e.getMessage());
            }
            return top;
        });
    }
}
