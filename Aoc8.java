import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Aoc8 {
    public static void main(String[] args) throws FileNotFoundException {
        int pairs; String datafile;

        boolean sample = false;//true;
        if (sample) {
            pairs = 10;
            datafile = "sample8.txt";
        }
        else {
            pairs = 1000;
            datafile = "day8.txt";
        }

        // Load the data
        File inputFile = new File(datafile);
        Scanner scan = new Scanner(inputFile);

        // Load the entire data as one token
        scan.useDelimiter("\\A");

        String[] lines = scan.next().split("\\R");
        Point3D[] points = new Point3D[lines.length];
        for (int i = 0; i < lines.length; i++) {
            points[i] = Point3D.from(lines[i]);
        }

        scan.close();

        // Solve the two parts of the puzzle
        long start = System.nanoTime();
        partOne(points, pairs);
        long first = System.nanoTime();
        partTwo(points);
        long sec = System.nanoTime();

        System.out.println("Part 1 duration: " + (first - start) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * @param points junction box locations 
     * 
     */
    public static void partOne(Point3D[] points, int pairs) {
        // Initialize each point as its own circuit
        List<Circuit> circuits = new ArrayList<>();
        for (Point3D p : points) {
            Circuit c = new Circuit();
            c.add(p);
            circuits.add(c);
        }

        // Create all possible pairs of points
        List<Pair> connectedPairs = new ArrayList<>();
        for (int j = 0; j < points.length; j++) {
            Point3D p1 = points[j];
            for (int k = j + 1; k < points.length; k++) {
                connectedPairs.add(new Pair(p1, points[k]));
            }
        }

        // Sort pairs by distance smallest to largest
        connectedPairs.sort((a, b) -> Double.compare(a.distance(), b.distance()));

        // Connect the closest pairs of points into circuits

        for (int i = 0; i < pairs; i++) {
            Pair pair = connectedPairs.get(i);
            Circuit c1 = null;
            Circuit c2 = null;

            for (Circuit c : circuits) {
                if (c.contains(pair.p1())) {
                    c1 = c;
                }
                if (c.contains(pair.p2())) {
                    c2 = c;
                }
            }

            // Merge the two circuits if they are not the same circuit
            if (c1 != c2) {
                Circuit merged = c1.merge(c2);
                circuits.remove(c1);
                circuits.remove(c2);
                circuits.add(merged);
            }
        }
        
        // Sort circuits by size largest to smallest
        circuits.sort((a, b) -> b.size() - a.size());

        System.out.println("Number of circuits: " + circuits.size());

        // Calculate the product of the sizes of the three largest circuits
        long total = 1;
        for (int i = 0; i < 3; i++) {
            Circuit c = circuits.get(i);
            System.out.println("Circuit size: " + c.size());
            total *= c.size();
        }

        if (total <= 12544) {
            throw new AssertionError("Error: product of sizes is too small: " + total);
        }
        System.out.println("Part 1: {" + total + "}");
    }

    /**
     * @param points 
     * 
     */
    public static void partTwo(Point3D[] points) {
        // Initialize each point as its own circuit
        List<Circuit> circuits = new ArrayList<>();
        for (Point3D p : points) {
            Circuit c = new Circuit();
            c.add(p);
            circuits.add(c);
        }

        // Create all possible pairs of points
        List<Pair> connectedPairs = new ArrayList<>();
        for (int j = 0; j < points.length; j++) {
            Point3D p1 = points[j];
            for (int k = j + 1; k < points.length; k++) {
                connectedPairs.add(new Pair(p1, points[k]));
            }
        }

        // Sort pairs by distance smallest to largest
        connectedPairs.sort((a, b) -> Double.compare(a.distance(), b.distance()));

        // Connect the closest pairs of points into circuits

        int i = 0;
        Pair pair = null;
        while(circuits.size() > 1) {
            pair = connectedPairs.get(i);
            Circuit c1 = null;
            Circuit c2 = null;

            for (Circuit c : circuits) {
                if (c.contains(pair.p1())) {
                    c1 = c;
                }
                if (c.contains(pair.p2())) {
                    c2 = c;
                }
            }

            // Merge the two circuits if they are not the same circuit
            if (c1 != c2) {
                Circuit merged = c1.merge(c2);
                circuits.remove(c1);
                circuits.remove(c2);
                circuits.add(merged);
            }
            i++;
        }
        
        if (pair == null) {
            throw new IllegalStateException("No pairs were connected");
        }
        
        System.out.println("Number of circuits: " + circuits.size());

        // Calculate the distance from the wall to the last pair
        long total = pair.p1().x() * pair.p2().x();

        System.out.println("Part 2: {" + total + "}");
    }
}

record Pair(Point3D p1, Point3D p2) {
    Pair(Point3D p1, Point3D p2) {
        if (p1 == null || p2 == null) {
            throw new IllegalArgumentException("Points cannot be null");
        }
        if (p1.equals(p2)) {
            throw new IllegalArgumentException("Points cannot be the same");
        }
        this.p1 = p1;
        this.p2 = p2;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return (p1.equals(pair.p1) && p2.equals(pair.p2)) ||
               (p1.equals(pair.p2) && p2.equals(pair.p1));
    }

    public double distance() {
        return p1.distance(p2);
    }
}

record Point3D(int x, int y, int z) {
    public static Point3D from (String s) {
        String[] parts = s.split(",");
        return new Point3D(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    public double distance(Point3D other) {
        long dx = this.x - other.x;
        long dy = this.y - other.y;
        long dz = this.z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}

class Circuit {
    Set<Point3D> points;
    
    public Circuit() {
        points = new HashSet<>();
    }

    public void add(Point3D p) {
        points.add(p);
    }

    public int size() {
        return points.size();
    }

    public boolean contains(Point3D p) {
        return points.contains(p);
    }

    public Circuit merge(Circuit other) {
        Circuit merged = new Circuit();
        merged.points.addAll(this.points);
        merged.points.addAll(other.points);
        return merged;
    }
}   