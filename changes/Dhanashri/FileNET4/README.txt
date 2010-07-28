This readme file presents how to configure the development environment for the Filenet connector.

REQUIRED FILES to compile and run the tests
�    Jace.jar (in <Filenet CE root folder>/CE_API/lib)
�    log4j-1.2.8.jar (in <Filenet CE root folder>/CE_API/lib)
�    commons-httpclient-3.0.1.jar (in file_third_party/lib folder of the project)
�    commons-codec-1.3.jar (in file_third_party/lib folder of the project)
�    json.jar (in third_party/lib folder of the project)
�    junit.jar (in third_party/lib folder of the project)
�    spring.jar (in third_party/lib folder of the project)
�    commons-logging.jar (in third_party/lib folder of the project)
�    connector-spi.jar (to copy in third_party/lib folder of the project)
�    connector.jar (to copy in third_party/lib folder of the project
�    connector-tests.jar (to copy in third_party/lib folder of the project



CONFIGURATION
Open Eclipse and create a java project from the existing source "connector-file"
Add to the classpath of the project all the jars that are in third_party\lib and file_third_party\lib
Also, add to the classpath 3 Class folders linked to the folder
�    google_enterprise_connector_file/config


JUNIT TESTS
To run the unit tests, open the classes
�    com.google.enterprise.connector.file.TestConnection in the folder fnapi-core/source/javatests
to add your environment configuration.