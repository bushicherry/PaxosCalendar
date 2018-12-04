package Paxos;

import java.io.IOException;
import java.net.*;
import java.util.Calendar;

public class UdpSender {

    private DatagramSocket socket;
    private DatagramPacket packet;

    public UdpSender(DatagramSocket socket, int clientPort, String clientName, int numOfPorts, LogAndDic.sendPac pac) {
        try {
            // pac to byte[]
            byte[] buffer = new byte[65507];
            int offset = Algorithm.sendPac2byte(pac, buffer, numOfPorts);

            byte[] buffer = new byte[65507];
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





            this.socket = socket;
            packet = new DatagramPacket(
                    buffer,
                    offset+1,
                    InetAddress.getByName(clientName),
                    clientPort
            );
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void run() {
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        socket.close();
    }

}
