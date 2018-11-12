package com.evgeniy_mh.paddingoracleserver;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

public class FXMLController {
    
    private MainApp mainApp;
    
    @FXML
    Button testButton;
    
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void initialize() {
        testButton.setOnAction(event->{
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Test");
            alert.showAndWait();
        });
    }    
}
