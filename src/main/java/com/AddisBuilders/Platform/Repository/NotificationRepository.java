package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);
    Optional<Notification> findByIdAndUserId(Long id, Long userId);
    long countByUserIdAndReadFalse(Long userId);
    long deleteByUserId(Long userId);
}
