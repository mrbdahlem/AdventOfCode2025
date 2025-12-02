import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Aoc1 {
    public static void main(String[] args) throws FileNotFoundException {
        File dataFile = new File("day1.txt");
        
        long now = System.nanoTime();
        partOne(new Scanner(dataFile));
        long first = System.nanoTime();
        partTwo(new Scanner(dataFile));
        long sec = System.nanoTime();

        System.out.println("Part 1 duration: " + (first - now) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }
    
    public static void partOne(Scanner scan) {
        int dial = 50;
        int count = 0;

        // process each line of input data
        while (scan.hasNextLine()) {
            String line = scan.nextLine();

            // break the line into a direction and click count
            String dir = line.substring(0, 1);
            int clicks = Integer.parseInt(line.substring(1));

            // turn the dial the specified number of clicks
            if (dir.equals("L")) {
                clicks = -clicks;
            }             
            dial = Math.floorMod(dial + clicks, 100);

            // count the times the dial hits 0
            if (dial == 0) {
                count++;
            }
        }
        
        System.out.println("Part 1: {" + count + "}");
    }

    public static void partTwo(Scanner scan) {
        int dial = 50;
        int count = 0;

        // process each line of input data
        while (scan.hasNextLine()) {
            String line = scan.nextLine();

            // break the line into a direction and click count
            String dir = line.substring(0, 1);
            int clicks = Integer.parseInt(line.substring(1));

            // Go through the clicks
            for (int i = 0; i < clicks; i++) {
                if (dir.equals("L")) {
                    dial--;
                }
                else {
                    dial++;
                }

                // Limit dial to 0..99
                dial = Math.floorMod(dial, 100);

                // if (dial > 99) {
                //     dial -= 100;
                // }
                // else if (dial < 0) {
                //     dial += 100;
                // }

                // Any time the dial hits 0, count it
                if (dial == 0) {
                    count++;
                }
            }

            // int spins = clicks / 100;
            // int rem = clicks % 100;
            
            // count += spins;
            // int was = dial;

            // if (dir.equals("L")) {
            //     dial = dial - rem;
            //     if (was > 0 && dial <= 0) {
            //         count++;
            //     }
            // }
            // else {
            //     dial = dial + rem;
            //     if (was < 100 && dial >= 100) {
            //         count++;
            //     }
            // }

            // dial = Math.floorMod(dial, 100);

            // System.out.println("{" + line + "}" + dir + ": " + clicks + "=" + dial + "| " + count);
        }
        System.out.println("Part 2: {" + count + "}");
    }
}