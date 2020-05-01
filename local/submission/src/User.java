package sim;

import java.util.*;

/**
 * Holds individual user information and statistics.
 */

public class User {

    static int maxFloor = 0;
    static int count = 0;   //Class variable to track user IDs
    RNG rng;
    int id;
    int arrTime;    //Arrival time
    int src;        //Source floor
    int dest;       //Destination floor
    int cab;        //Cab taken

    /**
     * Constructs a user with its parameters.
     *
     * @param arrTime time user arrived
     * @param src floor user arrive on
     */

    User(int arrTime, int src) {
        rng = new RNG();
        this.id = count;
        count++;
        this.arrTime = arrTime;
        this.src = src;
        //Set random destination for user
        this.dest = this.src;
        while (this.src == this.dest) {
            this.dest = rng.nextDest(this.src) + 1;
        } 
    }
}


