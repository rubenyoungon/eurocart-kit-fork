package nl.group9.eurocart.api;

import java.math.BigDecimal;

import nl.group9.eurocart.catalogue.Category;
import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.catalogue.Product;
import nl.group9.eurocart.vat.VatRates;

/** A product as shown to a customer in {@code country}: {@code price} excludes VAT, {@code priceInclVat} includes it. */
public record ProductView(
		String id,
		String name,
		Category category,
		BigDecimal price,
		Country countryOfOrigin,
		BigDecimal vatRate,
		BigDecimal priceInclVat) {

	public static ProductView of(Product product, Country country) {
		BigDecimal vatRate = VatRates.forDelivery(country, product.category());
		return new ProductView(product.id(), product.name(), product.category(), product.price(),
				product.countryOfOrigin(), vatRate, product.price().add(VatRates.vatOn(product.price(), vatRate)));
	}

}
