package sim;

/**
 * Main statistical aggregator.
 */

public class Stats {

    public int numIn;
    public int numOut;
    public int totTimeWait;
    public int totTime;

    /**
     * Constructs the required stats.
     */

    public Stats() {
        numIn = 0;
        numOut = 0;
        totTimeWait = 0;
        totTime = 0;
    }

    /**
     * Prints the statistics collected.
     * Verbose mode.
     */

    public void printStats() {

        System.out.println("Number of Users Arrived: " + numIn);
        System.out.println("Number of Users Exited: " + numOut);
        System.out.println("Average Time Spent Waiting for Cab: " + (totTimeWait / numIn));
        if (numOut != 0) {
            System.out.println("Average Time Spent Waiting and Travelling: " + (totTime / numOut));
        }
    }

    /**
     * Prints the statistics collected.
     * Non-verbose mode.
     */

    public void printQuiet() {
        if (numOut != 0) {
            System.out.println(numIn + "," + numOut + "," + (totTimeWait / numIn) + "," + (totTime / numOut));
        } else {
            System.out.println(numIn + "," + numOut + "," + (totTimeWait / numIn) + ",0");
        }
    }

}

