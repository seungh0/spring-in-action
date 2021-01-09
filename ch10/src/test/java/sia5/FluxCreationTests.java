package sia5;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FluxCreationTests {

	@Test
	void createAFlux_just() {
		Flux<String> fruitFlux = Flux.just("A", "B", "C", "D"); // 객체로부터 Flux 생성

		fruitFlux.subscribe(System.out::println); // 구독자 추가

		StepVerifier.create(fruitFlux)
				.expectNext("A")
				.expectNext("B")
				.expectNext("C")
				.expectNext("D")
				.verifyComplete();
	}

	@Test
	void createAFlux_fromArray() {
		String[] fruits = new String[]{"A", "B", "C", "D"}; // 컬렉션으로부터 Flux 생성

		Flux<String> fruitFlux = Flux.fromArray(fruits);

		StepVerifier.create(fruitFlux)
				.expectNext("A")
				.expectNext("B")
				.expectNext("C")
				.expectNext("D")
				.verifyComplete();
	}

	@Test
	void createAFlux_FromIterable() {
		List<String> fruitList = new ArrayList<>();
		fruitList.add("A");
		fruitList.add("B");
		fruitList.add("C");

		Flux<String> fruitFlux = Flux.fromIterable(fruitList);

		StepVerifier.create(fruitFlux)
				.expectNext("A")
				.expectNext("B")
				.expectNext("C")
				.verifyComplete();
	}

	@Test
	void createAFlux_fromStream() {
		Stream<String> fruitStream = Stream.of("A", "B", "C");

		Flux<String> fruitFlux = Flux.fromStream(fruitStream);

		StepVerifier.create(fruitFlux)
				.expectNext("A")
				.expectNext("B")
				.expectNext("C")
				.verifyComplete();
	}

	@Test
	void creatFlux_interval() {
		Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1)).take(5);

		StepVerifier.create(intervalFlux)
				.expectNext(0L)
				.expectNext(1L)
				.expectNext(2L)
				.expectNext(3L)
				.expectNext(4L)
				.verifyComplete();
	}

	@Test
	void creatAFlux_Range() {
		Flux<Integer> intervalFlux = Flux.range(1, 5);

		StepVerifier.create(intervalFlux)
				.expectNext(1)
				.expectNext(2)
				.expectNext(3)
				.expectNext(4)
				.expectNext(5)
				.verifyComplete();
	}

}
