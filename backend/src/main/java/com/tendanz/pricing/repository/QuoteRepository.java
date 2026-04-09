package com.tendanz.pricing.repository;

import com.tendanz.pricing.entity.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Quote entity.
 * Provides database operations for quotes.
 *
 * TODO: Add custom query methods:
 * - findByClientName(String clientName)
 * - findByProductId(Long productId)
 * - A custom @Query to find quotes with finalPrice above a given threshold
 *
 * Hint: Spring Data JPA can derive queries from method names.
 * For more complex queries, use @Query with JPQL.
 */

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    List<Quote> findByClientName(String clientName);

    List<Quote> findByProductId(Long productId);

    @Query("SELECT q FROM Quote q WHERE q.finalPrice >= :threshold")
    List<Quote> findByFinalPriceAbove(@Param("threshold") BigDecimal threshold);

    // TODO: Add custom query methods here

}
