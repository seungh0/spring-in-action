# 6. REST 서비스 생성하기

- 최근에는 모바일 장치, 태블릿, 스마트 워치, 음성 기반 장치들이 흔히 사용된다.
- 또한 웹 브라우저 기반의 애플리케이션조차도 서버 위주로 싫행되기보다는 프로세서가 있는 클라이언트에서 자바스크립트 애플리케이션으로 많이 실행된다.
- 이처럼 클라이언트 측에서 다양한 선택을 할 수 있으므로, 많은 애플리케이션이 클라이언트에 더 다가갈 수 있는 사용자 인터페이스 설계를 적용하고 있음.
- 또한 모든 종류의 클라이언트가 백엔드 기능과 상호작용할 수 있게 서버는 클라이언트가 필요로 하는 API를 제공.

## REST 컨트롤러 작성

```java

@RestController
@RequestMapping(path = "/design", produces = "application/json")
@CrossOrigin(origins = "*")
public class DesignTacoController {

	private final TacoRepository tacoRepository;

	public DesignTacoController(TacoRepository tacoRepository) {
		this.tacoRepository = tacoRepository;
	}

	@GetMapping("/recent")
	public Iterable<Taco> recentTacos() {
		PageRequest page = PageRequest.of(0, 12, Sort.by("createdAt").descending());
		return tacoRepository.findAll(page).getContent();
	}

}
```

### @RestController

- @Controller, @Service와 같이 스테레오타입 애노테이션으로, 이 애노테이션이 지정된 클래스를 스프링의 컴포넌트 검색으로 찾을 수 있다.
- @Controller(뷰로 보여줄 값을 반환)와 다르게 컨트롤러의 모든 HTTP 요청 처리 메소드에서 HTTP body에 직접 쓰는 값을 반환한다는 것을 스프링에게 알려줌.

### @CrossOrigin

- 클라이언트는 API와 별도의 도메인(호스트와 포트 중 하나라도 다른)에서 실행 중이므로 클라이언트에서 API를 사용하지 못하도록 브라우저가 막는다.
- 이러 제약은 서버 응답에 CORS 헤더를 포함시켜 극복할 수 있음.
- @CrossOrigin은 다른 도메인의 클라이언트에서 해당 REST API를 사용할 수 있게 해주는 어노테이션.

```java

@RestController
@RequestMapping(path = "/design", produces = "application/json")
@CrossOrigin(origins = "*")
public class DesignTacoController {

	private final TacoRepository tacoRepository;

	public DesignTacoController(TacoRepository tacoRepository) {
		this.tacoRepository = tacoRepository;
	}
	
	...

	@GetMapping("/{id}") // GET: http://localhost:8080/design/4
	public ResponseEntity<Taco> findTacoById(@PathVariable("id") Long id) {
		Optional<Taco> optionalTaco = tacoRepository.findById(id);
		if (optionalTaco.isPresent()) {
			return new ResponseEntity<>(optionalTaco.get(), HttpStatus.OK);
		}
		return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
	}

	@PostMapping(consumes = "application/json") // Content-Type: application/json와 일치하는 요청만 처리.
	@ResponseStatus(HttpStatus.CREATED)
	public Taco postTaco(@RequestBody Taco taco) {
		return tacoRepository.save(taco);
	}

}
```

### PUT vs PATCH

- PUT
    - 데이터를 변경하는 데 사용되기는 하지만, 실제로는 GET과 반대의 의미를 갖는다.
    - 즉, GET 요청은 서버로 부터 클라이언트로 데이터를 전송하는 반면, PUT 요청은 클라이언트로부터 서버로 데이터를 전송한다.
    - 이런 관점에서 PUT은 데이터 전체를 교체하는 것.
- PATCH
    - 데이터의 일부분을 변경하는 것.

```java

@RestController
@RequestMapping(path = "/orders", produces = "application/json")
@CrossOrigin(origins = "*")
public class OrderApiController {
	
	...

	@PutMapping(path = "/{orderId}", consumes = "application/json")
	public Order putOrder(@RequestBody Order order) {
		return repo.save(order);
	}

	@PatchMapping(path = "/{orderId}", consumes = "application/json")
	public Order patchOrder(@PathVariable("orderId") Long orderId, @RequestBody Order patch) {
		Order order = repo.findById(orderId).get();
		if (patch.getDeliveryName() != null) {
			order.setDeliveryName(patch.getDeliveryName());
		}
		if (patch.getDeliveryStreet() != null) {
			order.setDeliveryStreet(patch.getDeliveryStreet());
		}
		...
		return repo.save(order);
	}

}
```

