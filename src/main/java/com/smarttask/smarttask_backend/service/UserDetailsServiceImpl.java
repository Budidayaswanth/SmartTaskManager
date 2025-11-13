package com.smarttask.smarttask_backend.service;

import com.smarttask.smarttask_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

/** WHY: Bridges our User entity to Spring Security's UserDetails. */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var u = userRepo.findByUsername(username)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPassword())
                .roles(u.getRole())       // USER / ADMIN
                .disabled(!u.isEnabled()) // disabled users cannot log in
                .build();
    }
}
