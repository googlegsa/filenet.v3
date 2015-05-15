To compile the connector, you to copy Jace.jar from the FileNet
Content Engine API to the third_party/lib directory.

To run the unit tests, you need to copy all of the CE API JAR files:

Jace.jar
javaapi.jar
listener.jar
log4j-1.2.14.jar
p8cjares.jar
stax-api.jar
xlxpScanner.jar
xlxpScannerUtils.jar

You will also need these files in Tomcat/webapps/connector-manager/WEB-INF/lib
in order to run the connector.
