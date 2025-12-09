package day9;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Aoc9 {

    public static void main(String[] args) throws FileNotFoundException {
        // Load the data
        File inputFile = new File("day9.txt");
        Scanner scan = new Scanner(inputFile);
       
        // Load the entire data as one token
        scan.useDelimiter("\\A");
        String data = scan.next();
        scan.close();

        // Solve the two parts of the puzzle
        long start = System.nanoTime();
        Point[] points = prepare(data);
        long prepped = System.nanoTime();
        partOne(points);
        long first = System.nanoTime();
        partTwo(points);
        long sec = System.nanoTime();

        System.out.println("Preparation duration: " + (prepped - start) / 1000 / 1000 + "ms");
        System.out.println("Part 1 duration: " + (first - prepped) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * Convert raw input data into an array of Points
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
     * Find the largest rectangle area defined by any two points
     * @param points array of points
     */
    public static void partOne(Point[] points) {
        long maxArea = 0;

        // Iterate over all pairs of points
        for (int i = 0; i < points.length - 1; i++) {
            Point p1 = points[i];
            for (int j = i + 1; j < points.length; j++) {
                Point p2 = points[j];

                // Calculate the area of the rectangle defined by p1 and p2
                long area = Math.abs((p1.x() - p2.x() + 1) * (p1.y() - p2.y() + 1));

                // Update maxArea if this area is larger
                if (area > maxArea) {
                    maxArea = area;
                }
            }
        }

        System.out.println("Part 1: {" + maxArea + "}");
    }

    /**
     * Find the largest rectangle area defined by any two points that does not intersect any line segments
     * @param points the array of points
     */
    public static void partTwo(Point[] points) {
        long maxArea = 0;

        // Iterate over all pairs of points
        for (int i = 0; i < points.length - 1; i++) {
            Point p1 = points[i];
            for (int j = i + 1; j < points.length; j++) {
                Point p2 = points[j];

                // Define the rectangle boundaries formed by p1 and p2
                long rectLeft = Math.min(p1.x(), p2.x());
                long rectRight = Math.max(p1.x(), p2.x());
                long rectTop = Math.min(p1.y(), p2.y());
                long rectBottom = Math.max(p1.y(), p2.y()); 

                // Calculate the area of the rectangle
                long area = ((rectRight - rectLeft + 1) * (rectBottom - rectTop + 1));

                // If the area is larger than maxArea, check for intersections
                if (area > maxArea) {
                    for (int k = 0; k < points.length; k++) {
                        if (k == i || k == j) {
                            continue;
                        }
                        int l = (k + 1) % points.length;
                        if (l == i || l == j) {
                            continue;
                        }

                        // Get the current line segment defined by points[k] and points[(k + 1) % points.length]
                        Point p3 = points[k];
                        Point p4 = points[l];

                        // check if the line between p3 and p4 intersects the rectangle formed by p1 and p2
                        if (intersectsRectangle(p3, p4, rectLeft, rectRight, rectTop, rectBottom)) {
                            area = 0;
                            break;
                        }
                    }

                    if (area != 0) {
                        maxArea = area;
                    }
                }
            }
        }

        if (maxArea <= 1529641011L) {
            throw new AssertionError("Max area too small: " + maxArea);
        }
        if (maxArea >= 4650823975L) {
            throw new AssertionError("Max area too large: " + maxArea);
        }

        System.out.println("Part 2: {" + maxArea + "}");
    }

    /**
     * Check if the line segment between points a and b intersects the rectangle defined by left, right, top, bottom
     * @param a the first point of the line segment
     * @param b the second point of the line segment
     * @param left the left boundary of the rectangle
     * @param right the right boundary of the rectangle
     * @param top the top boundary of the rectangle
     * @param bottom the bottom boundary of the rectangle
     * @return true if the line segment intersects the rectangle, false otherwise
     */
    private static boolean intersectsRectangle(Point a, Point b,
                                               long left, long right,
                                               long top, long bottom) {
        // Determine if the line segment is vertical or horizontal
        if (a.x() == b.x()) {
            // This is a vertical edge at x = a.x
            long x = a.x();
            long y1 = Math.min(a.y(), b.y());
            long y2 = Math.max(a.y(), b.y());

            // The line intersects the rectangle if x is between left and right
            // and the y-range of the line overlaps with the y-range of the rectangle
            return x > left && x < right && y2 > top && y1 < bottom;

        } else {
             // This is a horizontal edge at y = a.y
            long y = a.y();
            long x1 = Math.min(a.x(), b.x());
            long x2 = Math.max(a.x(), b.x());

            // The line intersects the rectangle if y is between top and bottom
            // and the x-range of the line overlaps with the x-range of the rectangle
            return y > top && y < bottom && x2 > left && x1 < right;
        }
    }

}


/**
 * A point in 2D space
 */
record Point(long x, long y) {
    /**
     * Create a Point from a string in the format "x,y"
     * @param line the input string
     * @return the created Point
     */
    public static Point from(String line) {
        String[] parts = line.split(",");
        return new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}