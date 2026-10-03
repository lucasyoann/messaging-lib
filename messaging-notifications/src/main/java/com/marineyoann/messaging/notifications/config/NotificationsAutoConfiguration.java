package com.marineyoann.messaging.notifications.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableConfigurationProperties(VapidProperties.class)
@ComponentScan(basePackages = "com.marineyoann.messaging.notifications")
@EntityScan(basePackages = "com.marineyoann.messaging.notifications.push")
@EnableJpaRepositories(basePackages = "com.marineyoann.messaging.notifications.push")
public class NotificationsAutoConfiguration {


}