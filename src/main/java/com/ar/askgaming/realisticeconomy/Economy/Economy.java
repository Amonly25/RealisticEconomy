package com.ar.askgaming.realisticeconomy.Economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;
import com.ar.askgaming.realisticeconomy.Data.DatabaseManager;
import com.ar.askgaming.realisticeconomy.Data.PlayerData;

public class Economy {

    private double balance;
    private double inicialBalance;
    private double inflation;
    private double debtLimit;
    private double loanInterest;
    private double savingsInterest;

    private RealisticEconomy plugin;

    private DatabaseManager database;

    public Economy(RealisticEconomy plugin) {
        this.plugin = plugin;
        this.inicialBalance = plugin.getConfig().getDouble("initial_server_bank_balance", 1000000000.0);
        this.inflation = 0.0;
        this.debtLimit = plugin.getConfig().getDouble("debt_limit", 10000.0);
        this.loanInterest = plugin.getConfig().getDouble("loan_interest_per_day", 0.1666);
        this.savingsInterest = plugin.getConfig().getDouble("savings_interest_per_day", 0.0833);
        this.database = plugin.getDatabase();

        calculateInfation();
    }
    public double calculateInfation() {
        double inflation = 0.0;
        double balance = getBalance();
        double inicialBalance = getInicialBalance();
        if (balance > inicialBalance) {
            inflation = (balance - inicialBalance) / inicialBalance;
        }
        setInflation(inflation);
        return inflation;
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
    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getInicialBalance() {
        return inicialBalance;
    }

    public void setInicialBalance(double inicialBalance) {
        this.inicialBalance = inicialBalance;
    }

    public double getInflation() {
        return inflation;
    }

    public void setInflation(double inflation) {
        this.inflation = inflation;
    }

    public double getDebtLimit() {
        return debtLimit;
    }

    public void setDebtLimit(double debtLimit) {
        this.debtLimit = debtLimit;
    }

    public double getLoanInterest() {
        return loanInterest;
    }

    public void setLoanInterest(double loanInterest) {
        this.loanInterest = loanInterest;
    }

    public double getSavingsInterest() {
        return savingsInterest;
    }

    public void setSavingsInterest(double savingsInterest) {
        this.savingsInterest = savingsInterest;
    }
}
