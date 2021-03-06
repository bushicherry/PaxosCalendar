package Paxos;



import java.io.*;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Algorithm {

    /**
     * Ask the max number of log
     */
    public static void askForMaxLog(DatagramSocket socket, HashMap<String, int[] > HashPorts, String myname){
        Packet pac = new Packet(0,0,null, 8, 0, 0, myname, null, null);
        sendToAll(myname, HashPorts, socket, pac);
    }

    /**
     * When recv the asking for how many logs
     */

    public static void responseMaxlog(PaxosLog Plog,  Packet pac, DatagramSocket socket, HashMap<String, int[] > HashPorts, String myname){
        Packet newPac = new Packet(0, Plog.getRepLog().size(), null, 9, 0,0,myname,null,null);
        sendToAll(myname, HashPorts, socket, newPac);
    }

    /**
     * When recv log number from others
     *
     */

    public static void OnrecvMaxHoles(Packet pac, PaxosLog Plog, int myID){
        int ind = pac.accNum;
        Plog.updateLogIndex(ind);
    }


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
            if(Index >= pLog.getRepLog().size())break;
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
                    if(!dic.involved(myName, pac.accValue.getName())) return;
                } else {
                    // schedule
                    // check if conflict
                    if(dic.checkConflict(pac.accValue)){
                        System.out.println("Unable to schedule meeting" + pac.accValue.getName());
                        return;
                    }
                }
                propose(pac.accValue, Plog, myName, HashPorts, socket);

                // for debugging
