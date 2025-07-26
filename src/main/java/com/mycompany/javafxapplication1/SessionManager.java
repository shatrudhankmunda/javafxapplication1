package com.mycompany.javafxapplication1;
public class SessionManager {
    private static SessionManager instance;
    private String currentUser;
    private long lastActivityTime;
    private String password;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(String user, String password) {
        this.currentUser = user;
        this.password = password;
        updateActivity();
    }

    public void logout() {
        currentUser = null;
        password = null;
        lastActivityTime = 0;
    }

    public String getCurrentUser() {
        return currentUser;
    }
    public String getPassword() {
        return password;
    }

    public void updateActivity() {
        lastActivityTime = System.currentTimeMillis();
    }

    public boolean isSessionActive(long timeoutMillis) {
        return (System.currentTimeMillis() - lastActivityTime) < timeoutMillis;
    }
}
