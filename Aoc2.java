import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Aoc2 {
    public static void main(String[] args) throws FileNotFoundException {
        File inputFile = new File("day2.txt");
        Scanner scan = new Scanner(inputFile);
       
        // Get the product id ranges to check for invalid ids
        String[] ranges = scan.nextLine().split(",");

        scan.close();

        // Check for invalid ids
        long start = System.nanoTime();
        partOne(ranges);
        long first = System.nanoTime();
        partTwo(ranges);
        long sec = System.nanoTime();

        System.out.println("Part 1 duration: " + (first - start) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * Check for invalid ids composed solely of a sequence of digits
     * repeated twice
     * @param ranges product id ranges
     */
    public static void partOne(String[] ranges) {
        long total = 0;

        // Loop through each range of ids
        for (String range : ranges) {
            // Break the range into a first and last id
            String[] parts = range.split("\\-");
            long start = Long.parseLong(parts[0]);
            long end = Long.parseLong(parts[1]);

            // Loop through each id in the range
            for (long id = start; id <= end; id++) {
                String textId = "" + id;

                // Check if the two halves of the id are similar
                if (textId.length() % 2 == 0) {
                    String first = textId.substring(0, textId.length() / 2);
                    String second = textId.substring(textId.length()/2);

                    // If they are, the id is invalid
                    if (first.equals(second)) {
                        total += id;
                    }
                }
            }
        }

        System.out.println("Part 1: {" + total + "}");
    }

    /**
     * Check for invalid ids made solely of repeating patterns of digits
     * @param ranges the product id ranges to check
     */
    public static void partTwo(String[] ranges) {
        long total = 0;

        // Loop through each range of ids
        for (String range : ranges) {
            // Find the first and last id in the range
            String[] parts = range.split("\\-");
            long start = Long.parseLong(parts[0]);
            long end = Long.parseLong(parts[1]);

            // Loop through each id in the range
            for (long id = start; id <= end; id++) {
                String textId = "" + id;
                
                // Check for ids composed solely of any sized repetition
                for (int size = 1; size <= textId.length() / 2; size++) {
                    if (textId.length() % size != 0) {
                        continue;
                    }

                    boolean invalid = true;

                    // Find the part that could be repeating in the id
                    String first = textId.substring(0, size);
                    
                    // check if that part repeats throughout the id                    
                    for (int i = 1; i < textId.length() / size; i++) {
                        if (!first.equals(textId.substring(i * size, (i + 1) * size))) {
                            // if not, this could be a valid id
                            invalid = false;
                            break;
                        }
                    }

                    // If the id was only repeating patterns of digits, it's
                    // invalid so mark it
                    if (invalid) {
                        total += id;
                        break;
                    }                    
                }
            }
        }

        System.out.println("Part 2: {" + total + "}");
    }

    public static Scanner prepare(String filename) throws FileNotFoundException {
        return new Scanner(new File(filename));
    }
}