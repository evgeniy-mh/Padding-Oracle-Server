package com.evgeniy_mh.paddingoracleserver;

import com.evgeniy_mh.paddingoracleserver.AESEngine.AES_CBCPaddingCheckBytes;
import com.evgeniy_mh.paddingoracleserver.AESEngine.AES_CBCPaddingCheckFile;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientSocketProcessor implements Runnable {

    private final int PADDING_OK_RESPONSE = 200;
    private final int PADDING_ERROR_RESPONSE = 500;

    private final Socket mClientSocket;
    private final byte[] mKey;

    ClientSocketProcessor(Socket clientSocket, byte[] key) {
        mClientSocket = clientSocket;
        mKey = key;
    }

    @Override
    public void run() {
        try {
            InputStream sin = mClientSocket.getInputStream();
            OutputStream sout = mClientSocket.getOutputStream();

            DataInputStream in = new DataInputStream(sin);
            DataOutputStream out = new DataOutputStream(sout);
            String line = in.readUTF();

            if (line.equals("new file")) {
                long fileSize = in.readLong();

                boolean isPaddingCorrect = false;
                if (fileSize > 50000) {
                    File pathnameParentDir = new File(MainApp.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
                    File tempSavedFile = File.createTempFile("tempSaved", null, pathnameParentDir);

                    try (FileOutputStream fos = new FileOutputStream(tempSavedFile)) {
                        int t;
                        for (int i = 0; i < fileSize; i++) {
                            t = sin.read();
                            fos.write(t);
                        }
                    }
                    isPaddingCorrect = checkPadding(tempSavedFile, mKey);
                    tempSavedFile.delete();
                } else {
                    byte[] tempFile = new byte[(int) fileSize];
                    sin.read(tempFile, 0, (int) fileSize);
                    isPaddingCorrect = checkPadding(tempFile, mKey);
                }

                if (isPaddingCorrect) {
                    out.writeInt(PADDING_OK_RESPONSE);
                } else {
                    out.writeInt(PADDING_ERROR_RESPONSE);
                }

                out.flush();
                out.close();
                in.close();
                sout.close();
                sin.close();
                mClientSocket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerSocketProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean checkPadding(File file, byte[] key) throws IOException {
        File pathnameParentDir = new File(MainApp.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile();
        File tempDecryptedFile = File.createTempFile("tempDecSaved", null, pathnameParentDir);

        Callable c = new AES_CBCPaddingCheckFile(file, tempDecryptedFile, key);
        FutureTask<Boolean> ftask = new FutureTask<>(c);
        Thread thread = new Thread(ftask);
        thread.start();

        boolean isPaddingCorrect = false;
        try {
            isPaddingCorrect = ftask.get();

        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(ServerSocketProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        tempDecryptedFile.delete();
        return isPaddingCorrect;
    }

    private boolean checkPadding(byte[] file, byte[] key) throws IOException {
        Callable c = new AES_CBCPaddingCheckBytes(file, key);
        FutureTask<Boolean> ftask = new FutureTask<>(c);
        Thread thread = new Thread(ftask);
        thread.start();

        boolean isPaddingCorrect = false;
        try {
            isPaddingCorrect = ftask.get();
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(ServerSocketProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return isPaddingCorrect;
    }

}
