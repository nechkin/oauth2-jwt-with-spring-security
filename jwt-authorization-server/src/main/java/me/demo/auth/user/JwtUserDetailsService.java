package me.demo.auth.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final UserDataRepository userDataRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserData> userDataOptional = userDataRepository.getByUsername(username);
        if (userDataOptional.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        UserData userData = userDataOptional.get();
        return new JwtUser(username,
                userData.getPassword(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(userData.getAuthorities()),
                userData.getCustomInfo());
    }
}