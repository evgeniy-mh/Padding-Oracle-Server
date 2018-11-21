package com.evgeniy_mh.paddingoracleserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ProgressIndicator;

public class ServerSocketProcessor implements Runnable {

    private final int SERVER_PORT = 55555;    

    private final BlockingQueue<String> mMessageQueue;
    private final ProgressIndicator mProgressIndicator;    

    ServerSocketProcessor(BlockingQueue<String> messageQueue, ProgressIndicator progressIndicator) {
        mMessageQueue = messageQueue;
        mProgressIndicator = progressIndicator;
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
            mMessageQueue.put(message);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerSocketProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initServer() throws IOException {
        try (ServerSocket server = new ServerSocket(SERVER_PORT)) {
            while (true) {
                putMessage("Waiting for a client...");
                Socket fromclient = server.accept();
                putMessage("Client connected");                                
                
                ClientSocketProcessor clientSocketProcessor=new ClientSocketProcessor(mMessageQueue, mProgressIndicator, fromclient);
                Thread t=new Thread(clientSocketProcessor);
                t.setDaemon(true);
                t.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerSocketProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    

}
