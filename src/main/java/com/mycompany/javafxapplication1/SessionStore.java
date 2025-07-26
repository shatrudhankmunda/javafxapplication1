package com.mycompany.javafxapplication1;
import java.sql.*;

public class SessionStore {
    private static final String CREATE_SESSION = "CREATE TABLE IF NOT EXISTS session (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL, login_time DATETIME DEFAULT CURRENT_TIMESTAMP, logout_time DATETIME);";
    private static final String DB_URL = "jdbc:sqlite:comp20081.db";
    static {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            Statement stmt = conn.createStatement();
            stmt.execute(CREATE_SESSION);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void saveLogin(String username) {
    String sql = "INSERT INTO session (username) VALUES (?)";

    try (Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, username);
        stmt.executeUpdate();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
public static void saveLogout(String username) {
    String sql = " UPDATE session SET logout_time = CURRENT_TIMESTAMP WHERE id = (SELECT id FROM session WHERE username = ? AND logout_time IS NULL ORDER BY login_time DESC LIMIT 1);";

    try (Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, username);
        stmt.executeUpdate();

    } catch (SQLException e) {
        e.printStackTrace();
    }
}


    public static String getLastUser() {
        String sql = "SELECT username FROM session LIMIT 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void clearSession() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             var stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM session");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
