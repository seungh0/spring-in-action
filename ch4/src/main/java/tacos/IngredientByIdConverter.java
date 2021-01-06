package tacos;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import tacos.data.IngredientRepository;

/**
 * 데이터의 타입을 변환해주는 컨버터.
 * 우리가 Converter에 지정한 타입 변환이 필요할때 convert() 메소드가 자동 호출됨.
 * 우리 애플리케이션에서는 String 타입의 식자재 ID를 사용해서 데이터베이스에 저장된 특정 식자제 데이터를 읽은 후 Ingredient 객체로 변환하기 위해서 컨버터가 사용.
 */

@Component // 스프링에 의해 자동 생성 및 주입되는 빈으로 생성.
public class IngredientByIdConverter implements Converter<String, Ingredient> {

	private final IngredientRepository ingredientRepository;

	public IngredientByIdConverter(IngredientRepository ingredientRepository) {
		this.ingredientRepository = ingredientRepository;
	}

	@Override
	public Ingredient convert(String id) {
		return ingredientRepository.findById(id)
				.orElse(null);
	}

}
