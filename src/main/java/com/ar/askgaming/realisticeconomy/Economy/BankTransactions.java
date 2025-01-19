package com.ar.askgaming.realisticeconomy.Economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

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

        try (Connection conn = database.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery(); // Ejecutar la consulta y obtener el resultado
    
            if (rs.next()) { // Verificar si hay un resultado
                return rs.getDouble("balance"); // Devolver el balance obtenido
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Manejar posibles excepciones  
        }
    
        return 0.0; // Devolver 0.0 si no se encuentra un balance o hay un error
    }
    //#region withdrawFromServerToPlayer
    public boolean withdrawFromServerToPlayer(UUID playerUUID, double amount) {
        if (amount < 0) {
            return false;
        }
    
        double serverBalance = getBalance();
        if (serverBalance < amount) {
            return false;
        }
    
        PlayerData playerData = plugin.getDatabase().loadPlayerData(playerUUID);
        if (playerData == null) {
            return false;
        }
        
        playerData.setBalance(playerData.getBalance() + amount);
        boolean step = playerData.save();
        if (!step) {
            playerData.setBalance(playerData.getBalance() - amount);
            return false;
        }

        try (Connection conn = database.connect()) {
            conn.setAutoCommit(false); // Iniciar la transacción
    
            // Actualizar el balance del banco del servidor
            if (!saveServerBankBalance(serverBalance - amount, serverBalance, conn)) {
                return false;
            }
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerUUID);
            plugin.getEconomyLogger().log("Withdrawn " + amount + " from server bank to " + player.getName());
            conn.commit(); // Confirmar la transacción
            return true;
    
        } catch (SQLException e) {
            playerData.setBalance(playerData.getBalance() - amount);
            playerData.save();
            e.printStackTrace();
            return false;
        }
    }
    
    //#region depositFromPlayerToServer
    public boolean depositFromPlayerToServer(UUID playerUUID, double amount) {
        if (amount < 0) {
            return false;
        }
    
        PlayerData playerData = plugin.getDatabase().loadPlayerData(playerUUID);
        if (playerData == null) {
            return false;
        }
    
        double playerBalance = playerData.getBalance();
        if (playerBalance < amount) {
            return false;
        }
    
        playerData.setBalance(playerBalance - amount);
        boolean step = playerData.save();
        if (!step) {
            playerData.setBalance(playerBalance + amount);
            return false;
        }
        
        try (Connection conn = database.connect()) {
            conn.setAutoCommit(false); // Iniciar la transacción
        
            // Actualizar el balance del banco del servidor
            if (!saveServerBankBalance(getBalance() + amount, getBalance(), conn)) {
                return false;
            }
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerUUID);
            plugin.getEconomyLogger().log("Deposited " + amount + " from " + player.getName() + " to server bank");
            conn.commit(); // Confirmar la transacción
            return true;
    
        } catch (SQLException e) {
            playerData.setBalance(playerBalance + amount);
            playerData.save();
            e.printStackTrace();
            return false;
        }
    }
    public boolean setServerBankBalance(double newBalance) {
        try (Connection conn = database.connect()) {
            return saveServerBankBalance(newBalance, getBalance(), conn);
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getEconomyLogger().log("Error setting server bank balance: " + e.getMessage());
            return false;
        }
    }
    
    private boolean saveServerBankBalance(double newBalance, double oldBalance, Connection conn) throws SQLException {
        String sql = "UPDATE serverbank SET balance = ? WHERE balance = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setDouble(2, oldBalance);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            plugin.getEconomyLogger().log("Error updating server bank balance: " + e.getMessage());
            conn.rollback();
            throw e;
        }
    }
}
