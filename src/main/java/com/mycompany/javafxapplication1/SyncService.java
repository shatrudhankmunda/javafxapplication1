package com.mycompany.javafxapplication1;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SyncService {
    public static void syncAll() {
        try {
            AppLogSyncService.syncLogsFromMySQL();
            AppLogSyncService.syncLogsToMySQL();

            FileInfoSyncService.syncFromMySQL();
            FileInfoSyncService.syncToMySQL();


            SessionSyncService.syncSessionsToMySQL();

            SyncUser.syncUsersToMySQL();
            SyncUser.syncUsersFromMySQL();

            Platform.runLater(() -> showAlert("Sync Success", "All data synced!", Alert.AlertType.INFORMATION));
        } catch (Exception e) {
            Logger.getLogger(SyncService.class.getName()).log(Level.SEVERE,"Synced Failed, Error: "+ e.getMessage());
            Platform.runLater(() -> showAlert("Sync Failed", "Error: " + e.getMessage(), Alert.AlertType.ERROR));
        }
    }

    private static void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
