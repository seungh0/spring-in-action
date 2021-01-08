package tacos.messaging;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderListener {

	@KafkaListener(topics = "test", groupId = "test")
	public void handle(String message, ConsumerRecord<String, String> record) {
		log.info("Received message {} from partition {} with timestamp {}", message, record.partition(), record.timestamp());
	}

}
