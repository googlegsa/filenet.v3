This readme file presents how to configure the development environment for the Filenet connector.

REQUIRED FILES to compile and run the tests
•	activation.jar (in <Filenet root folder>/Worplace/WEB-INF/libs)
•	javaapi.jar (in <Filenet root folder>/Worplace/WEB-INF/libs)
•	listener.jar (in <Filenet root folder>/Worplace/WEB-INF/libs)
•	log4j-1.2.8.jar (in <Filenet root folder>/Worplace/WEB-INF/libs)
•	mailapi.jar (in <Filenet root folder>/Worplace/WEB-INF/libs)
•	xercesImpl.jar (in <Filenet root folder>/Worplace/WEB-INF/libs)
•	xml-apis.jar (in <Filenet root folder>/Worplace/WEB-INF/libs)
•	commons-httpclient-3.0.1.jar (in file_third_party/lib folder of the project)
•	commons-codec-1.3.jar (in file_third_party/lib folder of the project)
•	soap.jar (in file_third_party/lib folder of the project)
•	json.jar (in third_party/lib folder of the project)
•	jcr-1.0.jar
•	junit.jar (in third_party/lib folder of the project)
•	spring.jar (in third_party/lib folder of the project)
•	commons-logging.jar (in third_party/lib folder of the project)
•	connector-spi.jar (to copy in third_party/lib folder of the project)
•	connector.jar (to copy in third_party/lib folder of the project
•	connector-tests.jar (to copy in third_party/lib folder of the project


To build and run the junit tests for the connector manager, you must download 
jcr-1.0.jar and put it in the directory third_party/lib.  This jar is part of JSR-170.  
To get this jar, go to the official JSR-170 site, and follow the links from there:
http://jcp.org/aboutJava/communityprocess/review/jsr170/index.html


CONFIGURATION
Open Eclipse and create a java project from the existing source "connector-file"
Add to the classpath of the project all the jars that are in third_party\lib and file_third_party\lib
Also, add to the classpath 3 Class folders linked to the folder 
•	google_enterprise_connector_file/config


JUNIT TESTS
To run the unit tests, open the classes 
•	com.google.enterprise.connector.file.FnConnection in the folder file-core/source/javatests
•	com.google.enterprise.connector.file.FnMockConnection in the folder file-cilent/source/javatests
to add your environment configuration.