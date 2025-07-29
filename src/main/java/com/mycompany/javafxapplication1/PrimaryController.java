package com.mycompany.javafxapplication1;

import static com.mycompany.javafxapplication1.FilemanagerController.Containers;
import static com.mycompany.javafxapplication1.FilemanagerController.edialogue;
import static com.mycompany.javafxapplication1.ScpTo.numberOfChunks;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class PrimaryController {

    @FXML
    private Button registerBtn;

    @FXML
    private TextField userTextField;

    @FXML
    private PasswordField passPasswordField;
    
     public static User username_;

    @FXML
    private void registerBtnHandler(ActionEvent event) {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) registerBtn.getScene().getWindow();
        DB myObj = new DB("Users");
        try {
            DB fileDatabaseHandler = new DB("fileInfo");
            DB Log = new DB("appLogs");
            List<String> filesToDelete = fileDatabaseHandler.getFilesToDelete();
            for (String fileName : filesToDelete) {
            Delete(fileName);         
            Log.addLog("File: " + fileName + " Was deleted from "+ SessionManager.getInstance().getCurrentUser() + " Recently deleted folder", "log");
            }
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 640, 480);
            secondaryStage.setScene(scene);
            secondaryStage.setTitle("Register a new User");
            secondaryStage.show();
            primaryStage.close();
        } catch (Exception e) {
            Logger.getLogger(PrimaryController.class.getName()).log(Level.SEVERE, "Error during registration", e);
            FilemanagerController.edialogue("System Error", "An error occurred while trying to register. Please try again later.");
            return;
        }
    }
    

    @FXML
    private void switchToSecondary() {
        Stage secondaryStage = new Stage();
        Stage primaryStage = (Stage) registerBtn.getScene().getWindow();
        try {
            DB myObj = new DB("Users");
            String[] credentials = {userTextField.getText(), passPasswordField.getText()};
            if(myObj.validateUser(userTextField.getText(), passPasswordField.getText())){
                username_ = new User(userTextField.getText(), passPasswordField.getText());
                FXMLLoader loader = new FXMLLoader();
                DB fileDatabaseHandler = new DB("fileInfo");
                DB Log = new DB("appLogs");
                List<String> filesToDelete = fileDatabaseHandler.getFilesToDelete();
                for (String fileName : filesToDelete) {
                Delete(fileName);         
                Log.addLog("File: " + fileName + " Was deleted from "+ SessionManager.getInstance().getCurrentUser() + " Recently deleted folder", "log");
                 }
                loader.setLocation(getClass().getResource("secondary.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root, 640, 480);
                secondaryStage.setScene(scene);
                SecondaryController controller = loader.getController();
                controller.initialiseSession();
                secondaryStage.setTitle("Show Users");
                String msg="some data sent from Primary Controller";
                secondaryStage.setUserData(msg);               
                secondaryStage.show();
                primaryStage.close();
            }
            else{
                FilemanagerController.edialogue("Invalid User Name / Password","Please try again!");
            }

        } catch (Exception e) {
            Logger.getLogger(PrimaryController.class.getName()).log(Level.SEVERE, "Error during login", e);
            FilemanagerController.edialogue("System Error", "An error occurred while trying to log in. Please try again later.");
            return;
        }
    }
      private void Delete(String name) throws IOException, ClassNotFoundException {
        DB myObj = new DB("fileInfo");
        String[] chunkIds = myObj.getChunkIds(name, SessionManager.getInstance().getCurrentUser());
        for(int i = 0; i < numberOfChunks; i++){
//        ScpTo.dockerConnect("","Vchunk" + chunkIds[i-1] + ".bin", Containers[i-1], "delete");
            try {
                ScpTo.dockerConnect("","Vchunk" + chunkIds[i] + ".bin", "localhost",2221+i, "delete");
            } catch (Exception e) {
                Logger.getLogger(PrimaryController.class.getName()).log(Level.SEVERE, "Error deleting chunk: " + chunkIds[i], e.getMessage());
                edialogue("Error Deleting File", "Failed to delete chunk Vchunk" + chunkIds[i] + ".bin. Please check the connection or file existence.");
                return;
            }
        }
        myObj.deleteRecord("fileName_",name,SessionManager.getInstance().getCurrentUser());
    }
}
