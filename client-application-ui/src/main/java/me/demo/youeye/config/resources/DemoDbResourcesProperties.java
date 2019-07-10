package me.demo.youeye.config.resources;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oauth2.demo-db")
public class DemoDbResourcesProperties extends ClientResources {
}
