/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.javafxapplication1;

import static com.mycompany.javafxapplication1.FilemanagerController.pathToCreated;
import java.io.BufferedReader;
import java.io.File;
    import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author ntu-user
 */
public class Handler {



    public String terminal(String userCommand) {
        List<String> arr = new ArrayList<String>();
        String line = "";
        // Get user input for the command
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Enter a command (mv, cp, ls, mkdir, ps, whoami, tree, nano):");
       

        // Validate user input
        if (!isValidCommand(userCommand)) {
            return("Invalid command. Please enter a valid command.");
            
        }

        // Execute the chosen command
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(userCommand);

            Process process = processBuilder.start();
            
            try (var reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            
            while ((line = reader.readLine()) != null) {
                arr.add(line);
            } 
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        line = "";
        for (String i : arr) {
            line += i + "\n";
        }
        return line;
    }


    // Validate if the entered command is one of the allowed commands
    private boolean isValidCommand(String command) {
        String[] allowedCommands = {"mv", "cp", "ls", "mkdir", "ps", "whoami", "tree", "nano"};
        for (String allowedCommand : allowedCommands) {
            if (allowedCommand.equals(command)) {
                return true;
            }
        }
        return false;
    }
    
    
    
     public String fileCreating(String name) {
        String output = "";
        String directoryPath = pathToCreated;
        
        // Combine the directory path and the file name
        String filePath = directoryPath + name;
        
        File myObj = new File(filePath);
        try {
            // Create the directories if they don't exist
            myObj.getParentFile().mkdirs();
            
            if (myObj.createNewFile()) {
                output += ("File created: " + myObj.getName());
            } else {
                output += ("File already exists.");
            }
        } catch (IOException ex) {
            Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return output;
    }


    
    
        
        
}






