package com.apex.finance.repository;

import com.apex.finance.domain.IntercompanyLoan;
import com.apex.finance.domain.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntercompanyLoanRepository extends JpaRepository<IntercompanyLoan, String> {
    Optional<IntercompanyLoan> findByLoanReference(String loanReference);
    List<IntercompanyLoan> findByBorrowerSubsidiary(String borrowerSubsidiary);
    List<IntercompanyLoan> findByStatus(LoanStatus status);
}

