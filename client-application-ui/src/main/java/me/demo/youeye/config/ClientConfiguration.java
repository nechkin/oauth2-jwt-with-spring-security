package me.demo.youeye.config;

import lombok.RequiredArgsConstructor;
import me.demo.youeye.config.resources.DemoDbResourcesProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;

@RequiredArgsConstructor
@Configuration
public class ClientConfiguration {

    private final OAuth2ClientContext oauth2ClientContext;
    private final DemoDbResourcesProperties demoDbResources;

    @Bean
    public OAuth2RestTemplate greetRestTemplate() {
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(demoDbResources.getClient(), oauth2ClientContext);
        restTemplate.setRetryBadAccessTokens(false);
        return restTemplate;
    }
}
