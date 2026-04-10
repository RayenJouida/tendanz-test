package com.tendanz.pricing.service;

import com.tendanz.pricing.dto.QuoteRequest;
import com.tendanz.pricing.dto.QuoteResponse;
import com.tendanz.pricing.entity.PricingRule;
import com.tendanz.pricing.entity.Product;
import com.tendanz.pricing.entity.Quote;
import com.tendanz.pricing.entity.Zone;
import com.tendanz.pricing.enums.AgeCategory;
import com.tendanz.pricing.repository.PricingRuleRepository;
import com.tendanz.pricing.repository.ProductRepository;
import com.tendanz.pricing.repository.QuoteRepository;
import com.tendanz.pricing.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {

    private final ProductRepository productRepository;
    private final ZoneRepository zoneRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final QuoteRepository quoteRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public QuoteResponse calculateQuote(QuoteRequest request) {
        log.info("Calculating quote for client: {}, product: {}, zone: {}",
                request.getClientName(), request.getProductId(), request.getZoneCode());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product not found with ID: " + request.getProductId()));

        Zone zone = zoneRepository.findByCode(request.getZoneCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Zone not found with code: " + request.getZoneCode()));

        PricingRule rule = pricingRuleRepository.findByProductId(product.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No pricing rule found for product ID: " + product.getId()));

        AgeCategory ageCategory = AgeCategory.fromAge(request.getClientAge());
        log.debug("Client age {} mapped to category: {}", request.getClientAge(), ageCategory);

        BigDecimal ageFactor = getAgeFactor(rule, ageCategory);

        BigDecimal finalPrice = rule.getBaseRate()
                .multiply(ageFactor)
                .multiply(zone.getRiskCoefficient())
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Calculated price: {} × {} × {} = {}",
                rule.getBaseRate(), ageFactor, zone.getRiskCoefficient(), finalPrice);

        List<String> appliedRules = new ArrayList<>();
        appliedRules.add("Product: " + product.getName() + " — Base rate: " + rule.getBaseRate() + " TND");
        appliedRules.add("Client age: " + request.getClientAge() + " — Category: " + ageCategory + " — Age factor: " + ageFactor);
        appliedRules.add("Zone: " + zone.getName() + " (" + zone.getCode() + ") — Risk coefficient: " + zone.getRiskCoefficient());
        appliedRules.add("Final price: " + rule.getBaseRate() + " × " + ageFactor + " × " + zone.getRiskCoefficient() + " = " + finalPrice + " TND");

        Quote quote = Quote.builder()
                .product(product)
                .zone(zone)
                .clientName(request.getClientName())
                .clientAge(request.getClientAge())
                .basePrice(rule.getBaseRate())
                .finalPrice(finalPrice)
                .appliedRules(convertRulesToJson(appliedRules))
                .build();

        quoteRepository.save(quote);
        log.info("Quote saved with ID: {}", quote.getId());

        return mapToResponse(quote, appliedRules);
    }

    private BigDecimal getAgeFactor(PricingRule pricingRule, AgeCategory ageCategory) {
        return switch (ageCategory) {
            case YOUNG   -> pricingRule.getAgeFactorYoung();
            case ADULT   -> pricingRule.getAgeFactorAdult();
            case SENIOR  -> pricingRule.getAgeFactorSenior();
            case ELDERLY -> pricingRule.getAgeFactorElderly();
        };
    }

    private String convertRulesToJson(List<String> rules) {
        try {
            return objectMapper.writeValueAsString(rules);
        } catch (Exception e) {
            log.error("Error converting rules to JSON", e);
            return "[]";
        }
    }

    private QuoteResponse mapToResponse(Quote quote, List<String> appliedRules) {
        return QuoteResponse.builder()
                .quoteId(quote.getId())
                .productName(quote.getProduct().getName())
                .zoneName(quote.getZone().getName())
                .clientName(quote.getClientName())
                .clientAge(quote.getClientAge())
                .basePrice(quote.getBasePrice())
                .finalPrice(quote.getFinalPrice())
                .appliedRules(appliedRules)
                .createdAt(quote.getCreatedAt() != null ? quote.getCreatedAt().toString() : null)
                .build();
    }

    public QuoteResponse getQuote(Long id) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found with ID: " + id));

        List<String> appliedRules = deserializeRules(quote.getAppliedRules());
        return mapToResponse(quote, appliedRules);
    }

    private List<String> deserializeRules(String rulesJson) {
        try {
            return objectMapper.readValue(rulesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            log.error("Error deserializing rules from JSON", e);
            return new ArrayList<>();
        }
    }
}