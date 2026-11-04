package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByModuleIdOrderByLessonOrderAsc(Long moduleId);

    Optional<Lesson> findByIdAndModuleId(Long id, Long moduleId);

    boolean existsByModuleIdAndLessonOrder(Long moduleId, Integer lessonOrder);

    boolean existsByModuleIdAndLessonOrderAndIdNot(Long moduleId, Integer lessonOrder, Long id);

    long countByModuleId(Long moduleId);
    List<Lesson> findByContentUrlContaining(String value);
}
