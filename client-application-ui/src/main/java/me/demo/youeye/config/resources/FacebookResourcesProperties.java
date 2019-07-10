package me.demo.youeye.config.resources;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oauth2.facebook")
public class FacebookResourcesProperties extends ClientResources {
}
