package tacos.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class Order implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private UUID id = UUID.randomUUID();
	private Date placedAt = new Date();

	@Field("customer")

	private String deliveryName;

	private String deliveryStreet;

	private String deliveryCity;

	private String deliveryState;

	private String deliveryZip;

	private String ccNumber;

	private String ccExpiration;

	private String ccCVV;

	private List<Taco> tacos = new ArrayList<>();

	public void addDesign(Taco design) {
		this.tacos.add(design);
	}

}
