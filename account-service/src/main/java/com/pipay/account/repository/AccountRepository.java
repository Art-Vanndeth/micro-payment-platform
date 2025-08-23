package com.pipay.account.repository;

import com.pipay.account.constant.AccountStatus;
import com.pipay.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {

    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.status = :status")
    List<Account> findByUserIdAndStatus(@Param("userId") String userId,
                                        @Param("status") AccountStatus status);

    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount, a.updatedAt = :updatedAt WHERE a.accountNumber = :accountNumber")
    int updateBalance(@Param("accountNumber") String accountNumber,
                      @Param("amount") BigDecimal amount,
                      @Param("updatedAt") LocalDateTime updatedAt);

    Long countByStatus(AccountStatus accountStatus);
}
