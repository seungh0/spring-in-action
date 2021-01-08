package tacos.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Service;
import tacos.domain.Order;

@Slf4j
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

//	@Override
//	public void sendOrder(Order order) {
//		MessageConverter converter = rabbitTemplate.getMessageConverter();
//		MessageProperties properties = new MessageProperties();
//		properties.setHeader("X_ORDER_SOURCE", "WEB"); // 헤더를 설정
//		Message message = converter.toMessage(order, properties);
//		rabbitTemplate.send("tacocloud.orders", message);
//	}

//	@Override
//	public void sendOrder(Order order) {
//		// 모든 변환 작업을 RabbitTemplate이 처리하도록 convertAndSend()를 사용할 수 있다.
//		rabbitTemplate.convertAndSend("tacocloud.orders", order);
//	}

//	@Override
//	public void sendOrder(Order order) {
//		MessageConverter converter = rabbitTemplate.getMessageConverter();
//		MessageProperties properties = new MessageProperties();
//		Message message = converter.toMessage(order, properties);
//		rabbitTemplate.send("tacocloud.orders", message);
//	}

}
