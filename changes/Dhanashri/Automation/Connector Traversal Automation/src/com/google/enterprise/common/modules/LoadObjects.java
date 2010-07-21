/**
 * 
 */
package com.google.enterprise.common.modules;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 *This class is designed to provide functions which will return  
 *test objects(screen objects) to test scripts from the object store.
 * 
 *This reduces the risk of changing test scripts when objects on the 
 *screen changes
 * 
 * @author vishwas_londhe
 */
public class LoadObjects
{
	static Properties p = new Properties();
	String OSLoginPage = "objectstore\\GSALoginPage.properties";
	String OSHomePage = "objectstore\\GSAHomePage.properties";
	String OSSelectCMPage = "objectstore\\GSASelectCMPage.properties";
	String OSConnectorConfigPage = "objectstore\\FileNetConfigPage.properties";
	String OSFeedPage ="objectstore\\GSAFeedPage.properties";
	String OSGSAConfig="objectstore\\GSAConfigPage.properties";
	String temp = "";
	
	private void loadproperties(String objectstore)
	{
		try
		{
			p.load(new FileInputStream(new File(objectstore)));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("*****Unable to load Properties file*****");
		}
	}
	
	public String gsausername()
	{
		loadproperties(OSLoginPage);
		temp = p.getProperty("txtusername");
		return temp;
	}
	
	public String gsapassword()
	{
		loadproperties(OSLoginPage);
		temp = p.getProperty("txtpassword");
		return temp;
	}
	
	public String gsaloginbtn()
	{
		loadproperties(OSLoginPage);
		temp = p.getProperty("btnlogin");
		return temp;
	}
	
	public String gsalogoutbtn()
	{
		loadproperties(OSHomePage);
		temp = p.getProperty("btnlogout");
		return temp;
	}
	
	public String gsahome()
	{
		loadproperties(OSHomePage);
		temp = p.getProperty("lnkgsahome");
		return temp;
	}
	
	public String connectoradministration()
	{
		loadproperties(OSHomePage);
		temp = p.getProperty("lnkconnectoradministration");
		return temp;
	}
	
	public String connectors()
	{
		loadproperties(OSHomePage);
		temp = p.getProperty("lnkconnectors");
		return temp;
	}
	
	public String connectormanagers()
	{
		loadproperties(OSHomePage);
		temp = p.getProperty("lnkconnectormanagers");
		return temp;
	}
	
	public String crawlandindex()
	{
		loadproperties(OSHomePage);
		temp = p.getProperty("lnkcrawlandindex");
		return temp;
	}
	
	public String currentfeeds()
	{
		loadproperties(OSHomePage);
		temp = p.getProperty("lnkfeeds");
		return temp;
	}
	
	
	public String cmexists()
	{
		loadproperties(OSConnectorConfigPage);
		temp = p.getProperty("cmexists");
		return temp;
	}
	
	public String selectcm()
	{
		loadproperties(OSSelectCMPage);
		temp = p.getProperty("connectormanager");
		return temp;
	}
	
	public String addnewconnectorbtn()
	{
		loadproperties(OSSelectCMPage);
		temp = p.getProperty("btnaddnewconnector");
		return temp;
	}
	
	public String txtconnectorname()
	{
		loadproperties(OSConnectorConfigPage);
		temp = p.getProperty("txtconnectorname");
		return temp;
	}
	
	public String selectconnectortype()
	{
		loadproperties(OSConnectorConfigPage);
		temp = p.getProperty("selectconnectortype");
		return temp;
	}
	
	public String getconfigformbtn()
	{
		loadproperties(OSConnectorConfigPage);
		temp = p.getProperty("btngetconfigform");
		return temp;
	}
	
	public String saveconfigurationbtn()
	{
		loadproperties(OSConnectorConfigPage);
		temp = p.getProperty("btnsaveconfiguration");
		return temp;
	}
	
	public String gsaerrormessage()
	{
		loadproperties(OSGSAConfig);
		temp=p.getProperty("lblerrormessage");
		return temp;
	}
	
	public String chkboxispublic()
	{
		loadproperties(OSGSAConfig);
		temp=p.getProperty("chkispublic");
		return temp;
	}
	
	public String txttraversalrate()
	{
		loadproperties(OSGSAConfig);
		temp=p.getProperty("txttraversalrate");
		return temp;
	}
	
	public String txtretrydelay()
	{
		loadproperties(OSGSAConfig);
		temp=p.getProperty("txtretrydelay");
		return temp;
	}
	
	public String lblcurrentfeed()
	{
		loadproperties(OSFeedPage);
		temp=p.getProperty("lblcurrentfeed");
		return temp;
	}
	
}
