package com.planet.system.authByShiro.customSettings;

import lombok.Data;
import org.apache.shiro.authc.UsernamePasswordToken;
@Data
public class CustomUserToken extends UsernamePasswordToken {
    private Long userId;

    public CustomUserToken(String username, String password) {
        super(username, password);
    }
}
