package tacos.external;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.domain.Ingredient;

import java.time.Duration;

@Service
public class WebClientSample {

	public void getIngredientById(String id) {
		Mono<Ingredient> ingredientMono = WebClient.create()
				.get()
				.uri("http://localhost:8080/ingredients/{id}", id)
				.retrieve()
				.bodyToMono(Ingredient.class);
		ingredientMono.timeout(Duration.ofSeconds(1))
				.subscribe(System.out::println, System.out::println);
	}

	public void getIngredients() {
		Flux<Ingredient> ingredientFlux = WebClient.create()
				.get()
				.uri("http://localhost:8080/ingredients")
				.retrieve()
				.onStatus(status -> status == HttpStatus.BAD_GATEWAY, response -> Mono.just(new IllegalStateException()))
				.bodyToFlux(Ingredient.class);
		ingredientFlux.timeout(Duration.ofSeconds(1))
				.subscribe(System.out::println, System.out::println);
	}

}
