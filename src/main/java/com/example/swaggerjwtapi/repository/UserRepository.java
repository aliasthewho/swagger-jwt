package com.example.swaggerjwtapi.repository;

import com.example.swaggerjwtapi.model.AppUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {

    private final Map<String, AppUser> users = new ConcurrentHashMap<>();

    public UserRepository(PasswordEncoder passwordEncoder) {
        users.put(
                "admin",
                new AppUser(
                        1L,
                        "admin",
                        passwordEncoder.encode("12345"),
                        List.of("ROLE_ADMIN")
                )
        );

        users.put(
                "user",
                new AppUser(
                        2L,
                "user",
                    passwordEncoder.encode("12345"),
                    List.of("ROLE_USER")
                )
        );

    }

    public Optional<AppUser> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }
}