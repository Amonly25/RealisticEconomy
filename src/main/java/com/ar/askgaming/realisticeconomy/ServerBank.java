package com.ar.askgaming.realisticeconomy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ar.askgaming.realisticeconomy.datas.SQLiteDatabase;

import net.milkbowl.vault.economy.EconomyResponse;

public class ServerBank {

    private final SQLiteDatabase database;
    private RealisticEconomy plugin;

    public ServerBank(RealisticEconomy main) {
        plugin = main;
        database = plugin.getSqlDatabase();
    }

    private String getMsg(String key){
        return plugin.getLang().getFrom(key, plugin.getServerLang());
    }
    private void log(String key){
        plugin.getEconomyLogger().log(key);
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
    public EconomyResponse withdraw(double amount){
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.cant_negative"));
        }   
        double balance = getBalance();
        if (balance < amount) {
            log(getMsg("server_bank.no_enough"));
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("server_bank.no_enough"));
        } 
        EconomyResponse e = modifyBalance(-amount);
        if (e.transactionSuccess()){
            String s = getMsg("server_bank.withdraw").replace("%amount%", String.valueOf(amount));
            log(s);
            return new EconomyResponse(amount, balance - amount, EconomyResponse.ResponseType.SUCCESS, s);
        } 
        log(getMsg("transactions.error").replace("%action%","Withdraw bank"));
        return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%","Withdraw bank"));
        
    }

    public EconomyResponse deposit(double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, getMsg("economy.cant_negative"));
        }
        EconomyResponse e = modifyBalance(amount);
        if (e.transactionSuccess()){
            String s = getMsg("server_bank.deposit").replace("%amount%", String.valueOf(amount));
            log(s);
            return new EconomyResponse(amount, getBalance(), EconomyResponse.ResponseType.SUCCESS, s);
        } 
        String s = getMsg("transactions.error").replace("%action%","Deposit bank");
        log(s);
        return new EconomyResponse(0, e.balance, EconomyResponse.ResponseType.FAILURE, s);
    }

    private EconomyResponse modifyBalance(double d) {
        double balance = getBalance();
        double newBalance = balance + d;

        try (Connection conn = database.connect()) {
            String updateSql = "UPDATE serverbank SET balance = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                stmt.setDouble(1, newBalance);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    String s = getMsg("server_bank.new_balance").replace("%amount%", String.valueOf(newBalance));
                    return new EconomyResponse(d, newBalance, EconomyResponse.ResponseType.SUCCESS, s);
                } else {
                    String s = getMsg("transactions.error").replace("%action%","Modify bank balance database");
                    log(s);
                    return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, getMsg("transactions.error").replace("%action%","Modify bank balance"));
        }
    }

    public void setup(double amount) {

        if (amount < 0) {
            plugin.getServer().getLogger().warning("Cant setup a negative balance");
            return; 
        }
    
        // Conectar a la base de datos
        try (Connection conn = database.connect()) {
            // Consulta SQL para actualizar el balance en la tabla 'serverbank'
            String updateSql = "UPDATE serverbank SET balance = ?";
    
            try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                // Establecer el nuevo balance
                stmt.setDouble(1, amount); 
    
                // Ejecutar la actualización
                int rowsUpdated = stmt.executeUpdate();
    
                // Verificar si se actualizó alguna fila
                if (rowsUpdated > 0) {
                    plugin.getServer().getLogger().info("You have been setup the economy to " + amount);
                } else {
                    plugin.getServer().getLogger().warning("An error occurred while updating the database");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Imprimir traza de error en caso de fallo
        }
    }
}
