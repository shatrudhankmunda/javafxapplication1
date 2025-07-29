/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.javafxapplication1;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.ZipException;

import static com.mycompany.javafxapplication1.LoginController.username_;
import static com.mycompany.javafxapplication1.ScpTo.numberOfChunks;

/**
 * FXML Controller class
 *
 * @author ntu-user
 */
public class FilemanagerController {

    private static final Logger logger = Logger.getLogger(FilemanagerController.class.getName());
    public static String selectedFilePath;
    public static long fileSize;
//    public static UUID[] chunkUUIDs = new UUID[numberOfChunks];
    public static String[] chunkUUIDs = new String[4];
    public static String[] Containers = new String[4];
    public static final String downloadsPath = "C:\\Users\\hp\\Downloads\\";
    public static final String pathToTemp = "E:/Container-based-file-storage-system-main/cwk/JavaFXApplication1/temp/";
    public static final String pathToCreated = "E:/Container-based-file-storage-system-main/cwk/JavaFXApplication1/temp/createdFiles/";

    static {
        Containers[0] = "chunk_container_1";
        Containers[1] = "chunk_container_2";
        Containers[2] = "chunk_container_3";
        Containers[3] = "chunk_container_4";
    }

    @FXML
    private Label localFileLabel;
    @FXML
    private TextField cloudinput;
    @FXML
    private TextField receiverinput;
    @FXML
    private TextField cloudinput2;
    @FXML
    private TextArea output;
    @FXML
    private Button createbtn;
    @FXML
    private Button refreshbtnred;
    @FXML
    private Button readbtn;
    @FXML
    private Button updatebtn;
    @FXML
    private Button selectBtn;
    @FXML
    private Text fileText;
    @FXML
    private Button uploadBtn;
    @FXML
    private Button filemanagerextended;
    @FXML
    private Button recover;
    @FXML
    private Button backButton;
    @FXML
    private Button backButton2;
    @FXML
    private Button selectCloudBtn;
    @FXML
    private TextField newfileInput;
    @FXML
    private Button selectCloudBtn2;
    @FXML
    private CheckBox myCheckBox;
    @FXML
    private TableView dataTableView;
    @FXML
    private TableView dataTableView2;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private ProgressIndicator progressIndicator2;
    private String checkBoxState = "RW";
    private String access;
    private boolean selectLocalFile = false;
    private boolean selectedCloudFile = false;

    public static void dialogue(String headerMsg, String contentMsg) {
        Stage secondaryStage = new Stage();
        Group root = new Group();
        Scene scene = new Scene(root, 300, 300, Color.DARKGRAY);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(headerMsg);
        alert.setContentText(contentMsg);
        Optional<ButtonType> result = alert.showAndWait();
    }

    public static void edialogue(String headerMsg, String contentMsg) {
        Stage secondaryStage = new Stage();
        Group root = new Group();
        Scene scene = new Scene(root, 300, 300, Color.DARKGRAY);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(headerMsg);
        alert.setContentText(contentMsg);

        Optional<ButtonType> result = alert.showAndWait();
    }

    public static void splitFileIntoChunks(String filePath, String outputDir) {
        Path input = Paths.get(filePath);
        try {
            long fileSize = Files.size(input);
            long baseChunkSize = fileSize / numberOfChunks;
            long remainder = fileSize % numberOfChunks;


            for (int i = 0; i < numberOfChunks; i++) {
                chunkUUIDs[i] = UUID.randomUUID().toString();
            }

            try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(input))) {
                for (int i = 0; i < numberOfChunks; i++) {
                    long thisChunkSize = baseChunkSize + (i == numberOfChunks - 1 ? remainder : 0);
                    String chunkName = String.format("chunk_%02d.bin", i + 1);
                    Path chunkPath = Paths.get(outputDir, chunkName);

                    try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(chunkPath))) {
                        long bytesLeft = thisChunkSize;
                        byte[] buffer = new byte[8 * 1024];
                        int bytesRead;
                        while (bytesLeft > 0 && (bytesRead = bis.read(buffer, 0, (int)Math.min(buffer.length, bytesLeft))) != -1) {
                            bos.write(buffer, 0, bytesRead);
                            bytesLeft -= bytesRead;
                        }
                    }

