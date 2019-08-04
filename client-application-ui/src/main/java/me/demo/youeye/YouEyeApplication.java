package me.demo.youeye;

import lombok.extern.log4j.Log4j2;
import me.demo.youeye.config.resources.FacebookResourcesProperties;
import me.demo.youeye.controller.GreetingResourceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@EnableConfigurationProperties(GreetingResourceProperties.class)
@Log4j2
@SpringBootApplication
public class YouEyeApplication {

    public static void main(String[] args) {
        SpringApplication.run(YouEyeApplication.class, args);
    }

    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        if (log.isInfoEnabled()) {
            Authentication auth = event.getAuthentication();
            if (auth instanceof OAuth2Authentication) {
                log.info("Authentication success, principal name [{}]", auth.getPrincipal().toString());
            } else {
                log.info("Unexpected auth object {}", auth);
            }
        }
    }
}
