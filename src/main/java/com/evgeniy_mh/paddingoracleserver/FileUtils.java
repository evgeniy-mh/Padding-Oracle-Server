package com.evgeniy_mh.paddingoracleserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {

    public static void saveFile(File file, byte[] fileBytes) {
        if (file != null && fileBytes != null) {
            try {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(fileBytes);
                }
            } catch (IOException ex) {
                CommonUtils.reportExceptionToMainThread(ex, "Exception in saveFile");
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static byte[] readBytesFromFile(File file, int bytesToRead) {
        return readBytesFromFile(file, 0, bytesToRead);
    }

    /**
     * Считывание необходимого количества байт из файла
     *
     * @param f Файл для считывания
     * @param from Начальная позиция для считывания из файла(Номер байта)
     * @param to Конечная позиция для считывания из файла(Номер байта)
     * @return Массив байт которые были считаны из файла
     */
    public static byte[] readBytesFromFile(File f, int from, int to) {
        try {
            byte[] res;
            try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
                raf.seek(from);
                res = new byte[to - from];
                raf.read(res, 0, to - from);
            }
            return res;
        } catch (IOException ex) {
            CommonUtils.reportExceptionToMainThread(ex, "Exception in readBytesFromFile");
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * Выполняет сравнение двух файлов
     *
     * @param A Первый файл
     * @param B Второй файл
     * @return Результат сравнения
     */
    public static boolean compareFiles(File A, File B) {
        if (A != null && B != null) {
            if (A.length() == B.length()) {
                boolean result = true;
                try (FileInputStream finA = new FileInputStream(A); FileInputStream finB = new FileInputStream(B);) {

                    int iA = -1, iB = -1;
                    while ((iA = finA.read()) != -1 && (iB = finB.read()) != -1) {
                        if (iA != iB) {
                            result = false;
                            break;
                        }
                    }
                    return result;
                } catch (IOException ex) {
                    CommonUtils.reportExceptionToMainThread(ex, "compareFiles(File A, File B)");
                }
            }
        }
        return false;
    }

    public static void createFileCopy(File source, File destination) {
        try {
            FileChannel sourceChannel = new FileInputStream(source).getChannel();
            FileChannel destChannel = new FileOutputStream(destination).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            sourceChannel.close();
            destChannel.close();
        } catch (IOException ex) {
            CommonUtils.reportExceptionToMainThread(ex, "createFileCopy(File source, File distination)");
        }
    }
    
    /**
     *
     * @param source
     * @param destination
     * @param bytesCount сколько байт скопировать в файл out
     */
    public static void createFileCopy(File source, File destination, long bytesCount) {
        try {
            FileChannel sourceChannel = new FileInputStream(source).getChannel();
            FileChannel destChannel = new FileOutputStream(destination).getChannel();
            destChannel.transferFrom(sourceChannel, 0, bytesCount);
            sourceChannel.close();
            destChannel.close();
        } catch (IOException ex) {
            CommonUtils.reportExceptionToMainThread(ex, "createFileCopy(File source, File destination, long bytesCount)");
        }
    }
}
