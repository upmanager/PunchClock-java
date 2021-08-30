package com.association.punchclock.Models;

public class User {
    private String username;
    private String token;
    private int id;

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public int getId() {
        return id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setId(int id) {
        this.id = id;
    }
}
