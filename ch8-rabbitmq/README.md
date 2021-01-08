# 8. 비동기 메시지 전송하기

## RabbitMQ로 메시지 전송하기

- AMQP의 가장 중요한 구현이라 할 수 있는 RabbitMQ는 JMS보다 더 진보된 메시지 라우팅 전략을 제공.
- JMS 메시지가 수신자가 가져갈 메시지 도착지의 이름을 주소로 사용하는 반면, AMQP 메시지는 수신자가 리스닝하는 큐와 분리된 거래소 이름과 라우팅 키를 주소로 사용 함.

<img src="https://blog.kakaocdn.net/dn/qmw6t/btqtYLVwSxJ/S0khkY8PFruLvgBoJJnfyk/img.png">

- 메시지가 RabbitMQ 브로커에 도착하면 주소로 지정된 거래소로 들어간다.
- 거래소는 하나 이상의 큐에 메시지를 전달할 책임이 있다.

### 거래소 종류

- Default
    - 브로커가 자동으로 생성하는 특별한 거래소. 해당 메시지의 라우팅 키와 이름이 같은 큐로 메시지를 전달. 모든 큐는 자동으로 기본 거래소와 연결
- Direct
    - 바인딩 키가 해당 메시지의 라우팅 키와 같은 큐에 메시지를 전달.
- Topic
    - 바인딩 키와 해당 메시지의 라우팅 키와 일치하는 하나 이상의 큐에 메시지를 전달
- Fanout
    - 바인딩 키나 라우팅 키에 상관없이 모든 연결된 큐에 메시지를 전달
- Header
    - 토픽 거래소와 유사하며, 라우팅 키 대신 메시지 헤더 값을 기반으로 한다는 것만 다름
- Dead letter
    - 전달 불가능 모든 메시지를 보관하는 거래소

### RabbmitMQ를 스프링에 추가하기.

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

- AMQP 스타터를 빌드에 추가하면 다른 지원 컴포넌트는 물론이고 AMQP 연결 팩토리와 RabbitTemplate 빈을 생성하는 자동-구성이 수행됨.
- 따라서 스프링을 사용해서 RabbitMQ 브로커로부터 메시지를 전송 및 수신할 수 있다.

### RabbitMQ 브로커의 위치와 인증 정보를 구성하는 속성

```yaml
spring:
  profiles: prod
  rabbitmq:
    host: rabbit.tacocloud.com
    port: 5673
    username: tacoweb
    password: tacoweb
```

### RabbitTemplate을 사용해서 메시지 전송하기

```java
public interface OrderMessagingService {

	void sendOrder(Order order);

}
```

#### 방법1) send()

```java

@Service
public class RabbitOrderMessagingService implements OrderMessagingService {

	private final RabbitTemplate rabbitTemplate;

	public RabbitOrderMessagingService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void sendOrder(Order order) {
		MessageConverter converter = rabbitTemplate.getMessageConverter();
		MessageProperties properties = new MessageProperties();
		Message message = converter.toMessage(order, properties);
		rabbitTemplate.send("tacocloud.orders", message);
	}

}

```

#### 방법2) convertAndSend()

- 모든 변환 작업을 RabbitTemplate이 처리하도록 convertAndSend()를 사용할 수 있다.

```java

@Service
public class RabbitOrderMessagingService implements OrderMessagingService {

	private final RabbitTemplate rabbitTemplate;

	public RabbitOrderMessagingService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void sendOrder(Order order) {
		rabbitTemplate.convertAndSend("tacocloud.orders", order);
	}

}

```

### 메시지 변환기 구성하기

기본적인 메시지 변환은 SimpleMessageConverter로 수행되며, 이것은 String 같은 간단한 타입과 Serializable 객체를 Message 객체로 변환할 수 있다. 그러나 스프링은
RabbitTemplate에서 사용할 수 있는 다양한 메시지 변환기를 제공함.

- Jackson2JsonMessageConverter 객체를 JSON으로 상호 변환
- MarshallingMessageConverter
    - 스프링 Marshaller와 Unmarshaller를 사용해서 변환
- SerializerMessageConverter
    - 스프링의 Serializer, Deserializer를 사용해서 String 객체를 변환.
- SimpleMessageConverter
    - String, byte 배열, Serializable 타입을 변환.
- ContentTypeDelegatingMessageConverter
    - contentType 헤더를 기반으로 다른 메시지 변환기에 변환을 위임한다.

```java

@Configuration
public class MessagingConfig {

	@Bean
	public Jackson2JsonMessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

}

```

- JSON 기반 메시지 변환.
- 스프링 부트 자동-구성에서 이 빈을 찾아서 기본 메시지 변환기 대신 이 빈을 RabbitTemplate으로 주입한다.

### 메시지 속성 설정하기

- 메시지의 일부 헤더를 설정해야 하는 경우.
- Message 객체를 생성할 때 메시지 변환기에서 제공하는 MessageProperties 인스턴스를 통해 헤더를 설정할 수 있다.

#### 방법1) send()

```java

@Service
public class RabbitOrderMessagingService implements OrderMessagingService {

	private final RabbitTemplate rabbitTemplate;

	public RabbitOrderMessagingService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void sendOrder(Order order) {
		MessageConverter converter = rabbitTemplate.getMessageConverter();
		MessageProperties properties = new MessageProperties();
		properties.setHeader("X_ORDER_SOURCE", "WEB"); // 헤더를 설정
		Message message = converter.toMessage(order, properties);
		rabbitTemplate.send("tacocloud.orders", message);
	}

}

```

#### 방법2) convertAndSend()

```java

@Service
public class RabbitOrderMessagingService implements OrderMessagingService {

	private final RabbitTemplate rabbitTemplate;

	public RabbitOrderMessagingService(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void sendOrder(Order order) {
		// 모든 변환 작업을 RabbitTemplate이 처리하도록 convertAndSend()를 사용할 수 있다.
		rabbitTemplate.convertAndSend("tacocloud.orders", order, new MessagePostProcessor() {
			@Override
			public Message postProcessMessage(Message message) throws AmqpException {
				MessageProperties properties = message.getMessageProperties();
				properties.setHeader("X_ORDER_SOURCE", "WEB");
				return message;
			}
		});
	}

}
```

### RabbitMQ로부터 메시지 수신하기

- Pull 모델: RabbitTemplate을 사용해서 큐로부터 메시지를 가져온다.
- Push 모델: @RabbitListener가 지정된 메서드로부터 메시지가 푸시 된다.

```java

@Component
public class RabbitOrderReceiver {

	private final RabbitTemplate rabbitTemplate;

	public RabbitOrderReceiver(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	// 방법1
	public Order receiveOrder() {
		return rabbitTemplate.receiveAndConvert("tacocloud.orders", new ParameterizedTypeReference<Order>() {
		});
	}

	// 방법2
	public Order receiveOrder() {
		return (Order) rabbitTemplate.receiveAndConvert("tacocloud.orders");
	}

	// 방법3
	public Order receiveOrder() {
		Message message = rabbitTemplate.receive("tacocloud.orders");
		return message != null ? (Order) messageConverter.fromMessage(message) : null;
	}

}

```

#### 타임아웃 설정하기

```yaml
spring:
  rabbitmq:
    template:
      receive-timeout: 30000 # timeout

```

### 리스너를 사용해서 RabbitMQ 메시지 처리하기

```java

@Slf4j
@Component
public class OrderListener {

	@RabbitListener(queues = "tacocloud.orders")
	public void receiveOrder(Order order) {
		log.info(order.toString() + " send");
	}

}

```