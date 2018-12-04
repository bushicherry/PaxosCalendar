package Paxos;


import java.io.Serializable;
import java.util.ArrayList;

public class Packet implements Serializable {
    // for synod algorithm
    public int propNUm; // proposal number in the beginning
    public int accNum; // actual number each process
    public meetingInfo accValue; // actual value
    public int packetType; // the packet type
                    // 0 for prepare packet
                    // 1 for prepare response packet
                    // 2 for accept packet
                    // 3 for ack packet
                    // 4 for commit packet
                    // 5 for nack packet
                    // 6 for asking for missing holes
    public int LogIndex; // log index


    // for filling the hole
    public int StartNumOfLogIndex; // the start index for missing log
    public int EndNumOfLogIndex; // the end index for missing log
    public ArrayList<PaxosLog.Log> RespLogArray; // responded log array


    public Packet(int pn, int an, meetingInfo av, int pt,int ln, int StarNumberOfIndex_, int EndNumOfIndex_, ArrayList<PaxosLog.Log> LogArray){
        this.propNUm = pn;
        this.accNum = an;
        this.accValue = av;
        this.packetType = pt;
        this.LogIndex = ln;

        this.StartNumOfLogIndex = StarNumberOfIndex_;
        this.EndNumOfLogIndex = EndNumOfIndex_;
        this.RespLogArray = LogArray;

    }

}