import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Aoc9 {
    public static void main(String[] args) throws FileNotFoundException {
        // Load the data
        File inputFile = new File("sample9.txt");
        Scanner scan = new Scanner(inputFile);
       
        // Load the entire data as one token
        scan.useDelimiter("\\A");
        String data = scan.next();

        scan.close();

        // Solve the two parts of the puzzle
        long start = System.nanoTime();
        partOne();
        long first = System.nanoTime();
        partTwo();
        long sec = System.nanoTime();

        System.out.println("Part 1 duration: " + (first - start) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * @param data raw input data 
     */
    public static void prepare(String data) {

    }

    /**
     * 
     */
    public static void partOne() {
        long total = 0;

        System.out.println("Part 1: {" + total + "}");
    }

    /**
     * 
     */
    public static void partTwo() {
        long total = 0;

        System.out.println("Part 2: {" + total + "}");
    }
}
