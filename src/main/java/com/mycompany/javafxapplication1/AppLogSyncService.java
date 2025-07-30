package com.mycompany.javafxapplication1;
import java.sql.*;
public class AppLogSyncService {

    private static final int BATCH_SIZE = 500;

    public static void syncLogsToMySQL() {
        String selectSql = "SELECT log, date_and_time, last_modified FROM appLogs Where last_modified > ?";
        String insertSql =
                "INSERT INTO appLogs (log, date_and_time) VALUES (?, ?, ?)";
        try (Connection local = DB.getConnection();
             Connection remote = MySQLDB.getConnection()){

            remote.setAutoCommit(false);
            Timestamp lastSync = FileInfoSyncService.getLastSyncTimestamp(local, "appLogs");

            try (PreparedStatement psSelect = local.prepareStatement(selectSql);
                 ResultSet rs = psSelect.executeQuery()) {
                psSelect.setTimestamp(1, lastSync);
                int count = 0;
                try (PreparedStatement psUpsert = remote.prepareStatement(insertSql)) {
                    while (rs.next()) {
                        // bind parameters
                        int idx = 1;
                        psUpsert.setString(idx++, rs.getString("log"));
                        psUpsert.setTimestamp(idx++, rs.getTimestamp("date_and_time"));
                        psUpsert.setTimestamp(idx++, rs.getTimestamp("last_modified"));
                        psUpsert.addBatch();
                        if (++count % BATCH_SIZE == 0) {
                            psUpsert.executeBatch();
                        }
                    }
                    psUpsert.executeBatch();
                }
            }
            FileInfoSyncService.updateLastSyncTimestamp(local, "appLogs", new Timestamp(System.currentTimeMillis()));
            remote.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void syncLogsFromMySQL() {
        String selectSql = "SELECT log, date_and_time FROM appLogs WHERE last_modified > ?";
        String insertSql =
                "INSERT OR REPLACE INTO appLogs (log, date_and_time, last_modified) VALUES (?, ?, ?)";
        try (Connection local = DB.getConnection();
             Connection remote = MySQLDB.getConnection()) {
            local.setAutoCommit(false);
            Timestamp lastSync = FileInfoSyncService.getLastSyncTimestamp(local, "fileInfo");
            try (PreparedStatement psSelect = remote.prepareStatement(selectSql)) {
                psSelect.setTimestamp(1, lastSync);
               try (ResultSet rs = psSelect.executeQuery();
                 PreparedStatement ps = local.prepareStatement(insertSql)) {
                   int count = 0;
                   while (rs.next()) {
                       ps.setString(1, rs.getString("log"));
                       ps.setTimestamp(2, rs.getTimestamp("date_and_time"));
                       ps.setTimestamp(3, rs.getTimestamp("last_modified"));
                       ps.addBatch();

                       if (++count % BATCH_SIZE == 0) {
                           ps.executeBatch();
                       }
                   }
                   ps.executeBatch();
               }
            }
            FileInfoSyncService.updateLastSyncTimestamp(local, "appLogs", new Timestamp(System.currentTimeMillis()));
            local.commit();
            System.out.println("[Sync] appLogs pulled from MySQL.");
        }catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
