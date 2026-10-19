package com.AddisBuilders.Platform.Model;

import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") // Maps this entity to the 'users' table in MySQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Auto-increment primary key

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName; // Maps to full_name varchar(150)

    @Column(nullable = false, unique = true, length = 255)
    private String email; // Maps to email varchar(255), must be unique

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash; // Stores securely hashed passwords

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // LEARNER, MENTOR, COORDINATOR, EMPLOYER, SUPER_ADMIN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status; // ACTIVE, INACTIVE, SUSPENDED

    @Enumerated(EnumType.STRING)
    @Column(name = "coordinator_status")
    private ApprovalStatus coordinatorStatus; // PENDING, APPROVED, REJECTED (Specific for coordinators/employers)

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Automatically populated on record creation

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Automatically updated on record modification
}