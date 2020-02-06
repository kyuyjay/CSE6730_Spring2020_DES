package sim;

import java.util.*;

/**
 * Holds state variables for generators.
 */

public class Generator {

    static int count = 0;
    int id;
    Random r;

    /**
     * Constructs a random generator.
     *
     * @param mean average for generator. Not yet implemented.
     */

    Generator(int mean) {
        id = count;
        count++;
        r = new Random();
    }

    /**
     * Returns a random number with a given distribution.
     *
     * @param currTime current time
     * @return random time after current time
     */

    int gen(int currTime) {
        return currTime + r.nextInt(15);
    }
        
}

