package com.letsconnect.app.repository;

import com.letsconnect.app.model.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserReportRepository extends JpaRepository<UserReport, UUID> {
}
