package nl.group9.eurocart.api;

import java.util.Locale;
import nl.group9.eurocart.cart.CartService;
import nl.group9.eurocart.cart.CartView;
import nl.group9.eurocart.cart.Order;
import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.currency.Currency;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

	private final CartService cart;

	public CartController(CartService cart) {
		this.cart = cart;
	}

	@GetMapping
	public CartView view(Locale locale) {
		return cartFor(locale);
	}

	@PostMapping("/items")
	public CartView addItem(@RequestBody AddItemRequest request,
			Locale locale) {
		cart.add(request.productId(), request.quantityOrOne());
		return cartFor(locale);
	}

	@DeleteMapping("/items/{productId}")
	public CartView removeItem(@PathVariable String productId,
			Locale locale) {
		cart.remove(productId);
		return cartFor(locale);
	}

	@DeleteMapping
	public CartView clear(Locale locale) {
		cart.clear();
		return cartFor(locale);
	}

	@PostMapping("/checkout")
	public Order checkout(Locale locale) {
		return cart.checkout(Currency.forLocale(locale), Country.forLocale(locale));
	}

	private CartView cartFor(Locale locale) {
		return cart.view(Currency.forLocale(locale), Country.forLocale(locale));
	}

}
