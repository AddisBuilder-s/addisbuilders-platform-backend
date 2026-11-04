package com.AddisBuilders.Platform.Repository;

import com.AddisBuilders.Platform.Model.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, String> {
}
