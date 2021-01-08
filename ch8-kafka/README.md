# 8. 비동기 메시지 전송하기

## Kafka 사용하기

- 아파치 카프카는 가장 새로운 메시징 시스템이며 ActiveMQ, Artemis, RabbitMQ와 유사한 메시지 브로커이다.
- 그러나 카프카는 특유의 아키텍처를 가짐.

- 카프카는 높은 확장성을 제공하는 클러스터로 실행되도록 설계되었음.
- 클러스터의 모든 카프카 인스턴스에 걸쳐 토픽을 파티션으로 분할하여, 메시지를 관리.
- RabbitMQ가 거래소와 큐를 사용해서 메시지를 처리하는 반면, 카프카는 토픽만 사용.

- 카프카의 토픽은 모든 브로커에 걸쳐 복제된다.
- 클러스터의 각 노드는 하나 이상의 토픽에 대한 리더로 동작하며, 토픽 데이터를 관리하고 클러스터의 다른 노드로 데이터를 복제한다.

<img src="https://blog.kakaocdn.net/dn/d9ciLL/btquhiLfozP/RnFkbiCjYme9be9FlOTH3K/img.png">
- 카프카 클러스터는 여래 개의 브로커로 구성되며, 각 브로커는 토픽의 파티션의 리더로 동작.
- 각 토픽은 여러 개의 파티션으로 분할될 수 있따. 이 경우 클러스터의 각 노드는 한 토픽의 하나 이상의 파티션의 리더가 됨.

### 카프카 사용을 위해 스프링 설정하기

```xml

<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

- 의존성을 추가하면 스프링 부트가 카프카 사용을 위한 자동-구성을 해준다. (KafkaTemplate을 준비함)
- 우리는 KafkaTemplate을 주입하고 메시지를 전송, 수신하면 된다.

### 카프카 클러스터 설정

```yaml
spring:
  kafka:
    bootstrap-servers:
      - localhost:9092
    default-topic: test # 기본 토픽을 설정할 필요 있을 경

---
spring:
  profiles: prod
  kafka:
    bootstrap-servers:
      - kafka.tacocloud.com:9092
      - kafka.tacocloud.com:9093
      - kafka.tacocloud.com:9093 # 클러스터의 여러 서버를 지정할 수 있음.
```

```shell
bin/zookeeper-server-start.sh config/zookeeper.properties # 주키버 서버 실행 
bin/kafka-server-start.sh config/server.properties # 카프카 브로커 실행
bin/kafka-topics.sh -create -zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test # 토픽(test) 생성
```

### KafkaTemplate을 사용해서 메시지 전송하기

```java

@Service
public class KafkaOrderMessagingService implements OrderMessagingService {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaOrderMessagingService(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void sendOrder(Order order) {
		kafkaTemplate.send("test", order.getDeliveryName());
//		kafkaTemplate.sendDefault(order.getDeliveryName()); // Default 토픽을 설정하고 보내는 경우
	}

}

```

### 카프카 리스너 작성하기

- 카프카는 send(), sendDefault() 특유의 메소드 시그니처 외에도 KafkaTemplate은 메시지를 수신하는 메서드를 일체 제공하지 않음.
- 따라서 스프링을 사용해서 카프카 토픽 메시지를 가져오는 유일한 방법은 메시지 리스너를 작성하는 것.

```java

@Slf4j
@Component
public class OrderListener {

	@KafkaListener(topics = "test")
	public void handle(String message) {
		log.info("message is receive" + message);
	}

}
```

#### 수신된 메시지의 파티션과 타임스탬프를 가져올 수 있다.

```java

@Slf4j
@Component
public class OrderListener {

	@KafkaListener(topics = "test", groupId = "test")
	public void handle(String message, ConsumerRecord<String, String> record) {
		log.info("Received message {} from partition {} with timestamp {}", message, record.partition(), record.timestamp());
	}

}

```

### 정리

- 애플리케이션 간 비동기 메시지 큐를 이용한 통신 방식은 간접 계층을 제공하므로 애플리케이션 간의 결합도는 낮추면서 확장성은 높임.
- 스프링은 JMS, RabbitMQ 또는 아파치 카프카를 사용해서 비동기 메시징을 지원.
- 스프링 애플리케이션은 템플릿 기반의 클라이언트인 JmsTemplate, RabbitTemplate, KafkaTemplate을 사용해서 메시지 브로커를 통한 메시지 전송을 할 수 있다.
- 메시지 수신 애플리케이션은 같은 템플릿 기반의 클라이언트들을 사용해서 풀 모델 형태의 메시지 소비를 할 수 있다. (카프카 제외)
- 메시지 리스너 애플리케이션을 빈 메소드에 지정하면 푸쉬 모델의 형태로 Consumer에게 메시지가 전송될 수 있다.