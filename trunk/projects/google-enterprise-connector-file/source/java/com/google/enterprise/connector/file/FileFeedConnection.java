package com.google.enterprise.connector.file;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.enterprise.connector.pusher.FeedConnection;

public class FileFeedConnection implements FeedConnection {

	private int number;
	private FileOutputStream fileOutputStream;
	
	public FileFeedConnection() {
		number = 0;
	}
	
	public String sendData(InputStream data) throws IOException {
		number++;
		try {
			fileOutputStream = new FileOutputStream("testdata/crawlData/out" + number + ".txt");
			byte[] buffer = new byte[1000];
			int readCount = 0;
			while ((readCount = data.read(buffer)) != -1) {
				if (readCount < 1000) {
					fileOutputStream.write(buffer, 0, readCount);
				} else {
					fileOutputStream.write(buffer);
				}
			}
			fileOutputStream.close();
			data.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "FileFeedConnection";
	}

}
