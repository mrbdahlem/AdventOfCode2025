import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Aoc8 {
    private static Point3D[] points;
    private static List<Circuit> circuits;
    private static List<Pair> connectedPairs;
    private static Map<Point3D, Circuit> circuitMap;

    public static void main(String[] args) throws FileNotFoundException {
        int numPairs; String datafile;

        boolean sample = false;//true;
        if (sample) {
            numPairs = 10;
            datafile = "sample8.txt";
        }
        else {
            numPairs = 1000;
            datafile = "day8.txt";
        }

        // Load the data
        File inputFile = new File(datafile);
        Scanner scan = new Scanner(inputFile);

        // Load the entire data as one token
        scan.useDelimiter("\\A");
        String data = scan.next();

        scan.close();

        // Solve the two parts of the puzzle
        long start = System.nanoTime();
        prepare(data);
        long prepped = System.nanoTime();
        partOne(numPairs);
        long first = System.nanoTime();
        partTwo();
        long sec = System.nanoTime();

        System.out.println("Preparation duration: " + (prepped - start) / 1000 / 1000 + "ms");
        System.out.println("Part 1 duration: " + (first - prepped) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * @param data raw input data 
     * 
     */
    public static void prepare(String data) {

        long start = System.nanoTime();

        String[] lines = data.split("\\R");
        points = new Point3D[lines.length];
        for (int i = 0; i < lines.length; i++) {
            points[i] = Point3D.from(lines[i]);
        }

        long parse = System.nanoTime();

        connectedPairs = new ArrayList<>(points.length * (points.length - 1) / 2);
        for (int j = 0; j < points.length; j++) {
            Point3D p1 = points[j];
            for (int k = j + 1; k < points.length; k++) {
                connectedPairs.add(new Pair(p1, points[k]));
            }
        }
        
        long pairs = System.nanoTime();

        // Sort pairs by distance smallest to largest
        connectedPairs.sort((a, b) -> Long.compare(a.distance(), b.distance()));

        long sort = System.nanoTime();

        circuits = new ArrayList<>(points.length);
        circuitMap = new HashMap<>(points.length);
        for (Point3D p : points) {
            Circuit c = new Circuit();
            c.add(p);
            circuits.add(c);
            circuitMap.put(p, c);
        }

        long circuitsTime = System.nanoTime();

        System.out.println("Parsing duration: " + (parse - start) / 1000 / 1000 + "ms");
        System.out.println("Pairing duration: " + (pairs - parse) / 1000 / 1000 + "ms");
        System.out.println("Sorting duration: " + (sort - pairs) / 1000 / 1000 + "ms");
        System.out.println("Circuit initialization duration: " + (circuitsTime - sort) / 1000 / 1000 + "ms");
    }

    /**
     * @param points junction box locations 
     * 
     */
    public static void partOne(int numPairs) {
        // Connect the closest pairs of points into circuits
        for (int i = 0; i < numPairs; i++) {
            Pair pair = connectedPairs.get(i);
            Circuit c1 = circuitMap.get(pair.p1());
            Circuit c2 = circuitMap.get(pair.p2());

            // Merge the two circuits if they are not the same circuit
            if (c1 != c2) {
                Circuit merged = c1.merge(c2, circuitMap);
                circuits.remove(c1);
                circuits.remove(c2);
                circuits.add(merged);
            }
        }
        
        // Sort circuits by size largest to smallest
        circuits.sort((a, b) -> b.size() - a.size());

        // Calculate the product of the sizes of the three largest circuits
        long total = 1;
        for (int i = 0; i < 3; i++) {
            Circuit c = circuits.get(i);
            total *= c.size();
        }

        System.out.println("Part 1: {" + total + "}");
    }

    /**
     * @param points 
     * 
     */
    public static void partTwo() {
        // Connect the closest pairs of points into circuits
        int i = 0;
        Pair pair = null;
        while(circuits.size() > 1) {
            pair = connectedPairs.get(i);
            Circuit c1 = circuitMap.get(pair.p1());
            Circuit c2 = circuitMap.get(pair.p2());

            // Merge the two circuits if they are not the same circuit
            if (c1 != c2) {
                Circuit merged = c1.merge(c2, circuitMap);
                circuits.remove(c1);
                circuits.remove(c2);
                circuits.add(merged);
            }
            i++;
        }
        
        if (pair == null) {
            throw new IllegalStateException("No pairs were connected");
        }
        
        // Calculate the distance from the wall to the last pair
        long total = pair.p1().x() * pair.p2().x();

        System.out.println("Part 2: {" + total + "}");
    }
}

record Pair(Point3D p1, Point3D p2, long distance) {
    public Pair(Point3D p1, Point3D p2) {
        this(p1, p2, p1.distanceSquared(p2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return (p1.equals(pair.p1) && p2.equals(pair.p2)) ||
               (p1.equals(pair.p2) && p2.equals(pair.p1));
    }
}

record Point3D(int x, int y, int z) {
    public static Point3D from (String s) {
        String[] parts = s.split(",");
        return new Point3D(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    public long distanceSquared(Point3D other) {
        long dx = this.x - other.x;
        long dy = this.y - other.y;
        long dz = this.z - other.z;
        return (dx * dx + dy * dy + dz * dz);
    }
}

class Circuit {
    Set<Point3D> points = new LinkedHashSet<>();
    
    public void add(Point3D p) {
        points.add(p);
    }

    public int size() {
        return points.size();
    }

    public boolean contains(Point3D p) {
        return points.contains(p);
    }

    public Circuit merge(Circuit other, Map<Point3D,Circuit> circuitMap) {
        Circuit larger = this.size() >= other.size() ? this : other;
        Circuit smaller = this.size() >= other.size() ? other : this;
        
        larger.points.addAll(smaller.points);
        for (Point3D p : smaller.points) {
            circuitMap.put(p, larger);
        }
        return larger;
    }
}   