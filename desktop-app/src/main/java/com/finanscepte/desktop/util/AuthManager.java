package com.finanscepte.desktop.util;

public class AuthManager {

    private static AuthManager instance;
    private String jwtToken;

    private AuthManager() {}

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public void login(String token) {
        this.jwtToken = token;
        ApiClient.setAuthToken(token);
    }

    public void logout() {
        this.jwtToken = null;
        ApiClient.setAuthToken(null);
    }

    public boolean isAuthenticated() {
        return jwtToken != null && !jwtToken.isEmpty();
    }

    public String getToken() {
        return jwtToken;
    }
}
