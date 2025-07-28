package com.mycompany.javafxapplication1;

import java.sql.*;
public class SessionSyncService {

    public static void syncSessionsToMySQL() {
        String selectSQL = "SELECT * FROM session WHERE id NOT IN (SELECT id FROM remote_synced_sessions)";

        try (
            Connection localConn = DB.getConnection();
            Connection remoteConn = MySQLDB.getConnection();
        ) {
            Statement localStmt = localConn.createStatement();
            ResultSet rs = localStmt.executeQuery("SELECT * FROM session");

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String loginTime = rs.getString("login_time");
                String logoutTime = rs.getString("logout_time");

                // Push to remote MySQL
                PreparedStatement insert = remoteConn.prepareStatement("""
                    INSERT INTO session (username, login_time, logout_time)
                    VALUES (?, ?, ?)
                """);

                insert.setString(1, username);
                insert.setString(2, loginTime);
                insert.setString(3, logoutTime);
                insert.executeUpdate();
            }

            System.out.println("[Sync] Sessions synced to MySQL successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
