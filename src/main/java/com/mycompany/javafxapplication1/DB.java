/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package com.mycompany.javafxapplication1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ntu-user
 */
public class DB {
    private static final String fileName = "jdbc:sqlite:comp20081.db";
    private final int timeout = 30;
    private static final String dataBaseName = "COMP20081";
    private String dataBaseTableName;
    Connection connection = null;
    private final Random random = new SecureRandom();
    private static final String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private final int iterations = 10000;
    private final int keylength = 256;
    private String saltValue;

    /**
     * @brief constructor - generates the salt if it doesn't exists or load it from the file .salt
     */
    DB(String dbname) {
        try {
            File fp = new File(".salt");
            dataBaseTableName = dbname;
            if (!fp.exists()) {
                saltValue = this.getSaltvalue(30);
                FileWriter myWriter = new FileWriter(fp);
                myWriter.write(saltValue);
                myWriter.close();
            } else {
                Scanner myReader = new Scanner(fp);
                while (myReader.hasNextLine()) {
                    saltValue = myReader.nextLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @brief create a new table
     * @param tableName name of type String
     */
    public void createTable(String tableName) throws ClassNotFoundException {
        try {
            // create a database connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            statement.executeUpdate("create table if not exists " + tableName + "(id integer primary key autoincrement, name string, password string)");

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * @brief delete table
     * @param tableName of type String
     */
    public void delTable(String tableName) throws ClassNotFoundException {
        try {
            // create a database connection
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            statement.executeUpdate("drop table if exists " + tableName);
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * @brief add data to the database method
     * @param user name of type String
     * @param password of type String
     */
    public void addDataToDB(String user, String password) throws Exception {
        try (Connection conn = DriverManager.getConnection(fileName)) {
            Class.forName("org.sqlite.JDBC");
            String sql = "INSERT INTO " + dataBaseTableName + " (username, password, created_ts, updated_ts) VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, user);
                statement.setString(2, hashPassword(password));
                statement.executeUpdate();
            }
        }
    }


    public void addDataTofileDB(String user, String fileName_, long fileSize, String ACL, String chunk1id, String chunk2id, String chunk3id, String chunk4id, String encryptionKey, long CRC32) throws InvalidKeySpecException, ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Use a PreparedStatement for better handling of parameters and to prevent SQL injection
            String sql = "INSERT INTO fileInfo (userName, fileName_, fileSize, ACL, chunk1id, chunk2id, chunk3id, chunk4id, encryptionKey, CRC32, dateOfCreation, dateOfLastModification) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_DATE, CURRENT_DATE)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, user);
                statement.setString(2, fileName_);
                statement.setInt(3, (int) fileSize);
                statement.setString(4, ACL);
                statement.setString(5, chunk1id);
                statement.setString(6, chunk2id);
                statement.setString(7, chunk3id);
                statement.setString(8, chunk4id);
                statement.setString(9, encryptionKey);
                statement.setInt(10,(int) CRC32);

                statement.executeUpdate();
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    public void updateDataTofileDB(String user, String fileName_, long fileSize, String ACL, String chunk1id, String chunk2id, String chunk3id, String chunk4id, int encryptionKey, long CRC32) throws InvalidKeySpecException, ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Use a PreparedStatement for better handling of parameters and to prevent SQL injection
            String sql = "UPDATE fileInfo SET fileSize = ?, ACL = ?, chunk1id = ?, chunk2id = ?, chunk3id = ?, chunk4id = ?, encryptionKey = ?, CRC32 = ?, dateOfLastModification = CURRENT_DATE " +
                    "WHERE userName = ? AND fileName_ = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, fileSize);
                statement.setString(2, ACL);
                statement.setString(3, chunk1id);
                statement.setString(4, chunk2id);
                statement.setString(5, chunk3id);
                statement.setString(6, chunk4id);
                statement.setInt(7, encryptionKey);
                statement.setInt(8,(int) CRC32);
                statement.setString(9, user);
                statement.setString(10, fileName_);

                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }




    public void addLog(String logMessage, String columnName) {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Use a PreparedStatement for better handling of parameters and to prevent SQL injection
            String sql = "INSERT INTO appLogs (" + columnName + ", date_and_time) VALUES (?, CURRENT_TIMESTAMP)";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, logMessage);

                statement.executeUpdate();
                Logger.getLogger(DB.class.getName()).log(Level.INFO, "Log added: {0}", logMessage);
            }

        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    /**
     * @brief get data from the Database method
     * @retunr results as ResultSet
     */
    public ObservableList<User> getDataFromTable() throws ClassNotFoundException {
        ObservableList<User> result = FXCollections.observableArrayList();
        try(Connection connection = getConnection()) {
            var statement = connection.createStatement();
            statement.setQueryTimeout(timeout);
            ResultSet rs = statement.executeQuery("select * from " + this.dataBaseTableName);
            while (rs.next()) {
                // read the result set
                result.add(new User(rs.getString("username"),rs.getString("password")));
            }

        } catch (Exception ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return result;
    }



    public ObservableList<Map<String, String>> getExistingFileNames() throws ClassNotFoundException {
        ObservableList<Map<String, String>> result = FXCollections.observableArrayList();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            String sql = "SELECT DISTINCT fileName_ FROM " + dataBaseTableName + " WHERE Status = 'Exists'";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        Map<String, String> fileInfoMap = new HashMap<>();
                        fileInfoMap.put("fileName_", resultSet.getString("fileName_"));
                        result.add(fileInfoMap);
                    }
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return result;
    }


    /**
     * @param x
     * @brief get data from the Database method
     * @return results as ObservableList<Map<String, String>>
     */

    public ObservableList<Map<String, String>> getDataFromTable2(String x) throws ClassNotFoundException {
        ObservableList<Map<String, String>> result = FXCollections.observableArrayList();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);


            // Use a PreparedStatement for better handling of parameters and to prevent SQL injection
            String sql = "SELECT * FROM " + this.dataBaseTableName + " WHERE userName = ? AND Status = 'Exists'";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, x);

                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    // read the result set and create a Map for each row
                    Map<String, String> fileInfoMap = new HashMap<>();
                    fileInfoMap.put("fileName_", rs.getString("fileName_"));
                    result.add(fileInfoMap);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
        return result;
    }

    /**
     * @param x
     * @brief get data from the Database method
     * @return results as ObservableList<Map<String, String>>
     */
    public ObservableList<Map<String, String>> getDataFromTable3(String x) throws ClassNotFoundException {
        ObservableList<Map<String, String>> result = FXCollections.observableArrayList();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Use a PreparedStatement for better handling of parameters and to prevent SQL injection
            String sql = "SELECT * FROM " + this.dataBaseTableName + " WHERE userName = ? AND Status = 'Deleted'";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, x);

                ResultSet rs = statement.executeQuery();

                while (rs.next()) {
                    // read the result set and create a Map for each row
                    Map<String, String> fileInfoMap = new HashMap<>();
                    fileInfoMap.put("fileName_", rs.getString("fileName_"));
                    result.add(fileInfoMap);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }
        return result;
    }






    public String[] getChunkIds(String fileName_, String userName) throws ClassNotFoundException {
        String[] chunkIds = {"Not Found", "Not Found", "Not Found", "Not Found"}; // Initialize to a default value indicating no matching chunkIds found

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Use a PreparedStatement to handle parameters and prevent SQL injection
            String sql = "SELECT chunk1id, chunk2id, chunk3id, chunk4id FROM fileInfo WHERE userName = ? AND fileName_ = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userName);
                statement.setString(2, fileName_);

                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    // Retrieve the chunkIds from the result set
                    chunkIds[0] = rs.getString("chunk1id");
                    chunkIds[1] = rs.getString("chunk2id");
                    chunkIds[2] = rs.getString("chunk3id");
                    chunkIds[3] = rs.getString("chunk4id");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // connection close failed.
                System.err.println(e.getMessage());
            }
        }

        return chunkIds;
    }

