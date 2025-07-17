package com.github.dimitryivaniuta.videometadata.domain.entity;

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
    @SequenceGenerator(
            name         = "VM_UNIQUE_ID",
            sequenceName = "VM_UNIQUE_ID",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "VM_UNIQUE_ID")
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
