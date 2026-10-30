package nl.group9.eurocart.cart;

import java.math.BigDecimal;

import nl.group9.eurocart.catalogue.Country;
import nl.group9.eurocart.catalogue.ProductCatalogue;
import nl.group9.eurocart.currency.Currency;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartServiceTest {

	private final CartService cart = new CartService(new ProductCatalogue(new ObjectMapper()));

	@Test
	void startsEmpty() {
		CartView view = cart.view();

		assertThat(view.lines()).isEmpty();
		assertThat(view.subtotal()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void addsProductsAndSumsPricesExcludingVat() {
		cart.add("BOK-001", 2); // 2 x 12.50
		CartView view = cart.add("FOD-002", 1); // 1 x 1.29

		assertThat(view.lines()).hasSize(2);
		assertThat(view.subtotal()).isEqualByComparingTo("26.29");
	}

	@Test
	void addingSameProductAgainIncreasesQuantity() {
		cart.add("MED-001", 1);
		CartView view = cart.add("MED-001", 3);

		assertThat(view.lines()).singleElement().satisfies(line -> {
			assertThat(line.quantity()).isEqualTo(4);
			assertThat(line.lineTotal()).isEqualByComparingTo("9.96");
		});
	}

	@Test
	void removesProductRegardlessOfQuantity() {
		cart.add("LAP-001", 5);
		cart.add("BOK-010", 1);

		CartView view = cart.remove("LAP-001");

		assertThat(view.lines()).extracting(line -> line.product().id()).containsExactly("BOK-010");
	}

	@Test
	void clearEmptiesTheCart() {
		cart.add("LAP-001", 1);

		CartView view = cart.clear();

		assertThat(view.lines()).isEmpty();
	}

	@Test
	void rejectsUnknownProduct() {
		assertThatThrownBy(() -> cart.add("NOPE-999", 1))
				.isInstanceOf(UnknownProductException.class)
				.hasMessageContaining("NOPE-999");
	}

	@Test
	void rejectsQuantityBelowOne() {
		assertThatThrownBy(() -> cart.add("LAP-001", 0))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void checkoutPlacesOrderAndEmptiesCart() {
		cart.add("BOK-001", 2);

		Order order = cart.checkout();

		assertThat(order.orderNumber()).isEqualTo(1);
		assertThat(order.lines()).hasSize(1);
		assertThat(order.subtotal()).isEqualByComparingTo("25.00");
		assertThat(cart.view().lines()).isEmpty();
	}

	@Test
	void checkoutNumbersOrdersConsecutively() {
		cart.add("BOK-001", 1);
		cart.checkout();
		cart.add("BOK-001", 1);

		assertThat(cart.checkout().orderNumber()).isEqualTo(2);
	}

	@Test
	void rejectsCheckoutOfEmptyCart() {
		assertThatThrownBy(cart::checkout)
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void addsVatPerLineAtShopCountryRates() {
		cart.add("BOK-001", 2); // 25.00 books
		cart.add("LAP-001", 1); // 899.00 laptop

		CartView nl = cart.view(Currency.EUR, Country.NL); // 9% + 21%
		assertThat(nl.lines()).extracting(CartLine::vat).containsExactly(new BigDecimal("2.25"),
				new BigDecimal("188.79"));
		assertThat(nl.subtotal()).isEqualByComparingTo("924.00");
		assertThat(nl.vat()).isEqualByComparingTo("191.04");
		assertThat(nl.total()).isEqualByComparingTo("1115.04");

		CartView de = cart.view(Currency.EUR, Country.DE); // still NL rates: only e-books follow the customer
		assertThat(de.vat()).isEqualByComparingTo("191.04");
		assertThat(de.total()).isEqualByComparingTo("1115.04");
	}

	@Test
	void checkoutIncludesVat() {
		cart.add("EBK-001", 1); // 8.99 e-book, 20% in GB

		Order order = cart.checkout(Currency.EUR, Country.GB);

		assertThat(order.vat()).isEqualByComparingTo("1.80");
		assertThat(order.total()).isEqualByComparingTo("10.79");
		assertThat(order.country()).isEqualTo(Country.GB);
	}

}
