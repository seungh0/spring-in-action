# 11. 리액티브 API 개발하기, 12. 리액티브 데이터 퍼시스턴스

## 동기 vs 비동기 웹 프레임워크

### 동기 웹 프레임워크

- 매 연결마다 하나의 스레드를 사용하는 스프링 MVC 같은 전형적인 서블릿 기반의 웹 프레임워크는 스레드 블로킹과 다중 스레드로 수행된다.
- 즉 요청이 처리될 때 스레드 풀에서 작업 스레드를 가져와서 해당 요청을 처리하며, 작업 스레드가 종료될 때 까지 요청 스레드는 블로킹된다.
- 따라서 블로킹 웹 프레임워크는 요청량의 증가에 따른 확장이 사실상 어렵다.
- 게다가 처리가 느린 작업 스레드로 인해 훨씬 더 심각한 상황이 발생한다.
- 해당 작업 스레드가 풀로 반환되어 또 다른 요청 처리를 준비하는 데 더 많은 시간이 걸리기 때문.

### 비동기 웹 프레임워크

- 비동기 웹 프레임워크는 더 적은 수의 스레드로 더 높은 확장성을 성취한다.
- 이벤트 루핑이라는 기법을 적용한 이런 프레임워크는 한 스레드 당 많은 요청을 처리할 수 있어서 한 연결당 소요 비용이 더 경제적임.
- 데이터베이스나 네트워크 작업과 같은 집중적인 작업의 콜백과 요청을 비롯해서, 이벤트 루프에서는 모든 것이 이벤트로 처리된다. 비용이 드는 작업이 필요할 때 이벤트 루프는 해당 작업의 콜백을 등록하여 병행으로
  수행되게 하고 다른 이벤트 처리로 넘어간다. 그리고 작업이 완료될 떄 이것 역시 요청과 동일하게 이벤트로 처리된다.
- 결과적으로 비동기 웹 프레임워크는 소수의 스레드로 많은 요청을 처리할 수 있어서 스레드 관리 부담이 줄어들고 확장이 용이하다.

## 스프링 WebFlux 개요

### 스프링 WebFlux 스타터 의존성 추가

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

- 스프링 MVC 대신 WebFlux를 사용할 때는 기본적인 내장 서버가 톰캣 대신 Netty가 된다.
- Netty는 몇 안되는 비동기적인 이벤트 중심의 서버 중 하나.

### 스프링 WebFlux vs 리액티브 스프링 MVC

- 스프링 MVC가 리액티브 타입(Mono, Flux)를 전혀 사용하지 못하는 것은 아니다.
- 차이점은 스프링 WebFlux는 요청이 이벤트 루프로 처리되는 진정한 리액티브 웹 프레임워크인 반면
- 스프링 MVC는 다중 스레드에 의존하여 다수의 요청을 처리하는 서블릿 기반 웹 프레임워크임.

```java
public interface TacoRepository extends ReactiveCrudRepository<Taco, Long> {

}

```

## 리액티브 컨트롤러 작성하기

end-to-end 리액티브 스택

```java

@RestController
public class DesignTacoController {

	private final TacoRepository tacoRepository;

	public DesignTacoController(TacoRepository tacoRepository) {
		this.tacoRepository = tacoRepository;
	}

	@GetMapping("/recent")
	public Flux<Taco> recentTacos() {
		return tacoRepository.findAll().take(12);
	}

	@GetMapping("/{id}")
	public Mono<Taco> tacoById(@PathVariable("id") Long id) {
		return tacoRepository.findById(id);
	}

}
```

## 함수형 요청 핸들러 정의하기

- 스프링 MVC의 어노테이션 기반 프로그래밍 모델에는 몇가지 단점이 존재.
    - 어노테이션이 무엇을 하는지와 어떻게 해야 하는지를 정의하는 데 괴리가 있다.
    - 이로 인해 프로그래밍 모델을 커스터마이징하거나 확장할 때 복잡해진다.
    - 게다가 이런 코드의 디버깅은 까다롭다. (어노테이션 중단점을 설정할 수 없기 떄문)

- 따라서 WebFlux의 대안으로 스프링 5에서는 리액티브 API를 정의하기 위한 새로운 함수형 프로그래밍 모델이 소개되었다.

