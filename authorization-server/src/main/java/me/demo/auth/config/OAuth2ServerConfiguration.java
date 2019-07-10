package me.demo.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

@EnableAuthorizationServer
@Configuration
public class OAuth2ServerConfiguration extends AuthorizationServerConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;

    public OAuth2ServerConfiguration(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.passwordEncoder(passwordEncoder); // encoder to verify client secrets
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer configurer) throws Exception {
        // @formatter:off
        configurer
                .inMemory()
                    .withClient("demo")
                    .secret(passwordEncoder.encode("demosecret"))
                    .authorizedGrantTypes("authorization_code")
                    .scopes("read", "write")
                    .authorities("USER")
                    .accessTokenValiditySeconds(120)
                    .refreshTokenValiditySeconds(60 * 60)
                    // auto approve all scopes, as if the user granted the client the requested scopes ("read", "write")
                    .autoApprove(true)
                    .redirectUris("http://localhost:8080/login/demo");
                    // .resourceIds("");
        // @formatter:on

    }
}