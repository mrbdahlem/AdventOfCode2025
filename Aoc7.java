import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Aoc7 {
    public static void main(String[] args) throws FileNotFoundException {
        // Load the data
        File inputFile = new File("day7.txt");
        Scanner scan = new Scanner(inputFile);
       
        // Load the entire data as one token
        scan.useDelimiter("\\A");
        String data = scan.next();
        String[] lines = data.split("\\R");
        String[][] grid = new String[lines.length - 1][];
        for (int i = 0; i < lines.length - 1; i++) {
            grid[i] = lines[i].split("");
        }

        scan.close();

        // Determine the number of splits/timelines created
        long start = System.nanoTime();
        partOne(grid);
        long first = System.nanoTime();
        partTwo(grid);
        long sec = System.nanoTime();

        System.out.println("Part 1 duration: " + (first - start) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * Calculate the number of times the tachyon beam splits
     * @param grid The grid of splitters
     */
    public static void partOne(String[][] grid) {
        long splits = 0;

        // Find the starting point
        int start = -1;
        for (int i = 0; i < grid[0].length; i++) {
            if (grid[0][i].equals("S")) {
                start = i;
            }
        }

        // Ensure that a starting point was found
        if (start == -1) {
            System.err.println("Error: no starting point found");
            System.exit(1);
        }

        // Simulate the beam through the grid
        Set<Integer> beams = new HashSet<>(); // beams in the same column combine

        beams.add(start); // initial beam

        // Process the beam row by row from top to bottom
        for (int row = 1; row < grid.length; row++) {
            // Process each beam in the current row
            Set<Integer> newBeams = new HashSet<>();
            for (int beam : beams) {
                // Check if the beam hits a splitter
                if (grid[row][beam].equals("^")) {
                    // Beam splits into two new beams
                    newBeams.add(beam - 1);
                    newBeams.add(beam+ 1);
                    splits++;
                }
                else {
                    // Beam continues straight down
                    newBeams.add(beam);
                }
            }

            // Update the beams set for the next row
            beams = newBeams;
        } 

        System.out.println("Part 1: {" + splits + "}");
    }

    /**
     * Calculate the number of distinct timelines created by the tachyon beam
     * @param grid The grid of splitters
     */
    public static void partTwo(String[][] grid) {
        long total;

        // Find the starting point
        int start = -1;
        for (int i = 0; i < grid[0].length; i++) {
            if (grid[0][i].equals("S")) {
                start = i;
            }
        }

        // Ensure that a starting point was found
        if (start == -1) {
            System.err.println("Error: no starting point found");
            System.exit(1);
        }

        // Shoot the beam recursively and count distinct timelines created
        total = shootBeam(grid, new Point(1, start), new HashMap<>());

        System.out.println("Part 2: {" + total + "}");
    }

    /**
     * Recursively shoot the beam through the grid and count the number of distinct timelines
     * created
     * @param grid The grid of splitters
     * @param p The current point of the beam
     * @param memo Memoization map to store previously calculated results
     * @return The number of distinct timelines created by the beam from point p
     */
    private static long shootBeam(String[][] grid, Point p, Map<Point, Long> memo) {
        // Base case: if the beam has reached the bottom of the grid
        if (p.row() >= grid.length) {
            return 1;
        }

        // Check if the result is already memoized
        if (memo.containsKey(p)) {
            return memo.get(p);
        }

        // If the beam is out of bounds, return 0
        if (p.col() < 0 || p.col() >= grid[0].length) {
            return 0;
        }

        // Recursive case: shoot the beam downwards
        long count = 0;
        if (grid[p.row()][p.col()].equals("^")) {
            // If the current cell is a splitter, split the timeline so a beam goes 
            // BOTH left and right
            count += shootBeam(grid, new Point(p.row() + 1, p.col() - 1), memo);
            count += shootBeam(grid, new Point(p.row() + 1, p.col() + 1), memo);
        }
        else {
            // Otherwise, just shoot the beam straight down
            count += shootBeam(grid, new Point(p.row() + 1, p.col()), memo);

        }

        // Memoize the result before returning
        memo.put(p, count);
        return count;
    }
}

/**
 * A simple record to represent a point in the grid
 */
record Point(int row, int col) {};