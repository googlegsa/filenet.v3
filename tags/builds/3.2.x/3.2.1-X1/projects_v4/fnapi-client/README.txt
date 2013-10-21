This readme file presents how to configure the development environment for the FileNET connector.

REQUIRED FILES
•	jaas.conf
•	Jace.jar
•	connector_spi.jar
•	connector.jar
•	connector_tests.jar
•	json.jar
•	junit.jar


CONFIGURATION
Add the jars file in the folder projects\third_party\lib.
Copy the properties file in the folder projects\third_party.

JUNIT TESTS
The constants for the connection to a FileNET repository are stored in the class FnConnection in javatests.
Modify this class with your connection datas

