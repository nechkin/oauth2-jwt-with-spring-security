## Description

This is a demo project for Oauth2 capabilities with Spring Security Oauth. Some of the features used: single sign on 
with OAuth2 using authorization grant type, separating Authorization and Resource Servers, encoding user data in JWT 
tokens, using JDBC token store, password grant type. Note that plain http is used, and OAuth2 communication is not 
encrypted, including the tokens.

## Components

There is a number of standalone Spring Boot applications. A short description for the purpose of each one:

1. <b>authorization-server</b> - a simple oauth authorization server, using in memory token store. It also acts as a 
resource server to expose an endpoint with user data, this endpoint is token protected.
2. <b>jwt-authorization-server</b> - another authorization server, this one uses JdbcTokenStore. Also has an endpoint 
for user data. The token is a Json Web Tone (see https://jwt.io), It contains user data, user roles and is signed 
with asymmetric key (again, it's not encryptes, just has a signature, so the resource server can verify that the 
token was not tempered with). A custom access confirmation page is defined as an example (a page where the user 
specifies which scopes it approves for the client application) . 
3. <b>client-application-ui</b> - a minimal MVC application with a single page to login and display some info for the
 authenticated user. It uses the token acquired during authentication to access Resource Server via Oauth2RestTemplate.
4. <b>resource-jwt-greeting</b> - a resource server. It uses JwtAccessTokenConverter to decode tokens, and uses 
the public part of the verification key from the authorization server to verify tokens. The public key is requested 
from the authorization server /oauth/token_key endpoint on startup. 
5. <b>resource-jwt-echo</b> - a resource server. It uses authorization server /oauth/check_token endpoint to decode 
tokens (verification is done on). Every request with the token result in a verification request to the authorization 
server. 


## Run the app

Build it: 

`./gradlew assemble`

Run postgre for the JBDC token store:

```
cd docker/postgre
docker-compose up -d
```

Run it:
```
./gradlew authorization-server:bootRun
./gradlew client-application-ui:bootRun
./gradlew jwt-authorization-server:bootRun
./gradlew resource-jwt-echo:bootRun
./gradlew resource-jwt-greeting:bootRun
```

## Basic overview of OAuth2 flows

#### Authorization code grant type

1. The user (Resource Owner) requests a resource or site login from the application (Client Application).
2. The site sees that the user is not authenticated. It formulates a request for the identity provider (Authorization
 Server), encodes it, and sends it to the user as part of a redirect URL.
3. The user's browser requests the redirect URL for the identity provider, including the application's request 
([authorization code grant](https://tools.ietf.org/html/rfc6749#section-4))
4. If necessary, the identity provider authenticates the user (perhaps by asking them for their username and password)
5. Once the identity provider is satisfied that the user is sufficiently authenticated, it processes the application's 
request, formulates a response, and sends that back to the user along with a redirect URL back to the application. 
The redirect URL contains the authorization code.
6. The user's browser requests the redirect URL that goes back to the application, including the identity provider's 
authorization code.
7. Application handles the request and uses the authorization code to request an access token from the Authorization 
Server.
8. The application decodes the identity provider's response, and authenticates the user. User data can either be 
decoded from the token or requested from the appropriate resource using the access token.
9. Application can use an access token to gain direct access to the identity provider's services (or other token 
protected resources) on the user's behalf.

Client Application can now use the token to access the Resource Server.

#### Password grant type

1. Client Application requests the token from the Authorization Server, supplying client credentials via e.g. basic 
authorization, and user credentials via POST parameters (see examples below) 
2. Authorization Server authenticates the client and user, and if password grant type is allowed returns the 
token.
3. Client Application uses the token to access the Resource Server

## Technologies used

* Java 11
* Gradle
* Spring Boot
* Spring Security OAuth
* Json Web Tokens
* JPA

## RSA keystore

Asymmetric key to sign and verify JWT. Create a keystore:

`keytool -genkey -keyalg RSA -alias jwt-key-pair -keystore jwt-keystore.jks -storepass storepass`

or

`keytool -genkeypair -alias jwt-key-pair -keyalg RSA -dname "CN=void,OU=void,O=void,L=void,C=void" -keystore 
jwt-keystore.jks -storepass storepass`

Public key is required by the Resource Servers to verify the token (check that it was not tampered). Resource server 
can request the public key from the Authorization Server or have it stored statically. To export public key from the 
keystore:

`keytool -export -alias jwt-key-pair -keystore jwt-keystore.jks -storepass storepass | openssl x509 -inform der -pubkey -noout`

or

`keytool -list -rfc --keystore jwt-keystore.jks -storepass storepass | openssl x509 -inform pem -pubkey`

The public key for "me/demo/auth/config/jwt-keystore.jks":
```
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0pVCrZEs05WdRPRkqjGb
B1lMi4mvTuaQm+jCbtP9pC1sNrwaznsIrtdhhdGZgVN4Q2nCYP1se9kBnUwfiD7F
pX8Ahsrff3Cpl1sA7aj0l9KLq2eQWpodJ94OohevXSFpzI44811ClEawjYpScULi
BvNo7/ze5bdLx3xuX0taDskkiZdtTDmBIt0UQ6u7lAO6iLTEM3bcv03+wh5RYZX4
Fx21SiMl7nKf58vwHPAoG+nIKipdxQp/lMy/aXHdWOKi/MTeH1ziIy6Ze1uBBQgY
0ANyZ0wr3UVm2y0L2mfq77rHXykGj4tlfSf50+KiP46KYNSEDFxaiMf1rwp50hzA
4QIDAQAB
-----END PUBLIC KEY-----
```

To view private key in the keystore (just in case): 
```
keytool -importkeystore \
    -srckeystore jwt-keystore.jks \
    -destkeystore keystore.p12 \
    -deststoretype PKCS12 \
    -srcalias jwt-key-pair \
    -deststorepass storepass \
    -destkeypass storepass

openssl pkcs12 -in keystore.p12  -nokeys -out cert.pem

openssl pkcs12 -in keystore.p12  -nodes -nocerts -out key.pem
```

## Note on JDBC token store:

Spring Boot can automatically create the schema (DDL scripts) of your DataSource and initialize it (DML scripts). 
It loads SQL from the standard root classpath locations: schema.sql and data.sql. (see JdbcClientDetailsService)

Sot the schema and data for oauth jdbc store can be found the mentioned file for the <b>jwt-authorization-server</b> 
application

## Example requests to use the password grant type

Obtaine a token:
```
curl db_client:db_client_password@localhost:8082/idp-db/oauth/token -d grant_type=password -d username=john -d password=secret
{"access_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiaWRwLWRiLXJlc291cmNlIl0sInVzZXJfbmFtZV9jbGFpbSI6ImpvaG4iLCJ1c2VyX25hbWUiOiJqb2huIiwic2NvcGUiOlsicmVhZCIsIndyaXRlIl0sImN1c3RvbV9qd3RfY2xhaW0iOiJteSBjdXN0b20gdXNlciBkYXRhIiwiZXhwIjoxNTYzOTA4Mjc3LCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiYzZhYzM3MzUtMjFlYy00ZjU2LTk5ODAtMzY0MmIwYWU0ZTE5IiwiY2xpZW50X2lkIjoiZGJfY2xpZW50In0.vm9ILXjnMHmyzkr8dd5C6Ec6W-OVSga3SO31D9g690kAXt1jht3RI3DKGIf-EDzDwfvPqsWhTCfoCSPkx8LMdLDDgx8F9GG_wFI6vs6CmA8JiqRZBAUTCQYs9-qgN5iNmsmoMw_-SVZVMTkzUH8v2E2TbZpB3DoirQGYbsrmCGTFTP0veYDlP0NLMm2vO3-bx1udpdN54FJw1pJ_frluUsLN2Uh68Hm0SHM-fLzVf70RyKIcbk44qCL7bfxcOClZJYgsmCEOE8jH4w6o838rMWYNhIh46lRl6fsWs5wbRhTWtsEH1EN_CWqsOyWeDDTcsbGiQE9BladBYX43ePyx6w","token_type":"bearer","expires_in":3599,"scope":"read write","jti":"c6ac3735-21ec-4f56-9980-3642b0ae4e19"}
```

Use the token to access the resource:
```
curl -X GET -H 'Authorization: Bearer <token>' localhost:8083/oauth/res-greet/hello
```

## Links

* [Wikipedia on OAuth](https://en.wikipedia.org/wiki/OAuth)
* [Spring boot tutorial](https://spring.io/guides/tutorials/spring-boot-oauth2/#_social_login_authserver)
* [Spring OAuth developers guild](https://projects.spring.io/spring-security-oauth/docs/oauth2.html)
* [Example SQL init script for use with JdbcClientDetailsService](
https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/test/resources/schema.sql)
* [Spring Security OAuth github](https://github.com/spring-projects/spring-security-oauth)
* [Spring Security OAuth samples](https://github.com/spring-projects/spring-security-oauth/tree/master/samples)
