package chunkserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import transport.TCPServerThread;

public class StartChunkServer {
	public static void main(String[] args){

		if(args.length != 1){
			System.err.println("Usage: ChunkServer [PORT]");
			System.exit(-1);
		}

		try {
			String host = InetAddress.getLocalHost().getHostName();
			int port = Integer.parseInt(args[0]);
			ChunkServer chunkServer = new ChunkServer(host, port);

			TCPServerThread serverThread = new TCPServerThread(chunkServer);
			serverThread.start();

			// start heartbeat
			ChunkServerHeartBeat hb = new ChunkServerHeartBeat(chunkServer);
			hb.start();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
