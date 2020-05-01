package sim;

import java.util.*;

/**
 * Engine of the simulator.
 * Schedules recieved events.
 * Processes earliest event.
 */

public class Engine {

    private PriorityQueue<Event> e;
    private Params p;
    private Stats s;
    private Model m;

    /**
     * Constructs the main Engine.
     * 
     * @param p model parameters
     * @param s statistics aggregator
     */

    public Engine(Params p, Stats s, int algo, boolean v) {
        this.s = s;
        this.m = new Model(p,s,algo,v);
    }

    /**
     * Runs the engine until a predetermined time.
     *
     * @param endTime time to run the engine until
     */

    public void run(int endTime) {
        e = m.init();
        int time = 0;
        ArrayList<Event> triggeredEvents;
        //Iterate through list until predetermined end time
        while (time < endTime) {
            Event curr = e.poll();  //Pop earliest event
            time = curr.timestamp;  //Advance simulation time
            triggeredEvents = curr.process(m);  //Polymorphic
            if (triggeredEvents != null) {      //Add all triggered events
                triggeredEvents.forEach(event -> {
                    e.add(event);
                });
            }
        }
        return;
    }
    

}




