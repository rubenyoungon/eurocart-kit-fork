package nl.group9.eurocart.cart;

import java.math.BigDecimal;
import java.util.List;

import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.currency.Currency;

/** {@code address} is where the order is delivered, or {@code null} when none was given. */
public record Order(int orderNumber, List<CartLine> lines, BigDecimal subtotal, BigDecimal vat, BigDecimal total,
		Currency currency, Country country, ShippingAddress address) {
}
