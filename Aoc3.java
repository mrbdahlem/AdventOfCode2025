import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Aoc3 {
    public static void main(String[] args) throws FileNotFoundException {
        // Load the data
        File inputFile = new File("day3.txt");
        Scanner scan = new Scanner(inputFile);
        
        // Read all lines into a 2D array of integers representing the batteries in the banks
        String[] lines = scan.useDelimiter("\\Z").next().split("\n");
        int[][] batteries = new int[lines.length][];
        for (int i = 0; i < lines.length; i++) {
            batteries[i] = lines[i].chars().map(c -> c - '0').toArray();
        }

        scan.close();

        // Find the largest joltage numbers
        long start = System.nanoTime();
        partOne(batteries);
        long first = System.nanoTime();
        partTwo(batteries);
        long sec = System.nanoTime();

        System.out.println("Part 1 duration: " + (first - start) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * Find the largest joltage number by choosing 2 batteries from each bank and
     * concatenating them.
     * @param banks 2D array of batteries, each row is a bank
     */
    public static void partOne(int[][] banks) {
        long total = 0;

        for (int[] bank : banks) {
            long bankLargest = largestN(2, bank, 0, 2, new HashMap<IndexCount, Long>());
            total += bankLargest;
        }

        System.out.println("Part 1: {" + total + "}");
    }

    /**
     * Find the largest joltage number by choosing 12 batteries from each bank and
     * concatenating them.
     * @param banks 2D array of batteries, each row is a bank
     */
    public static void partTwo(int[][] banks) {
        long total = 0;

        for (int[] bank : banks) {
            long bankLargest = largestN(12, bank, 0, 12, new HashMap<IndexCount, Long>());
            total += bankLargest;
        }

        System.out.println("Part 2: {" + total + "}");
    }

    /**
     * Find the largest number that can be formed by choosing 'count' digits from
     * the 'bank' array starting from 'index'.
     * 
     * @param n number of digits to choose
     * @param bank array of digits
     * @param index current index in the bank
     * @param count number of digits left to choose
     * @param memo memoization map to store previously computed results
     * @return largest number that can be formed
     */
    private static long largestN(int n, int[] bank, int index, int count, Map<IndexCount, Long> memo) {
        if ((bank.length - index) < count || index >= bank.length || count < 1) {
            return 0;
        }

        if (memo.containsKey(new IndexCount(index, count))) {
            return memo.get(new IndexCount(index, count));
        }

        long take = bank[index];
        for (int i = 0; i < count - 1; i++) {
            take *= 10;
        }
        take += largestN(n, bank, index + 1, count - 1, memo);
        long skip = largestN(n, bank, index + 1, count, memo);

        long largest = Math.max(take, skip);
        memo.put(new IndexCount(index, count), largest);
        return largest;
    }
}

/**
 * Record to hold index and count for memoization
 */
record IndexCount (int index, int count){}