/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.evgeniy_mh.paddingoracleserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextArea;

/**
 *
 * @author evgeniy
 */
public class SocketProcessor implements Runnable {

    private final BlockingQueue<String> messageQueue;

    SocketProcessor(BlockingQueue<String> messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void run() {
        try {
            initServer();
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void putMessage(String message) {
        try {
            messageQueue.put(message);
        } catch (InterruptedException ex) {
            Logger.getLogger(SocketProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initServer() throws IOException {
        ServerSocket servers = null;
        servers = new ServerSocket(5555);

        Socket fromclient = null;

        putMessage("Waiting for a client...");
        fromclient = servers.accept();
        putMessage("Client connected");

        BufferedReader in = null;
        PrintWriter out = null;

        in = new BufferedReader(new InputStreamReader(fromclient.getInputStream()));
        out = new PrintWriter(fromclient.getOutputStream(), true);

        String input, output;

        putMessage("Wait for messages");
        while ((input = in.readLine()) != null) {
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            //out.println("S ::: " + input);
            putMessage(input);
        }
        out.close();
        in.close();
        fromclient.close();
        servers.close();
    }

}
