import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Aoc6 {
    public static void main(String[] args) throws FileNotFoundException {
        // Load the data
        File inputFile = new File("day6.txt");
        Scanner scan = new Scanner(inputFile);
       
        // Load the entire data as one token
        scan.useDelimiter("\\A");
        String data = scan.next();
        String[] lines = data.split("\\R");
        
        scan.close();

        // Solve the two parts of the puzzle
        long start = System.nanoTime();
        partOne(lines);
        long first = System.nanoTime();
        partTwo(lines);
        long sec = System.nanoTime();

        System.out.println("Part 1 duration: " + (first - start) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * Solve each math problem and sum the results  
     * @param lines puzzle input broken into horizontal lines
     */
    public static void partOne(String[] lines) {
        long[][] grid = new long[lines.length - 1][];

        for (int i = 0; i < lines.length - 1; i++) {
            String line = lines[i];
            String[] nums = line.split("\\s+");
            grid[i] = new long[nums.length];

            // Handle possible leading space
            int k = 0;
            if (nums[0].isEmpty()) {
                k = 1;
            }

            for (int j = 0; k < nums.length; j++, k++) {
                grid[i][j] = Long.parseLong(nums[k]);
            }
        }

        String[] ops = lines[lines.length - 1].split("\\s+");

        if (ops.length != grid[0].length) {
            System.err.println("Error: number of operations does not match number of columns");
            System.exit(1);
        }

        long total = 0;

        for (int i = 0; i < grid[0].length; i++) {
            String op = ops[i];

            long col = grid[0][i];
            for (int j = 1; j < grid.length; j++) {
                long val = grid[j][i];

                switch (op) {
                    case "*":
                        col *= val;
                        break;
                    case "+":
                        col += val;
                        break;
                }
            }
            total += col;
        }
        System.out.println("Part 1: {" + total + "}");
    }

    /**
     * Solve each math problem (the cephalopod way with numbers written vertically)
     * then sum the results
     * @param lines puzzle input broken into horizontal lines
     */
    public static void partTwo(String[] lines) {
        long total = 0;

        String num = "";
        char op = ' ';
        long col = 0;
        for (int i = 0; i < lines[0].length(); i++){

            for (int j = 0; j < lines.length - 1; j++){
                if (lines[j].charAt(i) == ' '){
                    continue;
                }
                num += lines[j].charAt(i);
            }

            if (lines[lines.length - 1].charAt(i) != ' '){
                System.out.println(" " + num);
                total += col;
                op = lines[lines.length - 1].charAt(i);
                col = Long.parseLong(num);
                num = "";
            }
            else {
                if (num.isEmpty()){
                    System.out.println("=" + col);
                    continue;
                }
                long val = Long.parseLong(num);
                if (op == '*') {
                    col *= val;
                } else {
                    col += val;
                }
                System.out.println(op + num);
                num = "";
            }

        }

        System.out.println("=" + col);
        total += col;

        System.out.println("Part 2: {" + total + "}");
    }
}
