package tacos.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tacos.domain.Ingredient;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TacoCloudClient {

	private final RestTemplate restTemplate;

	public TacoCloudClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	/**
	 * getForObject()의 두번째 매개변수는 응답이 바인딩 되는 타입이다.
	 * 여기서는 JSON 형식인 응답 데이터가 객체로 역직렬화되시ㅓ 반환된다.
	 */
	public Ingredient getIngredientById(String ingredientId) {
		return restTemplate.getForObject("http://localhost:8080/ingredients/{id}", Ingredient.class, ingredientId);
	}

	/**
	 * Map을 사용해서 URL 변수들을 지정할 수 있다.
	 */
	public Ingredient getIngredientById2(String ingredientId) {
		Map<String, String> urlVariables = new HashMap<>();
		urlVariables.put("id", ingredientId);
		return restTemplate.getForObject("http://localhost:8080/ingredients/{id}", Ingredient.class, urlVariables);
	}

	/**
	 * URI 매개변수를 사용할 때는 URI 객체를 구성하여 getForObject()를 호출해야한다.
	 */
	public Ingredient getIngredientById3(String ingredientId) {
		Map<String, String> urlVar = new HashMap<>();
		urlVar.put("id", ingredientId);
		URI uri = UriComponentsBuilder
				.fromHttpUrl("http://localhost:8080/ingredients/{id}")
				.build(urlVar);
		return restTemplate.getForObject(uri, Ingredient.class);
	}

	/**
	 * 응답의 Date헤더를 확인하고 싶은 경우
	 */
	public Ingredient getIngredientById4(String ingredientId) {
		ResponseEntity<Ingredient> responseEntity = restTemplate.getForEntity("http://localhost:8080/ingredients/{id}", Ingredient.class, ingredientId);
		log.info("Fetched Time: " + responseEntity.getHeaders().getDate());
		return responseEntity.getBody();
	}

	public void updateIngredient(Ingredient ingredient) {
		restTemplate.put("http://localhost:8080/ingredients/{id}", ingredient, ingredient.getId());
	}

	public void deleteIngredient(Ingredient ingredient) {
		restTemplate.delete("http://localhost:8080/ingredients/{id}", ingredient.getId());
	}

	public Ingredient createIngredient(Ingredient ingredient) {
		return restTemplate.postForObject("http://localhost:8080/ingredients", ingredient, Ingredient.class);
	}

}
