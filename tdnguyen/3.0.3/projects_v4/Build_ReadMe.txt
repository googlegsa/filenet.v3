Following are the steps to build Google Enterprise Connector for IBM FileNet P8 Systems 4.0,4.5 and 4.5.1:
==========================================================================================================
1. Ensure that you have Apache Ant installed on your system. It can be downloaded from http://ant.apache.org/bindownload.cgi

2. Ensure that an environment variable with the name ANT_HOME is created on your system and is pointing to the installed ANT home directory.

3. Ensure that "ant-contrib-1.0b3.jar" is present in the $ANT_HOME\lib. It can be downloaded from  "http://sourceforge.net/project/showfiles.php?group_id=36177".

4. Ensure that "Silk SVN, Subversion" is installed on your system. It can be downloaded from  "http://www.sliksvn.com/en/download".

5. Ensure that an system environment variable with the name PATH is created on your system and is pointing to the installed Silk SVN, Subversion bin directory.

6. Ensure that the latest Connector Manager binaries are present on your system. Latest Connector Manager binaries can be downloaded from http://code.google.com/p/google-enterprise-connector-manager/downloads/list

Required Connector Manager libraries:
* connector-spi.jar
* connector-util.jar
* connector.jar
* connector-logging.jar

7. The Connector Manager required libraries should be in '/{CONNECTOR_MANAGER_DIR}/dist/jarfile/' folder. 
Run the Connector Manager build to create Connector Manager required libraries.

Required files for IBM FileNet P8 Systems 4.0 or 4.5 connector configuration: Default location for this jar on IBM FileNet P8 Systems 4.0,4.5 server is "< FILENET_HOME >\ContentEngine\wsi\lib\"

* activation.jar
* builtin_serialization.jar
* javaapi.jar
* log4j.1.2.x.jar 
* jetty.jar
* jaxrpc.jar
* saaj.jar
* wasp.jar
* wsdl_api.jar

Required files for IBM FileNet P8 Systems 4.5.1 connector configuration: Default location for this jar on IBM FileNet P8 Systems 4.5.1 server is "< FILENET_HOME >\Workplace\download\"

* activation.jar 
* javaapi.jar 
* stax-api.jar 
* xlxpScanner.jar 
* xlxpScannerUtils.jar 
* wasp.jar

8. Copy all the  above required jars to the "\<FileNet_Source_Directory>\third_party\lib" directory. 

9. Copy the Jace.jar file from your FileNet P8 server and put it in "\<FileNet_Source_Directory>\third_party\lib" directory. Default location for Jace.jar on FileNet P8 server is "\Workplace\WEB-INF\lib\"

10. On command prompt, navigate to the FileNet source directory where build.xml is present and execute 'ant' command. The 'connector-file4.jar' will be created in the dist/jarfile directory.