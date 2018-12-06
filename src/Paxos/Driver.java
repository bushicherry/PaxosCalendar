package Paxos;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.*;

public class Driver {
    // set up timeout processes
    volatile static ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3);

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


        executor.setRemoveOnCancelPolicy(true);

        // set up a queue of unfinished operations (schedule or cancel)
//        Queue<meetingInfo> operations = new ConcurrentLinkedQueue<>();

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
                                    System.err.println("Received prepare");
                                    Algorithm.OnRecvPrepare(packet,log,hostsPorts,socket);
                                }
                                // receive promise
                                else if (packet.packetType == 1) {
                                    System.err.println("Received promise");
                                    Algorithm.OnRecvPromise(log,packet,myName,hostsPorts,socket);
                                }
                                // receive accept
                                else if (packet.packetType == 2) {
                                    System.err.println("Received accept");
                                    Algorithm.OnRecvAccept(log,packet,hostsPorts,socket);
                                }
                                // receive ack
                                else if (packet.packetType == 3) {
                                    System.err.println("Received ack");
                                    Algorithm.OnRecvAck(dictionary,log,packet,myName,hostsPorts,socket,executor);
                                }
                                // receive commit
                                else if (packet.packetType == 4) {
                                    System.err.println("Received commit");
                                    Algorithm.OnRecvCommit(dictionary,log,packet);
                                }
                                // receive fill holes request
                                else if (packet.packetType == 6) {
                                    System.err.println("Received fill holes request");
                                    Algorithm.DealWithHoleReq(myName,packet,socket,hostsPorts,log);
                                }
                                // receive reply of fill holes request
                                else if (packet.packetType == 7) {
                                    System.err.println("Received reply of fill holes request");
                                    Algorithm.fillHoleResp(dictionary,packet,log,myName,hostsPorts,socket);
                                }
                                // receive log number request
                                else if (packet.packetType == 8) {
                                    System.err.println("Received log number request");
                                    Algorithm.responseMaxlog(log,packet,socket,hostsPorts,myName);
                                }
                                // receive reply of log number
                                else if (packet.packetType == 9) {
                                    System.err.println("Received reply of log number");
                                    Algorithm.OnrecvMaxHoles(packet, log, myIndex);
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

                        }
                    };

                    new Thread(recvMsg).start();
                }
            }
        };

        Thread thread = new Thread(udpListener);
        thread.setDaemon(true);
        thread.start();


        // delayed repropose process
        Runnable delayedOperation = new Runnable() {
            @Override
            public void run() {
                System.err.println("delayed operation executed");
                Algorithm.rePropose(log.getCurrentLogIndex(),log,myName,hostsPorts,socket);
            }
        };

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
                date[0] = Integer.parseInt(dateArray[0]);
                date[1] = Integer.parseInt((dateArray[1]));
                date[2] = Integer.parseInt(dateArray[2]);

                // get start and end time
                String[] startStr =  commandS.next().split(":");
                String[] endStr = commandS.next().split(":");

                // for startTime and endTime
                startTime[0] = Integer.parseInt(startStr[0]);
                startTime[1] = Integer.parseInt(startStr[1]);

                //end time
                endTime[0] = Integer.parseInt(endStr[0]);
                endTime[1] = Integer.parseInt(endStr[1]);


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
                    System.out.println("444Unable to schedule meeting " + name +".");
                    continue;
                }

                // create a meetingInfo object for the proposed event
                meetingInfo proposedMeeting = new meetingInfo(name, date, startTime, endTime, participants);

                // when the site is the only site in the system, then it doesn't have to
                // execute the Paxos algorithm
                if (numOfHosts == 1) {
                    if (dictionary.add(proposedMeeting)) {
                        log.addLogEntry(1, 0, proposedMeeting, proposedMeeting);
                        System.out.println("555Meeting " + name + " scheduled.");
                    } else {
                        System.out.println("666Unable to schedule meeting " + proposedMeeting.getName() + ".");
                    }
                    continue;
                } else {

                    // schedule timeouts
                    executor.schedule(delayedOperation, 1000, TimeUnit.MILLISECONDS);
                    executor.schedule(delayedOperation, 2000, TimeUnit.MILLISECONDS);
                    ScheduledFuture<?> sf = executor.schedule(new Runnable() {
                        @Override
                        public void run() {
                            // remove the empty log
                            log.getRepLog().remove(log.getCurrentLogIndex());
                            System.out.println("333Unable to schedule meeting " + name + ".");
                        }
                    }, 3000, TimeUnit.MILLISECONDS);

                    if (log.IfHoleExist()) {
                        Algorithm.fillHolesReq(log, socket, proposedMeeting, hostsPorts, myName);
                    } else {
                        Algorithm.propose(proposedMeeting, log, myName, hostsPorts, socket);
                    }

                    //block the thread before timeout ends or shutdown
                    while ((!executor.isShutdown()) && (!sf.isDone())) {System.err.println("Not yet");}
                    if (executor.isShutdown()) {
                        executor = new ScheduledThreadPoolExecutor(3);
                        executor.setRemoveOnCancelPolicy(true);
                    }
                }


            }
            else if (command.equals("cancel")) {
                String name = commandS.next();


                // check if the operation is to cancel an event that has already been canceled
                if (log.checkIfCancelExists(name)) {
                    System.out.println("111Unable to cancel meeting " + name + ".");
                    continue;
                }

                if (dictionary.hasMeeting(name)) {
                    meetingInfo proposedCancelMeeting = new meetingInfo(name, null, null, null, null);
                    // schedule timeouts
                    executor.schedule(delayedOperation, 1000, TimeUnit.MILLISECONDS);
                    executor.schedule(delayedOperation, 2000, TimeUnit.MILLISECONDS);
                    ScheduledFuture<?> sf = executor.schedule(new Runnable() {
                        @Override
                        public void run() {
                            // remove the empty log
                            log.getRepLog().remove(log.getCurrentLogIndex());
                            System.out.println("222Unable to cancel meeting " + name + ".");
                        }
                    }, 3000, TimeUnit.MILLISECONDS);

                    if (log.IfHoleExist()) {
                        Algorithm.fillHolesReq(log, socket, proposedCancelMeeting, hostsPorts, myName);
                    } else {
                        Algorithm.propose(proposedCancelMeeting, log, myName, hostsPorts, socket);
                    }

                    //block the thread before timeout ends or shutdown
                    while ((!executor.isShutdown()) && (!sf.isDone())) {System.err.println("Not yet");}
                    if (executor.isShutdown()) {
                        executor = new ScheduledThreadPoolExecutor(3);
                        executor.setRemoveOnCancelPolicy(true);
                    }
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
                break;
            }
            else {
                System.out.println("The command is not recognizable. Please follow the following formats:\n" +
                        "schedule <name> <day> <start_time> <end_time> <participants>\n" +
                        "cancel <name>\nview\nmyview\nlog");
            }
        }
    }
}
