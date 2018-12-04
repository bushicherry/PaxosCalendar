package java;


import java.io.Serializable;

public class Packet implements Serializable {
    public int propNUm; // proposal number in the beginning
    public int accNum; // acctual number each process
    public meetingInfo accValue; // acctual value
    public int packeyType; // the packet type
                    // 0 for prepare packet
                    // 1 for prepare response packet
                    // 2 for accept packet
                    // 3 for ack packet
                    // 4 for commit packet
                    // 5 for nack packet
    public int LogIndex; // log index


    public Packet(int pn, int an, meetingInfo av, int pt,int ln){
        this.propNUm = pn;
        this.accNum = an;
        this.accValue = av;
        this.packeyType = pt;
        this.LogIndex = ln;
    }

}