package tacos.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tacos.domain.Order;
import tacos.messaging.RabbitOrderReceiver;

@RestController
public class OrderReceiveController {

	private final RabbitOrderReceiver orderReceiver;

	public OrderReceiveController(RabbitOrderReceiver orderReceiver) {
		this.orderReceiver = orderReceiver;
	}

	@GetMapping("/receive")
	public Order receive() {
		return orderReceiver.receiveOrder();
	}

}
