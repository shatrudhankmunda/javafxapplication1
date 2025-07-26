/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.javafxapplication1;

import static com.mycompany.javafxapplication1.LoginController.username_;
import static com.mycompany.javafxapplication1.ScpTo.Numberofchunks;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.CRC32;
import java.util.zip.ZipException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;


/**
 * FXML Controller class
 *
 * @author ntu-user
 */
public class FilemanagerController  {
    
    @FXML 
    private TextField fileNameinput;
    
    @FXML 
    private TextField cloudinput;
    
    
    @FXML 
    private TextField receiverinput;
    
    
    @FXML 
    private TextField cloudinput2;

    @FXML
    private  TextArea output;

    @FXML
    private Button createbtn;
    
    @FXML
    private Button refreshbtnred;

    @FXML
    private Button readbtn;

    @FXML
    private  Button updatebtn;

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
    private Button selectCloudBtn2;
    
    @FXML
    private CheckBox myCheckBox;
   
    @FXML
    private TableView dataTableView;
    
     @FXML
    private TableView dataTableView2;
    
    @FXML
    private ProgressIndicator  progressIndicator;
    
    @FXML
    private ProgressIndicator  progressIndicator2;
    
    public static String selectedFilePath;
       
    private String checkBoxState = "RW";
    
    private String access;
    
    public static long fileSize;

    public static String[] chunkUUIDs = new String[4];

    public static String[] Containers = new String[4];

    static {
    Containers[0] = "comp20081-files-container1";
    Containers[1] = "comp20081-files-container2";
    Containers[2] = "comp20081-files-container3";
    Containers[3] = "comp20081-files-container4";
    }
    
    public static String pathToTemp = "/home/ntu-user/App/COMP20081-CourseWork/cwk/JavaFXApplication1/temp/" ;
    public static String pathToCreated = "/home/ntu-user/App/COMP20081-CourseWork/cwk/JavaFXApplication1/temp/createdFiles/" ;

   
    
