# 8. 비동기 메시지 전송하기

- REST를 사용한 동기화 통신이외에도 비동기 메시징을 통해 애플리케이션 간 통신할 수 있다.
- 비동기 메시징은 애플리케이션 간에 응답을 기다리지 않고 간접적으로 메시지를 전송하는 방법
- 비동기 메시징을 통해 통신하는 애플리케이션 간의 결합도를 낮추고 확장성을 높여준다.

### 비동기 메시징 종류

- JMS
- RabbitMQ
- AMQP
- 아파치 카프카
- EJB의 MDB(스프링의 메시지 기반 POJO 지원)

## JMS로 메시지 전송하기

- 스프링은 JmsTemplate이라는 템플릿 기반의 클래스를 통해 JMS를 지원한다.
- JmsTemplate을 사용하면 Producer가 큐와 토픽에 메시지를 전송하고 Consumer는 그 메시지들을 받을 수 있다.
- 또한, 스프링은 POJO도 지원한다. POJO는 큐나 토픽에 도착하는 메시지에 반응하여 비동기 방식으로 메시지를 수신하는 간단한 자바 객체이다.

### 메시지 브로커 설정

- ActiveMQ Artemis

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-artemis</artifactId>
</dependency>
```

### Artemis 브로커의 위치와 인증정보를 구성하는 속성

```yml
spring:
  artemis:
    host: artemis.tacocloud.com
    port: 61617
    user: tacoweb
    password: tacoweb
```

### JMSTemplate을 사용해서 메시지 전송하기

- JMS starter 의존성이 우리 빌드에 지정되면, 메시지를 송수신하기 위해 주입 및 사용할 수 있는 JMSTemplate을 스프링 부트가 자동-구성한다.
- JMSTemplate은 스프링 JMS 통합 지원의 핵심이다. 스프링의 다른 템플릿 기반 컴포넌트와 마찬가지로, JMS로 작업하는 데 필요한 코드를 줄여준다.
- JMSTemplate을 통해 개발자는 메시지 전송에만 집중할 수 있게 해준다.

### Destination Queue 빈 선언

```java

@SpringBootApplication
public class TacoCloudApplication {
	
	...

	@Bean
	public Destination orderQueue() {
		return new ActiveMQQueue("tacocloud.order.queue");
	}

}

```

### 데이터 전송하기

```java

@Slf4j
@Service
public class JmsOrderMessagingService implements OrderMessagingService {

	private final JmsTemplate jmsTemplate;
	private final Destination orderQueue;

	public JmsOrderMessagingService(JmsTemplate jmsTemplate, Destination orderQueue) {
		this.jmsTemplate = jmsTemplate;
		this.orderQueue = orderQueue;
	}

	@Override
	public void sendOrder(Order order) {
		jmsTemplate.send(orderQueue, (session) -> session.createObjectMessage(order));
	}

}

```

### 후처리 메시지

```java

@Slf4j
@Service
public class JmsOrderMessagingService implements OrderMessagingService {

	private final JmsTemplate jmsTemplate;
	private final Destination orderQueue;

	public JmsOrderMessagingService(JmsTemplate jmsTemplate, Destination orderQueue) {
		this.jmsTemplate = jmsTemplate;
		this.orderQueue = orderQueue;
	}

	@Override
	public void sendOrder(Order order) {
		jmsTemplate.convertAndSend(orderQueue, order, (message) -> {
			message.setStringProperty("X_ORDER_SOURCE", "WEB");
			return message;
		});
	}

}
```

### JMS 메시지 수신하기

메시지를 수신하는 방식

- 풀 모델
    - 우리 코드에서 메시지를 요청하고 도착할 때까지 기다리는 방식
- 푸시 모델
    - 메시지가 수신 가능하게 되면 우리 코드로 자동 전달하는 방식

- 두가지 모델 모두 용도에 맞게 사용할 수 있다. 그러나 스레드의 실행을 막지 않으므로 일반적으로 푸시 모델이 좋은 선택이다.
- 단 많은 메시지가 너무 빨리 도착한다면 리스너에 과부하가 걸리는 경우가 생길 수 있음.

### JmsTemplate의 모델

- JmsTemplate은 메시지를 수신하는 여러 개의 메소드를 제공하지만, 모두 풀 모델을 사용.
- 따라서 이 메소드를 호출해서 메시지를 요청하면 스레드에서 메시지를 수신할 수 있을때 까지 기다림.

### JmsTemplate을 사용해서 메시지 수신하기 (Pull 모델)

```java

@Component
public class JmsOrderReceiver implements OrderReceiver {

	private final Destination messageQueue;
	private final JmsTemplate jmsTemplate;

	public JmsOrderReceiver(Destination messageQueue, JmsTemplate jmsTemplate) {
		this.messageQueue = messageQueue;
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	public Order receiveOrder() {
		return (Order) jmsTemplate.receiveAndConvert(messageQueue);
	}

}
```

### 메시지 리스너 선언
- 메시지 리스너는 메시지가 도착할 때 까지 대기하는 수동적 컴포넌트.
```java
@Slf4j
@Component
public class OrderListener {

	@JmsListener(destination = "tacocloud.order.queue")
	public void receiveOrder(Order order) {
		log.info(order.toString() + " send message");
	}

}

```

- 메시지 리스너는 중단 없이 다수의 메시지를 빠르게 처리할 수 있어서 좋은 선택이 될 때가 있다.
- 그러나 메시지 처리기가 자신의 시간에 맞춰 더 많은 메시지를 요청할 수 있어야 한다면 JmsTemplate이 제공하는 풀 모델이 더 적합.

### JMS의 단점
- JMS가 자바 명세이므로 자바 애플리케이션에서만 사용할 수 있다는 것.
- RabbitMQ와 카프카 같은 더 새로운 메시징 시스템은 이러한 단점을 해결하여 다른 언어와 JVM 외의 다른 플랫폼에서 사용할 수 있다.