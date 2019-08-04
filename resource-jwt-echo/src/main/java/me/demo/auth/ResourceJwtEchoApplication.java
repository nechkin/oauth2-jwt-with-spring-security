package me.demo.auth;

import me.demo.auth.user.JwtUser;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@SpringBootApplication
public class ResourceJwtEchoApplication extends WebSecurityConfigurerAdapter {

    public static void main(String[] args) {
        SpringApplication.run(ResourceJwtEchoApplication.class, args);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin().disable();
    }

    @RequestMapping("/echo/{message}")
    public String hello(Principal principal, @PathVariable("message") String message) {
        if (principal instanceof OAuth2Authentication) {
            OAuth2Authentication authentication = (OAuth2Authentication) principal;
            if (authentication.getPrincipal() instanceof JwtUser) {
                JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
                return "Hello, " + jwtUser.getUsername() + "! Your custom info is: " + jwtUser.getCustomInfo() +
                        ". You sent: " + message + "\n";
            }
        }
        return "Hello " + (principal != null ? principal.getName() : " ") + "!" + ". You sent: " + message + "\n";
    }
}
