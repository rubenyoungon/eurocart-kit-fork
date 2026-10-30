package nl.group9.eurocart.api;

import java.util.List;
import java.util.Locale;

import nl.group9.eurocart.catalogue.Product;
import nl.group9.eurocart.catalogue.ProductCatalogue;
import nl.group9.eurocart.currency.Currency;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class CatalogueController {

	private final ProductCatalogue catalogue;

	public CatalogueController(ProductCatalogue catalogue) {
		this.catalogue = catalogue;
	}

	@GetMapping
	public List<Product> all(Locale locale) {
		return catalogue.all().stream().map(Currency.forLocale(locale)::convert).toList();
	}

}
