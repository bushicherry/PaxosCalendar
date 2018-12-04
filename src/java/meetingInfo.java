package java;


import java.io.Serializable;
import java.util.*;

public class meetingInfo implements Serializable {
    // name of the meeting
    private String name;
    // date and time
    private int[] date; // 3 elements, year month date
    private int[] start; // 2 elements, hour, minute
    private int[] end; // 2 elements, hour, minute

    // users
    private HashSet<String> users;

    // constructor for deletion
    meetingInfo(String n){
        this.name = n;
        this.users = null;
        this.date = new int[3];
        this.start = new int[2];
        this.end = new int[2];
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

    public int[] getdate(){
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

    public void MmeetingPrint(){
        System.out.println();
    }

    public int compareTo(meetingInfo obj){
        if(date[0] == obj.getdate()[0]){
            if(date[1]== obj.getdate()[1]){
                if(date[2] == obj.getdate()[2]){
                    if(start[0] == obj.getStart()[0]){
                        if(start[1] == obj.getStart()[1]){
                            return 0;
                        }
                        return start[1] - obj.getStart()[1];
                    }
                    return start[0] - obj.getStart()[0];
                }
                return date[2] - obj.getdate()[2];
            }
            return date[1] - obj.getdate()[1];
        }
        return date[0] - obj.getdate()[0];
    }
}
