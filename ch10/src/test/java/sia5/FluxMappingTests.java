package sia5;

import lombok.Data;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

public class FluxMappingTests {

	/**
	 * 발행된 항목을 다른 형태나 타입으로 매핑.
	 * cf) 각 항목이 소스 Flux로 부터 발행될 때 동기적으로 매핑이 수행된다는 것.
	 * 따라서 비동기적으로 매핑을 수행하고 싶다면 flatMap() 오퍼레이션을 사용해야 함.
	 */
	@Test
	void map() {
		Flux<Player> playerFlux = Flux.just("Michael Jordan", "Scottile Pippen")
				.map(n -> {
					String[] split = n.split("\\s");
					return new Player(split[0], split[1]);
				});

		StepVerifier.create(playerFlux)
				.expectNext(new Player("Michael", "Jordan"))
				.expectNext(new Player("Scottile", "Pippen"))
				.verifyComplete();
	}

	/**
	 * 마지막에 subscriberOn()을 호출해서 각 구독이 병렬 스레드로 수행되어야 한다는 것을 나타냄.
	 * 따라서 다수의 입력 객체들의 map() 오퍼레이션이 비동기적으로 병행 수행될 수 있다.
	 */
	@Test
	void flatMap() {
		Flux<Player> playerFlux = Flux.just("Michael Jordan", "Scottile Pippen")
				.flatMap(n -> Mono.just(n).map(p -> {
					String[] split = p.split("\\s");
					return new Player(split[0], split[1]);
				}).subscribeOn(Schedulers.parallel()));

		List<Player> playerList = Arrays.asList(new Player("Michael", "Jordan"), new Player("Scottile", "Pippen"));

		StepVerifier.create(playerFlux)
				.expectNextMatches(playerList::contains)
				.expectNextMatches(playerList::contains)
				.verifyComplete();
	}

	@Data
	private static class Player {
		private final String firstName;
		private final String lastName;
	}

}
