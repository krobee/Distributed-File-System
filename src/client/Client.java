package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import chunkserver.ChunkServer;
import chunkserver.FileChunk;
import transport.Protocol;
import transport.TCPSender;
import util.Config;
import util.Node;

public class Client extends Node{

	private ArrayList<ChunkServer> chunkServerList;
	private ChunkServer chunkServer;
	private FileChunk fc;

	public Client(String host, int port){
		super(host, port);
		chunkServerList = new ArrayList<>();
	}

	public synchronized void store(String fileName){ 

		try {

			// split file in chunks
			ArrayList<FileChunk> fileChunkList = splitFile(fileName);

			for(FileChunk fc: fileChunkList){

				// get 3 random chunk servers from controller
				TCPSender sender = new TCPSender(Config.CTRL_HOST, Config.CTRL_PORT);
				sender.sendData(Protocol.SERVER3, this);
				wait();

				// send to chunk server
				sender = new TCPSender(chunkServerList.get(0));
				sender.sendData(Protocol.STORE, fc, chunkServerList, this);
				wait();
				System.out.println(fc.getChunkName() + " has been stored");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}


	}

	public synchronized void notifyServer3(ArrayList<ChunkServer> chunkServerList){
		this.chunkServerList = chunkServerList;
		notify();
	}

	public synchronized void notifyStore(){
		notify();
	}

	public synchronized void retrieve(String fileName){
		try{
			ArrayList<FileChunk> fileChunkList = splitFile(fileName);
			ArrayList<FileChunk> chunks = new ArrayList<>();
			for(FileChunk fc: fileChunkList){
				// get chunk server from controller
				TCPSender sender = new TCPSender(Config.CTRL_HOST, Config.CTRL_PORT);
				sender.sendData(Protocol.CTRL_RETRIEVE, fc, this);
				wait();

				// get file chunk from chunk server
				sender = new TCPSender(chunkServer);
				sender.sendData(Protocol.RETRIEVE, fc, this);
				wait();
				System.out.println(fc.getChunkName() + " has been retrieved");
				chunks.add(fc);


			}

			chunks.sort(Comparator.comparing(FileChunk::getSeq));

			mergeFile(chunks, Config.FILE_DIR + "/" + fileName);
			System.out.println(fileName + " has been stored in /tmp");
		}catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}



	}



	public synchronized void notifyRetrieveCTRL(ChunkServer chunkServer){
		this.chunkServer = chunkServer;
		notify();
	}

	public synchronized void notifyRetrieve(FileChunk fc){
		this.fc = fc;
		notify();
	}

	public synchronized void update(String fileName){
		try {
			delete(fileName);
			// split file in chunks
			ArrayList<FileChunk> fileChunkList = splitFile(fileName);

			for(FileChunk fc: fileChunkList){

				// get 3 random chunk servers from controller
				TCPSender sender = new TCPSender(Config.CTRL_HOST, Config.CTRL_PORT);
				sender.sendData(Protocol.SERVER3, this);
				wait();

				// send to chunk server
				sender = new TCPSender(chunkServerList.get(0));
				sender.sendData(Protocol.STORE, fc, chunkServerList, this);
				wait();
				System.out.println(fc.getChunkName() + " has been updated");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void delete(String fileName){
		FileChunk fc = new FileChunk(fileName);
		try {
			TCPSender sender = new TCPSender(Config.CTRL_HOST, Config.CTRL_PORT);
			sender.sendData(Protocol.REQ_DEL, fc, this);
			wait();
			for(ChunkServer cs: chunkServerList){
				sender = new TCPSender(cs);
				sender.sendData(Protocol.DEL, fc);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public synchronized void notifyDelServer(ArrayList<ChunkServer> chunkServerList){
		this.chunkServerList = chunkServerList;
		notify();
	}

	public static ArrayList<FileChunk> splitFile(String fileName){

		ArrayList<FileChunk> chunks = new ArrayList<>();
		File inputFile = new File(fileName);
		FileInputStream fis;

		long fileSize = inputFile.length();
		int readLength = Config.CHUNK_SIZE;
		int nChunks = 0, read = 0;
		byte[] byteChunkPart;

		try {
			fis = new FileInputStream(inputFile);

			while(fileSize > 0){

				if(fileSize <= Config.CHUNK_SIZE){
					readLength = (int)fileSize;
				}
				byteChunkPart = new byte[readLength];
				read = fis.read(byteChunkPart, 0, readLength);
				fileSize -= read;

				FileChunk chunk = new FileChunk(inputFile.getName(), nChunks, byteChunkPart);
				chunks.add(chunk);

				nChunks++;

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return chunks;
	}

	public static void mergeFile(List<FileChunk> chunks, String outputFileName){
		File file = new File(outputFileName);
		FileOutputStream fos;
		FileInputStream fis;
		byte[] fileBytes;
		int bytesRead = 0;
		try {
			fos = new FileOutputStream(file);
			for(FileChunk chunk: chunks){
				fos.write(chunk.getContent());
			}
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
