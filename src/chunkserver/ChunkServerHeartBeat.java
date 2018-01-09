package chunkserver;

import java.sql.Timestamp;

import util.Config;

public class ChunkServerHeartBeat extends Thread{
	
	private ChunkServer cs;
	private int count;
	
	public ChunkServerHeartBeat(ChunkServer cs){
		this.cs = cs;
		count = 0;
	}
	
	public void run(){
		while(true){
			try {
				Thread.sleep(Config.MINOR_INTERVAL*1000);
				count++;
				
				// major heartbeat
				if(count % (Config.MAJOR_INTERVAL/Config.MINOR_INTERVAL) == 0){
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					System.out.println(timestamp + " Major heartbeat: " + cs.getNickname());
					cs.sendMajorHB();
				}
				
				// minor heartbeat
				else{
					Timestamp timestamp = new Timestamp(System.currentTimeMillis());
					System.out.println(timestamp + " Minor heartbeat: " + cs.getNickname());
					cs.sendMinorHB();
					
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
