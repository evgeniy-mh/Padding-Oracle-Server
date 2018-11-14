/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.evgeniy_mh.paddingoracleserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
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
    File tempSavedFile;

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
        while (true) {
            ServerSocket servers = new ServerSocket(55555);
            putMessage("Waiting for a client...");
            Socket fromclient = servers.accept();
            putMessage("Client connected");

            InputStream sin = fromclient.getInputStream();
            OutputStream sout = fromclient.getOutputStream();

            DataInputStream in = new DataInputStream(sin);
            DataOutputStream out = new DataOutputStream(sout);

            String line = in.readUTF();

            if (line.equals("new file")) {
                long fileSize = in.readLong();
                putMessage("New file from client, size: " + fileSize);

                //File pathnameParentDir = new File(MainApp.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
                //tempSavedFile = new File(pathnameParentDir, "tempSavedFile");
                tempSavedFile = new File("/home/evgeniy/Files/Downloads/temp");

                tempSavedFile.createNewFile();
                FileOutputStream fis = new FileOutputStream(tempSavedFile);

                int t;
                while ((t = sin.read()) != -1) {
                    fis.write(t);
                }
                fis.close();
                putMessage("Saved new file from client");

            }

            out.close();
            in.close();
            sout.close();
            sin.close();
            fromclient.close();
            servers.close();
        }
    }

}
