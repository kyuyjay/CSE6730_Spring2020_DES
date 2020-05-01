package sim;

import java.util.*;

/**
 * Concrete implmentation of User Arrival Event
 */

public class UserArrEvent extends Event {

    /**
     * Construct an arrival event.
     *
     * @param timestamp time of arrival
     * @param u user arriving
     */

    UserArrEvent(int timestamp, User u) {
        super(timestamp,u);
    }

    /**
     * Assigns a cab and triggers exit.
     * Also triggers next user generation event.
     * 
     * @param m model in use
     * @return list of triggered events.
     */

    @Override
    ArrayList<Event> process(Model m) {
        ArrayList<Event> triggeredEvents = new ArrayList<Event>(2);
        //Trigger exit event
        triggeredEvents.add(m.assign(timestamp,u));;
        //Generate next user
        User x = new User(timestamp,u.src);
        triggeredEvents.add(new UserGenEvent(timestamp,x));
        return triggeredEvents;
    }
}


