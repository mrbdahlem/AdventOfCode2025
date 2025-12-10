package day10;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.IntSort;
import com.microsoft.z3.Model;
import com.microsoft.z3.Optimize;
import com.microsoft.z3.Status;

/**
 * Advent of Code 2024 - Day 10
 * @author Brian Dahlem
 * 
 * Light toggling machines with buttons affecting multiple lights.
 * Part 2 adds "joltage" requirements for each machine.
 */
public class Aoc10 {
    // Ensure the Z3 native library is available when running inside the dev container
    static {
        // Load libz3 (native core) then libz3java (JNI) explicitly from known system locations.
        String z3Core = "/usr/lib/x86_64-linux-gnu/libz3.so";
        String z3Java = "/usr/lib/x86_64-linux-gnu/jni/libz3java.so";
        try {
            System.load(z3Core);
            System.load(z3Java);
        } catch (UnsatisfiedLinkError e) {
            String hint = "Run with -Djava.library.path=/usr/lib/x86_64-linux-gnu/jni:/usr/lib/x86_64-linux-gnu or set LD_LIBRARY_PATH accordingly.";
            throw new RuntimeException("Unable to load Z3 native libraries (tried %s then %s). ".formatted(z3Core, z3Java) + hint, e);
        }
    }

    private static void ensureZ3Loaded() {
        // Touching this method ensures the static block executed; useful for explicit call sites.
    }

    public static void main(String[] args) throws FileNotFoundException {
        ensureZ3Loaded();
        // Load the data
        File inputFile = new File("day10.txt");
        Scanner scan = new Scanner(inputFile);
       
        // Load the entire data as one token
        scan.useDelimiter("\\A");
        String data = scan.next();
        scan.close();

        // Solve the two parts of the puzzle
        long start = System.nanoTime();
        Configuration[] config = prepare(data);
        long prepped = System.nanoTime();
        partOne(config);
        long first = System.nanoTime();
        partTwo(config);
        long sec = System.nanoTime();

        System.out.println("Preparation duration: " + (prepped - start) / 1000 / 1000 + "ms");
        System.out.println("Part 1 duration: " + (first - prepped) / 1000 / 1000 + "ms");
        System.out.println("Part 2 duration: " + (sec - first) / 1000 / 1000 + "ms");
    }

    /**
     * Prepare the input data for processing, convert each line into a machine Configuration
     * @param data raw input data
     * @return array of machine Configurations
     */
    public static Configuration[] prepare(String data) {
        String[] lines = data.split("\\R");
        Configuration[] config = new Configuration[lines.length];
        for (int i = 0; i < lines.length; i++) {
            config[i] = Configuration.from(lines[i]);
        }

        return config;
    }

    /**
     * Determine the correct number of button presses for each machine to reach its light goal
     * @param config array of machine Configurations 
     */
    public static void partOne(Configuration[] config) {
        long total = 0;

        // Sum minimum presses for each machine
        for (Configuration machine : config) {
            total += minPresses(machine);
        }
        
        System.out.println("Part 1: {" + total + "}");
    }

    /**
     * Determine the minimum button presses to reach the light goal for a machine
     * @param machine the machine Configuration
     * @return minimum number of button presses needed to set the lights to the goal
     */
    public static int minPresses(Configuration machine) {
        int presses = 0;

        boolean[] lights = new boolean[machine.lightGoal().length]; // all off initially
        int[][] buttons = machine.buttonEffects(); // the lights each button toggles
        Queue<MachineOp> ops = new PriorityQueue<>(); // operations to explore
        ops.addAll(nextStates(-1, lights, presses, buttons)); // inital button presses
        
        // Explore operations until we reach the goal
        while (!lightsMatch(lights, machine.lightGoal())) {
            // Get the results of the next operation
            MachineOp op = ops.poll();
            lights = op.lights();
            presses = op.presses();

            // Add next possible operations to explore
            ops.addAll(nextStates(op.button(), lights, presses, buttons));    
        }

        return presses;
    }

    /**
     * Generate the next possible machine operations from the current state
     * @param button the button to press
     * @param lights current light configuration
     * @param presses number of presses so far
     * @param buttons the button effects on lights
     * @return collection of next possible machine operations
     */
    private static Collection<? extends MachineOp> nextStates(int button, boolean[] lights, int presses, int[][] buttons) {
        List<MachineOp> ops = new ArrayList<>();

        // Try pressing each button (except the last pressed which would just undo it)
        for (int b = 0; b < buttons.length; b++) {
            if (b == button) {
                continue;
            }
            boolean[] newLights = lights.clone();

            // Toggle the lights affected by this button
            for (int lightIndex : buttons[b]) {
                newLights[lightIndex] = !newLights[lightIndex];
            }

            ops.add(new MachineOp(b, newLights, presses + 1));
        }

        return ops;
    }

    /**
     * Check if the current light configuration matches the goal
     * @param lights current light configuration
     * @param goal target light configuration
     * @return true if the configurations match, false otherwise
     */
    public static boolean lightsMatch(boolean[] lights, boolean[] goal) {
        return Arrays.equals(lights, goal);
    }
    
    /**
     * Determine the minimum number of button presses for each machine 
     * to match its joltage requirements
     * @param config array of machine Configurations 
     */
    public static void partTwo(Configuration[] config) {
        long total = 0;

        // Sum minimum presses for each machine
        for (int i = 0; i < config.length; i++) {
            System.out.println(i);
            total += minJoltagePresses(config[i]);
        }
        
        System.out.println("Part 2: {" + total + "}");
    }

