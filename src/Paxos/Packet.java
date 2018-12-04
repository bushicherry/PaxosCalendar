package Paxos;


import java.io.Serializable;
import java.util.ArrayList;

public class Packet implements Serializable {
    // for synod algorithm
    public int propNum; // proposal number in the beginning
    public int accNum; // actual number each process
    public meetingInfo accValue; // actual value
    public int packetType; // the packet type
    // -1 for no type
    // 0 for prepare packet
    // 1 for prepare response packet
    // 2 for accept packet
    // 3 for ack packet
    // 4 for commit packet
    // 6 for asking for missing holes
    // 7 for reply missing hole ask;
    // 8 for asking how
    public int LogIndex; // LogEntry index
    public int siteID;
    public String siteName;


    // for filling the hole
    public int[] missingIndexList;
    public ArrayList<PaxosLog.LogEntry> RespLogArray; // responded LogEntry array


    public Packet(int propNum, int accNum, meetingInfo accValue, int packetType,int logIndex, int siteID, String siteName, int[] missingIndexList, ArrayList<PaxosLog.LogEntry> respLogArray){
        this.propNum = propNum;
        this.accNum = accNum;
        this.accValue = accValue;
        this.packetType = packetType;
        this.LogIndex = logIndex;
        this.siteID = siteID;
        this.siteName = siteName;

        this.missingIndexList = missingIndexList;
        this.RespLogArray = respLogArray;

    }

}