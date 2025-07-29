package com.mycompany.javafxapplication1;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static com.mycompany.javafxapplication1.FilemanagerController.Containers;
import static com.mycompany.javafxapplication1.FilemanagerController.edialogue;
import static com.mycompany.javafxapplication1.ScpTo.numberOfChunks;

public class LoginController {
    private static final Logger logger = Logger.getLogger(LoginController.class.getName());
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    
   public static User username_;

    @FXML
    private void handleLogin(ActionEvent event) {
       try{
           String username = usernameField.getText();
           String password = passwordField.getText();
           if (username == null || username.trim().isEmpty()) {
               new Alert(Alert.AlertType.ERROR, "Username cannot be empty").show();
               return;
           }
           if (password == null || password.trim().isEmpty()) {
               new Alert(Alert.AlertType.ERROR, "Password cannot be empty").show();
               return;
           }
           DB userDB = new DB("Users");
           if (userDB.validateUser(username, password)) {
               username_ = new User(username, password);
               SessionManager.getInstance().login(username, password);
               SessionStore.saveLogin(username);
               logger.info("LOGIN: " + username + " logged in.");
               FXMLLoader loader = new FXMLLoader();
               DB fileDatabaseHandler = new DB("fileInfo");
               DB Log = new DB("appLogs");
               List<String> filesToDelete = fileDatabaseHandler.getFilesToDelete();
               for (String fileName : filesToDelete) {
                   delete(fileName);
                   Log.addLog("File: " + fileName + " Was deleted from "+ username + " Recently deleted folder", "log");
               }
               loader.setLocation(getClass().getResource("secondary.fxml"));
               Parent root = loader.load();
               Scene scene = new Scene(root, 640, 480);
               Stage secondaryStage = new Stage();
               secondaryStage.setScene(scene);
               SecondaryController controller = loader.getController();
               controller.initialiseSession();
               secondaryStage.setTitle("Show Users");
               String msg="some data sent from Primary Controller";
               secondaryStage.setUserData(msg);
               secondaryStage.show();
               Stage primaryStage = (Stage) usernameField.getScene().getWindow();
               primaryStage.close();
           } else {
               logger.warning("LOGIN FAILED: Attempted login with username=" + username);
               Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid credentials", ButtonType.OK);
               alert.showAndWait();
           }
       }catch(Exception e){
           Alert alert = new Alert(Alert.AlertType.ERROR, "System Error", ButtonType.OK);
       }
    }

    @FXML
    private void goToRegister(ActionEvent event) throws IOException {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Register.fxml"));
        stage.setTitle("Register new user");
        Parent root = loader.load();
        Scene scene = new Scene(root, 640, 480);
        stage.setScene(scene);
        stage.show();

    }
    private void delete(String name) throws IOException, ClassNotFoundException {
        DB myObj = new DB("fileInfo");
        String[] chunkIds = myObj.getChunkIds(name, SessionManager.getInstance().getCurrentUser());
        for(int i = 0; i < numberOfChunks; i++){
            try {
                ScpTo.dockerConnect("","Vchunk" + chunkIds[i] + ".bin", "localhost", 2221+i,  "delete");
            } catch (Exception e) {
                logger.severe("Failed to delete chunk Vchunk" + chunkIds[i] + ".bin: " + e.getMessage());
                edialogue("Error Deleting File", "Failed to delete chunk Vchunk" + chunkIds[i] + ".bin. Please check the connection or file existence.");
                return;
            }
        }
        myObj.deleteRecord("fileName_",name,SessionManager.getInstance().getCurrentUser());
    }
}
