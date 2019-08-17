package me.demo.youeye;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// @RunWith(SpringRunner.class)
// @SpringBootTest
public class OauthClientTest {

    // @Autowired
    // private OAuth2RestTemplate greetRestTemplate;

    // @Test
    // public void test() {
    //     greetRestTemplate.exchange("http://localhost:8083/res-greet/oauth/hello",
    //             HttpMethod.GET,
    //             new HttpEntity<>(null, new HttpHeaders()),
    //             Void.class);
    // }

    /**
     * Manual test to try out the authentication flow (UI applicaion at localhost:8080 and AuthServer at localhost:8082
     * should be running)
     */
    @Test
    public void testClient() throws IOException {
        HttpClient client = HttpClientBuilder.create()
                .disableRedirectHandling()
                // .disableCookieManagement()
                .build();
        CookieHandler.setDefault(new CookieManager());

        String url;
        HttpResponse response;

        //
        // Client app login request
        //

        url = "http://localhost:8080/login/demo-db";
        response = client.execute(new HttpGet(url));

        //
        // Should redirect to AuthServer oauth authorize url
        //

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 302);

        // Using cookieManager to manage cookies
        // XSRF-TOKEN and JSESSIOID cookies
        // Stream<String> clientAppCookies = Arrays.stream(response.getHeaders("Set-Cookie"))
        //         .map(NameValuePair::getValue);

        url = response.getFirstHeader("Location").getValue();
        response = client.execute(new HttpGet(url));

        //
        // Should redirect to AuthServer login, since user is not logged in and oauth authorize url is cookie protected
        //

        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 302);

        url = response.getFirstHeader("Location").getValue();
        response = client.execute(new HttpGet(url));

        //
        // Login with AuthServer login form
        //

        String loginPage = readContent(response.getEntity().getContent());

        HttpPost post = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", "john"));
        params.add(new BasicNameValuePair("password", "secret"));
        params.add(new BasicNameValuePair("_csrf", getCsrfToken(loginPage)));
        post.setEntity(new UrlEncodedFormEntity(params));

        response = client.execute(post);

        //
        // After successful login, AuthServer redirects to the initial request uri, i.e. authorize url, with
        // authorized cookie this time
        //

        url = response.getFirstHeader("Location").getValue();
        response = client.execute(new HttpGet(url));

        //
        // AuthServer forwards to the scope confirmation page
        //

        String scopeConfirmationPage = readContent(response.getEntity().getContent());

        URL authServerUrl = new URL(url);
        String authServerUrlStr =
                authServerUrl.getProtocol() + "://" + authServerUrl.getHost() + ":" + authServerUrl.getPort();

        HttpPost postScopeConfirmation = new HttpPost(
                authServerUrl + getScopeConfirmationFormActionUrl(scopeConfirmationPage));
        params = new ArrayList<>();
        params.add(new BasicNameValuePair("user_oauth_approval", "true"));
        params.add(new BasicNameValuePair("scope.read", "true"));
        params.add(new BasicNameValuePair("scope.write", "false"));
        params.add(new BasicNameValuePair("authorize", "Authorize"));
        params.add(new BasicNameValuePair("_csrf", fetchCsrfForScopeConfirm(client, authServerUrlStr)));
        postScopeConfirmation.setEntity(new UrlEncodedFormEntity(params));

        response = client.execute(postScopeConfirmation);

        //
        // Redirect back to the Client App, authorization code should be the the redirect now
        //

        url = response.getFirstHeader("Location").getValue();
        Assertions.assertTrue(url.contains("code="));
        response = client.execute(new HttpGet(url));

        //
        // Now login is passed (auth token is received by the client app "in the background"), show the landing
        //

        url = response.getFirstHeader("Location").getValue();
        response = client.execute(new HttpGet(url));
        Assertions.assertEquals(response.getStatusLine().getStatusCode(), 200);

        //
        // Request user info (we have authorized cookie for the client app now)
        //

        Assertions.assertEquals(
                new ObjectMapper().readValue(
                        readContent(client.execute(new HttpGet("http://localhost:8080/user")).getEntity().getContent()),
                        Map.class)
                        .get("name"),
                "john");

    }

    private String readContent(InputStream inputStream) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    private String getCsrfToken(String pageContent) {
        Pattern csrfInputPattern = Pattern.compile(
                "<input.*name=\"_csrf\" .*value=\"([A-Za-z0-9-]*)\".*/>");
        Matcher matcher = csrfInputPattern.matcher(pageContent);
        Assertions.assertTrue(matcher.find());
        return matcher.group(1);
    }

    private String getScopeConfirmationFormActionUrl(String pageContent) {
        Pattern csrfInputPattern = Pattern.compile(
                "<form id=\"confirmationForm\" name=\"confirmationForm\" action=\"(.*)\" method=\"post\">\n");
        Matcher matcher = csrfInputPattern.matcher(pageContent);
        Assertions.assertTrue(matcher.find());
        return matcher.group(1);
    }

    private String fetchCsrfForScopeConfirm(HttpClient client, String serverUrl) throws IOException {
        // Our auth server loads data for the scope confirm page via a separate GET call
        String url = serverUrl + "/idp-db/client-confirm-info";

        HttpResponse response = client.execute(new HttpGet(url));

        Map map = new ObjectMapper().readValue(readContent(response.getEntity().getContent()), Map.class);

        return (String) map.get("csrfToken");
    }
}
