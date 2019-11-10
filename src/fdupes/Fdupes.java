package fdupes;

import utils.FileSizeNameComparator;
import utils.FileSearchUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import db.HyperSQLHelper;

public class Fdupes {

    public static void main(String[] args) throws IOException {

        // Start:
        Long startTime = System.nanoTime();
        File filePath = new File(args[0]);
        // Formate list of files in the directory:
        ArrayList<File> files = FileSearchUtil.getListOfFiles(filePath);
        // Stop of formation of lists of files in the path:
        Long stopListingFiles = System.nanoTime();
        System.out.printf("Found files in the directory %s: %d.\nTime spent, ns: %d\n\n",
                args[0], files.size(), stopListingFiles - startTime);

        // Create file df.txt to log duplicates:
        File df = new File(args[0] + "\\df.txt");
        df.createNewFile();

        // Search duplicates:
        ArrayList<File> dupes = FileSearchUtil.getDupes(files);
        // Sort duplicates by file length and name:
        FileSizeNameComparator comparator = new FileSizeNameComparator();
        dupes.sort(comparator);

        // Stop of formation of lists of duplicates:
        Long stopListingDupes = System.nanoTime();
        System.out.printf("Found duplicates: %d.\nTime spent, ns: %d\n\n",
                dupes.size(), stopListingDupes - stopListingFiles);

        // Write sorted duplicates to the df.txt:
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss dd.MM.yyyy");
        
        try (FileWriter writer = new FileWriter(df)) {
            for (File dup : dupes) {
                writer.write(dup.getName() + " | " + dup.length() + " | "
                        + format.format(dup.lastModified()) + " | " + dup.getParent()
                        + " | " + "\r\n");
            }
            writer.close();
        }

        // Stop of file writing:
        Long stopWritingDupes = System.nanoTime();
        System.out.printf("Write to file is over. Time spent, ns: %d",
                (stopWritingDupes - stopListingDupes));
        
        // Create db-connection and a new table:
        HyperSQLHelper hsqlHelper = new HyperSQLHelper();
        if (!hsqlHelper.loadDriver()) return;
        if (!hsqlHelper.getConnection()) return;
        
        hsqlHelper.createTable();
        hsqlHelper.insertIntoTable(dupes);
        // Print all rows of the table to a terminal:
        //hsqlHelper.printTable();
        hsqlHelper.closeConnection();
        
        // Stop of db writing:
        Long stopWritingDB = System.nanoTime();
        System.out.printf("Write to data base is over. Time spent, ns: %d"
                + "\nTotal time, ns: %d (%d seconds)."
                + "\n\nYou can exempte %d kb deleting duplicates.\n\n",
                (stopWritingDB - stopListingDupes), (stopWritingDB - startTime),
                (stopWritingDB - startTime) / 1_000_000_000,
                FileSearchUtil.calculateExemptedMemory(dupes) / 1024);
    }
    
  
}