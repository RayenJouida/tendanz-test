package com.tendanz.pricing.controller;

import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
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
import com.itextpdf.text.Document;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.ByteArrayOutputStream;

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



    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadQuotePdf(@PathVariable Long id) {
    log.info("GET /api/quotes/{}/pdf", id);
    QuoteResponse quote = pricingService.getQuote(id);

    try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();

        document.add(new Paragraph("TENDANZ GROUP - Insurance Quote", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Quote ID: " + quote.getQuoteId()));
        document.add(new Paragraph("Client: " + quote.getClientName()));
        document.add(new Paragraph("Age: " + quote.getClientAge()));
        document.add(new Paragraph("Product: " + quote.getProductName()));
        document.add(new Paragraph("Zone: " + quote.getZoneName()));
        document.add(new Paragraph("Base Price: " + quote.getBasePrice() + " TND"));
        document.add(new Paragraph("Final Price: " + quote.getFinalPrice() + " TND"));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Applied Rules:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        for (String rule : quote.getAppliedRules()) {
            document.add(new Paragraph("  - " + rule));
        }
        document.add(new Paragraph(" "));
        document.add(new Paragraph("Generated: " + quote.getCreatedAt()));

        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "quote-" + id + ".pdf");

        return ResponseEntity.ok().headers(headers).body(baos.toByteArray());

    } catch (Exception e) {
        log.error("Error generating PDF for quote {}", id, e);
        throw new RuntimeException("Error generating PDF");
    }
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