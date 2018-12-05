package Paxos;


import java.io.Serializable;
import java.util.*;

public class meetingInfo implements Serializable {
    // name of the meeting
    private String name;
    // date and time
    private int[] date; // 3 elements, month date and year
    private int[] start; // 2 elements, hour, minute
    private int[] end; // 2 elements, hour, minute

    // users
    private HashSet<String> users;

    // constructor for deletion
    meetingInfo(String n){
        this.name = n;
        this.users = null;
        this.date = null;
        this.start = null;
        this.end = null;
    }
    // constructor
    meetingInfo(String n, int[] date_, int[] start_, int[] end_, HashSet<String> users_){
        this.name = n;
        this.start = start_;
        this.date = date_;
        this.end = end_;
        this.users = users_;
    }

    meetingInfo(meetingInfo MI){
        this.name = MI.name;
        this.start = MI.start;
        this.date = MI.date;
        this.end = MI.end;
        this.users = MI.users;
    }

    public String getName(){
        return this.name;
    }

    public int[] getDate(){
        return new int[]{date[0],date[1],date[2]};

    }

    public int[] getStart(){
        return new int[]{start[0], start[1]};
    }

    public int[] getEnd(){
        return new int[]{end[0],end[1]};
    }

    public HashSet<String> getUser(){
        return new HashSet<>(users);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof meetingInfo) {
            meetingInfo m = (meetingInfo)o;
            return name.equals(m.name); //Given that name of each meeting is unique
        }
        return false;
    }

    public String toString() {
        String ret;
        if (users == null) {
            ret = name + ",";
        } else {
            ret = String.format("%s %02d/%02d/%d %02d:%02d %02d:%02d ",name,date[0],date[1],date[2],start[0],start[1],end[0],end[1]);
        }
        for (String u : users) {
            ret = ret + u;
            ret = ret + ",";
        }
        return ret.substring(0,ret.length()-1);
    }

    public int compareTo(meetingInfo obj){
        if(date[2] == obj.getDate()[2]){
            if(date[0]== obj.getDate()[0]){
                if(date[1] == obj.getDate()[1]){
                    if(start[0] == obj.getStart()[0]){
                        if(start[1] == obj.getStart()[1]){
                            return this.name.compareTo(obj.getName());
                        }
                        return start[1] - obj.getStart()[1];
                    }
                    return start[0] - obj.getStart()[0];
                }
                return date[2] - obj.getDate()[2];
            }
            return date[1] - obj.getDate()[1];
        }
        return date[0] - obj.getDate()[0];
    }
}