### PATCH 를 하는 방법은 여러 가지가 있다.

- patchOrder() 메소드에 적용하는 방법은 두가지 제약을 갖는다.
    - 만일 특정 필드의 데이터를 변경하지 않는다는 것을 나타내기 위해 null 값이 사용된다면 해당 필드를 null로 변경하고 싶을 때 클라이언트에서 이를 나타낼 수 있는 방법이 필요하다.
    - 컬렉션에 저장된 항목을 삭제 혹은 추가할 방법이 없다. 따라서 클라이언트가 컬렉션의 항목을 삭제 혹은 추가하려면 변경될 컬렉션 데이터 저체를 전송해야 한다.

- PATCH 요청을 처리하는 방법이나 수신 데이터의 형식에 관해 반드시 지켜야 할 규칙은 없다. 따라서 클라이언트는 실제 도메인 데이터를 전송하는 대신 PATCH에 적용할 변경사항 명세를 전송할 수 있다.
- 이때 도메인 데이터 대신 PATCH 명세를 처리하도록 요청 처리 메소드가 작성되어야 한다.

## 하이퍼 미디어 사용

### HATEOAS

- 기본적인 API에서는 해당 API를 사용하는 클라이언트가 API의 URL Scheme을 알아야 한다.
- API 클라이언트 코드에서는 하드코딩된 URL 패턴을 사용하고 문자욜로 처리한다. 그러나 API의 URL 스킴이 변경되면 어떻게 될까?
- REST API를 구현하는 다른 방법은 HATEOAS(HyperMedia As the Engine Of Application State)가 있다.
- API로 부터 반환되는 리소스에 해당 리소스와 관련된 하이퍼링크들이 포함된다. 따라서 클라이언트가 최소한의 API URL만 알면 반환되는 리소스와 관련하여 처리 가능한 다른 API URL들을 알아내어 사용할 수
  있다.

```json
{
  "content": "Hello, World!",
  "_links": {
    "self": {
      "href": "http://localhost:8080/greeting?name=World"
      // JSON 응답에 하이퍼링크를 포함
    }
  }
}
```

- 이런 형태의 HATEOAS를 HAL이라고 한다. 이것은 JSON응답에 하이퍼링크를 포함시킬 때 주로 사용되는 형식.
- 각 요소는 _links라는 속성을 포함하는데, 이 속성은 클라이언트가 관련 API를 수행할 수 있는 하이퍼링크를 포함한다.

### Spring Data REST

- 스프링 데이터에는 애플리케이션의 API를 정의하는 데 도움을 줄 수 있는 기능도 있다.
- 스프링 데이터 REST는 스프링 데이터의 또 다른 모듈이며, 스프링 데이터가 생성하는 Repository의 REST API를 자동 생성한다.
- 따라서 스프링 데이터 REST를 우리 빌드에 추가하면 우리가 정의한 각 Repository 인터페이스를 사용하는 API를 얻을 수 있다.

### 정리

- REST 엔드포인트는 스프링 MVC, 그리고 브라우저 지향의 컨트롤러와 동일한 프로그래밍 모델을 따르는 컨트롤러로 생성할 수 있다.
- 모델과 뷰를 거치지 않고 요청 응답 몸체에 직접 데이터를 쓰기 위해 컨트롤러의 핸들러 메소드에는 @ResponseBody 어노테이션을 지정할 수 있으며, ResponseEntity 객체를 반환할 수 있다.
- @RestController 어노테이션을 컨트롤러에 지정하면 해당 컨트롤러의 각 핸들러 메소드에 @ResponseBody를 지정하지 않아도 되므로 컨트롤러를 단순화 해준다.
- 스프링 HATEOAS는 스프링 MVC에서 반환되는 리소스의 하이퍼링크를 추가할 수 있게 한다.
- 스프링 데이터 Repository는 스프링 데이터 REST를 사용하는 REST API로 자동 노출될 수 있다.