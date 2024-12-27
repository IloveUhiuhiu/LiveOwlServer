package com.server.liveowl.socket;

import com.server.liveowl.util.UdpHandler;
import java.net.DatagramSocket;
import java.net.SocketException;
import static com.server.liveowl.ServerConfig.*;

class ProcessSendImage implements Runnable {
    DatagramSocket socket;
    public ProcessGetImage processGetData;
    ProcessSendImage(ProcessGetImage processGetData) throws SocketException {
        socket = new DatagramSocket(1000 + processGetData.processId);
        this.processGetData = processGetData;
    }
    @Override
    public void run() {
        try {
            while(processGetData.isRunning()) {

                String packetId = processGetData.queueSendIds.poll();
                if (packetId != null) {
                    byte[] imageByteArray = processGetData.imageBuffer.get(packetId);
                    int pos = packetId.lastIndexOf(":");
                    int imageId = Integer.parseInt(packetId.substring(0, pos));
                    String clientId = packetId.substring(pos + 1);
                    int sequenceNumber = 0;
                    boolean flag;
                    int length = imageByteArray.length;
                    byte[] lengthBytes = new byte[maxDatagramPacketLength];
                    lengthBytes[0] = (byte) 0;
                    System.arraycopy(clientId.getBytes(), 0, lengthBytes, 1, 8);
                    lengthBytes[9] = (byte) (imageId);
                    lengthBytes[10] = (byte) (length >> 16);
                    lengthBytes[11] = (byte) (length >> 8);
                    lengthBytes[12] = (byte) (length);
                    UdpHandler.sendBytesArray(socket, lengthBytes, processGetData.addressTeacher, processGetData.portTeacher);
                    for (int i = 0; i < length; i = i + maxDatagramPacketLength - 12) {
                        sequenceNumber += 1;
                        byte[] message = new byte[maxDatagramPacketLength];
                        message[0] = (byte) (1);
                        System.arraycopy(clientId.getBytes(), 0, message, 1, 8);

                        message[9] = (byte) (imageId);
                        message[10] = (byte) (sequenceNumber);
                        if ((i + maxDatagramPacketLength - 12) >= imageByteArray.length) {
                            flag = true;
                            message[11] = (byte) (1);
                        } else {
                            flag = false;
                            message[11] = (byte) (0);
                        }

                        if (!flag) {
                            System.arraycopy(imageByteArray, i, message, 12, maxDatagramPacketLength - 12);
                        } else {
                            System.arraycopy(imageByteArray, i, message, 12, length - i);
                        }
                        UdpHandler.sendBytesArray(socket, message, processGetData.addressTeacher, processGetData.portTeacher);
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("Error in ProcessSendData:" + e.getMessage());
        } finally {
            if (socket != null) {
                socket.close();
                System.out.println("Close thread ProcessSendData");
            }
        }
    }
}