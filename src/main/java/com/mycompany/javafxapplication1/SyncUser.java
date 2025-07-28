package com.mycompany.javafxapplication1;
import java.sql.*;

public class SyncUser {

    public static void syncUsersFromMySQL() {
    String sqlSelect = "SELECT username, password, created_ts, updated_ts FROM users";
    String sqlInsertOrReplace = """
        INSERT OR REPLACE INTO users (username, password, created_ts, updated_ts)
        VALUES (?, ?, ?, ?)
    """;

    try (Connection localConn = DB.getConnection();
         Connection remoteConn = MySQLDB.getConnection();
         Statement stmt = remoteConn.createStatement();
         ResultSet rs = stmt.executeQuery(sqlSelect);
         PreparedStatement ps = localConn.prepareStatement(sqlInsertOrReplace)) {

        localConn.setAutoCommit(false);
        int count = 0;

        while (rs.next()) {
            ps.setString(1, rs.getString("username"));
            ps.setString(2, rs.getString("password"));
            ps.setTimestamp(3, rs.getTimestamp("created_ts"));
            ps.setTimestamp(4, rs.getTimestamp("updated_ts"));
            ps.addBatch();

            if (++count % 500 == 0) {
                ps.executeBatch();
            }
        }
        ps.executeBatch();
        localConn.commit();
        System.out.println("[Sync] Users pulled with timestamps to SQLite.");
    } catch (SQLException e) {
        e.printStackTrace();
    }
}


    public static void syncUsersToMySQL() {
    String sqlSelect = "SELECT username, password, created_ts FROM users";
    String sqlUpsert = """
        INSERT INTO users (username, password, created_ts)
        VALUES (?, ?, ?)
        ON DUPLICATE KEY UPDATE password = VALUES(password)
    """;

    try (Connection localConn = DB.getConnection();
         Connection remoteConn = MySQLDB.getConnection();
         Statement stmt = localConn.createStatement();
         ResultSet rs = stmt.executeQuery(sqlSelect);
         PreparedStatement ps = remoteConn.prepareStatement(sqlUpsert)) {

        remoteConn.setAutoCommit(false);
        int count = 0;

        while (rs.next()) {
            ps.setString(1, rs.getString("username"));
            ps.setString(2, rs.getString("password"));
            ps.setTimestamp(3, rs.getTimestamp("created_ts"));
            ps.addBatch();

            if (++count % 500 == 0) {
                ps.executeBatch();
            }
        }
        ps.executeBatch();
        remoteConn.commit();
        System.out.println("[Sync] Users pushed with timestamps to MySQL.");
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

}
