package com.evgeniy_mh.paddingoracleserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerSocketProcessor implements Runnable {

    private final int PADDING_OK_RESPONSE = 200;
    private final int PADDING_ERROR_RESPONSE = 500;

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
            ServerSocket server = new ServerSocket(55555);
            putMessage("Waiting for a client...");
            Socket fromclient = server.accept();
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
                FileOutputStream fos = new FileOutputStream(tempSavedFile);

                int t;
                for(int i=0;i<fileSize;i++){
                    t = sin.read();
                    fos.write(t);
                }                
                fos.close();
                putMessage("Saved new file from client");

                if (checkPadding(tempSavedFile)) {
                    out.writeInt(PADDING_OK_RESPONSE);
                    putMessage("Padding ok");
                } else {
                    out.writeInt(PADDING_ERROR_RESPONSE);
                    putMessage("Padding error");
                }
            }

            out.close();
            in.close();
            sout.close();
            sin.close();
            fromclient.close();
            server.close();
        }
    }

    private boolean checkPadding(File file) {

        return true;
    }

}
