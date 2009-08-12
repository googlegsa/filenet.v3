package com.google.enterprise.connector.file;

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer; 
import org.springframework.beans.factory.support.DefaultListableBeanFactory; 
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader; 
import org.springframework.core.io.ClassPathResource; 
import org.springframework.core.io.Resource; 

import com.google.enterprise.connector.spi.Connector;
import com.google.enterprise.connector.spi.ConnectorFactory;
import com.google.enterprise.connector.spi.RepositoryException;

public class FileNetConnectorFactory implements ConnectorFactory{      
	public static final String CONNECTOR_INSTANCE_XML = "config/connectorInstance.xml";     
	public static final String CONNECTOR_DEFAULTS_XML = "config/connectorDefaults.xml";     

	/**    
	 * Creates a connector instance by loading the bean definitions and creating      
	 * the connector bean instance using Spring      *      
	 * 
	 *  @see com.google.enterprise.connector.spi.ConnectorFactory#makeConnector(java.util.Map)      
	 **/ 

//	@Override     
	public Connector makeConnector(Map<String, String> config)	throws RepositoryException {         
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();         
		XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(factory);         
		Resource connectorInstanceConfig = new ClassPathResource(CONNECTOR_DEFAULTS_XML);
		Resource connectorDefaultsConfig = new ClassPathResource(CONNECTOR_INSTANCE_XML);
		beanReader.loadBeanDefinitions(connectorInstanceConfig);         
		beanReader.loadBeanDefinitions(connectorDefaultsConfig);          
		Properties props = new Properties();         
		props.putAll(config);          
		PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();         
		configurer.setProperties(props);         
		configurer.postProcessBeanFactory(factory);          
		return (Connector) factory.getBean("Filenet_P8");       
	} 
}
