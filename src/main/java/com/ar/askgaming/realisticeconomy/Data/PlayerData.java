package com.ar.askgaming.realisticeconomy.Data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;

public class PlayerData {

    private RealisticEconomy plugin = RealisticEconomy.getPlugin(RealisticEconomy.class);

    private double balance;
    private double bankBalance;
    private double debt;
    private int tokens;

    private UUID playerUUID;

    public PlayerData(UUID playerUUID, double balance, double bankBalance, double debt, int tokens) {
        this.balance = balance;
        this.bankBalance = bankBalance;
        this.debt = debt;
        this.tokens = tokens;
        this.playerUUID = playerUUID;
    }
    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
    public double getBankBalance() {
        return bankBalance;
    }
    public void setBankBalance(double bankBalance) {
        this.bankBalance = bankBalance;
    }
    public double getDebt() {
        return debt;
    }
    public void setDebt(double debt) {
        this.debt = debt;
    }
    public int getTokens() {
        return tokens;
    }
    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }
    public boolean save() {
        String sql = "UPDATE economy SET balance = ?, bankBalance = ?, debt = ?, tokens = ? WHERE uuid = ?";
        try (Connection conn = plugin.getDatabase().connect();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, getBalance());
            stmt.setDouble(2, getBankBalance());
            stmt.setDouble(3, getDebt());
            stmt.setInt(4, getTokens());
            stmt.setString(5, getPlayerUUID().toString());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error saving player data, error printed below.");
            plugin.getEconomyLogger().log("Error saving player data, error printed to console.");
            e.printStackTrace();
            return false;
        }
    }

}
