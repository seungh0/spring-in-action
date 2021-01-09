package sia5;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

public class FluxFilteringTests {

	/**
	 * 데이터가 전달될때 맨 앞부터 원하는 개수의 항목을 무시 (skip())
	 */
	@Test
	void skipAFlex() {
		Flux<String> flux = Flux.just("A", "B", "C")
				.skip(1); // 1개의 항목을 건너뛰고 마지막 두 항목만 발행

		StepVerifier.create(flux)
				.expectNext("B")
				.expectNext("C")
				.verifyComplete();
	}

	/**
	 * 지정된 시간이 경과할 때까지 기다렸다가 소스 Flux의 항목을 방출.
	 */
	@Test
	void skipAFewSeconds() {
		Flux<String> flux = Flux.just("one", "two", "three", "four", "five", "six")
				.delayElements(Duration.ofSeconds(1)) // 1초 동안 지연되는 Flux
				.skip(Duration.ofSeconds(4)); // 4초 동안 기다렸다가 값을 방출하는 결과 Flux 생성

		StepVerifier.create(flux)
				.expectNext("four", "five", "six")
				.verifyComplete();
	}

	/**
	 * take()는 처음부터 지정된 수의 항목만을 방출.
	 */
	@Test
	void take() {
		Flux<String> flux = Flux.just("one", "two", "three", "four", "five", "six")
				.take(3); // 처음부터 3개의 항목만을 방출.

		StepVerifier.create(flux)
				.expectNext("one", "two", "three")
				.verifyComplete();
	}

	/**
	 * take()도 skip()와 같이 항목 수가 아닌 경과 시간을 기준으로 하는 다른 형태를 갖는다.
	 */
	@Test
	void takeSeconds() {
		Flux<String> flux = Flux.just("one", "two", "three", "four", "five", "six")
				.delayElements(Duration.ofSeconds(1))
				.take(Duration.ofMillis(3500));

		StepVerifier.create(flux)
				.expectNext("one", "two", "three")
				.verifyComplete();
	}

	/**
	 * filter: Flux를 통해 항목을 전달할 것인가의 여부를 결정하는 조건식(Predicate)이 지정되면 filter() 오퍼레이션에서 우리가 원하는 조건을 기반으로 선택적인 발행을 할 수 있다.
	 */
	@Test
	void filter() {
		Flux<String> flux = Flux.just("one", "two", "three", "four", "five", "six")
				.filter(np -> np.startsWith("t"));

		StepVerifier.create(flux)
				.expectNext("two", "three")
				.verifyComplete();
	}

	/**
	 * 이미 발행되어 수신된 항목을 필터링으로 걸러낼 필요. => distinct()
	 */
	@Test
	void distinct() {
		Flux<String> flux = Flux.just("one", "one", "two", "three", "two", "four", "five", "six")
				.distinct();

		StepVerifier.create(flux)
				.expectNext("one", "two", "three", "four", "five", "six")
				.verifyComplete();
	}

}
