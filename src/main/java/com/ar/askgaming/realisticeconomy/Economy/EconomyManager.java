package com.ar.askgaming.realisticeconomy.Economy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;
import com.ar.askgaming.realisticeconomy.Data.DatabaseManager;
import com.ar.askgaming.realisticeconomy.Data.PlayerData;

public class EconomyManager {

    private double balance;
    private double inicialBalance;
    private double inflation;
    private double debtLimit;
    private double loanInterest;
    private double savingsInterest;

    private RealisticEconomy plugin;

    private DatabaseManager database;

    public EconomyManager(RealisticEconomy plugin) {
        this.plugin = plugin;
        
        loadEconomyData();
    }
    public void loadEconomyData() {
        this.inicialBalance = plugin.getConfig().getDouble("initial_server_bank_balance", 1000000000.0);
        this.inflation = 0.0;
        this.debtLimit = plugin.getConfig().getDouble("debt_limit", 10000.0);
        this.loanInterest = plugin.getConfig().getDouble("loan_interest_per_day", 0.1666);
        this.savingsInterest = plugin.getConfig().getDouble("savings_interest_per_day", 0.0833);
        this.database = plugin.getDatabase();

        calculateInflation();
    }

    public void calculateInflation() {
        double inflation = 0.0;
        double serverBalance = plugin.getServerBank().getBalance();
        double playerBalances = getPlayerBalances();
        double inicialBalance = getInicialBalance();
        
        inflation = ((serverBalance + playerBalances - inicialBalance) / inicialBalance) * 100;
    
        setInflation(inflation);
    }
    
    public double getPlayerBalances(){
        double total = 0.0;
        HashMap<String, Double> balances = plugin.getDatabase().getBalances();
        for (String key : balances.keySet()) {
            total += balances.get(key);
        }
        return total;
    }
    //#region createPlayerAccount
    public PlayerData createPlayerAccount(UUID uuid) {
        String sql;
        switch (database.getDatabaseType()) {
            case "SQLITE":
                sql = "INSERT OR IGNORE INTO economy (uuid, balance, bankBalance, debt, tokens, lastLogin) VALUES (?, 0, 0, 0, 0, 0)";
                break;
            case "MYSQL":
                sql = "INSERT INTO economy (uuid, balance, bankBalance, debt, tokens, lastLogin) VALUES (?, 0, 0, 0, 0, 0) ON DUPLICATE KEY UPDATE uuid=uuid";
                break;
            default:
                throw new IllegalStateException("Unknown data mode: " + database.getDatabaseType());
        }

        try (Connection conn = database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.executeUpdate();
            return new PlayerData(uuid, 0, 0, 0, 0,0);
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
    public static double formatDouble(double number) {
        String numberStr = Double.toString(number);
        int decimalIndex = numberStr.indexOf('.');
        
        if (decimalIndex < 0) {
            return number; // No hay punto decimal, es un número entero
        }

        int numDecimals = numberStr.length() - decimalIndex - 1;
        
        if (numDecimals > 4) {
            BigDecimal bd = new BigDecimal(number).setScale(4, RoundingMode.HALF_UP);
            return bd.doubleValue();
        } else {
            return number;
        }
    }
    public static String format(double number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String formattedDecimalNumber = decimalFormat.format(number);
        return formattedDecimalNumber;
    }
}
