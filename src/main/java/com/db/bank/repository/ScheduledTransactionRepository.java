package com.db.bank.repository;

import com.db.bank.domain.entity.ScheduledTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduledTransactionRepository extends JpaRepository<ScheduledTransaction, Long> {

}