    @FXML
    private void switchToFilemanagerExtended() throws ZipException{
        
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
            e.printStackTrace();
        }
    }
    
    @FXML
    private void Backbuttonahndler(ActionEvent event){
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
            controller.initialise();
            secondaryStage.setTitle("Login");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    @FXML
    private void Backbuttonahndler2(ActionEvent event){
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) backButton2.getScene().getWindow();
        try {
            
        
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("filemanager.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("Login");
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

            if(selectedFile != null){
                selectedFilePath = selectedFile.getCanonicalPath();
                dialogue("", "FILE SELECTED");
                fileNameinput.setText("");
                cloudinput.setText("");
            }
        }
        
    
    @FXML
    private void selectCloudBtnHandler(ActionEvent event) throws IOException, ClassNotFoundException {
        DB myObj = new DB("fileInfo");
        if (myObj.doesItemExist("fileName_", cloudinput2.getText(),  LoginController.username_.getUser(), "userName")){
             selectedFilePath = cloudinput2.getText();
             receiverinput.setText("");
             dialogue("", "FILE SELECTED");
        }
        else{
            edialogue("CANNOT SELECT", "FILE DOES NOT EXISTS");
        }
    }
    
      @FXML
    private void selectCloudBtnHandlerlocal(ActionEvent event) throws IOException, ClassNotFoundException {
        DB myObj = new DB("fileInfo");
        if(myObj.doesItemExist("fileName_", cloudinput.getText(),  LoginController.username_.getUser(), "userName")){
             selectedFilePath = cloudinput.getText();
             String[] chunkIds = myObj.getChunkIds(getFileName(selectedFilePath), LoginController.username_.getUser());
            System.arraycopy(chunkIds, 0, chunkUUIDs, 0, Numberofchunks);
             joinFiles(pathToTemp,getFileName(selectedFilePath),pathToCreated);
             dialogue("", "FILE SELECTED");
        }
        else{
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
        if(!(user.doesItemExist("username", receiver, receiver, "username"))){
            edialogue("CANNOT SHARE", "USER DOES NOT EXISTS");
            return;
        }
        
        if (selectedFilePath != null & (myObj.doesItemExist("fileName_", selectedFilePath, LoginController.username_.getUser(), "userName"))) {
            String[] chunkIds = myObj.getChunkIds(getFileName(selectedFilePath), LoginController.username_.getUser());
            System.arraycopy(chunkIds, 0, chunkUUIDs, 0, Numberofchunks);
            joinFiles(pathToTemp,getFileName(selectedFilePath),pathToCreated);
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
    splitFileIntoChunks(pathToCreated + selectedFilePath, pathToTemp);
     if(myObj.doesACLExist(LoginController.username_.getUser(), getFileName(selectedFilePath))){
          access = "R";
     }
     else{
         access = getCheckBoxState();
     }
        myObj.addDataTofileDB(person, newFileName, fileSize,access,chunkUUIDs[0], chunkUUIDs[1], chunkUUIDs[2], chunkUUIDs[3], "password",calculateCRC32(pathToCreated + selectedFilePath));
     if((myObj.getCrc(getuser_, getFileName(selectedFilePath))) == calculateCRC32(pathToCreated + selectedFilePath)){
    Log.addLog("User " + username_.getUser() + " Shared File: " + newFileName + " to " + person, "log");
    initialise2();
      dialogue("", "File shared to user: " + person + "successfully!");
     }
     else{
        edialogue("CRC ERROR", "CRC32 check failed");
     }
    

    if (doesFileExist(getFileName(selectedFilePath))) {
        deleteFile(pathToCreated + selectedFilePath);
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
        if(selectedFilePath == null ){
           edialogue("", "No file selected");
           return;
        }
        if (selectedFilePath != null & (myObj.doesItemExist("fileName_", selectedFilePath, LoginController.username_.getUser(), "userName"))
                & !(myObj.checkStatus(username_.getUser(), getFileName(selectedFilePath)))) {
            DB Log = new DB("appLogs");
            String[] chunkIds = myObj.getChunkIds(getFileName(selectedFilePath), LoginController.username_.getUser());
            System.arraycopy(chunkIds, 0, chunkUUIDs, 0, Numberofchunks);
            joinFiles(pathToTemp, getFileName(selectedFilePath),"/home/ntu-user/Downloads/");
            if((myObj.getCrc(getuser_, getFileName(selectedFilePath))) != calculateCRC32("/home/ntu-user/Downloads/" + getFileName(selectedFilePath))){
                edialogue("CRC ERROR", "CRC32 check failed");
            }
            Log.addLog("User " + LoginController.username_.getUser() + " Downloaded File: " + getFileName(selectedFilePath), "log");
            if(myObj.doesACLExist(LoginController.username_.getUser(), getFileName(selectedFilePath))){            
            makeFileReadOnly("/home/ntu-user/Downloads/" + getFileName(selectedFilePath) );
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
        DB myObj = new DB("fileInfo");
        DB Log = new DB("appLogs");
        myObj.updateField2(username_.getUser() ,getFileName(selectedFilePath),"Deleted");
        Log.addLog("User " + username_.getUser() + " Deleted File: " + getFileName(selectedFilePath), "log");
        initialise2();
        initialise3();
        cloudinput2.setText("");
        selectedFilePath = null;
        }
        else {
            edialogue("CANNOT DELETE", "NO FILE SELECTED");
        }
    
    }
    
    @FXML
    private void recoverBtnHandler(ActionEvent event) throws IOException, ClassNotFoundException {
        if (selectedFilePath != null) {
        DB myObj = new DB("fileInfo");
        DB Log = new DB("appLogs");
        myObj.updateField2(username_.getUser() ,getFileName(selectedFilePath),"Exists");
        Log.addLog("User " + username_.getUser() + " Deleted File: " + getFileName(selectedFilePath), "log");
        initialise2();
        initialise3();
        cloudinput2.setText("");
        selectedFilePath = null;
        }
        else {
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
            Log.addLog("File: " + fileName + " Was deleted from "+ username_.getUser() + " Recently deleted folder", "log");
        }
        initialise3();

    
    }

    private void Delete(String name) throws IOException, ClassNotFoundException {
        DB myObj = new DB("fileInfo");
        String[] chunkIds = myObj.getChunkIds(name, LoginController.username_.getUser());
        for(int i = 1; i <= Numberofchunks; i++){
        ScpTo.dockerConnect("","Vchunk" + chunkIds[i-1] + ".bin", Containers[i-1], "delete");
        }
        myObj.deleteRecord("fileName_",name,username_.getUser());
    }
    
    
    @FXML
    private void readFileButtonHandler() throws ClassNotFoundException {
    DB myobj = new DB("fileInfo");
    if(selectedFilePath == null ){
      edialogue("", "No file selected");
      return;
    }
    if((myobj.doesItemExist("fileName_", getFileName(selectedFilePath), LoginController.username_.getUser(), "userName"))){
    selectedFilePath = pathToCreated + selectedFilePath;
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
    
    @FXML
    private void UpdateButtonHandler() throws ClassNotFoundException, InvalidKeySpecException, IOException {
    DB myobj = new DB("fileInfo");
    DB Log = new DB("appLogs");    
    if(selectedFilePath == null ){
        edialogue("", "No file selected");
        return;
     }
    if(myobj.doesACLExist(LoginController.username_.getUser(), getFileName(selectedFilePath))){
       edialogue("Permission error", "You have read only permissions");
       return;
    }
   
    if(!(myobj.doesItemExist("fileName_", getFileName(selectedFilePath), LoginController.username_.getUser(), "userName"))){
    writeToFile(selectedFilePath, output.getText());
    dialogue("", "Upload to save changes");
    return;
    }
    
     if (myobj.doesItemExist("fileName_", getFileName(selectedFilePath), LoginController.username_.getUser(), "userName")) {
            writeToFile(pathToCreated + selectedFilePath, output.getText());
            String[] chunkIds = myobj.getChunkIds(getFileName(selectedFilePath), LoginController.username_.getUser());
            for(int i = 1; i <= Numberofchunks; i++){
            ScpTo.dockerConnect("","Vchunk" + chunkIds[i-1] + ".bin", Containers[i-1], "delete");
            }
            splitFileIntoChunks(pathToCreated + selectedFilePath, pathToTemp);
            myobj.updateDataTofileDB(LoginController.username_.getUser(), getFileName(selectedFilePath), fileSize, getFilePermissions(selectedFilePath), chunkUUIDs[0], chunkUUIDs[1], chunkUUIDs[2], chunkUUIDs[3], 123,calculateCRC32(selectedFilePath));
            initialise2();
            Log.addLog("User " + LoginController.username_.getUser() + " Updated file: " + getFileName(selectedFilePath), "log");
            selectedFilePath = null;
            cloudinput.setText("");
            output.setText("");
     }
    

}

    
   @FXML
   private void createButtonHandler(ActionEvent event) throws ClassNotFoundException {
    Handler hand = new Handler();

    String userCommand = fileNameinput.getText();
    DB myObj = new DB("fileInfo");
    DB Log = new DB("appLogs");
    
    
    if (myObj.doesItemExist("fileName_", userCommand, LoginController.username_.getUser(), "userName")) {
        // File with the same name already exists
        edialogue("CANNOT CREATE", "FILE ALREADY EXISTS ON THE CLOUD");
    } else {
        // File does not exist, proceed with creating
        cloudinput.setText("");
        output.setText("");
        hand.fileCreating(userCommand);
        selectedFilePath = pathToCreated + userCommand;
            
        if (!"".equals(userCommand)) {
            dialogue("CREATING FILE", "Successful!, You can now write and update your file");
            Log.addLog("User " + LoginController.username_.getUser() + " Created File: " + getFileName(selectedFilePath), "log");
        } else {
            edialogue("CANNOT CREATE", "ENTER FILE NAME");
        }
    }
}
    
     @FXML
    private void uploadButtonHandler(ActionEvent event) throws InvalidKeySpecException, net.lingala.zip4j.exception.ZipException, ClassNotFoundException, IOException {
        DB myObj = new DB("fileInfo");
        DB Log = new DB("appLogs");
        if(selectedFilePath == null ){
        edialogue("", "No file selected");
        return;
        }
        if (myObj.doesItemExist("fileName_", getFileName(selectedFilePath), LoginController.username_.getUser(), "userName")) {
        // File with the same name already exists
        edialogue("CANNOT UPLOAD", "TRY UPDATING INSTEAD");
        return;
    } 
        
        if (selectedFilePath != null & !(myObj.doesItemExist("fileName_", selectedFilePath, LoginController.username_.getUser(), "userName"))) {
             splitFileIntoChunks(pathToCreated + getFileName(selectedFilePath), pathToTemp);
             myObj.addDataTofileDB(LoginController.username_.getUser(), getFileName(selectedFilePath), fileSize, getFilePermissions(selectedFilePath), chunkUUIDs[0], chunkUUIDs[1], chunkUUIDs[2], chunkUUIDs[3], "password",calculateCRC32(selectedFilePath));
             initialise2();
             Log.addLog("User " + LoginController.username_.getUser() + " Uploaded File: " + getFileName(selectedFilePath), "log");
             if(doesFileExist(getFileName(selectedFilePath))){
                deleteFile(selectedFilePath);
                selectedFilePath = null;
                fileNameinput.setText("");
                cloudinput.setText("");
                output.setText("");
             }
        } else {
            edialogue("CANNOT UPLOAD", "NO FILE SELECTED");
            
        }
    
    }

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
            ObservableList<Map<String, String>> data = myObj.getDataFromTable2(LoginController.username_.getUser());

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
    
      public static void splitFileIntoChunks(String filePath, String Outputpath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            File file = new File(filePath);
            fileSize = file.length();
            long chunkSize = fileSize / Numberofchunks;

            // Generate UUIDs as strings directly and store them in the array
            for (int i = 0; i < Numberofchunks; i++) {
                chunkUUIDs[i] = UUID.randomUUID().toString();
            }

            for (int i = 1; i <= Numberofchunks; i++) {
                try (FileOutputStream fos = new FileOutputStream(Outputpath + "/chunk" + i + ".bin")) {
                    byte[] buffer = new byte[(int) chunkSize];
                    int bytesRead = fis.read(buffer);

                    // If the last chunk is smaller, adjust the buffer size
                    if (i == Numberofchunks && bytesRead < chunkSize) {
                        buffer = Arrays.copyOf(buffer, bytesRead);
                    }

                    fos.write(buffer);

                    // Use chunkUUIDs in your file transfer logic
                    ScpTo.dockerConnect("chunk" + i + ".bin","Vchunk" + chunkUUIDs[i - 1] + ".bin", Containers[i-1], "create");

                    deleteFile(Outputpath + "/chunk" + i + ".bin"); // Delete using UUID string
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void joinFiles(String directoryPath,String originalFileName, String outputPath) {
    try (FileOutputStream fos = new FileOutputStream(outputPath + originalFileName)) {
        for (int i = 1; i <= Numberofchunks; i++) {
            String virtualFileName = "Vchunk" + chunkUUIDs[i - 1] + ".bin";
            ScpTo.dockerConnect("chunk" + i + ".bin", virtualFileName, Containers[i-1], "get");
            
            try (FileInputStream fis = new FileInputStream(directoryPath + "/chunk" + i + ".bin")) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            deleteFile(directoryPath + "/chunk" + i + ".bin");
        }
    } catch (IOException e) {
        edialogue("Join error","Try again");
        e.printStackTrace();
    }
}



       
    
    public static void readFileAndOutputToTextArea(File file, TextArea outputTextArea) {
        if (file == null || outputTextArea == null) {
            System.err.println("File or TextArea is null");
            return;
        }
        
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

    public static void writeToFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
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
        File File = new File(filePath);
        if (File.delete()) {
        } else {
            System.out.println("Failed to delete chunk file: " + File.getName());
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

    public String getCheckBoxState() {
        return checkBoxState;
    }
    
    
    
     

 
    }