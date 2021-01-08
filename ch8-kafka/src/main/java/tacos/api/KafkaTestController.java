package tacos.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tacos.domain.Order;
import tacos.messaging.OrderMessagingService;

@RestController
public class KafkaTestController {

	private final OrderMessagingService orderMessagingService;

	public KafkaTestController(OrderMessagingService orderMessagingService) {
		this.orderMessagingService = orderMessagingService;
	}

	@GetMapping("/kafka")
	public void sendMessage(String name) {
		orderMessagingService.sendOrder(Order.create(name));
	}

}
