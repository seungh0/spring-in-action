package tacos.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tacos.external.WebClientSample;

@RestController
public class WebClientSampleController {

	private final WebClientSample webClientSample;

	public WebClientSampleController(WebClientSample webClientSample) {
		this.webClientSample = webClientSample;
	}

	@GetMapping("/test")
	public void test(String id) {
		webClientSample.getIngredientById(id);
	}

	@GetMapping("/test1")
	public void tes1() {
		webClientSample.getIngredients();
	}

}
