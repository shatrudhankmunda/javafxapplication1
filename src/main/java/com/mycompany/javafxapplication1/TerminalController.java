/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.javafxapplication1;
import static com.mycompany.javafxapplication1.PrimaryController.username_;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author ntu-user
 */
public class TerminalController {
    
   @FXML
    private TextField commandInput;
    
   @FXML
   private Button enterbtn;
   
   @FXML
   private TextArea commandDisplay;
   
   @FXML
   private Button backButton;
   
   
    @FXML
    private void onEnterButtonClicked(ActionEvent event) {
        
        Handler hand= new Handler();
        
        String userCommand = commandInput.getText();
        String output = hand.terminal(userCommand);
        commandDisplay.setText(output);
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
            controller.initialise();
            secondaryStage.setTitle("Secondary view");
            secondaryStage.show();
            primaryStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


   
   
    
    
    
    
    

