package com.association.punchclock.Models;

public class User {
    private String email;
    private String token;
    private int id;
    private String association;
    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public int getId() {
        return id;
    }

    public String getAssociation() {
        return association;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAssociation(String association) {
        this.association = association;
    }
}
