package com.mycompany.javafxapplication1;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLDB {
    private static final String URL = "jdbc:mysql://localhost:3306/cloudstore_remote";
    private static final String USER = "clouduser";
    private static final String PASSWORD = "cloudpass";

    static {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                createUsersTable(conn);
                createSessionTable(conn);
                createFileInfoTable(conn);
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
                                  updated_ts DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  last_modified DATE
                              );
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            Logger.getLogger(FilemanagerController.class.getName()).log(Level.INFO,"users table created");
        }
    }

    private static void createAppLogs(Connection conn) throws SQLException {
        String sql = """
                    CREATE TABLE IF NOT EXISTS appLogs (
                          log VARCHAR(1000),
                          date_and_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                          last_modified DATE
                      )
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            Logger.getLogger(FilemanagerController.class.getName()).log(Level.INFO,"appLogs table created");
        }
    }

    private static void createSessionTable(Connection conn) throws SQLException {
        String sql = """
                    CREATE TABLE IF NOT EXISTS session (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(100) NOT NULL,
                        login_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                        logout_time DATETIME,
                        last_modified DATE
                    );
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            Logger.getLogger(FilemanagerController.class.getName()).log(Level.INFO,"session table created");
        }
    }

    private static void createFileInfoTable(Connection conn) throws SQLException {
        String sql = """
    CREATE TABLE IF NOT EXISTS fileInfo (
      userName VARCHAR(100),
      filename_ VARCHAR(255),
      fileSize BIGINT,
      acl VARCHAR(100),
      chunk1id VARCHAR(255),
      chunk2id VARCHAR(255),
      chunk3id VARCHAR(255),
      chunk4id VARCHAR(255),
      encryptionKey VARCHAR(255),
      CRC32 INT,
      dateOfCreation DATE,
      dateOfLastModification DATE,
      last_modified DATE,
      Status VARCHAR(50) DEFAULT 'Exists',
      INDEX (userName),
      FOREIGN KEY (userName) REFERENCES users(userName)
        ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB;
""";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            Logger.getLogger(FilemanagerController.class.getName()).log(Level.INFO,"fileInfo table created");
        }
    }

}
