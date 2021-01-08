package tacos.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import tacos.domain.Order;

@Component
public class RabbitOrderReceiver {

	private final RabbitTemplate rabbitTemplate;

	public RabbitOrderReceiver(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public Order receiveOrder() {
		return rabbitTemplate.receiveAndConvert("tacocloud.orders", new ParameterizedTypeReference<Order>() {});
	}
//
//	public Order receiveOrder() {
//		return (Order) rabbitTemplate.receiveAndConvert("tacocloud.orders");
//	}

//	public Order receiveOrder() {
//		Message message = rabbitTemplate.receive("tacocloud.orders");
//		return message != null ? (Order) messageConverter.fromMessage(message) : null;
//	}

}
