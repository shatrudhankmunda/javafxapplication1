package com.mycompany.javafxapplication1;

//import com.gui.app.util.*;
//import com.mycompany.javafxapplication1.util.SQLiteHelper;
//import com.mycompany.javafxapplication1.util.SceneNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    private void handleRegister(ActionEvent event) throws IOException{
         String username = usernameField.getText();
         String password = passwordField.getText();
         String confirmPassword = confirmPasswordField.getText();
        if (username == null || username.trim().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Username cannot be empty").show();
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Password cannot be empty").show();
            return;
        }
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Confirm Password cannot be empty").show();
            return;
        }
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) usernameField.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader();
            DB myObj = new DB("Users");
            if (password.equals(confirmPassword)) {
                if (myObj.validateExistUser(username)) {
                    usernameField.clear();
                    passwordField.clear();
                    confirmPasswordField.clear();
                    new Alert(Alert.AlertType.ERROR, "Username already exists. Please choose another.").show();
                    return;
                }
                myObj.addDataToDB(username, password);
                //dialogue("Adding information to the database", "Successful!");
                String[] credentials = {username, password};
                loader.setLocation(getClass().getResource("Login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 640, 480);
                secondaryStage.setScene(scene);
                LoginController controller = loader.getController();
                secondaryStage.setTitle("User Login");
                //controller.initialise(credentials);
                String msg = "some data sent from Register Controller";
                secondaryStage.setUserData(msg);
            } else {
                new Alert(Alert.AlertType.ERROR, "Passwords do not match").show();
                return;
            }
            Logger.getLogger(RegisterController.class.getName()).log(Level.INFO,"Resgistration successful!");
            new Alert(Alert.AlertType.INFORMATION, "Registration successful!").showAndWait();
            secondaryStage.show();

        } catch (Exception e) {
            usernameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
            Logger.getLogger(RegisterController.class.getName()).log(Level.SEVERE, e.getMessage(), e);
           new Alert(Alert.AlertType.ERROR, "Failed to  register user !").show();
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) throws IOException {

        Stage stage = (Stage) usernameField.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            stage.setScene(scene);
            stage.setTitle("User Login");
            stage.show();

        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Failed to load login screen").show();
        }
    }
}
