package nl.group9.eurocart.currency;

import java.util.Locale;

import nl.group9.eurocart.catalogue.Country;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyTest {

	@ParameterizedTest
	@CsvSource({ "nl-NL, EUR", "de-DE, EUR", "fr, EUR", "pl-PL, PLN", "pl, PLN", "en-PL, PLN", "en-GB, GBP",
			"de-CH, CHF", "fr-CH, CHF", "en-US, EUR" })
	void forLocaleFollowsCustomerCountry(String languageTag, Currency expected) {
		assertThat(Currency.forLocale(Locale.forLanguageTag(languageTag))).isEqualTo(expected);
	}

	@ParameterizedTest
	@CsvSource({ "NL, EUR", "DE, EUR", "FR, EUR", "PL, PLN", "GB, GBP", "CH, CHF" })
	void forCountryGivesTheCountrysCurrency(Country country, Currency expected) {
		assertThat(Currency.forCountry(country)).isEqualTo(expected);
	}

}
