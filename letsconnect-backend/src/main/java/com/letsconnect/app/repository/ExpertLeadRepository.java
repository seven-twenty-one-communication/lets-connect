package com.letsconnect.app.repository;

import com.letsconnect.app.model.entity.ExpertLead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpertLeadRepository extends JpaRepository<ExpertLead, UUID> {

    List<ExpertLead> findByPhoneNumber(String phoneNumber);

    Page<ExpertLead> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<ExpertLead> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<ExpertLead> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);
}
