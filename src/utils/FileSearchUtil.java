package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * The class is used for work with files:
 * 1. Recursively goes around the directory and all nested directories
 * and saves all files to the ArrayList LIST_OF_FILES.
 * 2. Searches for files with the same length and saves them to a new ArrayList.
 * 3. Searches for files-duplicates with the same check sum among 
 * the "suspicious" files and saves them to a new ArrayList.
 * 4. Generate hashcode by file content using the md5 algorithm.
 * 5. Calculate a memory size which can be exempted if you delete all duplicates.
 */
public class FileSearchUtil {

    /**
     * A list of all files in the directory.
     */
    private static final ArrayList<File> LIST_OF_FILES = new ArrayList<File>();

    /**
     * Recursively goes around the directory and all nested directories
     * and saves all files to the ArrayList LIST_OF_FILES.
     *
     * @param filePath - a path to the directory.
     * @return LIST_OF_FILES - a list of all files in the directory filePath.
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
                // Exception handling
            }
        }
        return LIST_OF_FILES;
    }

    /**
     * Searches for files with the same length and saves them to a new ArrayList.
     *
     * @param files - a list of files.
     * @return dupes - a list of all files with the same length, "suspicious" files.
     * @quantityOfFiles - HashMap (file length : quantity).
     */
    public static ArrayList<File> getDupes(ArrayList<File> files) throws IOException {
        ArrayList<File> dupes = new ArrayList<>();
        HashMap<Long, Integer> quantityOfFiles = new HashMap<>();

        // Count quantity of files with the length:
        for (File file : files) {
            if (!quantityOfFiles.containsKey(file.length())) {
                quantityOfFiles.put(file.length(), 1);
            } else if (quantityOfFiles.containsKey(file.length())) {
                quantityOfFiles.put(file.length(), quantityOfFiles.get(file.length()) + 1);
            }

        }

        // Save to dupes only files whose length occurs more than once in the files:
        for (File file : files) {
            if ((quantityOfFiles.get(file.length()) != null)
                    && (quantityOfFiles.get(file.length()) > 1)) {
                dupes.add(file);
            }
        }
        
        // The dupes passes to the getDeepDupes method:
        return getDeepDupes(dupes);
    }

    /**
     * Searches for files-duplicates with the same check sum among 
     * the "suspicious" files and saves them to a new ArrayList.
     *
     * @param dupes - the list of "suspicious" files.
     * @return deepDupes - a list of files-duplicates.
     * @quantityOfDupes - HashMap (check sum : quantity).
     */
    public static ArrayList<File> getDeepDupes(ArrayList<File> dupes) throws IOException {
        ArrayList<File> deepDupes = new ArrayList<>();
        HashMap<String, Integer> quantityOfDupes = new HashMap<>();

        // Count quantity of files with the check sum:
        for (File dup : dupes) {
            String content = getHashMD5(dup);

            if (!quantityOfDupes.containsKey(content)) {
                quantityOfDupes.put(content, 1);

            } else if (quantityOfDupes.containsKey(content)) {
                quantityOfDupes.put(content, quantityOfDupes.get(content) + 1);
            }

        }

        // Save to deepDupes only files whose check sum occurs more
        // than once in the dupes:
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
     * Generate hashcode by file content using the md5 algorithm.
     *
     * @param file
     * @return String md5 - hex check sum of the file.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String getHashMD5(File file) throws FileNotFoundException, IOException {
        String md5 = DigestUtils.md5Hex(new FileInputStream(file));
        return md5;
    }

    /**
     * Calculate a memory size which can be exempted if you delete all duplicates.
     *
     * @param dupes list of file-duplicates
     * @return exemptedMemory - memory (in bytes) which can be exempted if
     * duplicates were deleted.
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
