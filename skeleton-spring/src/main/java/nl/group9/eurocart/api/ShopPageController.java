package nl.group9.eurocart.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Serves the shop front-end for a fixed country, whatever the browser's language. */
@Controller
public class ShopPageController {

	/** The shop as a customer in Poland sees it: prices in PLN with Polish VAT. */
	@GetMapping({ "/pl", "/pl/" })
	public String poland() {
		return "forward:/index.html";
	}

	/** The shop as a customer in Germany sees it: prices in EUR with German VAT. */
	@GetMapping({ "/de", "/de/" })
	public String germany() {
		return "forward:/index.html";
	}

	/** The shop as a customer in France sees it: prices in EUR with French VAT. */
	@GetMapping({ "/fr", "/fr/" })
	public String france() {
		return "forward:/index.html";
	}

}
