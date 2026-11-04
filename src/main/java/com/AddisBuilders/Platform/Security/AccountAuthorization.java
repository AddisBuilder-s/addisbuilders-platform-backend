package com.AddisBuilders.Platform.Security;

import com.AddisBuilders.Platform.Model.User;
import com.AddisBuilders.Platform.Model.enums.ApprovalStatus;
import com.AddisBuilders.Platform.Model.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("accountAuthorization")
public class AccountAuthorization {
    public boolean isApprovedMentor(Authentication authentication) {
        User user = authenticatedUser(authentication);
        return user != null && user.getRole() == UserRole.MENTOR && user.getMentorStatus() == ApprovalStatus.APPROVED;
    }

    public boolean isApprovedCoordinator(Authentication authentication) {
        User user = authenticatedUser(authentication);
        return user != null && user.getRole() == UserRole.INTERNSHIP_COORDINATOR
                && user.getCoordinatorStatus() == ApprovalStatus.APPROVED;
    }

    private User authenticatedUser(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof User user ? user : null;
    }
}
