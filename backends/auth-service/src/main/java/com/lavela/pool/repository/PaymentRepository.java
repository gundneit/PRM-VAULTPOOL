package com.lavela.pool.repository;

import com.lavela.pool.domain.entity.Payment;
import com.lavela.pool.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    /**
     * Idempotency: webhook may be called multiple times for same providerTxnId.
     * Unique constraint (provider, provider_txn_id) ensures one record per provider transaction.
     */
    Optional<Payment> findByProviderAndProviderTxnId(String provider, String providerTxnId);
}
