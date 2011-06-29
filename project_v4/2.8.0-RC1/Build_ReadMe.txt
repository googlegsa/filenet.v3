Configure the development environment for the Filenet connector (Running build.xml with ANT) for IBM FileNet P8 Systems 4.0,4.5 and 4.5.1
=====================================================================================================================================
1. Put "ant-contrib-1.0b3.jar" in the $ANT_HOME\lib. 
You can download this jar from "http://sourceforge.net/project/showfiles.php?group_id=36177".
2. Copy all the jars from lib folder of connector manager and put it in "\source\third_party\lib" directory.
3. Copy all the files mentioned below and put it in "\source\third_party\lib" directory. 

Required files for IBM FileNet P8 Systems 4.0 or 4.5 connector configuiration: Default location for this jar on IBM FileNet P8 Systems 4.0,4.5 server is "< FILENET_HOME >\ContentEngine\wsi\lib\"

• activation.jar
• builtin_serialization.jar
• javaapi.jar
• log4j.1.2.x.jar 
• jetty.jar
• jaxrpc.jar
• saaj.jar
• wasp.jar
• wsdl_api.jar

Required files for IBM FileNet P8 Systems 4.5.1 connector configuiration: Default location for this jar on IBM FileNet P8 Systems 4.5.1 server is "< FILENET_HOME >\Workplace\download\"
• activation.jar 
• log4j.jar
• javaapi.jar 
• stax-api.jar 
• xlxpScanner.jar 
• xlxpScannerUtils.jar 

4. Copy the file Jace.jar from your IBM FileNet P8 Systems 4.0,4.5 and 4.5.1 server and put it in "\source\third_party\lib" directory. Default location for this jar on IBM FileNet P8 Systems 4.0,4.5 and 4.5.1 server is "< FILENET_HOME >\ContentEngine\lib\"
5. Copy the file junit.jar from your IBM FileNet P8 Systems 4.0,4.5 and 4.5.1 server and put it in "\source\third_party\lib" directory. 

CONFIGURATION
=============
Open Eclipse and create a java project from the existing source from http://code.google.com/p/google-enterprise-connector-file/
Add all the jars that are in third_party\lib and file_third_party\lib to the classpath of the project.

JUNIT TESTS
===========
To run the unit tests, open the class com.google.enterprise.connector.filenet4.TestConnection in the folder fnapi-core/source/javatests
to add FileNet environment configuration.