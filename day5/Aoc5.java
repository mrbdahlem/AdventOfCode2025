package day5;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Advent of Code 2025 - Day 5
 * @author Brian Dahlem
 * 
 * Determine which ingredients are still fresh based on given ranges
 * and calculate the total number of fresh ingredient ids covered by the ranges.
 */
public class Aoc5 {
    public static void main(String[] args) throws FileNotFoundException {
        // Load the data
        File inputFile = new File("day5.txt");
        Scanner scan = new Scanner(inputFile);
       
        // Load the entire data as one token
        scan.useDelimiter("\\A");

        String data = scan.next();

        // Parse the data
        String[] parts = data.split("\n\n");
        
        // Break into fresh ingredient id ranges and ingredient ids
        String[] freshRangeStrings = parts[0].split("\n");
        String[] ingredientStrings = parts[1].split("\n");

        // Convert the fresh ranges to Range objects
        Range[] freshRanges = new Range[freshRangeStrings.length];
        for (int i = 0; i < freshRangeStrings.length; i++) {
            String[] bounds = freshRangeStrings[i].split("-");
            freshRanges[i] = new Range(Long.parseLong(bounds[0]), Long.parseLong(bounds[1]));
        }

        // Convert the ingredient strings to numbers
        long[] ingredients = new long[ingredientStrings.length];
        for (int i = 0; i < ingredientStrings.length; i++) {
            ingredients[i] = Long.parseLong(ingredientStrings[i]);
        }

        scan.close();

        // Solve the two parts of the puzzle
        long start = System.nanoTime();
        partOne(freshRanges, ingredients);
        long first = System.nanoTime();
        partTwo(freshRanges);
        long sec = System.nanoTime();

        System.out.println("Part 1 duration: " + (first - start) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * Determine how many ingredients are fresh
     * @param ingredients the available ingredients' ids
     * @param freshRanges the ranges of fresh ingredient ids
     */
    public static void partOne(Range[] freshRanges, long[] ingredients) {
        long total = 0;

        for (long ingredient : ingredients) {
            boolean isFresh = false;
            for (Range range : freshRanges) {
                if (range.contains(ingredient)) {
                    isFresh = true;
                    break;
                }
            }

            if (isFresh) {
                total ++;
            }
        }

        System.out.println("Part 1: {" + total + "}");
    }

    /**
     * Determine the total number of fresh ingredient ids covered by the ranges
     * @param freshRanges the ranges of fresh ingredient ids
     */
    public static void partTwo(Range[] freshRanges) {
        long total = 0;

        // Merge overlapping ranges
        List<Range> mergedRanges = new ArrayList<>(List.of(freshRanges));
        boolean consolidated = true;
        while (consolidated) {
            consolidated = false;
            for (int i = 0; i < mergedRanges.size(); i++) {
                Range current = mergedRanges.get(i);
                for (int j = mergedRanges.size() - 1; j > i; j--) {
                    Range other = mergedRanges.get(j);
                    if (current.overlaps(other)) {                    
                        // System.out.print("Merged " + other + " into " + current);
                        current = current.merge(other);
                        // System.out.println(" to form " + current);
                        mergedRanges.set(i, current);
                        mergedRanges.remove(j);
                        consolidated = true;
                    }
                }
                // System.out.println(mergedRanges.size());
            }
        }

        // Calculate the total length of the merged ranges
        for (Range range : mergedRanges) {
            // System.out.println("Merged Range: " + range);
            total += range.length();
        }
                           
        // System.out.println(mergedRanges.size() + " remaining ranges.");

        System.out.println("Part 2: {" + total + "} > 302898883543177 < 366601505152801");
        if (total >= 366601505152801L || total <= 302898883543177L) {
            throw new RuntimeException("Total out of bounds!!!");
        }
    }
}

/**
 * A range of long integers from start to end, inclusive
 */
record Range(long start, long end) {
    public boolean contains(long value) {
        return value >= start && value <= end;
    }

    public boolean overlaps(Range other) {
        return this.start <= other.end && other.start <= this.end;
    }

    public long length() {
        return end - start + 1;
    }

    public Range merge(Range other) {
        return new Range(Math.min(this.start, other.start), Math.max(this.end, other.end));
    }
}
