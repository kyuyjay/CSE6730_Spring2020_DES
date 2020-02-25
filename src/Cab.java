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
    int timeToUsr;
    int timeToUsrCollide;

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
        this.shaft = shaft;
        this.pos = pos;
        this.curr = pos + 1; 
        this.nextAvail = 0;
    }

    boolean getAvail(int timestamp) {
        if (timestamp - nextAvail >= 0) {
            return true;
        } else {
            return false;
        }
    }
}

