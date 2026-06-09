package com.example.swaggerjwtapi.model;

import java.util.List;

public record AppUser(
        Long id,
        String username,
        String password,
        List<String> roles
) {
    public AppUser(String username, String password, List<String> roles) {
        this(null, username, password, roles);
    }
}