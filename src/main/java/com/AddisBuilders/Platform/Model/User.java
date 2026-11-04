package com.AddisBuilders.Platform.Model;

import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Model.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 200)
    private String university;

    @Column(length = 200)
    private String department;

    @Column(name = "student_id", length = 100)
    private String studentId;

    @Column(name = "cv_url", length = 1000)
    private String cvUrl;

    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;

    @Column(name = "employee_id", length = 100)
    private String employeeId;

    @Column(name = "proof_document_url", length = 1000)
    private String proofDocumentUrl;

    @Column(name = "coordinator_decision_notes", columnDefinition = "TEXT")
    private String coordinatorDecisionNotes;

    @Column(name = "coordinator_decision_at")
    private LocalDateTime coordinatorDecisionAt;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "coordinator_status")
    private ApprovalStatus coordinatorStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "mentor_status")
    private ApprovalStatus mentorStatus;

    @Column(name = "mentor_decision_notes", columnDefinition = "TEXT")
    private String mentorDecisionNotes;

    @Column(name = "mentor_decision_at")
    private LocalDateTime mentorDecisionAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- Spring Security UserDetails Contract Implementation ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Automatically prefix the role with "ROLE_" so Spring Security's hasRole() works out of the box
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return this.passwordHash; // Maps password field for authentication checks
    }

    @Override
    public String getUsername() {
        return this.email; // We use email as the unique login identifier
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.status != UserStatus.SUSPENDED; // Account is locked if status is SUSPENDED
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.status == UserStatus.ACTIVE; // Account must be ACTIVE to log in
    }
}
