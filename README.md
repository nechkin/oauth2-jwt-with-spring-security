## Basic overview of OAuth2 flow

#### Authorization code grant type

1. The user (Resource Owner) requests a resource or site login from the application (Client Application).
2. The site sees that the user is not authenticated. It formulates a request for the identity provider (Authorization
 Server), encodes it, and sends it to the user as part of a redirect URL.
3. The user's browser requests the redirect URL for the identity provider, including the application's request 
([authorization code grant](https://tools.ietf.org/html/rfc6749#section-4))
4. If necessary, the identity provider authenticates the user (perhaps by asking them for their username and password)
5. Once the identity provider is satisfied that the user is sufficiently authenticated, it processes the application's 
request, formulates a response, and sends that back to the user along with a redirect URL back to the application.
6. The user's browser requests the redirect URL that goes back to the application, including the identity provider's 
response
7. The application decodes the identity provider's response, and carries on accordingly.
8. The identity provider's response includes an access token which the application can use to gain direct access to 
the identity provider's services on the user's behalf.

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
can request the public key from the Authorization ServerTo or have it stored statically. To export public key from the 
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

## readme TODOs:

about schema and data
(check out Spring's JdbcClientDetailsService, it uses the schema...)
Spring Boot can automatically create the schema (DDL scripts) of your DataSource and initialize it (DML scripts). It loads SQL from the standard root classpath locations: schema.sql and data.sql


curl db_client:db_client_password@localhost:8082/idp-db/oauth/token -d grant_type=password -d username=john -d password=secret
{"access_token":"eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiaWRwLWRiLXJlc291cmNlIl0sInVzZXJfbmFtZV9jbGFpbSI6ImpvaG4iLCJ1c2VyX25hbWUiOiJqb2huIiwic2NvcGUiOlsicmVhZCIsIndyaXRlIl0sImN1c3RvbV9qd3RfY2xhaW0iOiJteSBjdXN0b20gdXNlciBkYXRhIiwiZXhwIjoxNTYzOTA4Mjc3LCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiXSwianRpIjoiYzZhYzM3MzUtMjFlYy00ZjU2LTk5ODAtMzY0MmIwYWU0ZTE5IiwiY2xpZW50X2lkIjoiZGJfY2xpZW50In0.vm9ILXjnMHmyzkr8dd5C6Ec6W-OVSga3SO31D9g690kAXt1jht3RI3DKGIf-EDzDwfvPqsWhTCfoCSPkx8LMdLDDgx8F9GG_wFI6vs6CmA8JiqRZBAUTCQYs9-qgN5iNmsmoMw_-SVZVMTkzUH8v2E2TbZpB3DoirQGYbsrmCGTFTP0veYDlP0NLMm2vO3-bx1udpdN54FJw1pJ_frluUsLN2Uh68Hm0SHM-fLzVf70RyKIcbk44qCL7bfxcOClZJYgsmCEOE8jH4w6o838rMWYNhIh46lRl6fsWs5wbRhTWtsEH1EN_CWqsOyWeDDTcsbGiQE9BladBYX43ePyx6w","token_type":"bearer","expires_in":3599,"scope":"read write","jti":"c6ac3735-21ec-4f56-9980-3642b0ae4e19"}

curl -X GET -H 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiaWRwLWRiLXJlc291cmNlIiwicmVzb3VyY2Utand0LWdyZWV0aW5nIl0sInVzZXJfbmFtZSI6ImpvaG4iLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXSwiY3VzdG9tX2p3dF9jbGFpbSI6ImpvaG4ncyBjdXN0b20gaW5mbyIsImV4cCI6MTU2NDI1MTk4MCwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6IjA5OTBhZDU1LWQ5NjYtNDQ3Zi1hNzQxLTA0ZDMwMjQ1ZDlkMiIsImNsaWVudF9pZCI6ImRiX2NsaWVudCJ9.eAzn7MA7lZe2buyS0JORpjDeo2-DNRmC1zRKA1lcZq3nUHep4pykU5YIKjhycaYTRK7FwP1vonSY2OPjVt6n1HlwpFIJ1HvP-ESncqhZ8ZGIZHK8j9R84I2nADjrqjWufYweiOSGpY1p1KXBAc6YsQrvEDrsP-DCSe2lrWHQ3xbBefcIpyPV3lxpNTuVtIDyHMI3c88zlzHs0cv5H3eELORa5ak9DJJLoR1vLtmkYHsqejsEdzcrgU6xfYada4-QeRsNybodg9xrSiaktXhtRCoIxe84f7xHsn_sOoCyZreo4MPbLRMMWD5wBhSC-eTQRsmNpXSybFyh4ofUNPralQ' localhost:8083/res-greet/hello

curl -X GET -H 'Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiaWRwLWRiLXJlc291cmNlIiwicmVzb3VyY2Utand0LWdyZWV0aW5nIl0sInVzZXJfbmFtZSI6ImpvaG4iLCJzY29wZSI6WyJyZWFkIiwid3JpdGUiXSwiY3VzdG9tX2p3dF9jbGFpbSI6ImpvaG4ncyBjdXN0b20gaW5mbyIsImV4cCI6MTU2NDI1MTk4MCwiYXV0aG9yaXRpZXMiOlsiUk9MRV9VU0VSIl0sImp0aSI6IjA5OTBhZDU1LWQ5NjYtNDQ3Zi1hNzQxLTA0ZDMwMjQ1ZDlkMiIsImNsaWVudF9pZCI6ImRiX2NsaWVudCJ9.eAzn7MA7lZe2buyS0JORpjDeo2-DNRmC1zRKA1lcZq3nUHep4pykU5YIKjhycaYTRK7FwP1vonSY2OPjVt6n1HlwpFIJ1HvP-ESncqhZ8ZGIZHK8j9R84I2nADjrqjWufYweiOSGpY1p1KXBAc6YsQrvEDrsP-DCSe2lrWHQ3xbBefcIpyPV3lxpNTuVtIDyHMI3c88zlzHs0cv5H3eELORa5ak9DJJLoR1vLtmkYHsqejsEdzcrgU6xfYada4-QeRsNybodg9xrSiaktXhtRCoIxe84f7xHsn_sOoCyZreo4MPbLRMMWD5wBhSC-eTQRsmNpXSybFyh4ofUNPralQ' localhost:8084/res-echo/echo/test

## Links

* [Wikipedia on OAuth](https://en.wikipedia.org/wiki/OAuth)
* [Spring boot tutorial](https://spring.io/guides/tutorials/spring-boot-oauth2/#_social_login_authserver)
* [Spring OAuth developers guild](https://projects.spring.io/spring-security-oauth/docs/oauth2.html)
* [Example SQL init script for use with JdbcClientDetailsService](
https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/test/resources/schema.sql)
* [Spring Security OAuth github](https://github.com/spring-projects/spring-security-oauth)
* [Spring Security OAuth samples](https://github.com/spring-projects/spring-security-oauth/tree/master/samples)
