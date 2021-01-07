# 5. 구성 속성 사용하기

- 스프링 부트의 자동-구성(autoConfiguration)은 스프링 애플리케이션 개발을 단순화 한다.
- 스프링 XML 구성으로 속성 값을 설정하던 지난 10년간은 명시적으로 빈을 구성하지 않고서는 속성을 설정하는 마땅한 방법이 없었다.
- 하지만, 스프링 부트는 구성 속성을 사용하는 방법을 제공한다.
- 스프링 애플리케이션 컨텍스트에서 구성 속성은 빈의 속성이다.
- JVM 시스템 속성, 명령행 인자, 환경 변수 등의 여러 가지 원천 속성 중에서 설정할 수 있다.

## 5-1. 자동-구성 세부 조정하기

스프링에는 두 가지 형태의 서로 구성 방법이 존재.

- 빈 연결
    - 빈으로 생성되는 애플리케이션 컴포넌트 및 상호 간에 주입되는 방법을 선언하는 구성
- 속성 주입
    - 빈의 속성 값을 설정하는 구성

### 스프링 환경 추상화 이해하기

- 구성 가능한 모든 속성을 한 곳에서 관리하는 개념. 
- 속성을 근원을 추상화하여 각 속성을 필요로 하는 빈이 스프링 자체에서 해당 속성을 사용할 수 있게 해줌

스프링 환경에서는 다음과 같은 속성을 근원으로부터 원천 속성을 가져옴.

- JVM 시스템 속성
- 운영체제의 환경 변수
- 명령행 인자
- 애플리케이션의 속성 구성 파일

그런 다음에 스프링 환경에서는 이 속성들을 한 군데로 모은 후 각 속성이 주입되는 스프링 빈을 사용할 수 있게 해줌.
(스프링 환경에서는 원천 속성들을 가져와서 애플리케이션 컨텍스트의 빈이 사용할 수 있게 해줌)

### ex1) 데이터 소스 구성하기

- 스프링 부트 사용시에는 Datasource 빈을 명시적으로 구성할 필요가 없으며, 구성 속성을 통해 데이터베이스의 URL과 인증을 구성하는 것이 간단.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost/tacocloud
    username: tacodb
    password: tacopassword
    schema:
      - data.sql
      - schema.sql
```

### ex2) 내장 서버 구성하기

- 서블릿 컨테이너의 포트를 설정
- server.port가 0으로 설정되면, 사용 가능한 포트를 무작위로 선택하여 시작. (자동화된 통합 테스트를 실행할 때 유용하며, 마이크로서비스와 같이 시작 포트가 중요하지 않을 때도 유용)

```yaml
server:
  port: 0 # 랜덤 포트
```

### ex3) 로깅 구성하기 (logback.xml)

- 기본적으로 스프링 부트는 INFO 수준으로 콘솔에 로그 메시지를 쓰기 위해 Logback을 통해 로깅을 구성한다.
- 로깅 구성을 제어할 때는 classpath의 루트에 logback.xml 파일을 생성할 수 있다.

```xml

<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>
                %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
            </pattern>
        </encoder>
    </appender>
    <logger name="root" level="INFO"/>
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>

```

## 5-2. 우리의 구성 속성 생성하기

- 구성 속성은 빈의 속성일 뿐이며, 스프링 환경 추상화로부터 여러가지 구성을 받기 위해 설계되었다.
- 구성 속성의 올바른 주입을 지원하기 위해 스프링 부트는 @ConfigurationProperties 애노테이션을 제공한다.
- 그리고 어떤 스프링 빈이건 이 애노테이션이 지정되면, 해당 빈의 속성들이 스프링 환경의 속성으로부터 주입될 수 있다.

```java

@Component
@ConfigurationProperties(prefix = "taco.orders")
@Data
public class OrderProps {

	@Min(value = 5, message = "must be between 5 and 25")
	@Max(value = 25, message = "must be between 5 and 25")
	private int pageSize = 20;

}

```

```java

@Slf4j
@Controller
@RequestMapping("/orders")
@SessionAttributes("order")
public class OrderController {

	...

	private final OrderProps orderProps;

	public OrderController(...,OrderProps orderProps) {
		...
		this.orderProps = orderProps;
	}
	...

	@GetMapping
	public String ordersForUser(@AuthenticationPrincipal User user, Model model) {
		Pageable pageable = PageRequest.of(0, orderProps.getPageSize());
		model.addAttribute("orders", orderRepository.findByUserOrderByPlacedAtDesc(user, pageable));
		return "orderList";
	}

}
```

### application.yml

```yml
taco:
  orders:
    pageSize: 10
```

### spring-boot-configuration-processor (스프링 부트 구성 처리기) 의존성을 pom.xml에 추가.

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
```

