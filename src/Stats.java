package sim;

/**
 * Main statistical aggregator.
 */

public class Stats {

    public int numIn;
    public int numOut;
    public int totTime;

    /**
     * Constructs the required stats.
     */

    public Stats() {
        numIn = 0;
        numOut = 0;
        totTime = 0;
    }

    /**
     * Prints the statistics collected.
     */

    public void printStats() {
        System.out.println("Number of Users Arrived: " + numIn);
        System.out.println("Number of Users Exited: " + numOut);
        System.out.println("Average Time Spent Waiting and Travelling: " + (totTime / numOut));
    }

}

