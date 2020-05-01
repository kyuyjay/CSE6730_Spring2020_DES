import sim.*;

/**
 * Main driver file to run simulation.
 *
 * PARAMETERS:
 *
 * NUM_FLOORS number of floors
 * NUM_SHAFTS number of shafts
 * NUM_STACKED number of cabs per shaft
 * BOUNDS level bounds for each cab
 * RUNTIME total simulation time
 * ALGO algorithm to run
 */

public class Driver {

    final static int NUM_FLOORS = 21;
    final static int NUM_SHAFTS = 6;
    final static int NUM_STACKED = 2;
    final static int[] BOUNDS = new int[]{1,12,2,21};
    final static int RUNTIME = 3600;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: Driver [ALGO] [VERBOSE]");
            System.exit(1);
        }
        int algo = Integer.parseInt(args[0]);
        boolean v = true;
        if (Integer.parseInt(args[1]) == 0) {
            v = false;
        }
        sim.Params p = new sim.Params(NUM_FLOORS,NUM_SHAFTS,NUM_STACKED,BOUNDS);
        sim.Stats s = new sim.Stats();
        sim.Engine e = new sim.Engine(p,s,algo,v);
        e.run(RUNTIME);
        if (v) {
            s.printStats();
        } else {
            s.printQuiet();
        }
    }

}

