package com.server.liveowl;

import com.server.liveowl.socket.SocketServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiveowlApplication {

	public static void main(String[] args) {

		SpringApplication.run(LiveowlApplication.class, args);
		new Thread(new SocketServer()).start();
	}

}
