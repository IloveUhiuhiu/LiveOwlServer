package com.server.liveowl.socket;
import com.server.liveowl.dto.ImageDTO;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class ProcessSaveImage implements Runnable {
    public ProcessGetImage processGetData;
    public static int frameWidth = 768;
    public static int frameHeight = 432;
    public static int fps = 15;
    public ProcessSaveImage(ProcessGetImage processGetData) {
        this.processGetData = processGetData;
    }
    public void run() {
        try {
            while (processGetData.isRunning()) {
                if (!processGetData.queueSavedImages.isEmpty()) {
                    ImageDTO imageDto = processGetData.queueSavedImages.poll();
                    if (imageDto.getImage() != null) {
                        System.out.println(imageDto.getClientId() + ", " + imageDto.getImage().length + " lưu video");
                        Mat frame = byteArrayToMat(imageDto.getImage());
                        if (!frame.empty()) {
                            if (processGetData.videoWriters.get(imageDto.getClientId()) != null) {
                                processGetData.videoWriters.get(imageDto.getClientId()).write(frame);
                                System.out.println("Đã ghi khung hình vào video.");
                            }
                        } else {
                            System.err.println("Khung hình trống, không ghi vào video.");
                        }
                    } else {
                        System.err.println(imageDto.getClientId() + ", image null");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in save video: " + e.getMessage());
        } finally {
            if (processGetData != null && processGetData.videoWriters != null) {
                processGetData.videoWriters.forEach((integer, videoWriter) -> {
                    videoWriter.release();
                    System.out.println("Đã giải phóng VideoWriter cho clientId: " + integer);
                });
            }
            System.out.println("Close thread ProcessSavedData");
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