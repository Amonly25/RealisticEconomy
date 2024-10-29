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
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
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
}
