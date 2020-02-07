package com.mohyehia.jndiDatasource.config;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.NamingException;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.mohyehia.jndiDatasource.dao",
        entityManagerFactoryRef = "entityManagerFactoryBean"
)
@EnableTransactionManagement
public class DataSourceConfig {

    private HibernateProperties hibernateProperties;
    private DatabaseProperties databaseProperties;

    @Autowired
    public DataSourceConfig(HibernateProperties hibernateProperties, DatabaseProperties databaseProperties) {
        this.hibernateProperties = hibernateProperties;
        this.databaseProperties = databaseProperties;
    }

    @Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory(){
        return new TomcatServletWebServerFactory(){
            @Override
            protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {
                // Enable JNDI naming which is disabled by default
                tomcat.enableNaming();
                // Factory method called to create the TomcatWebServer
                return super.getTomcatWebServer(tomcat);
            }

            @Override
            protected void postProcessContext(Context context) {
                // Post process the Tomcat Context before it's used with the Tomcat Server.
                ContextResource contextResource = new ContextResource();
                contextResource.setType(DataSource.class.getName());
                contextResource.setName(databaseProperties.getJndiName());
                contextResource.setProperty("url", databaseProperties.getUrl());
                contextResource.setProperty("username", databaseProperties.getUsername());
                contextResource.setProperty("password", databaseProperties.getPassword());

                context.getNamingResources().addResource(contextResource);
            }
        };
    }

    @Bean
    public DataSource jndiDataSource() throws NamingException {
        JndiObjectFactoryBean factoryBean = new JndiObjectFactoryBean();
        factoryBean.setJndiName("java:comp/env/" + databaseProperties.getJndiName());
        factoryBean.setProxyInterface(DataSource.class); // Specify the proxy interface to use for the JNDI object.
        factoryBean.setLookupOnStartup(false); // Set whether to look up the JNDI object on startup.
        factoryBean.afterPropertiesSet(); // Look up the JNDI object and store it.
        return (DataSource) factoryBean.getObject();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean() throws NamingException {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(jndiDataSource());
        factoryBean.setPackagesToScan("com.mohyehia.jndiDatasource.entity");
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factoryBean.setJpaProperties(hibernateProperties.getHibernateProperties());
        factoryBean.afterPropertiesSet();
        return factoryBean;
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws NamingException {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(entityManagerFactoryBean().getObject());
        return jpaTransactionManager;
    }
}
