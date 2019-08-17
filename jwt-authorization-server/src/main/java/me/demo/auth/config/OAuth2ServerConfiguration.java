package me.demo.auth.config;

import me.demo.auth.user.JwtUserDetailsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.sql.DataSource;

/**
 * Using JdbcTokenStore.
 * @EnableTransactionManagement to prevent clashes between client apps competing for the same rows when tokens are
 * created. It is configured by spring boot autoconfiguration since spring data is on the classpath.
 */
@EnableAuthorizationServer
@Configuration
public class OAuth2ServerConfiguration extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final DataSource dataSource;
    private final TokenStore tokenStore;
    private final JwtAccessTokenConverter accessTokenConverter;
    private final JwtUserDetailsService jwtUserDetailsService;

    public OAuth2ServerConfiguration(
            @Qualifier("authenticationManagerBean") AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            DataSource dataSource,
            TokenStore tokenStore,
            JwtAccessTokenConverter accessTokenConverter,
            JwtUserDetailsService jwtUserDetailsService) {
        this.tokenStore = tokenStore;
        this.accessTokenConverter = accessTokenConverter;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.dataSource = dataSource;
        this.jwtUserDetailsService = jwtUserDetailsService;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security
                // see AuthorizationServerSecurityConfiguration#configure(HttpSecurity http)
                // .antMatchers(tokenKeyPath).access(configurer.getTokenKeyAccess())
                // /oauth/token_key (url to get public key for token verifier)
                .tokenKeyAccess("permitAll()")
                // /oauth/check_token (can be used by Resource Servers to decode access tokens)
                // .checkTokenAccess("isAuthenticated()")
                .checkTokenAccess("hasAuthority('ROLE_TRUSTED_CLIENT')")
                // encoder to verify client secrets
                .passwordEncoder(passwordEncoder);

                // can force HTTPs for /oauth2/token endpoint
                // .sslOnly();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer configurer) throws Exception {
        configurer.jdbc(dataSource);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints

                // The AuthenticationManager for the password grant. That is the manager for Client Applications that
                // use the "password" grant type. Not required for e.g. "authorization_code" grant type.
                // Here using the same authenticationManager as configured in the web security config of this app.
                .authenticationManager(authenticationManager)
                .tokenStore(tokenStore)
                .accessTokenConverter(accessTokenConverter)
                // For refresh tokens to work (see AuthorizationServerEndpointsConfigurer#addUserDetailsService):
                .userDetailsService(jwtUserDetailsService);

                // By default a TokenApprovalStore will be constructed backed by the same tokenStore as configured
                // for approvalStore below, so no need to supply the same approval store here, though it can be done
                // with no harm
                // .approvalStore(approvalStore)

                // custom logic for client approval (e.g. enable auto approve, if client was approved before. Client
                // is considered approved if at least one approved scope for that client is found in the approvalStore.
                // .userApprovalHandler(userApprovalHandler)
    }


    @Configuration
    protected static class JdbcTokenServicesConfiguration {


        /**
         * Configure how the tokens are stored
         */
        @Bean
        public TokenStore tokenStore(DataSource dataSource) {
            return new JdbcTokenStore(dataSource);
        }

        /**
         * Converts between token values and authentication information. Authorization server needs it to generate
         * token form the authentication information, resource server might need it to decode authentication
         * information from the token value.
         */
        @Bean
        public JwtAccessTokenConverter accessTokenConverter() {
            JwtAccessTokenConverter converter = new JwtAccessTokenConverter();

            // TODO: extract constants
            ClassPathResource resource = new ClassPathResource("me/demo/auth/config/jwt-keystore.jks");
            KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, "storepass".toCharArray());
            converter.setKeyPair(keyStoreKeyFactory.getKeyPair("jwt-key-pair"));

            UserAuthenticationConverter userTokenConverter = new CustomUserAuthenticationConverter();
            DefaultAccessTokenConverter accessTokenConverter =
                    (DefaultAccessTokenConverter) converter.getAccessTokenConverter();
            accessTokenConverter.setUserTokenConverter(userTokenConverter);

            return converter;
        }

        @Bean
        public ApprovalStore approvalStore() throws Exception {
            TokenApprovalStore store = new TokenApprovalStore();
            store.setTokenStore(tokenStore(null));
            return store;
        }
    }
}