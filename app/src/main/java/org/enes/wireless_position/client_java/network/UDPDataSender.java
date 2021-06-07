package org.enes.wireless_position.client_java.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPDataSender {

    private static final String server_address = "192.168.1.178";

    private static final int server_port = 8080;

    public static void sendDataToServer(String data) {
        byte[] buf = data.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
        datagramPacket.setPort(server_port);
        try {
            datagramPacket.setAddress(InetAddress.getByName(server_address));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket();

        } catch (SocketException e) {
            e.printStackTrace();
        }
        if(datagramSocket != null) {
            try {
                datagramSocket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
                datagramSocket.close();
            } finally {
                datagramSocket.close();
            }
        }
    }

}
