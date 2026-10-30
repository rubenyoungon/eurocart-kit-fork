package nl.group9.eurocart.cart;

import java.math.BigDecimal;

import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.catalogue.Product;
import nl.group9.eurocart.vat.VatRates;

/**
 * A product in the cart. {@code lineTotal} excludes VAT; {@code vatRate} is a percentage and {@code vat} the
 * VAT on the line total, rounded to cents. {@code priceInclVat} is the price of one item including VAT.
 */
public record CartLine(Product product, int quantity, BigDecimal lineTotal, BigDecimal vatRate, BigDecimal vat,
		BigDecimal priceInclVat) {

	public static CartLine of(Product product, int quantity, Country country) {
		BigDecimal lineTotal = product.price().multiply(BigDecimal.valueOf(quantity));
		BigDecimal vatRate = VatRates.percentage(country, product.category());
		return new CartLine(product, quantity, lineTotal, vatRate, VatRates.vatOn(lineTotal, vatRate),
				product.price().add(VatRates.vatOn(product.price(), vatRate)));
	}

}