- @ConfigurationProperties 애노테이션이 지정된 애플리케이션 클래스에 관한 메타데이터를 생성하는 애노테이션 처리기.
- 생성된 메타데이터는 application.yml, application.properties를 작성할 때 자동-완성 기능 제공 및 속성의 문서를 보여주기 이해 IDE에서 사용.

### 메타 데이터 추가

src/main/resources/META_INF/additaional-spring-configuration-metadata.json

```json
{
  "properties": [
    {
      "name": "taco.orders.pageSize",
      "type": "java.lang.Integer",
      "description": "Sets the max number of orders to display in a list."
    }
  ]
}
```

## 5-3. 프로파일 사용해서 구성하기

- 애플리케이션이 서로 다른 런타임 환경에 배포, 설치될 때는 대개 구성 명세가 달라짐.

- 예를 들어, 데이터베이스 연결 명세가 개발환경과 배포환경에서 다를 것

- 이때 스프링 프로파일 설정으로 런타임 시에 활성화 되는 프로파일에 따라 서로 다른 빈, 구성 클래스, 구성 속성들이 적용 또는 무시되도록 할 수 있음.

### 프로파일 특정 속성 정의하기

#### 방법1

- application-{프로파일 이름}.yml 혹은 application-{프로파일 이름}.properties 파일 생성.
- 해당 프로파일에 적합한 구성 속성들을 지정할 수 있음.

### ex) application-prod.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost/tacocloud
    username: taco
    password: taco
logging:
  level:
    tacos: WARN
```

#### 방법2

- YAML 구성에서만 가능한 방법으로, 프로파일 특정 속성을 정의할 수 도 있음.
- 이떄는 프로파일에 특정되지 않고 공통으로 적용되는 기본 속성과 함께 프로파일 특정 속성을 application.yml에 지정할 수 있음.

```yaml
# 모든 프로파일에 공통으로 적용
taco:
  orders:
    pageSize: 10
logging:
  level: INFO

---
# Prod 프로파일에만 적용됨
spring:
  profiles: prod
  datasource:
    url: jdbc:mysql://localhost/tacocloud
    username: taco
    password: taco
logging:
  level: DEBUG


```

### 프로파일 활성화하기

#### 방법1: spring.profiles.active 속성에 지정

```yaml
spring:
  profiles:
    active:
      - prod
      - audit
```

- 가장 좋지 않은 프로파일 활성화 방버
- 프로덕션 환경 특정 속성을 개발 속성과 분리시키기 위해 프로파일을 사용하는 장점을 전혀 살릴 수 없음.
- 이 방법 대신 환경 변수를 사용해서 활성화 프로파일을 설정하는 것을 권장.

#### 방법2

```shell
% export SPRING_PROFILES_ACTIVE=prod 

% export SPRING_PROFILES_ACTION=prod,audit
```

```shell
java -jar taco-cloud.jar --spring.profiles.active=prod
```

### 프로파일을 사용해서 조건별로 빈 생성하기

- 서로 다른 프로파일 각각에 적합한 빈들을 제공하는 것이 유용할 때가 있다.
- 일반적으로 자바 구성 클래스에 선언된 빈은 활성화되는 프로파일과 무관하게 생성되지만, 특정 프로파일이 활성화 될 때만 생성되어야 하는 빈들이 있다고 해보자.
- 이 경우 @Profile 애노테이션을 사용하면 지정된 프로파일에만 적합한 빈들을 나타낼 수 있다.

```java

@SpringBootApplication
public class TacoCloudApplication {

	public static void main(String[] args) {
		SpringApplication.run(TacoCloudApplication.class, args);
	}

	@Bean
	@Profile("dev") // dev 프로파일이 활성화 되었을때
//	@Profile({"dev", "qa"}) // dev, qa 프로파일 중 하나가 활성화 되었을때
//	Profile("!prod") // prod 프로파일이 활성화 되지 않을때
	public CommandLineRunner dataLoader(IngredientRepository repo) {
		return new CommandLineRunner() {
			@Override
			public void run(String... args) throws Exception {
				repo.save(new Ingredient("FLTO", "Flour Tortilla", Type.WRAP));
				repo.save(new Ingredient("COTO", "Corn Tortilla", Type.WRAP));
			}
		};
	}

}

```

### 정리

- 스프링 빈에 @ConfigurationProperties를 지정하면 여러 가지 원천 속성으로부터 구성 속성 값의 주입을 활성화 할 수 있다.
- 구성 속성은 명령행 인자, 환경 변수, JVM 시스템 속성, 속성 파일, YAML 파일, 커스텀 속성 등에서 설정할 수 있다.
- 데이터 소스 URL과 로깅 수준의 지정을 포함해서 구성 속성은 스프링의 자동-구성 설정을 변경하는 데 사용할 수 있음.
- 스프링 프로파일은 활성화된 프로파일을 기반으로 구성 속성을 설정하기 위해 사용할 수 있다.