    public boolean doesFileNameExist(String fileName) throws ClassNotFoundException {
        List<Map<String, String>> result = new ArrayList<>();
        try {
            Class.forName("org.sqlite.JDBC");
            connection = getConnection();
            String sql = "SELECT COUNT(*) FROM fileInfo WHERE fileName_ = ? AND  Status != 'Deleted'";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                 statement.setString(1, fileName);
                ResultSet rs = statement.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return false;
    }

    public boolean doesItemExist(String columnName, String itemName, String username, String field) throws ClassNotFoundException {
        boolean exists = false;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Use a PreparedStatement for better handling of parameters and to prevent SQL injection
            String sql = "SELECT COUNT(*) FROM " + dataBaseTableName + " WHERE " + columnName + " = ? AND " + field + "= ?" ;

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, itemName);
                statement.setString(2, username);

                ResultSet rs = statement.executeQuery();

                // Check if any rows are returned (count > 0 means the item exists)
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return exists;
    }

    public boolean doesACLExist(String userName, String fileName_) throws ClassNotFoundException {
        boolean exists = false;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Use a PreparedStatement for better handling of parameters and to prevent SQL injection
            String sql = "SELECT COUNT(*) FROM " + dataBaseTableName + " WHERE ACL = 'R' AND userName = ? AND fileName_ = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userName);
                statement.setString(2, fileName_);

                ResultSet rs = statement.executeQuery();

                // Check if any rows are returned (count > 0 means the item exists)
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return exists;
    }

