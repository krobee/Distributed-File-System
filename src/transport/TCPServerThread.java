package transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import util.Node;

public class TCPServerThread extends Thread {

	private Node node;
	private ServerSocket serverSocket;
	private int port;

	public TCPServerThread(Node node) throws IOException {
		this.node = node;
		port = node.getPort();
		serverSocket = new ServerSocket(port);
	}


	@Override
	public void run() {
		while (true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				TCPReceiverThread receiverThread = new TCPReceiverThread(node, socket);
				receiverThread.start();
			} catch (IOException ioe) {
				ioe.printStackTrace();
				continue;
			}

		}
	}

}
