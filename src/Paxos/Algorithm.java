package Paxos;


import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Algorithm {

    /**
     * Ask the max number of log
     */
    public static void askForMaxLog(){

    }

    /**
     * When recv the
     */


    /**
     * fill hole then send propose
     */
    public static void fillHolesReq(PaxosLog Plog, DatagramSocket socket, meetingInfo meeting, HashMap<String, int[] > HashPorts, String myname){
        // check if there is missing hole
        if(!Plog.IfHoleExist())return;
        //learn holes
        Packet tempPac = new Packet(0,0, meeting, 6, 0,Plog.getSiteID(), myname  , Plog.MissingLogIndex(), null);
        sendToAll(myname, HashPorts, socket, tempPac);
    }

    /**
     * When recv the filling hole request
     */

    public static void DealWithHoleReq(String myName,Packet pac, DatagramSocket Socket, HashMap<String, int[] > HashPorts, PaxosLog pLog){
        Packet tempPac = new Packet(0,0, pac.accValue, 7, 0,  pLog.getSiteID(), myName, pac.missingIndexList, null);
        int ind =  0;
        ArrayList<PaxosLog.LogEntry> tempList = new ArrayList<>();
        for(int i = 0; i < pac.missingIndexList.length; i++){
            int Index = pac.missingIndexList[i];
            if(Index > pLog.getRepLog().size())break;
            if(pLog.getRepLog().get(Index).getMeeting() != null){
                tempList.add(pLog.getRepLog().get(Index));
                ind++;
            }
        }
        if(ind == 0) return;
        tempPac.RespLogArray = tempList;
        UdpSender udpSender = new UdpSender(Socket, HashPorts.get(pac.siteName)[0], pac.siteName, tempPac);
        new Thread(udpSender).start();
    }

    /**
     * When recv the filling hole response for test
     */

    public static void fillHoleResp(Packet pac, PaxosLog Plog, Dictionary dic){
        if(!Plog.IfHoleExist()) return;
        int recvLognum = pac.RespLogArray.size();
        for(int i = 0; i < recvLognum;i++){
            int ind = pac.RespLogArray.get(i).getLogIndex();
            if( Plog.getRepLog().get(ind) == null){
                Plog.fillTheHole(ind,pac.RespLogArray.get(i).getMeeting());
                meetingInfo m = Plog.getRepLog().get(ind).getMeeting();
                if(m.getUser() == null){
                    dic.removeByName(m.getName());
                } else {
                    dic.add(m);
                }
            }
        }
    }

    /**
     * for real
     */

    public static void fillHoleResp(Dictionary dic, Packet pac, PaxosLog Plog, String myName, HashMap<String, int[] > HashPorts,  DatagramSocket socket){
        if(!Plog.IfHoleExist()) return;
        int recvLognum = pac.RespLogArray.size();
        for(int i = 0; i < recvLognum;i++){
            int ind = pac.RespLogArray.get(i).getLogIndex();
            if( Plog.getRepLog().get(ind) == null){
                Plog.fillTheHole(ind,pac.RespLogArray.get(i).getMeeting());
                meetingInfo m = Plog.getRepLog().get(ind).getMeeting();
                if(m.getUser() == null){
                    dic.removeByName(m.getName());
                } else {
                    dic.add(m);
                }
            }
        }
        if(!Plog.IfHoleExist()){
            if(pac.accValue != null) {
                if(pac.accValue.getUser() == null){ // cancel
                    // check if there exist meeting, if not, unable to schedule
                    if(!dic.hasMeeting(pac.accValue.getName())){
                        System.out.println("Unable to cancel meeting" + pac.accValue.getName());
                        return;
                    }
                } else {
                    // schedule
                    // check if conflict
                    if(dic.checkConflict(pac.accValue)){
                        System.out.println("Unable to schedule meeting" + pac.accValue.getName());
                        return;
                    }
                }
                Propose(pac.accValue, Plog, myName, HashPorts, socket);
                System.err.println("Proposed, wait for response");
            } else {
                // do CheckPoint

            }
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
     * @param hashPorts
     * @param datagramSocket
     * Before doing proposal, you should check if there is any hole exists.
     * If not, call fillHolesReq instead.
     */
    public static void Propose(meetingInfo proposedMeeting, PaxosLog Plog, String myname, HashMap<String, int[]> hashPorts, DatagramSocket datagramSocket) {
        // get a new proposer number:
        int PropNum = Plog.getSiteID() + 100;
        // Add an empty log into Paxoslog
        int index = Plog.getRepLog().size();
        PaxosLog.LogEntry log1 = new PaxosLog.LogEntry(hashPorts.get(myname)[1],index, 0, PropNum, null, proposedMeeting);
        Plog.insertLogEntry(log1);
        Packet preparePacket = new Packet(PropNum, 0, null, 0, index, Plog.getSiteID(), myname, null,null);
        sendToAll(myname, hashPorts, datagramSocket, preparePacket);
    }

    /**
     * to re-propose a timeout proposal
     * 1> change the proposer number to a larger one
     * 2> send prepare packet to all other sites
     * @param logIndex
     * @param log
     * @param myName
     * @param hashPorts
     * @param datagramSocket
     */
    public static void rePropose(int logIndex, PaxosLog log, String myName, HashMap<String, int[]> hashPorts, DatagramSocket datagramSocket) {
        // change the proposer number to a larger one
        int propNum = log.addUpProposalNumber(logIndex);
        // send prepare packets to all except itself
        Packet preparePacket = new Packet(log.getProposalNumber(logIndex), 0, null, 0, logIndex, log.getSiteID(), myName, null, null);
        sendToAll(myName, hashPorts, datagramSocket, preparePacket);
    }

    /**
     * Once receiving proposal (prepare(n))
     * 1> check if the the log entry exist in local log. If not, add hole(s)
     * 2> check if the received propasal number is the largest
     * 3> if so, update maxPrepare and then send promise package to the proposer
     * @param preparePacket
     * @param log
     * @param myName
     * @param hashPorts
     * @param datagramSocket
     */
    public static void OnRecvPrepare(Packet preparePacket, PaxosLog log, String myName, HashMap<String, int[]> hashPorts,  DatagramSocket datagramSocket) {
        //add holes if the log entry does not exist
        log.checkIfLogEntryExist(preparePacket.LogIndex);

        State state = log.getRepLog().get(preparePacket.LogIndex).getCurState();
        if (preparePacket.propNum > state.maxPrep) {
            state.maxPrep = preparePacket.propNum;
            Packet promisePacket = new Packet(0, state.accNum, state.accValue, 1, preparePacket.LogIndex, 0, null, null, null);
            UdpSender udpSender = new UdpSender(datagramSocket, hashPorts.get(preparePacket.siteName)[0], preparePacket.siteName, promisePacket);
            new Thread(udpSender).start();
        }

    }

    /**
     * On receiving promise(accNum,accVal) as a proposer:
     * 1> update State.accNum and accVal when accNum is larger than the stored value
     * 2> add up the counter and check if it reaches majority
     * 3> if so, send accept to all other sites
     * @param log contains all the local log entries
     * @param promisePacket the received promise packet
     * @param myName the name of the site
     * @param hashPorts a hashmap that maps the site ports and IDs to site names
     * @param datagramSocket the udp socket of current site
     */
    public static void OnRecvPromise(PaxosLog log, Packet promisePacket, String myName, HashMap<String, int[]> hashPorts, DatagramSocket datagramSocket) {
        State state = log.getRepLog().get(promisePacket.LogIndex).getCurState();
        if (state.state > 0) return;
        if (promisePacket.accNum > state.accNum) {
            state.accNum = promisePacket.accNum;
            state.accValue = promisePacket.accValue;
        }
        state.propMaj++;
        if (state.propMaj == hashPorts.size()/2 + 1) { //Only send out accept to all when getting promise from majority, and only do this for once
            state.state = Math.max(1,state.state);
            meetingInfo accValue = state.accValue==null ? log.getRepLog().get(promisePacket.LogIndex).proposedMeeting : state.accValue;
            Packet acceptPacket = new Packet(log.getProposalNumber(promisePacket.LogIndex), 0, accValue, 2, promisePacket.LogIndex, hashPorts.get(myName)[1], myName, null, null);
            sendToAll(myName,hashPorts,datagramSocket,acceptPacket);
        }
    }

    /**
     * On receiving accept(propNum,v) from a proposer:
     * 1> check if the the log entry exist in local log. If not, add hole(s)
     * 2> check if the received proposal number is greater or equal to the maxPrepare stored
     *      in the state
     * 3> if true, update the state(accNum,accValue,maxPrepare) of the log entry
     *      and then send ack to the proposer
     * @param log
     * @param acceptPacket
     * @param hashPorts
     * @param datagramSocket
     */
    public static void OnRecvAccept(PaxosLog log, Packet acceptPacket, HashMap<String, int[]> hashPorts, DatagramSocket datagramSocket) {
        //add holes if the log entry does not exist
        log.checkIfLogEntryExist(acceptPacket.LogIndex);

        State state = log.getRepLog().get(acceptPacket.LogIndex).getCurState();
        if (acceptPacket.propNum >= state.maxPrep) {
            state.accNum = acceptPacket.propNum;
            state.accValue = acceptPacket.accValue;
            state.maxPrep = acceptPacket.propNum;
            Packet ackPacket = new Packet(0, state.accNum, state.accValue, 3, acceptPacket.LogIndex, 0, null, null, null);
            UdpSender udpSender = new UdpSender(datagramSocket, hashPorts.get(acceptPacket.siteName)[0], acceptPacket.siteName, ackPacket);
            new Thread(udpSender).start();
        }
    }

    /**
     * On receiving ack as a proposer:
     * 1> add up the counter and check if it reaches majority
     * 2> if so, cancel the scheduled post-timeout re-proposing process(es),
     *      output result on commandline (sends commit to itself),
     *      send commit to all
     * @param log
     * @param ackPacket
     * @param myName
     * @param hashPorts
     * @param datagramSocket
     * @param delayedRepropose the delayed thread executor for timeout
     */
    public static void OnRecvAck(PaxosLog log, Packet ackPacket, String myName, HashMap<String, int[]> hashPorts, DatagramSocket datagramSocket, ScheduledThreadPoolExecutor delayedRepropose) {
        State state = log.getRepLog().get(ackPacket.LogIndex).getCurState();
        if (state.state > 1) return;
        //Does not have to check accNum or accVal when receiving ack
        state.ackMaj++;
        //Only send out commit to all when getting ack from majority, and only do this for once
        if (state.ackMaj == hashPorts.size()/2 + 1) {
            state.state = Math.max(2,state.state);
            delayedRepropose.shutdownNow(); //Cancel the scheduled post-timeout re-proposing process(es)

            if (log.checkIfProposedMeetingAccepted(ackPacket.LogIndex)) {
                if (log.getRepLog().get(ackPacket.LogIndex).meeting.getUser() == null) { // cancel event
                    System.out.println("Cancel " + log.getRepLog().get(ackPacket.LogIndex).meeting.toString());
                } else { // schedule event
                    System.out.println("Schedule " + log.getRepLog().get(ackPacket.LogIndex).meeting.toString());
                }
            } else {
                if (log.getRepLog().get(ackPacket.LogIndex).meeting.getUser() == null) { // cancel event
                    System.out.println("Unable to cancel meeting " + log.getRepLog().get(ackPacket.LogIndex).meeting.getName() + ".");
                } else {
                    System.out.println("Unable to schedule meeting " + log.getRepLog().get(ackPacket.LogIndex).meeting.getName() + ".");
                }
            }

            Packet commitPacket = new Packet(0,0, state.accValue, 4, ackPacket.LogIndex, 0, null, null, null);
            sendToAll(myName,hashPorts,datagramSocket,commitPacket);
        }
    }

    /**
     * On receiving commit:
     * 1> fill in the value
     * 2> if the site is a proposer, output result on commondline
     * @param log
     * @param commitPacket
     */
    public static void OnRecvCommit(PaxosLog log, Packet commitPacket) {
        //add holes if the log entry does not exist
        log.checkIfLogEntryExist(commitPacket.LogIndex);

        log.fillTheHole(commitPacket.LogIndex, commitPacket.accValue);

        if (log.checkIfProposedMeetingAccepted(commitPacket.LogIndex)) {
            if (log.getRepLog().get(commitPacket.LogIndex).meeting.getUser() == null) { // cancel event
                System.out.println("Cancel " + log.getRepLog().get(commitPacket.LogIndex).meeting.toString());
            } else { // schedule event
                System.out.println("Schedule " + log.getRepLog().get(commitPacket.LogIndex).meeting.toString());
            }
        } else if (log.getRepLog().get(commitPacket.LogIndex).proposedMeeting != null) {
            if (log.getRepLog().get(commitPacket.LogIndex).meeting.getUser() == null) { // cancel event
                System.out.println("Unable to cancel meeting " + log.getRepLog().get(commitPacket.LogIndex).meeting.getName() + ".");
            } else {
                System.out.println("Unable to schedule meeting " + log.getRepLog().get(commitPacket.LogIndex).meeting.getName() + ".");
            }
        }

    }


    /**
     * send the packet to all sites except itself on a new thread
     * @param myName
     * @param hashPorts
     * @param datagramSocket
     * @param packet
     */
    private static void sendToAll(String myName, HashMap<String, int[] > hashPorts, DatagramSocket datagramSocket, Packet packet) {
        for (String siteName : hashPorts.keySet()) {
            if (!myName.equals(siteName)) {
                UdpSender udpSender = new UdpSender(datagramSocket, hashPorts.get(siteName)[0], siteName, packet);
                new Thread(udpSender).start();
            }
        }
    }


}