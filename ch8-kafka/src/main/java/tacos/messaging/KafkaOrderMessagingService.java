package tacos.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tacos.domain.Order;

@Service
public class KafkaOrderMessagingService implements OrderMessagingService {

	private final KafkaTemplate<String, String> kafkaTemplate;

	public KafkaOrderMessagingService(KafkaTemplate<String, String> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void sendOrder(Order order) {
		kafkaTemplate.send("test", order.getDeliveryName());
//		kafkaTemplate.sendDefault(order);
	}

}
