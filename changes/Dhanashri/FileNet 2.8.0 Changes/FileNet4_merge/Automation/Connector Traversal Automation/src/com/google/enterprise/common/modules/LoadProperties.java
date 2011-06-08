/**
 * 
 */
package com.google.enterprise.common.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * @author vishwas_londhe
 *
 */
public class LoadProperties {

	/**
	 * This class loads GSA properties from a property file and gives getter methods to retrieve values
	 */
		
	Properties p = new Properties();
	String GSAProperties = "common\\GSA.properties";
	String SeleniumProperties = "common\\Selenium.properties";
	String ErrorMessages = "common\\GSAErrors.properties";
	String temp = "";
	FileInputStream input;
	FileOutputStream output;
	
	private void loadproperties(String propertyfile)
	{
		try
		{
			p.load(input = new FileInputStream(new File(propertyfile)));
			input.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("*****Unable to load Properties file*****");
		}
	}
	
	public String gsausername()
	{
		loadproperties(GSAProperties);
		temp = p.getProperty("username");
		return temp;
	}

	public String gsapassword()
	{
		loadproperties(GSAProperties);
		temp = p.getProperty("password");
		return temp;
	}
	public String connectormanager()
	{
		loadproperties(GSAProperties);
		temp = p.getProperty("connectormanager");
		return temp;
	}

	public String traversalrate()
	{
		loadproperties(GSAProperties);
		temp = p.getProperty("traversalrate");
		return temp;
	}
	
	public String connectorfeedfile()
	{
		loadproperties(GSAProperties);
		temp = p.getProperty("connectorfeedfile");
		return temp;
	}
	
	public String applicationcontext()
	{
		loadproperties(GSAProperties);
		temp = p.getProperty("applicationcontext");
		return temp;
	}
	
	public String connectorlogsfolder()
	{
		loadproperties(GSAProperties);
		temp = p.getProperty("connectorlogsfolder");
		return temp;
	}
	
	public String StartconnectorService()
	{
		loadproperties(GSAProperties);
		temp = p.getProperty("startconnectorservice");
		return temp;
	}
	
	public String StopconnectorService()
	{
		loadproperties(GSAProperties);
		temp = p.getProperty("stopconnectorservice");
		return temp;
	}
	
	
	public String seleniumurl()
	{
		loadproperties(SeleniumProperties);
		temp = p.getProperty("url");
		return temp;
	}

	public String seleniumbrowser()
	{
		loadproperties(SeleniumProperties);
		temp = p.getProperty("browser");
		return temp;
	}
	public String seleniumserver()
	{
		loadproperties(SeleniumProperties);
		temp = p.getProperty("server");
		return temp;
	}
	public int seleniumserverport()
	{
		loadproperties(SeleniumProperties);
		int temp = Integer.parseInt(p.getProperty("serverport"));
		return temp;
	}
	public String geterrormessage(String errormsgfile, String error)
	{
		loadproperties(errormsgfile);
		temp = p.getProperty(error);
		return temp;
	}
	public String blankconnectornameerror()
	{
		loadproperties(ErrorMessages);
		temp = p.getProperty("blankconnectorname");
		return temp;
	}
	public String blankusernameerror()
	{
		loadproperties(ErrorMessages);
		temp = p.getProperty("blankusername");
		return temp;
	}
	public String blankpassworderror()
	{
		loadproperties(ErrorMessages);
		temp = p.getProperty("blankpassword");
		return temp;
	}
	public String blankobjectstoreerror()
	{
		loadproperties(ErrorMessages);
		temp = p.getProperty("blankobjectstore");
		return temp;
	}
	public String blankcontentengineurlerror()
	{
		loadproperties(ErrorMessages);
		temp = p.getProperty("blankcontentengineurl");
		return temp;
	}
	public String blankworkplaceurlerror()
	{
		loadproperties(ErrorMessages);
		temp = p.getProperty("blankworkplaceurl");
		return temp;
	}
	public String invalidobjectstoreerror()
	{
		loadproperties(ErrorMessages);
		temp = p.getProperty("invalidobjectstore");
		return temp;
	}
	public String invalidcontentengineurlerror()
	{
		loadproperties(ErrorMessages);
		temp = p.getProperty("invalidcontentengineurl");
		return temp;
	}
	public String invalidworkplaceurlerror()
	{
		loadproperties(ErrorMessages);
		temp = p.getProperty("invalidworkplaceurl");
		return temp;
	}
	public String invalidcredentialserror()
	{
		loadproperties(ErrorMessages);
		temp = p.getProperty("invalidcredentials");
		return temp;
	}
	
	public void setproperty(String Filename, String Key, String Value) 
	{
		Properties sp = new Properties();
		try
		{
			sp.load(input = new FileInputStream(new File(Filename)));
			input.close();
			if(sp.getProperty(Key)== null)
			{
				sp.clear();
				sp.setProperty(Key, Value);
				sp.store(output = new FileOutputStream(Filename,true), null);
				output.close();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("*****Unable to load Properties file*****");
		}
		
	}
}
