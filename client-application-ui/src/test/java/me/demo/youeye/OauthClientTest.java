package me.demo.youeye;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OauthClientTest {

    @Autowired
    private OAuth2RestTemplate greetRestTemplate;

    @Test
    public void testClient() {
        // todo: test happy path sso - set cookies, catch redirect, submit form login, request redirect after login
        greetRestTemplate.exchange("http://localhost:8083/res-greet/oauth/hello",
                HttpMethod.GET,
                new HttpEntity<>(null, new HttpHeaders()),
                Void.class);
    }
}
