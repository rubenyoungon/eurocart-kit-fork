package nl.group9.eurocart.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import nl.group9.eurocart.catalogue.Product;

/** Currencies prices can be shown in. Catalogue prices are in EUR; rates are fixed EUR → currency. */
public enum Currency {

	EUR(BigDecimal.ONE),
	PLN(new BigDecimal("4.25"));

	private final BigDecimal ratePerEuro;

	Currency(BigDecimal ratePerEuro) {
		this.ratePerEuro = ratePerEuro;
	}

	/**
	 * The currency for a customer's location, taken from their browser locale ({@code Accept-Language}).
	 * Uses the region when the locale has one (pl-PL, en-PL), else the language (pl).
	 */
	public static Currency forLocale(Locale locale) {
		String region = locale.getCountry();
		boolean poland = region.isEmpty() ? locale.getLanguage().equals("pl") : region.equals("PL");
		return poland ? PLN : EUR;
	}

	public BigDecimal convert(BigDecimal euros) {
		return euros.multiply(ratePerEuro).setScale(2, RoundingMode.HALF_UP);
	}

	/** The same product with its price in this currency. */
	public Product convert(Product product) {
		return new Product(product.id(), product.name(), product.category(), convert(product.price()),
				product.countryOfOrigin());
	}

}
