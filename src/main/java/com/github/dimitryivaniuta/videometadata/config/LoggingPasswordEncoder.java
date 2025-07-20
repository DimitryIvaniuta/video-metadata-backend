package com.github.dimitryivaniuta.videometadata.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Wraps the real BCryptPasswordEncoder to log raw vs. stored passwords at DEBUG level.
 */
public class LoggingPasswordEncoder implements PasswordEncoder {
    private static final Logger log = LoggerFactory.getLogger(LoggingPasswordEncoder.class);
    private final PasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        String encoded = delegate.encode(rawPassword);
        log.debug("Encoding raw password [{}] -> hash [{}]", rawPassword, encoded);
        return encoded;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        boolean result = delegate.matches(rawPassword, encodedPassword);
        log.debug("Matching raw password [{}] against hash [{}] -> {}", rawPassword, encodedPassword, result);
        return result;
    }
}
