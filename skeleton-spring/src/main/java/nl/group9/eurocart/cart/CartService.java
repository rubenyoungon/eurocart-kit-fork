package nl.group9.eurocart.cart;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.catalogue.Product;
import nl.group9.eurocart.catalogue.ProductCatalogue;
import nl.group9.eurocart.currency.Currency;

import org.springframework.stereotype.Service;

@Service
public class CartService {

	private final ProductCatalogue catalogue;

	private final Map<String, Integer> quantitiesByProductId = new LinkedHashMap<>();

	private int lastOrderNumber;

	public CartService(ProductCatalogue catalogue) {
		this.catalogue = catalogue;
	}

	/** Adds {@code quantity} of a product. Adding an existing product increases its quantity. */
	public synchronized CartView add(String productId, int quantity) {
		if (quantity < 1) {
			throw new IllegalArgumentException("Quantity must be at least 1");
		}
		if (catalogue.find(productId).isEmpty()) {
			throw new UnknownProductException(productId);
		}
		quantitiesByProductId.merge(productId, quantity, Integer::sum);
		return view();
	}

	/** Removes a product entirely, whatever its quantity. Unknown ids are ignored. */
	public synchronized CartView remove(String productId) {
		quantitiesByProductId.remove(productId);
		return view();
	}

	public synchronized CartView clear() {
		quantitiesByProductId.clear();
		return view();
	}

	/** Places an order for everything in the cart and empties it. */
	public synchronized Order checkout() {
		return checkout(Currency.EUR, Country.NL);
	}

	/**
	 * Places an order for everything in the cart, priced in {@code currency} with VAT for a customer in
	 * {@code country}, and empties it.
	 */
	public synchronized Order checkout(Currency currency, Country country) {
		if (quantitiesByProductId.isEmpty()) {
			throw new IllegalStateException("Cart is empty");
		}
		CartView view = view(currency, country);
		quantitiesByProductId.clear();
		return new Order(++lastOrderNumber, view.lines(), view.subtotal(), view.vat(), view.total(), currency,
				country);
	}

	public synchronized CartView view() {
		return view(Currency.EUR, Country.NL);
	}

	/** The cart with all prices converted to {@code currency} and VAT for a customer in {@code country}. */
	public synchronized CartView view(Currency currency, Country country) {
		List<CartLine> lines = quantitiesByProductId.entrySet().stream()
				.map(entry -> CartLine.of(currency.convert(product(entry.getKey())), entry.getValue(), country))
				.toList();
		BigDecimal subtotal = lines.stream()
				.map(CartLine::lineTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal vat = lines.stream()
				.map(CartLine::vat)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return new CartView(lines, subtotal, vat, subtotal.add(vat), currency, country);
	}

	private Product product(String productId) {
		return catalogue.find(productId).orElseThrow(() -> new UnknownProductException(productId));
	}

}
