package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;

public class FileSearchUtil {

    /**
     * поле под список всех файлов в директории
     */
    private static final ArrayList<File> LIST_OF_FILES = new ArrayList<File>();

    /**
     * Рекурсивно проходит по всем директориям внутри filePath, записывает
     * найденные файлы в новый массив listOfFiles.
     *
     * @param filePath - путь к заданной директории.
     * @return listOfFiles - список файлов в filePath.
     */
    public static ArrayList<File> getListOfFiles(File filePath) {

        File[] listOfDirs = filePath.listFiles();

        for (File dir : listOfDirs) {

            try {
                if (dir.isDirectory()) {
                    getListOfFiles(dir);
                } else if (dir.isFile()) {
                    LIST_OF_FILES.add(dir);
                }
            } catch (NullPointerException e) {
                // Обработка исключения
            }
        }
        return LIST_OF_FILES;
    }

    /**
     * Ищет подозрения на дубликаты (по размеру) в списке файлов и записывает их
     * в новый массив.
     *
     * @param files - список ссылок на файлы в директории.
     * @return dupes - список ссылок на подозрения на дубликаты.
     * @quantityOfFiles - HashMap вида (имяРазмерФайла : количество найденных)
     */
    public static ArrayList<File> getDupes(ArrayList<File> files) throws IOException {
        ArrayList<File> dupes = new ArrayList<>();
        HashMap<Long, Integer> quantityOfFiles = new HashMap<>();

        // Считаем количество файлов одного размера
        for (File file : files) {
            //String nameSize = file.getName() + file.length();
            if (!quantityOfFiles.containsKey(file.length())) {
                quantityOfFiles.put(file.length(), 1);
            } else if (quantityOfFiles.containsKey(file.length())) {
                quantityOfFiles.put(file.length(), quantityOfFiles.get(file.length()) + 1);
            }

        }

        // Проходим по всем найденным файлам и сохраняем в dupes только те,
        // что встречаются больше 1 раза
        for (File file : files) {
            //String nameSize = file.getNa9me() + file.length();
            if ((quantityOfFiles.get(file.length()) != null)
                    && (quantityOfFiles.get(file.length()) > 1)) {
                dupes.add(file);
            }
        }

        return getDeepDupes(dupes);
    }

    /**
     * Ищет дубликаты (с одинаковой контрольной суммой файла) в подозрительных
     * файлах (одинаковых по размеру) и записывает их в новый массив.
     *
     * @param dupes - список ссылок на файлы-подозрения на дубликаты.
     * @return deepDupes - список ссылок на найденные дубликаты.
     * @quantityOfDupes - HashMap вида (контрольная сумма файла : количество
     * найденных)
     */
    public static ArrayList<File> getDeepDupes(ArrayList<File> dupes) throws IOException {
        ArrayList<File> deepDupes = new ArrayList<>();
        HashMap<String, Integer> quantityOfDupes = new HashMap<>();

        // Считаем количество файлов с каждой встречающейся контрольной суммой
        for (File dup : dupes) {
            String content = getHashMD5(dup);

            if (!quantityOfDupes.containsKey(content)) {
                quantityOfDupes.put(content, 1);

            } else if (quantityOfDupes.containsKey(content)) {
                quantityOfDupes.put(content, quantityOfDupes.get(content) + 1);
            }

        }

        // Проходим по всем найденным файлам и сохраняем в deepDupes только те,
        // что встречаются больше 1 раза
        for (File dup : dupes) {
            String content = getHashMD5(dup);

            if ((quantityOfDupes.get(content) != null)
                    && (quantityOfDupes.get(content) > 1)) {
                deepDupes.add(dup);
            }
        }
        return deepDupes;
    }

    /**
     * Вычисляет hash-код по содержимому файла
     *
     * @param file
     * @return String md5 - hex-checked sum of the file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String getHashMD5(File file) throws FileNotFoundException, IOException {
        String md5 = DigestUtils.md5Hex(new FileInputStream(file));
        return md5;
    }

    /**
     * Считает размер памяти, который можно освободить, удалив все дубликаты
     *
     * @param dupes list of file-duplicates
     * @return exemptedMemory - memory (in bytes) which can be exempted if
     * duplicates were deleted
     */
    public static long calculateExemptedMemory(ArrayList<File> dupes) throws IOException {
        long exemptedMemory = 0;
        for (File dup : dupes) {
            exemptedMemory += dup.length();
        }

        Map<String, Long> originals = new HashMap<>();
        for (File dup : dupes) {
            originals.put(FileSearchUtil.getHashMD5(dup), dup.length());
        }

        for (Long originalLength : originals.values()) {
            exemptedMemory -= originalLength;

        }
        return exemptedMemory;
    }
}
