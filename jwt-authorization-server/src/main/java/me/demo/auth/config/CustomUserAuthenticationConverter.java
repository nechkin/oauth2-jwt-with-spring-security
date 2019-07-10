package me.demo.auth.config;

import lombok.extern.log4j.Log4j2;
import me.demo.auth.user.JwtUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;

import java.util.Map;

/**
 * Converts user details part of JWT from and to authentication information
 */
@Log4j2
public class CustomUserAuthenticationConverter extends DefaultUserAuthenticationConverter {

    private static final String CUSTOM_JWT_CLAIM = "custom_jwt_claim";

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication userAuthentication) {
        Map<String, Object> response = (Map<String, Object>) super.convertUserAuthentication(userAuthentication);

        // If we had custom user, it could be used here to extract custom info to put it into the token
        if (userAuthentication.getPrincipal() instanceof JwtUser) {
            String customInfo = ((JwtUser) userAuthentication.getPrincipal()).getCustomInfo();
            response.put(CUSTOM_JWT_CLAIM, customInfo);

        }
        return response;
    }

    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        Authentication authentication = super.extractAuthentication(map);
        log.info("Authentication extracted, custom claim is: {}", map.get(CUSTOM_JWT_CLAIM));
        return authentication;
    }
}
