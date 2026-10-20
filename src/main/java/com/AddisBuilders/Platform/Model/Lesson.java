package com.AddisBuilders.Platform.Model;

import com.AddisBuilders.Platform.Model.enums.ContentType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lessons", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"module_id", "lesson_order"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ContentType type;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(name = "content_url", length = 1000)
    private String contentUrl;

    @Column(name = "lesson_order", nullable = false)
    private Integer lessonOrder;
}