package me.demo.youeye.config;

import lombok.RequiredArgsConstructor;
import me.demo.youeye.config.resources.DemoDbResourcesProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

@RequiredArgsConstructor
@Configuration
public class ClientConfiguration {

    private final OAuth2ClientContext oauth2ClientContext;
    private final DemoDbResourcesProperties demoDbResources;

    @Bean
    public OAuth2RestTemplate greetRestTemplate() {
        OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(demoDbResources.getClient(), oauth2ClientContext) {
            /**
             * Dirty.
             * Make concurrent refresh token requests work (concurrent refresh requests may be called by index.html ->
             * YouEyeController). Only single refresh token request should be sent per client and user (can use
             * striped locks per client and user to get some concurrency)
             * see https://github.com/spring-projects/spring-security-oauth/issues/1242
             */
            @Override
            public OAuth2AccessToken getAccessToken() throws UserRedirectRequiredException {

                OAuth2AccessToken accessToken = getOAuth2ClientContext().getAccessToken();

                if (accessToken == null || accessToken.isExpired()) {
                    synchronized (ClientConfiguration.class) {
                        accessToken = getOAuth2ClientContext().getAccessToken();
                        if (accessToken == null || accessToken.isExpired()) {
                            return super.getAccessToken();
                        } else {
                            return accessToken;
                        }
                    }
                }
                return accessToken;
            }
        };

        restTemplate.setRetryBadAccessTokens(false);
        return restTemplate;
    }
}
