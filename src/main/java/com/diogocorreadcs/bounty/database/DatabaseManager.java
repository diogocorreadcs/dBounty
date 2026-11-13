package com.diogocorreadcs.bounty.database;

import com.diogocorreadcs.bounty.BountyPlugin;
import com.diogocorreadcs.bounty.managers.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {

    private final BountyPlugin plugin;
    private final ConfigManager configManager;
    private HikariDataSource dataSource;
    private boolean mysql;

    public DatabaseManager(BountyPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public boolean connect() {
        String type = configManager.getDatabaseType().toUpperCase();
        this.mysql = type.equals("MYSQL");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("dBounty-Pool");

        if (this.mysql) {
            String host = configManager.getMysqlHost();
            int port = configManager.getMysqlPort();
            String database = configManager.getMysqlDatabase();
            String username = configManager.getMysqlUsername();
            String password = configManager.getMysqlPassword();
            boolean useSsl = configManager.isMysqlSsl();

            hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSsl + "&autoReconnect=true");
            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.setMaximumPoolSize(10);
            hikariConfig.setMinimumIdle(2);
            hikariConfig.setConnectionTimeout(30000);
            hikariConfig.setLeakDetectionThreshold(10000);
            hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
            hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
            hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        } else {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Nao foi possivel criar a pasta do plugin.");
            }
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            if (!dbFile.exists()) {
                try {
                    if (!dbFile.createNewFile()) {
                        plugin.getLogger().warning("Nao foi possivel criar o arquivo database.db.");
                    }
                } catch (IOException e) {
                    plugin.getLogger().severe("Erro ao criar database.db: " + e.getMessage());
                    return false;
                }
            }
            hikariConfig.setDriverClassName("org.sqlite.JDBC");
            hikariConfig.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
            hikariConfig.setMaximumPoolSize(1);
            hikariConfig.setConnectionTestQuery("SELECT 1");
        }

        try {
            this.dataSource = new HikariDataSource(hikariConfig);
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao inicializar o HikariCP: " + e.getMessage());
            return false;
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("O DataSource do banco de dados esta fechado.");
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public boolean isMysql() {
        return mysql;
    }
}