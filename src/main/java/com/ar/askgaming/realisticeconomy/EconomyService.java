package com.ar.askgaming.realisticeconomy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.ar.askgaming.realisticeconomy.datas.SQLiteDatabase;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class EconomyService implements Economy {

    private final SQLiteDatabase database;
    private Main plugin;

    public EconomyService(Main main) {
        plugin = main;
        database = plugin.getSqlDatabase();
        database.createTable();
    }
 
    @Override
    public EconomyResponse bankBalance(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bankBalance'");
    }
    @Override
    public EconomyResponse bankDeposit(String arg0, double arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bankDeposit'");
    }
    @Override
    public EconomyResponse bankHas(String arg0, double arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bankHas'");
    }
    @Override
    public EconomyResponse bankWithdraw(String arg0, double arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'bankWithdraw'");
    }
    @Override
    public EconomyResponse createBank(String arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createBank'");
    }
    @Override
    public EconomyResponse createBank(String arg0, OfflinePlayer arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createBank'");
    }
    public boolean createPlayerAccount(String playerName) {
        try (Connection conn = database.connect()) {
            String sql = "INSERT OR IGNORE INTO economy (uuid, balance) VALUES (?, 0)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean createPlayerAccount(OfflinePlayer arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createPlayerAccount'");
    }
    @Override
    public boolean createPlayerAccount(String arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createPlayerAccount'");
    }
    @Override
    public boolean createPlayerAccount(OfflinePlayer arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createPlayerAccount'");
    }
    @Override
    public String currencyNamePlural() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'currencyNamePlural'");
    }
    @Override
    public String currencyNameSingular() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'currencyNameSingular'");
    }
    @Override
    public EconomyResponse deleteBank(String arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteBank'");
    }
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(playerName), EconomyResponse.ResponseType.FAILURE, "No se puede depositar una cantidad negativa.");
        }
        try (Connection conn = database.connect()) {
            String sql = "INSERT OR IGNORE INTO economy (uuid, balance) VALUES (?, 0)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                stmt.executeUpdate();
            }

            String updateSql = "UPDATE economy SET balance = balance + ? WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setDouble(1, amount);
                stmt.setString(2, playerName);
                stmt.executeUpdate();
            }

            return new EconomyResponse(amount, getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, null);
        } catch (SQLException e) {
            e.printStackTrace();
            return new EconomyResponse(0, getBalance(playerName), EconomyResponse.ResponseType.FAILURE, "Error al depositar.");
        }
    }
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer arg0, double arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'depositPlayer'");
    }
    @Override
    public EconomyResponse depositPlayer(String arg0, String arg1, double arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'depositPlayer'");
    }
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer arg0, String arg1, double arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'depositPlayer'");
    }
    @Override
    public String format(double arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'format'");
    }
    @Override
    public int fractionalDigits() {
        return 2;
    }
    @Override
    public double getBalance(String playerName) {
        try (Connection conn = database.connect()) {
            String sql = "SELECT balance FROM economy WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0; // Retorna 0 si no se encuentra el jugador
    }
    @Override
    public double getBalance(OfflinePlayer arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBalance'");
    }
    @Override
    public double getBalance(String arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBalance'");
    }
    @Override
    public double getBalance(OfflinePlayer arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBalance'");
    }
    @Override
    public List<String> getBanks() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBanks'");
    }
    @Override
    public String getName() {
        return "RealisticEconomy";
    }
    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }
    @Override
    public boolean has(OfflinePlayer arg0, double arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'has'");
    }
    @Override
    public boolean has(String arg0, String arg1, double arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'has'");
    }
    @Override
    public boolean has(OfflinePlayer arg0, String arg1, double arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'has'");
    }
    @Override
    public boolean hasAccount(String playerName) {
        try (Connection conn = database.connect()) {
            String sql = "SELECT COUNT(*) FROM economy WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, playerName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public boolean hasAccount(OfflinePlayer arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasAccount'");
    }
    @Override
    public boolean hasAccount(String arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasAccount'");
    }
    @Override
    public boolean hasAccount(OfflinePlayer arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasAccount'");
    }
    @Override
    public boolean hasBankSupport() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hasBankSupport'");
    }
    @Override
    public EconomyResponse isBankMember(String arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBankMember'");
    }
    @Override
    public EconomyResponse isBankMember(String arg0, OfflinePlayer arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBankMember'");
    }
    @Override
    public EconomyResponse isBankOwner(String arg0, String arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBankOwner'");
    }
    @Override
    public EconomyResponse isBankOwner(String arg0, OfflinePlayer arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBankOwner'");
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {

        double balance = getBalance(playerName);
        if (balance >= amount) {
            try (Connection conn = database.connect()) {
                String updateSql = "UPDATE economy SET balance = balance - ? WHERE uuid = ? AND balance >= ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setDouble(1, amount);
                    stmt.setString(2, playerName);
                    stmt.setDouble(3, amount);
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        return new EconomyResponse(amount, getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, null);
                    } else {
                        return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "No se pudo actualizar el balance.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "Error al retirar.");
            }
        } else {
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "No hay suficiente saldo.");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer arg0, double arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdrawPlayer'");
    }
    @Override
    public EconomyResponse withdrawPlayer(String arg0, String arg1, double arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdrawPlayer'");
    }
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer arg0, String arg1, double arg2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withdrawPlayer'");
    }
}
