package com.server.liveowl;

public class ServerConfig {
    public static final String BASE_PATH = System.getProperty("user.dir");
    public static final int SERVER_PORT = 9000;
    public static final int SERVER_VIDEO_PORT = 1604;
    public static final String SERVER_HOST = "10.10.3.132";
    public static final int MAX_DATAGRAM_PACKET_LENGTH = 1500;
    public static final int NUM_OF_THREAD = 10;
    public static final int SEND_KEYBOARD_PORT = 8888;
    public static final String VIDEO_PATH = BASE_PATH + "\\src\\main\\java\\com\\server\\liveowl\\uploads\\video\\";
    public static final String KEYBOARD_PATH = BASE_PATH + "\\src\\main\\java\\com\\server\\liveowl\\uploads\\keyboard\\";
}
