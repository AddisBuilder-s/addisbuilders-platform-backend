package com.AddisBuilders.Platform.Model;

import com.AddisBuilders.Platform.Model.enums.InternshipStatus;
import com.AddisBuilders.Platform.Model.enums.WorkMode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "internship_listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InternshipListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinator_id")
    private User coordinator;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", nullable = false, length = 50)
    private WorkMode workMode;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private InternshipStatus status = InternshipStatus.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}