    public boolean doesACLExistRW(String userName, String fileName_) throws ClassNotFoundException {
        boolean exists = false;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Use a PreparedStatement for better handling of parameters and to prevent SQL injection
            String sql = "SELECT COUNT(*) FROM " + dataBaseTableName + " WHERE ACL = 'RW' AND userName = ? AND fileName_ = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userName);
                statement.setString(2, fileName_);

                ResultSet rs = statement.executeQuery();

                // Check if any rows are returned (count > 0 means the item exists)
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }

        return exists;
    }

    public long getCrc(String userName, String fileName_) throws ClassNotFoundException {
        long crcValue = 0;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Use a PreparedStatement for better handling of parameters and to prevent SQL injection
            String sql = "SELECT CRC32 FROM " + dataBaseTableName + " WHERE userName = ? AND fileName_ = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userName);
                statement.setString(2, fileName_);

                ResultSet rs = statement.executeQuery();

                // Check if any rows are returned
                if (rs.next()) {
                    crcValue = rs.getLong("crc32");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return crcValue;
    }

    public boolean checkStatus(String userName, String fileName_) throws ClassNotFoundException {
        boolean exists = false;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Use a PreparedStatement for better handling of parameters and to prevent SQL injection
            String sql = "SELECT COUNT(*) FROM " + dataBaseTableName + " WHERE Status = 'Deleted' AND userName = ? AND fileName_ = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userName);
                statement.setString(2, fileName_);

                ResultSet rs = statement.executeQuery();

                // Check if any rows are returned (count > 0 means the item exists)
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return exists;
    }

    public void deleteRecord(String columnName, String columnValue, String username) throws ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            String sql = "DELETE FROM " + dataBaseTableName + " WHERE " + columnName + " = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, columnValue);
                int affectedRows = statement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Record deleted successfully.");
                } else {
                    System.out.println("No matching record found.");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }


    public List<String> getFilesToDelete() {
        List<String> filesToDelete = new ArrayList<>();

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            String sql = "SELECT DISTINCT fileName_ FROM " + dataBaseTableName + " WHERE Status = 'Deleted' AND julianday('now') - julianday(dateOfLastModification) > 31";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String fileNames = resultSet.getString("fileName_");
                        filesToDelete.add(fileNames);
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
        }

        return filesToDelete;
    }



    public List<String> getFilesToDelete2(String username) {
        List<String> filesToDelete = new ArrayList<>();

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);
            String sql = "SELECT fileName_ FROM " + dataBaseTableName + " WHERE userName = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username); // Set the username parameter
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        String fileName = resultSet.getString("fileName_");
                        filesToDelete.add(fileName);
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            // Handle the exception appropriately
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
            }
        }

        return filesToDelete;
    }


    public boolean nameExists(String name) throws ClassNotFoundException {
        boolean exists = false;

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Use a PreparedStatement for better handling of parameters and to prevent SQL injection
            String sql = "SELECT COUNT(*) FROM " + dataBaseTableName + " WHERE username = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);

                ResultSet rs = statement.executeQuery();

                // Check if any rows are returned (count > 0 means the name exists)
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }

        return exists;
    }



    public void deleteUser(String userName) throws ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Use a PreparedStatement to handle parameters and prevent SQL injection
            String sql = "DELETE FROM " + dataBaseTableName + " WHERE username = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, userName);

                int affectedRows = statement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("User deleted successfully.");
                } else {
                    System.out.println("No matching user found.");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }




    public void updateField(String columnName, String newColumnValue, String username,String columnName2) throws ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            // Use a PreparedStatement to handle parameters and prevent SQL injection
            String sql = "UPDATE " + dataBaseTableName + " SET " + columnName + " = ? WHERE " + columnName2 + "= ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, newColumnValue);
                statement.setString(2, username);

                int affectedRows = statement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Field updated successfully.");
                } else {
                    System.out.println("No matching record found.");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void updateField2(String username, String value, String status) throws ClassNotFoundException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(fileName);

            String sql = "UPDATE " + dataBaseTableName + " SET Status = ? WHERE  fileName_ = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, status);
                statement.setString(2, value);

                int affectedRows = statement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Field updated successfully.");
                } else {
                    System.out.println("No matching record found.");
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * @brief decode password method
     * @param user name as type String
     * @param pass plain password of type String
     * @return true if the credentials are valid, otherwise false
     */
    public boolean validateUser(String username, String password) throws InvalidKeySpecException, ClassNotFoundException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if(rs.next()){
                String storedHash = rs.getString("password");
                if(storedHash.equals(hashPassword(password)))
                    return true;
            }
        } catch (Exception e) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, "Error :", e.getMessage());
            return false;
        }
        return false;
    }

    private String getSaltvalue(int length) {
        StringBuilder finalval = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            finalval.append(characters.charAt(random.nextInt(characters.length())));
        }

        return new String(finalval);
    }

    /* Method to generate the hash value */
    private byte[] hash(char[] password, byte[] salt) throws InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keylength);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    public String hashPassword(String password) throws InvalidKeySpecException {
        String finalval = null;

        byte[] securePassword = hash(password.toCharArray(), saltValue.getBytes());

        finalval = Base64.getEncoder().encodeToString(securePassword);

        return finalval;
    }

    /**
     * @brief get table name
     * @return table name as String
     */
    public String getTableName() {
        return this.dataBaseTableName;
    }

    /**
     * @brief print a message on screen method
     * @param message of type String
     */
    public void log(String message) {
        System.out.println(message);

    }

