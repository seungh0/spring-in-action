package tacos.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tacos.domain.Order;

@Slf4j
@Component
public class OrderListener {

	@RabbitListener(queues = "tacocloud.orders")
	public void receiveOrder(Order order) {
		log.info(order.toString() + " send");
	}
	
}
