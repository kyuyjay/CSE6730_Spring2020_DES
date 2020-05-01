package sim;

import java.util.*;
import java.util.stream.Collectors;
import java.lang.Math;

/**
 * Main model that holds all states and processes events.
 */

public class Model {

    public int algo;

    final private int TRAVEL_TIME = 2;
    private boolean v;
    private Params p;
    private Stats s;
    private ArrayList<Generator> gens;
    private ArrayList<Cab> cabs;

    // Current state variables
    private User curr_u;
    private int curr_time;

    /**
     * Constructs the main Model.
     *
     * @param p model parameters
     * @param s statistics aggregator
     */

    Model(Params p, Stats s, int algo, boolean v) {
        this.v = v;
        this.p = p;
        this.s = s;
        this.algo = algo;
        //Initiatialize model parameters
        initGens(p.numFloors);
        initCabs(p.numShafts);    
        User.maxFloor = p.numFloors;
    }


    //////// Initialization Functions ////////
    
    /**
     * Initializes future event list by generating the first user for each floor.
     *
     * @return initial future event list
     */

    PriorityQueue<Event> init() {
        if (v) {
            if (v) {
                System.out.println("Initializing Model");
            }
        }
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
        if (v) {
            System.out.println("Setting up generators");
        }
        for (int i = 0; i < numFloors; i++) {
            gens.add(new Generator());
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
        if (v) {
            System.out.println("Setting up cabs");
        }
        for (int i = 0; i < numShafts; i++) {
            for (int j = 0; j < p.numStacked; j++) {
                cabs.add(new Cab(i,j));
            }
        }
        return;
    }
    ////////    ////////

    //////// Event Processing Functions ////////

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
        if (v) {
            System.out.println("Scheduling user " + u.id + " at time " + u.arrTime);
        }
        return new UserArrEvent(timestamp,u);
    }


    /**
     * Assign a cab to a user and schedule their exit.
     * Current algorithm naively assigns the first cab to be avialable.
     * If multiple cabs are avialable, assigns the oldestt unused cab.
     * Does not account for the bounds of each elevator cab.
     *
     * @param timestamp time user arrives
     * @param u user arriving
     * @return user exit event
     */

