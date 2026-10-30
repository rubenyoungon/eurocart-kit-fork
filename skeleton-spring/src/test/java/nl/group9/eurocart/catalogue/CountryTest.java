package nl.group9.eurocart.catalogue;

import java.util.Locale;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CountryTest {

	@ParameterizedTest
	@CsvSource({ "nl-NL, NL", "de, DE", "de-CH, CH", "fr-CH, CH", "fr, FR", "pl, PL", "en-GB, GB", "en-US, NL",
			"en, NL" })
	void forLocaleUsesRegionThenLanguageAndFallsBackToNl(String languageTag, Country expected) {
		assertThat(Country.forLocale(Locale.forLanguageTag(languageTag))).isEqualTo(expected);
	}

}
