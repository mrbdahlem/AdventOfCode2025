import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Aoc4 {
    public static void main(String[] args) throws FileNotFoundException {
        // Load the data
        File inputFile = new File("day4.txt");
        Scanner scan = new Scanner(inputFile);

        // Load the entire data as one token
        scan.useDelimiter("\\A");

        String[] lines = scan.next().split("\n");
        String[][] map = new String[lines.length][];
        for (int i = 0; i < lines.length; i++) {
            map[i] = lines[i].split("");
        }

        scan.close();

        // Solve the aoc parts
        long start = System.nanoTime();
        partOne(map);
        long first = System.nanoTime();
        partTwo(map);
        long sec = System.nanoTime();

        System.out.println("Part 1 duration: " + (first - start) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * Find out how many paper rolls can be removed from the warehouse using a forklift
     * that can only access rolls with less than 4 neighboring rolls.
     * 
     * @param map The map of paper rolls in the warehouse
     */
    public static void partOne(String[][] map) {
        long total = 0;

        // Check each roll in the warehouse
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                // Skip empty spaces
                if (!map[row][col].equals("@")) {
                    continue;
                }

                int neighbors = 0;

                // Check for neighboring rolls
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) {
                            continue;
                        }
                        int newRow = row + dy;
                        int newCol = col + dx;
                        if (newRow >= 0 && newRow < map.length &&
                            newCol >= 0 && newCol < map[row].length) {
                            if (map[newRow][newCol].equals("@")) {
                                neighbors++;
                            }
                        }
                    }
                } 
                
                // If less than 4 neighbors, it can be removed
                if (neighbors < 4) {
                    total++;
                }
            }
        }

        System.out.println("Part 1: {" + total + "}");
    }

    /**
     * Remove all possible paper rolls from the warehouse using a forklift
     * 
     * @param map The map of paper rolls in the warehouse
     */
    public static void partTwo(String[][] map) {
        long total = 0;

        // Keep removing rolls until no more can be removed
        boolean done = false;
        while (!done) {
            // Assume we are done unless we remove a roll this iteration
            done = true;

            // Create a new map for the next iteration
            String[][] nextMap = new String[map.length][map[0].length];

            // Check each roll in the warehouse
            for (int row = 0; row < map.length; row++) {
                for (int col = 0; col < map[row].length; col++) {
                    nextMap[row][col] = map[row][col];

                    // Skip empty spaces
                    if (!map[row][col].equals("@")) {
                        continue;
                    }

                    int neighbors = 0;

                    // Check for neighboring rolls
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (dx == 0 && dy == 0) {
                                continue;
                            }
                            int newRow = row + dy;
                            int newCol = col + dx;
                            if (newRow >= 0 && newRow < map.length &&
                                newCol >= 0 && newCol < map[row].length) {
                                if (map[newRow][newCol].equals("@")) {
                                    neighbors++;
                                }
                            }
                        }
                    }

                    // If less than 4 neighbors, it can be removed so update the next map
                    if (neighbors < 4) {
                        total++;
                        nextMap[row][col] = ".";
                        done = false;
                    }                
                }
            }
            map = nextMap;
        }

        System.out.println("Part 2: {" + total + "}");
    }
}
