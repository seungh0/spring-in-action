package tacos;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tacos.domain.Order;
import tacos.messaging.OrderMessagingService;

@Component
public class MessagingRunner implements CommandLineRunner {

	private final OrderMessagingService orderMessagingService;

	public MessagingRunner(OrderMessagingService orderMessagingService) {
		this.orderMessagingService = orderMessagingService;
	}

	@Override
	public void run(String... args) {
		Order order = Order.create("order");
		orderMessagingService.sendOrder(order);
	}

}
