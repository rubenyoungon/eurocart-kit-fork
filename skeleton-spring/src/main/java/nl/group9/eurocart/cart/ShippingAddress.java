package nl.group9.eurocart.cart;

import nl.group9.eurocart.catalogue.Country;

/** Where an order is delivered. The {@code country} decides the currency and VAT of the order. */
public record ShippingAddress(String name, String street, String postalCode, String city, Country country) {

	/** Throws {@link IllegalArgumentException} naming the first field that is missing or blank. */
	public void requireComplete() {
		requireText("name", name);
		requireText("street", street);
		requireText("postalCode", postalCode);
		requireText("city", city);
		if (country == null) {
			throw new IllegalArgumentException("Address is missing country");
		}
	}

	private static void requireText(String field, String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Address is missing " + field);
		}
	}

}
