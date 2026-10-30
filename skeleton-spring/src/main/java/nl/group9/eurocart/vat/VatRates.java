package nl.group9.eurocart.vat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

import nl.group9.eurocart.catalogue.Category;
import nl.group9.eurocart.catalogue.Country;

/**
 * VAT percentages per customer country and product category. Categories without a reduced rate in a country
 * pay that country's standard rate.
 */
public final class VatRates {

	private static final Map<Country, BigDecimal> STANDARD = new EnumMap<>(Map.of(
			Country.NL, new BigDecimal("21"),
			Country.DE, new BigDecimal("19"),
			Country.FR, new BigDecimal("20"),
			Country.PL, new BigDecimal("23"),
			Country.CH, new BigDecimal("8.1"),
			Country.GB, new BigDecimal("20")));

	private static final Map<Country, Map<Category, BigDecimal>> REDUCED = new EnumMap<>(Map.of(
			// 9%: books, food, medicine
			Country.NL, Map.of(
					Category.BOOK, new BigDecimal("9"),
					Category.FOOD, new BigDecimal("9"),
					Category.MEDICINE, new BigDecimal("9")),
			// 7%: books, food (medicine is standard rate)
			Country.DE, Map.of(
					Category.BOOK, new BigDecimal("7"),
					Category.FOOD, new BigDecimal("7")),
			// 5.5%: basic food; 2.1%: prescription medicine
			Country.FR, Map.of(
					Category.FOOD, new BigDecimal("5.5"),
					Category.MEDICINE, new BigDecimal("2.1")),
			// 8%: medicine; 5%: books, basic food
			Country.PL, Map.of(
					Category.MEDICINE, new BigDecimal("8"),
					Category.BOOK, new BigDecimal("5"),
					Category.FOOD, new BigDecimal("5")),
			// 2.6%: basic food, books, medicine
			Country.CH, Map.of(
					Category.FOOD, new BigDecimal("2.6"),
					Category.BOOK, new BigDecimal("2.6"),
					Category.MEDICINE, new BigDecimal("2.6")),
			// 0%: most food, books
			Country.GB, Map.of(
					Category.FOOD, BigDecimal.ZERO,
					Category.BOOK, BigDecimal.ZERO)));

	private VatRates() {
	}

	/** The VAT percentage (e.g. {@code 21} for 21%) a customer in {@code country} pays on {@code category}. */
	public static BigDecimal percentage(Country country, Category category) {
		return REDUCED.get(country).getOrDefault(category, STANDARD.get(country));
	}

	/** The VAT on {@code amount} at {@code percentage}, rounded half-up to cents. */
	public static BigDecimal vatOn(BigDecimal amount, BigDecimal percentage) {
		return amount.multiply(percentage).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
	}

}
