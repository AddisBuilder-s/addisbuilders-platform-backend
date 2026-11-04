package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.NotificationResponse;
import com.AddisBuilders.Platform.Model.Notification;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;

    @Transactional
    public void notify(User recipient, String title, String message, String type, String link) {
        repository.save(Notification.builder().user(recipient).title(title).message(message)
                .type(type == null ? "INFO" : type).link(link).build());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(User user, boolean unreadOnly) {
        List<Notification> items = unreadOnly
                ? repository.findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId())
                : repository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return items.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(User user) {
        return repository.countByUserIdAndReadFalse(user.getId());
    }

    @Transactional
    public void delete(Long id, User user) {
        Notification notification = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Notification does not belong to this account"));
        repository.delete(notification);
    }

    @Transactional
    public void clear(User user) {
        repository.deleteByUserId(user.getId());
    }

    @Transactional
    public void markRead(Long id, User user) {
        Notification notification = repository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Notification does not belong to this account"));
        notification.setRead(true);
    }

    @Transactional
    public void markAllRead(User user) {
        repository.findByUserIdOrderByCreatedAtDesc(user.getId()).forEach(item -> item.setRead(true));
    }

    private NotificationResponse toResponse(Notification item) {
        return NotificationResponse.builder().id(item.getId()).userId(item.getUser().getId())
                .title(item.getTitle()).message(item.getMessage()).type(item.getType()).link(item.getLink())
                .isRead(item.isRead()).createdAt(item.getCreatedAt()).build();
    }
}
