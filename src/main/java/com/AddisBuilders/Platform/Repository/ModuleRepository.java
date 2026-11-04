package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByCourseIdOrderByModuleOrderAsc(Long courseId);

    Optional<Module> findByIdAndCourseId(Long id, Long courseId);

    boolean existsByCourseIdAndModuleOrder(Long courseId, Integer moduleOrder);

    boolean existsByCourseIdAndModuleOrderAndIdNot(Long courseId, Integer moduleOrder, Long id);

    long countByCourseId(Long courseId);
}
