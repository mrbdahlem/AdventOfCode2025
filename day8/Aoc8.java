package day8;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

/**
 * Advent of Code 2025 - Day 8
 * @author Brian Dahlem
 * 
 * Connect junction boxes (points) in 3D space into circuits to minimize wiring
 * distance so that the elves can set up their holiday lights!
 */
public class Aoc8 {
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
        CircuitConnectionInfo circuitConnections = prepare(data);

        long prepped = System.nanoTime();
        partOne(numPairs, circuitConnections);
        long first = System.nanoTime();
        partTwo(circuitConnections);
        long sec = System.nanoTime();

        System.out.println("Preparation duration: " + (prepped - start) / 1000 / 1000 + "ms");
        System.out.println("Part 1 duration: " + (first - prepped) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * Parse the input data into useful structures
     * @param data raw input data 
     */
    public static CircuitConnectionInfo prepare(String data) {
        // Parse the points from the input data
        String[] lines = data.split("\\R");
        Point3D[] points = new Point3D[lines.length];
        for (int i = 0; i < lines.length; i++) {
            points[i] = Point3D.from(lines[i]);
        }

        // Create all pairs of points and add them to a priority queue (sorted by distance)
        Queue<Pair> connectedPairs = new PriorityQueue<>(points.length * (points.length - 1) / 2);
        for (int j = 0; j < points.length; j++) {
            Point3D p1 = points[j];
            for (int k = j + 1; k < points.length; k++) {
                connectedPairs.add(new Pair(p1, points[k]));
            }
        }

        // Initialize each point as its own circuit
        List<Circuit> circuits = new ArrayList<>(points.length);
        Map<Point3D, Circuit> circuitMap = new HashMap<>(points.length);
        for (Point3D p : points) {
            Circuit c = new Circuit();
            c.add(p);
            circuits.add(c);
            circuitMap.put(p, c);
        }

        return new CircuitConnectionInfo(connectedPairs, circuitMap, circuits);
    }

    /**
     * Connect the closest pairs of points into circuits
     * @param numPairs number of pairs to connect
     * @param circuits the circuits of connected points
     * @param circuitMap map of points to the circuits they belong to
     * @param connectedPairs priority queue of connected pairs of points sorted by distance
     */
    public static void partOne(int numPairs, CircuitConnectionInfo circuitConnections) {
        // Connect the closest pairs of points into circuits
        for (int i = 0; i < numPairs; i++) {
            connectNextPair(circuitConnections);
        }
        
        List<Circuit> circuits = circuitConnections.circuits();

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
     * Connect the closest pairs of points into circuits until all points are connected in one circuit
     * @param circuits the circuits of connected points
     * @param circuitMap map of points to the circuits they belong to
     * @param connectedPairs priority queue of connected pairs of points sorted by distance
     */
    public static void partTwo(CircuitConnectionInfo circuitConnections) {
        // Connect the closest pairs of points into circuits
        Pair pair = null;
        while(circuitConnections.circuits().size() > 1) {
            pair = connectNextPair(circuitConnections);
        }
        
        // Calculate the distance from the wall to the last pair
        long total = pair.p1().x() * pair.p2().x();

        System.out.println("Part 2: {" + total + "}");
    }

    private static Pair connectNextPair(CircuitConnectionInfo circuitConnections) {
        Queue<Pair> connectedPairs = circuitConnections.connectedPairs();
        Map<Point3D, Circuit> circuitMap = circuitConnections.circuitMap();
        List<Circuit> circuits = circuitConnections.circuits();

        Pair pair = connectedPairs.poll();
        Circuit c1 = circuitMap.get(pair.p1());
        Circuit c2 = circuitMap.get(pair.p2());

        // Merge the two circuits if they are not the same circuit
        if (c1 != c2) {
            Circuit merged = c1.merge(c2, circuitMap);
            circuits.remove(c1);
            circuits.remove(c2);
            circuits.add(merged);
        }

        return pair;
    }

    /**
     * A record to hold prepped data structures
     */
    record CircuitConnectionInfo(Queue<Pair> connectedPairs, Map<Point3D, Circuit> circuitMap, List<Circuit> circuits) {}
}

/**
 * A pair of 3D points and their squared distance
 */
record Pair(Point3D p1, Point3D p2, long distance) implements Comparable<Pair> {
    public Pair(Point3D p1, Point3D p2) {
        this(p1, p2, p1.distanceSquared(p2));
    }

    /**
     * Check if this pair is equal to another pair (regardless of order)
     * @param o the other pair
     * @return true if the pairs are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair pair = (Pair) o;
        return (p1.equals(pair.p1) && p2.equals(pair.p2)) ||
            (p1.equals(pair.p2) && p2.equals(pair.p1));
    }

    /**
     * Compare this pair to another pair based on their distance
     * @param other the other pair
     * @return negative if this pair is closer, positive if farther, zero if equal
     */        @Override
    public int compareTo(Pair other) {
        return Long.compare(this.distance, other.distance);
    }
}

/**
 * A point in 3D space
 */
record Point3D(int x, int y, int z) {
    /**
     * Create a Point3D from a comma-separated string
     * @param s the string
     * @return the Point3D
     */
    public static Point3D from (String s) {
        String[] parts = s.split(",");
        return new Point3D(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    /**
     * Calculate the squared distance to another point
     * @param other the other point
     * @return the squared distance
     */
    public long distanceSquared(Point3D other) {
        long dx = this.x - other.x;
        long dy = this.y - other.y;
        long dz = this.z - other.z;
        return (dx * dx + dy * dy + dz * dz);
    }
}

/**
 * A circuit of connected points
 */
class Circuit {
    // The points in the circuit
    private Set<Point3D> points = new LinkedHashSet<>();
    
    /**
     * Add a point to the circuit
     * @param p the point to add
     */
    public void add(Point3D p) {
        points.add(p);
    }

    /**
     * Get the size of the circuit
     * @return the number of points in the circuit
     */
    public int size() {
        return points.size();
    }

    /**
     * Merge this circuit with another circuit
     * @param other the other circuit
     * @param circuitMap the map of points to circuits
     * @return the merged circuit
     */
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
