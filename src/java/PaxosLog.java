package java;

import java.io.Serializable;
import java.util.ArrayList;

public class PaxosLog implements Serializable {
    // a log array to store
    private ArrayList<Log> repLog; // replicated log for each site
    private int lastPropNum; // the last used proposal number

    // constructor
    public PaxosLog(){
        this.repLog = new ArrayList<>();
        this.lastPropNum = 0;
    }

    public PaxosLog(PaxosLog pLog){
        this.repLog = pLog.getRepLog();
        this.lastPropNum = pLog.getLastPropNum();
    }

    // get
    public ArrayList<Log> getRepLog(){
        return this.repLog;
    }

    public int getLastPropNum(){
        return lastPropNum;
    }

    public void setLastProNum(int newNum){
        this.lastPropNum = newNum;
    }

    // insert an log
    public void insertLog(Log l){ // l is a new log
        this.repLog.add(l);
    }

    public static class Log implements Serializable {
        // 0 -> schedule; 1 -> cancel
        int LogIndex;
        int type;
        // unique ID for each log,the accNum in synod algorithm
        int uniqueID;
        // value v
        meetingInfo meeting;
        public Log(int type, int ID, meetingInfo value, int LgIndex)  {
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