    Event assign(int timestamp, User u) {
        curr_u = u;
        curr_time = timestamp;
        Cab assigned = cabs.get(0);
        Optional<Cab> possible;
        switch(algo) {
            // Algorithm avoids collision
            case 1:
                possible = cabs.stream()
                    .filter(cab -> checkBounds(cab))                // Filter for eligible cabs
                    .map(cab -> findTimeToUsr(cab))                 // Evaluate cost
                    .filter(cab -> isCollide(cab,curr_time))        // Avoid collision
                    .min(new Comparator<Cab>() {
                        @Override
                        // Cost by time to user
                        public int compare(Cab o1, Cab o2) {
                            if (o1.timeToUsr < o2.timeToUsr) {
                                return -1;
                            } else if (o1.timeToUsr > o2.timeToUsr) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    });
                if (possible.isPresent()) {
                    assigned = possible.get(); 
                } else {                                            // Collision unavoidable
                    possible = cabs.stream()
                        .filter(cab -> checkBounds(cab))
                        .map(cab -> findTimeToUsr(cab))
                        .min(new Comparator<Cab>() {
                            @Override
                            public int compare(Cab o1, Cab o2) {
                                if (o1.timeToUsr < o2.timeToUsr) {
                                    return -1;
                                } else if (o1.timeToUsr > o2.timeToUsr) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            }
                        });
                    assigned = possible.get();
                }
                break;
            // Algorithm accounts for collision in cost
            case 2:
                possible = cabs.stream()
                    .filter(cab -> checkBounds(cab))        // Filter for eligible cabs
                    .map(cab -> findTimeToUsr(cab))         // Evaluate cost
                    .min(new Comparator<Cab>() {
                        @Override
                        // Cost by time to user including collision
                        public int compare(Cab o1, Cab o2) {
                            if (o1.timeToUsrCollide < o2.timeToUsrCollide) {
                                return -1;
                            } else if (o1.timeToUsrCollide > o2.timeToUsrCollide) {
                                return 1;
                            } else {
                                return 0;
                            }
                        }
                    });
                assigned = possible.get(); 
                break;
        }

        // Time elevator travels, including travelling to pickup user
        int nextTime = 0;
        Cab other = otherCab(assigned);
        // Time to destination equals to the time to reach user added to the time to traverse to destination
        nextTime = assigned.timeToUsrCollide + Math.abs(curr_u.dest - curr_u.src) * TRAVEL_TIME;  
        // Move other cab if in the way
        // First check if collision occurs when assigned cab is free
        if (!isCollide(assigned,Math.max(curr_time,assigned.nextAvail))) {
            // Move other cab when assigned cab is available
            if (other.pos == 1) {
                // Move to one above for upper cab
                int curr = Math.max(curr_u.src,curr_u.dest) + 1;
                if (other.dest < curr) {
                    other.nextAvail = curr_time + Math.abs(curr - other.dest) * TRAVEL_TIME;
                    other.dest = curr;
                } 
            } else {
                // Move to one below for lower cab
                int curr = Math.min(curr_u.src,curr_u.dest) - 1;
                if (other.dest > curr) {
                    other.nextAvail = curr_time + Math.abs(curr - other.dest) * TRAVEL_TIME;
                    other.dest = curr;
                } 
            }
        } else { 
            // Move other cab when it is available
            other.nextAvail = other.nextAvail + (Math.abs(other.dest - curr_u.dest) + 1) * TRAVEL_TIME;
            if (other.pos == 1) {
                other.dest = curr_u.dest + 1;
            } else {
                other.dest = curr_u.dest - 1;
            }
        }
        // Update state
        assigned.dest = curr_u.dest;
        assigned.nextAvail = nextTime;
        curr_u.cab = assigned.id;
        if (v) {
            System.out.println("Cab " + assigned.id + " assigned to user " + u.id + " from " + u.src + " to " + u.dest + " and reaches destination at " + nextTime);
        }
        // Update stats
        s.numIn++;
        s.totTimeWait += assigned.timeToUsrCollide - curr_time;
        return new UserExitEvent(nextTime,u);
    }

    /**
     * Removes user from the system and computes statistics.
     *
     * @param timestamp time user exits
     * @param u user exiting
     */

    void exit(int timestamp,User u) {
        if (v) {
            System.out.println("User " + u.id + " exits at time " + timestamp);
        }
        Cab cab = cabs.get(u.cab);
        cab.curr = u.dest;  //Update cab's current position
        s.numOut++;         //Update user exit stats
        s.totTime += (timestamp - u.arrTime);   //Update total time stats
        return;

    }

    ////////    ////////


    //////// Utility Functions ////////

    /**
     * Checks if cab is moving within its set bounds.
     * Only works for 2 cabs in a shaft.
     *
     * @param cab cab to check
     * @return true if cab is within bounds, false if otherwise
     */

    boolean checkBounds(Cab cab) {
        int pos_bound = cab.pos * 2;
        if (curr_u.src >= p.bounds[pos_bound] && curr_u.src <= p.bounds[pos_bound + 1]) {
            if (curr_u.dest >= p.bounds[pos_bound] && curr_u.dest <= p.bounds[pos_bound + 1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Looks for other cab in the same shaft.
     *
     * @param cab cab to check
     * @return other cab in shaft
     */

    Cab otherCab(Cab cab) {
        Cab sameShaft;
        if (cab.pos == 0) {
            sameShaft = cabs.get(cab.shaft * 2 + 1);
        } else {
            sameShaft = cabs.get(cab.shaft * 2);
        }
        return sameShaft;
    }

    /**
     * Check if cab will collide with other cabs in the same shaft.
     *
     * @param cab cab to check
     * @param time time to check
     * @return true if collision occurs, false if otherwise
     */

    boolean isCollide(Cab cab, int time) {
        Cab sameShaft = otherCab(cab);
        // Check if cab is free to move
        if (sameShaft.getAvail(time)) {
            return false;
        }
        // If not free to move, check if both source and destination is intercepted
        if (cab.pos == 0){
            if (curr_u.src < sameShaft.dest && curr_u.dest < sameShaft.dest) {
                return false;
            }
        }
        else{
            if (curr_u.src > sameShaft.dest && curr_u.dest > sameShaft.dest){
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the time it takes to the user, with and without collision.
     * 
     * @param cab cab to check
     * @return cab with updated times
     */

    Cab findTimeToUsr(Cab cab) {
        Cab other = otherCab(cab);
        // If cab is currently available, use current time
        if (cab.getAvail(curr_time)){
            // Check if cab will collide given current state
            if (!isCollide(cab,curr_time)) {
                cab.timeToUsrCollide = curr_time + (TRAVEL_TIME * Math.abs(cab.dest - curr_u.src));
            } else {
                // Wait for other cab to be free
                // Next available time for other is always more as collision was triggered
                cab.timeToUsrCollide = other.nextAvail + (TRAVEL_TIME * Math.abs(cab.dest - curr_u.src)); 
            }
            cab.timeToUsr = curr_time + (TRAVEL_TIME * Math.abs(cab.dest - curr_u.src));
        }
        // If cab is currently unavailable, need to offload passengers first, use next available time
        else {
            if (!isCollide(cab,cab.nextAvail)) {
                cab.timeToUsrCollide = cab.nextAvail + (TRAVEL_TIME * Math.abs(cab.dest - curr_u.src));
            } else {
                // Wait for other cab to be free
                // Next available time for other is always more as collision was triggered
                cab.timeToUsrCollide = other.nextAvail + (TRAVEL_TIME * Math.abs(cab.dest - curr_u.src)); 
            }
            cab.timeToUsr = cab.nextAvail + (TRAVEL_TIME * Math.abs(cab.dest - curr_u.src));
        }   
        return cab;
    }

    ////////    ////////

}


// for (Iterator<Cab> j = possible.iterator(); j.hasNext();) {
//     Cab curr = j.next();
//     if (curr.avail == true){
//         if isCollide(curr,u.src, u.destt);
//             assigned = curr;
//             break;
//         else{

//         }
//     }
//     assigned = possible[0]


// // find avaibale shaft
// int[] avail = new int[p.numShafts];
// for (int i=0; i < cabs.size(); i++) {
//     Cab curr = cabs.get(i);
//     if (!curr.avail){
//         avail[curr.shaft]++;
//     }
// }

// List<Cab> possible = new ArrayList<Cab>();
// if (u.src > p.bounds[1]){
//     // empty shaft
//     possible = cabs.stream()
//         .filter(cab -> avail[cab.shaft] == 0)
//         .filter(cab -> cab.pos == 1)
//         .collect(Collectors.toList());
//     if (possible.size() > 0) {
//         int minDist = Integer.MAX_VALUE;
//         for (Iterator<Cab> j = possible.iterator(); j.hasNext();) {
//             Cab curr = j.next();
//             if (Math.abs(curr.curr - u.src) < minDist) {
//                 minDist = curr.curr - u.src;
//                 assigned = curr;
//             }
//         }
//     }
//     else{   // no empty shaft

//         // upper cab is avaiable
//         ArrayList<Cab> possible2 = new ArrayList();
//         possible2 = cabs.stream()
//             .filter(cab -> cab.avail)
//             .filter(cab -> cab.pos == 1)
//             .toList();

//         if (possible2.size() > 0){
//             // lower cab is not affected the use of upper cab
//             ArrayList<Cab> possible3 = new ArrayList();
//             possible3 = possible2.stream()
//                 .filter(cab -> checkBounds(cab,u.destt))
//                 .toList();
//             if (possible3.size() > 0){
//                 int minDist = Integer.MAX_VALUE;
//                 for (Iterator<Cab> j = possible2.iterator(); j.hasNext();){
//                     Cab curr = j.next();
//                     if (Math.abs(curr.curr - u.src) < minDist) {
//                         minDist = curr.curr - u.src;
//                         assigned = curr;
//                     }
//                 }    
//             }

//             else {   // lower cab is on the way of the use of upper cab

//                 // find min |cabL.curr-cabL.destt|+|u.des-cabL.dest| 
//                 int minDist = Integer.MAX_VALUE;
//                 for (int i =0; i < p.numShafts; i++){
//                     dist = Math.abs(cabs[i*p.numStacked].nextAvail)+Math.abs(u.destt-cabs[i*p.numStacked].dest);
//                     if (dist < minDist){
//                         minDist = dist;
//                         assigned = cabs[i*p.numStacked+1];
//                     }
//                 }
//             }
//         }
//         else{ // upper cab is not avaiable



//             // minDist max(distL, distU)
//             int minDist = Integer.MAX_VALUE;
//             for (int i =0; i < p.numShafts; i++){
//                 if (cabs[i*p.numStacked].avail == true) {
//                     if (cabs[i*p.numStacked].curr < u.destt){
//                         distL = 0;
//                     }
//                     else{
//                         distL = Math.abs(u.destt-cabs[i*p.numStacked].dest);
//                     }
//                 }
//                 else {
//                     distL = Math.abs(cabs[i*p.numStacked].nextAvail)+Math.abs(u.destt-cabs[i*p.numStacked].dest);
//                 }

//                 distU = Math.abs(cabs[i*p.numStacked+1].nextAvail)+Math.abs(u.src-cabs[i*p.numStacked+1].destt);
//                 dist = Math.max(distL, distU);
//                 if (dist < minDist){
//                     minDist = dist;
//                     assigned = cabs[i*p.numStacked+1];
//                 }
//             }
//         }
//     }
// }

// else{
//     // user in first floor
//     if {u.src = p.bounds[0]}{
//         // emptey shaft
//         possible = cabs.stream()
//             .filter(cab -> avail[cab.shaft] == 0)
//             .filter(cab -> cab.pos == 0)
//             .collect(Collectors.toList());
//         if (possible.size > 0){
//             int minDist = Integer.MAX_VALUE;
//             for (Iterator<Cab> j = possible.iterator(); j.hasNext();) {
//                 Cab curr = j.next();
//                 if (Math.abs(curr.curr - u.src) < minDist) {
//                     minDist = curr.curr - u.src;
//                     assigned = curr;
//                 }                       
//             }
//         }
//         else{ // no empty shaft
//             // lower cab is avaiable
//             ArrayList<Cab> possible2 = new ArrayList();
//             possible2 = cabs.stream()
//                 .filter(cab -> cab.avail)
//                 .filter(cab -> cab.pos == 0)
//                 .toList();

//             if (possible2.size() > 0){
//                 // upper cab is not affected the use of lower cab
//                 ArrayList<Cab> possible3 = new ArrayList();
//                 possible3 = possible2.stream()
//                     .filter(cab -> checkBounds(cab,u.destt))
//                     .toList();
//                 if (possible3.size() > 0){
//                     int minDist = Integer.MAX_VALUE;
//                     for (Iterator<Cab> j = possible2.iterator(); j.hasNext();){
//                         Cab curr = j.next();
//                         if (Math.abs(curr.curr - u.src) < minDist) {
//                             minDist = curr.curr - u.src;
//                             assigned = curr;
//                         }
//                     }    
//                 }

//                 else {   // upper cab is on the way of the use of lower cab

//                     // find min |cabU.curr-cabU.dest|+|u.des-cabU.des|
//                     int minDist = Integer.MAX_VALUE;
//                     for (int i =0; i < p.numShafts; i++){
//                         dist = Math.abs(cabs[i*p.numStacked+1].nextAvail)+Math.abs(u.destt-cabs[i*p.numStacked+1].dest);
//                         if (dist < minDist){
//                             minDist = dist;
//                             assigned = cabs[i*p.numStacked];
//                         }
//                     }
//                 }
//             }
//             else{ // lower cab is not avaiable

//                 // minDist max(distL, distU)
//                 int minDist = Integer.MAX_VALUE;
//                 for (int i =0; i < p.numShafts; i++){
//                     if (cabs[i*p.numStacked+1].avail == true) {
//                         if (cabs[i*p.numStacked+1].curr < u.destt){
//                             distU = 0;
//                         }
//                         else{
//                             distU = Math.abs(u.destt-cabs[i*p.numStacked+1].dest);
//                         }
//                     }
//                     else {
//                         distU = Math.abs(cabs[i*p.numStacked+1].nextAvail)+Math.abs(u.destt-cabs[i*p.numStacked+1].dest);
//                     }
//                     distL = Math.abs(cabs[i*p.numStacked].nextAvail)+Math.abs(u.src-cabs[i*p.numStacked].destt);

//                     dist = Math.max(distL, distU);
//                     if (dist < minDist){
//                         minDist = dist;
//                         assigned = cabs[i*p.numStacked];
//                     }
//                 }
//             }
//         }
//     }
//     else{  // user in 2-11, all cabs are possible
//         // u.destt > 11, U 
//         if (u.destt > p.bounds[1]){
//             // empty shaft
//             possible = cabs.stream()
//                 .filter(cab -> avail[cab.shaft] == 0)
//                 .filter(cab -> cab.pos == 1)
//                 .collect(Collectors.toList());
//             if (possible.size() > 0) {
//                 int minDist = Integer.MAX_VALUE;
//                 for (Iterator<Cab> j = possible.iterator(); j.hasNext();) {
//                     Cab curr = j.next();
//                     if (Math.abs(curr.curr - u.src) < minDist) {
//                         minDist = curr.curr - u.src;
//                         assigned = curr;
//                     }
//                 }
//             }

//             else{   // no empty shaft

//                 // upper cab is avaiable
//                 ArrayList<Cab> possible2 = new ArrayList();
//                 possible2 = cabs.stream()
//                     .filter(cab -> cab.avail)
//                     .filter(cab -> cab.pos == 1)
//                     .toList();

//                 if (possible2.size() > 0){
//                     // lower cab is not affected the use of upper cab
//                     ArrayList<Cab> possible3 = new ArrayList();
//                     possible3 = possible2.stream()
//                         .filter(cab -> checkBounds(cab,u.destt))
//                         .toList();
//                     if (possible3.size() > 0){
//                         int minDist = Integer.MAX_VALUE;
//                         for (Iterator<Cab> j = possible2.iterator(); j.hasNext();){
//                             Cab curr = j.next();
//                             if (Math.abs(curr.curr - u.src) < minDist) {
//                                 minDist = curr.curr - u.src;
//                                 assigned = curr;
//                             }
//                         }    
//                     }

//                     else {   // lower cab is on the way of the use of upper cab

//                         // find min |cabL.curr-cabL.destt|+|u.des-cabL.dest| 
//                         int minDist = Integer.MAX_VALUE;
//                         for (int i =0; i < p.numShafts; i++){
//                             dist = Math.abs(cabs[i*p.numStacked].nextAvail)+Math.abs(u.destt-cabs[i*p.numStacked].dest);
//                             if (dist < minDist){
//                                 minDist = dist;
//                                 assigned = cabs[i*p.numStacked+1];
//                             }
//                         }
//                     }
//                 }
//                 else{ // upper cab is not avaiable

//                     // minDist max(distL, distU)
//                     int minDist = Integer.MAX_VALUE;
//                     for (int i =0; i < p.numShafts; i++){
//                         if (cabs[i*p.numStacked].avail == true) {
//                             if (cabs[i*p.numStacked].curr < u.destt){
//                                 distL = 0;
//                             }
//                             else{
//                                 distL = Math.abs(u.destt-cabs[i*p.numStacked].dest);
//                             }
//                         }
//                         else {
//                             distL = Math.abs(cabs[i*p.numStacked].nextAvail)+Math.abs(u.destt-cabs[i*p.numStacked].dest);
//                         }

//                         distU = Math.abs(cabs[i*p.numStacked+1].nextAvail)+Math.abs(u.src-cabs[i*p.numStacked+1].destt);
//                         dist = Math.max(distL, distU);
//                         if (dist < minDist){
//                             minDist = dist;
//                             assigned = cabs[i*p.numStacked+1];
//                         }
//                     }
//                 }
//             }
//         }
//         // 2<=u.destt <= 11 U/L
//         else{
//             // empty shaft
//             possible = cabs.stream()
//                 .filter(cab -> avail[cab.shaft] == 0)
//                 .collect(Collectors.toList());
//             if (possible.size > 0) {
//                 // find cabs with no collision 
//                 ArrayList<Cab> possible2 = new ArrayList();
//                 possible2 = possible.stream()
//                     .filter(cab -> checkBounds(cab,u.destt))
//                     .toList();
//                 if (possible2.size() > 0) {
//                     int minDist = Integer.MAX_VALUE;
//                     for (Iterator<Cab> j = possible2.iterator(); j.hasNext();){
//                         Cab curr = j.next();
//                         if (Math.abs(curr.curr - u.src) < minDist) {
//                             minDist = curr.curr - u.src;
//                             assigned = curr;
//                         }
//                     }   
//                 }
//                 // cabs in empty shaft all has collision, find minDist (move with another to avoid collision ???????)
//                 else{
//                     int minDist = Integer.MAX_VALUE;
//                     for (Iterator<Cab> j = possible.iterator(); j.hasNext();){
//                         Cab curr = j.next();
//                         if (Math.abs(curr.curr - u.src) < minDist) {
//                             minDist = curr.curr - u.src;
//                             assigned = curr;
//                         }
//                     }   
//                 }    
//             }
//             else { // no empty shaft
//                 // cab avaiable 
//                 ArrayList<Cab> possible3 = new ArrayList();
//                 possible3 = cabs.stream()
//                     .filter(cab -> cab.avail)
//                     .toList();
//                 if (possible3.size() > 0){
//                     //no collision 
//                     ArrayList<Cab> possible4 = new ArrayList();
//                     possible4 = possible3.stream()
//                         .filter(cab -> checkBounds(cab,u.destt))
//                         .toList();
//                     if (possible4.size() > 0){
//                         int minDist = Integer.MAX_VALUE;
//                         for (Iterator<Cab> j = possible4.iterator(); j.hasNext();){
//                             Cab curr = j.next();
//                             if (Math.abs(curr.curr - u.src) < minDist) {
//                                 minDist = curr.curr - u.src;
//                                 assigned = curr;
//                             }
//                         }   
//                     }
//                     else { // collision
//                         int minDist = Integer.MAX_VALUE;
//                         for (int i =0; i < p.numShafts; i++){

//                     }
//                 }

//                 // no cab avaiable
//             }

//         }
//     }

// }    





