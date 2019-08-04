package me.demo.auth.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserDataRepository {

    private final Map<String, UserData> userRepository = new HashMap<>();

    public UserDataRepository(PasswordEncoder passwordEncoder) {
        userRepository.put("john", UserData.builder()
                .password(passwordEncoder.encode("secret"))
                .authorities("ROLE_USER")
                .customInfo("john's custom info")
                .build()
        );
        userRepository.put("mary", UserData.builder()
                .password(passwordEncoder.encode("s3cr3t"))
                .authorities("ROLE_USER,ROLE_EXTENDED")
                .customInfo("mary's custom info")
                .build()
        );
    }

    public Optional<UserData> getByUsername(final String username) {
        return Optional.ofNullable(userRepository.get(username));
    }
}
