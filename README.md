# Pricing Engine — Tendanz Group

## How to run

### Backend

cd backend
mvn spring-boot:run

API runs on `http://localhost:8080`  
H2 console: `http://localhost:8080/h2-console` — JDBC URL: `jdbc:h2:mem:testdb`

### Frontend

cd frontend
npm install
ng serve

App runs on `http://localhost:4200`

---

## What I did

### Backend

The pricing logic lives entirely in `PricingService.calculateQuote()`, it loads the product, zone, and pricing rule from the DB, determines the age category, and applies the formula.

I used `BigDecimal` for the price calculation because `double` has rounding issues with decimal numbers (not acceptable for financial data).

Error handling is centralized in `GlobalExceptionHandler` 400 for validation, 404 when a product or zone doesn't exist, 500 for anything unexpected.

I also registered the `JavaTimeModule` on the `ObjectMapper` bean to handle `LocalDateTime` serialization properly, and added a CORS filter to allow requests from the Angular app.

### Frontend

Three pages : quote form, quote list, quote detail. The form uses Angular Reactive Forms with validators for all fields. Products are loaded from the API so the dropdown always reflects what's in the database.

The list supports filtering by product and minimum price, sorting by date or price, and client-side pagination (2 quotes per page).

### Tests

8 unit tests using `@DataJpaTest` that covers all 4 age categories, invalid product/zone errors, quote retrieval by ID, and age boundary cases (24 vs 25, etc).



## Bonus

- PDF export: `GET /api/quotes/{id}/pdf` using iText, with a download button on the detail page
- 4th product: "Assurance Voyage" added in `data.sql` with its own pricing rules
- Frontend pagination on the quote list

---

## What I'd improve with more time

- Backend pagination with Spring `Pageable` instead of slicing in memory on the frontend
- More integration tests covering the REST endpoints