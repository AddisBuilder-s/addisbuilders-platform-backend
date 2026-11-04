package com.AddisBuilders.Platform.Service;

import com.AddisBuilders.Platform.Exception.ResourceNotFoundException;

import com.AddisBuilders.Platform.DTO.CoordinatorRequestResponse;
import com.AddisBuilders.Platform.DTO.CoordinatorStatusResponse;
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
public class CoordinatorService {
    private final UserRepository users;
    private final NotificationService notifications;
    private final AuditLogService auditLog;

    @Transactional(readOnly = true)
    public CoordinatorStatusResponse status(User user) {
        if (user.getRole() != UserRole.INTERNSHIP_COORDINATOR) {
            throw new AccessDeniedException("Coordinator account required");
        }
        return new CoordinatorStatusResponse(user.getCoordinatorStatus(), user.getCoordinatorDecisionNotes());
    }

    @Transactional(readOnly = true)
    public List<CoordinatorRequestResponse> listPending() {
        return users.findByRoleAndCoordinatorStatusOrderByCreatedAtDesc(
                UserRole.INTERNSHIP_COORDINATOR, ApprovalStatus.PENDING).stream().map(this::toRequest).toList();
    }

    @Transactional
    public CoordinatorRequestResponse decide(Long id, ApprovalStatus status, String notes, User admin,
                                             HttpServletRequest request) {
        User target = users.findById(id).orElseThrow(() -> new ResourceNotFoundException("Coordinator not found"));
        if (target.getRole() != UserRole.INTERNSHIP_COORDINATOR) {
            throw new IllegalArgumentException("User is not an internship coordinator");
        }
        if (status == ApprovalStatus.PENDING) throw new IllegalArgumentException("Decision cannot be PENDING");
        target.setCoordinatorStatus(status);
        target.setCoordinatorDecisionNotes(notes);
        target.setCoordinatorDecisionAt(LocalDateTime.now());
        users.save(target);
        notifications.notify(target, "Coordinator application updated", "Your coordinator account is " + status + ".",
                status == ApprovalStatus.APPROVED ? "SUCCESS" : "WARNING", "/internship-manager/dashboard");
        auditLog.record(admin, "COORDINATOR_DECISION", "USER", id, "Status set to " + status, request);
        return toRequest(target);
    }

    private CoordinatorRequestResponse toRequest(User user) {
        return CoordinatorRequestResponse.builder().id(user.getId()).userId(String.valueOf(user.getId()))
                .fullName(user.getFullName()).email(user.getEmail()).university(user.getUniversity())
                .department(user.getDepartment()).employeeId(user.getEmployeeId())
                .proofDocumentUrl(user.getProofDocumentUrl()).status(user.getCoordinatorStatus())
                .requestDate(user.getCreatedAt()).decisionDate(user.getCoordinatorDecisionAt())
                .decisionNotes(user.getCoordinatorDecisionNotes()).build();
    }
}
