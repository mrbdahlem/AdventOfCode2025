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

import javax.naming.Context;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.IntExpr;
import com.microsoft.z3.IntNum;
import com.microsoft.z3.Model;
import com.microsoft.z3.Optimize;

/**
 * Advent of Code 2024 - Day 10
 * @author Brian Dahlem
 * 
 * Light toggling machines with buttons affecting multiple lights.
 * Part 2 adds "joltage" requirements for each machine.
 */
public class Aoc10 {
    public static void main(String[] args) throws FileNotFoundException {
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

    private static long minJoltagePresses(Configuration machine) {
        int[] goal = machine.joltage();
        int[][] buttons = machine.buttonEffects();
        int numButtons = buttons.length;

        // Solve as ILP with Z3: minimize sum(x_j) subject to A*x = goal, x_j >= 0
        try (Context ctx = new Context()) {
            Optimize opt = ctx.mkOptimize();
            IntExpr[] x = new IntExpr[numButtons];
            ArithExpr[] xAsArith = new ArithExpr[numButtons];

            for (int j = 0; j < numButtons; j++) {
                x[j] = ctx.mkIntConst("x" + j);
                xAsArith[j] = x[j];
                opt.Add(ctx.mkGe(x[j], ctx.mkInt(0)));
            }

            int numIndices = goal.length;
            for (int i = 0; i < numIndices; i++) {
                List<ArithExpr> terms = new ArrayList<>();
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

                ArithExpr sum;
                if (terms.size() == 1) {
                    sum = terms.get(0);
                } else {
                    sum = ctx.mkAdd(terms.toArray(new ArithExpr[0]));
                }
                opt.Add(ctx.mkEq(sum, ctx.mkInt(goal[i])));
            }

            ArithExpr total;
            if (xAsArith.length == 1) {
                total = xAsArith[0];
            } else {
                total = ctx.mkAdd(xAsArith);
            }
            opt.MkMinimize(total);

            Status status = opt.Check();
            if (status != Status.SATISFIABLE && status != Status.UNKNOWN) {
                return -1;
            }

            Model model = opt.getModel();
            if (model == null) return -1;
            IntNum val = (IntNum) model.evaluate(total, false);
            return val.getInt64();
        }
    }

}

record Configuration (boolean[] lightGoal, int[][] buttonEffects, int[] joltage) {
    public static Configuration from(String data) {
        String[] parts = data.split("\s+");
        String lightConfig = parts[0].substring(1, parts[0].length() - 1);
        
        boolean[] lightGoal = new boolean[lightConfig.length()];
        for (int i = 0; i < lightConfig.length(); i++) {
            lightGoal[i] = lightConfig.charAt(i) == '#';
        }

        int[][] buttonEffects = new int[parts.length - 2][];
        for (int i = 1; i < parts.length - 1; i++) {
            String[] effectParts = parts[i].substring(1, parts[i].length() - 1).split(",");
            buttonEffects[i - 1] = new int[effectParts.length];
            for (int j = 0; j < effectParts.length; j++) {
                buttonEffects[i - 1][j] = Integer.parseInt(effectParts[j]);
            }
        }

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

record MachineOp (int button, boolean[] lights, int presses) implements Comparable<MachineOp> {
    @Override
    public int compareTo(MachineOp other) {
        return Integer.compare(this.presses, other.presses);
    }
}

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