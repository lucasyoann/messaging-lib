package com.marineyoann.messaging.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableConfigurationProperties(MessagingProperties.class)
@ComponentScan(basePackages = "com.marineyoann.messaging")
@EntityScan(basePackages = "com.marineyoann.messaging.domain")
@EnableJpaRepositories(basePackages = "com.marineyoann.messaging.repository")
public class MessagingLibAutoConfiguration {
}
