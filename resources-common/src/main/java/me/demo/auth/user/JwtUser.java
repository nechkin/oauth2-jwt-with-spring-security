package me.demo.auth.user;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class JwtUser extends User {

    private final String customInfo;

    public JwtUser(String username,
                   String password,
                   Collection<? extends GrantedAuthority> authorities,
                   String customInfo) {
        super(username, password, authorities);
        this.customInfo = customInfo;
    }
}
