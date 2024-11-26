package com.server.liveowl.savedvideo;

import com.server.liveowl.dto.ImageDTO;
import com.server.liveowl.socket.SocketServer;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoWriter;

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class SavedVideo implements Runnable {
    public static String outputFilePath = "E:\\Downloads\\LiveOwlServer\\src\\main\\java\\com\\server\\liveowl\\uploads\\video\\";
    public static int frameWidth = 768;
    public static int frameHeight = 432;
    public static int fps = 15;
    public Map<String, VideoWriter> videoWriters;
    public SavedVideo(Map<String, VideoWriter> videoWriters) {
        this.videoWriters = videoWriters;
    }
    public void run() {
        try {
            while (true) {

                if (!SocketServer.savedImages.isEmpty()) {
                    ImageDTO imageDto = SocketServer.savedImages.poll();
                    if (imageDto.getImage() != null) {
                        System.out.println(imageDto.getClientId() + ", " + imageDto.getImage().length + " lưu video");
                        Mat frame = byteArrayToMat(imageDto.getImage());
                        if (!frame.empty()) {
                            if (videoWriters.get(imageDto.getClientId()) != null) {
                                videoWriters.get(imageDto.getClientId()).write(frame);
                                System.out.println("Đã ghi khung hình vào video.");
                            }
                        } else {
                            System.out.println("Khung hình trống, không ghi vào video.");
                        }
                    } else {
                        System.out.println(imageDto.getClientId() + ", image null");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            videoWriters.forEach((integer, videoWriter) -> {
                videoWriter.release();
                System.out.println("Đã giải phóng VideoWriter cho clientId: " + integer);
            });
        }
    }

    private Mat byteArrayToMat(byte[] bytes) {
        Mat mat = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.IMREAD_UNCHANGED);
        if (mat.empty()) {
            System.out.println("Không thể chuyển đổi byte array thành Mat");
        } else {
            System.out.println("Kích thước khung hình: " + mat.size());
        }
        return mat;
    }
}