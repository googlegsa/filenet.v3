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
public class FilenetObjects
{
  static Properties p = new Properties();
  String temp = "";
  String OSConnectorConfigPage = "objectstore\\FileNetConfigPage.properties";

  private void loadproperties(String objectstore)
  {
    try
    {
      p.load(new FileInputStream(new File(objectstore)));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.out.println("*****Unable to load Properties file*****");
    }
  }

  public String lblconnectormanager()
  {
    loadproperties(OSConnectorConfigPage);
    temp = p.getProperty("lblconnectormanager");
    return temp;
  }

  public String lblconnectortype()
  {
    loadproperties(OSConnectorConfigPage);
    temp = p.getProperty("lblconnectortype");
    return temp;
  }

  public String txtconnectorusername()
  {
    loadproperties(OSConnectorConfigPage);
    temp = p.getProperty("txtconnectorusername");
    return temp;
  }

  public String txtconnectorpassword()
  {
    loadproperties(OSConnectorConfigPage);
    temp = p.getProperty("txtconnectorpassword");
    return temp;
  }

  public String txtobjectstore()
  {
    loadproperties(OSConnectorConfigPage);
    temp = p.getProperty("txtobjectstore");
    return temp;
  }

  public String txtcontentengineurl()
  {
    loadproperties(OSConnectorConfigPage);
    temp = p.getProperty("txtcontentengineurl");
    return temp;
  }

  public String txtworkplaceurl()
  {
    loadproperties(OSConnectorConfigPage);
    temp = p.getProperty("txtworkplaceurl");
    return temp;
  }

  public String txtadditionalwhereclause()
  {
    loadproperties(OSConnectorConfigPage);
    temp = p.getProperty("txtadditionalwhereclause");
    return temp;
  }

  public String deleteconnector()
  {
    loadproperties(OSConnectorConfigPage);
    temp = p.getProperty("lnkdeleteconnector");
    return temp;
  }

  public String editconnector()
  {
    loadproperties(OSConnectorConfigPage);
    temp = p.getProperty("lnkeditconnector");
    return temp;
  }

  public String connectorexists()
  {
    loadproperties(OSConnectorConfigPage);
    temp = p.getProperty("connectorexists");
    return temp;
  }
}
