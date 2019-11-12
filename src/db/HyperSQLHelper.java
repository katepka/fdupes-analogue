package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Helps to work with data bases:
 * 1. Loads driver to hsqldb.
 * 2. Gets connection to db.
 * 3. Creates a new table with known fields.
 * 4. Inserts data into the table.
 * 5. Prints the content of the table to a terminal.
 * 6. Closes the connection to a data base.
 */
public class HyperSQLHelper {

    private Connection connection = null;
    
    /**
     * Loads hsqldb driver.
     * @return true if driver loaded successfully.
     */
    public boolean loadDriver() {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver is not found");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /**
     * Gets connection to db using credentials from src\resurces\config.properties file.
     * @return true if connection created successfully.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public boolean getConnection() throws FileNotFoundException, IOException {
        Properties properties = new Properties();

        try {
            FileInputStream fis = new FileInputStream("src\\resources\\config.properties");
            properties.load(fis);
            String path = properties.getProperty("db.path");
            String dbname = properties.getProperty("db.dbname");
            String connectionString = properties.getProperty("db.connection") + path + dbname;
            String login = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");
            connection = DriverManager.getConnection(connectionString, login, password);

        } catch (SQLException e) {
            System.out.println("Connection not created");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /**
     * Creates a new table FDUPESTABLE with fields:
     * id IDENTITY
     * filename VARCHAR(255)
     * filesize BIGINT
     * lastmodified VARCHAR(20)
     * pathto VARCHAR(260)
     */
    public void createTable() {
        try {
            Statement statement = connection.createStatement();
            String sql = "CREATE TABLE FDUPESTABLE (id IDENTITY,"
                    + "filename VARCHAR(255), filesize BIGINT, "
                    + "lastmodified VARCHAR(20), pathto VARCHAR(260))";
            statement.executeUpdate(sql);
        } catch (SQLException e) {

        }
    }
    
    /**
     * Inserts data into the table FDUPESTABLE.
     * @param dupes list of file duplicates.
     */
    public void insertIntoTable(ArrayList<File> dupes) {
        Statement statement;
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss dd.MM.yyyy");
        try {
            statement = connection.createStatement();
            for (File dup : dupes) {
                String filename = dup.getName();
                Long filesize = dup.length();
                String lastmodified = format.format(dup.lastModified());
                String pathto = dup.getParent();
                String sql = "INSERT INTO FDUPESTABLE (filename, filesize, lastmodified, pathto) "
                    + String.format("VALUES('%s', %d, '%s', '%s')", 
                            filename, filesize, lastmodified, pathto);
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * SELECTs rows and prints the content of the table FDUPESTABLE
     * to a terminal row by row.
     */
    public void printTable() {
        Statement statement;
        try {
            statement = connection.createStatement();
            String sql = "SELECT id, filename, filesize, lastmodified, pathto FROM FDUPESTABLE";
            ResultSet resultSet = statement.executeQuery(sql);
            
            System.out.printf("\n%-5s| %-50s| %-10s| %-20s| %-260s|\n", 
                    "id", "filename", "filesize", "lastmodified", "pathto");
            System.out.println("-".repeat(355));
            
            while (resultSet.next()) {
                System.out.printf("%-5s| %-50s| %-10s| %-20s| %-260s%n",
                        resultSet.getInt(1), 
                        resultSet.getString(2), 
                        resultSet.getString(3),
                        resultSet.getString(4),
                        resultSet.getString(5));
                System.out.println("-".repeat(355));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Closes the connection to a data base.
     */
    public void closeConnection() {

        Statement statement;
        try {
            statement = connection.createStatement();
            String sql = "SHUTDOWN";
            statement.execute(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
