package sim;

import java.util.*;

/**
 * Holds individual user information and statistics.
 */

public class User {

    static int maxFloor = 0;
    static int count = 0;   //Class variable to track user IDs
    static Random rand = new Random();
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
        this.id = count;
        count++;
        this.arrTime = arrTime;
        this.src = src;
        //Set random destination for user.
        Random r = new Random();
        this.dest = this.src;
        while (this.src == this.dest) {
            this.dest = r.nextInt(maxFloor) + 1;
        }
    }
}


