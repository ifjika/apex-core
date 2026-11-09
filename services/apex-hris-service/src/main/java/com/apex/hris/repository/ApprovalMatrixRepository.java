package com.apex.hris.repository;

import com.apex.hris.domain.ApprovalMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalMatrixRepository extends JpaRepository<ApprovalMatrix, String> {
    Optional<ApprovalMatrix> findByRole(String role);
}

