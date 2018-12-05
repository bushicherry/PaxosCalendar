package Paxos;

import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

class CompareTime implements Comparator<meetingInfo> {
    public int compare(meetingInfo m1, meetingInfo m2) {
        return m1.compareTo(m2);
    }
}

public class Dictionary {
    private TreeSet<meetingInfo> timeOrderedSet;
    private HashMap<String, meetingInfo> mapByName;
    private HashMap<String, TreeSet<meetingInfo>> mapByUser;

    /**
     * Constructor
     */
    public Dictionary() {
        timeOrderedSet = new TreeSet<>(new CompareTime());
        mapByName = new HashMap<>();
        mapByUser = new HashMap<>();
    }

    /**
     * Check if a meeting conflict with current dictionary
     * Need to be locked since it involves add and remove
     * @param m
     * @return true if conflict, false if not
     */
    public synchronized boolean checkConflict(meetingInfo m) {
        timeOrderedSet.add(m);
        int[] myEnd = m.getEnd();
        int[] myStart = m.getStart();
        int[] highEnd = timeOrderedSet.higher(m).getEnd();
        int[] lowStart = timeOrderedSet.lower(m).getStart();
        if (highEnd[0]>myStart[0] || (highEnd[0]==myStart[0] && highEnd[1]>myStart[1]) ||
                myEnd[0]>lowStart[0] || (myEnd[0]==lowStart[0] && myEnd[1]>lowStart[1])) {
            return true;
        }
        timeOrderedSet.remove(m);
        return false;
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
        int[] myStart = m.getStart();
        meetingInfo high = timeOrderedSet.higher(m);
        meetingInfo low = timeOrderedSet.lower(m);
        //check if the higher element and the lower element exists
        if (high == null && low == null) {
            addHelper(m);
            return true;
        } else if (high == null) {
            int[] lowEnd = timeOrderedSet.lower(m).getEnd();
            if (lowEnd[0]>myStart[0] || (lowEnd[0]==myStart[0] && lowEnd[1]>myStart[1])) {
                timeOrderedSet.remove(m);
                return false;
            }
            addHelper(m);
            return true;
        } else if (low == null) {
            int[] highStart = timeOrderedSet.higher(m).getStart();
            if (myEnd[0]>highStart[0] || (myEnd[0]==highStart[0] && myEnd[1]>highStart[1])) {
                timeOrderedSet.remove(m);
                return false;
            }
            addHelper(m);
            return true;
        } else {
            int[] lowEnd = timeOrderedSet.lower(m).getEnd();
            int[] highStart = timeOrderedSet.higher(m).getStart();
            if (lowEnd[0]>myStart[0] || (lowEnd[0]==myStart[0] && lowEnd[1]>myStart[1]) ||
                    myEnd[0]>highStart[0] || (myEnd[0]==highStart[0] && myEnd[1]>highStart[1])) {
                timeOrderedSet.remove(m);
                return false;
            }
            addHelper(m);
            return true;
        }
    }

    private synchronized void addHelper(meetingInfo m) {
        mapByName.put(m.getName(),m);
        for (String u : m.getUser()) {
            if (mapByUser.containsKey(u)) {
                mapByUser.get(u).add(m);
            } else {
                TreeSet<meetingInfo> ts = new TreeSet<>(new CompareTime());
                ts.add(m);
                mapByUser.put(u, ts);
            }
        }
    }

    /**
     * remove the record of a certain meeting given its name
     * @param name
     * @return false when no such meeting found
     */
    public synchronized boolean removeByName(String name) {
        meetingInfo m = mapByName.get(name);
        if (m == null) return false;
        boolean removed = timeOrderedSet.remove(m);
        for (String u : m.getUser()) {
            removed = removed && mapByUser.get(u).remove(m);
        }
        if (removed) mapByName.remove(name);
        return removed;
    }

    /**
     * check if a certain user is involved in a certain meeting
     * @param userName
     * @param meetingName
     * return true when involved, false when not
     */
    public synchronized boolean involved(String userName, String meetingName) {
        if (mapByName.containsKey(meetingName) && mapByName.get(meetingName).getUser().contains(userName))
            return true;
        return false;
    }

    /**
     * check if meeting m is in the dictionary
     * @param meetingName
     * @return true if m is in the dictionary, false if not
     */
    public synchronized boolean hasMeeting(String meetingName) {
        return mapByName.containsKey(meetingName);
    }

    /**
     * print the entire dictionary in lexicographical order
     */
    public synchronized void printEntireDic() {
        for (meetingInfo m: timeOrderedSet) {
            System.out.println(m.toString());
        }
    }

    /**
     * given a ceitain user, print all meetings that involve him/her
     * @param user
     */
    public synchronized void printIndividualDic(String user) {
        if (mapByUser.containsKey(user)) {
            for (meetingInfo m: mapByUser.get(user)) {
                System.out.println(m.toString());
            }
        }
    }
}
