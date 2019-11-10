package utils;

import java.io.File;
import java.util.Comparator;

public class FileSizeNameComparator implements Comparator<File> {    
   
    /**
     * 
     * @param file1
     * @param file2
     * @return -1 - если o1 меньше o2;
     *          0 - если o1 = o2;
     *          1 - если o1 больше o2;
     * сравнение по длине и по имени
     */
    @Override
    public int compare(File file1, File file2) {
        if (file1.length() != file2.length()) {
            return Long.compare(file1.length(), file2.length());
        } else if (file1.getName().equals(file2.getName())) {
            return 0;
        }
        return file1.getName().compareTo(file2.getName());
    }
    
}
