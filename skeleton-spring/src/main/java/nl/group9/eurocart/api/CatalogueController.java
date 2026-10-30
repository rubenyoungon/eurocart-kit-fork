package nl.group9.eurocart.api;

import java.util.List;
import java.util.Locale;

import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.catalogue.ProductCatalogue;
import nl.group9.eurocart.currency.Currency;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class CatalogueController {

	private final ProductCatalogue catalogue;

	public CatalogueController(ProductCatalogue catalogue) {
		this.catalogue = catalogue;
	}

	/** All products, priced for the {@code country} parameter (the delivery country), else the locale's country. */
	@GetMapping
	public List<ProductView> all(@RequestParam(name = "country", required = false) Country deliveryCountry,
			Locale locale) {
		Country country = deliveryCountry != null ? deliveryCountry : Country.forLocale(locale);
		Currency currency = Currency.forCountry(country);
		return catalogue.all().stream().map(product -> ProductView.of(currency.convert(product), country)).toList();
	}

}
