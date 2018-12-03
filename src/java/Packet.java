package java;


import java.io.Serializable;

public class Packet implements Serializable {
    private int propNUm; // proposal number in the beginning
    private int accNum; // acctual number each process
    private meetingInfo accValue; // acctual value
    private int packeyType; // the packet type
                    // 0 for prepare packet
                    // 1 for prepare response packet
                    // 2 for accept packet
                    // 3 for ack packet
                    // 4 for commit packet
                    // 5 for nack packet

    public Packet(int pn, int an, meetingInfo av, int pt){
        this.propNUm = pn;
        this.accNum = an;
        this.accValue = av;
        this.packeyType = pt;
    }

    public int getPropNum(){
        return propNUm;

    }

    public int getAccNum(){
        return accNum;
    }


    public int getPackeyType(){
        return packeyType;
    }

    public meetingInfo getAccValue(){
        return accValue;
    }
}