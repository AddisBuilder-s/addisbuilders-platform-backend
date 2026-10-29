package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.InternshipListing;
import com.AddisBuilders.Platform.Model.enums.InternshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InternshipListingRepository extends JpaRepository<InternshipListing, Long> {
    List<InternshipListing> findByOrganizationId(Long organizationId);
    List<InternshipListing> findByStatus(InternshipStatus status);
}