package com.mycompany.javafxapplication1;

import static com.mycompany.javafxapplication1.FilemanagerController.edialogue;
import static com.mycompany.javafxapplication1.FilemanagerController.pathToCreated;
import java.io.File;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;


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

    @FXML
    private Label syncStatusLabel;

    @FXML
    private AnchorPane rootPane;

    private static final long TIMEOUT_MILLIS = 5 * 60 * 1000;

    private static final Logger logger = Logger.getLogger(SecondaryController.class.getName());

    private void setupUserActivityListeners() {
        rootPane.setOnMouseMoved(e -> SessionManager.getInstance().updateActivity());
        rootPane.setOnKeyPressed(e -> SessionManager.getInstance().updateActivity());
    }

    public void startAutoLogoutTimer() throws Exception{
        Timeline checker = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> {
                    SessionManager session = SessionManager.getInstance();
                    if (!session.isSessionActive(TIMEOUT_MILLIS)) {
                        String user = session.getCurrentUser();
                        if (user != null) {
                            logger.info("SESSION TIMEOUT: " + user);
                        }
                        session.logout();

                        try {
                            Stage stage = new Stage();
                            FXMLLoader loader = new FXMLLoader();
                            loader.setLocation(getClass().getResource("Login.fxml"));
                            Parent root = loader.load();
                            Scene scene = new Scene(root, 640, 480);
                            stage.setScene(scene);
                            LoginController controller = loader.getController();
                            stage.setTitle("User Login");
                            stage.show();
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                })
        );
        checker.setCycleCount(Timeline.INDEFINITE);
        checker.play();
    }


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
            logger.log(Level.SEVERE, "Error loading terminal: " + e.getMessage());
            edialogue("Error", "Failed to load terminal. Please try again later.");
        }
        
    };
    @FXML
    private void handleSyncNow() {
        syncStatusLabel.setText("Syncing...");
        logger.info("Syncing started at " + LocalTime.now().withNano(0));
        try{
            if(MySQLDB.getConnection()==null){
                edialogue("Sync Failed !!", "MySQL Cloud is Down !!");
                logger.log(Level.SEVERE,"Sync Failed !! MySQL Cloud is Down !!");
                syncStatusLabel.setText("Not Synced");
                return;
            }
            Task<Void> syncTask = new Task<>() {
                @Override
                protected Void call() {
                    SyncService.syncAll();
                    return null;
                }
            };

            syncTask.setOnSucceeded(e -> {
                syncStatusLabel.setText("Sync completed at " + LocalTime.now().withNano(0));
                logger.info("Sync completed at " + LocalTime.now().withNano(0));
            });

            new Thread(syncTask).start();
        }catch (Exception e){
            edialogue("",e.getMessage());
            logger.log(Level.SEVERE,"Error: " + e.getMessage());
        }
    }
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
            logger.log(Level.SEVERE, "Error loading account settings: " + e.getMessage());
            edialogue("Error", "Failed to load account settings. Please try again later.");
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
                logger.log(Level.SEVERE, "Error during logout: " + e.getMessage(), e.getCause());
                edialogue("Logout Error", "An error occurred while logging out. Please try again.");
            }
        }
    }

    public void initialiseSession() throws Exception{
        SessionManager session = SessionManager.getInstance();
        String username = session.getCurrentUser();
        if (username != null) {
//            session.updateActivity();
//            setupUserActivityListeners();
//            startAutoLogoutTimer();
        } else {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            stage.setScene(scene);
            LoginController controller = loader.getController();
            stage.setTitle("User Login");
            stage.show();

        }
        userTextField.setText(SessionManager.getInstance().getCurrentUser());
        DB myObj = new DB("Users");
        ObservableList<User> data;
        try {
            data = myObj.getDataFromTable();
            TableColumn user = new TableColumn("User");
        user.setCellValueFactory(
        new PropertyValueFactory<>("user"));

        TableColumn pass = new TableColumn("Pass");
        pass.setCellValueFactory(
            new PropertyValueFactory<>("pass"));
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
            edialogue("Error", "Failed to load File Manager. Please try again later.");
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
            Logger.getLogger(SecondaryController.class.getName()).log(Level.WARNING, "Directory does not exist: " + directory.getAbsolutePath());
        }
    }
    
    private static void createDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
            } else {
                Logger.getLogger(SecondaryController.class.getName()).log(Level.SEVERE, "Failed to create directory: " + directoryPath);
            }
        }
    }


}