    /**
     * Determine the minimum button presses to reach the joltage goal for a machine
     * @param machine the machine Configuration
     * @return 
     */
    @SuppressWarnings("unchecked")
    private static long minJoltagePresses(Configuration machine) {
        int[] goal = machine.joltage();
        int[][] buttons = machine.buttonEffects();
        int numButtons = buttons.length;

        // Solve as ILP with Z3: minimize sum(x_j) subject to A*x = goal, x_j >= 0
        // where A_ij = 1 if button j affects index i, else 0
        try (Context ctx = new Context()) {
            Optimize opt = ctx.mkOptimize();
            IntExpr[] x = new IntExpr[numButtons];
            IntExpr[] xAsInts = new IntExpr[numButtons];

            // Variables: number of presses for each button
            for (int j = 0; j < numButtons; j++) {
                x[j] = ctx.mkIntConst("x" + j);
                xAsInts[j] = x[j];
                BoolExpr geZero = ctx.mkGe(x[j], ctx.mkInt(0));
                opt.Add(new BoolExpr[] { geZero });
            }

            // Constraints: for each index, sum of affecting buttons = goal at that index
            int numIndices = goal.length;
            for (int i = 0; i < numIndices; i++) {
                List<IntExpr> terms = new ArrayList<>();
                // Find buttons that affect this index
                for (int j = 0; j < numButtons; j++) {
                    for (int idx : buttons[j]) {
                        if (idx == i) {
                            terms.add(x[j]);
                            break;
                        }
                    }
                }

                // If no button affects this index, only solvable if goal is zero
                if (terms.isEmpty()) {
                    if (goal[i] != 0) {
                        return -1;
                    }
                    continue;
                }

                // Create equality constraint for this index
                ArithExpr<IntSort> sum;
                if (terms.size() == 1) {
                    sum = terms.get(0);
                } else {
                    sum = ctx.mkAdd(terms.toArray(new IntExpr[0]));
                }
                BoolExpr equality = ctx.mkEq(sum, ctx.mkInt(goal[i]));
                opt.Add(new BoolExpr[] { equality });
            }

            ArithExpr<IntSort> total;
            if (xAsInts.length == 1) {
                total = xAsInts[0];
            } else {
                total = ctx.mkAdd(xAsInts);
            }
            opt.MkMinimize(total);

            // Solve the ILP
            Status status = opt.Check();
            if (status != Status.SATISFIABLE && status != Status.UNKNOWN) {
                return -1;
            }

            // Extract the minimum total presses from the model
            Model model = opt.getModel();
            if (model == null) return -1;
            IntNum val = (IntNum) model.evaluate(total, false);
            return val.getInt64();
        }
    }

}

/**
 * Configuration of a light toggling machine
 * @param lightGoal target light configuration
 * @param buttonEffects lights affected by each button
 * @param joltage joltage requirements for the machine
 */
record Configuration (boolean[] lightGoal, int[][] buttonEffects, int[] joltage) {
    /**
     * Create a Configuration from a raw data string
     * @param data raw data string
     * @return the Configuration represented by the data
     */
    public static Configuration from(String data) {
        String[] parts = data.split("\s+");
        String lightConfig = parts[0].substring(1, parts[0].length() - 1);
        
        // Parse light goal configuration
        boolean[] lightGoal = new boolean[lightConfig.length()];
        for (int i = 0; i < lightConfig.length(); i++) {
            lightGoal[i] = lightConfig.charAt(i) == '#';
        }

        // Parse button effects for each button
        int[][] buttonEffects = new int[parts.length - 2][];
        for (int i = 1; i < parts.length - 1; i++) {
            String[] effectParts = parts[i].substring(1, parts[i].length() - 1).split(",");
            buttonEffects[i - 1] = new int[effectParts.length];
            for (int j = 0; j < effectParts.length; j++) {
                buttonEffects[i - 1][j] = Integer.parseInt(effectParts[j]);
            }
        }

        // Parse joltage requirements
        String joltageSpec = parts[parts.length - 1];
        joltageSpec = joltageSpec.substring(1, joltageSpec.length() - 1);
        String[] joltageParts = joltageSpec.split(",");
        int[] joltage = new int[joltageParts.length];
        for (int i = 0; i < joltageParts.length; i++) {
            joltage[i] = Integer.parseInt(joltageParts[i]);
        }
        
        return new Configuration(lightGoal, buttonEffects, joltage);
    }
}

/**
 * Represents a machine operation state for searching
 * @param button the last button pressed
 * @param lights current light configuration
 * @param presses number of presses so far
 */
record MachineOp (int button, boolean[] lights, int presses) implements Comparable<MachineOp> {
    @Override
    public int compareTo(MachineOp other) {
        return Integer.compare(this.presses, other.presses);
    }
}

/**
 * Represents a joltage operation state for searching
 * @param joltage current joltage configuration
 * @param presses number of presses so far
 * @param goal target joltage configuration
 * @param key unique key for this state
 */
record JoltageOp (int[] joltage, int presses, int[] goal, String key) implements Comparable<JoltageOp> {
    @Override
    public int compareTo(JoltageOp other) {
        int thisScore = this.presses + estimateRemaining(this.joltage, this.goal);
        int otherScore = other.presses + estimateRemaining(other.joltage, other.goal);
        return Integer.compare(thisScore, otherScore);
    }

    static int estimateRemaining(int[] current, int[] goal) {
        // For each index, find min presses of any single button that touches it
        // Sum those mins as a lower bound
        int bound = 0;
        for (int i = 0; i < current.length; i++) {
            int deficit = goal[i] - current[i];
            if (deficit <= 0) continue;
            
            // Find cheapest button (touches fewest other indices)
            // to cover this deficit
            int minCost = deficit; // worst case: no button helps
            // (in reality, at least one button touches each index)
            
            bound += minCost;
        }
        return bound;
    }
}