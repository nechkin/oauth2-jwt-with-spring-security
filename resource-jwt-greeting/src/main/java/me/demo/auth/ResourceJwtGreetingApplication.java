package me.demo.auth;

import me.demo.auth.user.JwtUser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@EnableGlobalMethodSecurity(securedEnabled = true)
@RestController
@SpringBootApplication
public class ResourceJwtGreetingApplication extends WebSecurityConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(ResourceJwtGreetingApplication.class, args);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin().disable();
    }

    @RequestMapping("/oauth/hello")
    public String hello(Principal principal) {
        if (principal instanceof OAuth2Authentication) {
            OAuth2Authentication authentication = (OAuth2Authentication) principal;
            if (authentication.getPrincipal() instanceof JwtUser) {
                JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
                return "Hello, " + jwtUser.getUsername() + "! Your custom info is: " + jwtUser.getCustomInfo() + "\n";
            }
        }
        return "Hello " + (principal != null ? principal.getName() : " ") + "!\n";
    }

    @Secured("ROLE_EXTENDED")
    @RequestMapping("/oauth/helloExtended")
    public String helloExtendedRole() {
        return "Hello user with extended role\n";
    }

    @RequestMapping("/oauth/helloWriteOrExtended")
    public String helloWriteScopeOrExtendedRole() {
        return "Hello user with write scope or extended role\n";
    }
}
