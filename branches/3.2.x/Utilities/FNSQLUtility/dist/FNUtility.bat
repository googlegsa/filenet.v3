Set INSTALL_PATH=C:\FNUtility
cd %INSTALL_PATH%
set JDK_HOME="C:\Program Files\Java\jdk1.5.0_16\"
set path="%JDK_HOME%";%PATH%
set CLASSPATH=.\FNUtility.jar;.\lib\Jace.jar;.\lib\log4j.1.2.15.jar;.\lib\wasp.jar;.\lib\wsdl_api.jar;.\lib\saaj.jar;.\lib\activation.jar;.\lib\jaxrpc.jar;.\lib\javaapi.jar;.\lib\builtin_serialization.jar;.\lib\jetty.jar
cls
%JDK_HOME%\bin\java -cp %CLASSPATH% com.google.enterprise.connector.filenet.utility.ui.FileNetUtilFrontEnd