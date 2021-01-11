package tacos.data;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import tacos.domain.Ingredient;

public interface IngredientRepository extends ReactiveCrudRepository<Ingredient, String> {

}
