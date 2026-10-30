package nl.group9.eurocart.cart;

import java.math.BigDecimal;
import java.util.List;

import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.currency.Currency;

/** {@code subtotal} excludes VAT; {@code total} is subtotal plus {@code vat}, for a customer in {@code country}. */
public record CartView(List<CartLine> lines, BigDecimal subtotal, BigDecimal vat, BigDecimal total,
		Currency currency, Country country) {
}
