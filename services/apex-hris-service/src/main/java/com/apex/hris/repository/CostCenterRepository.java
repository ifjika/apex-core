package com.apex.hris.repository;

import com.apex.hris.domain.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CostCenterRepository extends JpaRepository<CostCenter, String> {
    Optional<CostCenter> findByCode(String code);
}

