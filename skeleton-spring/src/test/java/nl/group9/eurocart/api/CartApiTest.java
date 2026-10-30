package nl.group9.eurocart.api;

import nl.group9.eurocart.cart.CartService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartApiTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CartService cartService;

	@BeforeEach
	void emptyCart() {
		cartService.clear();
	}

	@Test
	void listsAllProducts() throws Exception {
		mockMvc.perform(get("/api/products"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(51))
				.andExpect(jsonPath("$[0].id").value("LAP-001"))
				.andExpect(jsonPath("$[0].price").value(899.00));
	}

	@Test
	void productsShowPriceInclVatOfShopCountryExceptEbooks() throws Exception {
		mockMvc.perform(get("/api/products").header("Accept-Language", "nl-NL"))
				.andExpect(jsonPath("$[?(@.id == 'LAP-001')].vatRate").value(21))
				.andExpect(jsonPath("$[?(@.id == 'LAP-001')].priceInclVat").value(1087.79))
				.andExpect(jsonPath("$[?(@.id == 'BOK-001')].priceInclVat").value(13.63));

		mockMvc.perform(get("/api/products").header("Accept-Language", "de-CH"))
				.andExpect(jsonPath("$[?(@.id == 'BOK-001')].price").value(11.47))
				.andExpect(jsonPath("$[?(@.id == 'BOK-001')].vatRate").value(9))
				.andExpect(jsonPath("$[?(@.id == 'BOK-001')].priceInclVat").value(12.50))
				.andExpect(jsonPath("$[?(@.id == 'EBK-001')].vatRate").value(8.1));
	}

	@ParameterizedTest
	@ValueSource(strings = { "/pl", "/pl/", "/de", "/de/", "/fr", "/fr/" })
	void countryShopPagesServeTheFrontEnd(String path) throws Exception {
		mockMvc.perform(get(path))
				.andExpect(status().isOk())
				.andExpect(forwardedUrl("/index.html"));
	}

	@Test
	void addsItemAndReturnsCartWithSubtotal() throws Exception {
		mockMvc.perform(post("/api/cart/items")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"BOK-001\",\"quantity\":2}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lines.length()").value(1))
				.andExpect(jsonPath("$.lines[0].product.id").value("BOK-001"))
				.andExpect(jsonPath("$.lines[0].quantity").value(2))
				.andExpect(jsonPath("$.lines[0].lineTotal").value(25.00))
				.andExpect(jsonPath("$.subtotal").value(25.00))
				.andExpect(jsonPath("$.lines[0].vatRate").value(9))
				.andExpect(jsonPath("$.lines[0].priceInclVat").value(13.63))
				.andExpect(jsonPath("$.vat").value(2.25))
				.andExpect(jsonPath("$.total").value(27.25))
				.andExpect(jsonPath("$.country").value("NL"));
	}

	@Test
	void nonEbooksPayShopCountryVatWhateverTheLocale() throws Exception {
		cartService.add("BOK-001", 2);

		mockMvc.perform(get("/api/cart").header("Accept-Language", "de-DE"))
				.andExpect(jsonPath("$.country").value("DE"))
				.andExpect(jsonPath("$.lines[0].vatRate").value(9))
				.andExpect(jsonPath("$.vat").value(2.25))
				.andExpect(jsonPath("$.total").value(27.25));

		mockMvc.perform(get("/api/cart").header("Accept-Language", "pl-PL"))
				.andExpect(jsonPath("$.country").value("PL"))
				.andExpect(jsonPath("$.currency").value("PLN"))
				.andExpect(jsonPath("$.subtotal").value(108.70))
				.andExpect(jsonPath("$.vat").value(9.78))
				.andExpect(jsonPath("$.total").value(118.48));
	}

	@Test
	void ebooksPayVatOfDeliveryCountry() throws Exception {
		cartService.add("EBK-001", 1);

		mockMvc.perform(get("/api/cart").header("Accept-Language", "de-DE"))
				.andExpect(jsonPath("$.lines[0].vatRate").value(19))
				.andExpect(jsonPath("$.vat").value(1.71));

		mockMvc.perform(get("/api/cart").param("country", "PL"))
				.andExpect(jsonPath("$.lines[0].vatRate").value(23))
				.andExpect(jsonPath("$.subtotal").value(39.09))
				.andExpect(jsonPath("$.vat").value(8.99));
	}

	@Test
	void deliveryCountryOverridesLocaleForCurrencyAndVat() throws Exception {
		cartService.add("BOK-001", 2);

		mockMvc.perform(get("/api/cart").param("country", "PL").header("Accept-Language", "de-DE"))
				.andExpect(jsonPath("$.country").value("PL"))
				.andExpect(jsonPath("$.currency").value("PLN"))
				.andExpect(jsonPath("$.subtotal").value(108.70))
				.andExpect(jsonPath("$.vat").value(9.78));

		mockMvc.perform(get("/api/products").param("country", "GB").header("Accept-Language", "nl-NL"))
				.andExpect(jsonPath("$[?(@.id == 'BOK-001')].vatRate").value(9))
				.andExpect(jsonPath("$[?(@.id == 'EBK-001')].vatRate").value(20));
	}

	@Test
	void unknownDeliveryCountryGivesBadRequest() throws Exception {
		mockMvc.perform(get("/api/cart").param("country", "XX"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void checkoutWithAddressUsesItsCountry() throws Exception {
		cartService.add("BOK-001", 2);

		mockMvc.perform(post("/api/cart/checkout")
				.header("Accept-Language", "nl-NL")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"Jan Kowalski","street":"ul. Długa 1","postalCode":"00-001",
						 "city":"Warszawa","country":"PL"}"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.country").value("PL"))
				.andExpect(jsonPath("$.currency").value("PLN"))
				.andExpect(jsonPath("$.total").value(118.48))
				.andExpect(jsonPath("$.address.city").value("Warszawa"));
	}

	@Test
	void checkoutWithIncompleteAddressGivesBadRequestAndKeepsCart() throws Exception {
		cartService.add("BOK-001", 1);

		mockMvc.perform(post("/api/cart/checkout")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Jan\",\"street\":\" \",\"postalCode\":\"1234 AB\",\"city\":\"Utrecht\",\"country\":\"NL\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("Address is missing street"));

		mockMvc.perform(get("/api/cart"))
				.andExpect(jsonPath("$.lines.length()").value(1));
	}

	@Test
	void quantityDefaultsToOne() throws Exception {
		mockMvc.perform(post("/api/cart/items")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"FOD-002\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lines[0].quantity").value(1))
				.andExpect(jsonPath("$.subtotal").value(1.29));
	}

	@Test
	void removesItem() throws Exception {
		cartService.add("LAP-001", 1);
		cartService.add("BOK-001", 1);

		mockMvc.perform(delete("/api/cart/items/LAP-001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lines.length()").value(1))
				.andExpect(jsonPath("$.lines[0].product.id").value("BOK-001"));
	}

	@Test
	void clearsCart() throws Exception {
		cartService.add("LAP-001", 1);

		mockMvc.perform(delete("/api/cart"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lines.length()").value(0))
				.andExpect(jsonPath("$.subtotal").value(0));
	}

	@Test
	void unknownProductGivesNotFoundProblem() throws Exception {
		mockMvc.perform(post("/api/cart/items")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"NOPE-999\"}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Unknown product: NOPE-999"));
	}

	@Test
	void zeroQuantityGivesBadRequestProblem() throws Exception {
		mockMvc.perform(post("/api/cart/items")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"productId\":\"LAP-001\",\"quantity\":0}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void checkoutReturnsOrderAndEmptiesCart() throws Exception {
		cartService.add("BOK-001", 2);

		mockMvc.perform(post("/api/cart/checkout"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.orderNumber").isNumber())
				.andExpect(jsonPath("$.lines.length()").value(1))
				.andExpect(jsonPath("$.subtotal").value(25.00))
				.andExpect(jsonPath("$.vat").value(2.25))
				.andExpect(jsonPath("$.total").value(27.25));

		mockMvc.perform(get("/api/cart"))
				.andExpect(jsonPath("$.lines.length()").value(0));
	}

	@Test
	void checkoutOfEmptyCartGivesConflictProblem() throws Exception {
		mockMvc.perform(post("/api/cart/checkout"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("Cart is empty"));
	}

}
