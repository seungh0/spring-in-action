package sia5;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

public class FluxBufferTests {

	@Test
	void buffer() {
		Flux<String> fruitFlux = Flux.just("apple", "banana", "kiwi", "orange");

		Flux<List<String>> bufferedFlux = fruitFlux.buffer(2);

		StepVerifier
				.create(bufferedFlux)
				.expectNext(Arrays.asList("apple", "banana"))
				.expectNext(Arrays.asList("kiwi", "orange"))
				.verifyComplete();
	}

	@Test
	void bufferFlatMap() {
		Flux.just("apple", "banana", "kiwi", "orange")
				.buffer(3)
				.flatMap(x ->
						Flux.fromIterable(x)
								.map(y -> y.toUpperCase())
								.subscribeOn(Schedulers.parallel())
								.log()
				).subscribe();
	}

}
