package Paxos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class WBAlgorithm {

    // True means it's fine to make the schedule
    public static boolean ifFine(meetingInfo a, meetingInfo b){
        return ( !(a.end.after(b.start)) ) || ( !(b.end.after(a.start)) );
    }

    public static boolean Insert(LogAndDic m, meetingInfo e, HashMap<String, int[] > myhash, String myname, DatagramSocket socket){
        // First check the collision
        if(m.check_collision(e) != null){
            System.out.println("Unable to schedule meeting " + e.name);
            return false;
        } else {
            m.Insert(e);
            System.out.println("Meeting " + e.name + " scheduled");
//            System.out.println(e.users);
//            System.out.println(myhash);
            //send
            LogAndDic.sendPac pac_log = m.get_log_pac();
            LogAndDic.sendPac pac_dic = m.get_dic_pac();
            save_process(pac_log, pac_dic, m.get_usernum());

            udpsend(e.users,myhash,m,myname, socket);
            return true;

        }
    }
    public static boolean Cancel(LogAndDic m, String mt, String s, HashMap<String, int[] > myhash, DatagramSocket socket){
        // check if self in the meeting
        if(m.check_user(mt,s)){
            // get users
            Vector<String> meetingusers = m.get_user(mt);
            m.Delete(mt);
            System.out.println("Meeting " + mt + " cancelled");

            //for recovery
            LogAndDic.sendPac pac_log = m.get_log_pac();
            LogAndDic.sendPac pac_dic = m.get_dic_pac();
            save_process(pac_log, pac_dic, m.get_usernum());

            //send
            udpsend(meetingusers, myhash, m, s, socket);
            return true;
        } else {
            System.out.println("User is either not in the meeting or the meeting doesn't exist");
            return false;
        }
    }

    public static void Onrec(LogAndDic m, LogAndDic.sendPac pac, String myname, HashMap<String, int[] > myhash, DatagramSocket socket){
        //check pac's avalable:
        m.dealWithReceive(pac, myname, myhash,socket);

        LogAndDic.sendPac pac_log = m.get_log_pac();
        LogAndDic.sendPac pac_dic = m.get_dic_pac();
        save_process(pac_log, pac_dic, m.get_usernum());

    }

    private static void udpsend(Vector<String> meetingusers, HashMap<String, int[] > myhash, LogAndDic m, String s, DatagramSocket socket){
        if(meetingusers.size() > 0) {
            for (String uname : meetingusers) {
                int proIndex = myhash.get(uname)[1];
                if (proIndex != m.get_process()) {
                    LogAndDic.sendPac my_pac = m.PacReady(proIndex);
                    //send
                    int client_port = myhash.get(uname)[0];
                    UdpSender udpSender = new UdpSender(socket, client_port, uname, myhash.size(), my_pac);
                    new Thread(udpSender).start();
                }
            }
        }
    }

    public static LogAndDic.sendPac byte2sendPac(byte[] buffer, int numOfHosts) {
        byte[] tmp = Arrays.copyOfRange(buffer,0,64);
        String msg_recv = new String(tmp).trim();
        int index_recv = buffer[64];
        int[][] Ti_recv = new int[numOfHosts][numOfHosts];
        for (int i=0; i<numOfHosts; i++) {
            for (int j=0; j<numOfHosts; j++) {
                Ti_recv[i][j] = buffer[65+i*numOfHosts+j];
            }
        }
        Vector<LogAndDic.eRecord> NP_recv = new Vector<>();

        int offset = 65+numOfHosts*numOfHosts;
        byte comma = ',';
        while (buffer[offset]==comma) {
            int ts_recv = buffer[offset+1];
            ts_recv = ts_recv*128+buffer[offset+2];

            int Pi_recv = buffer[offset+3];

            tmp = Arrays.copyOfRange(buffer,offset+4,offset+24);
            String name_recv = new String(tmp).trim();

            meetingInfo op_recv;

            if (buffer[offset+24]!=(byte)0) {
                int month_recv = buffer[offset+24];
                int day_recv = buffer[offset+25];
                int year_recv = buffer[offset+26];
                year_recv = year_recv*128+buffer[offset+27];
                int shour_recv = buffer[offset+28];
                int smin_recv = buffer[offset+29];
                int ehour_recv = buffer[offset+30];
                int emin_recv = buffer[offset+31];

                offset = offset+32;
                Vector<String> users_recv = new Vector<>();
                while (buffer[offset]==comma) {
                    tmp = Arrays.copyOfRange(buffer,offset+1,offset+26);
                    users_recv.add(new String(tmp).trim());
                    offset += 26;
                }

                op_recv = new meetingInfo(name_recv,
                        new GregorianCalendar(year_recv, month_recv, day_recv),
                        new GregorianCalendar(year_recv, month_recv, day_recv, shour_recv, smin_recv),
                        new GregorianCalendar(year_recv, month_recv, day_recv, ehour_recv, emin_recv),
                        users_recv);
                offset++;
            } else {
                op_recv = new meetingInfo(name_recv);
                offset += 25;
            }

            NP_recv.add(new LogAndDic.eRecord(op_recv, ts_recv, Pi_recv));
        }
        LogAndDic.sendPac answer;
        answer = new LogAndDic.sendPac(msg_recv,Ti_recv,NP_recv,index_recv);

        return answer;
    }

    public static int sendPac2byte(LogAndDic.sendPac pac, byte[] buffer, int numOfPorts) {
        byte[] msg_byte = pac.msg.getBytes(); //msg size <= 64
        int offset = 0;
        for (byte t : msg_byte) {
            buffer[offset] = t;
            offset++;
        }

        buffer[64] = (byte)pac.index;

        for (int m=0; m<numOfPorts; m++) {
            for (int n=0; n<numOfPorts; n++) {
                buffer[65+m*numOfPorts+n] = (byte)pac.Ti[m][n];
            }
        }
        offset = 65+numOfPorts*numOfPorts;

        for (LogAndDic.eRecord eR : pac.NP) {
            buffer[offset] = ',';
            buffer[offset+1] = (byte)(eR.tm/128);
            buffer[offset+2] = (byte)(eR.tm%128);
            buffer[offset+3] = (byte)eR.P_ind;

            byte[] name_byte = eR.op.name.getBytes(); // name size <= 20
            offset += 4;
            int i = 0;
            for (byte t : name_byte) {
                buffer[offset+i] = t;
                i++;
            }
            offset+= 20;

            if (eR.op.day!=null) {
                buffer[offset] = (byte)(eR.op.day.get(Calendar.MONTH));
                buffer[offset+1] = (byte)(eR.op.day.get(Calendar.DATE));
                buffer[offset+2] = (byte)(eR.op.day.get(Calendar.YEAR)/128);
                buffer[offset+3] = (byte)(eR.op.day.get(Calendar.YEAR)%128);
                buffer[offset+4] = (byte)(eR.op.start.get(Calendar.HOUR_OF_DAY));
                buffer[offset+5] = (byte)(eR.op.start.get(Calendar.MINUTE));
                buffer[offset+6] = (byte)(eR.op.end.get(Calendar.HOUR_OF_DAY));
                buffer[offset+7] = (byte)(eR.op.end.get(Calendar.MINUTE));

                offset += 8;
                for (String s : eR.op.users) {
                    buffer[offset] = ',';
                    byte[] user_byte = s.getBytes();
                    int j = 0;
                    for (byte t : user_byte) {
                        buffer[offset+j+1] = t;
                        j++;
                    }
                    offset += 26;
                }
            }

            offset++;

        }
        return offset;
    }

    public static void save_process(LogAndDic.sendPac log_pac, LogAndDic.sendPac dic_pac, int numOfPorts){
        byte[] logBuf = new byte[65507];
        byte[] dicBuf = new byte[65507];

        sendPac2byte(log_pac,logBuf,numOfPorts);
        sendPac2byte(dic_pac,dicBuf,numOfPorts);

        File logFile = new File("log.txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path path1 = Paths.get("log.txt");
        try {
            Files.write(path1, logBuf);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File dicFile = new File("log.txt");
        if (!dicFile.exists()) {
            try {
                dicFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Path path2 = Paths.get("dic.txt");
        try {
            Files.write(path2, dicBuf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LogAndDic.sendPac[] reload_pro(int numOfHosts){
        LogAndDic.sendPac[] answer = new LogAndDic.sendPac[2];
        try {
            File logFile = new File("log.txt");
            byte[] buffer1 = new byte[(int) logFile.length()];

            if (logFile.exists()) {
                //read file into bytes[]
                FileInputStream ifstream = new FileInputStream(logFile);
                ifstream.read(buffer1);

                try {
                    Path path = Paths.get("log.txt");
                    Files.write(path, buffer1);
                    answer[0] = byte2sendPac(buffer1,numOfHosts);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                answer[0] = null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            File dicFile = new File("dic.txt");
            byte[] buffer2 = new byte[(int) dicFile.length()];

            if (dicFile.exists()) {
                //read file into bytes[]
                FileInputStream ifstream2 = new FileInputStream(dicFile);
                ifstream2.read(buffer2);

                try {
                    Path path2 = Paths.get("dic.txt");
                    Files.write(path2, buffer2);
                    answer[1] = byte2sendPac(buffer2,numOfHosts);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                answer[1] = null;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return answer;

    }


}

