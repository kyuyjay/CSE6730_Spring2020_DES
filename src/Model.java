package sim;

import java.util.*;
import java.lang.Math;

/**
 * Main model that holds all states and processes events.
 */

public class Model {

    final private int TRAVEL_TIME = 2;
    private Params p;
    private Stats s;
    private ArrayList<Generator> gens;
    private ArrayList<Cab> cabs;

    /**
     * Constructs the main Model.
     *
     * @param p model parameters
     * @param s statistics aggregator
     */

    Model(Params p, Stats s) {
        this.p = p;
        this.s = s;
        //Initiatialize model parameters
        initGens(p.numFloors);
        initCabs(p.numShafts);    
        User.maxFloor = p.numFloors;
    }

    /**
     * Initializes future event list by generating the first user for each floor.
     *
     * @return initial future event list
     */

    PriorityQueue<Event> init() {
        System.out.println("Initializing Model");
        PriorityQueue<Event> e = new PriorityQueue<Event>(p.numFloors);
        //Set first user generation event for each floor
        for (int i = 0; i < p.numFloors; i++) {
            User u = new User(0,i+1);
            e.add(new UserGenEvent(0,u));
        }
        return e;
    }

    /**
     * Initializes the required user generators.
     *
     * @param numFloors number of floors
     */

    private void initGens(int numFloors) {
        this.gens = new ArrayList<Generator>(numFloors);
        System.out.println("Setting up generators");
        for (int i = 0; i < numFloors; i++) {
            gens.add(new Generator(50));
        }
        return;
    }

    /**
     * Initializes the required elevator cabs.
     *
     * @param numShafts number of elevator shafts
     */

    private void initCabs(int numShafts) {
        this.cabs = new ArrayList<Cab>(numShafts * 2);
        System.out.println("Setting up cabs");
        for (int i = 0; i < numShafts; i++) {
            for (int j = 0; j < p.numStacked; j++) {
                cabs.add(new Cab(i,j));
            }
        }
        return;
    }

    /**
     * Schedule new user.
     *
     * @param u user to be generated
     * @return user arrival event
     */

    Event gen(User u) {
        //Generate random timestamp
        int timestamp = gens.get(u.src - 1).gen(u.arrTime);
        //Set arrival time to randomly generated time
        u.arrTime = timestamp;
        System.out.println("Scheduling user " + u.id + " at time " + u.arrTime);
        return new UserArrEvent(timestamp,u);
    }

    /**
     * Assign a cab to a user and schedule their exit.
     * Current algorithm naively assigns the first cab to be avialable.
     * If multiple cabs are avialable, assigns the oldest unused cab.
     * Does not account for the bounds of each elevator cab.
     *
     * @param timestamp time user arrives
     * @param u user arriving
     * @return user exit event
     */

    Event assign(int timestamp,User u) {
        Cab assigned = cabs.get(0);
        int min = Integer.MAX_VALUE;
        //Look for next available cab.
        for (int i = 0; i < cabs.size(); i++) {
            Cab cab = cabs.get(i);
            if (cab.nextAvail - timestamp < min) {
                min = cab.nextAvail - timestamp;
                assigned = cab;
            }
        }
        //Time user spends waiting for cab to be free
        int baseTime = Math.max(timestamp,assigned.nextAvail);
        //Time elevator travels, including travelling to pickup user
        int nextTime = baseTime + TRAVEL_TIME * (Math.abs(assigned.curr- u.src) + Math.abs(u.src - u.dest));
        assigned.avail = false;         //Set availability to false
        assigned.nextAvail = nextTime;  //Update next available time
        u.cab = assigned.id;
        System.out.println("Cab " + assigned.id + " assigned to user " + u.id + " from " + u.src + " to " + u.dest + " and reaches destination at " + nextTime);
        s.numIn++;      //Update user arrival stats
        return new UserExitEvent(nextTime,u);
    }

    /**
     * Removes user from the system and computes statistics.
     *
     * @param timestamp time user exits
     * @param u user exiting
     */

    void exit(int timestamp,User u) {
        System.out.println("User " + u.id + " exits at time " + timestamp);
        Cab cab = cabs.get(u.cab);
        cab.curr = u.dest;  //Update cab's current position
        cab.avail = true;   //Set avilability to true
        s.numOut++;         //Update user exit stats
        s.totTime += (timestamp - u.arrTime);   //Update total time stats
        return;
        
    }

}


