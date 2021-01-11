package tacos.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document // 몽고DB에 저장되거나 읽을 수 있는 문서 엔티티라는 것을 나타냄.
public class Ingredient {

	@Id
	private String id;

	private String name;

	private Type type;

	public Ingredient(String id, String name, Type type) {
		this.id = id;
		this.name = name;
		this.type = type;
	}

	public static enum Type {
		WRAP, PROTEIN, VEGGIES, CHEESE, SAUCE
	}

}
