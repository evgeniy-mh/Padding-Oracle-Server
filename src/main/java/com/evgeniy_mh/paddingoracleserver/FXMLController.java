package com.evgeniy_mh.paddingoracleserver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FXMLController {

    private MainApp mainApp;
    private ServerSocketProcessor processor = null;
    final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(1);

    @FXML
    Button startServerButton;
    @FXML
    Button stopServerButton;
    @FXML
    TextField secretKeyTextField;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void initialize() {
        stopServerButton.setDisable(true);

        startServerButton.setOnAction(event -> {
            byte[] key = getSecretKey();
            if (key == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ошибка ключа шифрования AES");
                alert.setHeaderText("Вы не ввели ключ или ввели ключ длина которого больше 128 бит.");
                alert.showAndWait();
            } else {
                processor = new ServerSocketProcessor(key);
                Thread server = new Thread(processor);
                server.setDaemon(true);
                server.start();
                stopServerButton.setDisable(false);
                startServerButton.setDisable(true);
            }
        });

        stopServerButton.setOnAction(event -> {
            if (processor != null && processor.isRunning()) {
                processor.stop();
                startServerButton.setDisable(false);
                stopServerButton.setDisable(true);
            }

        });

        /*final LongProperty lastUpdate = new SimpleLongProperty();
        final long minUpdateInterval = 0;
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate.get() > minUpdateInterval) {
                    final String message = messageQueue.poll();
                    if (message != null) {
                        
                        //redo!
                        if(ServerOutputTextArea.getText().length()>100) ServerOutputTextArea.clear();
                        
                        ServerOutputTextArea.appendText(message + "\n");
                    }
                    lastUpdate.set(now);
                }
            }
        };
        timer.start();*/
    }

    private byte[] getSecretKey() {
        byte[] key = secretKeyTextField.getText().getBytes(StandardCharsets.UTF_8);
        if (key.length == 0 || key.length > 128) {
            return null;
        } else {
            return key;
        }
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
