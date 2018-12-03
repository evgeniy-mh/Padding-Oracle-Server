package com.evgeniy_mh.paddingoracleserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.ProgressIndicator;

public class ServerSocketProcessor implements Runnable {

    private final int SERVER_PORT = 55555;
    private AtomicBoolean running = new AtomicBoolean(false);
    private ServerSocket server = null;

    private final byte[] mKey;

    public ServerSocketProcessor( byte[] key) {
        mKey = key;
    }

    @Override
    public void run() {
        try {
            running.set(true);
            initServer();
        } catch (IOException ex) {
            Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void stop() {
        running.set(false);
        try {
            if (!server.isClosed()) {
                server.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerSocketProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isRunning() {
        return running.get();
    }

    private void initServer() throws IOException {
        server = new ServerSocket(SERVER_PORT);
        while (running.get()) {
            try {
                Socket fromclient = server.accept();

                ClientSocketProcessor clientSocketProcessor = new ClientSocketProcessor(fromclient, mKey);
                Thread t = new Thread(clientSocketProcessor);
                t.setDaemon(true);
                t.start();
            } catch (SocketException ex) {
                System.out.println("initServer() SocketException");
            }
        }
        server.close();
    }

}
