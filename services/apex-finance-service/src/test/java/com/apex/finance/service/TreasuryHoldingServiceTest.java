package com.apex.finance.service;

import com.apex.common.exception.BusinessException;
import com.apex.finance.domain.IntercompanyLoan;
import com.apex.finance.domain.LoanStatus;
import com.apex.finance.domain.TreasuryCashPool;
import com.apex.finance.dto.LoanOriginationRequest;
import com.apex.finance.dto.LoanResponse;
import com.apex.finance.dto.TreasurySummaryResponse;
import com.apex.finance.repository.IntercompanyLoanRepository;
import com.apex.finance.repository.TreasuryCashPoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreasuryHoldingServiceTest {

    @Mock
    private TreasuryCashPoolRepository poolRepository;

    @Mock
    private IntercompanyLoanRepository loanRepository;

    @Mock
    private CryptographicLedgerService ledgerService;

    @InjectMocks
    private TreasuryHoldingService treasuryService;

    private TreasuryCashPool lenderPool;

    @BeforeEach
    void setUp() {
        lenderPool = new TreasuryCashPool(
            "pool-agro-1",
            "SUB-AGRO",
            "PT Apex Agro Lestari",
            new BigDecimal("50000000000.0000"), // 50 Miliar
            new BigDecimal("0.0600")
        );
    }

    @Test
    @DisplayName("Should successfully originate intercompany loan from surplus pool")
    void testOriginateLoanSuccess() {
        when(poolRepository.findBySubsidiaryCode("SUB-AGRO")).thenReturn(Optional.of(lenderPool));
        when(loanRepository.save(any(IntercompanyLoan.class))).thenAnswer(i -> i.getArgument(0));

        BigDecimal loanAmount = new BigDecimal("300000000.0000"); // 300 Juta
        LoanOriginationRequest request = new LoanOriginationRequest(
            "SUB-MFG",
            "SUB-AGRO",
            "pr-12345",
            loanAmount,
            6
        );

        LoanResponse response = treasuryService.originateLoan(request);

        assertThat(response).isNotNull();
        assertThat(response.borrowerSubsidiary()).isEqualTo("SUB-MFG");
        assertThat(response.lenderSubsidiary()).isEqualTo("SUB-AGRO");
        assertThat(response.principalAmount()).isEqualByComparingTo(loanAmount);
        assertThat(response.interestRate()).isEqualByComparingTo(new BigDecimal("0.0850")); // 8.5%
        assertThat(response.spreadRate()).isEqualByComparingTo(new BigDecimal("0.0250"));   // 2.5%
        assertThat(response.status()).isEqualTo(LoanStatus.ACTIVE);

        // Verify withdrawal from lender pool
        assertThat(lenderPool.getCurrentBalance()).isEqualByComparingTo(new BigDecimal("49700000000.0000"));

        // Verify double-entry GL ledger call
        verify(ledgerService).postBalancedEntry(
            anyString(),
            eq("1105-INTERCOMPANY-LOAN-RECEIVABLE"),
            eq("1001-HOLDING-CASH-POOL"),
            eq(loanAmount)
        );
    }

    @Test
    @DisplayName("Should reject loan origination if lender liquidity is insufficient")
    void testOriginateLoanInsufficientLiquidity() {
        TreasuryCashPool lowPool = new TreasuryCashPool(
            "pool-low-1",
            "SUB-AGRO",
            "PT Apex Agro",
            new BigDecimal("100000.0000"), // Only 100k
            new BigDecimal("0.0600")
        );
        when(poolRepository.findBySubsidiaryCode("SUB-AGRO")).thenReturn(Optional.of(lowPool));

        LoanOriginationRequest request = new LoanOriginationRequest(
            "SUB-MFG",
            "SUB-AGRO",
            "pr-12345",
            new BigDecimal("500000000.0000"),
            6
        );

        assertThatThrownBy(() -> treasuryService.originateLoan(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Insufficient surplus liquidity");
    }

    @Test
    @DisplayName("Should reject loan if borrower and lender are identical")
    void testOriginateLoanSameEntities() {
        LoanOriginationRequest request = new LoanOriginationRequest(
            "SUB-AGRO",
            "SUB-AGRO",
            "pr-12345",
            new BigDecimal("500000000.0000"),
            6
        );

        assertThatThrownBy(() -> treasuryService.originateLoan(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("distinct entities");
    }

    @Test
    @DisplayName("Should accrue monthly net interest margin to cryptographic ledger")
    void testAccrueMonthlyInterest() {
        IntercompanyLoan loan = new IntercompanyLoan(
            "loan-1",
            "ICL-001",
            "SUB-MFG",
            "SUB-AGRO",
            "pr-1",
            new BigDecimal("120000000.0000"), // 120 Juta
            new BigDecimal("0.0850"),
            new BigDecimal("0.0250"),          // 2.5% Spread = 3 Juta/year -> 250k/month
            12
        );
        when(loanRepository.findById("loan-1")).thenReturn(Optional.of(loan));

        treasuryService.accrueMonthlyInterest("loan-1");

        // Verify monthly net interest income posted to GL: 120M * 2.5% / 12 = 250,000
        verify(ledgerService).postBalancedEntry(
            eq("ICL-001-MINT"),
            eq("1001-HOLDING-CASH-POOL"),
            eq("4105-INTERCOMPANY-INTEREST-INCOME"),
            eq(new BigDecimal("250000.0000"))
        );
    }
}

