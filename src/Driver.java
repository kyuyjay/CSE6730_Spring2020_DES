import sim.*;

/**
 * Main driver file to run simulation.
 * TODO add parameters to configuration file.
 *
 * PARAMETERS:
 *
 * NUM_FLOORS number of floors
 * NUM_SHAFTS number of shafts
 * NUM_STACKED number of cabs per shaft
 * BOUNDS level bounds for each cab
 * RUNTIME total simulation time
 */

public class Driver {

    final static int NUM_FLOORS = 5;
    final static int NUM_SHAFTS = 2;
    final static int NUM_STACKED = 2;
    final static int[] BOUNDS = new int[]{1,5,1,5};
    final static int RUNTIME = 20;

    public static void main(String[] args) {
        sim.Params p = new sim.Params(NUM_FLOORS,NUM_SHAFTS,NUM_STACKED,BOUNDS);
        sim.Stats s = new sim.Stats();
        sim.Engine e = new sim.Engine(p,s);
        e.run(RUNTIME);
        s.printStats();
    }

}

