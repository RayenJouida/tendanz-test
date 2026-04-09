package com.tendanz.pricing.controller;

import com.tendanz.pricing.dto.QuoteRequest;
import com.tendanz.pricing.dto.QuoteResponse;
import com.tendanz.pricing.entity.Quote;
import com.tendanz.pricing.repository.QuoteRepository;
import com.tendanz.pricing.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
@Slf4j
public class QuoteController {

    private final PricingService pricingService;
    private final QuoteRepository quoteRepository;

    @PostMapping
    public ResponseEntity<QuoteResponse> createQuote(@Valid @RequestBody QuoteRequest request) {
        log.info("POST /api/quotes — client: {}", request.getClientName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pricingService.calculateQuote(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponse> getQuote(@PathVariable Long id) {
        log.info("Fetching quote with ID: {}", id);
        return ResponseEntity.ok(pricingService.getQuote(id));
    }

    @GetMapping
    public ResponseEntity<List<QuoteResponse>> getAllQuotes(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Double minPrice) {

        log.info("GET /api/quotes — productId: {}, minPrice: {}", productId, minPrice);

        List<Quote> quotes;

        if (productId != null && minPrice != null) {
            quotes = quoteRepository.findByProductId(productId).stream()
                    .filter(q -> q.getFinalPrice().compareTo(BigDecimal.valueOf(minPrice)) >= 0)
                    .toList();
        } else if (productId != null) {
            quotes = quoteRepository.findByProductId(productId);
        } else if (minPrice != null) {
            quotes = quoteRepository.findByFinalPriceAbove(BigDecimal.valueOf(minPrice));
        } else {
            quotes = quoteRepository.findAll();
        }

        return ResponseEntity.ok(
                quotes.stream().map(q -> pricingService.getQuote(q.getId())).toList()
        );
    }
}