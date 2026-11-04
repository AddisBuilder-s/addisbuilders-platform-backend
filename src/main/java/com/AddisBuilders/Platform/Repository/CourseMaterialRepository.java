package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {
    List<CourseMaterial> findByCourseIdOrderBySortOrderAscIdAsc(Long courseId);
    void deleteByCourseId(Long courseId);
}
