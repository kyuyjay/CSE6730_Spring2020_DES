package sim;

/**
 * Holds state variables for generators.
 */

public class Generator {

    static int count = 0;
    int id;
    RNG rng;

    /**
     * Constructs a random generator.
     *
     * @param mean average for generator. Not yet implemented.
     */

    Generator() {
        id = count;
        count++;
        rng = new RNG();
    }

    /**
     * Returns a random number with a triangular distribution
     *
     * @param currTime current time
     * @return random time after current time
     */

    int gen(int currTime) {
        return rng.nextTime() + currTime;
    }
        
}

