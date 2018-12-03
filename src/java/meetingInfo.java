package java;


import java.io.Serializable;
import java.util.*;

public class meetingInfo implements Serializable {
    // name of the meeting
    private String name;
    // day and time
    private int[] day; // 3 elements, year month day
    private int[] start; // 2 elements, hour, minute
    private int[] end; // 2 elements, hour, minute

    // users
    private HashSet<String> users;

    // constructor for deletion
    meetingInfo(String n){
        this.name = n;
        this.users = null;
        this.day = new int[3];
        this.start = new int[2];
        this.end = new int[2];
    }
    // constructor
    meetingInfo(String n, int[] day_, int[] start_, int[] end_, HashSet<String> users_){
        this.name = n;
        this.start = start_;
        this.day = day_;
        this.end = end_;
        this.users = users_;
    }

    meetingInfo(meetingInfo MI){
        this.name = MI.name;
        this.start = MI.start;
        this.day = MI.day;
        this.end = MI.end;
        this.users = MI.users;
    }

    public String getName(){
        return this.name;
    }

    public int[] getDay(){
        return new int[]{day[0],day[1],day[2]};

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

    public int compareTo(meetingInfo obj){
        if(day[0] == obj.getDay()[0]){
            if(day[1]== obj.getDay()[1]){
                if(day[2] == obj.getDay()[2]){
                    if(start[0] == obj.getStart()[0]){
                        if(start[1] == obj.getStart()[1]){
                            return 0;
                        }else return start[1] - obj.getStart()[1];
                    }return start[0] - obj.getStart()[0];
                }else return day[2] - obj.getDay()[2];
            }else return day[1] - obj.getDay()[1];
        } else return day[0] - obj.getDay()[0];
    }
}
