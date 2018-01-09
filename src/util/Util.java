package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

public class Util {
	public static int getRandInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max);
	}
	
	public static String SHA1(String file) throws NoSuchAlgorithmException, IOException
	{
		MessageDigest sha1 = MessageDigest.getInstance("SHA1");
		FileInputStream fis = new FileInputStream(file);

		byte[] data = new byte[1024];
		int read = 0; 
		while ((read = fis.read(data)) != -1) {
			sha1.update(data, 0, read);
		};
		byte[] hashBytes = sha1.digest();

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < hashBytes.length; i++) {
			sb.append(Integer.toString((hashBytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		String fileHash = sb.toString();
		return fileHash;
	}
}
