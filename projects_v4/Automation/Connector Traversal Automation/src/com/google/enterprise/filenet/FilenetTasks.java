/**
 *
 */
package com.google.enterprise.filenet;

import com.google.enterprise.common.modules.Tasks;

/**
 * @author vishwas_londhe
 *
 */
public class FilenetTasks
{
  public static FilenetObjects obj = new FilenetObjects();
  public static FilenetProperties property = new FilenetProperties();

  public static void configureConnector()
  {
    Tasks.selenium.type(obj.txtconnectorusername(), property.connectorusername());
    Tasks.selenium.type(obj.txtconnectorpassword(), property.connectorpassword());
    Tasks.selenium.type(obj.txtobjectstore(), property.objectstore());
    Tasks.selenium.type(obj.txtcontentengineurl(), property.contentenginurl());
    Tasks.selenium.type(obj.txtworkplaceurl(), property.workplaceurl());
    Tasks.selenium.type(obj.txtadditionalwhereclause(), property.additionalwhereclause());
  }

  public static boolean DeleteConnectorInstance()
  {
    Tasks.gotoConnectors();
    Tasks.selenium.click(obj.deleteconnector());
    Tasks.sleep(5);
    Tasks.selenium.getConfirmation();
    Tasks.selenium.chooseOkOnNextConfirmation();
    Tasks.sleep(15);
    return true;
  }

  public static boolean EditConnectorInstance()
  {
    Tasks.gotoConnectors();
    Tasks.selenium.click(obj.editconnector());
    Tasks.sleep(5);
    return true;
  }

}
