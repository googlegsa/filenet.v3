/**
 *
 */
package com.google.enterprise.filenet;

import com.google.enterprise.common.modules.FileUtils;
import com.google.enterprise.common.modules.Tasks;

import junit.framework.TestCase;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * @author vishwas_londhe
 *
 */
public class FileNetTestSuite extends TestCase
{
  static TestNG testng = new TestNG();
  static TestListenerAdapter tla = new TestListenerAdapter();


  @BeforeSuite
  public void setup()
  {
    Tasks.startConnectorService();
    Tasks.getSession();
    Tasks.setfeedlogtrue();
  }

  @AfterSuite
  public void teardown()
  {
    Tasks.endSession();
    Tasks.stopConnectorService();
    Tasks.sleep(60);
    FileUtils.ClearLogs();
  }
  @Test
  public void FilenetTestSuite()
  {
    testng.setDefaultTestName("Connector Traversal");
    testng.setOutputDirectory("TestResults\\TestNG");
    testng.addListener(tla);

    testng.setTestClasses
    (
        new Class[]
                  {
              FileNet.class,
              FilenetPublicfeed.class
                  }
    );

    testng.run();
  }
}

