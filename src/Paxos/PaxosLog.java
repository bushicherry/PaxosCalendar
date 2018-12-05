package Paxos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

public class PaxosLog implements Serializable {
    // a LogEntry array to store
    private ArrayList<LogEntry> repLog; // replicated LogEntry for each site
//    private int lastPropNum; // the last used proposal number
    private Vector<PaxosLog.LogEntry> EmptyLog; // indicate if there are any holes
    private int siteID;

    //state variables
    private LogEntry currentState;

    // constructor
    public PaxosLog(int siteID){
        this.repLog = new ArrayList<>();
//        this.lastPropNum = siteID;
        this.EmptyLog = new Vector<>();
        this.siteID = siteID;
        this.currentState = new LogEntry(siteID, 0, 0, 0, null, null);
    }

    // copy constructor
    public PaxosLog(PaxosLog pLog){
        this.repLog = pLog.getRepLog();
//        this.lastPropNum = pLog.getLastPropNum();
        EmptyLog = pLog.getEmptyLog();

    }

    // get
    public ArrayList<LogEntry> getRepLog(){
        return this.repLog;
    }
//
//    public int getLastPropNum(){
//        return lastPropNum;
//    }

    public Vector<PaxosLog.LogEntry> getEmptyLog() {
        return EmptyLog;
    }

    /**
     * for non-proposer, only accNum, accValue, maxPrep and LogIndex are needed for state
     * @return
     */
    public State getCurrentState() {
        return currentState.getCurState();
    }

    public int getCurrentLogIndex() {
        return currentState.LogIndex;
    }

//    public void setLastProNum(int newNum){
//        this.lastPropNum = newNum;
//    }

    public int getSiteID(){
        return siteID;
    }

    // insert a LogEntry
    public void insertLogEntry(LogEntry logEntry){ // l is a new LogEntry
        this.repLog.add(logEntry);
        if(logEntry.isEmpty()){
            this.EmptyLog.add(logEntry);
        }
    }

    public void addLogEntry(int type, int propNum, meetingInfo value, meetingInfo proposedValue) {
        LogEntry logEntry = new LogEntry(siteID,repLog.size(),type,propNum,value,proposedValue);
        insertLogEntry(logEntry);
    }

    public boolean isEmpty(int logIndex) {
        return repLog.get(logIndex).isEmpty();
    }

    public int getProposalNumber(int logIndex) {
        return repLog.get(logIndex).getPropNum();
    }

    public int addUpProposalNumber(int logIndex) throws RuntimeException{
        if (!repLog.get(logIndex).isEmpty()) throw new RuntimeException("Not a hole! Cannot change the proposal number!");
        repLog.get(logIndex).setPropNum(repLog.get(logIndex).getPropNum()+100);
        return repLog.get(logIndex).getPropNum();
    }

    /**
     * Check if the log Entry exists
     * @param logIndex log index
     * @effect If the log entry exists, then nothing changed.
     *          Otherwise, insert hole(s)
     * @return true if the logIndex
     */
    public boolean checkIfLogEntryExist(int logIndex) {
        // logIndex >= repLog.size() means I am not a proposer, since a proposer will create an empty log entry
        if (logIndex >= repLog.size()) { //add the missing log entries to the log as holes
            for (int index = repLog.size(); index < logIndex; index++ ) {
                addLogEntry(0,0,null,null);
            }
            return false;
        }
        return true;
    }

    /**
     * Call this method every time I receive a packet as acceptor or propose a new event
     * Has no effect on proposer
     * @param logIndex
     */
    public void updateLogIndex(int logIndex) {
        if (currentState.LogIndex < logIndex) {
            currentState.LogIndex = logIndex;
            currentState.clearCurrentState();
        }
    }

    public boolean checkIfProposedMeetingAccepted(int logIndex) {
        if (repLog.get(logIndex).proposedMeeting == null) return false;
        if (repLog.get(logIndex).meeting == null) return false;
        return repLog.get(logIndex).meeting.equals(repLog.get(logIndex).proposedMeeting);
    }

    /**
     * insert a value to empty hole
     * @param Index
     * @param value
     * @return true if successfully inserted, false if not
     */
    public boolean fillTheHole(int Index, meetingInfo value){
        if(!repLog.get(Index).isEmpty()){
            System.err.println("Not a hole");
            return false;
        }
        this.repLog.get(Index).setMeeting(value);
        this.EmptyLog.remove(repLog.get(Index));
        return true;
    }


    // check if there is any empty hole
    public boolean IfHoleExist(){
        return EmptyLog.size() != 0;
    }


    // return the array of indexes of holes (empty log entries)
    public int[] MissingLogIndex(){
        if(EmptyLog.size() == 0) return null;
        int [] i = new int[EmptyLog.size()];
        for(int k = 0;  k < EmptyLog.size();k++){
            i[k] = EmptyLog.get(k).getLogIndex();
        }
        return i;
    }


    public boolean checkIfCancelExists(String meetingName) {
        for (LogEntry le : repLog) {
            if (le.meeting != null && le.meeting.getUser() == null && le.meeting.getName().equals(meetingName))
                return true;
        }
        return false;
    }


    //print out all LogEntry
    void PrintLog(){
        if(repLog.size() != 0) {
            for (LogEntry l : repLog) {
                if (l.getMeeting() != null) {
                    if (l.getMeeting().getUser() == null) {
                        System.out.println("Cancel " + l.getMeeting().getName());
                    } else {
                        System.out.println("Schedule " + l.getMeeting().toString());
                    }
                }
            }
        }
    }

    public static class LogEntry implements Serializable {
        // 0 -> schedule; 1 -> cancel
        int siteID; // siteID
        int LogIndex;
        int type;
        // -> 0 for cancel
        // -> 1 for schedule
        // unique ID for each LogEntry,the accNum in synod algorithm
        private int uniqueID; // proposal number
        // value v
        meetingInfo meeting;
        // local value
        meetingInfo proposedMeeting;
        // state for this log
        State CurState ;// current state


        public LogEntry(int siteID, int LogIndex, int type, int propNum, meetingInfo value, meetingInfo proposedValue)  {
            this.type = type;
            this.uniqueID = propNum; // the accNum in synod algorithm
            this.meeting = value;
            this.proposedMeeting = proposedValue;
            this.LogIndex = LogIndex;
            this.siteID = siteID; // in anther word, SiteID
            this.CurState = new State();
        }

        public void setPropNum(int newPropNum) {
            uniqueID = newPropNum;
        }

        public meetingInfo getMeeting(){
            return this.meeting;
        }

        public int getPropNum (){
            return uniqueID;
        }

        public void setMeeting(meetingInfo m) {
            meeting = m;
        }

        public int getLogIndex(){
            return this.LogIndex;
        }
        public int getSiteID(){
            return this.siteID;
        }

        public State getCurState(){
            return CurState;
        }

        /**
         * check if the log entry is a hole, or in another word, is empty
         * @return true if empty, false if not
         */
        public boolean isEmpty() {
            if (meeting == null) return true;
            else return false;
        }

        public void clearCurrentState() {
            CurState.maxPrep = 0;
            CurState.accValue = null;
            CurState.accNum = 0;
        }

    }

}


