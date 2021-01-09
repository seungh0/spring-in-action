package sia5;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class FluxLogicTests {

	/**
	 * Flux가 발행하는 모든 문자열이 a를 포함하는지 확인
	 */
	@Test
	void all() {
		Flux<String> animalFlux = Flux.just("a", "ab", "ac");
		Mono<Boolean> hasAMono = animalFlux.all(a -> a.contains("a"));

		StepVerifier.create(hasAMono)
				.expectNext(true)
				.verifyComplete();
	}

	/**
	 * 최소한 하나의 항목이 일치하는지 검사
	 */
	@Test
	void any() {
		Flux<String> animalFlux = Flux.just("a", "b", "c");
		Mono<Boolean> hasAMono = animalFlux.any(a -> a.contains("a"));

		StepVerifier.create(hasAMono)
				.expectNext(true)
				.verifyComplete();
	}

}
