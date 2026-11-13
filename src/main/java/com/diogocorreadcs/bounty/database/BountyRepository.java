package com.diogocorreadcs.bounty.database;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.models.Bounty;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BountyRepository {

    private final BountyPlugin plugin;
    private final DatabaseManager databaseManager;

    public BountyRepository(BountyPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void createTables() {
        String activeTable = "CREATE TABLE IF NOT EXISTS dbounty_active (" +
                "target_uuid VARCHAR(36) PRIMARY KEY, " +
                "target_name VARCHAR(16) NOT NULL, " +
                "total_amount DOUBLE NOT NULL, " +
                "created_at BIGINT NOT NULL" +
                ");";

        String contributorsTable = "CREATE TABLE IF NOT EXISTS dbounty_contributors (" +
                "target_uuid VARCHAR(36) NOT NULL, " +
                "contributor_uuid VARCHAR(36) NOT NULL, " +
                "amount DOUBLE NOT NULL, " +
                "PRIMARY KEY (target_uuid, contributor_uuid)" +
                ");";

        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(activeTable);
            stmt.execute(contributorsTable);
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao criar tabelas de recompensas: " + e.getMessage());
        }
    }

    public Map<UUID, Bounty> loadAllActiveBounties() {
        Map<UUID, Bounty> bounties = new HashMap<>();
        String queryActive = "SELECT target_uuid, target_name, total_amount, created_at FROM dbounty_active;";
        String queryContributors = "SELECT target_uuid, contributor_uuid, amount FROM dbounty_contributors;";

        try (Connection conn = databaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(queryActive);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID targetUuid = UUID.fromString(rs.getString("target_uuid"));
                    String targetName = rs.getString("target_name");
                    double totalAmount = rs.getDouble("total_amount");
                    long createdAt = rs.getLong("created_at");
                    bounties.put(targetUuid, new Bounty(targetUuid, targetName, totalAmount, createdAt));
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(queryContributors);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID targetUuid = UUID.fromString(rs.getString("target_uuid"));
                    Bounty bounty = bounties.get(targetUuid);
                    if (bounty != null) {
                        UUID contributorUuid = UUID.fromString(rs.getString("contributor_uuid"));
                        double amount = rs.getDouble("amount");
                        bounty.setContributorAmount(contributorUuid, amount);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao carregar recompensas ativas: " + e.getMessage());
        }
        return bounties;
    }

    public CompletableFuture<Void> saveBountyAsync(Bounty bounty) {
        return CompletableFuture.runAsync(() -> saveBountySync(bounty));
    }

    public void saveBountySync(Bounty bounty) {
        String upsertActive = databaseManager.isMysql()
                ? "INSERT INTO dbounty_active (target_uuid, target_name, total_amount, created_at) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE target_name = VALUES(target_name), total_amount = VALUES(total_amount);"
                : "INSERT INTO dbounty_active (target_uuid, target_name, total_amount, created_at) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(target_uuid) DO UPDATE SET target_name = excluded.target_name, total_amount = excluded.total_amount;";

        String upsertContributor = databaseManager.isMysql()
                ? "INSERT INTO dbounty_contributors (target_uuid, contributor_uuid, amount) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE amount = VALUES(amount);"
                : "INSERT INTO dbounty_contributors (target_uuid, contributor_uuid, amount) VALUES (?, ?, ?) " +
                "ON CONFLICT(target_uuid, contributor_uuid) DO UPDATE SET amount = excluded.amount;";

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psActive = conn.prepareStatement(upsertActive);
                 PreparedStatement psContrib = conn.prepareStatement(upsertContributor)) {

                psActive.setString(1, bounty.getTargetUuid().toString());
                psActive.setString(2, bounty.getTargetName());
                psActive.setDouble(3, bounty.getTotalAmount());
                psActive.setLong(4, bounty.getCreatedAt());
                psActive.executeUpdate();

                for (Map.Entry<UUID, Double> entry : bounty.getContributors().entrySet()) {
                    psContrib.setString(1, bounty.getTargetUuid().toString());
                    psContrib.setString(2, entry.getKey().toString());
                    psContrib.setDouble(3, entry.getValue());
                    psContrib.addBatch();
                }
                psContrib.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao salvar recompensa de " + bounty.getTargetName() + ": " + e.getMessage());
        }
    }

    public CompletableFuture<Void> deleteBountyAsync(UUID targetUuid) {
        return CompletableFuture.runAsync(() -> deleteBountySync(targetUuid));
    }

    public void deleteBountySync(UUID targetUuid) {
        String deleteActive = "DELETE FROM dbounty_active WHERE target_uuid = ?;";
        String deleteContributors = "DELETE FROM dbounty_contributors WHERE target_uuid = ?;";

        try (Connection conn = databaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(deleteActive);
                 PreparedStatement ps2 = conn.prepareStatement(deleteContributors)) {
                ps1.setString(1, targetUuid.toString());
                ps1.executeUpdate();

                ps2.setString(1, targetUuid.toString());
                ps2.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erro ao deletar recompensa (" + targetUuid + "): " + e.getMessage());
        }
    }
}