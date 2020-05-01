package sim;

/**
 * Holds state variables for generators.
 */

public class testGenerator {

    static int count = 0;
    int id;
    RNG rng;

    /**
     * Constructs a random generator.
     *
     * @param mean average for generator. Not yet implemented.
     */

    testGenerator() {
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
        return 5 + currTime;
    }
        
}

