import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Aoc9 {
    public static Point[] points;

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
            Point p1 = points[i];
            for (int j = i + 1; j < points.length; j++) {
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
        long maxArea = 0;

        for (int i = 0; i < points.length - 1; i++) {
            Point p1 = points[i];
            for (int j = i + 1; j < points.length; j++) {
                Point p2 = points[j];

                long rectLeft = Math.min(p1.x(), p2.x());
                long rectRight = Math.max(p1.x(), p2.x());
                long rectTop = Math.min(p1.y(), p2.y());
                long rectBottom = Math.max(p1.y(), p2.y()); 

                long area = ((rectRight - rectLeft + 1) * (rectBottom - rectTop + 1));
                if (area > maxArea) {
                    for (int k = 0; k < points.length; k++) {
                        if (k == i || k == j) {
                            continue;
                        }
                        Point p3 = points[k];

                        int l = (k + 1) % points.length;
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

    private static boolean intersectsRectangle(Point a, Point b,
                                               long left, long right,
                                               long top, long bottom) {
        // Axis-aligned edges only
        if (a.x() == b.x()) { // vertical edge at x = a.x
            long x = a.x();
            long y1 = Math.min(a.y(), b.y());
            long y2 = Math.max(a.y(), b.y());
            // intersects rectangle interior if x is strictly inside and y-range overlaps
            return x > left && x < right && y2 > top && y1 < bottom;
        } else { // horizontal edge at y = a.y
            long y = a.y();
            long x1 = Math.min(a.x(), b.x());
            long x2 = Math.max(a.x(), b.x());
            return y > top && y < bottom && x2 > left && x1 < right;
        }
    }

    private static record Point(long x, long y) {
        public static Point from(String line) {
            String[] parts = line.split(",");
            return new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
    }
}

