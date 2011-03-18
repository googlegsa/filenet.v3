/**
 * 
 */
package com.google.enterprise.filenet;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author vishwas_londhe
 *
 */
public class FilenetProperties 
{
	Properties p = new Properties();
	String FileNetProperties = "common\\FileNet.properties";
	String temp = "";
	FileInputStream input;
	
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
	
	public String connectorname()
	{
		loadproperties(FileNetProperties);
		temp = p.getProperty("connectorname");
		return temp;
	}

	public String connectorusername()
	{
		loadproperties(FileNetProperties);
		temp = p.getProperty("connectorusername");
		return temp;
	}
	public String connectorpassword()
	{
		loadproperties(FileNetProperties);
		temp = p.getProperty("connectorpassword");
		return temp;
	}
	public String connectortype()
	{
		loadproperties(FileNetProperties);
		temp = p.getProperty("connectortype");
		return temp;
	}

	public String objectstore()
	{
		loadproperties(FileNetProperties);
		temp = p.getProperty("objectstore");
		return temp;
	}
	public String contentenginurl()
	{
		loadproperties(FileNetProperties);
		temp = p.getProperty("contentengineurl");
		return temp;
	}
	public String workplaceurl()
	{
		loadproperties(FileNetProperties);
		temp = p.getProperty("workplaceurl");
		return temp;
	}
	public String additionalwhereclause()
	{
		loadproperties(FileNetProperties);
		temp = p.getProperty("additionalwhereclause");
		return temp;
	}
	
	public String connectorstatefile()
	{
		loadproperties(FileNetProperties);
		temp = p.getProperty("connectorstatefile");
		return temp;
	}
	
	public String connectorpath()
	{
		loadproperties(FileNetProperties);
		temp = p.getProperty("connectorpath");
		return temp;
	}
	
	public String startservicebat()
	{
		loadproperties(FileNetProperties);
		temp = p.getProperty("startconnectorservice");
		return temp;
	}
	
	public String stopservicebat()
	{
		loadproperties(FileNetProperties);
		temp = p.getProperty("stopconnectorservice");
		return temp;
	}
}
