package com.github.dimitryivaniuta.videometadata.domain.entity;

import com.github.dimitryivaniuta.videometadata.domain.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Persistent user of the Video Metadata system.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    /**
     * Database‐sequence‐backed primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VM_UNIQUE_ID")
    @SequenceGenerator(
            name            = "VM_UNIQUE_ID",    // matches generator in @GeneratedValue
            sequenceName    = "VM_UNIQUE_ID",    // matches your actual DB sequence
            allocationSize  = 1                  // must match INCREMENT BY 1
    )
    private Long id;

    /**
     * Bcrypt‐hashed password.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Role granted to this user.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Unique username for login.
     */
    @Column(nullable = false, unique = true)
    private String username;
}
