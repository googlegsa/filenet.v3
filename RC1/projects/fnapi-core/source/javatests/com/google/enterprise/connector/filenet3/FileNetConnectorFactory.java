//Copyright 2009 Google Inc.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.filenet3;

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

/**
 * The FileNet connector factory for testing purposes. It basically creates the
 * connector instance required for unit tests
 * <p>
 * The connectorInstance.xml and connectorDefaults.xml is expected to be in the
 * config folder under tests. Uses the {@link XmlBeanDefinitionReader} for
 * loading the FileNet connector bean definition from the configuration files
 * </p>
 *
 * @author rakeshs
 *
 */
public class FileNetConnectorFactory implements ConnectorFactory {

    public static final String CONNECTOR_INSTANCE_XML = "config/connectorInstance.xml";
    public static final String CONNECTOR_DEFAULTS_XML = "config/connectorDefaults.xml";

    /**
     * Creates a connector instance by loading the bean definitions and creating
     * the connector bean instance using Spring
     *
     * @see com.google.enterprise.connector.spi.ConnectorFactory#makeConnector(java
     *      .util.Map)
     */
//    @Override
    public Connector makeConnector(Map<String, String> config)
            throws RepositoryException {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader beanReader = new XmlBeanDefinitionReader(
                factory);
        Resource connectorInstanceConfig = new ClassPathResource(
                CONNECTOR_DEFAULTS_XML);
        Resource connectorDefaultsConfig = new ClassPathResource(
                CONNECTOR_INSTANCE_XML);

        beanReader.loadBeanDefinitions(connectorInstanceConfig);
        beanReader.loadBeanDefinitions(connectorDefaultsConfig);

        Properties props = new Properties();
        props.putAll(config);

        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setProperties(props);
        configurer.postProcessBeanFactory(factory);

        return (Connector) factory.getBean("FileConnectorInstance");
    }

}