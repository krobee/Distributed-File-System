package controller;

import java.io.IOException;
import transport.TCPServerThread;
import util.Config;

public class StartController {
	public static void main(String[] args){
		Controller ctrl = new Controller(Config.CTRL_HOST, Config.CTRL_PORT);

		TCPServerThread serverThread;
		try {
			serverThread = new TCPServerThread(ctrl);
			serverThread.start();
			System.out.println("Controller started on " + ctrl.getNickname());
			
			// wait for first minor heartbeat to come
			Thread.sleep(Config.MINOR_INTERVAL*1000);
			ctrl.requestMajorHB();
			
			// start heartbeat
			ControllerHeartBeat hb = new ControllerHeartBeat(ctrl);
			hb.start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
