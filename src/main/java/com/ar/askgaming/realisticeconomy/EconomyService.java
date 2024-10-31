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
    private String getMsg(String key){
        return plugin.getLang().getFrom(key, plugin.getServerLang());
    }
    private void log(String key){
        plugin.getEconomyLogger().log(key);
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
        OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(uuid);
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(uuid), EconomyResponse.ResponseType.FAILURE, getMsg("economy.cant_negative"));
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
            if (offPlayer.isOnline()){
                offPlayer.getPlayer().sendMessage(getMsg("bank.deposit_receiver").replace("%amount%", String.valueOf(amount)));
            }
            String s = getMsg("bank.deposit_player").replace("%amount%", String.valueOf(amount)).replace("%player%", offPlayer.getName());
            log(s);
            return new EconomyResponse(amount, getBalance(uuid), EconomyResponse.ResponseType.SUCCESS, s);
        } catch (SQLException e) {
            e.printStackTrace();
            return new EconomyResponse(amount, getBalance(uuid), EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%","Player Deposit"));
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
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_found"));
            }
        }
    }
    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {
        if (offlinePlayer.hasPlayedBefore()){
            return depositPlayer(offlinePlayer.getUniqueId(), amount);
        }else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_found"));
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
        OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(uuid);
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
                        if (offPlayer.isOnline()){
                            offPlayer.getPlayer().sendMessage(getMsg("bank.withdraw_receiver").replace("%amount%", String.valueOf(amount)));
                        }
                        String s = getMsg("server_bank.withdraw").replace("%amount%", String.valueOf(amount)).replace("%player%", offPlayer.getName());
                        log(s);
                        return new EconomyResponse(amount, getBalance(uuid), EconomyResponse.ResponseType.SUCCESS, s);
                    } else {
                        return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%","Player Withdraw Database"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%","Player Withdraw"));
            }
        } else {
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_enough"));
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
                return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_found"));
            }
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        if (offlinePlayer.hasPlayedBefore()){
            return withdrawPlayer(offlinePlayer.getUniqueId(), amount);
        }else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_found"));
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

    public EconomyResponse playerPayPlayer(UUID payer, UUID receiver, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, getBalance(payer), EconomyResponse.ResponseType.FAILURE, getMsg("economy.cant_negative"));
        }
    
        double payerBalance = getBalance(payer);
    
        if (payerBalance < amount) {
            return new EconomyResponse(0, payerBalance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.payer_not_enough"));
        }
    
        try (Connection conn = database.connect()) {
            conn.setAutoCommit(false); // Comenzar una transacción
    
            try {
                // Asegurar que el receptor existe en la base de datos
                String ensureReceiverSql = "INSERT OR IGNORE INTO economy (uuid, balance) VALUES (?, 0)";
                try (PreparedStatement ensureStmt = conn.prepareStatement(ensureReceiverSql)) {
                    ensureStmt.setString(1, receiver.toString());
                    ensureStmt.executeUpdate();
                }
    
                // Actualizar el saldo del pagador
                String withdrawSql = "UPDATE economy SET balance = balance - ? WHERE uuid = ? AND balance >= ?";
                try (PreparedStatement withdrawStmt = conn.prepareStatement(withdrawSql)) {
                    withdrawStmt.setDouble(1, amount);
                    withdrawStmt.setString(2, payer.toString());
                    withdrawStmt.setDouble(3, amount);
                    int rowsUpdated = withdrawStmt.executeUpdate();
                    if (rowsUpdated == 0) {
                        conn.rollback();
                        return new EconomyResponse(0, payerBalance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%","Player Withdraw Database"));
                    }
                }
    
                // Actualizar el saldo del receptor
                String depositSql = "UPDATE economy SET balance = balance + ? WHERE uuid = ?";
                try (PreparedStatement depositStmt = conn.prepareStatement(depositSql)) {
                    depositStmt.setDouble(1, amount);
                    depositStmt.setString(2, receiver.toString());
                    depositStmt.executeUpdate();
                }
    
                conn.commit(); // Finalizar la transacción
    
                double newPayerBalance = getBalance(payer);
                //double newReceiverBalance = getBalance(receiverUUID);
                if (Bukkit.getOfflinePlayer(receiver).isOnline()){
                    Bukkit.getPlayer(receiver).sendMessage(getMsg("transactions.receive").replace("%amount%", String.valueOf(amount)).replace("%player%", Bukkit.getPlayer(payer).getName()));
                }
                String s = getMsg("transactions.pay_succed").replace("%amount%", String.valueOf(amount)).replace("%receiver%", Bukkit.getOfflinePlayer(receiver).getName()).replace("%payer%", Bukkit.getOfflinePlayer(payer).getName());
                log(s);
                return new EconomyResponse(amount, newPayerBalance, EconomyResponse.ResponseType.SUCCESS, s);
            } catch (SQLException e) {
                conn.rollback(); // Revertir la transacción en caso de error
                e.printStackTrace();
                return new EconomyResponse(0, payerBalance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%","Player Pay Player Database rollback"));
            } finally {
                conn.setAutoCommit(true); // Restablecer el modo de confirmación automática
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new EconomyResponse(0, payerBalance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%","Player Pay Player Database"));
        }
    }

    public EconomyResponse playerPayPlayer(Player payer, String receiverName, double amount) {

        @SuppressWarnings("deprecation")
        OfflinePlayer receiver = Bukkit.getOfflinePlayer(receiverName);
        if (receiver.hasPlayedBefore()) {
            return playerPayPlayer(payer.getUniqueId(), receiver.getUniqueId(), amount);
        } else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_found"));
        }
    }
    @SuppressWarnings("deprecation")
    public EconomyResponse playerPayPlayer(String payerName, String receiverName, double amount) {

        OfflinePlayer receiver = Bukkit.getOfflinePlayer(receiverName);
        OfflinePlayer payer = Bukkit.getOfflinePlayer(payerName);
        if (receiver.hasPlayedBefore() && payer.hasPlayedBefore()) {
            return playerPayPlayer(payer.getUniqueId(), receiver.getUniqueId(), amount);
        } else {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.player_not_found"));
        }
    }


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
        return false;
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
