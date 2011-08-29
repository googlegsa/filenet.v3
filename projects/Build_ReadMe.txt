Following are the steps to build Google Enterprise Connector for IBM FileNet P8 Systems 3.5.2:
===============================================================================================
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

Required files for IBM FileNet P8 Systems 3.5.2 connector configuration: Default location for this jar on IBM FileNet P8 Systems 3.5.2 server is "< FILENET_HOME >\Workplace\WEB-INF\lib"

* javaapi.jar
* activation.jar
* mailapi.jar
* p8cjares.jar
* soap.jar

8. Copy all the  above required jars to the "\<FileNet_Source_Directory>\third_party\lib" directory. 

9. On command prompt, navigate to the FileNet source directory where build.xml is present and execute 'ant' command. The 'connector-file.jar' will be created in the dist/jarfile directory.
