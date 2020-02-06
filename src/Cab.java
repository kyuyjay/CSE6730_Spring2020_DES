package sim;

/**
 * Holds state information about a elevator cab.
 */

public class Cab {

    static int count = 0;   //Class variable tracks cab ids
    int id;
    int curr;
    int dest;
    int pos;
    int shaft;
    int nextAvail;
    boolean avail;

    /**
     * Constructs a cab at its initial position.
     *
     * @param shaft shaft number
     * @param pos position in the shaft
     */

    Cab(int shaft, int pos) {
        id = count;
        count++;
        this.dest = 0;
        this.avail = true;
        this.shaft = shaft;
        this.pos = pos;
        this.curr = pos + 1; 
        this.nextAvail = 0;
    }
}

