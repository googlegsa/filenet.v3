/**
 *
 */
package com.google.enterprise.common.modules;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Properties;

import com.google.enterprise.filenet.FilenetTasks;


/**
 * This class has functions to compare property files and log files
 *
 * used to compare configuration property files and feed log files
 *
 *@author vishwas_londhe
 */
public class FileUtils
{
  public static LoadProperties property = new LoadProperties();
  public static LoadObjects obj = new LoadObjects();

  /**
   * CheckConfigSaved Function checks connector configurations file
   * with the baseline(expected)configuration property files
   * */
  public static boolean CheckConfigSaved(String ConnectorName,String ConfigFile)
  {
    System.out.println("Checking configuration files...");
    Properties Actual = new Properties();
    Properties Expected = new Properties();
    boolean Flag = true;

    try
    {
      Thread.sleep(5000);
      Actual.load(new FileInputStream(new File(FilenetTasks.property.connectorpath()+ ConnectorName+".properties")));
      Expected.load(new FileInputStream(new File(ConfigFile)));

      Collection<Object> ActualValues= Actual.values();
      Collection<Object> ExpectedValues= Expected.values();
      Object ActualKeys[] = Actual.keySet().toArray();
      Object ExpectedKeys[] = Expected.keySet().toArray();

      Object AValues[]= ActualValues.toArray();
      Object EValues[]= ExpectedValues.toArray();

      if (ActualKeys.length == ExpectedKeys.length)
      {
        for (int i = 0;i< ActualKeys.length;i++)
        {
          if (!ActualKeys[i].toString().contentEquals(ExpectedKeys[i].toString()))
          {
            System.out.println("Actual = " +ActualKeys[i]+ "Expected = " +ExpectedKeys[i]);
            Flag = false;
          }

          if (!AValues[i].toString().contentEquals(EValues[i].toString()))
          {
            if (!ActualKeys[i].toString().contentEquals("Password"))
            {
              System.out.println("Actual = " +AValues[i]+ "Expected = " +EValues[i]);
              Flag = false;
            }

          }
        }
      }
      else
        Flag = false;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.out.println("*****Unable to load"+ConnectorName+".Properties file*****");
      return false;
    }
    return Flag;
  }

  /**
   * CheckFeedFile Function checks feed logs against the baseline(expected)
   * feed log file
   * WaitTime should be equal to the time taken by connector to generate a feed file
   * */
  public static boolean CheckFeedFile(String FeedFile, int WaitTime)
  {
    System.out.println("Checking Feed file...");
    boolean Flag = true;
    try
    {
      Thread.sleep(WaitTime*1000);
      File Expectedfile = new File(FeedFile);
      File Actualfile = new File(property.connectorfeedfile());
      FileInputStream ExpectedFileInputStream = null;
      BufferedReader ExpectedBufferedReader = null;
        InputStreamReader ExpectedInputStreamReader = null;
        FileInputStream ActualFileInputStream = null;
      BufferedReader ActualBufferedReader = null;
        InputStreamReader ActualInputStreamReader = null;

        ExpectedFileInputStream = new FileInputStream(Expectedfile);
      ExpectedInputStreamReader = new InputStreamReader(ExpectedFileInputStream);
      ExpectedBufferedReader = new BufferedReader(ExpectedInputStreamReader);
      String Expected =ExpectedBufferedReader.readLine();

      ActualFileInputStream = new FileInputStream(Actualfile);
      ActualInputStreamReader = new InputStreamReader(ActualFileInputStream);
      ActualBufferedReader = new BufferedReader(ActualInputStreamReader);
      String Actual =ActualBufferedReader.readLine();

      System.out.println("Expected = " + Expected );
      System.out.println("Actual = " + Actual);

      if (Actual == null)
      {
        Flag = false;
        System.out.println("Feed file is empty!!!");
      }
      while (Expected != null && Actual != null)
      {
        if (Expected.contains("com.google.enterprise.connector.pusher.DocPusher submitFeed"))
//          System.out.println("This is Log generation time: " + Expected);
          System.out.println("");
        else
        {
          if (Expected.compareTo(Actual)!=0)
          {
            Flag = false;
            System.out.println("Expected = " + Expected );
            System.out.println("Actual = " + Actual);
          }

        }
        Expected =ExpectedBufferedReader.readLine();
//        System.out.println("Expected = " + Expected);
        Actual =ActualBufferedReader.readLine();
//        System.out.println("Actual = " + Actual);
        if ((Expected == null && Actual != null)||(Expected != null && Actual == null))
        {
          Flag = false;
          System.out.println("Failed Expected = " + Expected );
          System.out.println("Failed Actual = " + Actual);
          return Flag;
        }

      }

      ExpectedFileInputStream.close();
      ActualFileInputStream.close();
    }
    catch ( Exception e)
    {
      e.printStackTrace();
      Flag = false;
      return Flag;
    }

    return Flag;
  }

  public static boolean ClearLogs()
  {
    System.out.println("Deleting log files...");
    File directory = new File(property.connectorlogsfolder());
    // Get all files in directory
    File[] files = directory.listFiles();
    for (File file : files)
    {
       // Delete each file

       if (!file.delete())
       {
           // Failed to delete file
           System.out.println("Failed to delete "+file);
           return false;
       }
    }
    return true;
  }

  public static boolean ClearConnectorState(String StateFilePath)
  {
    System.out.println("Deleting connector state...");
    File statefile = new File(StateFilePath);
    if (!statefile.delete())
    {
      System.out.println("Failed to delete "+statefile);
        return false;
    }
    return true;
  }
}
