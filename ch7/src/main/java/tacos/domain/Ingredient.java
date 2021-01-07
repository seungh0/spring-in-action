package tacos.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
// JPA에서는 기본생성자를 가져야 한다 + 초기화가 필요한 final 속성이 있으므로 force 속성을 true => Lombok이 자동 생성한 생성자에서 그 속성들을 null로 설정.
@Entity  // JPA 매핑 어노테이션을 우리 도메인 객체에 추가해야 한다.
public class Ingredient {

	@Id
	private final String id;
	private final String name;
	private final Type type;

	public static enum Type {
		WRAP, PROTEIN, VEGGIES, CHEESE, SAUCE
	}

}
