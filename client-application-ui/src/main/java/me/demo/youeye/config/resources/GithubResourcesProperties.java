package me.demo.youeye.config.resources;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("oauth2.github")
public class GithubResourcesProperties extends ClientResources {
}
