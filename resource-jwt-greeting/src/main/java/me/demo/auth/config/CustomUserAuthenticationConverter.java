package me.demo.auth.config;

import lombok.extern.log4j.Log4j2;
import me.demo.auth.JwtUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * Converts user details part of JWT from and to authentication information
 */
@Log4j2
public class CustomUserAuthenticationConverter implements UserAuthenticationConverter {

    public static final String CUSTOM_JWT_CLAIM = "custom_jwt_claim";

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication userAuthentication) {
        // Resource server doesn't need this one
        throw new UnsupportedOperationException();
    }

    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        log.info("Authentication extracted, custom claim is: {}", map.get(CUSTOM_JWT_CLAIM));

        if (map.containsKey(USERNAME)) {
            final Collection<? extends GrantedAuthority> authorities = getAuthorities(map);
            JwtUser principal = new JwtUser((String) map.get(USERNAME),
                    "N/A",
                    authorities,
                    (String) map.get(CUSTOM_JWT_CLAIM));
            return new UsernamePasswordAuthenticationToken(principal, "N/A", authorities);
        }

        return null;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Map<String, ?> map) {
        if (!map.containsKey(AUTHORITIES)) {
            return null;
        }
        Object authorities = map.get(AUTHORITIES);
        if (authorities instanceof String) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList((String) authorities);
        }
        if (authorities instanceof Collection) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils
                    .collectionToCommaDelimitedString((Collection<?>) authorities));
        }
        throw new IllegalArgumentException("Authorities must be either a String or a Collection");
    }
}
