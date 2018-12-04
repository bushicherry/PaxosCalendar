package Paxos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

public class PaxosLog implements Serializable {
    // a LogEntry array to store
    private ArrayList<LogEntry> repLog; // replicated LogEntry for each site
    private int lastPropNum; // the last used proposal number
    private Vector<PaxosLog.LogEntry> EmptyLog; // indicate if there are any holes

    // constructor
    public PaxosLog(){
        this.repLog = new ArrayList<>();
        this.lastPropNum = 0;
        EmptyLog = new Vector<>();
    }

    public PaxosLog(PaxosLog pLog){
        this.repLog = pLog.getRepLog();
        this.lastPropNum = pLog.getLastPropNum();
        EmptyLog = pLog.getEmptyLog();
    }

    // get
    public ArrayList<LogEntry> getRepLog(){
        return this.repLog;
    }

    public int getLastPropNum(){
        return lastPropNum;
    }

    public Vector<PaxosLog.LogEntry> getEmptyLog() {
        return EmptyLog;
    }

    public void setLastProNum(int newNum){
        this.lastPropNum = newNum;
    }

    // insert an LogEntry
    public void insertLog(LogEntry l){ // l is a new LogEntry
        this.repLog.add(l);
        if(l.getMeeting() == null){
            this.EmptyLog.add(l);
        }
    }

    // insert a value to empty hole
    public boolean insertEmptyLog(int Index, meetingInfo value){
        if(repLog.get(Index-1).getMeeting() != null){
            System.out.println("Not a hole, but a value is intended to be inserted");
            return false;
        }
        this.repLog.get(Index -1).setMeeting(value);
        this.EmptyLog.remove(repLog.get(Index-1));
        return true;
    }


    // check if there is any empty hole exist
    public boolean IfHoleExist(){
        if (EmptyLog.size() == 0) return false;
        else return true;
    }
    // check if


    //print_out LogEntry
    void LogArrayPrint(){
        for(LogEntry l: repLog){
            if (l.getMeeting().getUser() ==null){
                System.out.println("Cancel " + l.getMeeting().getName());
            } else {
                System.out.print("Schedule " + l.getMeeting().toString());
            }
        }
    }

    public static class LogEntry implements Serializable {
        // 0 -> schedule; 1 -> cancel
        int LogIndex;
        int type;
        // unique ID for each LogEntry,the accNum in synod algorithm
        int uniqueID;
        // value v
        meetingInfo meeting;
        public LogEntry(int type, int ID, meetingInfo value, int LgIndex)  {
            this.type = type;
            this.uniqueID = ID; // the accNum in synod algorithm
            this.meeting = value;
            this.LogIndex = LgIndex;
        }


        public meetingInfo getMeeting(){
            return this.meeting;
        }

        public int getID (){
            return uniqueID;
        }

        public int getType(){
            return this.type;
        }

        public void setMeeting(meetingInfo m) {
            meeting = m;
        }
    }

}


