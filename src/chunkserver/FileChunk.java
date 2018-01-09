package chunkserver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Arrays;

import util.Config;

public class FileChunk implements Serializable{

	private String chunkName;
	private byte[] content;
	private int seq;
	private int version;
	private Timestamp timestamp;
	private String fileName;
	private String checksum;

	public FileChunk(String fileName, int seq, byte[] content){
		chunkName = fileName + "_chunk" + seq;
		this.seq = seq;
		this.content = content;
		this.fileName = fileName;
		version = 0;
		timestamp = new Timestamp(System.currentTimeMillis());
	}
	
	public FileChunk(String fileName){
		this.fileName = fileName;
	}

	public void writeChunk(){
		File file = new File(Config.FILE_DIR + "/" + chunkName);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			fos.write(content);
			fos.flush();
			fos.close();
			checksum = util.Util.SHA1(file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		

	}

	public void writeMeta(){
		File file = new File(Config.FILE_DIR + "/" + chunkName + "_meta");

		try {
			FileOutputStream fos = new FileOutputStream(file);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			bw.write(version + "\n");
			bw.write(seq + "\n");
			bw.write(fileName + "\n");
			bw.write(timestamp + "\n");
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public String getFileName(){
		return fileName;
	}
	
	public int getVersion(){
		return version;
	}
	
	public void setVersion(int version){
		this.version = version;
	}
	
	public void setTimestamp(Timestamp timestamp){
		this.timestamp = timestamp;
	}
	
	public String getChecksum(){
		return checksum;
	}
	
	public byte[] getContent(){
		return content;
	}

	public String getChunkName(){
		return chunkName;
	}

	public int getSeq(){
		return seq;
	}

	public String toString(){
		String ret = chunkName + "\n" + new String(content) + "\n";
		return ret;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((chunkName == null) ? 0 : chunkName.hashCode());
		result = prime * result + Arrays.hashCode(content);
		result = prime * result + seq;
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
		FileChunk other = (FileChunk) obj;
		if (chunkName == null) {
			if (other.chunkName != null)
				return false;
		} else if (!chunkName.equals(other.chunkName))
			return false;
		if (!Arrays.equals(content, other.content))
			return false;
		if (seq != other.seq)
			return false;
		return true;
	}
}
