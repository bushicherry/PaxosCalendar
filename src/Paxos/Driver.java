//package java;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.net.DatagramPacket;
//import java.net.DatagramSocket;
//import java.net.SocketException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.LocalTime;
//import java.util.*;
//
//public class Driver {
//
//    public static void main(String[] args) throws SocketException {
////        Scanner hostsInput;
////        int numHosts=0;
////        HashMap<String, int[] > hostsPorts = new HashMap<>();
////
////        // attempts to create scanner for 'knownhosts_udp.txt'
////        try
////        {
////            hostsInput = new Scanner(new File("knownhosts_udp.txt"));
////        }
////        catch(FileNotFoundException e)
////        {
////            System.out.println("File 'knownhosts_udp.txt' doesn't exist.");
////            return;
////        }
//
////        // reads the file line by line
////        while(hostsInput.hasNextLine()) {
////            numHosts = numHosts+1;
////            int[] intArr = new int[2];
////            String tmpName = hostsInput.next();
////            intArr[0] = Integer.parseInt(hostsInput.next());
////            intArr[1] = numHosts;
////            hostsPorts.put(tmpName,intArr);
////        }
//
//        // test output the map
//        /*for (HashMap.Entry<String,Integer> entry : hostsPorts.entrySet()) {
//            System.out.println(entry.getKey() + " " + entry.getValue() + "\n");
//        }*/
//
////        final String myName = args[0];
////        final int myPort = hostsPorts.get(myName)[0];
////        final int myIndex = hostsPorts.get(myName)[1];
////        final int numOfHosts = numHosts;
//        // set up log and dictionary
////        final LogAndDic logAndDic = new LogAndDic(numHosts,myIndex);
////        LogAndDic.sendPac[] recov = WBAlgorithm.reload_pro(numOfHosts);
////        logAndDic.read_DicAlog_pac(recov[0], recov[1]);
////        final DatagramSocket socket = new DatagramSocket(myPort);
//
//
//        // set up udp socket on a new thread to listen for msgs from other sites
//        Runnable udpListener = new Runnable() {
//
//            @Override
//            public void run() {
//                System.out.println(myName + ": start listening for commands.");
//                while (true) {
//                    byte[] buffer = new byte[65507];
//                    DatagramPacket packet = new DatagramPacket(buffer,0,buffer.length);
//                    try {
//                        socket.receive(packet);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                        return;
//                    }
//
//                    Runnable recvMsg = new Runnable() {
//                        @Override
//                        public synchronized void run() {
//                            // byte to sendPac
//                            LogAndDic.sendPac pac = WBAlgorithm.byte2sendPac(buffer,numOfHosts);
//                            WBAlgorithm.Onrec(logAndDic, pac, myName, hostsPorts, socket);
//                        }
//                    };
//
//                    new Thread(recvMsg).start();
//                }
//            }
//        };
//
//        new Thread(udpListener).start();
//
//
//        // loop to deal with commands
//        while (true) {
//            Scanner commandS = new Scanner(System.in);
//            String command;
//
//            command = commandS.next();
//            if (command.equals("schedule")) {
//                String name = commandS.next();
//
//                String dateStr = commandS.next();
//                String pattern = "MM/dd/yyyy";
//                Calendar date = Calendar.getInstance();
//                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);
//                try {
//                    date.setTime(sdf.parse(dateStr));// all done
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                    continue;
//                }
//
//                String startStr =  commandS.next();
//                String endStr = commandS.next();
//                LocalTime startTime;
//                LocalTime endTime;
//                try {
//                    startTime = LocalTime.parse(startStr);
//                    endTime = LocalTime.parse(endStr);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    continue;
//                }
//                // check if the times are in 30 minutes increments
//                if (startTime.getMinute()%30 != 0 || endTime.getMinute()%30 != 0) {
//                    System.out.println("Unable to schedule meeting " + name +".");
//                    continue;
//                } else if( startTime.isAfter(endTime) ){
//                    System.out.println("Unable to schedule meeting <Start time after end time>" + name +".");
//                    continue;
//                }
//
//                Vector<String> participants = new Vector<>();
//                boolean selfIncluded = false;
//                String users = commandS.next();
//                String[] userArray = users.split(",");
//                for (String u : userArray) {
//                    if (u.equals(myName)) selfIncluded = true;
//                    participants.add(u);
//                }
//                if (!selfIncluded) {
//                    System.out.println("Unable to schedule meeting " + name +".");
//                    continue;
//                }
//
//                GregorianCalendar gDate = new GregorianCalendar(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
//                GregorianCalendar gStartTime = new GregorianCalendar(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), startTime.getHour(), startTime.getMinute());
//                GregorianCalendar gEndTime = new GregorianCalendar(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE), endTime.getHour(), endTime.getMinute());
//                meetingInfo m = new meetingInfo(name, gDate, gStartTime, gEndTime, participants);
//
//                WBAlgorithm.Insert(logAndDic, m, hostsPorts, myName, socket);
//
//
//
//            }
//            else if (command.equals("cancel")) {
//                String name = commandS.next();
//                WBAlgorithm.Cancel(logAndDic,name,myName,hostsPorts,socket);
//            }
//            else if (command.equals("view")) {
//                logAndDic.View_dic();
//            }
//            else if (command.equals("myview")) {
//                logAndDic.myView(myName);
//            }
//            else if (command.equals("log")) {
//                logAndDic.View_log();
//            }
//            else {
//                System.out.println("The command is not recognizable. Please follow the following formats:\n" +
//                        "schedule <name> <day> <start_time> <end_time> <participants>\n" +
//                        "cancel <name>\nview\nmyview\nlog");
//            }
//        }
//    }
//
//}
