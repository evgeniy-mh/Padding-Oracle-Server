/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.evgeniy_mh.paddingoracleserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
public class ServerSocketProcessor implements Runnable {

    private final BlockingQueue<String> messageQueue;

    ServerSocketProcessor(BlockingQueue<String> messageQueue) {
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
            Logger.getLogger(ServerSocketProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initServer() throws IOException {
        ServerSocket servers = new ServerSocket(55555);
        putMessage("Waiting for a client...");
        Socket fromclient = servers.accept();
        putMessage("Client connected");

        /*BufferedReader in = new BufferedReader(new InputStreamReader(fromclient.getInputStream()));
        PrintWriter out = new PrintWriter(fromclient.getOutputStream(), true);

        String input, output;

        putMessage("Wait for messages");
        while ((input = in.readLine()) != null) {
            //out.println("S ::: " + input);
            putMessage(input);
        }*/
        InputStream sin = fromclient.getInputStream();
        OutputStream sout = fromclient.getOutputStream();

        DataInputStream in = new DataInputStream(sin);
        DataOutputStream out = new DataOutputStream(sout);

        String line = null;
        while ((line = in.readUTF()) != null) {
            putMessage(line);
        }

        out.close();
        in.close();
        fromclient.close();
        servers.close();
    }

}
