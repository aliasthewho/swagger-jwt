package com.example.swaggerjwtapi.service;

import com.example.swaggerjwtapi.dto.LoginRequest;
import com.example.swaggerjwtapi.dto.LoginResponse;
import com.example.swaggerjwtapi.model.AppUser;
import com.example.swaggerjwtapi.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Credentials are not valid"));

        if (!passwordEncoder.matches(request.password(), user.password())) {
            throw new BadCredentialsException("Credentials are not valid");
        }

        String token = jwtService.generateToken(user.username(),  user.roles());
        return new LoginResponse(token, "Bearer ", jwtService.getExpirationSeconds());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.withUsername(user.username())
                .password(user.password())
                .authorities(user.roles().toArray(String[]::new))
                .build();
    }
}