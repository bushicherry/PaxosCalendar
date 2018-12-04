package Paxos;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

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
//                                System.out.println("Receive: ");
//                                System.out.println(m.toString());
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
        // open new thread
        //                 -> for package received
        //==========

        //==========


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
                if (!selfIncluded) {
                    System.out.println("Unable to schedule meeting " + name +".");
                    continue;
                }

                // set up meetingInfo, and do insert
                //=========
                // if holes
                // send fill hole request




                //==========



                // Propose to acceptors



            }
            else if (command.equals("cancel")) {

            }
            else if (command.equals("view")) {

            }
            else if (command.equals("myview")) {

            }
            else if (command.equals("log")) {

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
