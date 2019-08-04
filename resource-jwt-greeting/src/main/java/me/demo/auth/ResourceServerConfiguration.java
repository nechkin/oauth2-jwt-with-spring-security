package me.demo.auth;

import lombok.SneakyThrows;
import me.demo.auth.user.CustomUserAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Configuration
@EnableResourceServer
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
    // private static final String JWT_VERIFIER_PUBLIC_KEY_FILE = "me/demo/auth/jwt_verifier_public_key.txt";

    @Override
    public void configure(final ResourceServerSecurityConfigurer config) {
        config.resourceId("resource-jwt-greeting")
                .tokenServices(tokenServices())
                .stateless(true);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .requestMatchers()
                    .antMatchers("/oauth/**")
                .and()
                    .authorizeRequests()
                        .antMatchers("/oauth/hello").access("#oauth2.hasScope('read')")
                        .antMatchers("/oauth/helloWriteOrExtended")
                            .access("#oauth2.hasScope('write') or hasRole('ROLE_EXTENDED')");
        // @formatter:on
    }

    @Bean
    public ResourceServerTokenServices tokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        return defaultTokenServices;
    }

    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }

    @SneakyThrows
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

        // Resource resource = new ClassPathResource(JWT_VERIFIER_PUBLIC_KEY_FILE);
        // String publicKey = String.join("\n", Files.readAllLines(resource.getFile().toPath()));
        RestTemplate rt = new RestTemplate();
        // Rather than storing key locally, it can be requested from the Authorization Server
        Map publicKey = rt.getForObject("http://localhost:8082/idp-db/oauth/token_key", Map.class);
        converter.setVerifierKey((String) publicKey.get("value"));

        DefaultAccessTokenConverter accessTokenConverter =
                (DefaultAccessTokenConverter) converter.getAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(new CustomUserAuthenticationConverter());

        return converter;
    }

}
