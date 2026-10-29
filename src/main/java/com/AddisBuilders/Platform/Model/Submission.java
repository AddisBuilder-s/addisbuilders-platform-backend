package com.AddisBuilders.Platform.Model;

import com.AddisBuilders.Platform.Model.enums.SubmissionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;

    @Column(name = "text_answer", columnDefinition = "TEXT")
    private String textAnswer;

    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;

    @Column(precision = 5, scale = 2)
    private BigDecimal grade;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @CreationTimestamp
    @Column(name = "submitted_at", updatable = false)
    private LocalDateTime submittedAt;

    @Column(name = "graded_at")
    private LocalDateTime gradedAt;
}