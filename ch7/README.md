# 7. REST 서비스 사용하기

- 스프링 애플리케이션에서 API를 제공하면서 다른 애플리케이션의 API를 요청.
- 마이크로서비스에서는 REST API를 많이 사용.

- RestTemplate
    - 스프링 프레임워크에서 제공하는 간단하고 동기화된 REST 클라이언트.
- Traverson
    - 스프링 HATEOAS에서 제공하는 하이퍼링크를 인식하는 동기화 REST 클라이언트로 같은 이름의 자바스크립트 라이브러리로부터 비롯.
- WebClient
    - 스프링 5에서 소개된 반응형 비동기 REST 클라이언트

## RestTemplate으로 REST 엔드포인트 사용.

- 클라이언트 입장에서 REST 리소스와 상호작용하려면 해야 할 일이 많아서 코드가 장황해진다.
- 저수준의 HTTP 라이브러리로 작업하면서 클라이언트는 클라이언트 인스턴스와 요청 객체를 생성하고, 해당 요청을 실행하고, 응답을 분석해 관련 되메인 객체와 연관시켜 처리해야 함.
- 또한 발생될 수 있는 예외도 처리해야 함.

- 이러한 장황한 코드를 피하기 위해 스프링은 RestTemplate을 제공.
- RestTemplate은 REST 리소스를 사용하는 데 번잡한 일을 처리해 준다.

### GET

```java

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

}

```

### PUT, POST, DELETE
```java
@Slf4j
@Service
public class TacoCloudClient {

	private final RestTemplate restTemplate;

	public TacoCloudClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
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

```

## Traverson으로 REST API 사용하기
- Traverson은 스프링 데이터 HATEOAS와 같이 제공되며, 스프링 애플리케이션에서 하이퍼미디어 API를 사용할 수 있는 솔루션.
