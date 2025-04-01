package com.flashdash.activity.util;

import org.springframework.stereotype.Component;

@Component
public class UserContext {
    private static final ThreadLocal<String> userFrnHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> userEmailHolder = new ThreadLocal<>();

    public void setUserFrn(String userFrn) {
        userFrnHolder.set(userFrn);
    }

    public String getUserFrn() {
        return userFrnHolder.get();
    }

    public void setUserEmail(String userEmail) {
        userEmailHolder.set(userEmail);
    }

    public String getUserEmail() {
        return userEmailHolder.get();
    }

    public void clear() {
        userFrnHolder.remove();
        userEmailHolder.remove();
    }
}
