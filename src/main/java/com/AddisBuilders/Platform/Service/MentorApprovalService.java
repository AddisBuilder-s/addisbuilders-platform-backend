package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.DTO.MentorRequestResponse;
import com.AddisBuilders.Platform.DTO.MentorStatusResponse;
import com.AddisBuilders.Platform.Exception.ResourceNotFoundException;
import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import com.AddisBuilders.Platform.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorApprovalService {
    private final UserRepository users;
    private final NotificationService notifications;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public MentorStatusResponse status(User mentor) {
        if (mentor.getRole() != UserRole.MENTOR) throw new AccessDeniedException("Mentor account required");
        return new MentorStatusResponse(mentor.getMentorStatus(), mentor.getMentorDecisionNotes());
    }

    @Transactional(readOnly = true)
    public List<MentorRequestResponse> listPending() {
        return users.findByRoleAndMentorStatusOrderByCreatedAtDesc(UserRole.MENTOR, ApprovalStatus.PENDING)
                .stream().map(this::toRequest).toList();
    }

    @Transactional
    public MentorRequestResponse decide(Long id, ApprovalStatus status, String notes, User admin,
                                        HttpServletRequest request) {
        User target = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));
        if (target.getRole() != UserRole.MENTOR) throw new IllegalArgumentException("User is not a mentor");
        if (status == ApprovalStatus.PENDING) throw new IllegalArgumentException("Decision cannot be PENDING");

        target.setMentorStatus(status);
        target.setMentorDecisionNotes(notes);
        target.setMentorDecisionAt(LocalDateTime.now());
        users.save(target);
        notifications.notify(target, "Mentor application updated", "Your mentor account is " + status + ".",
                status == ApprovalStatus.APPROVED ? "SUCCESS" : "WARNING", "/mentor-pending");
        auditLog.record(admin, "MENTOR_DECISION", "USER", id, "Status set to " + status, request);
        return toRequest(target);
    }

    private MentorRequestResponse toRequest(User user) {
        return MentorRequestResponse.builder().id(user.getId()).fullName(user.getFullName()).email(user.getEmail())
                .university(user.getUniversity()).department(user.getDepartment()).employeeId(user.getEmployeeId())
                .proofDocumentUrl(user.getProofDocumentUrl()).status(user.getMentorStatus())
                .requestDate(user.getCreatedAt()).decisionDate(user.getMentorDecisionAt())
                .decisionNotes(user.getMentorDecisionNotes()).build();
    }
}
