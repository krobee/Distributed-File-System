package controller;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.Timestamp;
import java.util.ArrayList;

import chunkserver.ChunkServer;
import chunkserver.FileChunk;
import client.Client;
import transport.Protocol;
import transport.TCPSender;
import util.Config;
import util.Node;
import util.Util;

public class Controller extends Node{

	private ArrayList<ChunkServer> chunkList;

	public Controller(String host, int port){
		super(host, port);
		chunkList = new ArrayList<>();
	}

	public synchronized boolean register(ChunkServer chunkServer){
		boolean ret = false;
		try {
			TCPSender sender = new TCPSender(chunkServer);
			chunkList.add(chunkServer);
			ret = true;
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			System.out.println("\n" + timestamp + " ChunkServer joined: " + chunkServer.getNickname());

			sender.sendData(Protocol.REGISTER_ACK, ret);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public synchronized void processMajorHB(ChunkServer chunkServer, FileChunk fc, boolean flag){
		ArrayList<ChunkServer> fixServers = new ArrayList<>();
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		System.out.println("\n" + timestamp);
		System.out.println("Available chunk servers:");
		for(ChunkServer cs: chunkList){
			if(cs.equals(chunkServer)){
				chunkList.set(chunkList.indexOf(cs), chunkServer);
			}
			System.out.println("\t" + cs.getNickname() + ": " + cs.getFreeSpace() + " MB");
			ArrayList<FileChunk> chunks = cs.getFileChunkList();
			for(FileChunk chunk: chunks){
				// get all chunk servers that contain corrupted chunk
				if(flag == true){
					if(chunk.getChunkName().equals(fc.getChunkName()) && !cs.equals(chunkServer)){
						fixServers.add(cs);
					}
				}

				System.out.println("\t\t" + chunk.getChunkName());
			}
		}

		if(flag == true){
			System.err.println("\nData corruption on " + chunkServer.getNickname() + ": " + fc.getChunkName());
			try {
				// send fixServer back to corrupted chunkServer
				TCPSender sender = new TCPSender(chunkServer);
				sender.sendData(Protocol.CTRL_FIX, fixServers);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void processMinorHB(ChunkServer chunkServer){
		if(chunkList.contains(chunkServer)){
			for(ChunkServer cs: chunkList){
				if(cs.equals(chunkServer)){
					chunkList.set(chunkList.indexOf(cs), chunkServer);
					break;
				}
			}
		}
		else{
			register(chunkServer);
		}
	}

	public synchronized void getServer3(Client c) {

		ArrayList<ChunkServer> randomList = new ArrayList<>();

		while(randomList.size() < 3){
			int index = Util.getRandInt(0, chunkList.size());
			if(!randomList.contains(chunkList.get(index))){
				randomList.add(chunkList.get(index));
			}
		}

		try {
			TCPSender sender = new TCPSender(c);
			sender.sendData(Protocol.SERVER3_ACK, randomList);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void getChunkServer(FileChunk fc, Client c){
		for(ChunkServer cs: chunkList){
			if(cs.getFileChunkList().contains(fc)){
				try {
					TCPSender sender = new TCPSender(c);
					sender.sendData(Protocol.CTRL_RETRIEVE_ACK, cs);
					break;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


	public synchronized void sendHB(){
		ArrayList<ChunkServer> downServer = new ArrayList<>();
		try {
			for(ChunkServer cs: chunkList){
				try{
					TCPSender sender = new TCPSender(cs);
					sender.sendData(Protocol.CTRL_HB);
					
				} catch (ConnectException e){
					downServer.add(cs);
				}
			}
			
			// dealing with terminated servers
			System.out.println();
			for(ChunkServer downCS: downServer){
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
				System.out.println(timestamp + " " + downCS.getNickname() + " is down \nRedistributing files...");
				
				ArrayList<FileChunk> fileChunkList = downCS.getFileChunkList();
				for(FileChunk fc: fileChunkList){
					for(ChunkServer cs : chunkList){
						if(!cs.equals(downCS) && !cs.getFileChunkList().contains(fc)){
							TCPSender sender = new TCPSender(cs);
							sender.sendData(Protocol.REDIS, fc);
							break;
						}
					}
				}
				chunkList.remove(chunkList.indexOf(downCS));
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void requestMajorHB(){
		for(ChunkServer cs:chunkList){
			cs.sendMajorHB();
		}
	}

	public void getDelServer(FileChunk fc, Client c){
		ArrayList<ChunkServer> delServer = new ArrayList<>();
		for(ChunkServer cs: chunkList){
			for(FileChunk fchunk: cs.getFileChunkList()){
				if(fchunk.getFileName().equals(fc.getFileName())){
					delServer.add(cs);
					break;
				}
			}
		}
		try {
			TCPSender sender = new TCPSender(c);
			sender.sendData(Protocol.REQ_DEL_ACK, delServer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}








}
