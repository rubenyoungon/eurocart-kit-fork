# EuroCart skeleton (Spring Boot)

Optional quick start for the EuroCart case. It gives you a running shop with a product catalogue and a
cart that sums base prices, so your first slice can be a real feature instead of plumbing.

## Run

Requires Java 21 or newer. Nothing else to install.

```bash
./mvnw spring-boot:run        # http://localhost:8080
./mvnw test                   # run the tests
```

On Windows use `mvnw.cmd`. 

## Data

Both files live in `src/main/resources/` and are plain JSON, so you can also use them without this skeleton.

- `catalogue.json`: 51 products, prices in EUR excluding VAT.
- `discount.json`: 50 discount codes as issued by Sales. Types are `PERCENTAGE`, `FIXED_AMOUNT` (with
  `currency`) and `FREE_SHIPPING`. Optional fields: `categories`, `countries`, `validFrom`, `validUntil`
  and `minimumOrder`; a missing field means no restriction. Whether every code is actually allowed is
  something to ask the experts. The skeleton does not load this file yet.
