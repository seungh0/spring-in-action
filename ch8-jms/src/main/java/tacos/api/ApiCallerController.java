package tacos.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tacos.domain.Ingredient;
import tacos.external.TacoCloudClient;

@RestController
public class ApiCallerController {

	private final TacoCloudClient tacoCloudClient;

	public ApiCallerController(TacoCloudClient tacoCloudClient) {
		this.tacoCloudClient = tacoCloudClient;
	}

	@GetMapping("/test1")
	public Ingredient test1(String ingredientId) {
		return tacoCloudClient.getIngredientById(ingredientId);
	}

	@GetMapping("/test2")
	public Ingredient test2(String ingredientId) {
		return tacoCloudClient.getIngredientById2(ingredientId);
	}

	@GetMapping("/test3")
	public Ingredient test3(String ingredientId) {
		return tacoCloudClient.getIngredientById3(ingredientId);
	}

	@GetMapping("/test4")
	public Ingredient test4(String ingredientId) {
		return tacoCloudClient.getIngredientById4(ingredientId);
	}

}
