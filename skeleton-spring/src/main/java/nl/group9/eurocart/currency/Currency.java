package nl.group9.eurocart.currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.catalogue.Product;

/** Currencies prices can be shown in. Catalogue prices are in EUR; rates are fixed, in euros per unit of currency. */
public enum Currency {

	EUR(BigDecimal.ONE),
	PLN(new BigDecimal("0.23")),
	GBP(new BigDecimal("1.20")),
	CHF(new BigDecimal("1.09"));

	private final BigDecimal eurosPerUnit;

	Currency(BigDecimal eurosPerUnit) {
		this.eurosPerUnit = eurosPerUnit;
	}

	/**
	 * The currency for a customer's location, taken from their browser locale ({@code Accept-Language}): the
	 * currency of the country {@link Country#forLocale} finds (pl-PL → PLN, en-GB → GBP, de-CH → CHF), else EUR.
	 */
	public static Currency forLocale(Locale locale) {
		return forCountry(Country.forLocale(locale));
	}

	/** The currency customers in {@code country} pay in. */
	public static Currency forCountry(Country country) {
		return switch (country) {
			case PL -> PLN;
			case GB -> GBP;
			case CH -> CHF;
			case NL, DE, FR -> EUR;
		};
	}

	public BigDecimal convert(BigDecimal euros) {
		return euros.divide(eurosPerUnit, 2, RoundingMode.HALF_UP);
	}

	/** The same product with its price in this currency. */
	public Product convert(Product product) {
		return new Product(product.id(), product.name(), product.category(), convert(product.price()),
				product.countryOfOrigin());
	}

}
