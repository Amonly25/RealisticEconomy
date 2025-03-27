package com.ar.askgaming.realisticeconomy.Economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import com.ar.askgaming.realisticeconomy.RealisticEconomy;
import com.ar.askgaming.realisticeconomy.Data.DatabaseManager;
import com.ar.askgaming.realisticeconomy.Data.PlayerData;

public class BankTransactions {

    private DatabaseManager database;
    private RealisticEconomy plugin;

    public BankTransactions(RealisticEconomy main) {
        plugin = main;
        database = plugin.getDatabase();
    }

    public double getBalance() {

        String sql = "SELECT balance FROM serverbank LIMIT 1"; // Consulta para obtener el balance

        try (Connection conn = database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery(); // Ejecutar la consulta y obtener el resultado
    
            if (rs.next()) { // Verificar si hay un resultado
                return rs.getDouble("balance"); // Devolver el balance obtenido
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Manejar posibles excepciones  
        }
    
        return 0.0; // Devolver 0.0 si no se encuentra un balance o hay un error
    }
    //#region transfer
    /**
     * Transfers money between the server and a player.
     *
     * @param playerUUID The UUID of the player involved in the transaction.
     * @param amount The amount of money to transfer (must be positive).
     * @param serverPays If true, the server gives money to the player. If false, the server receives money from the player.
     * @return true if the transaction was successful, false otherwise.
     */
    public boolean transferWithPlayer(@NotNull UUID playerUUID, double amount, boolean serverPays) {
        if (amount < 0) {
            return false;
        }

        PlayerData playerData = plugin.getDatabase().loadPlayerData(playerUUID);
        if (playerData == null) {
            return false;
        }

        double playerBalance = playerData.getBalance();
        double serverBalance = getBalance();

        if (serverPays) {
            if (serverBalance < amount) {
                return false; // The server bank does not have enough money
            }
            playerData.setBalance(playerBalance + amount);
        } else {
            if (playerBalance < amount) {
                return false; // The player does not have enough money
            }
            playerData.setBalance(playerBalance - amount);
        }

        boolean step = playerData.save();
        if (!step) {
            // Revert changes if the player update fails
            playerData.setBalance(serverPays ? playerBalance - amount : playerBalance + amount);
            return false;
        }

        // Update the server bank balance
        double newServerBalance = serverPays ? serverBalance - amount : serverBalance + amount;
        if (!saveServerBankBalance(newServerBalance)) {
            // Revert if the bank update fails
            playerData.setBalance(serverPays ? playerBalance - amount : playerBalance + amount);
            playerData.save();
            return false;
        }

        OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerUUID);
        String action = serverPays ? "Withdrawn" : "Deposited";
        plugin.getEconomyLogger().log(action + " " + amount + " " + (serverPays ? "to" : "from") + " " + player.getName());

        return true;
    }

    //#region set
    public boolean setServerBankBalance(double newBalance) {
        return saveServerBankBalance(newBalance);
    }

    //#region save
    private boolean saveServerBankBalance(double amount) {
        String sql = "UPDATE serverbank SET balance = balance + ?"; // ← Corrección aquí
        try (Connection conn = database.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            plugin.getEconomyLogger().log("Error updating server bank balance: " + e.getMessage());
            return false;
        }
    }
}
