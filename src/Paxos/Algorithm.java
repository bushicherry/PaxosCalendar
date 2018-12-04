package Paxos;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;

public class Algorithm {


    /**
     * fill hole then send propose
     */
    public static void fillHolesReq(PaxosLog Plog, DatagramSocket socket, meetingInfo meeting, HashMap<String, int[] > HashPorts, String myname){
        // check if there is missing hole
        if(!Plog.IfHoleExist())return;
        //learn holes
        Packet tempPac = new Packet(0,0 , meeting, 6, 0,Plog.getSiteID(), myname  , Plog.MissingLogIndex(), null);
        sendToAll(myname, HashPorts, socket, tempPac);
    }

    /**
     * When recv the filling hole request
     */

    public static void DealWithHoleReq(String myName,Packet pac, DatagramSocket Socket, HashMap<String, int[] > HashPorts, PaxosLog pLog){
        Packet tempPac = new Packet(0,0, pac.accValue, 7, 0,  pLog.getSiteID(), myName, null, null);
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

    public static void fillHoleResp(Packet pac, PaxosLog Plog){
        if(!Plog.IfHoleExist()) return;
        int recvLognum = pac.RespLogArray.size();
        for(int i = 0; i < recvLognum;i++){
            int ind = pac.RespLogArray.get(i).getLogIndex();
            int myAccNum = Plog.getRepLog().get(ind).getCurState().accNum;
            int hisAccNum = pac.RespLogArray.get(i).getCurState().accNum;
            if( myAccNum <= hisAccNum ){
                Plog.getRepLog().get(ind).getCurState().accNum = hisAccNum;
                Plog.insertEmptyLog(ind,pac.RespLogArray.get(i).getMeeting());
            }
        }
    }

    /**
     * for real
     */

    public static void fillHoleResp(Packet pac, PaxosLog Plog, String myName, HashMap<String, int[] > HashPorts,  DatagramSocket socket){
        if(!Plog.IfHoleExist()) return;
        int recvLognum = pac.RespLogArray.size();
        for(int i = 0; i < recvLognum;i++){
            int ind = pac.RespLogArray.get(i).getLogIndex();
            int myAccNum = Plog.getRepLog().get(ind).getCurState().accNum;
            int hisAccNum = pac.RespLogArray.get(i).getCurState().accNum;
            if( myAccNum <= hisAccNum ){
                Plog.getRepLog().get(ind).getCurState().accNum = hisAccNum;
                Plog.insertEmptyLog(ind,pac.RespLogArray.get(i).getMeeting());
            }
        }
        if(!Plog.IfHoleExist()){
            if(pac.accValue != null) {
                Propose(Plog, myName, HashPorts, socket);
                System.out.println("Poposed, wait for response");
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
     * @param HashPorts
     * @param socket
     * Before doing proposal, you should check if there is any hole exists.
     */
    public static void Propose(PaxosLog Plog, String myname, HashMap<String, int[] > HashPorts,  DatagramSocket socket){
        // get a new proposer number:
        int PropNum = Plog.getSiteID() + 100;
        // Add an empty log into Paxoslog
        int index = Plog.getRepLog().size();
        Packet tempPac = new Packet(PropNum, 0, null, 0, index,Plog.getSiteID(),myname, null,null);
        sendToAll(myname, HashPorts, socket, tempPac);
    }




    /**
     * On receiving promise(accNum,accVal):
     * 1> update State.accNum and accVal when accNum is larger than the stored value
     * 2> add up the counter and check if it reaches majority
     * 3> if so, send packet to all other sites
     * @param log
     * @param promisePacket
     * @param myName
     * @param hashPorts
     * @param datagramSocket
     */
    public static void OnRecvPromise(PaxosLog log, Packet promisePacket, String myName, HashMap<String, int[] > hashPorts, DatagramSocket datagramSocket) {
        State state = log.getRepLog().get(promisePacket.LogIndex).getCurState();
        if (promisePacket.accNum > state.accNum) {
            state.accNum = promisePacket.accNum;
            state.accValue = promisePacket.accValue;
        }
        state.propMaj++;
        if (state.propMaj > hashPorts.size()/2) {
            Packet acceptPacket = new Packet(log.getLastPropNum(), 0, state.accValue, 2, promisePacket.LogIndex, hashPorts.get(myName)[1], myName, null, null);
            sendToAll(myName,hashPorts,datagramSocket,acceptPacket);
        }
    }


    public static void OnRecvAccept(PaxosLog log, Packet acceptPacket, HashMap<String, int[] > hashPorts, DatagramSocket datagramSocket) {
        State state = log.getRepLog().get(acceptPacket.LogIndex).getCurState();

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