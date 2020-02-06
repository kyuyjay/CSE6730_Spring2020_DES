package sim;

import java.util.*;

/**
 * Abstract class for event polymorphism.
 */

public abstract class Event implements Comparable<Event>{

    int timestamp;
    User u;

    /**
     * Main constructor for all events.
     */

    Event(int timestamp,User u) {
        this.timestamp = timestamp;
        this.u = u;
    }

    /**
     * Comparison used for priority queue.
     *
     * @param o event to be compared to
     * @return -1, 1, 0, if less than, more than, equals to other event respectively
     */

    @Override
    public int compareTo(Event o) {
        if (this.timestamp < o.timestamp) {
            return -1;
        } else if (this.timestamp > o.timestamp) {
            return 1;
        } else {
            if (this.u.id < o.u.id) {
                return -1;
            } else if (this.u.id > o.u.id) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Comparison used for priority queue.
     *
     * @param obj object to be compared to
     * @return true if same event, false otherwise
     */

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Event)) {
            return false;
        }
        Event o = (Event) obj;
        if (this.timestamp == o.timestamp) {
            if (this.u.id != o.u.id) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Event processing specific to event type.
     *
     * @param m model to use for processing
     * @return list of triggered events
     */

    abstract ArrayList<Event> process(Model m);

}

