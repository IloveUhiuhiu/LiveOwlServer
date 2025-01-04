package com.server.liveowl.socket;

public class ProscessTesting extends Thread {
    public static volatile int countImageKiemThu = 0;
    public static volatile int countImageKiemThuLast = 0;
    public static volatile int countStudent = 0;

    public void run() {
        try {
            while (true) {
                if (countImageKiemThu > 0) {
                    Thread.sleep(1000);
                    double ans = (countImageKiemThu - countImageKiemThuLast) * 1.0 / (countStudent);
                    System.out.println(countImageKiemThu + ": " + countImageKiemThuLast + ": " + countStudent + ": " + ans);
                    countImageKiemThuLast = countImageKiemThu;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Luồng kiểm thử bị gián đoạn.");
        }
    }
}
