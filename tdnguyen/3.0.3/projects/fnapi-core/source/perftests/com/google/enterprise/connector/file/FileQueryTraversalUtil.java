package com.google.enterprise.connector.file;

import com.google.enterprise.connector.filenet3.FileTraversalManager;
import com.google.enterprise.connector.pusher.DocPusher;
import com.google.enterprise.connector.pusher.FeedException;
import com.google.enterprise.connector.pusher.GsaFeedConnection;
import com.google.enterprise.connector.pusher.PushException;
import com.google.enterprise.connector.spi.Document;
import com.google.enterprise.connector.spi.DocumentList;
import com.google.enterprise.connector.spi.RepositoryException;
import com.google.enterprise.connector.spi.SpiConstants;
import com.google.enterprise.connector.spi.TraversalManager;

import java.net.MalformedURLException;

//import com.google.enterprise.connector.pusher.DocPusher;
//import com.google.enterprise.connector.pusher.GsaFeedConnection;
//import com.google.enterprise.connector.pusher.PushException;
//
//import com.google.enterprise.connector.spi.Document;
//import com.google.enterprise.connector.spi.DocumentList;
//import com.google.enterprise.connector.spi.TraversalManager;
//import com.google.enterprise.connector.spi.RepositoryException;
//import com.google.enterprise.connector.spi.SpiConstants;

public class FileQueryTraversalUtil {

	public static void runTraversal(TraversalManager queryTraversalManager,
			int batchHint) throws RepositoryException, PushException {

		FileTraversalManager fileQTM = (FileTraversalManager) queryTraversalManager;
		fileQTM.setBatchHint(batchHint);

		DocumentList resultSet = fileQTM.startTraversal();
		// int nb=resultSet.size();
		// System.out.println("nb vaut "+nb);
		// The real connector manager will not always start from the beginning.
		// It will start from the beginning if it receives an explicit admin
		// command to do so, or if it thinks that it has never run this
		// connector
		// before. It decides whether it has run a connector before by storing
		// every checkpoint it receives from
		// the connector. If it can find no stored checkpoint, it assumes that
		// it has never run this connector before and starts from the beginning,
		// as here.
		if (resultSet == null) {
			// in this test program, we will stop in this situation. The real
			// connector manager might wait for a while, then try again
			return;
		}

		DocPusher push = null;
		try {
			push = new DocPusher(new GsaFeedConnection("8.6.49.36", null,
					19900, batchHint), "TestConnector", null, null);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// DocPusher push = new DocPusher(new FileFeedConnection());
		while (true) {
			int counter = 0;

			// FileDocument pm = null;
			// for (Iterator iter = resultSet.iterator(); iter.hasNext();) {
			// pm = (Document) iter.next();
			// counter++;

			Document doc;
			while ((doc = resultSet.nextDocument()) != null) {

				counter++;
				if (counter == batchHint) {
					System.out.println("counter == batchhint !!!!");
					// this test program only takes batchHint results from each
					// resultSet. The real connector manager may take fewer -
					// for
					// example, if it receives a shutdown request

					break;
				}
				System.out.println("counter " + counter);
				System.out.println(doc.findProperty(SpiConstants.PROPNAME_DISPLAYURL).nextValue());
				try {
					push.take(doc, null);
				} catch (FeedException e) {
					System.out.println("FeedException occured");
				}

			}

			if (counter == 0) {
				// this test program stops if it receives zero results in a
				// resultSet.
				// the real connector Manager might wait a while, then try again
				break;
			}

			String checkPointString = resultSet.checkpoint();
			resultSet = fileQTM.resumeTraversal(checkPointString);

			// the real connector manager will call checkpoint (as here) as soon
			// as possible after processing the last property map it wants to
			// process.
			// It would then store the checkpoint string it received in
			// persistent
			// store.
			// Unlike here, it might not then immediately turn around and call
			// resumeTraversal. For example, it may have received a shutdown
			// command,
			// so it won't call resumeTraversal again until it starts up again.
			// Or, it may be running this connector on a schedule and there may
			// be a
			// scheduled pause.
		}
	}

}
