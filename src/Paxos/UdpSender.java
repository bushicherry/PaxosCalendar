package Paxos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Calendar;

public class UdpSender {

    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;

    public UdpSender(DatagramSocket datagramSocket, int clientPort, String clientName, Packet packet) {
        this.datagramSocket = datagramSocket;

        // packet to byte[]
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(packet);
            out.flush();
            byte[] buffer = bos.toByteArray();
            datagramPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(clientName), clientPort);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    public synchronized void run() {
        try {
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        datagramSocket.close();
    }

}
