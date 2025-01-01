package com.server.liveowl;

import com.server.liveowl.keylogger.FileServer;
import com.server.liveowl.savevideo.VideoServer;
import com.server.liveowl.socket.SocketServer;
import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class LiveowlApplication {
	static {
//		System.out.println(System.getProperty("java.library.path"));
//		System.loadLibrary("opencv_java4100");
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	public static void main(String[] args) {

		SpringApplication.run(LiveowlApplication.class, args);
		ExecutorService executor = Executors.newFixedThreadPool(3);
		executor.submit(new VideoServer());
		executor.submit(new SocketServer());
		executor.submit(new FileServer());
		executor.shutdown();

	}

}
