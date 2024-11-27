package com.server.liveowl.savedvideo;
import com.server.liveowl.util.UdpHandler;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import static com.server.liveowl.ServerConfig.*;


public class SendVideo implements Runnable {
        DatagramSocket socket;
        public InetAddress address;
        public int port;
        String code;
        String clientId;
        int imageId = 0;
        SendVideo(DatagramPacket packet, String code, String clientId,int countConnected) throws SocketException {
            socket = new DatagramSocket(serverVideoPort + countConnected);
            address = packet.getAddress();
            port = packet.getPort();
            this.code = code;
            this.clientId = clientId;
        }
        @Override
        public void run() {
            try {
                VideoCapture capture = new VideoCapture(videoPath + "\\_" + code + "\\video_" + clientId + ".mp4");
                Mat frame = new Mat();

                if (!capture.isOpened()) {
                    System.out.println("Không thể mở video: " + videoPath);
                    return;
                }

                while (true) {
                    if (capture.read(frame)) {
                        byte[] data = convertMatToBytes(frame);
                        sendPacket(data);
                        System.out.println("Đã gửi khung hình: " + frame.size());
                    } else {
                        System.out.println("Đã hết video.");
                        break;
                    }
                }
                sendEnd();
                capture.release();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                socket.close();
            }
        }
        private void sendPacket(byte[] imageByteArray) {
            try {
                int sequenceNumber = 0;
                boolean flag;
                int length = imageByteArray.length;
                byte[] lengthBytes = new byte[maxDatagramPacketLength];
                lengthBytes[0] = (byte) 0;
                lengthBytes[1] = (byte) (imageId);
                lengthBytes[2] = (byte) (length >> 16);
                lengthBytes[3] = (byte) (length >> 8);
                lengthBytes[4] = (byte) (length);
                UdpHandler.sendBytesArray(socket, lengthBytes, address, port);
                for (int i = 0; i < length; i = i + maxDatagramPacketLength - 4) {
                    sequenceNumber += 1;
                    byte[] message = new byte[maxDatagramPacketLength];
                    message[0] = (byte) (1);
                    message[1] = (byte) (imageId);
                    message[2] = (byte) (sequenceNumber);
                    if ((i + maxDatagramPacketLength - 4) >= imageByteArray.length) {
                        flag = true;
                        message[3] = (byte) (1);
                    } else {
                        flag = false;
                        message[3] = (byte) (0);
                    }

                    if (!flag) {
                        System.arraycopy(imageByteArray, i, message, 4, maxDatagramPacketLength - 4);
                    } else {
                        System.arraycopy(imageByteArray, i, message, 4, length - i);
                    }
                    UdpHandler.sendBytesArray(socket, message, address, port);
                }
            } catch (Exception e) {

            }
            imageId= (imageId + 1) % 5;
        }
        private void sendEnd() {
            try {
                byte[] bytesArray = new byte[maxDatagramPacketLength];
                bytesArray[0] = (byte) 2;
                UdpHandler.sendBytesArray(socket, bytesArray, address, port);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Gửi kết thúc thành công");
        }
        private static byte[] convertMatToBytes(Mat mat) {
            MatOfInt params = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 90);
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", mat, matOfByte, params);
            byte[] imageBytes = matOfByte.toArray();
            System.out.println("Gửi ảnh có độ dài " + imageBytes.length);
            return imageBytes;
        }
}