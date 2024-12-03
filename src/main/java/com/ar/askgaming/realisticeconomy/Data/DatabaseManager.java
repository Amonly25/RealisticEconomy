package com.ar.askgaming.realisticeconomy.Data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class DatabaseManager {

    private final String databaseType;
    private final String databaseUrl;
    private final String username;
    private final String password;

    private final HashMap<UUID, PlayerData> playerCache = new HashMap<>();

    public HashMap<UUID, PlayerData> getPlayerCache() {
        return playerCache;
    }

    private double initialServerBankBalance;

    private RealisticEconomy plugin;

    public DatabaseManager(RealisticEconomy main) {
        plugin = main;
        databaseType = plugin.getConfig().getString("data_mode", "SQLITE").equalsIgnoreCase("mysql") ? "MYSQL" : "SQLITE";
        initialServerBankBalance = plugin.getConfig().getDouble("initial_server_bank_balance", 1000000000.0);

        switch (databaseType.toUpperCase()) {
            case "SQLITE":
                databaseUrl = plugin.getDataFolder()+"/economy.db";
                username = null;
                password = null;
                break;
            case "MYSQL":
                databaseUrl = plugin.getConfig().getString("mysql.url");
                username = plugin.getConfig().getString("mysql.username");
                password = plugin.getConfig().getString("mysql.password");
                break;
            default:
                throw new IllegalArgumentException("Unknown database type: " + databaseType);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            playerCache.put(p.getUniqueId(), loadPlayerData(p.getUniqueId()));
        }
    }

    public Connection connect() throws SQLException {
        switch (databaseType) {
            case "SQLITE":
                return DriverManager.getConnection("jdbc:sqlite:" + databaseUrl);
            case "MYSQL":
                return DriverManager.getConnection("jdbc:mysql://" + databaseUrl, username, password);
            default:
                throw new IllegalArgumentException("Unknown database type: " + databaseType);
        }
    }
    public String getDatabaseType() {
        return databaseType;
    }

    public void createTable() {
        String sql = "";
        switch (databaseType) {
            case "SQLITE":
                sql = "CREATE TABLE IF NOT EXISTS economy ("
                    + "uuid TEXT PRIMARY KEY,"
                    + "balance REAL,"
                    + "bankBalance REAL,"
                    + "debt REAL,"
                    + "tokens INTEGER,"
                    + "lastLogin BIGINT"
                    + ");";
                break;
            case "MYSQL":
                sql = "CREATE TABLE IF NOT EXISTS economy ("
                    + "uuid VARCHAR(36) PRIMARY KEY,"
                    + "balance DOUBLE,"
                    + "bankBalance DOUBLE,"
                    + "debt DOUBLE,"
                    + "tokens INT,"
                    + "lastLogin BIGINT"
                    + ");";
                break;
            default:
                throw new IllegalStateException("Unknown data mode: " + databaseType);
        }

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void createServerBankTable() {
        String createSql = "";
        switch (databaseType) {
            case "SQLITE":
                createSql = "CREATE TABLE IF NOT EXISTS serverbank ("
                          + "balance REAL DEFAULT " + initialServerBankBalance
                          + ");";
                break;
            case "MYSQL":
                createSql = "CREATE TABLE IF NOT EXISTS serverbank ("
                          + "balance DOUBLE DEFAULT " + initialServerBankBalance
                          + ");";
                break;
            default:
                throw new IllegalStateException("Unknown data mode: " + databaseType);
        }
    
        try (Connection conn = connect();
             PreparedStatement createStmt = conn.prepareStatement(createSql)) {
    
            // Crear la tabla
            createStmt.execute();
    
            // Insertar un registro inicial solo si la tabla fue creada
            String insertSql = "";
            switch (databaseType) {
                case "SQLITE":
                    insertSql = "INSERT OR IGNORE INTO serverbank (balance) VALUES (" + initialServerBankBalance + ")";
                    break;
                case "MYSQL":
                    insertSql = "INSERT INTO serverbank (balance) VALUES (" + initialServerBankBalance + ") "
                              + "ON DUPLICATE KEY UPDATE balance=balance";
                    break;
                default:
                    throw new IllegalStateException("Unknown data mode: " + databaseType);
            }
    
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.executeUpdate();
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public HashMap<String, Double> getBalances() {
        String sql = "SELECT uuid, balance, bankBalance, debt FROM economy";
        HashMap<String, Double> balances = new HashMap<>();
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
    
            while (rs.next()) {
                String uuid = rs.getString("uuid");
                double balance = rs.getDouble("balance");
                double bankBalance = rs.getDouble("bankBalance");
                double debt = rs.getDouble("debt");
                double totalBalance = balance + bankBalance - debt;
                balances.put(uuid, totalBalance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balances;
    }
    public PlayerData loadPlayerData(UUID playerUUID) {
        if (playerCache.containsKey(playerUUID)) {
            return playerCache.get(playerUUID);
        }
        String sql = "SELECT balance, bankBalance, debt, tokens, lastLogin FROM economy WHERE uuid = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    double bankBalance = rs.getDouble("bankBalance");
                    double debt = rs.getDouble("debt");
                    int tokens = rs.getInt("tokens");
                    long lastConnected = rs.getLong("lastLogin");
                    // Almacenar en memoria
                    PlayerData playerData = new PlayerData(playerUUID, balance, bankBalance, debt, tokens, lastConnected);
                    playerCache.put(playerUUID, playerData);
                    return playerData;
                } else {
                    return plugin.getEconomyManager().createPlayerAccount(playerUUID);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
   
}
