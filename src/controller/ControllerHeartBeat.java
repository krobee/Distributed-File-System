package controller;

import java.sql.Timestamp;

import util.Config;

public class ControllerHeartBeat extends Thread{
	
	private Controller ctrl;
	
	public ControllerHeartBeat(Controller ctrl){
		this.ctrl = ctrl;
	}
	
	public void run(){
		while(true){
			try {
				Thread.sleep(Config.CTRL_INTERVAL*1000);
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				System.out.println("\n" + timestamp + " Controller heartbeat: " + ctrl.getNickname());
				ctrl.sendHB();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
