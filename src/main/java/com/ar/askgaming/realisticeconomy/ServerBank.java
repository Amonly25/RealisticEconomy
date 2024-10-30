package com.ar.askgaming.realisticeconomy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ar.askgaming.realisticeconomy.datas.SQLiteDatabase;

import net.milkbowl.vault.economy.EconomyResponse;

public class ServerBank {

    private final SQLiteDatabase database;
    private Main plugin;

    public ServerBank(Main main) {
        plugin = main;
        database = plugin.getSqlDatabase();
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
            plugin.getServer().getLogger().warning("No se puede retirar un monto negativo.");
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "No se puede retirar un monto negativo.");
        }   
        double balance = getBalance();
        if (balance < amount) {
            plugin.getServer().getLogger().warning("No hay suficiente dinero en el banco del servidor.");
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "No hay suficiente dinero en el banco del servidor.");
        } 
        EconomyResponse e = modifyBalance(-amount);
        if (e.transactionSuccess()){
            return new EconomyResponse(amount, balance - amount, EconomyResponse.ResponseType.SUCCESS, "Retiro exitoso del banco, " + amount + " " + (balance - amount));
        } 
        return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "Error al retirar el monto.");
        
    }

    public EconomyResponse deposit(double amount) {
        if (amount < 0) {
            plugin.getServer().getLogger().warning("No se puede depositar un monto negativo.");
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "No se puede depositar un monto negativo.");
        }
        EconomyResponse e = modifyBalance(amount);
        if (e.transactionSuccess()){
            return new EconomyResponse(amount, getBalance(), EconomyResponse.ResponseType.SUCCESS, "Balance del banco actualizado, " + amount + " " + getBalance());
        } 
        return new EconomyResponse(0, getBalance(), EconomyResponse.ResponseType.FAILURE, "Error al depositar el monto.");
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
                    plugin.getServer().getLogger().info("Se ha modificado el balance del servidor en " + d);
                    return new EconomyResponse(d, newBalance, EconomyResponse.ResponseType.SUCCESS, "Balance modificado exitosamente.");
                } else {
                    plugin.getServer().getLogger().warning("No se encontró ningún registro para actualizar en 'serverbank'.");
                    return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "No se encontró ningún registro para actualizar en 'serverbank'.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "Error al modificar el balance.");
        }
    }

    public void setup(double amount) {
        // Comprobar si el monto es negativo
        if (amount < 0) {
            plugin.getServer().getLogger().warning("No se puede establecer un balance negativo.");
            return; // Salir del método si el monto es negativo
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
                    plugin.getServer().getLogger().info("Se ha establecido el balance del servidor en " + amount);
                } else {
                    plugin.getServer().getLogger().warning("No se encontró ningún registro para actualizar en 'serverbank'.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Imprimir traza de error en caso de fallo
        }
    }
}
