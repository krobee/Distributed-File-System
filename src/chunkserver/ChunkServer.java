package chunkserver;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

import client.Client;
import controller.Controller;
import transport.Protocol;
import transport.TCPSender;
import transport.TCPServerThread;
import util.Config;
import util.Node;

public class ChunkServer extends Node{

	private ArrayList<FileChunk> fileChunkList;
	private long freeSpace;
	private String host;
	private FileChunk fchunk;

	public ChunkServer(String host, int port){
		super(host, port);
		fileChunkList = new ArrayList<>();
		updateDiskSpace();
		this.host = host;
	}

	public void notify(boolean flag){
		System.out.println(flag);
	}

	public void updateDiskSpace(){
		freeSpace = new File(Config.FILE_DIR).getFreeSpace()/1000000;
	}

	public void sendMajorHB(){
		boolean corruptFlag = false;
		fchunk = null;
		try{
			for(FileChunk fc: fileChunkList){
				if(!fc.getChecksum().equals(util.Util.SHA1(Config.FILE_DIR + "/" + fc.getChunkName()))){
					corruptFlag = true;
					fchunk = fc;
					break;
				}
			}
			
			TCPSender sender = new TCPSender(Config.CTRL_HOST, Config.CTRL_PORT);
			sender.sendData(Protocol.MAJOR_HB, this, fchunk, corruptFlag);
			
		}catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public void sendMinorHB(){

		TCPSender sender;
		try {
			sender = new TCPSender(Config.CTRL_HOST, Config.CTRL_PORT);
			sender.sendData(Protocol.MINOR_HB, this);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public ArrayList<FileChunk> getFileChunkList(){
		return fileChunkList;
	}

	public long getFreeSpace(){
		return freeSpace;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChunkServer other = (ChunkServer) obj;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		return true;
	}

	public void store(FileChunk fc, ArrayList<ChunkServer> chunkServerList, Client client){
		fileChunkList.add(fc);
		fc.writeChunk();
		fc.writeMeta();
		updateDiskSpace();

		System.out.println("FileChunk: " + fc.getChunkName() + " has been stored on " + this.getNickname());

		chunkServerList.remove(0);

		try {
			if(chunkServerList.size() != 0){
				TCPSender sender = new TCPSender(chunkServerList.get(0));
				sender.sendData(Protocol.STORE, fc, chunkServerList, client);
			}
			else{
				TCPSender sender = new TCPSender(client);
				sender.sendData(Protocol.STORE_ACK, this);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void retrieve(FileChunk fc, Client client){
		for(FileChunk fchunk: fileChunkList){
			if(fchunk.equals(fc)){
				try {
					TCPSender sender = new TCPSender(client);
					sender.sendData(Protocol.RETRIEVE_ACK, fchunk);
					break;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void fix(ArrayList<ChunkServer> chunkServerList){
		System.out.println("Valid replications:");
		for(ChunkServer cs: chunkServerList){
			System.out.println( "\t" + cs.getNickname());
		}
		try {
			TCPSender sender = new TCPSender(chunkServerList.get(0));
			sender.sendData(Protocol.FIX, fchunk, this);
			wait();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		for(FileChunk fc: fileChunkList){
			if(fc.getChunkName().equals(fchunk.getChunkName())){
				fileChunkList.set(fileChunkList.indexOf(fc), fchunk);
				fchunk.setVersion(fchunk.getVersion() + 1);
				fchunk.setTimestamp(new Timestamp(System.currentTimeMillis()));
				fchunk.writeChunk();
				fchunk.writeMeta();
				updateDiskSpace();
				System.out.println("Chunk fixed: " + fchunk.getChunkName());
				break;
			}
		}
	}
	
	public void getFileChunk(FileChunk fc, ChunkServer cs){
		for(FileChunk fchunk: fileChunkList){
			if(fchunk.getChunkName().equals(fc.getChunkName())){
				try {
					TCPSender sender = new TCPSender(cs);
					sender.sendData(Protocol.FIX_ACK, fchunk);
					System.out.println("Sending valid chunk to " + cs.getNickname());
					break;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public synchronized void notifyFix(FileChunk fc){
		this.fchunk = fc;
		notify();
	}


	public void store(FileChunk fc){
		fileChunkList.add(fc);
		fc.writeChunk();
		fc.writeMeta();
		updateDiskSpace();

		System.out.println("FileChunk: " + fc.getChunkName() + " has been stored on " + this.getNickname());
	}


	public void delete(FileChunk fc){
		
		Iterator<FileChunk> iter = fileChunkList.iterator();

		while (iter.hasNext()) {
		    FileChunk fchunk = iter.next();
		    if(fchunk.getFileName().equals(fc.getFileName())){
				iter.remove();
				File file = new File(Config.FILE_DIR + "/" + fchunk.getChunkName());
				file.delete();
			}
		}
		updateDiskSpace();
	}















}
