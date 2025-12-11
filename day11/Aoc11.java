package day11;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Advent of Code 2025 - Day 11
 * @author Brian Dahlem
 * 
 * Brief description of the puzzle.
 */
public class Aoc11 {
    public static void main(String[] args) throws FileNotFoundException {
        // Load the data
        File inputFile = new File("day11.txt");
        Scanner scan = new Scanner(inputFile);
       
        // Load the entire data as one token
        scan.useDelimiter("\\A");
        String data = scan.next();
        scan.close();

        // Solve the two parts of the puzzle
        long start = System.nanoTime();
        Map<String, Device> deviceMap = prepare(data);
        long prepped = System.nanoTime();
        partOne(deviceMap);
        long first = System.nanoTime();
        partTwo(deviceMap);
        long sec = System.nanoTime();

        System.out.println("Preparation duration: " + (prepped - start) / 1000 / 1000 + "ms");
        System.out.println("Part 1 duration: " + (first - prepped) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * @param data raw input data
     */
    public static Map<String, Device> prepare(String data) {
        String[] lines = data.split("\\R");
        Device[] devices = new Device[lines.length];
        for (int i = 0; i < lines.length; i++) {
            devices[i] = Device.from(lines[i]);
            System.out.println(devices[i]);
        }
        Map<String, Device> deviceMap = new HashMap<>();
        for (Device device : devices) {
            deviceMap.put(device.name(), device);
        }

        deviceMap.put("out", new Device("out", List.of()));
        if (!deviceMap.containsKey("you")) {
            deviceMap.put("you", deviceMap.get("svr"));
        }

        return deviceMap;
    }

    /**
     * @param deviceMap 
     * 
     */
    public static void partOne(Map<String,Device> deviceMap) {
        Device start = deviceMap.get("you");
        Device target = deviceMap.get("out");

        long total = findPaths(start, target, deviceMap, new HashSet<>());

        System.out.println("Part 1: {" + total + "}");
    }

    private static long findPaths(Device current, Device target,
        Map<String, Device> deviceMap, Set<String> visited) {
            
        if (current.name().equals(target.name())) {
            return 1;
        }

        visited.add(current.name());

        long pathCount = 0;
        for (String connectionName : current.connections()) {
            if (!visited.contains(connectionName)) {
                Device nextDevice = deviceMap.get(connectionName);
                pathCount += findPaths(nextDevice, target, deviceMap,
                    new HashSet<>(visited));
            }
        }

        return pathCount;
    }
    /**
     * @param deviceMap 
     * 
     */
    public static void partTwo(Map<String,Device> deviceMap) {
        Device start = deviceMap.get("svr");
        Device target = deviceMap.get("out");
        Device[] include = {
            deviceMap.get("dac"),
            deviceMap.get("fft")
        };

        long total = findPathsVisiting(start, target, deviceMap, new HashSet<>(), include);

        System.out.println("Part 2: {" + total + "}");
    }
    
        private static long findPathsVisiting(Device current, Device target,
            Map<String,Device> deviceMap, HashSet<String> visited, Device[] include) {
            if (current.name().equals(target.name())) {
                for (Device req : include) {
                    if (!visited.contains(req.name())) {
                        return 0;
                    }
                }
                
                return 1;
            }

            visited.add(current.name());

            long pathCount = 0;
            for (String connectionName : current.connections()) {
                if (!visited.contains(connectionName)) {
                    Device nextDevice = deviceMap.get(connectionName);
                    pathCount += findPathsVisiting(nextDevice, target, deviceMap,
                        new HashSet<>(visited), include);
                }
            }
            return pathCount;
        }
}

record Device (String name, List<String> connections) {
    public static Device from(String data) {
        String[] parts = data.split(":\s+");
        String name = parts[0];
        List<String> connections = List.of(parts[1].split("\s+"));
        return new Device(name, connections);
    }
}
