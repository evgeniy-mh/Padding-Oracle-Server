package com.evgeniy_mh.paddingoracleserver.AESEngine;

import java.io.File;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

public abstract class Encryptor {
    protected final AES mAES;
    protected final ProgressIndicator progressIndicator;

    public Encryptor(ProgressIndicator progressIndicator) {
        mAES = new AES();
        this.progressIndicator = progressIndicator;
    }
    
    /**
     * Выполняет шифрование файла
     *
     * @param in Файл открытого текста
     * @param out Файл для сохранения результата шифрования (будет перезаписан)
     * @param key Ключ шифрования
     */
     abstract Task encrypt(File in, File out, final byte[] key);
     
     /**
     * Выполняет дешифрование файла
     *
     * @param in Файл шифрованного текста
     * @param out Файл для сохранения результата расшифрования (будет
     * перезаписан)
     * @param key Ключ шифрования
     */
    abstract Task decrypt(File in, File out, final byte[] key);
}
