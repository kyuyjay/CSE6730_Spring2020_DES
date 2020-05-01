package sim;

import java.util.*;

/**
 * Concrete implmentation of User Generation Event
 */

public class UserGenEvent extends Event {

    /**
     * Construct a generation event.
     *
     * @param timestamp time of exit
     * @param u user exiting
     */

    UserGenEvent(int timestamp, User u) {
        super(timestamp,u);
    }

    /**
     * Triggers a user arriving event
     * 
     * @param m model in use
     * @return user arriving event
     */

    @Override
    ArrayList<Event> process(Model m) {
        ArrayList<Event> triggeredEvents = new ArrayList<Event>();
        triggeredEvents.add(m.gen(u));
        return triggeredEvents;
    }
}

