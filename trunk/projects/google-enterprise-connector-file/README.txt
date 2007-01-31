This readme file presents how to configure the development environment for the FileNET connector.

REQUIRED FILES
•	WcmApiConfig.properties
•	UTCryptoKeyFile.properties
•	javaapi.jar
•	p8cjares.jar
•	jcr-1.0.jar
•	connector_spi.jar
•	connector.jar
•	connector_tests.jar

CONFIGURATION
Add the jars file in the folder projects\third_party\lib.
Copy the properties file in the folder projects\third_party.

JUNIT TESTS
The constants for the connection to a FileNET repository are stored in the class FnConnection in javatests.
Modify this class with your connection datas

