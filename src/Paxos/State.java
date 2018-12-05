package Paxos;

import java.io.Serializable;

public class State implements Serializable {

    public int state; // indicate the state in synod algorithm
    // 0 --> no state
    // 1 --> already get majority's promises as a proposer
    // 2 --> already get majority's acks as a proposer

    // State as Acceptor
    public int maxPrep;
    public int accNum;
    public meetingInfo accValue;

    // State as proposal
    // for majority detection
    public int propMaj; // count the number of propose response
    public int ackMaj;// count the number of ack response

    public State (){
        state = 0;
        maxPrep = 0;
        accNum = 0;
        accValue = null;
        propMaj = 1;
        ackMaj = 1;
    }

    public void clearProposerState() {
        propMaj = 1;
        ackMaj = 1;
        state = 0;
    }
}
