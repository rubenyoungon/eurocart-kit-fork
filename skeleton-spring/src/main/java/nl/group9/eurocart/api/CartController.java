package nl.group9.eurocart.api;

import java.util.Locale;
import nl.group9.eurocart.cart.CartService;
import nl.group9.eurocart.cart.CartView;
import nl.group9.eurocart.cart.Order;
import nl.group9.eurocart.cart.ShippingAddress;
import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.currency.Currency;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The cart, priced for a customer in the {@code country} request parameter (the delivery country) or, without
 * it, the country of their browser locale.
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

	private final CartService cart;

	public CartController(CartService cart) {
		this.cart = cart;
	}

	@GetMapping
	public CartView view(@RequestParam(required = false) Country country, Locale locale) {
		return cartFor(country, locale);
	}

	@PostMapping("/items")
	public CartView addItem(@RequestBody AddItemRequest request,
			@RequestParam(required = false) Country country, Locale locale) {
		cart.add(request.productId(), request.quantityOrOne());
		return cartFor(country, locale);
	}

	@DeleteMapping("/items/{productId}")
	public CartView removeItem(@PathVariable String productId,
			@RequestParam(required = false) Country country, Locale locale) {
		cart.remove(productId);
		return cartFor(country, locale);
	}

	@DeleteMapping
	public CartView clear(@RequestParam(required = false) Country country, Locale locale) {
		cart.clear();
		return cartFor(country, locale);
	}

	/** Orders the cart. With a delivery address, its country decides currency and VAT; else the locale does. */
	@PostMapping("/checkout")
	public Order checkout(@RequestBody(required = false) ShippingAddress address, Locale locale) {
		if (address != null) {
			return cart.checkout(address);
		}
		return cart.checkout(Currency.forLocale(locale), Country.forLocale(locale));
	}

	private CartView cartFor(Country country, Locale locale) {
		Country customerCountry = country != null ? country : Country.forLocale(locale);
		return cart.view(Currency.forCountry(customerCountry), customerCountry);
	}

}
