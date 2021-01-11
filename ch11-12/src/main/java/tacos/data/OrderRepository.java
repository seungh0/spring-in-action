package tacos.data;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import tacos.domain.Order;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {

}
