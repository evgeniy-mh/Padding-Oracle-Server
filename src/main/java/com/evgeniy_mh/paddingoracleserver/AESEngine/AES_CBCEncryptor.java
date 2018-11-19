package com.evgeniy_mh.paddingoracleserver.AESEngine;

import com.evgeniy_mh.paddingoracleserver.CommonUtils;
import com.evgeniy_mh.paddingoracleserver.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

public class AES_CBCEncryptor extends Encryptor {

    public AES_CBCEncryptor(ProgressIndicator progressIndicator) {
        super(progressIndicator);
    }

    @Override
    public Task encrypt(File in, File out, byte[] key) {
        return new Task<Void>() {
            @Override
            protected Void call() throws IOException {
                Random random = new Random();
                byte[] IV = new byte[AES.BLOCK_SIZE];
                random.nextBytes(IV);

                byte[] tempKey = key;
                if (key.length % AES.BLOCK_SIZE != 0) {
                    tempKey = PKCS7.PKCS7(key);
                }
                mAES.makeKey(tempKey, 128, AES.DIR_BOTH);

                try {
                    RandomAccessFile OUTraf = new RandomAccessFile(out, "rw");
                    OUTraf.setLength(IV.length + in.length());
                    OUTraf.write(IV);

                    RandomAccessFile INraf = new RandomAccessFile(in, "r");

                    int nBlocks = CommonUtils.countBlocks(in, AES.BLOCK_SIZE); //сколько блоков открытого текста
                    byte[] temp = new byte[AES.BLOCK_SIZE]; //считываемый блок исходного текста
                    byte[] prev = new byte[AES.BLOCK_SIZE]; //предыдущий зашифрованный блок

                    for (int i = 0; i < nBlocks + 1; i++) {
                        INraf.seek(i * 16); //установка указателя для считывания файла

                        if ((i + 1) == nBlocks + 1) { //последняя итерация
                            int deltaToBlock = (int) (in.length() % AES.BLOCK_SIZE);
                            if (deltaToBlock > 0) {
                                temp = new byte[deltaToBlock];
                                INraf.read(temp, 0, deltaToBlock);  //считывание неполного блока в temp
                                temp = PKCS7.PKCS7(temp);
                            } else {
                                temp = new byte[AES.BLOCK_SIZE];
                                for (int t = 0; t < AES.BLOCK_SIZE; t++) {
                                    temp[t] = (byte) AES.BLOCK_SIZE;
                                }
                            }
                        } else {
                            INraf.read(temp, 0, AES.BLOCK_SIZE); //считывание блока в temp
                        }

                        byte[] k = new byte[AES.BLOCK_SIZE];
                        if (i == 0) { //первая итерация
                            for (int j = 0; j < AES.BLOCK_SIZE; j++) {
                                k[j] = (byte) (IV[j] ^ temp[j]);
                            }
                        } else { //последующие итерации
                            for (int j = 0; j < AES.BLOCK_SIZE; j++) {
                                k[j] = (byte) (prev[j] ^ temp[j]);
                            }
                        }
                        mAES.encrypt(k, prev);
                        OUTraf.write(prev);
                        progressIndicator.setProgress((double) i / nBlocks);
                    }

                    INraf.close();
                    OUTraf.close();
                } catch (IOException e) {
                    CommonUtils.reportExceptionToMainThread(e, "Exception in encrypt thread!");
                }
                progressIndicator.setProgress(0.0);
                return null;
            }
        };
    }

    @Override

    public Task decrypt(File in, File out, byte[] key) {
        return new Task<Void>() {
            @Override
            protected Void call() throws IOException {
                byte[] IV = FileUtils.readBytesFromFile(in, AES.BLOCK_SIZE);

                byte[] tempKey = key;
                if (key.length % AES.BLOCK_SIZE != 0) {
                    tempKey = PKCS7.PKCS7(key);
                }
                mAES.makeKey(tempKey, 128, AES.DIR_BOTH);

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
                            if (c[AES.BLOCK_SIZE - 1] > 0 && c[AES.BLOCK_SIZE - 1] <= 16) {
                                nToDeleteBytes = c[AES.BLOCK_SIZE - 1]; //на случай дешифрования с неправильным ключем
                            }
                        }
                        progressIndicator.setProgress((double) i / nBlocks);
                    }

                    OUTraf.setLength(OUTraf.length() - nToDeleteBytes);
                    OUTraf.close();
                    INraf.close();
                } catch (IOException e) {
                    CommonUtils.reportExceptionToMainThread(e, "Exception in decrypt thread!");
                }
                progressIndicator.setProgress(0.0);
                return null;
            }
        };

    }
}
