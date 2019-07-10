package me.demo.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This app is an Identity Provider having both Authorization Server and Resource Server endpoints.
 * So this is the Resource Server part.
 */
@EnableResourceServer
@RestController
@Configuration
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    // ResourceServer has it's own authenticationManager (see ResourceServerSecurityConfigurer)

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        // same resource id is setup in data.sql for clients
        resources.resourceId("idp-db-resource").stateless(true);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // Main user-facing security config has order 100, resource server security has order 3, so /me is protected
        // with resource filter chain, and won't be available for the requests with main security cookie.
        http.antMatcher("/me")
                .authorizeRequests().anyRequest().authenticated();
        // .antMatcher("/me").access("#oauth2.hasScope('read')")
    }

    /**
     * Endpoint to supply user info. It is secured with access tokens granted locally by this app.
     *
     * @param principal authenticated user for session identified with the Bearer header (OAuth2AuthenticationManager
     *                  should pick up the Bearer header)
     * @return map with principal name under the key 'name'
     */
    @RequestMapping("/me")
    public Map<String, String> user(Principal principal) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("name", principal.getName());
        return map;
    }
}
