package com.evgeniy_mh.paddingoracleserver.AESEngine;

import com.evgeniy_mh.paddingoracleserver.CommonUtils;
import com.evgeniy_mh.paddingoracleserver.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AES_CBCPaddingCheckFile implements Callable<Boolean> {

    private final AES mAES;
    private final File in;
    private final File out;
    private final byte[] key;

    public AES_CBCPaddingCheckFile(File in, File out, byte[] key) {
        mAES = new AES();
        this.in = in;
        this.out = out;
        this.key = key;
    }

    @Override
    public Boolean call() throws Exception {
        byte[] IV = FileUtils.readBytesFromFile(in, AES.BLOCK_SIZE);

        byte[] tempKey = key;
        if (key.length % AES.BLOCK_SIZE != 0) {
            tempKey = PKCS7.PKCS7(key);
        }
        mAES.makeKey(tempKey, 128, AES.DIR_BOTH);
        boolean error = false;
        try {
            RandomAccessFile OUTraf = new RandomAccessFile(out, "rw");
            OUTraf.setLength(in.length() - IV.length);
            RandomAccessFile INraf = new RandomAccessFile(in, "r");

            int nBlocks = CommonUtils.countBlocks(in, AES.BLOCK_SIZE); //сколько блоков шифро текста
            int nToDeleteBytes = 0; //сколько байт нужно удалить с конца сообщения

            byte[] temp = new byte[AES.BLOCK_SIZE];
            byte[] prev = new byte[AES.BLOCK_SIZE];
            for (int i = 1; i < nBlocks; i++) {
                INraf.seek(i * 16); //установка указателя для считывания файла
                INraf.read(temp, 0, AES.BLOCK_SIZE); //считывание блока в temp

                byte[] k = new byte[AES.BLOCK_SIZE]; // k_i
                byte[] c = new byte[AES.BLOCK_SIZE]; //c_i

                mAES.decrypt(temp, k);

                if (i == 1) { //первая итерация
                    for (int j = 0; j < AES.BLOCK_SIZE; j++) {
                        c[j] = (byte) (IV[j] ^ k[j]);
                    }
                    System.arraycopy(temp, 0, prev, 0, AES.BLOCK_SIZE);
                } else {
                    for (int j = 0; j < AES.BLOCK_SIZE; j++) {
                        c[j] = (byte) (prev[j] ^ k[j]);
                    }
                }
                System.arraycopy(temp, 0, prev, 0, AES.BLOCK_SIZE);
                OUTraf.write(c);

                if ((i + 1) == nBlocks) {
                    //проверка дополнения
                    byte paddingCount = c[AES.BLOCK_SIZE - 1];

                    if (paddingCount > 0 && paddingCount <= 16) {
                        for (int p = 0; p < paddingCount; p++) {
                            if (c[AES.BLOCK_SIZE - 1 - p] != paddingCount) {
                                error = true;
                                break;
                            }
                        }
                    } else {
                        error = true;
                    }
                    if (!error) {
                        nToDeleteBytes = c[AES.BLOCK_SIZE - 1];
                    }
                }
            }
            OUTraf.setLength(OUTraf.length() - nToDeleteBytes);
            OUTraf.close();
            INraf.close();
        } catch (IOException e) {
            Logger.getLogger(AES_CBCPaddingCheckFile.class.getName()).log(Level.SEVERE, null, e);
            CommonUtils.reportExceptionToMainThread(e, "Exception in decrypt thread!");
        }
        return !error;
    }
}
