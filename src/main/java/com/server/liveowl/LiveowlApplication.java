package com.server.liveowl;

import com.server.liveowl.keylogger.ProcessSendFile;
import com.server.liveowl.savevideo.VideoServer;
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
		new Thread(new VideoServer()).start();
		new Thread(new SocketServer()).start();
		new Thread(new ProcessSendFile(8888, "D:/PBL4/LiveOwlServer/src/main/java/com/server/liveowl/Keylogger/")).start();

	}

}
