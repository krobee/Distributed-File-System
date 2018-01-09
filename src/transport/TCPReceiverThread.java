package transport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;

import chunkserver.ChunkServer;
import chunkserver.FileChunk;
import client.Client;
import controller.Controller;
import util.Node;



public class TCPReceiverThread extends Thread {

	private Socket socket;
	private ObjectInputStream ois;
	private Node node;

	public TCPReceiverThread(Node node, Socket socket) throws IOException {
		this.node = node;
		this.socket = socket;
		ois = new ObjectInputStream(this.socket.getInputStream());
	}

	@Override
	public void run() {
		try {
			int code = ois.readInt();

			switch (code) {

			case (Protocol.REGISTER):
				ChunkServer chunkServer = (ChunkServer) ois.readObject();
				((Controller) node).register(chunkServer);
				break;

			case (Protocol.REGISTER_ACK):
				boolean flag = ois.readBoolean();
				((ChunkServer) node).notify(flag);
				break;

			case (Protocol.MAJOR_HB):
				chunkServer = (ChunkServer) ois.readObject();
				FileChunk fc = (FileChunk) ois.readObject();
				flag = ois.readBoolean();
				((Controller) node).processMajorHB(chunkServer, fc, flag);
				break;
				
			case (Protocol.MINOR_HB):
				chunkServer = (ChunkServer) ois.readObject();
				((Controller) node).processMinorHB(chunkServer);
				break;
				
			case (Protocol.SERVER3):
				Client client = (Client) ois.readObject();
				((Controller) node).getServer3(client);
				break;
				
			case (Protocol.SERVER3_ACK):
				ArrayList<ChunkServer> chunkServerList = (ArrayList<ChunkServer>) ois.readObject();
				((Client) node).notifyServer3(chunkServerList);
				break;
				
			case (Protocol.STORE):
				fc = (FileChunk) ois.readObject();
				chunkServerList = (ArrayList<ChunkServer>) ois.readObject();
				client = (Client) ois.readObject();
				((ChunkServer) node).store(fc, chunkServerList, client);
				break;
				
			case (Protocol.STORE_ACK):
				((Client) node).notifyStore();
				break;
				
			case (Protocol.CTRL_RETRIEVE):
				fc = (FileChunk) ois.readObject();
				client = (Client) ois.readObject();
				((Controller) node).getChunkServer(fc, client);
				break;
				
			case (Protocol.CTRL_RETRIEVE_ACK):
				chunkServer = (ChunkServer) ois.readObject();
				((Client) node).notifyRetrieveCTRL(chunkServer);
				break;
				
			case (Protocol.RETRIEVE):
				fc = (FileChunk) ois.readObject();
				client = (Client) ois.readObject();
				((ChunkServer) node).retrieve(fc, client);
				break;
				
			case (Protocol.RETRIEVE_ACK):
				fc = (FileChunk) ois.readObject();
				((Client) node).notifyRetrieve(fc);
				break;
				
			case (Protocol.CTRL_FIX):
				chunkServerList = (ArrayList<ChunkServer>) ois.readObject();
				((ChunkServer) node).fix(chunkServerList);
				break;
				
			case (Protocol.FIX):
				fc = (FileChunk) ois.readObject();
				chunkServer = (ChunkServer) ois.readObject();
				((ChunkServer) node).getFileChunk(fc, chunkServer);
				break;
				
			case (Protocol.FIX_ACK):
				fc = (FileChunk) ois.readObject();
				((ChunkServer) node).notifyFix(fc);
				break;	
				
			case (Protocol.CTRL_HB):
				System.out.println("Received HB from Controller");
				break;
			
			case (Protocol.REDIS):
				fc = (FileChunk) ois.readObject();
				((ChunkServer) node).store(fc);
				break;
				
			case (Protocol.REQ_DEL):
				fc = (FileChunk) ois.readObject();
				client = (Client) ois.readObject();
				((Controller) node).getDelServer(fc, client);
				break;
				
			case (Protocol.REQ_DEL_ACK):
				chunkServerList = (ArrayList<ChunkServer>) ois.readObject();
				((Client) node).notifyDelServer(chunkServerList);
				break;
				
			case (Protocol.DEL):
				fc = (FileChunk) ois.readObject();
				((ChunkServer) node).delete(fc);
				break;	
			
			default:
				System.err.println("Unknown code");

			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