//                System.err.println("Proposed, wait for response");
            } else {
                // do CheckPoint
                setCheckPoint(Plog, dic);
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
    public static void propose(meetingInfo proposedMeeting, PaxosLog Plog, String myname, HashMap<String, int[]> hashPorts, DatagramSocket datagramSocket) {
        // get a new proposer number:
        int PropNum = Plog.getSiteID() + 100;
        // Add an empty log into Paxoslog
        int index = Plog.getRepLog().size();
        Plog.updateLogIndex(index); // update currentState
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
     * @param hashPorts
     * @param datagramSocket
     */
    public static void OnRecvPrepare(Packet preparePacket, PaxosLog log, HashMap<String, int[]> hashPorts,  DatagramSocket datagramSocket) {
        //add holes if the log entry does not exist
        boolean isProposer = log.checkIfLogEntryExist(preparePacket.LogIndex); // true means I am concurrent proposer
        log.updateLogIndex(preparePacket.LogIndex);
        //Use currentState as state if I am not a concurrent proposer
        State state;
        if (isProposer) {
            state = log.getRepLog().get(preparePacket.LogIndex).getCurState();
        } else {
            state = log.getCurrentState();
        }
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
//        System.err.println("Enter RecvPromise");
        if (promisePacket.accNum > state.accNum) {
            state.accNum = promisePacket.accNum;
            state.accValue = promisePacket.accValue;
        }
        state.propMaj++;
        if (state.propMaj > hashPorts.size()/2) { //Only send out accept to all when getting promise from majority, and only do this for once
            state.state = Math.max(1,state.state);
            meetingInfo accValue = state.accValue==null ? log.getRepLog().get(promisePacket.LogIndex).proposedMeeting : state.accValue;
            state.accValue = accValue;
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
        boolean isProposer = log.checkIfLogEntryExist(acceptPacket.LogIndex); // true means I am concurrent proposer
        log.updateLogIndex(acceptPacket.LogIndex);
        //Use currentState as state if I am not a concurrent proposer
        State state;
        if (isProposer) {
            state = log.getRepLog().get(acceptPacket.LogIndex).getCurState();
        } else {
            state = log.getCurrentState();
        }
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
    public static void OnRecvAck(Dictionary dictionary, PaxosLog log, Packet ackPacket, String myName, HashMap<String, int[]> hashPorts, DatagramSocket datagramSocket, ScheduledThreadPoolExecutor delayedRepropose) {
        if (log.getRepLog().size() <= ackPacket.LogIndex) return;
        State state = log.getRepLog().get(ackPacket.LogIndex).getCurState();
        if (state.state > 1) return;
        //Does not have to check accNum or accVal when receiving ack
        state.ackMaj++;
        //Only send out commit to all when getting ack from majority, and only do this for once
        if (state.ackMaj > hashPorts.size()/2) {
            state.state = Math.max(2,state.state);
            delayedRepropose.shutdownNow(); //Cancel the scheduled post-timeout re-proposing process(es)

//            System.err.println("shut down executed");
//            System.err.println(delayedRepropose.getQueue().size());

            // commit itself
            // execute log to the dictionary
            boolean success = false;
            log.fillTheHole(ackPacket.LogIndex,ackPacket.accValue);

            if (ackPacket.accValue.getUser() != null) { //schedule
                success = dictionary.add(ackPacket.accValue); // check if conflict
            } else { //cancel
                success = dictionary.removeByName(ackPacket.accValue.getName()); // check if no such event
            };

            if (log.checkIfProposedMeetingAccepted(ackPacket.LogIndex) && success) {
                // COMMIT the log
//                log.fillTheHole(ackPacket.LogIndex,ackPacket.accValue);
                writeLog2File(log);
                if(CheckState(log)){
                    if(log.IfHoleExist()){
                        fillHolesReq(log, datagramSocket, null, hashPorts, myName);
                    } else {
                        writeDic2File(dictionary);
                    }
                }

                Packet commitPacket = new Packet(0,0, state.accValue, 4, ackPacket.LogIndex, 0, null, null, null);
                sendToAll(myName,hashPorts,datagramSocket,commitPacket);

                if (log.getRepLog().get(ackPacket.LogIndex).meeting.getUser() == null) { // cancel event

                    System.out.println("Cancel " + log.getRepLog().get(ackPacket.LogIndex).meeting.toString() + ".");
                } else { // schedule event
                    System.out.println("Meeting " + log.getRepLog().get(ackPacket.LogIndex).meeting.getName() +  " scheduled.");
                }
            } else {

//                System.err.println(ackPacket.LogIndex);
//                System.err.println(log.getRepLog().get(ackPacket.LogIndex).meeting.toString());
                if (log.getRepLog().get(ackPacket.LogIndex).meeting.getUser() == null) { // cancel event
                    System.out.println("Unable to cancel meeting " + log.getRepLog().get(ackPacket.LogIndex).meeting.getName() + ".");
                } else {
                    System.out.println("Unable to schedule meeting " + log.getRepLog().get(ackPacket.LogIndex).meeting.getName() + ".");
                }
                log.getRepLog().remove(log.getRepLog().size()-1);
            }

        }
    }

    /**
     * On receiving commit:
     * 1> fill in the value
     * 2> if the site is a proposer, output result on commondline
     * @param log
     * @param commitPacket
     */
    public static void OnRecvCommit(Dictionary dictionary, PaxosLog log, Packet commitPacket, DatagramSocket datagramSocket, HashMap<String, int[]> hashPorts, String myName) {
        //add holes if the log entry does not exist
        boolean isProposer = log.checkIfLogEntryExist(commitPacket.LogIndex); // true means I am concurrent proposer
        log.updateLogIndex(commitPacket.LogIndex);

        //Add new log entry if I am not a concurrent proposer
        if (!isProposer) {
            log.addLogEntry(0, commitPacket.propNum, commitPacket.accValue, null);
        } else { // Fill the empty log entry if I am a proposer
            log.fillTheHole(commitPacket.LogIndex, commitPacket.accValue);
        }
        // Execute the log to the dictionary
        boolean success = false;
        if (commitPacket.accValue.getUser() != null) { //schedule
            success = dictionary.add(commitPacket.accValue);
        } else { //cancel
            success = dictionary.removeByName(commitPacket.accValue.getName());
        }

        if (success) {
            writeLog2File(log);
            if(CheckState(log)){
                if(log.IfHoleExist()){
                    fillHolesReq(log, datagramSocket, null, hashPorts, myName);
                } else {
                    writeDic2File(dictionary);
                }
            }
        }
        if (log.checkIfProposedMeetingAccepted(commitPacket.LogIndex) && success) {
            if (log.getRepLog().get(commitPacket.LogIndex).meeting.getUser() == null) { // cancel event
                System.out.println("Cancel " + log.getRepLog().get(commitPacket.LogIndex).meeting.toString() + ".");
            } else { // schedule event
                System.out.println("Meeting " + log.getRepLog().get(commitPacket.LogIndex).meeting.getName() + " scheduled");
            }


        } else if (log.getRepLog().get(commitPacket.LogIndex).proposedMeeting != null) { // is a proposer
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
    /**
     *
     * used to update do checkpoint
     */
    public static void setCheckPoint(PaxosLog log, Dictionary dic){
        writeLog2File(log);
        if(log.getRepLog().size() % 5 == 0 ){
            writeDic2File(dic);
        }
    }

    /**
     * checkpoint status, true: is ok for checkpoint
     */
    public static boolean CheckState(PaxosLog log){
        return log.getRepLog().size()/5 == 0;
    }

    /**
     * operate log when recovery
     */
    public static void operateLog(PaxosLog log, Dictionary dic){
        if(log.getRepLog().size() != 0){
            int ind = 5*log.getRepLog().size()/5;
            for (int i = ind; i < log.getRepLog().size(); i++){
                meetingInfo m = log.getRepLog().get(i).getMeeting();
                if(m.getUser() == null){
                    dic.removeByName(m.getName());
                } else {
                    dic.add(m);
                }
            }
        }
    }


    /**
     *
     * @param log
     */

    public static void writeLog2File(PaxosLog log){
        try {
            // for log
            FileOutputStream f = new FileOutputStream(new File("log.txt"));
            ObjectOutputStream o = new ObjectOutputStream(f);
            // Write objects to file
            o.writeObject(log);
            o.close();
            f.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream for log write" );
        }
    }

    /**
     * Write dic to files
     * @param dic
     */

    public static void writeDic2File(Dictionary dic){
        try {
            // for log
            FileOutputStream f = new FileOutputStream(new File("dic.txt"));
            ObjectOutputStream o = new ObjectOutputStream(f);
            // Write objects to file
            o.writeObject(dic);
            o.close();
            f.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream for dic write");
        }
    }


    public static PaxosLog readlog(){
        PaxosLog myLog = null;
        try {
            // for log
            FileInputStream fi1 = new FileInputStream(new File("log.txt"));
            ObjectInputStream oi1 = new ObjectInputStream(fi1);
            // Read objects
            myLog = (PaxosLog) oi1.readObject();
            oi1.close();
            fi1.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream for read");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return myLog;
    }

    public static Dictionary readDic(){
        Dictionary myDic = null;
        try{
            FileInputStream fi1 = new FileInputStream(new File("dic.txt"));
            ObjectInputStream oi1 = new ObjectInputStream(fi1);
            // Read objects
            myDic = (Dictionary) oi1.readObject();
            oi1.close();
            fi1.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("Error initializing stream for read");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return myDic;
    }
}