//    public static void main(String[] args) throws InvalidKeySpecException {
//        DB myObj = new DB();
//        myObj.log("-------- Simple Tutorial on how to make JDBC connection to SQLite DB ------------");
//        myObj.log("\n---------- Drop table ----------");
//        myObj.delTable(myObj.getTableName());
//        myObj.log("\n---------- Create table ----------");
//        myObj.createTable(myObj.getTableName());
//        myObj.log("\n---------- Adding Users ----------");
//        myObj.addDataToDB("ntu-user", "12z34");
//        myObj.addDataToDB("ntu-user2", "12yx4");
//        myObj.addDataToDB("ntu-user3", "a1234");
//        myObj.log("\n---------- get Data from the Table ----------");
//        myObj.getDataFromTable(myObj.getTableName());
//        myObj.log("\n---------- Validate users ----------");
//        String[] users = new String[]{"ntu-user", "ntu-user", "ntu-user1"};
//        String[] passwords = new String[]{"12z34", "1235", "1234"};
//        String[] messages = new String[]{"VALID user and password",
//            "VALID user and INVALID password", "INVALID user and VALID password"};
//
//        for (int i = 0; i < 3; i++) {
//            System.out.println("Testing " + messages[i]);
//            if (myObj.validateUser(users[i], passwords[i], myObj.getTableName())) {
//                myObj.log("++++++++++VALID credentials!++++++++++++");
//            } else {
//                myObj.log("----------INVALID credentials!----------");
//            }
//        }
//    }


    private static void createUsersTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "             username TEXT PRIMARY KEY," +
                "              password TEXT NOT NULL," +
                "              created_ts DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "              updated_ts DATETIME DEFAULT CURRENT_TIMESTAMP " +
                "              );";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("User table created");
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
                    CREATE TABLE IF NOT EXISTS "fileInfo" (
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

    private static void createAppLogs(Connection conn) throws SQLException {
        String sql = """
                    CREATE TABLE IF NOT EXISTS appLogs (
                          log TEXT,
                          date_and_time DATETIME DEFAULT CURRENT_TIMESTAMP
                      )
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
    /**
     * Checks if a user with the given username already exists in the users table.
     * @param username the username to check
     * @return true if the user exists, false otherwise
     */
    public boolean validateExistUser(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, "Error :", e.getMessage());
            return false;
        }
    }

    static {
        try (Connection conn = getConnection()) {
            if (conn != null) {

                createUsersTable(conn);
                createFileInfoTable(conn);
                createAppLogs(conn);
                createSessionTable(conn);

                System.out.println("Local SQLite tables created (if not exist).");
            }
        } catch (SQLException e) {
            System.err.println("SQLite Initialization Error: " + e.getMessage());
        }
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(fileName);
    }
}