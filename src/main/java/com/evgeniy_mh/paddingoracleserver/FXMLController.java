package com.evgeniy_mh.paddingoracleserver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javafx.animation.AnimationTimer;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

public class FXMLController {

    private MainApp mainApp;
    final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(1);

    @FXML
    Button testButton;
    @FXML
    TextArea ServerOutputTextArea;
    @FXML
    ProgressBar progressBar;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void initialize() {
        testButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Test");
            alert.showAndWait();
        });

        ServerSocketProcessor processor = new ServerSocketProcessor(messageQueue, progressBar);
        Thread server = new Thread(processor);
        server.setDaemon(true);
        server.start();

        final LongProperty lastUpdate = new SimpleLongProperty();
        final long minUpdateInterval = 0;
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate.get() > minUpdateInterval) {
                    final String message = messageQueue.poll();
                    if (message != null) {
                        ServerOutputTextArea.appendText(message + "\n");
                    }
                    lastUpdate.set(now);
                }
            }
        };
        timer.start();
    }

    public static void showExceptionToUser(Throwable e, String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Exception!");
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        errorAlert.setContentText(message + "\n" + sw.toString());
        errorAlert.showAndWait();
    }
}
