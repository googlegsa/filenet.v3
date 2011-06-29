Configure the development environment for the FileNet connector (Running build.xml with ANT) for IBM FileNet P8 Systems 3.5.2
=============================================================================================================================
1. Put "ant-contrib-1.0b3.jar" in the $ANT_HOME\lib.
You can download this jar from "http://sourceforge.net/project/showfiles.php?group_id=36177".
2. Copy all the jars from lib folder of connector manager and put it in "\source\third_party\lib" directory.
3. Copy all the files mentioned below and put it in "\source\third_party\lib" directory.

Required files for IBM FileNet P8 Systems 3.5.2 connector configuration: Default location for this jar on IBM FileNet P8 Systems 3.5.2 server is "< FILENET_HOME >\Workplace\WEB-INF\lib"

� javaapi.jar
� activation.jar
� mailapi.jar
� p8cjares.jar
� log4j.1.2.x.jar
� soap.jar
� spring.jar

4. Copy the file junit.jar from your IBM FileNet P8 Systems 3.5.2 server and put it in "\source\third_party\lib" directory.

CONFIGURATION
=============
Open Eclipse and create a java project from the existing source from http://code.google.com/p/google-enterprise-connector-file/.
Add all the jars that are in third_party\lib and file_third_party\lib to the classpath of the project.

JUNIT TESTS
===========
To run the unit tests, add FileNet environment configuration to the FnMockConnection.java, FnConnection.java, FileNETConnection.java files.