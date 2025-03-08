package com.flashdash.activity.util;

import org.springframework.stereotype.Component;

@Component
public class UserContext {
    private static final ThreadLocal<String> userFrnHolder = new ThreadLocal<>();

    public void setUserFrn(String userFrn) {
        userFrnHolder.set(userFrn);
    }

    public String getUserFrn() {
        return userFrnHolder.get();
    }

    public void clear() {
        userFrnHolder.remove();
    }
}
