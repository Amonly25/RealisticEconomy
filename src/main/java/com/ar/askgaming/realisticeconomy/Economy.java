package com.ar.askgaming.realisticeconomy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.datas.DatabaseManager;

public class Economy {

    private double balance;

    private double inicialBalance;

    private double inflation;

    private RealisticEconomy plugin;

    private DatabaseManager database;

    public Economy(RealisticEconomy plugin) {
        this.plugin = plugin;
        this.inicialBalance = plugin.getConfig().getDouble("initial_server_bank_balance", 1000000000.0);
        this.inflation = 0.0;
        this.database = plugin.getDatabase();
    }

    //#region createPlayerAccount
    public PlayerData createPlayerAccount(UUID uuid) {
        String sql;
        switch (database.getDatabaseType()) {
            case "SQLITE":
                sql = "INSERT OR IGNORE INTO economy (uuid, balance, bankBalance, debt, tokens) VALUES (?, 0, 0, 0, 0)";
                break;
            case "MYSQL":
                sql = "INSERT INTO economy (uuid, balance, bankBalance, debt, tokens) VALUES (?, 0, 0, 0, 0) ON DUPLICATE KEY UPDATE uuid=uuid";
                break;
            default:
                throw new IllegalStateException("Unknown data mode: " + database.getDatabaseType());
        }

        try (Connection conn = database.connect();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
            return new PlayerData(uuid, 0, 0, 0, 0);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    public PlayerData createPlayerAccount(Player player) {
        return createPlayerAccount(player.getUniqueId());
    }   
    //#endregion  
}
