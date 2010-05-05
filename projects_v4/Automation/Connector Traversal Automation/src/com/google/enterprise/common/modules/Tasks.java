/**
 * 
 */
package com.google.enterprise.common.modules;

//import com.google.enterprise.connector.common.GlobalConstants;

import java.io.IOException;

import org.openqa.selenium.server.SeleniumServer;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.SeleneseTestCase;

/**
 * This class provides functions with performs simple tasks such as
 * navigation or combination of some GUI operations which can be grouped
 * to be called as a single operation

 * @author vishwas_londhe
 */
public class Tasks extends SeleneseTestCase
{
	static SeleniumServer  SELENIUM_SERVER;
	public static DefaultSelenium selenium;
	public static LoadProperties property = new LoadProperties();
	
	public static LoadObjects obj = new LoadObjects();
	
	public static boolean getSession()
	{
		System.out.println("Establishing selenium session...");
		try
		{
			selenium = new DefaultSelenium(property.seleniumserver(),property.seleniumserverport(),property.seleniumbrowser(),property.seleniumurl());
			selenium.setSpeed("500");
			selenium.start();
			
			selenium.windowMaximize();
			selenium.open(property.seleniumurl());
			selenium.waitForPageToLoad("10000");
		
		}
		catch(Exception e)
		{
			return false;
		}
		
		return true;
	}
	
	public static void LoginToGSA() 
	{
		selenium.type(obj.gsausername(), property.gsausername());
		selenium.type(obj.gsapassword(), property.gsapassword());
		selenium.click(obj.gsaloginbtn());
		
		waitfor(obj.connectoradministration());
	}
	
	public static void gotoHome()
	{
		selenium.click(obj.gsahome());
		waitfor(obj.connectoradministration());
	}
	
	public static void gotoConnectorAdministration()
	{
		selenium.click(obj.connectoradministration());
		waitfor(obj.connectormanagers());
	}
	
	public static void gotoConnectors()
	{
//		selenium.click(obj.connectors());
//		waitfor(obj.selectcm());
		selenium.open("/EnterpriseController?actionType=viewConnectors");
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			try { if ("Connectors".equals(selenium.getTitle())) break; } catch (Exception e)
			{e.printStackTrace();}
			sleep(1);}
	}
	
	public static void gotoConnectorManagers()
	{
//		selenium.click(obj.connectormanagers());
		selenium.open("/EnterpriseController?actionType=viewConnectorManagers");
		for (int second = 0;; second++){
			if (second >= 60) fail("timeout");
			try {if ("Connector Managers".equals(selenium.getTitle())) break;}catch (Exception e)
			{e.printStackTrace();}
			sleep(1);}
	}
	
	public static void gotoCrawlandIndex()
	{
		selenium.open("/EnterpriseController?actionType=startURLs");
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			try { if ("Crawl URLs".equals(selenium.getTitle())) break; } catch (Exception e) {}
			sleep(1);}
	}
	public static void gotoCurrentFeeds()
	{
//		selenium.click(obj.crawlandindex());
//		waitfor(obj.currentfeeds());
		gotoCrawlandIndex();
		selenium.click(obj.currentfeeds());
//		waitfor(obj.lblcurrentfeed());
		for (int second = 0;; second++)	{
			if (second >= 60) fail("timeout");
			try{if ("Feeds".equals(selenium.getTitle())) break;}catch (Exception e)
			{e.printStackTrace();}
			sleep(10);}
	}
	
	public static boolean selectConnectorManager(String CM)
	{
//		waitfor(obj.selectcm());
//		sleep(5);
		gotoConnectors();
		selenium.select(obj.selectcm(), CM);
		selenium.click(obj.addnewconnectorbtn());
		waitfor(obj.selectconnectortype());
		return true;
	}
	
	public static boolean setConnectorName(String CN)
	{
//		selenium.type(obj.txtconnectorname(), property.connectorname());
//		selenium.select(obj.selectconnectortype(), property.connectortype());
		selenium.type(obj.txtconnectorname(),CN);
		selenium.click(obj.getconfigformbtn());
		waitfor(obj.selectconnectortype());
		return true;
	}
	
	
	public static boolean LogoutGSA()
	{
		selenium.click(obj.gsalogoutbtn());
		return true;
	}
	public static boolean endSession()
	{
		System.out.println("Releasing selenium session...");
		selenium.close();
		selenium.stop();
		return true;
	}
	
	public static boolean IsConnectorPresent(String connector)
	{
		return (selenium.isElementPresent(connector));
	}
	
	public static boolean IsCMPresent()
	{
		return (selenium.isElementPresent(obj.cmexists()));
	}
	
	public static boolean IsErrorPresent()
	{
		return (selenium.isElementPresent(obj.gsaerrormessage()));
	}
	
	public static String GetGSAErrorMsg()
	{
		if(selenium.getText(obj.gsaerrormessage()).contains("User admin logged in from"))
		{
			return(selenium.getText(obj.gsaerrormessage()+ "[2]"));
		}
		else
		{
			return(selenium.getText(obj.gsaerrormessage()));
		}
		
	}
	
	public static void setIspublicState(String state)
	{
		System.out.println(selenium.getValue(obj.chkboxispublic()));
		if(!selenium.getValue(obj.chkboxispublic()).contentEquals(state))
			selenium.click(obj.chkboxispublic());
	}
	
	public static void setTraversalRate(String rate)
	{
		selenium.type(obj.txttraversalrate(), rate);
	}
	
	public static void setRetryDelay(String delay)
	{
		System.out.println(obj.txtretrydelay());
		selenium.type(obj.txtretrydelay(),delay);
	}
	
	public static void startConnectorService()
	{
		Runtime load = Runtime.getRuntime();
		String command = "cmd /C start "+property.StartconnectorService()+" ";
		try 
		{
			load.exec(command);
			System.out.println("Starting Connector Service...");
			sleep(30);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	public static void stopConnectorService()
	{
		Runtime load = Runtime.getRuntime();
		String command = "cmd /C start "+property.StopconnectorService()+" ";
		try 
		{
			load.exec(command);
			System.out.println("Stopping Connector Service...");
			sleep(30);
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
		
	public static void waitfor(String element) 
	{
		for (int second = 0;; second++) 
		{
			if (second >= 60) fail("timeout");
			try { if (selenium.isElementPresent(element)) break; } catch (Exception e) {}
			try {Thread.sleep(1000);}
			catch (InterruptedException e) {e.printStackTrace();}
		}
	}
	
	public static void sleep(int time)
	{
		try {
				Thread.sleep(1000 * time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
	
	public static void StartSelenium()
	{
		try
        {
        	SeleniumServer.reusingBrowserSessions();
        	SeleniumServer.setTrustAllSSLCertificates(true);
        	SeleniumServer.setAvoidProxy(false);
        	SELENIUM_SERVER= new SeleniumServer(property.seleniumserverport());
        	SELENIUM_SERVER.isMultiWindow();
	        SELENIUM_SERVER.start();
        }
        catch(Exception e)
        {
        	throw new IllegalStateException("Can't start selenium server", e);
        }
	}
	
	public static void StopSelenium()
	{
		try
        {
				SELENIUM_SERVER.stop();
        }
        catch(Exception e)
        {
        	throw new IllegalStateException("Can't stop selenium server", e);
        }
	}

	public static void setfeedlogtrue()
	{
		try 
		{
			property.setproperty(property.applicationcontext(), "feedLoggingLevel", "ALL");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
	}

}
