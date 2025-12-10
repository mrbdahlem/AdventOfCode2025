package day10;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

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
     * @param data raw input data
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
     * @param config 
     * 
     */
    public static void partOne(Configuration[] config) {
        long total = 0;

        for (Configuration machine : config) {
            total += minPresses(machine);
        }
        
        System.out.println("Part 1: {" + total + "}");
    }

    public static int minPresses(Configuration machine) {
        boolean[] lights = new boolean[machine.lightGoal().length];
        int presses = 0;

        int[][] buttons = machine.buttonEffects();

        Queue<MachineOp> ops = new PriorityQueue<>();
        ops.addAll(nextStates(-1, lights, presses, buttons));
        
        while (!lightsMatch(lights, machine.lightGoal())) {
            MachineOp op = ops.poll();
            lights = op.lights();
            presses = op.presses();

            ops.addAll(nextStates(op.button(), lights, presses, buttons));    
        }

        return presses;
    }

    private static Collection<? extends MachineOp> nextStates(int button, boolean[] lights, int presses, int[][] buttons) {
        List<MachineOp> ops = new ArrayList<>();

        for (int b = 0; b < buttons.length; b++) {
            if (b == button) {
                continue;
            }
            boolean[] newLights = lights.clone();
            for (int lightIndex : buttons[b]) {
                newLights[lightIndex] = !newLights[lightIndex];
            }
            ops.add(new MachineOp(b, newLights, presses + 1));
        }

        return ops;
    }

    public static boolean lightsMatch(boolean[] lights, boolean[] goal) {
        if (lights.length != goal.length) {
            return false;
        }

        for (int i = 0; i < lights.length; i++) {
            if (lights[i] != goal[i]) {
                return false;
            }
        }

        return true;
    }
    
    /**
     * @param config 
     * 
     */
    public static void partTwo(Configuration[] config) {
        long total = 0;

        
        for (int i = 0; i < config.length; i++) {
            System.out.println(i);
            Configuration machine = config[i];
            total += minJoltagePresses(machine);
        }
        
        System.out.println("Part 2: {" + total + "}");
    }

    private static long minJoltagePresses(Configuration machine) {
        int[] currentJoltage = new int[machine.joltage().length];
        int presses = 0;
        int[][] buttons = machine.buttonEffects();
        int[] goal = machine.joltage();

        int best = greedyUpperBound(goal, buttons);

        Set<String> visited = new HashSet<>();

        Queue<JoltageOp> ops = new PriorityQueue<>();
        ops.addAll(nextJoltageStates(currentJoltage, goal, presses, buttons, visited));
        
        while (!ops.isEmpty()) {
            JoltageOp op = ops.poll();
            if (!visited.add(op.key())) {
                continue;
            }
            
            int g = op.presses();
            int h = JoltageOp.estimateRemaining(op.joltage(), goal);
            if (g + h >= best) {
                continue;
            }

            if (joltageMatchs(op.joltage(), goal)) {
                return op.presses();
            }
            
            ops.addAll(nextJoltageStates(op.joltage(), goal, op.presses(), buttons, visited));
        }
    
        return -1;
    }
    
    private static boolean joltageMatchs(int[] joltageGoal, int[] joltage) {
        for (int i = 0; i < joltageGoal.length; i++) {
            if (joltageGoal[i] != joltage[i]) {
                return false;
            }
        }
        return true;
    }
    
    private static Collection<? extends JoltageOp> nextJoltageStates(int[] joltage, int[] goal, int presses, int[][] buttons, Set<String> visited) {
        List<JoltageOp> ops = new ArrayList<>();

        int[] deficit = deficits(joltage, goal);

        for (int i = 0; i < buttons.length; i++) {
            boolean helps = false;
            for (int index : buttons[i]) {
                if (deficit[index] > 0) {
                    helps = true;
                    break;
                }
            }
            if (!helps) {
                continue;
            }

            boolean bad = false;
            int[] newJoltage = joltage.clone();
            for (int index : buttons[i]) {
                newJoltage[index] += 1;

                if (newJoltage[index] > goal[index]) {
                    bad = true;
                    break;
                }
            }
            String key = Arrays.toString(newJoltage);
            if (!bad && !visited.contains(key)) {
                ops.add(new JoltageOp(newJoltage, presses + 1, goal, key));
            }
        }

        return ops;            
    }

    private static int greedyUpperBound(int[] goal, int[][] buttons) {
        int[] cur = new int[goal.length];
        int presses = 0;
        // simple greedy: press the button that reduces the current max deficit most
        while (true) {
            int[] deficit = deficits(cur, goal);
            int maxDef = 0;
            for (int d : deficit) if (d > maxDef) maxDef = d;
            if (maxDef == 0) return presses;

            int bestBtn = -1, bestHit = 0;
            for (int i = 0; i < buttons.length; i++) {
                int hit = 0;
                for (int idx : buttons[i]) if (deficit[idx] > 0) hit++;
                if (hit > bestHit) { bestHit = hit; bestBtn = i; }
            }
            if (bestBtn == -1) return Integer.MAX_VALUE; // no progress possible

            for (int idx : buttons[bestBtn]) cur[idx]++;
            presses++;
            // stop if clearly exceeding any goal
            for (int i = 0; i < cur.length; i++) if (cur[i] > goal[i]) return Integer.MAX_VALUE;
        }
    }

    private static int[] deficits(int[] cur, int[] goal) {
        int[] d = new int[cur.length];
        for (int i = 0; i < cur.length; i++) d[i] = goal[i] - cur[i];
        return d;
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
        int sum = 0, max = 0;
        for (int i = 0; i < current.length; i++) {
            int d = goal[i] - current[i];
            if (d > 0) {
                sum += d;
                if (d > max) max = d;
            }
        }

        return (max << 10) + sum;
    }
}