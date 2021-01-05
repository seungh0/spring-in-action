package tacos.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import tacos.Order;
import tacos.Taco;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcOrderRepository implements OrderRepository {

	private final SimpleJdbcInsert orderInserter; // Taco_Order 테이블에 주문 데이터를 추가하기 위해 구성, 이떄 Order 객체의 id 속성 값은 데이터베이스가 생성해 주는 것을 사용.
	private final SimpleJdbcInsert orderTacoInserters; // Taco_Order_Tacos 테이블에 해당 주문 id 및 이것과 연관된 타코들의 id를 추가하기 위해 구성.
	private final ObjectMapper objectMapper;

	public JdbcOrderRepository(JdbcTemplate jdbcTemplate) {
		this.orderInserter = new SimpleJdbcInsert(jdbcTemplate)
				.withTableName("Taco_Order")
				.usingGeneratedKeyColumns("id");
		this.orderTacoInserters = new SimpleJdbcInsert(jdbcTemplate)
				.withTableName("Taco_Order_Tacos");
		this.objectMapper = new ObjectMapper();
	}

	@Override
	public Order save(Order order) {
		order.setPlacedAt(new Date());
		long orderId = saveOrderDetails(order);
		order.setId(orderId);
		List<Taco> tacos = order.getTacos();
		for (Taco taco : tacos) {
			saveTacoToOrder(taco, orderId);
		}
		return order;
	}

	private long saveOrderDetails(Order order) {
		@SuppressWarnings("unchecked")
		Map<String, Object> values = objectMapper.convertValue(order, Map.class);
		return orderInserter.executeAndReturnKey(values)
				.longValue();
	}

	private void saveTacoToOrder(Taco taco, long orderId) {
		Map<String, Object> values = new HashMap<>();
		values.put("tacoOrder", orderId);
		values.put("taco", taco.getId());
		orderTacoInserters.execute(values);
	}

}
