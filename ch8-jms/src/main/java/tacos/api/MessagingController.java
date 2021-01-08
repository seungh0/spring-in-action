package tacos.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tacos.domain.Order;
import tacos.messaging.OrderMessagingService;

@RestController
public class MessagingController {

	private final OrderMessagingService orderMessagingService;

	public MessagingController(OrderMessagingService orderMessagingService) {
		this.orderMessagingService = orderMessagingService;
	}

	@GetMapping("/messaging")
	public String messaging() {
		Order order = Order.create("test");
		orderMessagingService.sendOrder(order);
		return "OK";
	}

}
