package com.evgeniy_mh.paddingoracleserver;

import com.evgeniy_mh.paddingoracleserver.AESEngine.AES_CBCDencryptor;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

public class ServerSocketProcessor implements Runnable {

    private final int SERVER_PORT = 55555;
    private final int PADDING_OK_RESPONSE = 200;
    private final int PADDING_ERROR_RESPONSE = 500;

    private final BlockingQueue<String> mMessageQueue;
    private final ProgressIndicator mProgressIndicator;

    private File tempSavedFile;

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
                    try (FileOutputStream fos = new FileOutputStream(tempSavedFile)) {
                        int t;
                        for (int i = 0; i < fileSize; i++) {
                            t = sin.read();
                            fos.write(t);
                        }
                    }
                    putMessage("Saved new file from client");

                    if (checkPadding(tempSavedFile)) {
                        out.writeInt(PADDING_OK_RESPONSE);
                        putMessage("Padding ok");
                    } else {
                        out.writeInt(PADDING_ERROR_RESPONSE);
                        putMessage("Padding error");
                    }
                    out.flush();
                    tempSavedFile.delete();
                    out.close();
                    in.close();
                    sout.close();
                    sin.close();
                    fromclient.close();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerSocketProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean checkPadding(File file) {
        //File pathnameParentDir = new File(MainApp.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
        //tempSavedFile = new File(pathnameParentDir, "tempDecryptedFile");
        File tempDecryptedFile = new File("/home/evgeniy/Files/Downloads/temp_dec");
        byte[] key = {5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
        Callable c = new AES_CBCDencryptor(file, tempDecryptedFile, key, mProgressIndicator);
        FutureTask<Boolean> ftask = new FutureTask<>(c);
        Thread thread = new Thread(ftask);
        thread.start();
        ftask.run();

        boolean isPaddingCorrect = false;
        try {
            isPaddingCorrect = ftask.get();

        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(ServerSocketProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        tempDecryptedFile.delete();
        return isPaddingCorrect;
    }

}
