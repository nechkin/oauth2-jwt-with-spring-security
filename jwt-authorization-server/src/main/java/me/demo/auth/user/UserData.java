package me.demo.auth.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserData {
    private String password;
    private String authorities;
    private String customInfo;
}
