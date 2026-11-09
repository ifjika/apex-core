package com.apex.finance.repository;

import com.apex.finance.domain.TreasuryCashPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TreasuryCashPoolRepository extends JpaRepository<TreasuryCashPool, String> {
    Optional<TreasuryCashPool> findBySubsidiaryCode(String subsidiaryCode);
}

