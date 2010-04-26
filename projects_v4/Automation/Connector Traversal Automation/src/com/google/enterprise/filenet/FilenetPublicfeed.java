/**
 * 
 */
package com.google.enterprise.filenet;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.enterprise.common.modules.FileUtils;
import com.google.enterprise.common.modules.Tasks;
import com.thoughtworks.selenium.SeleneseTestCase;


/**
 * @author vishwas_londhe
 *
 */
public class FilenetPublicfeed extends SeleneseTestCase
{
	@BeforeClass
	public void setup()
	{
		Tasks.getSession();
		Tasks.LoginToGSA();
		Tasks.gotoConnectorAdministration();
		Tasks.gotoConnectors();
		
		if(Tasks.IsConnectorPresent(FilenetTasks.obj.connectorexists())== true)
			FilenetTasks.DeleteConnectorInstance();
		
		System.out.println(Tasks.property.connectormanager());
		
		Tasks.selectConnectorManager(Tasks.property.connectormanager());
		Tasks.setConnectorName(FilenetTasks.property.connectorname());
	}
	
	@AfterClass
	public void teardown()
	{
		if(Tasks.selenium.isElementPresent(Tasks.obj.gsalogoutbtn()))
			Tasks.LogoutGSA();
		Tasks.endSession();
	}
	
	@Test
	public void testconfigureconnector()
	{
		FilenetTasks.configureConnector();
		Tasks.setIspublicState("on");
		Tasks.setTraversalRate("52");
		Tasks.setRetryDelay("15");
		Tasks.selenium.click(Tasks.obj.saveconfigurationbtn());
		Tasks.waitfor(FilenetTasks.obj.connectorexists());
	}
	
	@Test(dependsOnMethods="testconfigureconnector")
	public void testIsConnectorRunning()
	{
		assertEquals(Tasks.IsConnectorPresent(FilenetTasks.obj.connectorexists()), true);
	}
	
	@Test(dependsOnMethods="testIsConnectorRunning")
	public void testConfigurationSaved()
	{
		assertEquals(FileUtils.CheckConfigSaved(FilenetTasks.property.connectorname(),"baseline\\fpautomationPublic.properties"), true);
	}
	
	@Test(dependsOnMethods="testConfigurationSaved")
	public void testFeedfile()
	{
		assertEquals(FileUtils.CheckFeedFile("baseline\\fpautomationFeedFilePublic.log"), true);
	}
	
	@Test(dependsOnMethods="testFeedfile")
	public void testGSAFeed()
	{
		Tasks.sleep(180);
		Tasks.gotoCurrentFeeds();
		
	}
}
