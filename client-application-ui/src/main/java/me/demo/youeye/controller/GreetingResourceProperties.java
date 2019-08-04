package me.demo.youeye.controller;

import lombok.Getter;
import lombok.Setter;
import me.demo.youeye.config.resources.ClientResources;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties("resource.greeting")
public class GreetingResourceProperties extends ClientResources {
    private String helloUrl;
    private String helloExtendedUrl;
    private String helloWriteOrExtendedUrl;
}
