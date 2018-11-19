package com.evgeniy_mh.paddingoracleserver;

import java.io.File;
import javafx.application.Platform;

public class CommonUtils {  
    
    /**
     * Подсчет количества целых блоков
     * @param f Файл с данными
     * @param blockSize Размер блока
     * @return Количество блоков
     */
    public static int countBlocks(File f, int blockSize) { 
        return (int) (f.length() / blockSize);
    }
    
    /**
     * Выполняет конкатенацию двух массивов байт
     * @return конкатенация массивов a и b
     */
    public static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);

        return result;
    }    
    
    /**
     * Вывод в консоль массива байт
     *
     * @param mes Сообщение для вывода
     * @param array Массив байт содержимое которого нужно вывести
     */
    static public void debugPrintByteArray(String mes, byte[] array) {
        System.out.println(mes);
        for (int i = 0; i < array.length; i++) {
            System.out.print(String.format("0x%08X", array[i]) + " ");
        }
        System.out.println();
    }
    
    /**
     * Отправка сообщения о исключении в Application Thread
     * @param t
     * @param message Дополнительное сообщение для пользователя
     */
    public static void reportExceptionToMainThread(final Throwable t, final String message) {
        Platform.runLater(() -> {
            FXMLController.showExceptionToUser(t,message);
        });
    }
}
