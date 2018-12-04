package java;

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
     * add a new record of meeting information to the dictionary
     * @requires the time slots does not conflict with any existing meetings
     * @param m
     * @return false when fail to add, true when success
     */
    public synchronized boolean add(meetingInfo m) {
        timeOrderedSet.add(m);
        int[] myEnd = m.getEnd();
        int[] myStart = m.getStart();
        int[] highEnd = timeOrderedSet.higher(m).getEnd();
        int[] lowStart = timeOrderedSet.lower(m).getStart();
        if (highEnd[0]>myStart[0] || (highEnd[0]==myStart[0] && highEnd[1]>myStart[1]) ||
                myEnd[0]>lowStart[0] || (myEnd[0]==lowStart[0] && myEnd[1]>lowStart[1])) {
            timeOrderedSet.remove(m);
            return false;
        }
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
        return true;
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
     * print the entire dictionary in lexicographical order
     */
    public synchronized void printEntireDic() {
        for (meetingInfo m: timeOrderedSet) {
            int[] date = m.getDate();
            int[] start = m.getStart();
            int[] end = m.getEnd();
            String ret = String.format("%s %d/%d/%d %02d:%02d %02d:%02d ",m.getName(),date[0],date[1],date[2],start[0],start[1],end[0],end[1]);
            for (String u : m.getUser()) {
                ret = ret + u;
                ret = ret + ",";
            }
            System.out.println(ret.substring(0,ret.length()-1));
        }
    }

    /**
     * given a ceitain user, print all meetings that involve him/her
     * @param user
     */
    public synchronized void printIndividualDic(String user) {
        if (mapByUser.containsKey(user)) {
            for (meetingInfo m: mapByUser.get(user)) {
                int[] date = m.getDate();
                int[] start = m.getStart();
                int[] end = m.getEnd();
                String ret = String.format("%s %d/%d/%d %02d:%02d %02d:%02d ",m.getName(),date[0],date[1],date[2],start[0],start[1],end[0],end[1]);
                for (String u : m.getUser()) {
                    ret = ret + u;
                    ret = ret + ",";
                }
                System.out.println(ret.substring(0,ret.length()-1));
            }
        }
    }
}
