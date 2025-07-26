package com.mycompany.javafxapplication1;

import static com.mycompany.javafxapplication1.FilemanagerController.pathToCreated;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;



public class SecondaryController {
    
    @FXML
    private TextField userTextField;
    
    @FXML
    private TableView dataTableView;

    @FXML
    private Button secondaryButton;
    
    @FXML
    private Button refreshBtn;
    
    @FXML
    private Button fileBtn;
    
    @FXML
    private TextField customTextField;
    
    @FXML
    private Button terminalbtn;
    
    @FXML
    private Button accountbtn;
    

    private static final Logger logger = Logger.getLogger(SecondaryController.class.getName());

    @FXML
    private void terminalbtnHandler(ActionEvent event){
        Stage stage = (Stage) terminalbtn.getScene().getWindow();
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Terminal.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            stage.setScene(scene);
            stage.setTitle("Terminal");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    };
    
    @FXML
    private void accountbtnHandler(ActionEvent event){
        

        Stage stage = (Stage) accountbtn.getScene().getWindow();
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("accountsettings.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            stage.setScene(scene);
            AccountController controller = loader.getController();
            controller.initialise();
            stage.setTitle("Update User profile");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    };
    
    @FXML
    private void RefreshBtnHandler(ActionEvent event){
        Stage primaryStage = (Stage) customTextField.getScene().getWindow();
        customTextField.setText((String)primaryStage.getUserData());
    }

    @FXML
    private void handleLogout() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout Confirmation");
        alert.setHeaderText("Are you sure you want to logout?");
        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            logger.info("LOGOUT: " + SessionManager.getInstance().getCurrentUser());
            SessionStore.saveLogout(SessionManager.getInstance().getCurrentUser());
            SessionManager.getInstance().logout();
            Stage stage = (Stage) secondaryButton.getScene().getWindow();
            String directoryPath = pathToCreated;
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("Login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 640, 480);
                stage.setScene(scene);
                stage.setTitle("User login");
                stage.show();
                deleteDirectory(new File(directoryPath));
                createDirectory(directoryPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initialise() {
        userTextField.setText(SessionManager.getInstance().getCurrentUser());
        DB myObj = new DB("Users");
        ObservableList<User> data;
        try {
            data = myObj.getDataFromTable();
            TableColumn user = new TableColumn("User");
        user.setCellValueFactory(
        new PropertyValueFactory<>("username"));

        TableColumn pass = new TableColumn("Pass");
        pass.setCellValueFactory(
            new PropertyValueFactory<>("password"));
        dataTableView.setItems(data);
        dataTableView.getColumns().addAll(user, pass);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SecondaryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    @FXML
    private void handleFilemanager(){

        Stage stage = (Stage) secondaryButton.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Filemanager.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            stage.setScene(scene);
            FilemanagerController controller = loader.getController();
            controller.initialise2();
            stage.setTitle("File Manager");
            stage.show();
        } catch (Exception ex) {
            Logger.getLogger(SecondaryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
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


}
