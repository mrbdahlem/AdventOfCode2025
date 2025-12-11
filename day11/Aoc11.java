package day11;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayDeque;

/**
 * Advent of Code 2025 - Day 11
 * @author Brian Dahlem
 * 
 * 
 * Find all distinct paths through a network of devices, first from 'you' to 'out',
 * then from 'svr' to 'out' while visiting both 'dac' and 'fft' at least once.
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
     * Prepares the device map from the raw input data.
     * @param data raw input data
     */
    public static Map<String, Device> prepare(String data) {
        // Create Device objects from input lines
        String[] lines = data.split("\\R");
        Device[] devices = new Device[lines.length];
        for (int i = 0; i < lines.length; i++) {
            devices[i] = Device.from(lines[i]);
        }

        // Map device names to Device objects
        Map<String, Device> deviceMap = new HashMap<>();
        for (Device device : devices) {
            deviceMap.put(device.name(), device);
        }

        // Ensure 'out' and 'you' devices exist
        deviceMap.put("out", new Device("out", List.of()));

        // in the second sample data 'you' is missing, so we link it to 'svr'
        if (!deviceMap.containsKey("you")) {
            deviceMap.put("you", deviceMap.get("svr"));
        }

        return deviceMap;
    }

    /**
     * Determines the number of distinct paths from 'you' to 'out'.
     * @param deviceMap the map of device names to Device objects
     */
    public static void partOne(Map<String,Device> deviceMap) {
        Device start = deviceMap.get("you");
        Device target = deviceMap.get("out");

        long total = findPaths(start, target, deviceMap, new HashSet<>());

        System.out.println("Part 1: {" + total + "}");
    }

    /**
     * Recursively finds all distinct paths from current to target device.
     * @param current the current device
     * @param target the target device
     * @param deviceMap the map of device names to Device objects
     * @param visited the set of visited device names
     * @return the number of distinct paths from current to target device
     */
    private static long findPaths(Device current, Device target,
        Map<String, Device> deviceMap, Set<String> visited) {
            
        if (current.name().equals(target.name())) {
            return 1;
        }

        visited.add(current.name());

        long pathCount = 0;
        for (String connectionName : current.connections()) {
            Device nextDevice = deviceMap.get(connectionName);
            if (nextDevice == null) {
                continue; // Skip unknown connections
            }

            String nextName = nextDevice.name();
            if (!visited.contains(nextName)) {
                pathCount += findPaths(nextDevice, target, deviceMap,
                    new HashSet<>(visited));
            }
        }

        return pathCount;
    }
    /**
     * Determines the number of distinct paths from 'svr' to 'out' that visit
     * both 'dac' and 'fft' at least once.
     * @param deviceMap the map of device names to Device objects 
     */
       public static void partTwo(Map<String,Device> deviceMap) {
        Device start = deviceMap.get("svr");
        Device target = deviceMap.get("out");
        Set<String> requiredDevices = Set.of("dac", "fft");

        CondensedGraph cg = CondensedGraph.build(deviceMap, start.name(), target.name(), requiredDevices);

        long total = cg.countPaths();
        System.out.println("Part 2: {" + total + "}");
    }
    
}

/**
 * Represents a device in the network with its outbound connections.
 */
record Device (String name, List<String> connections) {
    public static Device from(String data) {
        String[] parts = data.split(":\s+");
        String name = parts[0];
        List<String> connections = List.of(parts[1].split("\s+"));
        return new Device(name, connections);
    }
}

/**
 * A condensed graph where strongly connected components are merged into single nodes to form a
 * Directed Acyclic Graph (DAG).
 */
class CondensedGraph {
    /**
     * A node in the condensed graph representing a strongly connected component
     */
    static record Node (String name, Set<String> outgoing, Set<String> containsRequired) {
        public Node(String name) {
            this(name, new HashSet<>(), new HashSet<>());
        }
    }

    /** The nodes of the condensed graph keyed by component name */
    private Map<String, Node> nodes;
    /** The component containing the start device */
    private String startComponent;
    /** The component containing the target device */
    private String targetComponent;
    /** The names of components required in the path */
    private Set<String> requiredComponentNames;

    /**
     * Constructor for CondensedGraph used in the build method.
     * @param nodes the nodes of the condensed graph
     * @param startComponent the component containing the start device
     * @param targetComponent the component containing the target device
     * @param requiredNames the names of components required in the path
     */
    private CondensedGraph(Map<String, Node> nodes, String startComponent, String targetComponent,
        Set<String> requiredNames) {
        this.nodes = nodes;
        this.startComponent = startComponent;
        this.targetComponent = targetComponent;
        this.requiredComponentNames = requiredNames;
    }

