package nl.group9.eurocart.vat;

import java.math.BigDecimal;

import nl.group9.eurocart.catalogue.Category;
import nl.group9.eurocart.catalogue.Country;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class VatRatesTest {

	@ParameterizedTest
	@CsvSource({
			"NL, LAPTOP, 21", "NL, BOOK, 9", "NL, FOOD, 9", "NL, MEDICINE, 9", "NL, EBOOK, 21",
			"DE, LAPTOP, 19", "DE, BOOK, 7", "DE, FOOD, 7", "DE, MEDICINE, 19", "DE, EBOOK, 19",
			"FR, LAPTOP, 20", "FR, BOOK, 20", "FR, FOOD, 5.5", "FR, MEDICINE, 2.1", "FR, EBOOK, 20",
			"PL, LAPTOP, 23", "PL, BOOK, 5", "PL, FOOD, 5", "PL, MEDICINE, 8", "PL, EBOOK, 23",
			"CH, LAPTOP, 8.1", "CH, BOOK, 2.6", "CH, FOOD, 2.6", "CH, MEDICINE, 2.6", "CH, EBOOK, 8.1",
			"GB, LAPTOP, 20", "GB, BOOK, 0", "GB, FOOD, 0", "GB, MEDICINE, 20", "GB, EBOOK, 20" })
	void percentagePerCountryAndCategory(Country country, Category category, BigDecimal expected) {
		assertThat(VatRates.percentage(country, category)).isEqualByComparingTo(expected);
	}

	@Test
	void vatIsRoundedHalfUpToCents() {
		assertThat(VatRates.vatOn(new BigDecimal("1.29"), new BigDecimal("9"))).isEqualByComparingTo("0.12");
		assertThat(VatRates.vatOn(new BigDecimal("0.50"), new BigDecimal("9"))).isEqualByComparingTo("0.05");
		assertThat(VatRates.vatOn(new BigDecimal("12.50"), new BigDecimal("8.1"))).isEqualByComparingTo("1.01");
	}

}
