package Paxos;

import java.net.DatagramSocket;
import java.util.HashMap;

public class Algorithm {


    /**
     * fill hole then send popose
     */
    public void fillHolesReq(PaxosLog Plog, DatagramSocket socket, meetingInfo meeting, HashMap<String, int[] > HashPorts, String myname){
        // check if there is missing hole
        if(!Plog.IfHoleExist())return;
        else {

        }

    }

    /**
     * It should be noted that when this happens, all the holes are filled
     * 1> add an empty log into PaxosLog
     * 2> prepare packet
     * 3> send packet to all other sites
     * 4> set state
     * @param Plog
     * @param myname
     * @param HashPorts
     * @param socket
     */
    public void Propose(PaxosLog Plog, String myname, HashMap<String, int[] > HashPorts,  DatagramSocket socket){
        // get a new proposer number:
        int PropNum = Plog.getSiteID() + 100;
        // Add an empty log into Paxoslog

    }





}