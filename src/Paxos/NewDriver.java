package Paxos;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.*;

public class NewDriver {
    public static void main(String[] args) throws SocketException {
        Scanner hostsInput;
        int numHosts=0;
        HashMap<String, int[] > hostsPorts = new HashMap<>();
        // attempts to create scanner for 'knownhosts_udp.txt'
        try
        {
            hostsInput = new Scanner(new File("knownhosts_udp.txt"));
        }
        catch(FileNotFoundException e)
        {
            System.out.println("File 'knownhosts_udp.txt' doesn't exist.");
            return;
        }

        // reads the file line by line
        while(hostsInput.hasNextLine()) {
            numHosts = numHosts+1;
            int[] intArr = new int[2];
            String tmpName = hostsInput.next();
            intArr[0] = Integer.parseInt(hostsInput.next());
            intArr[1] = numHosts;
            hostsPorts.put(tmpName,intArr);
        }

        final String myName = args[0];
        final int myPort = hostsPorts.get(myName)[0];
        final int myIndex = hostsPorts.get(myName)[1];
        final int numOfHosts = numHosts;

        // set up datagram socket
        final DatagramSocket socket = new DatagramSocket(myPort);

        // set up new log and dic
        final PaxosLog log = new PaxosLog(myIndex);
        final Dictionary dictionary = new Dictionary();

        // set up timeout processes
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
        final ScheduledFuture<?>[] sf = {};

        // set up a queue of unfinished operations (schedule or cancel)
        final Queue<meetingInfo> operations = new ConcurrentLinkedQueue<>();
        // set up a new thread to process operations
        Runnable operationProcessor = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    meetingInfo currentOperation = operations.poll();
                    if (currentOperation != null) {
                        Runnable delayedOperation = new Runnable() {
                            @Override
                            public void run() {
                                Algorithm.rePropose(log.getCurrentLogIndex(),log,myName,hostsPorts,socket);
                            }
                        };
                        executor.schedule(delayedOperation, 1, TimeUnit.SECONDS);
                        executor.schedule(delayedOperation, 2, TimeUnit.SECONDS);
                        sf[0] = executor.schedule(new Runnable() {
                            @Override
                            public void run() {
                                // remove the empty log
                                log.getRepLog().remove(log.getCurrentLogIndex());

                                if (currentOperation.getUser() == null) { // cancel event
                                    System.out.println("Unable to cancel meeting " + currentOperation.getName() + ".");
                                } else {
                                    System.out.println("Unable to schedule meeting " + currentOperation.getName() + ".");
                                }
                            }
                        }, 3, TimeUnit.SECONDS);

                        if (log.IfHoleExist()) {
                            Algorithm.fillHolesReq(log, socket, currentOperation, hostsPorts, myName);
                        } else {
                            Algorithm.propose(currentOperation, log, myName, hostsPorts, socket);
                        }

                        //block the processor thread before timeout ends or shutdown
                        while (!sf[0].isDone()) {}
                    }
                }
            }
        };
        new Thread(operationProcessor).start();

        // Message Listener
        // set up udp socket on a new thread to listen for msgs from other sites
        Runnable udpListener = new Runnable() {

            @Override
            public void run() {
                System.out.println(myName + ": start listening for msgs.");
                while (true) {
                    byte[] buffer = new byte[65507];
                    DatagramPacket datagramPacket = new DatagramPacket(buffer,0,buffer.length);
                    try {
                        socket.receive(datagramPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }

                    Runnable recvMsg = new Runnable() {
                        @Override
                        public synchronized void run() {
                            ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                            ObjectInput in = null;
                            try {
                                in = new ObjectInputStream(bis);
                                Packet packet = (Packet) in.readObject();

                                // receive prepare
                                if (packet.packetType == 0) {
                                    Algorithm.OnRecvPrepare(packet,log,hostsPorts,socket);
                                }
                                // receive promise
                                else if (packet.packetType == 1) {
                                    Algorithm.OnRecvPromise(log,packet,myName,hostsPorts,socket);
                                }
                                // receive accept
                                else if (packet.packetType == 2) {
                                    Algorithm.OnRecvAccept(log,packet,hostsPorts,socket);
                                }
                                // receive ack
                                else if (packet.packetType == 3) {
                                    Algorithm.OnRecvAck(dictionary,log,packet,myName,hostsPorts,socket,executor);
                                }
                                // receive commit
                                else if (packet.packetType == 4) {
                                    Algorithm.OnRecvCommit(log,packet);
                                }
                                // receive fill holes request
                                else if (packet.packetType == 6) {
                                    Algorithm.DealWithHoleReq(myName,packet,socket,hostsPorts,log);
                                }
                                // receive reply of fill holes request
                                else if (packet.packetType == 7) {
                                    Algorithm.fillHoleResp(dictionary,packet,log,myName,hostsPorts,socket);
                                }
                                // receive hole
                                else if (packet.packetType == 8) {
                                    ??
                                }
                                // receive promise
                                else if (packet.packetType == 9) {
                                    ??
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (in != null) {
                                        in.close();
                                    }
                                } catch (IOException ex) {
                                    // ignore close exception
                                }
                            }

//                            // byte to sendPac
//                            LogAndDic.sendPac pac = WBAlgorithm.byte2sendPac(buffer,numOfHosts);
//                            WBAlgorithm.Onrec(logAndDic, pac, myName, hostsPorts, socket);
                        }
                    };

                    new Thread(recvMsg).start();
                }
            }
        };

        new Thread(udpListener).start();


        // Commandline Scanner
        while (true) {
            Scanner commandS = new Scanner(System.in);
            String command;

            command = commandS.next();
            if (command.equals("schedule")) {
                // construct date, start, end
                int[] date = new int[3];
                int[] startTime = new int[2];
                int[] endTime = new int[2];

                // get name
                String name = commandS.next();
                String dateStr = commandS.next();
                // set up date

                String[] dateArray = dateStr.split("/");
                // for date array
                date[0] = Integer.getInteger(dateArray[0]);
                date[1] = Integer.getInteger((dateArray[1]));
                date[2] = Integer.getInteger(dateArray[2]);

                // get start and end time
                String[] startStr =  commandS.next().split(":");
                String[] endStr = commandS.next().split(":");

                // for startTime and endTime
                startTime[0] = Integer.getInteger(startStr[0]);
                startTime[1] = Integer.getInteger(startStr[1]);

                //end time
                endTime[0] = Integer.getInteger(endStr[0]);
                endTime[1] = Integer.getInteger(endStr[1]);


                HashSet<String> participants = new HashSet<>();
                boolean selfIncluded = false;
                String users = commandS.next();
                String[] userArray = users.split(",");
                for (String u : userArray) {
                    if (u.equals(myName)) selfIncluded = true;
                    participants.add(u);
                }
                // user cannot propose an event that does not involve him/herself
                if (!selfIncluded) {
                    System.out.println("Unable to schedule meeting " + name +".");
                    continue;
                }

                // create a meetingInfo object for the proposed event
                meetingInfo proposedMeeting = new meetingInfo(name, date, startTime, endTime, participants);

                // when the site is the only site in the system, then it doesn't have to
                // execute the Paxos algorithm
                if (numOfHosts == 1) {
                    if (dictionary.add(proposedMeeting)) {
                        log.addLogEntry(1, 0, proposedMeeting, proposedMeeting);
                        System.out.println("Schedule " + proposedMeeting.toString() + ".");
                    } else {
                        System.out.println("Unable to schedule meeting " + proposedMeeting.getName() + ".");
                    }
                    continue;
                }

                operations.add(proposedMeeting);


            }
            else if (command.equals("cancel")) {
                String name = commandS.next();
                if (dictionary.hasMeeting(name)) {
                    meetingInfo proposedCancelMeeting = new meetingInfo(name, null, null, null, null);
                    operations.add(proposedCancelMeeting);
                }
            }
            else if (command.equals("view")) {
                dictionary.printEntireDic();
            }
            else if (command.equals("myview")) {
                dictionary.printIndividualDic(myName);
            }
            else if (command.equals("log")) {
                log.PrintLog();
            }
            else if (command.equals("exit")){

            }
            else {
                System.out.println("The command is not recognizable. Please follow the following formats:\n" +
                        "schedule <name> <day> <start_time> <end_time> <participants>\n" +
                        "cancel <name>\nview\nmyview\nlog");
            }
        }
    }
}
