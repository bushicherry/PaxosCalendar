package Paxos;

public class State {

    public int state; // indicate the state in synod algorithm
    // 0 --> no state
    // 1 --> waiting for propose response
    // 2 --> waiting for ack

    // required variable in synod algorithm
    public int maxPrep;
    public int accNum;
    public meetingInfo accValue;

    // for majority detection
    public int propMaj; // count the number of propose reponse
    public int ackMaj;// count the number of ack response

    public State (){
        state = 0;
        maxPrep = 0;
        accNum = 0;
        accValue = null;
        propMaj = 1;
        ackMaj = 1;
    }
}
