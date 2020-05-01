package sim;

/**
 * Holds all input parameters. 
 * Config file option to be implemented to remove need for recompilation.
 */

public class Params {

    int numFloors;
    int numShafts;
    int numStacked;
    int[] bounds;

    /**
     * Construct parameters
     *
     * @param numFloors number of floors
     * @param numShafts number of shafts
     * @param numStacked number of cabs in each shaft
     * @param bounds level bounds for each cab
     */

    public Params(int numFloors, int numShafts, int numStacked, int[] bounds) {
        this.numFloors = numFloors;
        this.numShafts = numShafts;
        this.numStacked = numStacked;
        this.bounds = bounds;
    }

}

