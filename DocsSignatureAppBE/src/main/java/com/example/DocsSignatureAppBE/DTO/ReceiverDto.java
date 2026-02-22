package com.example.DocsSignatureAppBE.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReceiverDto {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("role")
    private String role;

    @JsonProperty("color")
    private String color;

    @JsonProperty("requirePassword")
    private boolean requirePassword;

    @JsonProperty("password")
    private String password;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isRequirePassword() {
        return requirePassword;
    }

    public void setRequirePassword(boolean requirePassword) {
        this.requirePassword = requirePassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
