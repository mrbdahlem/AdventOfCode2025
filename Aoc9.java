import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Aoc9 {
    public static Point[] points;

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
        points = prepare(data);
        long prepped = System.nanoTime();
        partOne();
        long first = System.nanoTime();
        partTwo();
        long sec = System.nanoTime();

        System.out.println("Preparation duration: " + (prepped - start) / 1000 / 1000 + "ms");
        System.out.println("Part 1 duration: " + (first - prepped) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * @param data raw input data 
     */
    public static Point[] prepare(String data) {
        String[] lines = data.split("\\R");
        Point[] points = new Point[lines.length];

        for (int i = 0; i < lines.length; i++) {
            points[i] = Point.from(lines[i]);
        }

        return points;
    }

    /**
     * 
     */
    public static void partOne() {
        long maxArea = 0;

        for (int i = 0; i < points.length - 1; i++) {
            for (int j = i + 1; j < points.length; j++) {
                Point p1 = points[i];
                Point p2 = points[j];

                long area = Math.abs((p1.x() - p2.x() + 1) * (p1.y() - p2.y() + 1));
                if (area > maxArea) {
                    maxArea = area;
                }
            }
        }

        System.out.println("Part 1: {" + maxArea + "}");
    }

    /**
     * 
     */
    public static void partTwo() {
        long total = 0;

        System.out.println("Part 2: {" + total + "}");
    }
}

record Point(int x, int y) {
    public static Point from(String line) {
        String[] parts = line.split(",");
        return new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}