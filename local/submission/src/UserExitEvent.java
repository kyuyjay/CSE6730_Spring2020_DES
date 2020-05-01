package sim;

import java.util.*;

/**
 * Concrete implmentation of User Exit Event
 */

public class UserExitEvent extends Event {

    /**
     * Construct an exit event.
     *
     * @param timestamp time of exit
     * @param u user exiting
     */

    UserExitEvent(int timestamp,User u) {
        super(timestamp,u);
    }

    /**
     * Exits the system.
     * 
     * @param m model in use
     * @return null
     */

    @Override
    ArrayList<Event> process(Model m) {
        m.exit(timestamp,u);
        return null;
    }
}

