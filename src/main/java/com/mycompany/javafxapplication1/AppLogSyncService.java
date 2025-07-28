package com.mycompany.javafxapplication1;
import java.sql.*;
public class AppLogSyncService {

    private static final int BATCH_SIZE = 500;

    public static void syncLogsToMySQL() {
        String selectSql = "SELECT log, date_and_time FROM appLogs";
        String insertSql =
                "INSERT INTO appLogs (log, date_and_time) VALUES (?, ?)";
        try (Connection local = DB.getConnection();
             Connection remote = MySQLDB.getConnection();
             Statement stmt = local.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql);
             PreparedStatement ps = remote.prepareStatement(insertSql)) {

            remote.setAutoCommit(false);
            int count = 0;
            while (rs.next()) {
                ps.setString(1, rs.getString("log"));
                ps.setTimestamp(2, rs.getTimestamp("date_and_time"));
                ps.addBatch();

                if (++count % BATCH_SIZE == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
            remote.commit();
            System.out.println("[Sync] appLogs pushed to MySQL.");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void syncLogsFromMySQL() {
        String selectSql = "SELECT log, date_and_time FROM appLogs";
        String insertSql =
                "INSERT OR IGNORE INTO appLogs (log, date_and_time) VALUES (?, ?)";
        try (Connection local = DB.getConnection();
             Connection remote = MySQLDB.getConnection();
             Statement stmt = remote.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql);
             PreparedStatement ps = local.prepareStatement(insertSql)) {

            local.setAutoCommit(false);
            int count = 0;
            while (rs.next()) {
                ps.setString(1, rs.getString("log"));
                ps.setTimestamp(2, rs.getTimestamp("date_and_time"));
                ps.addBatch();

                if (++count % BATCH_SIZE == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
            local.commit();
            System.out.println("[Sync] appLogs pulled from MySQL.");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
