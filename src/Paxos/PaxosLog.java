package Paxos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

public class PaxosLog implements Serializable {
    // a LogEntry array to store
    private ArrayList<LogEntry> repLog; // replicated LogEntry for each site
    private int lastPropNum; // the last used proposal number
    private Vector<PaxosLog.LogEntry> EmptyLog; // indicate if there are any holes
    private int siteID;

    // constructor
    public PaxosLog(int siteID){
        this.repLog = new ArrayList<>();
        this.lastPropNum = siteID;
        EmptyLog = new Vector<>();
        this.siteID = siteID;
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

    public int getSiteID(){
        return siteID;
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
        if(repLog.get(Index).getMeeting() != null){
            System.out.println("Not a hole, but a value is intended to be inserted");
            return false;
        }
        this.repLog.get(Index).setMeeting(value);
        this.EmptyLog.remove(repLog.get(Index));
        return true;
    }


    // check if there is any empty hole exist
    public boolean IfHoleExist(){
        return EmptyLog.size() != 0;
    }
    // return the array of missing log emtry index
    public int[] MissingLogIndex(){
        if(EmptyLog.size() == 0) return null;
        int [] i = new int[EmptyLog.size()];
        for(int k = 0;  k < EmptyLog.size();k++){
            i[k] = EmptyLog.get(k).getLogIndex();
        }
        return i;
    }


    //print_out LogEntry
    void LogArrayPrint(){

        for(LogEntry l: repLog){
            if(l.getMeeting() != null) {
                if (l.getMeeting().getUser() == null) {
                    System.out.println("Cancel " + l.getMeeting().getName());
                } else {
                    System.out.println("Schedule " + l.getMeeting().toString());
                }
            }
        }
    }

    public static class LogEntry implements Serializable {
        // 0 -> schedule; 1 -> cancel
        int ProposerID; // siteID
        int LogIndex;
        int type;
        // -> 0 for cancel
        // -> 1 for schedule
        // unique ID for each LogEntry,the accNum in synod algorithm
        int uniqueID; // proposal number
        // value v
        meetingInfo meeting;
        // state for this log
        State CurState ;// current state


        public LogEntry(int ProposerID,int LgIndex, int type, int ID, meetingInfo value)  {
            this.type = type;
            this.uniqueID = ID; // the accNum in synod algorithm
            this.meeting = value;
            this.LogIndex = LgIndex;
            this.ProposerID = ProposerID; // in anther word, SiteID
            this.CurState = new State();
        }


        public meetingInfo getMeeting(){
            return this.meeting;
        }

        public int getPropNum (){
            return uniqueID;
        }

        public int getType(){
            return this.type;
        }

        public void setMeeting(meetingInfo m) {
            meeting = m;
        }

        public int getLogIndex(){
            return this.LogIndex;
        }
        public int getSiteID(){
            return this.ProposerID;
        }

        public State getCurState(){
            return CurState;
        }

    }

}


