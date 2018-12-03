package java;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

class CompareTime implements Comparator<meetingInfo> {
    public int compare(meetingInfo m1, meetingInfo m2) {
        if (m1.getYear() == m2.getYear()) {
            if (m1.getMonth() == m2.getMonth()) {
                if (m1.getDay() == m2.getYear()) {

                }
                return m1.getYear() - m2.getYear();
            }
            return m1.getMonth() - m2.getMonth();
        }
        return m1.getYear() - m2.getYear();
    }
}

public class Dictionary {
    private TreeSet<meetingInfo> timeOrderedSet;
    private HashMap<String, meetingInfo> mapByName;

    /**
     * Constructor
     */
    public Dictionary() {
        timeOrderedSet = new TreeSet<>(new CompareTime);
        mapByName = new HashMap<>();
    }

    /**
     * add a new record of meeting information to the dictionary
     * @requires the time slots does not conflict with any existing meetings
     * @param m
     * @return false when fail to add, true when success
     */
    public synchronized boolean add(meetingInfo m) {
        timeOrderedSet.add(m);
        int[] myEnd = m.getEnd();
        int[] highEnd = timeOrderedSet.higher(m).getEnd();
        int[] lowEnd = timeOrderedSet.lower(m).getEnd();
        if (highEnd[0]<myEnd[0] || (highEnd[0]==myEnd[0] &&
        timeOrderedSet.higher(m.getEndHour()))
        mapByName.put(m.getName(),m);
        return true;
    }

    /**
     * remove the record of a certain meeting given its name
     * @param name
     * @return false when no such meeting found
     */
}
