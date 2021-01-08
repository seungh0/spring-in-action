package tacos.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import tacos.domain.Order;

@Slf4j
@Service
public class JmsOrderMessagingService implements OrderMessagingService {

	private final JmsTemplate jmsTemplate;

	public JmsOrderMessagingService(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	public void sendOrder(Order order) {
		log.info("message send" + order.toString());
		jmsTemplate.convertAndSend("tacocloud.order.queue", order, (message) -> {
			message.setStringProperty("X_ORDER_SOURCE", "WEB");
			return message;
		});
	}

//	@Override
//	public void sendOrder(Order order) {
//		log.info("message send" + order.toString());
//		jmsTemplate.convertAndSend("tacocloud.order.queue", order);
//	}

}
