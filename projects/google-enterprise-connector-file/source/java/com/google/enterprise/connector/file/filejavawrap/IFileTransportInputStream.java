package com.google.enterprise.connector.file.filejavawrap;

import com.filenet.wcm.api.TransportInputStream;
import com.google.enterprise.connector.file.filewrap.ITransportInputStream;

public class IFileTransportInputStream implements ITransportInputStream {

	private TransportInputStream content;
	
	public IFileTransportInputStream(TransportInputStream content) {
		this.content = content;
	}

}
