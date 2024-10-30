package com.ar.askgaming.realisticeconomy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
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
    }
 
    //#region createPlayerAccount

    public boolean createPlayerAccount(UUID uuid) {
        try (Connection conn = database.connect()) {
            String sql = "INSERT OR IGNORE INTO economy (uuid, balance) VALUES (?, 0)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    } 
    public boolean createPlayerAccount(Player player) {
        return createPlayerAccount(player.getUniqueId());
    } 

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return createPlayerAccount(offlinePlayer.getUniqueId());
    }
    @Override
    public boolean createPlayerAccount(String playerName, String arg1) {
        Player p = Bukkit.getPlayerExact(playerName);
        if (p != null) {
            return createPlayerAccount(p.getUniqueId());
        } else {
            return false;
        }
    }
    @Override
    public boolean createPlayerAccount(String arg0) {
        if (plugin.getServer().getPlayer(arg0) != null) {
            return createPlayerAccount(plugin.getServer().getPlayer(arg0));
        } else {
            return false;
        }
    }
    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String arg1) {
        if (offlinePlayer.isOnline()   ){
            return createPlayerAccount((Player) offlinePlayer);
        } else return false;
    }

    //#endregion

    //#region depositPlayer
    public EconomyResponse depositPlayer(UUID uuid, double amount) {
        String data = uuid.toString();
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(uuid), EconomyResponse.ResponseType.FAILURE, "No se puede depositar una cantidad negativa.");
        }
        
        try (Connection conn = database.connect()) {
            String sql = "INSERT OR IGNORE INTO economy (uuid, balance) VALUES (?, 0)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, data);
                stmt.executeUpdate();
            }

            String updateSql = "UPDATE economy SET balance = balance + ? WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setDouble(1, amount);
                stmt.setString(2, data);
                stmt.executeUpdate();
            }

            return new EconomyResponse(amount, getBalance(uuid), EconomyResponse.ResponseType.SUCCESS, "Depósito exitoso.");
        } catch (SQLException e) {
            e.printStackTrace();
            return new EconomyResponse(0, getBalance(uuid), EconomyResponse.ResponseType.FAILURE, "Error al depositar.");
        }
    }
    public EconomyResponse depositPlayer(Player player, double amount) {
        return depositPlayer(player.getUniqueId(), amount);
    }
    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        Player p = Bukkit.getPlayerExact(playerName);
        if (p != null){
            return depositPlayer(p.getUniqueId(), amount);
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(playerName);
            if (offPlayer.hasPlayedBefore()) {
                return depositPlayer(offPlayer.getUniqueId(), amount);
            } else {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "El jugador no existe.");
            }
        }
    }
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {
        if (offlinePlayer.hasPlayedBefore()){
            return depositPlayer(offlinePlayer.getUniqueId(), amount);
        }else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "El jugador no existe.");
        }
        
    }
    @Override
    public EconomyResponse depositPlayer(String playerName, String arg1, double amount) {
        return depositPlayer(playerName, amount);
    }
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer arg0, String arg1, double arg2) {
        return depositPlayer(arg0, arg2);
    }
    //#endregion

    //#region getBalance
    public double getBalance(UUID uuid) {
        try (Connection conn = database.connect()) {
            String sql = "SELECT balance FROM economy WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
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
    public double getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }
    @Override
    public double getBalance(String playerName) {
        Player p = Bukkit.getPlayerExact(playerName);
        if (p != null){
            return getBalance(p.getUniqueId());
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(playerName);
            if (offPlayer.hasPlayedBefore()) {
                return getBalance(offPlayer.getUniqueId());
            } else {
                return 0.0;
            }
        }
    }
    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.hasPlayedBefore()) {
            return getBalance(offlinePlayer.getUniqueId());
        } else {
            return 0;
        }
    }
    @Override
    public double getBalance(String arg0, String arg1) {
        return getBalance(arg0);
    }
    @Override
    public double getBalance(OfflinePlayer arg0, String arg1) {
        return getBalance(arg0);
    }
    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }
    @Override
    public boolean has(OfflinePlayer arg0, double arg1) {
        return getBalance(arg0) >= arg1;
    }
    @Override
    public boolean has(String arg0, String arg1, double arg2) {
        return getBalance(arg0) >= arg2;
    }
    @Override
    public boolean has(OfflinePlayer arg0, String arg1, double arg2) {
        return getBalance(arg0) >= arg2;
    }
    //#endregion

    //#region withdrawPlayer
    public EconomyResponse withdrawPlayer(UUID uuid, double amount) {

        double balance = getBalance(uuid);
        if (balance >= amount) {
            try (Connection conn = database.connect()) {
                String updateSql = "UPDATE economy SET balance = balance - ? WHERE uuid = ? AND balance >= ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setDouble(1, amount);
                    stmt.setString(2, uuid.toString());
                    stmt.setDouble(3, amount);
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        return new EconomyResponse(amount, getBalance(uuid), EconomyResponse.ResponseType.SUCCESS, "Retiro exitoso.");
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
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        Player p = Bukkit.getPlayerExact(playerName);
        if (p != null){
            return withdrawPlayer(p.getUniqueId(), amount);
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(playerName);
            if (offPlayer.hasPlayedBefore()) {
                return withdrawPlayer(offPlayer.getUniqueId(), amount);
            } else {
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "El jugador no existe.");
            }
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        if (offlinePlayer.hasPlayedBefore()){
            return withdrawPlayer(offlinePlayer.getUniqueId(), amount);
        }else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "El jugador no existe.");
        }
    }
    @Override
    public EconomyResponse withdrawPlayer(String arg0, String arg1, double arg2) {
        return withdrawPlayer(arg0, arg2);
    }
    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer arg0, String arg1, double arg2) {
        return withdrawPlayer(arg0, arg2);
    }
    //#endregion

    //#region hasAccount
    public boolean hasAccount(String playerName) {
        Player p = Bukkit.getPlayerExact(playerName);
        if (p != null){
            return p.hasPlayedBefore();
        } else {
            @SuppressWarnings("deprecation")
            OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(playerName);
            return offPlayer.hasPlayedBefore();
        }
    }
    @Override
    public boolean hasAccount(OfflinePlayer arg0) {
        return arg0.hasPlayedBefore();
    }
    @Override
    public boolean hasAccount(String arg0, String arg1) {
        return hasAccount(arg0);
    }
    @Override
    public boolean hasAccount(OfflinePlayer arg0, String arg1) {
        return hasAccount(arg0);
    }
    //#endregion

    @Override
    public String currencyNamePlural() {
        return plugin.getConfig().getString("currency_name_plural","Undefined");
    }
    @Override
    public String currencyNameSingular() {
        return plugin.getConfig().getString("currency_name_singular","Undefined");
    }
    @Override
    public EconomyResponse deleteBank(String arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteBank'");
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
    public List<String> getBanks() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getBanks'");
    }
    @Override
    public String getName() {
        return "RealisticEconomy";
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

}
