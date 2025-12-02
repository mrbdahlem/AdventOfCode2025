import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Aoc {
    public static void main(String[] args) throws FileNotFoundException {
        // Load the data
        File inputFile = new File("dayX.txt");
        Scanner scan = new Scanner(inputFile);
       

        scan.close();

        // Check for invalid ids
        long start = System.nanoTime();
        partOne();
        long first = System.nanoTime();
        partTwo();
        long sec = System.nanoTime();

        System.out.println("Part 1 duration: " + (first - start) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
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
