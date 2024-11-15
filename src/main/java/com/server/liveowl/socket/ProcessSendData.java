package com.server.liveowl.socket;

import com.server.liveowl.util.UdpHandler;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static com.server.liveowl.socket.SocketServer.maxDatagramPacketLength;

class ProcessSendData implements Runnable {
    DatagramSocket socket;
    public InetAddress addressTeacher;
    public int portTeacher;
    ProcessSendData(DatagramPacket packet) throws SocketException {
        socket = new DatagramSocket(9080);
        addressTeacher = packet.getAddress();
        portTeacher = packet.getPort();
    }
    @Override
    public void run() {
        while(true) {
            try {
                String packetId = SocketServer.listIds.poll();
                if (packetId != null) {
                    byte[] imageByteArray = SocketServer.imageBuffer.get(packetId);
                    int pos = packetId.lastIndexOf(":");
                    int imageId = Integer.parseInt(packetId.substring(0, pos));
                    int clientId = Integer.parseInt(packetId.substring(pos + 1));
                    int sequenceNumber = 0;
                    boolean flag;
                    int length = imageByteArray.length;
                    byte[] lengthBytes = new byte[maxDatagramPacketLength];
                    lengthBytes[0] = (byte) 0;
                    lengthBytes[1] = (byte) (clientId);
                    lengthBytes[2] = (byte) (imageId);
                    lengthBytes[3] = (byte) (length >> 16);
                    lengthBytes[4] = (byte) (length >> 8);
                    lengthBytes[5] = (byte) (length);
                    lengthBytes[6] = (byte) ((length + maxDatagramPacketLength - 6) / (maxDatagramPacketLength - 5));
                    UdpHandler.sendBytesArray(socket, lengthBytes, addressTeacher, portTeacher);
                    for (int i = 0; i < length; i = i + maxDatagramPacketLength - 5) {
                        sequenceNumber += 1;
                        byte[] message = new byte[maxDatagramPacketLength];
                        message[0] = (byte) (1);
                        message[1] = (byte) (clientId);
                        message[2] = (byte) (imageId);
                        message[3] = (byte) (sequenceNumber);
                        if ((i + maxDatagramPacketLength - 5) >= imageByteArray.length) {
                            flag = true;
                            message[4] = (byte) (1);
                        } else {
                            flag = false;
                            message[4] = (byte) (0);
                        }

                        if (!flag) {
                            System.arraycopy(imageByteArray, i, message, 5, maxDatagramPacketLength - 5);
                        } else {
                            System.arraycopy(imageByteArray, i, message, 5, length - i);
                        }
                        UdpHandler.sendBytesArray(socket, message, addressTeacher, portTeacher);
                    }
                    ++SocketServer.imageCount;
                    System.out.println("Gửi thành công ảnh thứ: " + SocketServer.imageCount + "length = " + length);
                }
            } catch (Exception e) {
                System.out.println("Loi khi gui anh:" + e.getMessage());
            }
        }
    }
}