package me.demo.youeye.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import javax.servlet.Filter;

@RequiredArgsConstructor
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final Filter ssoFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement()
                .sessionFixation()
                .none();
        // @formatter:off
            // All requests are protected with Spring Security by default
        http.antMatcher("/**")
                .authorizeRequests()
                    // allow requests for these endpoints (though Spring Security filters are still run for them)
                    .antMatchers("/", "/login**", "/webjars/**", "/error/**", "/favicon.ico").permitAll()
                    // 	All other endpoints require an authenticated user
                    .anyRequest().authenticated()
                    .and()
                .logout()
                    .logoutSuccessUrl("/").permitAll()
                    .and()
                .exceptionHandling()
                    // Unauthenticated users are re-directed to "/"
                    .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/"))
                    .and()
                .csrf()
                    // Protect from CSRF. WithHttpOnlyFalse, because we need to read the Cookie on the page with JS
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .and()
                // OAuth2 filter
                .addFilterBefore(ssoFilter, BasicAuthenticationFilter.class);
        // @formatter:on
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // Turn off spring security for static resources
        web.ignoring().antMatchers("/webjars/**", "/favicon.ico");
    }
}
