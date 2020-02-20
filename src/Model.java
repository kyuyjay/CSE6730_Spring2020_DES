package sim;

import java.util.*;
import java.util.stream.Collectors;
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

    void minTime (Cab cab, User u){

        if (cab.avail){
            cab.timeToUsr = Math.abs(cab.curr - u.src)*TRAVEL_TIME;
        }
        else {
            cab.timeToUsr = cab.nextAvail+TRAVEL_TIME * Math.abs(cab.des-u.dest);
        }   
        return;
    }

    void minTime2 (Cab cab, User u){
        Cab sameShaft;
        if (cab.pos == 0) {
            sameShaft = Cabs.get(cab.shaft * 2 + 1);
        } else {
            sameShaft = Cabs.get(cab.shaft * 2);
        }
        if (cab.avail){
            cab.timeToUsr = Math.abs(cab.curr - u.src)*TRAVEL_TIME;
            if (sameShaft.avail){
                sameShaft.timeToUsr = (Math.abs(cab.curr - u.dest)+1)*TRAVEL_TIME;
            }
            else{
                sameShaft.timeToUsr = sameShaft.nextAvail + (Math.abs(sameShaft.dest-u.dest)+1)*TRAVEL_TIME;
            }
        }
        else {
            cab.timeToUsr = cab.nextAvail+TRAVEL_TIME * Math.abs(cab.des-u.dest);
            if (sameShaft.avail){
                sameShaft.timeToUsr = (Math.abs(cab.curr - u.dest)+1)*TRAVEL_TIME;
            }
            else{
                sameShaft.timeToUsr = sameShaft.nextAvail + (Math.abs(sameShaft.dest-u.dest)+1)*TRAVEL_TIME;
            }
        }   
        cab.timeToUsrCollide = cab.timeToUsr + sameShaft.timeToUsr;
        return;
    }

    boolean checkBounds (Cab cab, User u){
        if (u.src == p.bounds[0]){
            return cab.pos == 0;
        }
        else if (u.src > p.bounds[1]){
            return cab.pos == 1;
        }
        else {
            if (u.dest > p.bounds[1]){
                return cab.pos == 1;        
            }
        }

    }

    void isCollide (Cab cab, User u){
        Cab sameShaft;
        if (cab.pos == 0) {
            sameShaft = Cabs.get(cab.shaft * 2 + 1);
        } else {
            sameShaft = Cabs.get(cab.shaft * 2);
        }
        if (cab.pos == 0){
            if (cab.avail){
                if (sameShaft.avail){
                    if (cab.curr < sameShaft.curr){
                        return true;
                    }
                }
                else {
                    if (cab.curr < sameShaft.dest){
                        return true;
                    }
                }
                
            }
            else{
                if (cab.dest < sameShaft.dest){
                    return true;
                }
            }
            
        }
        else{
            if (cab.avail){
                if (sameShaft.avail){
                    if (cab.curr > sameShaft.curr){
                        return true;
                    }
                }
                else {
                    if (cab.curr > sameShaft.dest){
                        return true;
                    }
                }
                
            }
            else{
                if (cab.dest > sameShaft.dest){
                    return true;
                }
            }
        }
    }

    Event assign(int timestamp,User u) {
        // PriorityQueue<Cabs> fastest = new PriorityQueue(numShafts * numStacked)
        // Cab assigned = cabs.get(0);
        // int min = Integer.MAX_VALUE;
        //Look for next available cab.
        // for (int i = 0; i < cabs.size(); i++) {
        //     Cab cab = cabs.get(i);
        //     if (cab.nextAvail - timestamp < min) {
        //         min = cab.nextAvail - timestamp;
        //         assigned = cab;
        //     }
        // }

        // greedy paradigm
        List<Cab> possible = new ArrayList<Cab>();
        possible = cab.stream()
            .filter(new Predicate<Cab>() {
                @Override
                boolean test(Cab cab) {
                    return checkBounds(cab, u);
                }
            })
            .filter(cab -> cab.avail)
            .forEach(new Consumer<Cab>() {
                @Override
                void accept(Cab cab) {
                    minTime(cab, u);
                    return;
                }
            })
            .sorted(new Comparator<Cab>() {
                @Override
                int compare(Cab o1, Cab o2) {
                    if (o1.timeToUsr < o2.timeToUsr) {
                        return -1;
                    } else if (o1.timeToUsr > o2.timeToUsr) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            })
            .collect(Collectors.toList());
        if (possible.size() > 0){
            List<Cab> possible2 = new ArrayList<Cab>();
            possible2 = possible.steam()
                .filter(new Predicate<Cab>() {
                    @Override
                    boolean test(Cab cab) {
                        return isCollide(cab,u);
                    }
                })
                .collect(Collectors.toList());
            if (possible2.size() > 0){
                assigned = possible2[0];
            }
            else {
                assigned = possible[0];
            }
        }
        else {
            possible4 = cab.stream()
                .filter(new Predicate<Cab>() {
                    @Override
                    boolean test(Cab cab) {
                        return checkBounds(cab, u);
                    }
                })
                .forEach(new Consumer<Cab>() {
                    @Override
                    void accept(Cab cab) {
                        minTime(cab, u);
                        return;
                    }
                })
                .sorted(new Comparator<Cab>() {
                    @Override
                    int compare(Cab o1, Cab o2) {
                        if (o1.timeToUsr < o2.timeToUsr) {
                            return -1;
                        } else if (o1.timeToUsr > o2.timeToUsr) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                })
                .collect(Collectors.toList());
            assigned = possible4[0];
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




    Event assign2(int timestamp,User u) {
    
        // Algo 2
        List<Cab> possible = new ArrayList<Cab>();
        possible = cab.stream()
            .filter(new Predicate<Cab>() {
                @Override
                boolean test(Cab cab) {
                    return checkBounds(cab, u.src, u.dest);
                }
            })
            .filter(new Predicate<Cab>() {
                    @Override
                    boolean test(Cab cab) {
                        return isCollide(cab,u.src, u.dest);
                    }
                })
            .forEach(new Consumer<Cab>() {
                @Override
                void accept(Cab cab) {
                    minTime(cab, u.src, u.dest);
                    return;
                }
            })
            .sorted(new Comparator<Cab>() {
                @Override
                int compare(Cab o1, Cab o2) {
                    if (o1.timeToUsr < o2.timeToUsr) {
                        return -1;
                    } else if (o1.timeToUsr > o2.timeToUsr) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            })
            .collect(Collectors.toList());
        if (possible.size() > 0){
            assigned = possible [0];
        }
        else {
            List<Cab> possible2 = new ArrayList<Cab>();
        possible2 = cab.stream()
            .filter(new Predicate<Cab>() {
                @Override
                boolean test(Cab cab) {
                    return checkBounds(cab, u.src, u.dest);
                }
            })
            .forEach(new Consumer<Cab>() {
                @Override
                void accept(Cab cab) {
                    minTime2(cab, u.src, u.dest);
                    return;
                }
            })
            .sorted(new Comparator<Cab>() {
                @Override
                int compare(Cab o1, Cab o2) {
                    if (o1.timeToUsr < o2.timeToUsr) {
                        return -1;
                    } else if (o1.timeToUsr > o2.timeToUsr) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            })
            .collect(Collectors.toList());
        assigned = possible2[0];
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


        // for (Iterator<Cab> j = possible.iterator(); j.hasNext();) {
        //     Cab curr = j.next();
        //     if (curr.avail == true){
        //         if isCollide(curr,u.src, u.dest);
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
        //                 .filter(cab -> checkBounds(cab,u.dest))
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
                        
        //                 // find min |cabL.curr-cabL.dest|+|u.des-cabL.dest| 
        //                 int minDist = Integer.MAX_VALUE;
        //                 for (int i =0; i < p.numShafts; i++){
        //                     dist = Math.abs(cabs[i*p.numStacked].nextAvail)+Math.abs(u.dest-cabs[i*p.numStacked].dest);
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
        //                     if (cabs[i*p.numStacked].curr < u.dest){
        //                         distL = 0;
        //                     }
        //                     else{
        //                         distL = Math.abs(u.dest-cabs[i*p.numStacked].dest);
        //                     }
        //                 }
        //                 else {
        //                     distL = Math.abs(cabs[i*p.numStacked].nextAvail)+Math.abs(u.dest-cabs[i*p.numStacked].dest);
        //                 }
                        
        //                 distU = Math.abs(cabs[i*p.numStacked+1].nextAvail)+Math.abs(u.src-cabs[i*p.numStacked+1].dest);
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
        //                     .filter(cab -> checkBounds(cab,u.dest))
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
                        
        //                     // find min |cabU.curr-cabU.des|+|u.des-cabU.des|
        //                     int minDist = Integer.MAX_VALUE;
        //                     for (int i =0; i < p.numShafts; i++){
        //                         dist = Math.abs(cabs[i*p.numStacked+1].nextAvail)+Math.abs(u.dest-cabs[i*p.numStacked+1].dest);
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
        //                         if (cabs[i*p.numStacked+1].curr < u.dest){
        //                             distU = 0;
        //                         }
        //                         else{
        //                             distU = Math.abs(u.dest-cabs[i*p.numStacked+1].dest);
        //                         }
        //                     }
        //                     else {
        //                         distU = Math.abs(cabs[i*p.numStacked+1].nextAvail)+Math.abs(u.dest-cabs[i*p.numStacked+1].dest);
        //                     }
        //                     distL = Math.abs(cabs[i*p.numStacked].nextAvail)+Math.abs(u.src-cabs[i*p.numStacked].dest);
                            
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
        //         // u.dest > 11, U 
        //         if (u.dest > p.bounds[1]){
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
        //                         .filter(cab -> checkBounds(cab,u.dest))
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
                                
        //                         // find min |cabL.curr-cabL.dest|+|u.des-cabL.dest| 
        //                         int minDist = Integer.MAX_VALUE;
        //                         for (int i =0; i < p.numShafts; i++){
        //                             dist = Math.abs(cabs[i*p.numStacked].nextAvail)+Math.abs(u.dest-cabs[i*p.numStacked].dest);
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
        //                             if (cabs[i*p.numStacked].curr < u.dest){
        //                                 distL = 0;
        //                             }
        //                             else{
        //                                 distL = Math.abs(u.dest-cabs[i*p.numStacked].dest);
        //                             }
        //                         }
        //                         else {
        //                             distL = Math.abs(cabs[i*p.numStacked].nextAvail)+Math.abs(u.dest-cabs[i*p.numStacked].dest);
        //                         }
                                
        //                         distU = Math.abs(cabs[i*p.numStacked+1].nextAvail)+Math.abs(u.src-cabs[i*p.numStacked+1].dest);
        //                         dist = Math.max(distL, distU);
        //                         if (dist < minDist){
        //                             minDist = dist;
        //                             assigned = cabs[i*p.numStacked+1];
        //                         }
        //                     }
        //                 }
        //             }
        //         }
        //         // 2<=u.dest <= 11 U/L
        //         else{
        //             // empty shaft
        //             possible = cabs.stream()
        //                 .filter(cab -> avail[cab.shaft] == 0)
        //                 .collect(Collectors.toList());
        //             if (possible.size > 0) {
        //                 // find cabs with no collision 
        //                 ArrayList<Cab> possible2 = new ArrayList();
        //                 possible2 = possible.stream()
        //                     .filter(cab -> checkBounds(cab,u.dest))
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
        //                         .filter(cab -> checkBounds(cab,u.dest))
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
                  




