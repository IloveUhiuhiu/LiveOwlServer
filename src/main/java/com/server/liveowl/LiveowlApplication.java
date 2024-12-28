package com.server.liveowl;


import com.server.liveowl.keylogger.FileServer;
import com.server.liveowl.savevideo.VideoServer;
import com.server.liveowl.socket.SocketServer;
import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import static com.server.liveowl.ServerConfig.sendkeyboardPath;
import static com.server.liveowl.ServerConfig.SEND_KEYBOARD_PORT;


@SpringBootApplication
public class LiveowlApplication {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	public static void main(String[] args) {

		SpringApplication.run(LiveowlApplication.class, args);
		new Thread(new VideoServer()).start();
		new Thread(new SocketServer()).start();
		new Thread(new FileServer()).start();

	}

}
