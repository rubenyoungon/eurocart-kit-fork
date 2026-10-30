package nl.group9.eurocart.catalogue;

import java.util.Locale;

public enum Country {
	NL,
	DE,
	FR,
	PL,
	CH,
	GB;

	/**
	 * The customer's country, taken from their browser locale ({@code Accept-Language}). Uses the region when
	 * the locale has one (de-CH, en-GB), else the language (nl, de, fr, pl). Anything else is treated as NL.
	 */
	public static Country forLocale(Locale locale) {
		String code = locale.getCountry().isEmpty() ? locale.getLanguage() : locale.getCountry();
		code = code.toUpperCase(Locale.ROOT);
		if (code.equals("UK")) {
			code = "GB";
		}
		for (Country country : values()) {
			if (country.name().equals(code)) {
				return country;
			}
		}
		return NL;
	}

}
