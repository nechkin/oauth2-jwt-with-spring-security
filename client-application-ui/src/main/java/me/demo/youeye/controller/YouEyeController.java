package me.demo.youeye.controller;

import lombok.RequiredArgsConstructor;
import me.demo.youeye.config.resources.DemoDbResourcesProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class YouEyeController {

    private final OAuth2RestTemplate oAuth2RestTemplate;
    private final DemoDbResourcesProperties demoDbResources;
    private final GreetingResourceProperties greetingResourceProperties;

    /**
     * /user endpoint, it is secured with cookies created when the user authenticates
     *
     * @param principal authenticated user for session identified by cookie
     * @return map with principal name under the key 'name'
     */
    @RequestMapping("/user")
    public Map<String, String> user(Principal principal) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("name", principal.getName());
        return map;
    }

    @RequestMapping("/hello")
    public String hello(Principal principal) {
        return getFromGreetingsResource(principal, greetingResourceProperties.getHelloUrl());
    }

    @RequestMapping("/helloExtended")
    public String helloExtended(Principal principal) {
        return getFromGreetingsResource(principal, greetingResourceProperties.getHelloExtendedUrl());
    }

    @RequestMapping("/helloWriteOrExtended")
    public String helloWriteOrExtended(Principal principal) {
        return getFromGreetingsResource(principal, greetingResourceProperties.getHelloWriteOrExtendedUrl());
    }

    private String getFromGreetingsResource(Principal principal, String url) {
        if (isAuthenticatedWithDbClient(principal)) {
            try {
                return oAuth2RestTemplate.getForObject(url, String.class);
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        return "Invalid client application";
    }

    private boolean isAuthenticatedWithDbClient(Principal principal) {
        if (principal instanceof OAuth2Authentication) {
            OAuth2Authentication authentication = (OAuth2Authentication) principal;
            return demoDbResources.getClient().getClientId().equals(authentication.getOAuth2Request().getClientId());
        }
        return false;
    }
}
