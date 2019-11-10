package fdupes;

import utils.FileSizeNameComparator;
import utils.FileSearchUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Ищет в директории, подаваемой в args, в течение заданного времени (300 m),
 * дублирующиеся файлы (dir, filename, size, date_modificate), сортирует их по длине 
 * и записывает в файл df.txt. Сначала находит файлы с одинаковыми именами
 * и одного размера, затем сравнивает их по вычисленному хэшу или побайтно.
 *
 * Использовать рекурсивный проход каталогов.
 * 
 * TODO: Посчитать место, которое можно освободить при удалении дубликатов.
 * TODO: Подключить библиотеку hsqlbd.jar. Содать bd и записывать в нее дубликаты.
 * 
 */
public class Fdupes {

    public static void main(String[] args) throws IOException {

        // Начало работы программы
        Long startTime = System.nanoTime();
        // из аргумента командной строки получаем путь к заданной директории
        File filePath = new File(args[0]); //"C:\\Users\\Kate\\java_learning\\projects\\fdupes\\src\\TestDir\\new"
        
        // Получаем список всех файлов в директории и поддиректориях
        ArrayList<File> files = FileSearchUtil.getListOfFiles(filePath);

        // Окончание формирования списка файлов
        Long stopListingFiles = System.nanoTime();
        System.out.printf("Найдено файлов в директории %s: %d.\nЗатрачено времени, нс: %d\n\n",
                args[0], files.size(), stopListingFiles - startTime);

        // Создаем файл логирования дубликатов df.txt в переданной директории
        File df = new File(args[0] + "\\df.txt");
        df.createNewFile();

        // Ищем файлы-дубликаты:
        ArrayList<File> dupes = FileSearchUtil.getDupes(files);
        // Сортируем найденные дубликаты по длине и по имени
        FileSizeNameComparator comparator = new FileSizeNameComparator();
        dupes.sort(comparator);

        // Окончание формирования списка дубликатов
        Long stopListingDupes = System.nanoTime();
        System.out.printf("Найдено дубликатов: %d.\nЗатрачено времени, нс: %d\n\n",
                dupes.size(), stopListingDupes - stopListingFiles);

        // Записываем в файл df.txt отсортированные дубликаты
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss dd.MM.yyyy");
        
        try (FileWriter writer = new FileWriter(df)) {
            for (File dup : dupes) {
                writer.write(dup.getName() + " | " + dup.length() + " | "
                        + format.format(dup.lastModified()) + " | " + dup.getParent()
                        + " | " + "\r\n");
            }
            writer.close();
        }

        // Окончание записи данных по дубликатам в файл
        Long stopWritingDupes = System.nanoTime();
        System.out.printf("Запись в файл окончена. Затрачено времени, нс: %d"
                + "\nВсего затрачено времени, нс: %d"
                + "\nВы можете освободить %d кб, удалив дубликаты.\n",
                (stopWritingDupes - stopListingDupes), (stopWritingDupes - startTime),
                FileSearchUtil.calculateExemptedMemory(dupes) / 1024);

    }
    
  
}