package tacos.messaging;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import tacos.domain.Order;

@Component
public class JmsOrderReceiver implements OrderReceiver {

	private final JmsTemplate jmsTemplate;

	public JmsOrderReceiver(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	@Override
	public Order receiveOrder() {
		return (Order) jmsTemplate.receiveAndConvert("tacocloud.order.queue");
	}

}
