package com.tendanz.pricing;

import com.tendanz.pricing.dto.QuoteRequest;
import com.tendanz.pricing.dto.QuoteResponse;
import com.tendanz.pricing.entity.PricingRule;
import com.tendanz.pricing.entity.Product;
import com.tendanz.pricing.entity.Quote;
import com.tendanz.pricing.entity.Zone;
import com.tendanz.pricing.repository.PricingRuleRepository;
import com.tendanz.pricing.repository.ProductRepository;
import com.tendanz.pricing.repository.QuoteRepository;
import com.tendanz.pricing.repository.ZoneRepository;
import com.tendanz.pricing.service.PricingService;

import jakarta.transaction.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({PricingService.class, ObjectMapper.class})
@Transactional
class PricingServiceTest {

    @Autowired
    private PricingService pricingService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private PricingRuleRepository pricingRuleRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    private Product product;
    private Zone zone;
    private PricingRule pricingRule;

    @BeforeEach
void setUp() {
    product = productRepository.findAll().stream()
            .filter(p -> p.getName().equals("Assurance Auto"))
            .findFirst().orElseThrow();

    zone = zoneRepository.findByCode("TUN").orElseThrow();

    pricingRule = pricingRuleRepository.findByProductId(product.getId()).orElseThrow();
}
    @Test
    void testCalculateQuoteForAdult() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(product.getId())
                .zoneCode("TUN")
                .clientName("Rayen Jouida")
                .clientAge(30)
                .build();

        QuoteResponse response = pricingService.calculateQuote(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("600.00"), response.getFinalPrice());
        assertEquals(new BigDecimal("500.00"), response.getBasePrice());
        assertEquals("Rayen Jouida", response.getClientName());
        assertEquals(30, response.getClientAge());
    }

    @Test
    void testCalculateQuoteForYoungClient() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(product.getId())
                .zoneCode("TUN")
                .clientName("Young Client")
                .clientAge(20)
                .build();

        QuoteResponse response = pricingService.calculateQuote(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("780.00"), response.getFinalPrice());
    }

    @Test
    void testCalculateQuoteForSeniorClient() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(product.getId())
                .zoneCode("TUN")
                .clientName("Senior Client")
                .clientAge(55)
                .build();

        QuoteResponse response = pricingService.calculateQuote(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("720.00"), response.getFinalPrice());
    }

    @Test
    void testCalculateQuoteForElderlyClient() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(product.getId())
                .zoneCode("TUN")
                .clientName("Elderly Client")
                .clientAge(70)
                .build();

        QuoteResponse response = pricingService.calculateQuote(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("900.00"), response.getFinalPrice());
    }

    @Test
    void testCalculateQuoteWithInvalidProductId() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(999L)
                .zoneCode("TUN")
                .clientName("Test Client")
                .clientAge(30)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> pricingService.calculateQuote(request));
    }

    @Test
    void testCalculateQuoteWithInvalidZoneCode() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(product.getId())
                .zoneCode("INVALID")
                .clientName("Test Client")
                .clientAge(30)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> pricingService.calculateQuote(request));
    }

    @Test
    void testGetQuoteById() {
        QuoteRequest request = QuoteRequest.builder()
                .productId(product.getId())
                .zoneCode("TUN")
                .clientName("Rayen Jouida")
                .clientAge(30)
                .build();

        QuoteResponse created = pricingService.calculateQuote(request);
        QuoteResponse fetched = pricingService.getQuote(created.getQuoteId());

        assertNotNull(fetched);
        assertEquals(created.getQuoteId(), fetched.getQuoteId());
        assertEquals(created.getFinalPrice(), fetched.getFinalPrice());
        assertEquals(created.getClientName(), fetched.getClientName());
    }

    @Test
    void testAgeBoundaries() {
        QuoteRequest youngBoundary = QuoteRequest.builder()
                .productId(product.getId()).zoneCode("TUN")
                .clientName("Boundary Young").clientAge(24).build();

        QuoteRequest adultBoundary = QuoteRequest.builder()
                .productId(product.getId()).zoneCode("TUN")
                .clientName("Boundary Adult").clientAge(25).build();

        QuoteResponse youngResponse = pricingService.calculateQuote(youngBoundary);
        QuoteResponse adultResponse = pricingService.calculateQuote(adultBoundary);

        assertEquals(new BigDecimal("780.00"), youngResponse.getFinalPrice());
        assertEquals(new BigDecimal("600.00"), adultResponse.getFinalPrice());
    }
}