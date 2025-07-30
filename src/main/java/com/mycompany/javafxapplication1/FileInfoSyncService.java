package com.mycompany.javafxapplication1;

import java.sql.*;

public class FileInfoSyncService {
    private static final int BATCH_SIZE = 500;

    // Push local changes (including inserts, updates, deletes) to MySQL
    public static void syncToMySQL() throws SQLException {
        String sqlSelect = "SELECT userName, fileName_, fileSize, ACL, chunk1id, chunk2id, chunk3id, chunk4id, encryptionKey, CRC32, dateOfCreation, dateOfLastModification, Status, last_modified FROM fileInfo WHERE last_modified > ?";
        String sqlUpsert = """
                    INSERT INTO fileInfo (userName, fileName_, fileSize, ACL, chunk1id, chunk2id, chunk3id, chunk4id,
                                          encryptionKey, CRC32, dateOfCreation, dateOfLastModification, Status, last_modified)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                       fileSize = VALUES(fileSize),
                       ACL = VALUES(ACL),
                       chunk1id = VALUES(chunk1id),
                       chunk2id = VALUES(chunk2id),
                       chunk3id = VALUES(chunk3id),
                       chunk4id = VALUES(chunk4id),
                       encryptionKey = VALUES(encryptionKey),
                       CRC32 = VALUES(CRC32),
                       dateOfLastModification = VALUES(dateOfLastModification),
                       Status = VALUES(Status),
                       last_modified = VALUES(last_modified),
                """;

        try (Connection local = DB.getConnection();
             Connection remote = MySQLDB.getConnection()) {

            remote.setAutoCommit(false);

            Timestamp lastSync = getLastSyncTimestamp(local, "fileInfo");
            try (PreparedStatement psSelect = local.prepareStatement(sqlSelect);
                 ResultSet rs = psSelect.executeQuery()) {
                psSelect.setTimestamp(1, lastSync);
                int count = 0;
                try (PreparedStatement psUpsert = remote.prepareStatement(sqlUpsert)) {
                    while (rs.next()) {
                        // bind parameters
                        int idx = 1;
                        psUpsert.setString(idx++, rs.getString("userName"));
                        psUpsert.setString(idx++, rs.getString("fileName_"));
                        psUpsert.setLong(idx++, rs.getLong("fileSize"));
                        psUpsert.setString(idx++, rs.getString("ACL"));
                        psUpsert.setString(idx++, rs.getString("chunk1id"));
                        psUpsert.setString(idx++, rs.getString("chunk2id"));
                        psUpsert.setString(idx++, rs.getString("chunk3id"));
                        psUpsert.setString(idx++, rs.getString("chunk4id"));
                        psUpsert.setString(idx++, rs.getString("encryptionKey"));
                        psUpsert.setLong(idx++, rs.getLong("CRC32"));
                        psUpsert.setDate(idx++, rs.getDate("dateOfCreation"));
                        psUpsert.setDate(idx++, rs.getDate("dateOfLastModification"));
                        psUpsert.setString(idx++, rs.getString("Status"));
                        psUpsert.setTimestamp(idx++, rs.getTimestamp("last_modified"));
                        psUpsert.addBatch();
                        if (++count % BATCH_SIZE == 0) {
                            psUpsert.executeBatch();
                        }
                    }
                    psUpsert.executeBatch();
                }
            }
            updateLastSyncTimestamp(local, "fileInfo", new Timestamp(System.currentTimeMillis()));
            remote.commit();
        }
    }

    // Pull updates (and deletes) from MySQL to local
    public static void syncFromMySQL() throws SQLException {
        String sqlSelect = "SELECT userName, fileName_, fileSize, ACL, chunk1id, chunk2id, chunk3id, chunk4id, encryptionKey, CRC32, dateOfCreation, dateOfLastModification, Status, last_modified FROM fileInfo WHERE last_modified > ?";
        String sqlInsertOrReplace = """
                    INSERT OR REPLACE INTO fileInfo (userName, fileName_, fileSize, ACL, chunk1id, chunk2id, chunk3id, chunk4id,
                                                     encryptionKey, CRC32, dateOfCreation, dateOfLastModification, Status, last_modified)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection local = DB.getConnection();
             Connection remote = MySQLDB.getConnection()) {

            local.setAutoCommit(false);
            Timestamp lastSync = getLastSyncTimestamp(local, "fileInfo");

            try (PreparedStatement psSelect = remote.prepareStatement(sqlSelect)) {
                psSelect.setTimestamp(1, lastSync);
                try (ResultSet rs = psSelect.executeQuery();
                     PreparedStatement psLocal = local.prepareStatement(sqlInsertOrReplace)) {

                    int count = 0;
                    while (rs.next()) {
                        int  idx= 1;
                        psLocal.setString(idx++, rs.getString("userName"));
                        psLocal.setString(idx++, rs.getString("fileName_"));
                        psLocal.setLong(idx++, rs.getLong("fileSize"));
                        psLocal.setString(idx++, rs.getString("ACL"));
                        psLocal.setString(idx++, rs.getString("chunk1id"));
                        psLocal.setString(idx++, rs.getString("chunk2id"));
                        psLocal.setString(idx++, rs.getString("chunk3id"));
                        psLocal.setString(idx++, rs.getString("chunk4id"));
                        psLocal.setString(idx++, rs.getString("encryptionKey"));
                        psLocal.setLong(idx++, rs.getLong("CRC32"));
                        psLocal.setDate(idx++, rs.getDate("dateOfCreation"));
                        psLocal.setDate(idx++, rs.getDate("dateOfLastModification"));
                        psLocal.setString(idx++, rs.getString("Status"));
                        psLocal.setTimestamp(idx++, rs.getTimestamp("last_modified"));
                        psLocal.addBatch();
                        if (++count % BATCH_SIZE == 0) {
                            psLocal.executeBatch();
                        }
                    }
                    psLocal.executeBatch();
                }
            }
            updateLastSyncTimestamp(local, "fileInfo", new Timestamp(System.currentTimeMillis()));
            local.commit();
        }
    }

    public static Timestamp getLastSyncTimestamp(Connection local, String tableName) throws SQLException {
        String sql = "SELECT last_sync FROM sync_metadata WHERE table_name = ?";
        try (PreparedStatement ps = local.prepareStatement(sql)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp(1);
                }
            }
        }
        // If not found, default to epoch (or a sensible initial threshold)
        return Timestamp.valueOf("1970-01-01 00:00:00");
    }

     public static void updateLastSyncTimestamp(Connection local, String tableName, Timestamp ts) throws SQLException {
        String sql = """
                    INSERT INTO sync_metadata(table_name, last_sync)
                    VALUES (?, ?)
                    ON CONFLICT(table_name) DO UPDATE SET last_sync = excluded.last_sync
                """;
        try (PreparedStatement ps = local.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setTimestamp(2, ts);
            ps.executeUpdate();
        }
    }

}
