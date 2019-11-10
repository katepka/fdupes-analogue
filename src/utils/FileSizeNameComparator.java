package utils;

import java.io.File;
import java.util.Comparator;

public class FileSizeNameComparator implements Comparator<File> {    
   
    /**
     * 
     * @param file1
     * @param file2
     * @return -1 - if o2 less than o1;
     *          0 - if o1 = o2;
     *          1 - if o2 greater than o1;
     * comraring by files lengths and names
     */
    @Override
    public int compare(File file1, File file2) {
        if (file1.length() != file2.length()) {
            return Long.compare(file2.length(), file1.length());
        } else if (file1.getName().equals(file2.getName())) {
            return 0;
        }
        return file1.getName().compareTo(file2.getName());
    }
    
}
