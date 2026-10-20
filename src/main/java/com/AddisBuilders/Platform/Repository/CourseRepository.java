package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.Course;
import com.AddisBuilders.Platform.Model.enums.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByStatus(CourseStatus status);
    List<Course> findByMentorId(Long mentorId);
}