package me.demo.youeye.config;

import lombok.AllArgsConstructor;
import me.demo.youeye.config.resources.ClientResources;
import me.demo.youeye.config.resources.DemoDbResourcesProperties;
import me.demo.youeye.config.resources.DemoResourcesProperties;
import me.demo.youeye.config.resources.FacebookResourcesProperties;
import me.demo.youeye.config.resources.GithubResourcesProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

@EnableConfigurationProperties({
        FacebookResourcesProperties.class,
        GithubResourcesProperties.class,
        DemoResourcesProperties.class,
        DemoDbResourcesProperties.class
})
@EnableOAuth2Client
@Configuration
@AllArgsConstructor
public class SsoConfiguration {

    private final OAuth2ClientContext oauth2ClientContext;

    private final FacebookResourcesProperties facebookResources;
    private final GithubResourcesProperties githubResources;
    private final DemoResourcesProperties demoResources;
    private final DemoDbResourcesProperties demoDbResources;

    private final ApplicationContext applicationContext;

    /**
     * Filter to perform user authentication with various oauth2 authorization servers
     */
    @Bean
    public Filter ssoFilter() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(ssoFilter(demoResources, "/login/demo"));
        filters.add(ssoFilter(facebookResources, "/login/facebook"));
        filters.add(ssoFilter(githubResources, "/login/github"));
        filters.add(ssoFilter(demoDbResources, "/login/demo-db"));
        filter.setFilters(filters);
        return filter;
    }

    private Filter ssoFilter(ClientResources client, String path) {
        OAuth2ClientAuthenticationProcessingFilter oAuth2ClientAuthenticationFilter =
                new OAuth2ClientAuthenticationProcessingFilter(path);

        // Rest template to negotiate with Authorization Server
        OAuth2RestTemplate oAuth2RestTemplate = new OAuth2RestTemplate(client.getClient(), oauth2ClientContext);
        oAuth2ClientAuthenticationFilter.setRestTemplate(oAuth2RestTemplate);

        // Resource Server setup to get user info
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(client.getResource().getUserInfoUri(),
                client.getClient().getClientId());
        tokenServices.setRestTemplate(oAuth2RestTemplate);
        oAuth2ClientAuthenticationFilter.setTokenServices(tokenServices);

        // Publish oauth lifecycle events to the applicationContext
        oAuth2ClientAuthenticationFilter.setApplicationEventPublisher(applicationContext);

        return oAuth2ClientAuthenticationFilter;
    }

    /**
     * <p>Filter to handle redirects from this App to Authorization Server.
     * Autowire available OAuth2ClientContextFilter filter, and register it with a sufficiently low order that it
     * comes before the main Spring Security filter. In this way we can use it to handle
     * redirects signaled by exceptions in authentication requests.
     * <p/>
     * <p>This filter handles UserRedirectRequiredException to determine where to redirect.<br/>
     * E.g. redirect to Facebook (as configured by oauth2.facebook.client.UserAuthorizationUri) happens after
     * exception is thrown in AuthorizationCodeAccessTokenProvider#getRedirectForAuthorization, that is called when
     * no authorization code is present (when we first go to /login/facebook). We get to getRedirectForAuthorization
     * when OAuth2ClientAuthenticationProcessingFilter uses Oauth2RestTemplate to obtain an access token.<br/>
     * </p>
     */
    @Bean
    public FilterRegistrationBean<OAuth2ClientContextFilter> oauth2ClientFilterRegistration(
            OAuth2ClientContextFilter filter) {
        FilterRegistrationBean<OAuth2ClientContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }
}
