package com.sap.refapp.sf.extension.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is the config class of embeddded H2 DB
 *
 */
@Configuration
@Profile("test")
@EntityScan(basePackages = "com.sap.refapp.sf.extension")
@PropertySource("classpath:test-db.properties")
@EnableTransactionManagement
public class H2JPAConfig {

}