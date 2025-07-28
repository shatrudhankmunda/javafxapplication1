package com.mycompany.javafxapplication1;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLDB {
    private static final String URL = "jdbc:mysql://localhost:3306/cloudstore_remote";
    private static final String USER = "clouduser";
    private static final String PASSWORD = "cloudpass";

    static {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                createSessionTable(conn);
                createFileInfoTable(conn);
                createUsersTable(conn);
                createAppLogs(conn);
                System.out.println("MySQL tables ensured (created if not exists).");
            }
        } catch (SQLException e) {
            System.err.println("MySQL Initialization Error: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to MySQL!");
            return conn;
        } catch (SQLException e) {
            System.err.println("MySQL Connection Error: " + e.getMessage());
            return null;
        }

    }

    private static void createUsersTable(Connection conn) throws SQLException {
        String sql = """
                    CREATE TABLE IF NOT EXISTS users (
                                  username VARCHAR(100) PRIMARY KEY,
                                  password VARCHAR(255) NOT NULL,
                                  created_ts DATETIME DEFAULT CURRENT_TIMESTAMP,
                                  updated_ts DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                              );
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static void createAppLogs(Connection conn) throws SQLException {
        String sql = """
                    CREATE TABLE appLogs (
                          log TEXT,
                          date_and_time DATETIME DEFAULT CURRENT_TIMESTAMP
                      )
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static void createSessionTable(Connection conn) throws SQLException {
        String sql = """
                    CREATE TABLE IF NOT EXISTS session (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(100) NOT NULL,
                        login_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                        logout_time DATETIME
                    );
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static void createFileInfoTable(Connection conn) throws SQLException {
        String sql = """
                    CREATE TABLE "fileInfo" (
                    	"userName"	TEXT,
                    	"fileName_"	TEXT,
                    	"fileSize"	INTEGER,
                    	"ACL"	TEXT,
                    	"chunk1id"	TEXT,
                    	"chunk2id"	TEXT,
                    	"chunk3id"	TEXT,
                    	"chunk4id"	TEXT,
                    	"encryptionKey"	TEXT,
                    	"CRC32"	INTEGER,
                    	"dateOfCreation"	DATE,
                    	"dateOfLastModification"	DATE,
                    	"Status"	TEXT DEFAULT 'Exists',
                    	FOREIGN KEY("userName") REFERENCES "Users"("name") ON DELETE CASCADE ON UPDATE CASCADE
                    );
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

}
