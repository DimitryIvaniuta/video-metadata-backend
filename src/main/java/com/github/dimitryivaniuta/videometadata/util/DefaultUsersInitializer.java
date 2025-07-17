package com.github.dimitryivaniuta.videometadata.util;

import com.github.dimitryivaniuta.videometadata.domain.entity.Role;
import com.github.dimitryivaniuta.videometadata.domain.entity.User;
import com.github.dimitryivaniuta.videometadata.persistence.repository.UserRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Inserts hard‑coded default users (admin/user) on first startup,
 * if the users table is empty.
 */
@Configuration
public class DefaultUsersInitializer {

    @Bean
    public ApplicationRunner initializer(
            UserRepository userRepository,
            PasswordEncoder     passwordEncoder
    ) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("adminpass"));
                admin.setRole(Role.ADMIN);
                userRepository.save(admin);

                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("userpass"));
                user.setRole(Role.USER);
                userRepository.save(user);
            }
        };
    }
}
