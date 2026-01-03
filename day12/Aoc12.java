import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Advent of Code 2025 - Day 12
 *
 * @author Brian Dahlem
 * 
 * Brief description of the puzzle.
 */
public class Aoc12 {
    private static Map<Integer, Package> packages;
    private static Region[] regions;

    public static void main(String[] args) throws FileNotFoundException {
        // Load the data
        File inputFile = new File("day12.txt");
        Scanner scan = new Scanner(inputFile);
       
        // Load the entire data as one token
        scan.useDelimiter("\\A");
        String data = scan.next();
        scan.close();

        // Solve the two parts of the puzzle
        long start = System.nanoTime();
        prepare(data);
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
    public static void prepare(String data) {
        String[] parts = data.split("\n\n");
        
        packages = new HashMap<>();
        for (int i = 0; i < parts.length - 1; i++) {
            Package pkg = new Package(parts[i]);
            packages.put(pkg.getId(), pkg);
        }
        
        String[] regionParts = parts[parts.length - 1].split("\\R");
        regions = new Region[regionParts.length];
        for (int i = 0; i < regionParts.length; i++) {
            regions[i] = new Region(regionParts[i]);
        }
    }

    /**
     * 
     */
    public static void partOne() {
        long total = 0;

        for (Region region : regions) {
            int regionArea = region.getWidth() * region.getHeight();
            int packagesArea = 0;
            for (int pkgId = 0; pkgId < region.getPackageQtys().length; pkgId++) {
                Package pkg = packages.get(pkgId);
                packagesArea += pkg.getArea() * region.getPackageQtys()[pkgId];
            }
            if (packagesArea <= regionArea) {
                int cols = region.getWidth() / 3;
                int rows = region.getHeight() / 3;
                int spaces = cols * rows;

                for (int pkgQty: region.getPackageQtys()) {
                    spaces -= pkgQty;
                }

                if (spaces >= 0) {
                    total += 1;
                }
            }
        }

        System.out.println("Part 1: {" + total + "}");
    }

    /**
     * 
     */
    public static void partTwo() {
        long total = 0;

        System.out.println("Part 2: {" + total + "}");
    }
}

class Package {
    private final int id;
    private final int width;
    private final int height;
    private final int area;

    public Package(String data) {
        String[] parts = data.split("\\R");
        this.id = Integer.parseInt(parts[0].substring(0, parts[0].length() - 1));
        this.width = parts[1].length();
        this.height = parts.length - 1;

        int count = 0;        
        String[][] grid = new String[height][];
        for (int r = 0; r < height; r++) {
            grid[r] = parts[r + 1].split("");
            for (int c = 0; c < width; c++) {
                if (grid[r][c].equals("#")) {
                    count++;
                }
            }
        }

        this.area = count;
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getArea() {
        return area;
    }
}

class Region {
    private final int width;
    private final int height;
    private final int[] packageQtys;

    public Region(String data) {
        String[] parts = data.split(":");
        String[] dimensions = parts[0].trim().split("x");
        String[] pkgs = parts[1].trim().split(" ");

        this.width = Integer.parseInt(dimensions[0]);
        this.height = Integer.parseInt(dimensions[1]);
        this.packageQtys = new int[pkgs.length];
        for (int i = 0; i < pkgs.length; i++) {
            this.packageQtys[i] = Integer.parseInt(pkgs[i]);
        }
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public int[] getPackageQtys() {
        return packageQtys;
    }
    public int getArea() {
        return width * height;
    }
}