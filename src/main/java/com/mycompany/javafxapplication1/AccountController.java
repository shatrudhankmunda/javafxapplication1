/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.javafxapplication1;


import static com.mycompany.javafxapplication1.FilemanagerController.Containers;
import static com.mycompany.javafxapplication1.FilemanagerController.dialogue;
import static com.mycompany.javafxapplication1.FilemanagerController.edialogue;
import static com.mycompany.javafxapplication1.ScpTo.numberOfChunks;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author ntu-user
 */
public class AccountController {
     @FXML
    private Text fileText;
     
    @FXML
    private TextField namechangefield;
    
     @FXML
    private TextField namechangefield2;
     
    @FXML
    private TextField changepass1;
    
    @FXML
    private TextField repass;
    
    @FXML
    private TextField oldpass;
    
    @FXML
    private TextField delacctname;
    
    @FXML
    private TextField delacctpass;
    
    
    @FXML
    private Button confirm;
    
    @FXML
    private Button confirm2;
    
    @FXML
    private Button confirm3;
    
    @FXML
    private Button backButton;
        
    private static final Logger logger = Logger.getLogger(AccountController.class.getName());
     
     
     
     @FXML
     public void initialise(){
     fileText.setText(SessionManager.getInstance().getCurrentUser());
     }
     
     
     
     
     @FXML
    private void Backbuttonahndler(ActionEvent event){
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) backButton.getScene().getWindow();
        try {
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
            Logger.getLogger(AccountController.class.getName()).log(Level.SEVERE, "Error during navigation to secondary.fxml", e);
            edialogue("System Error", "An error occurred while trying to navigate. Please try again later.");
        }
    }


     
     
     
     
     
      @FXML
    private void chnageUserNameHandler(ActionEvent event) throws ClassNotFoundException {
        String getuser_ = SessionManager.getInstance().getCurrentUser();
        String getpass = SessionManager.getInstance().getPassword();
        String newName = namechangefield.getText(); // Get the new username from the text field
        String enteredPassword = namechangefield2.getText();
          if (newName == null || newName.trim().isEmpty()) {
              edialogue("Invalid Input", "Username cannot be empty");
              return;
          }
          if (enteredPassword == null || enteredPassword.trim().isEmpty()) {
              edialogue("Invalid Input", "Password cannot be empty");
              return;
          }
        DB myobj = new DB("Users");
        DB log = new DB("appLogs");
        DB fileInfo = new DB("fileInfo");
        
        if(myobj.nameExists(newName)){
                edialogue("NAME ERROR","Username Taken");
                return;
            }

        try {
            if (getpass.equals(enteredPassword)) {
                if (!newName.equals(getuser_)) {
                    myobj.updateField("username", newName, getuser_,"username");
                    fileInfo.updateField("userName",newName, getuser_,"userName");
                    log.addLog("User " + getuser_ + " Changed Username to " + newName, "log");
                    namechangefield.clear();
                    namechangefield2.clear();
                    changepass1.clear();
                    repass.clear();
                    oldpass.clear();
                    delacctname.clear();
                    delacctpass.clear();
                    fileText.setText(newName);
                    SessionManager.getInstance().login(newName,getpass);
                    Logger.getLogger(AccountController.class.getName()).log(Level.INFO, "Username changed successfully for user: {0}", newName);
                    dialogue("Username Update", "Successful");
                    
                } else {
                    // New name is the same as the current name
                    Logger.getLogger(AccountController.class.getName()).log(Level.WARNING, "Attempted to change username to the same name: {0}", newName);
                    edialogue("Invalid Name", "New name cannot be the same as the current name");
                }
            } else {
                // Password mismatch
                Logger.getLogger(AccountController.class.getName()).log(Level.WARNING, "Password mismatch for user: {0}", getuser_);
                edialogue("Invalid Password", "Please try again with the correct password");
            }
        } catch (Exception e) {
            // SQLiteException occurred
            Logger.getLogger(AccountController.class.getName()).log(Level.SEVERE, "Error changing username for user: " + getuser_, e.getMessage());
            edialogue("Error", "Name already in use");
        } 
    }
  
    
    
    
     @FXML
    private void chnageUserpasswordHandler(ActionEvent event) throws ClassNotFoundException, InvalidKeySpecException {

        String old = oldpass.getText(); 
        String newpass = changepass1.getText(); 
        String retyp = repass.getText();
         String getuser_ = SessionManager.getInstance().getCurrentUser();
         String getpass = SessionManager.getInstance().getPassword();
         if (old == null || old.trim().isEmpty()) {
             Logger.getLogger(AccountController.class.getName()).log(Level.WARNING, "Old password is empty for user: {0}", getuser_);
             edialogue("Invalid Input", "Old password cannot be empty");
             return;
         }
         if (newpass == null || newpass.trim().isEmpty()) {
             edialogue("Invalid Input", "New Password cannot be empty");
             return;
         }
         if (retyp == null || retyp.trim().isEmpty()) {
             edialogue("Invalid Input", "Re-type Password cannot be empty");
             return;
         }

        DB myobj = new DB("Users");
        DB Log = new DB("appLogs");

        if (old.equals(getpass) && newpass.equals(retyp) && !newpass.equals(getpass)) {
            Log.addLog("User " + getuser_ + " Changed Password", "log");
            myobj.updateField("password",myobj.hashPassword(newpass), getuser_,"username");
            SessionManager.getInstance().login(getuser_, newpass);
            namechangefield.clear();
            namechangefield2.clear();
            changepass1.clear();
            repass.clear();
            oldpass.clear();
            delacctname.clear();
            delacctpass.clear();
            Logger.getLogger(AccountController.class.getName()).log(Level.INFO, "Password changed successfully for user: {0}", getuser_);
            dialogue("Username Update", "Successful");
        } else {
            Logger.getLogger(AccountController.class.getName()).log(Level.WARNING, "Password change failed for user: {0}", getuser_);
            edialogue("COULD NOT CHANGE", "Please try again");
        }
    }
    
    
    @FXML
    private void deleteAccountBtnHandler(ActionEvent event) throws ClassNotFoundException, InvalidKeySpecException, IOException {
        String getuser_ = SessionManager.getInstance().getCurrentUser();
        String getpass = SessionManager.getInstance().getPassword();
        String username = delacctname.getText();
        String pass = delacctpass.getText();
        if (username == null || username.trim().isEmpty()) {
            edialogue("Invalid Input", "Username cannot be empty");
            return;
        }
        if (pass == null || pass.trim().isEmpty()) {
            edialogue("Invalid Input", "Password cannot be empty");
            return;
        }

        // Show confirmation dialog before deleting
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Are you sure you want to delete your account?");
        alert.setContentText("This action cannot be undone.");

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            DB myobj = new DB("Users");
            DB files = new DB("fileInfo");
            DB Log = new DB("appLogs");

            if (getuser_.equals(username) && getpass.equals(pass)) {
                Log.addLog("User " + getuser_ + " Account was deleted", "log");
                List<String> filesToDelete = files.getFilesToDelete2(username);
                for (String fileName : filesToDelete) {
                    Delete(fileName);
                }
                myobj.deleteUser(username);
                logger.info("User " + SessionManager.getInstance().getCurrentUser() + " deleted their account.");
                dialogue("ACCOUNT DELETED", "Successful!");
                logger.info("LOGOUT: " + SessionManager.getInstance().getCurrentUser());
                SessionStore.saveLogout(SessionManager.getInstance().getCurrentUser());
                SessionManager.getInstance().logout();
                switchToPrimary();
            } else {
                logger.warning("Account deletion failed for user: " + getuser_ + ". Incorrect username or password.");
                edialogue("ACCOUNT NOT DELETED", "Please try again with the correct Username and password");
            }
        }
    }
    
    
    
    @FXML
    private void switchToPrimary(){

        Stage stage = (Stage) confirm3.getScene().getWindow();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            stage.setScene(scene);
            stage.setTitle("Login user");
            stage.show();
        } catch (Exception e) {
            Logger.getLogger(AccountController.class.getName()).log(Level.SEVERE, "Error during navigation to Login.fxml", e);
            edialogue("System Error", "An error occurred while trying to navigate. Please try again later.");
        }
    }
    private void Delete(String name) throws IOException, ClassNotFoundException {
        DB myObj = new DB("fileInfo");
        String[] chunkIds = myObj.getChunkIds(name, SessionManager.getInstance().getCurrentUser());
        logger.info("Deleting file: " + name + " with chunk IDs: " + String.join(", ", chunkIds));
        for(int i = 0; i < numberOfChunks; i++){
//        ScpTo.dockerConnect("","Vchunk" + chunkIds[i-1] + ".bin", Containers[i-1], "delete");
            try {
                ScpTo.dockerConnect("", "Vchunk" + chunkIds[i] + ".bin", "localhost", 2221+i, "delete");
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error deleting chunk: " + chunkIds[i], e.getMessage());
                edialogue("Error", "Failed to delete chunk: " + chunkIds[i]);
                return;
            }
        }
        myObj.deleteRecord("fileName_",name,SessionManager.getInstance().getCurrentUser());
    }

  
}
