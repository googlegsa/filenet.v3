package com.google.enterprise.connector.filenet.utility.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.security.auth.Subject;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.property.PropertyFilter;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;

public class FileNetSQLSupport {

	public static void runQuery(String userName, String password, String objectStore,
			String contentEngineURL, String waspLocation, String domainName,
			String query, String logFilePath, int pageSize, int filterLevel,
			int continuable, boolean isFullLog){
		try{
			System.setProperty("wasp.location", waspLocation);
			Connection conn = null;
			Domain domain = null;
			conn = Factory.Connection.getConnection(contentEngineURL);
			domain = Factory.Domain.getInstance(conn, null);

			UserContext uc = UserContext.get();
			Subject s = UserContext.createSubject(conn, userName, password, "FileNetP8");
			uc.pushSubject(s);
			com.filenet.api.core.ObjectStore os = Factory.ObjectStore.fetchInstance(domain, objectStore, null);
			SearchScope ss = new SearchScope(os);

			long beforeTime = System.currentTimeMillis();
			LinkedList ll = execute(query,ss, pageSize, filterLevel, continuable);
			long afterTime = System.currentTimeMillis();
			double timeTaken = (double)(afterTime - beforeTime)/1000;

			StringBuffer result = new StringBuffer();
			result.append("Total number of documents discovered: ");
			result.append(ll.size());
			result.append("\nTotal time taken to execute the query: "+timeTaken+ " Seconds\n\n");
			System.out.println(result.toString());

			FileOutputStream fos = null;
			try{
				File file = new File(logFilePath);
				if(!file.exists()){
					file.createNewFile();
				}

				fos = new FileOutputStream(file);

				fos.write(result.toString().getBytes());
				if(isFullLog){
					for(int i=0;i<ll.size();i++){
						fos.write((ll.get(i).toString()+"\n").getBytes());
					}
				}
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				if(fos != null){
					fos.close();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{

		}
	}

	private static LinkedList execute(String query, SearchScope search, int pageSize, int filterLevel, int continuble) {
		LinkedList objectList = new LinkedList();
		IndependentObjectSet myObjects = null;

		SearchSQL sqlObject = new SearchSQL();
		sqlObject.setQueryString(query);

		Integer myPageSize = new Integer(pageSize);

		PropertyFilter myFilter = new PropertyFilter();
		//		int myFilterLevel = 1;
		myFilter.setMaxRecursion(filterLevel);

		Boolean continuable = null;
		if(continuble == 1)
			continuable = new Boolean(true);
		else if(continuble == 0)
			continuable = new Boolean(false);

		try {
			myObjects = search.fetchObjects(sqlObject, myPageSize, myFilter,
					continuable);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(myObjects != null){
			Iterator it = myObjects.iterator();
			while (it.hasNext()) {
				objectList.add((IndependentObject) it.next());
			}
		}
		return objectList;
	}
}
