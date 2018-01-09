package util;

import java.io.Serializable;

public class Node implements Serializable {
	
	private String host;
	private int port;
	
	public Node(String host, int port){
		this.host = host;
		this.port = port;
	}
	
	public String toString(){
		return "[" + host + ":" + port + "]";
	}
	
	public String getHost(){
		return host;
	}
	
	public int getPort(){
		return port;
	}
	
	public String getNickname(){
		return host + "-" + String.valueOf(port);
	}
	

}
