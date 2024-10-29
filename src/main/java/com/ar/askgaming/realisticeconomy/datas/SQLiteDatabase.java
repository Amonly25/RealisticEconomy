package com.ar.askgaming.realisticeconomy.datas;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.ar.askgaming.realisticeconomy.Main;

public class SQLiteDatabase {

    private final String url;
    private final Main plugin;

    public SQLiteDatabase(Main main, String dbFile) {
        url = "jdbc:sqlite:" + dbFile;
        plugin = main;

        createTable();
        createServerBankTable();
    }

    public Connection connect() {

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS economy ("
                + "uuid TEXT PRIMARY KEY,"
                + "balance REAL"
                + ");";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void createServerBankTable() {
        String createSql = "CREATE TABLE IF NOT EXISTS serverbank ("
                         + "balance REAL DEFAULT 1000.0" // Puedes establecer un valor por defecto aquí
                         + ");";
    
        try (Connection conn = connect(); 
             PreparedStatement createStmt = conn.prepareStatement(createSql)) {
    
            // Crear la tabla
            createStmt.execute();
            System.out.println("Tabla 'serverbank' creada o ya existe.");
    
            // Insertar un registro inicial solo si la tabla fue creada
            String insertSql = "INSERT OR IGNORE INTO serverbank (balance) VALUES (1000.0)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.executeUpdate();
                System.out.println("Registro inicial insertado en 'serverbank'.");
            }
    
        } catch (SQLException e) {
            System.err.println("Error al crear la tabla 'serverbank' o insertar el registro inicial.");
            e.printStackTrace();
        }
    }
}