    /**
     * Builds a condensed graph from the given device graph using Tarjan's
     * algorithm to find strongly connected components.
     * 
     * @param graph the original device graph
     * @param start the device where paths start from
     * @param target the device to reach
     * @param required the set of required devices to include in paths
     * @return the condensed graph
     */
    static CondensedGraph build(Map<String, Device> graph, String start, String target,
        Set<String> required) {

        Map<String,Integer> index = new HashMap<>();
        Map<String,Integer> lowlink = new HashMap<>();
        Map<String,String> componentOf = new HashMap<>();
        ArrayDeque<String> stack = new ArrayDeque<>();
        Set<String> onStack = new HashSet<>();
        int[] nextIndex = {0};

        // Run Tarjan's algorithm to find strongly connected components for all
        // nodes in the device graph
        for (String v : graph.keySet()) {
            if (!index.containsKey(v)) {
                runTarjan(v, graph, index, lowlink, componentOf, stack, onStack, nextIndex);
            }
        }

        // Create nodes keyed by representative name
        Map<String, Node> nodesByName = new HashMap<>();
        for (String compName : new HashSet<>(componentOf.values())) {
            nodesByName.put(compName, new Node(compName));
        }

        // Build edges between components
        for (Map.Entry<String, Device> entry : graph.entrySet()) {
            String compName = componentOf.get(entry.getKey());
            Node componentNode = nodesByName.get(compName);

            // Mark required nodes contained in this component
            if (required.contains(entry.getKey())) {
                componentNode.containsRequired.add(entry.getKey());
            }

            // Add edges to other components
            for (String neighbor : entry.getValue().connections()) {
                // Skip unknown connections
                if (!graph.containsKey(neighbor)) continue;

                // Add edge if neighbor is in a different component
                String neighborComp = componentOf.get(neighbor);
                if (!neighborComp.equals(compName)) {
                    componentNode.outgoing.add(neighborComp);
                }
            }
        }

        return new CondensedGraph(nodesByName, componentOf.get(start),
            componentOf.get(target), required);
    }

    /**
     * Tarjan's algorithm recursive helper to find strongly connected components.
     * @param node the current node being visited
     * @param graph the original device graph
     * @param index map of node to its index
     * @param lowlink map of node to its lowlink value
     * @param componentOf map of node to its component representative
     * @param stack the stack of nodes in the current path
     * @param onStack set of nodes currently on the stack
     * @param nextIndex array containing the next index value to assign
     */
    private static void runTarjan(String node, Map<String, Device> graph,
            Map<String,Integer> index, Map<String,Integer> lowlink, 
            Map<String,String> componentOf, ArrayDeque<String> stack, 
            Set<String> onStack, int[] nextIndex) {

        // Set the depth index for node to the smallest unused index
        index.put(node, nextIndex[0]);

        // Initially, lowlink is the same as index
        lowlink.put(node, nextIndex[0]);

        // Push node onto the stack
        nextIndex[0]++;
        stack.push(node);
        onStack.add(node);

        // Consider successors of the node
        for (String neighbor : graph.get(node).connections()) {
            // Skip unknown connections
            if (!graph.containsKey(neighbor)) {
                continue;
            }
            // Successor has not yet been visited; recurse on it
            if (!index.containsKey(neighbor)) {
                runTarjan(neighbor, graph, index, lowlink, componentOf, stack, onStack, nextIndex);
                lowlink.put(node, Math.min(lowlink.get(node), lowlink.get(neighbor)));
            }
            // Successor is in stack and hence in the current SCC
            else if (onStack.contains(neighbor)) {
                lowlink.put(node, Math.min(lowlink.get(node), index.get(neighbor)));
            }
        }

        // If node is a root node, pop the stack and generate an SCC
        if (lowlink.get(node).equals(index.get(node))) {
            // choose a representative name (lexicographically smallest)
            List<String> members = new java.util.ArrayList<>();
            while (true) {
                String popped = stack.pop();
                onStack.remove(popped);
                members.add(popped);
                if (popped.equals(node)) break;
            }

            // Assign all members to the same component
            String rep = members.stream().min(String::compareTo).orElse(node);
            for (String m : members) {
                componentOf.put(m, rep);
            }
        }
    }

    /**
     * Counts the number of distinct paths from the start component to the
     * target component that visit all required components at least once.
     * @return the number of valid paths
     */
    long countPaths() {
        // DP on DAG with state (componentName, requiredSeenSet)
        Map<String, Long> memo = new HashMap<>();
        return countFrom(startComponent, Set.of(), memo);
    }

    /**
     * Recursive helper to count paths from a given component with a set of
     * required components already seen.
     * @param componentName the current component name
     * @param requiredSeen the set of required components seen so far
     * @param memo memoization map to store intermediate results
     * @return the number of valid paths from the current component
     */
    private long countFrom(String componentName, Set<String> requiredSeen, Map<String, Long> memo) {
        // Add any required nodes contained in this component
        Set<String> nextSeen = new HashSet<>(requiredSeen);
        nextSeen.addAll(nodes.get(componentName).containsRequired);

        // Check if already computed
        String key = memoKey(componentName, nextSeen);
        if (memo.containsKey(key)) {
            return memo.get(key);
        }

        // If at target, check if all required have been seen
        if (componentName.equals(targetComponent)) {
            long ways = nextSeen.containsAll(requiredComponentNames) ? 1L : 0L;
            memo.put(key, ways);
            return ways;
        }

        // Recurse for all outgoing edges
        long total = 0;
        for (String nextName : nodes.get(componentName).outgoing) {
            total += countFrom(nextName, nextSeen, memo);
        }
        memo.put(key, total);
        return total;
    }

    /**
     * Generate a unique key for memoization based on the current component
     * and the set of components seen so far.
     * @param componentName the current component name
     * @param seen the set of components seen so far
     * @return a unique string key for memoization
     */
    private String memoKey(String componentName, Set<String> seen) {
        if (seen.isEmpty()) {
            return componentName + "|";
        }
        
        String seenStr = seen.stream().sorted()
            .reduce("", (acc, name) -> acc + name + ',', String::concat);
        return componentName + "|" + seenStr;
    }
}