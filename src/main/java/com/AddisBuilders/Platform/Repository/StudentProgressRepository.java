package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.StudentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentProgressRepository extends JpaRepository<StudentProgress, Long> {

    // Find specific progress entry for a student and lesson
    Optional<StudentProgress> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    // Count completed lessons by student for a given list of lesson IDs
    long countByStudentIdAndLessonIdIn(Long studentId, List<Long> lessonIds);

    // Find all completed progress entries for a student
    List<StudentProgress> findByStudentId(Long studentId);
}