```java

@Configuration
public class RouterFunctionConfig {

	private final TacoRepository tacoRepository;

	public RouterFunctionConfig(TacoRepository tacoRepository) {
		this.tacoRepository = tacoRepository;
	}

	@Bean
	public RouterFunction<?> helloRouterFunction() {
		return route(GET("/hello"),
				request -> ok().body(just("Hello World"), String.class));
	}

	@Bean
	public RouterFunction<?> routerFunction() {
		return route(GET("/design/taco"), this::recent);
	}

	private Mono<ServerResponse> recent(ServerRequest request) {
		return ServerResponse.ok()
				.body(tacoRepository.findAll().take(12), Taco.class);
	}

}
```

## REST API를 리액티브하게 사용하기

- 기존의 RestTemplate이 제공하는 모든 메소드는 리액티브가 아닌 도메인 타입이나 컬렉션을 처리한다.
- 따라서 리액티브 방식으로 응답 데이터를 사용하고자 하면, 이것을 Flux나 Mono 타입으로 래핑해야 한다.
- 스프링 5에서는 RestTemplate의 리액티브 대안으로 WebClient를 제공.

```java
public class WebClientSample {

	public void getIngredientById(String id) {
		Mono<Ingredient> ingredientMono = WebClient.create()
				.get()
				.uri("http://localhost:8080/ingredients/{id}", id)
				.retrieve()
				.bodyToMono(Ingredient.class);
		ingredientMono.timeout(Duration.ofSeconds(1))
				.subscribe(System.out::println, System.out::println);
	}

	public void getIngredients() {
		Flux<Ingredient> ingredientFlux = WebClient.create()
				.get()
				.uri("http://localhost:8080/ingredients")
				.retrieve()
//				.onStatus(status -> status == HttpStatus.BAD_GATEWAY, response -> Mono.just(new IllegalStateException()))
				.bodyToFlux(Ingredient.class);
		ingredientFlux.timeout(Duration.ofSeconds(1))
				.subscribe(System.out::println, System.out::println);
	}

}
```

## 리액티브 몽고디비 Repository

### 리액티브 스프링 데이터 몽고DB 스타터 의존성 추가

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>

```

### 몽고DB 인메모리 의존성

```xml

<dependency>
    <groupId>de.flapdoodle.embed</groupId>
    <artifactId>de.flapdoodle.embed.mongo</artifactId>
</dependency>
```

### 몽고디비 속성 설정

```yaml
spring:
  data:
    mongodb:
      host: mongodb.tacocloud.com
      port: 27018
      username: tacocloud
      password: taco
```

### 데이터 모델링 변경

```java

@Data
@NoArgsConstructor
@Document // 몽고DB에 저장되거나 읽을 수 있는 문서 엔티티라는 것을 나타냄.
public class Ingredient {

	@Id
	private String id;

	private String name;

	private Type type;

	public Ingredient(String id, String name, Type type) {
		this.id = id;
		this.name = name;
		this.type = type;
	}

	public static enum Type {
		WRAP, PROTEIN, VEGGIES, CHEESE, SAUCE
	}

}

```

### 리액티브 레퍼지터리

```java
public interface IngredientRepository extends ReactiveCrudRepository<Ingredient, String> {

}

```

### 정리

- 스프링 WebFlux는 리애티브 웹 프레임워크를 제공한다.
- 스프링5는 또한 스프링 Webflux의 대안으로 함수형 프로그래밍 모델을 제공한다.
- 리액티브 컨트롤러는 WebTestClient를 사용해서 테스트할 수 있다.
- 클라이언트 측에서는 스프링 5가 스프링 RestTemplate의 리액티브 버전인 WebClient를 제공.
- 스프링 시큐리티5는 리액티브 보안을 지원하며, 이것의 프로그래밍 모델은 리액티브가 아닌 스프링 MVC 애플리케이션의 것과 크게 다르지 않다.

- 스프링 데이터는 카산드라, 몽고디비, 카우치베이스, 레디스의 리액티브 리퍼지터리를 지원.
- 스프링 데이터의 리액티브 리퍼지터리는 리액티브가 아닌 리퍼지터리와 동일한 프로그래밍 모델을 따름.
- JPA 리퍼지터리와 같은 리액티브가 아닌 리퍼지터리는 Mono나 Flux를 사용하도록 조정할 수 있다. 그러나 데이터를 가져오거나 저장할 때 여전히 블로킹이 생긴다.
- 관계형이 아닌 데이터베이스를 사용하려면 해당 데이터베이스에 데이터를 저장하는 방법에 맞게 데이터를 모델링하는 방법을 알아야 한다.