package com.server.liveowl;

import com.server.liveowl.Keylogger.SendFile;
import com.server.liveowl.Keylogger.ServerKeylogger;
import com.server.liveowl.savedvideo.ServerVideo;
import com.server.liveowl.socket.SocketServer;
import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiveowlApplication {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	public static void main(String[] args) {

		SpringApplication.run(LiveowlApplication.class, args);
		new Thread(new ServerVideo()).start();
		new Thread(new SocketServer()).start();
		new Thread(new ServerKeylogger()).start();
		new Thread(new SendFile(8888, "D:/PBL4/LiveOwlServer/src/main/java/com/server/liveowl/Keylogger/")).start();
	}

}
