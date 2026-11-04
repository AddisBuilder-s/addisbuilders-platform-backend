package com.AddisBuilders.Platform.Model;

import com.AddisBuilders.Platform.Model.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "organizations") // Maps to the ORGANIZATIONS table in MySQL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Auto-increment primary key

    // Establishes a Many-to-One relationship with the User entity (Employer/Coordinator owner)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 200)
    private String name; // Organization or University department name

    @Column(length = 150)
    private String industry;

    @Column(length = 255)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description; // Detailed overview of the organization

    @Column(length = 255)
    private String website; // Official corporate or university website URL

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 40)
    private String contactPhone;

    @Column(name = "registration_number", length = 150)
    private String registrationNumber;

    @Column(name = "tax_id", length = 150)
    private String taxId;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "verification_document_name", length = 255)
    private String verificationDocumentName;

    @Column(name = "verification_document_url", length = 1000)
    private String verificationDocumentUrl;

    // Architectural field: Tracks whether Super Admin has verified this organization
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING; // PENDING, APPROVED, REJECTED

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // Record insertion timestamp

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // Record modification timestamp
}
