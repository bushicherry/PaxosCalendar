package Paxos;

import java.io.Serializable;
import java.util.ArrayList;

public class PaxosLog implements Serializable {
    // a LogEntry array to store
    private ArrayList<LogEntry> repLog; // replicated LogEntry for each site
    private int lastPropNum; // the last used proposal number

    // constructor
    public PaxosLog(int siteId){
        this.repLog = new ArrayList<>();
        this.lastPropNum = siteId;
    }

    public PaxosLog(PaxosLog pLog){
        this.repLog = pLog.getRepLog();
        this.lastPropNum = pLog.getLastPropNum();
    }

    // get
    public ArrayList<LogEntry> getRepLog(){
        return this.repLog;
    }

    public int getLastPropNum(){
        return lastPropNum;
    }

    public void setLastProNum(int newNum){
        this.lastPropNum = newNum;
    }

    // insert an LogEntry
    public void insertLog(LogEntry l){ // l is a new LogEntry
        this.repLog.add(l);
    }


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
    }

}


