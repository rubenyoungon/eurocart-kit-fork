package nl.group9.eurocart.api;

import nl.group9.eurocart.cart.CartService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
				.andExpect(jsonPath("$.vat").value(2.25))
				.andExpect(jsonPath("$.total").value(27.25))
				.andExpect(jsonPath("$.country").value("NL"));
	}

	@Test
	void vatFollowsCustomerCountryFromLocale() throws Exception {
		cartService.add("BOK-001", 2);

		mockMvc.perform(get("/api/cart").header("Accept-Language", "de-DE"))
				.andExpect(jsonPath("$.country").value("DE"))
				.andExpect(jsonPath("$.vat").value(1.75))
				.andExpect(jsonPath("$.total").value(26.75));

		mockMvc.perform(get("/api/cart").header("Accept-Language", "pl-PL"))
				.andExpect(jsonPath("$.country").value("PL"))
				.andExpect(jsonPath("$.currency").value("PLN"))
				.andExpect(jsonPath("$.subtotal").value(106.26))
				.andExpect(jsonPath("$.vat").value(5.31))
				.andExpect(jsonPath("$.total").value(111.57));
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