                    String uuidStr = chunkUUIDs[i];
                    try {
                        ScpTo.dockerConnect(
                                chunkPath.toString(),
                                "Vchunk" + uuidStr + ".bin",
                                "localhost",
                                2221+i,// Assuming localhost for simplicity; replace with actual container name if needed
                                "create"
                        );
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Failed to upload chunk: " + chunkName, e.getMessage());
                        edialogue("Upload Error", "Failed to upload chunk: " + chunkName + " — " + e.getMessage());
                        return;
                    }

                    try {
                        Files.deleteIfExists(chunkPath);
                    } catch (IOException e) {
                        System.err.println("Could not delete chunk file: " + chunkPath + " — " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void joinFiles(String directoryPath, String originalFileName, String outputPath) {
        if (chunkUUIDs == null || Containers == null || chunkUUIDs.length < numberOfChunks || Containers.length < numberOfChunks) {
            edialogue("Join error", "Invalid chunk metadata (UUIDs or Containers).");
            return;
        }

        File outputFile = Paths.get(outputPath, originalFileName).toFile();

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            for (int i = 0; i < numberOfChunks; i++) {
                String virtualFileName = "Vchunk" + chunkUUIDs[i] + ".bin";
                String localChunkName = "chunk" + (i + 1) + ".bin";
                String localChunkPath = Paths.get(directoryPath, localChunkName).toString();

                // Attempt to download the chunk from Docker
                try {
                    ScpTo.dockerConnect(localChunkName, virtualFileName, "localhost", 2221+i,  "get");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to download chunk: " + localChunkName, e.getMessage());
                    edialogue("Join error", "Failed to download chunk: " + localChunkName + " — " + e.getMessage());
                    return;
                }

                // Ensure chunk file was downloaded
                File chunkFile = new File(localChunkPath);
                if (!chunkFile.exists()) {
                    Logger.getLogger(FilemanagerController.class.getName()).log(Level.SEVERE, "Missing chunk file: " + localChunkPath);
                    edialogue("Join error", "Missing chunk file: " + localChunkName);
                    return;
                }

                // Append chunk content to the output file
                try (FileInputStream fis = new FileInputStream(chunkFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }

                // Delete temp chunk after use
                deleteFile(localChunkPath);
            }
            Logger.getLogger(FilemanagerController.class.getName()).log(Level.INFO, "Files joined successfully into: {0}", outputFile.getAbsolutePath());
            edialogue("Join complete", "File successfully reassembled: " + outputFile.getName());

        } catch (IOException e) {
            Logger.getLogger(FilemanagerController.class.getName()).log(Level.SEVERE, "Error joining files", e);
            edialogue("Join error", "An error occurred while joining files. Please try again.");
        }
    }


    public static void readFileAndOutputToTextArea(File file, TextArea outputTextArea) {
        if (file == null || outputTextArea == null) {
            System.err.println("File or TextArea is null");
            return;
        }
        outputTextArea.clear();
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }

        outputTextArea.setText(content.toString());
    }

    public static void writeToFile(String filePath, String content) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        }
    }

    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            // Finally, delete the directory itself
            directory.delete();
        } else {
            System.err.println("Directory does not exist: " + directory.getAbsolutePath());
        }
    }

    private static void createDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
            } else {
                System.err.println("Failed to create directory.");
            }
        }
    }

    public static String getFileName(String filePath) {
        File file = new File(filePath);
        return file.getName();
    }

    public static boolean doesFileExist(String fileName) {
        // Construct the full path to the file

        String filePath = pathToCreated + File.separator + fileName;

        // Create a File object for the specified file path
        File file = new File(filePath);

        // Check if the file exists
        return file.exists();
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if(file.exists()){
          boolean deleted =  file.delete();
          if(deleted){
              Logger.getLogger(FilemanagerController.class.getName()).log(Level.INFO, "File deleted successfully: {0}", file.getName());
          }else{
              Logger.getLogger(FilemanagerController.class.getName()).log(Level.SEVERE, "Failed to delete file: {0}", file.getName());
          }
        }
    }

    public static long calculateCRC32(String filePath) throws IOException {
        // Create a CRC32 object
        CRC32 crc32 = new CRC32();

        // Open the file input stream
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[8192]; // Buffer size
            int bytesRead;
            // Read bytes from the file and update the CRC32 checksum
            while ((bytesRead = fis.read(buffer)) != -1) {
                crc32.update(buffer, 0, bytesRead);
            }
        }

        // Get the CRC32 checksum value
        return crc32.getValue();
    }

    @FXML
    private void handleRowClicked(MouseEvent event) {
        Map<String, String> selectedItem = (Map<String, String>) dataTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            cloudinput.setText(selectedItem.get("fileName_"));
        }
    }
    @FXML
    private void handleTableData(MouseEvent event) {
        Map<String, String> selectedItem = (Map<String, String>) dataTableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            cloudinput2.setText(selectedItem.get("fileName_"));
        }
    }

    @FXML
    private void switchToFilemanagerExtended()  {

        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) filemanagerextended.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("filemanagerextended.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("Share and Download");
            FilemanagerController controller = loader.getController();
            controller.initialise2();
            secondaryStage.show();
            primaryStage.close();
        } catch (Exception e) {
           Logger.getLogger(FilemanagerController.class.getName()).severe("Error: "+ e.getMessage());
            edialogue("Error", "Failed to switch to File Manager Extended view. Please try again.");
        }
    }

    @FXML
    private void Backbuttonahndler(ActionEvent event) {
        String getuser_ = username_.getUser();
        String getpass = username_.getPass();
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) backButton.getScene().getWindow();
        try {

            String[] credentials = new String[2];
            credentials[0] = getuser_;
            credentials[1] = getpass;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("secondary.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            SecondaryController controller = loader.getController();
            controller.initialiseSession();
            secondaryStage.setTitle("Dashboard");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
           Logger.getLogger(FilemanagerController.class.getName()).severe("Error: "+ e.getMessage());
        }
    }

    @FXML
    private void Backbuttonahndler2(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) backButton2.getScene().getWindow();
        try {


            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("filemanager.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("File Manager");
            FilemanagerController controller = loader.getController();
            controller.initialise2();
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void selectBtnHandler(ActionEvent event) throws IOException {
        Stage primaryStage = (Stage) selectBtn.getScene().getWindow();
        primaryStage.setTitle("Select a File");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            selectLocalFile = true;
            selectedFilePath = selectedFile.getCanonicalPath();
            logger.log(Level.INFO, "Selected file path: {0}", selectedFilePath);
            dialogue("", "FILE SELECTED");
            selectedCloudFile = false;
            localFileLabel.setText(selectedFile.getName());
            cloudinput.setText("");
            newfileInput.setText("");
            output.setText("");
            //TO DO
        }
    }

    @FXML
    private void selectCloudBtnHandler(ActionEvent event) throws IOException, ClassNotFoundException {
        if( cloudinput2.getText().isEmpty()) {
            edialogue("CANNOT SELECT", "ENTER FILE NAME or SELECT A FILE NAME !");
            return;
        }else{
            selectedFilePath = cloudinput2.getText();
            receiverinput.setText("");
            logger.log(Level.INFO, "Selected cloud file: {0}", selectedFilePath);
            dialogue("", "FILE SELECTED");
            return;
        }
//        DB myObj = new DB("fileInfo");
//        if (myObj.doesItemExist("fileName_", cloudinput2.getText(), LoginController.username_.getUser(), "userName")) {
//            selectedFilePath = cloudinput2.getText();
//            receiverinput.setText("");
//            dialogue("", "FILE SELECTED");
//        }
    }

    @FXML
    private void selectCloudBtnHandlerlocal(ActionEvent event) throws IOException, ClassNotFoundException {
        if(cloudinput.getText().isEmpty()){
            edialogue("Warning !!", "Please select file or enter file name!!");
            return;
        }
        DB myObj = new DB("fileInfo");
        dialogue("", "FILE SELECTED");
        logger.log(Level.INFO, "Selected cloud file: {0}", cloudinput.getText());
        selectedCloudFile = true;
        if (myObj.doesItemExist("fileName_", cloudinput.getText(), LoginController.username_.getUser(), "userName")) {
            selectedFilePath = cloudinput.getText();
            String[] chunkIds = myObj.getChunkIds(getFileName(selectedFilePath), LoginController.username_.getUser());
            System.arraycopy(chunkIds, 0, chunkUUIDs, 0, numberOfChunks);
            joinFiles(pathToTemp, getFileName(selectedFilePath), pathToCreated);
        } else {
            edialogue("CANNOT SELECT", "FILE DOES NOT EXISTS");
        }
    }

    @FXML
    private void handleCheckBoxAction(ActionEvent event) {
        if (myCheckBox.isSelected()) {
            checkBoxState = "R";
        } else {
            checkBoxState = "RW";
        }
    }

    @FXML
    private void uncheckCheckBox() {
        myCheckBox.setSelected(false);
    }

    @FXML
    private void ShareFileHandler(ActionEvent event) throws IOException, ClassNotFoundException, InvalidKeySpecException {
        DB myObj = new DB("fileInfo");
        DB user = new DB("Users");
        String receiver = receiverinput.getText();
        if (!(user.doesItemExist("username", receiver, receiver, "username"))) {
            logger.log(Level.WARNING, "User does not exist: {0}", receiver);
            edialogue("CANNOT SHARE", "USER DOES NOT EXISTS");
            return;
        }

        if (selectedFilePath != null & (myObj.doesItemExist("fileName_", selectedFilePath, LoginController.username_.getUser(), "userName"))) {
            String[] chunkIds = myObj.getChunkIds(getFileName(selectedFilePath), LoginController.username_.getUser());
            Logger.getLogger(FilemanagerController.class.getName()).log(Level.INFO, "Chunk IDs: {0}", Arrays.toString(chunkIds));
            System.arraycopy(chunkIds, 0, chunkUUIDs, 0, numberOfChunks);
            joinFiles(pathToTemp, getFileName(selectedFilePath), pathToCreated);
            helper(receiver);
            selectedFilePath = null;
        } else {
            FilemanagerController.edialogue("CANNOT Share", "NO FILE SELECTED");
        }
    }

    private void helper(String person) throws InvalidKeySpecException, ClassNotFoundException, IOException {
        DB myObj = new DB("fileInfo");
        DB Log = new DB("appLogs");
        String getuser_ = username_.getUser();
        String originalFileName = getFileName(selectedFilePath);
        String newFileName = originalFileName;

        // Check if the file already exists in the receiver's database
        if (myObj.doesItemExist("fileName_", originalFileName, person, "userName")) {
            // Get the file extension (if any)
            String extension = "";
            int extensionIndex = originalFileName.lastIndexOf('.');
            if (extensionIndex != -1) {
                extension = originalFileName.substring(extensionIndex);
                originalFileName = originalFileName.substring(0, extensionIndex);
            }

            int counter = 1;
            // Keep incrementing the counter until a unique file name is found
            while (myObj.doesItemExist("fileName_", newFileName, person, "userName")) {
                newFileName = originalFileName + "(" + counter + ")" + extension;
                counter++;
            }
        }
        splitFileIntoChunks(pathToCreated + File.separator + getFileName(selectedFilePath), pathToTemp);
        if (myObj.doesACLExist(LoginController.username_.getUser(), getFileName(selectedFilePath))) {
            access = "R";
        } else {
            access = getCheckBoxState();
        }
        myObj.addDataTofileDB(person, newFileName, fileSize, access, chunkUUIDs[0], chunkUUIDs[1], chunkUUIDs[2], chunkUUIDs[3], "password", calculateCRC32(pathToCreated + File.separator + selectedFilePath));
        Log.addLog("User " + username_.getUser() + " Shared File: " + newFileName + " to " + person, "log");
        initialise2();
        logger.log(Level.INFO, "File shared successfully: {0} to {1}", new Object[]{newFileName, person});
        dialogue("", "File shared to user: " + person + "successfully!");
        //        if ((myObj.getCrc(getuser_, getFileName(selectedFilePath))) == calculateCRC32(pathToCreated + File.separator + selectedFilePath)) {
//
//        } else {
////            edialogue("CRC ERROR", "CRC32 check failed");
//        }


        if (doesFileExist(getFileName(selectedFilePath))) {
            deleteFile(pathToCreated + File.separator + selectedFilePath);
            selectedFilePath = null;
            receiverinput.setText("");
            cloudinput2.setText("");
            uncheckCheckBox();
        }
    }

    @FXML
    private void downloadButtonHandler(ActionEvent event) throws ClassNotFoundException, IOException {
        DB myObj = new DB("fileInfo");
        String getuser_ = username_.getUser();
        if (selectedFilePath == null) {
            edialogue("", "No file selected");
            return;
        }
        if (selectedFilePath != null & (myObj.doesItemExist("fileName_", selectedFilePath, LoginController.username_.getUser(), "userName"))
                & !(myObj.checkStatus(username_.getUser(), getFileName(selectedFilePath)))) {
            DB Log = new DB("appLogs");
            String[] chunkIds = myObj.getChunkIds(getFileName(selectedFilePath), LoginController.username_.getUser());
            logger.log(Level.INFO, "Chunk IDs: {0}", Arrays.toString(chunkIds));
            System.arraycopy(chunkIds, 0, chunkUUIDs, 0, numberOfChunks);
            joinFiles(pathToTemp, getFileName(selectedFilePath), downloadsPath);
            if ((myObj.getCrc(getuser_, getFileName(selectedFilePath))) != calculateCRC32(downloadsPath + getFileName(selectedFilePath))) {
//                edialogue("CRC ERROR", "CRC32 check failed");
            }
            Log.addLog("User " + LoginController.username_.getUser() + " Downloaded File: " + getFileName(selectedFilePath), "log");
            logger.log(Level.INFO, "File downloaded successfully: {0}", getFileName(selectedFilePath));
            if (myObj.doesACLExist(LoginController.username_.getUser(), getFileName(selectedFilePath))) {
                makeFileReadOnly("downloadsPath" + getFileName(selectedFilePath));
                dialogue("File downloaded", "Check download directory");
            }

            selectedFilePath = null;
            dialogue("File downloaded", "Check download directory");

        } else {
            cloudinput2.setText("");
            FilemanagerController.edialogue("CANNOT DOWNLOAD", "NO FILE SELECTED");
        }
    }

    @FXML
    private void DeleteBtnHandler(ActionEvent event) throws IOException, ClassNotFoundException {
        if (selectedFilePath != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Confirmation");
            confirmAlert.setHeaderText("Are you sure you want to delete this file?");
            confirmAlert.setContentText("File: " + getFileName(selectedFilePath));
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                DB myObj = new DB("fileInfo");
                DB Log = new DB("appLogs");
                if(myObj.doesACLExistRW(LoginController.username_.getUser(), getFileName(selectedFilePath))){
                    myObj.updateField2(username_.getUser(), getFileName(selectedFilePath), "Deleted");
                    Log.addLog("User " + username_.getUser() + " Deleted File: " + getFileName(selectedFilePath), "log");
                    logger.log(Level.INFO, "File deleted successfully: {0}", getFileName(selectedFilePath));
                    initialise2();
                    initialise3();
                    cloudinput2.setText("");
                    selectedFilePath = null;
                    new Alert(Alert.AlertType.INFORMATION) {{
                        setTitle("Delete Status");
                        setHeaderText("File Deleted");
                        setContentText("File deleted successfully!");
                        showAndWait();
                    }};
                }else {
                    edialogue("CANNOT DELETE","You Don't have permission to delete this file !!");
                }

            }
        } else {
            edialogue("CANNOT DELETE", "NO FILE SELECTED");
        }
    }

    @FXML
    private void recoverBtnHandler(ActionEvent event) throws IOException, ClassNotFoundException {
        if (selectedFilePath != null) {
            DB myObj = new DB("fileInfo");
            DB Log = new DB("appLogs");
            myObj.updateField2(username_.getUser(), getFileName(selectedFilePath), "Exists");
            Log.addLog("User " + username_.getUser() + " Deleted File: " + getFileName(selectedFilePath), "log");
            initialise2();
            initialise3();
            cloudinput2.setText("");
            selectedFilePath = null;
        } else {
            edialogue("CANNOT RECOVER", "NO FILE SELECTED");
        }
    }

    @FXML
    private void refreashBtnHandler(ActionEvent event) throws IOException, ClassNotFoundException {
        DB fileDatabaseHandler = new DB("fileInfo");
        DB Log = new DB("appLogs");
        List<String> filesToDelete = fileDatabaseHandler.getFilesToDelete();
        for (String fileName : filesToDelete) {
            Delete(fileName);
            Log.addLog("File: " + fileName + " Was deleted from " + username_.getUser() + " Recently deleted folder", "log");
        }
        initialise3();


    }

    private void Delete(String name) throws IOException, ClassNotFoundException {
        DB myObj = new DB("fileInfo");
        String[] chunkIds = myObj.getChunkIds(name, LoginController.username_.getUser());
        for (int i = 0; i < numberOfChunks; i++) {
            try {
                ScpTo.dockerConnect("", "Vchunk" + chunkIds[i] + ".bin", "localhost", 2221+i, "delete");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to delete chunk: " + chunkIds[i], e.getMessage());
                edialogue("Delete Error", "Failed to delete chunk: " + chunkIds[i] + " — " + e.getMessage());
                return;
            }
        }
        myObj.deleteRecord("fileName_", name, username_.getUser());
    }

    @FXML
    private void readFileButtonHandler() throws ClassNotFoundException {
        DB myobj = new DB("fileInfo");
        if (selectedFilePath == null) {
            edialogue("", "No file selected");
            return;
        }
        System.out.println(selectLocalFile);
        if (selectLocalFile) {
            File file = new File(selectedFilePath);
            if (selectedFilePath != null && !selectedFilePath.isEmpty()) {
                if (file.exists()) {
                    readFileAndOutputToTextArea(file, output);
                   // selectedFilePath = getFileName(selectedFilePath);
                } else {
                    System.err.println("File does not exist: " + selectedFilePath);
                }
            } else {
                System.err.println("Selected file path is null or empty.");
            }
        } else {
            if ((myobj.doesItemExist("fileName_", getFileName(selectedFilePath), LoginController.username_.getUser(), "userName"))) {
                selectedFilePath = pathToCreated + File.separator + selectedFilePath;
            }
            File file = new File(selectedFilePath);
            if (selectedFilePath != null && !selectedFilePath.isEmpty()) {
                if (file.exists()) {
                    readFileAndOutputToTextArea(file, output);
                    selectedFilePath = getFileName(selectedFilePath);
                } else {
                    System.err.println("File does not exist: " + selectedFilePath);
                }
            } else {
                System.err.println("Selected file path is null or empty.");
            }
        }
    }

    @FXML
    private void UpdateButtonHandler() throws ClassNotFoundException, InvalidKeySpecException, IOException {
        DB myobj = new DB("fileInfo");
        DB Log = new DB("appLogs");
        if (selectedFilePath == null) {
            edialogue("", "No file selected");
            return;
        }
        if (myobj.doesACLExist(LoginController.username_.getUser(), getFileName(selectedFilePath))) {
            edialogue("Permission error", "You have read only permissions");
            return;
        }

//        if (!(myobj.doesItemExist("fileName_", getFileName(selectedFilePath), LoginController.username_.getUser(), "userName"))) {
//            writeToFile(selectedFilePath, output.getText());
//            dialogue("", "Upload to save changes");
//            return;
//        }
         if(!myobj.doesFileNameExist(getFileName(selectedFilePath))){
             if(selectLocalFile){
                 try {
                     String path = pathToCreated;
                     writeToFile(path+getFileName(selectedFilePath), output.getText());
                 } catch (Exception e) {
                        logger.log(Level.SEVERE, "Failed to write to file: " + pathToCreated, e.getMessage());
                        edialogue("Write Error", "Failed to write to file: " + pathToCreated + " — " + e.getMessage());
                        return;
                 }
             }
             try {
                 writeToFile(selectedFilePath, output.getText());
             } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to write to file: " + selectedFilePath, e.getMessage());
                    edialogue("Write Error", "Failed to write to file: " + selectedFilePath + " — " + e.getMessage());
                    return;
             }
             logger.log(Level.INFO, "File {0} does not exist in the database, writing to local file.", getFileName(selectedFilePath));
              dialogue("", "Upload to save changes");
              return;
         }
        if (myobj.doesItemExist("fileName_", getFileName(selectedFilePath), LoginController.username_.getUser(), "userName")) {
            try {
                writeToFile(pathToCreated + File.separator + getFileName(selectedFilePath), output.getText());
            } catch (Exception e) {
               logger.log(Level.SEVERE, "Failed to write to file: " + pathToCreated + File.separator + getFileName(selectedFilePath), e.getMessage());
                edialogue("Write Error", "Failed to write to file: " + pathToCreated + File.separator + getFileName(selectedFilePath) + " — " + e.getMessage());
                return;
            }
            String[] chunkIds = myobj.getChunkIds(getFileName(selectedFilePath), LoginController.username_.getUser());
            for (int i = 0; i < numberOfChunks; i++) {
                try {
                    ScpTo.dockerConnect("", "Vchunk" + chunkIds[i] + ".bin", "localhost", 2221+i, "delete");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to delete chunk: " + chunkIds[i], e.getMessage());
                    edialogue("Update Error", "Failed to delete chunk: " + chunkIds[i] + " — " + e.getMessage());
                    return;
                }
            }
            if(selectedCloudFile){
                splitFileIntoChunks(pathToCreated + File.separator + getFileName(selectedFilePath), pathToTemp);
                myobj.updateDataTofileDB(LoginController.username_.getUser(), getFileName(selectedFilePath), fileSize, getFilePermissions(selectedFilePath), chunkUUIDs[0], chunkUUIDs[1], chunkUUIDs[2], chunkUUIDs[3], 123, calculateCRC32(pathToCreated+selectedFilePath));
                initialise2();
                Log.addLog("User " + LoginController.username_.getUser() + " Updated file: " + getFileName(selectedFilePath), "log");
                dialogue("", "File updated successfully on cloud !!");
                if (doesFileExist(getFileName(pathToCreated + File.separator + getFileName(selectedFilePath)))) {
                    deleteFile(pathToCreated + File.separator + getFileName(selectedFilePath));
                }
            }else{
                splitFileIntoChunks(pathToCreated + File.separator + getFileName(selectedFilePath), pathToTemp);
                myobj.updateDataTofileDB(LoginController.username_.getUser(), getFileName(selectedFilePath), fileSize, getFilePermissions(selectedFilePath), chunkUUIDs[0], chunkUUIDs[1], chunkUUIDs[2], chunkUUIDs[3], 123, calculateCRC32(selectedFilePath));
                initialise2();
                Log.addLog("User " + LoginController.username_.getUser() + " Updated file: " + getFileName(selectedFilePath), "log");
                logger.log(Level.INFO, "File {0} updated successfully on cloud.", getFileName(selectedFilePath));
                dialogue("", "File updated successfully on cloud !!");
                try {
                    writeToFile(selectedFilePath, output.getText());
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to write to file: " + selectedFilePath + File.separator + getFileName(selectedFilePath), e.getMessage());
                    edialogue("Write Error", "Failed to write to file: " + selectedFilePath + File.separator + getFileName(selectedFilePath) + " — " + e.getMessage());
                    return;
                }
                if (doesFileExist(getFileName(pathToCreated + File.separator + getFileName(selectedFilePath)))) {
                    deleteFile(pathToCreated + File.separator + getFileName(selectedFilePath));
                }
            }
        }


    }

    @FXML
    private void createButtonHandler(ActionEvent event) throws ClassNotFoundException {
        Handler hand = new Handler();

        String userCommand = newfileInput.getText();
        if (userCommand == null || userCommand.trim().isEmpty()) {
            edialogue("CANNOT CREATE", "ENTER FILE NAME");
            return;
        }
        DB myObj = new DB("fileInfo");
        DB Log = new DB("appLogs");


        if (myObj.doesItemExist("fileName_", userCommand, LoginController.username_.getUser(), "userName")) {
            // File with the same name already exists
            edialogue("CANNOT CREATE", "FILE ALREADY EXISTS ON THE CLOUD");
        } else {
            // File does not exist, proceed with creating
            cloudinput.setText("");
            output.setText("");
            localFileLabel.setText("No local file seleted");
            selectLocalFile = false;
            hand.fileCreating(userCommand);
            selectedFilePath = pathToCreated + userCommand;
            if (!"".equals(userCommand)) {
                logger.log(Level.INFO, "Creating file: {0}", selectedFilePath);
                dialogue("CREATING FILE", "Successful!, You can now write and update your file");
                Log.addLog("User " + LoginController.username_.getUser() + " Created File: " + getFileName(selectedFilePath), "log");
            }
        }
    }

    @FXML
    private void uploadButtonHandler(ActionEvent event) {
        try {
            DB myObj = new DB("fileInfo");
            DB Log = new DB("appLogs");
            if (selectedFilePath == null) {
                edialogue("", "No file selected");
                return;
            }
        if (myObj.doesItemExist("fileName_", getFileName(selectedFilePath), LoginController.username_.getUser(), "userName")) {
            // File with the same name already exists
            edialogue("CANNOT UPLOAD", "TRY UPDATING INSTEAD");
            return;
        }

            if (selectedFilePath != null & !(myObj.doesItemExist("fileName_", selectedFilePath, LoginController.username_.getUser(), "userName"))) {
                splitFileIntoChunks(pathToCreated + File.separator + getFileName(selectedFilePath), pathToTemp);
                myObj.addDataTofileDB(LoginController.username_.getUser(), getFileName(selectedFilePath), fileSize, getFilePermissions(selectedFilePath), chunkUUIDs[0], chunkUUIDs[1], chunkUUIDs[2], chunkUUIDs[3], "password", calculateCRC32(selectedFilePath));
                initialise2();
                Log.addLog("User " + LoginController.username_.getUser() + " Uploaded File: " + getFileName(selectedFilePath), "log");
                logger.log(Level.INFO, "File uploaded successfully: {0}", getFileName(selectedFilePath));
                if (doesFileExist(getFileName(pathToCreated + File.separator + getFileName(selectedFilePath)))) {
                    deleteFile(pathToCreated + File.separator + getFileName(selectedFilePath));
                    selectedFilePath = null;
                    //fileNameinput.setText("")
                    cloudinput.setText("");
                    output.setText("");
                    localFileLabel.setText("No local file selected");
                }
            } else {
                logger.log(Level.WARNING, "File already exists in the database: {0}", getFileName(selectedFilePath));
                edialogue("CANNOT UPLOAD", "NO FILE SELECTED");

            }
            new Alert(Alert.AlertType.INFORMATION) {{
                setTitle("Upload Status");
                setHeaderText("File Upload");
                setContentText("File uploaded successfully!");
                showAndWait();
            }};
        } catch (IOException e) {
            Logger.getLogger(FilemanagerController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            edialogue("Upload Error", "An error occurred while uploading the file.");
        } catch (ClassNotFoundException e) {
            Logger.getLogger(FilemanagerController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            edialogue("Upload Error", "An error occurred while processing the file.");
        } catch (InvalidKeySpecException e) {
            Logger.getLogger(FilemanagerController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            edialogue("Upload Error", "An error occurred with the encryption key.");
        }
    }

    private void makeFileReadOnly(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.setReadOnly();
        } else {
            System.out.println("File does not exist: " + filePath);
        }
    }

    private String getFilePermissions(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.canWrite()) {
                return "RW";
            } else {
                return "R";
            }
        } else {
            return "RW";
        }
    }

    public void initialise2() {
        DB myObj = new DB("fileInfo");
        try {
            ObservableList<Map<String, String>> data =myObj.getExistingFileNames(); //myObj.getDataFromTable2(LoginController.username_.getUser());

            // Create a TableColumn with the appropriate property name
            TableColumn<Map<String, String>, String> fileNameColumn = new TableColumn<>("Files");
            fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get("fileName_")));

            // Set the items and columns to the TableView
            dataTableView.getItems().clear(); // Clear existing data
            dataTableView.getColumns().clear(); // Clear existing columns
            dataTableView.setItems(data);
            dataTableView.getColumns().addAll(fileNameColumn);


            addContextMenu();

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FilemanagerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initialise3() {
        DB myObj = new DB("fileInfo");
        try {
            ObservableList<Map<String, String>> data = myObj.getDataFromTable3(LoginController.username_.getUser());

            // Create a TableColumn with the appropriate property name
            TableColumn<Map<String, String>, String> fileNameColumn = new TableColumn<>("Recently deleted");
            fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get("fileName_")));

            // Set the items and columns to the TableView
            dataTableView2.getItems().clear(); // Clear existing data
            dataTableView2.getColumns().clear(); // Clear existing columns
            dataTableView2.setItems(data);
            dataTableView2.getColumns().addAll(fileNameColumn);


            addContextMenu2();

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(FilemanagerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        // Create a menu item to copy the file name
        MenuItem copyFileNameMenuItem = new MenuItem("Copy File Name");
        copyFileNameMenuItem.setOnAction(event -> {
            Map<String, String> selectedRow = (Map<String, String>) dataTableView.getSelectionModel().getSelectedItem();
            if (selectedRow != null) {
                String fileName = selectedRow.get("fileName_");

                // Use Clipboard to copy the file name to the system clipboard
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(fileName);
                clipboard.setContent(content);
            }
        });

        // Add the menu item to the context menu
        contextMenu.getItems().add(copyFileNameMenuItem);

        // Set the context menu for each row in the TableView
        dataTableView.setContextMenu(contextMenu);
    }

    public void addContextMenu2() {
        ContextMenu contextMenu = new ContextMenu();

        // Create a menu item to copy the file name
        MenuItem copyFileNameMenuItem = new MenuItem("Copy File Name");
        copyFileNameMenuItem.setOnAction(event -> {
            Map<String, String> selectedRow = (Map<String, String>) dataTableView2.getSelectionModel().getSelectedItem();
            if (selectedRow != null) {
                String fileName = selectedRow.get("fileName_");

                // Use Clipboard to copy the file name to the system clipboard
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(fileName);
                clipboard.setContent(content);
            }
        });

        // Add the menu item to the context menu
        contextMenu.getItems().add(copyFileNameMenuItem);

        // Set the context menu for each row in the TableView
        dataTableView2.setContextMenu(contextMenu);
    }

    public String getCheckBoxState() {
        return checkBoxState;
    }

}