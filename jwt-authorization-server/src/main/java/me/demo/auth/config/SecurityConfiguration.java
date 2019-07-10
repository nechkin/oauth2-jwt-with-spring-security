package me.demo.auth.config;

import lombok.RequiredArgsConstructor;
import me.demo.auth.user.JwtUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;

    private final JwtUserDetailsService jwtUserDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http.antMatcher("/**")
                .authorizeRequests()
                    .antMatchers( "/webjars/**").permitAll()
                    // among other paths, securing /oauth/authorize (user must be logged in before asking for auth code)
                    .anyRequest().authenticated()
                    .and()
                .formLogin();
        // @formatter:on
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jwtUserDetailsService)
                .passwordEncoder(passwordEncoder);
                // .inMemoryAuthentication()
                // .withUser("john").password(passwordEncoder.encode("secret")).roles("USER");
    }

    /**
     * Expose authenticationManagerBean.<br/>
     * Note that this AuthenticationManager won't be picked up by AuthorizationServerSecurityConfiguration to
     * authorize Oauth2 Clients.<br/>
     * See AuthorizationServerSecurityConfiguration#configure(ClientDetailsServiceConfigurer clientDetails) - it's
     * overridden, so that "local" AuthenticationManager will be built for /oauth endpoints. They are secured with
     * Basic authorization where Client Application credentials should be supplied<br/>
     * This one is used by this app's web security and for oauth2 "password" grant type. So when client credentials
     * are authorized, the token can be got on behalf of the user from this authentication manager (username and
     * password are supplied as POST params for the token request)
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
