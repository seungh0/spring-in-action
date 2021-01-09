# 10. 리액터 개요

애플리케이션 코드를 개발할 때는 명령형과 리액티브의 두 가지 형태로 코드를 작성할 수 있다.

- 명령형 코드
    - 순차적으로 연속되는 작업
    - 각 작업은 한 번에 하나씩 그리고 이전 작업 다음에 실행된다.
    - 데이터는 모아서 처리되고 이런 작업이 데이터 처리를 끝낸 후에 다음 작업으로 넘어갈 수 있다.
- 리액티브 코드
    - 데이터 처리를 위해 일련의 작업들이 정의되지만, 이 작업들은 병렬로 실행될 수 있다.
    - 그리고 각 작업은 부분 집합의 데이터를 처리할 수 있으며, 처리가 끝난 데이터를 다음 작업에 넘겨주고 다른 부분 집합의 데이터로 계속 작업할 수 있다.

### 리액터

- 스프링 프로젝트의 일부분인 리액티브 프로그래밍 라이브러리.
- 리액터는 스프링 5에서 리액티브 프로그래밍을 지원하는 데 필요한 기반.

## 리액티브 프로그래밍

### 명령형 프로그래밍

#### 명령형 프로그래밍의 문제점

- 명령형 프로그래밍은 한 번에 하나씩 만나는 순서대로 실행되는 명령어로 코드를 작성. 그리고 하나의 작업이 완전히 끝나기를 기다렸다가 그 다음 작업을 수행.
- 각 단계마다 처리되는 데이터는 전체를 처리할 수 있도록 사용할 수 있어야 함.
- 그러나 작업이 수행되는 동안 특히 원격지 서버로 부터 데이터베이스에 데이터를 쓰거나 가져오는 것과 같이 이 작업이 완료될 떄 까지 아무것도 할 수 없다
- 따라서 이 작업을 수행하는 쓰레드는 차단된다. (이렇게 차단되는 스레드는 낭비)

#### 스레드 관리의 어려움

- 대부분의 프로그래밍 언어는 동시 프로그래밍을 지원한다. 자바에서는 스레드가 어떤 작업을 계속 수행하는 동안 이 스레드에서 다른 스레드를 시작시키고 작업을 수행하게 하는 것은 매우 쉽다.
- 그러나 스레드를 생성하는 것은 쉬울지라도 생성된 스레드는 어떤 이유로든 결국 차단된다.
- 게다가 다중 스레드로 동시성을 관리하는 것은 쉽지 않다. (스레드가 많을수록 더 복잡해지기 때문)

### 리액티브 프로그래밍

- 본질적으로 함수적이며 선언적이다.
- 순차적으로 수행되는 작업 단계를 나타낸 것이 아니라, 데이터가 흘러가는 파이프라인이나 스트림을 포함.
- 이런 리액티브 스트림은 데이터 전체를 사용할 수 있을때까지 기다리지 않고 사용 가능한 데이터가 있을 때 마다 처리되므로 사실상 입력되는 데이터는 무한할 수 있다.
- 동시에 여러 작업을 수행하여 더 큰 확장성을 얻게 해준다.

### 리액티브 스트림 정의하기

#### 백 프레셔란?

- 백 프레셔는 데이터를 소비하는 컨슈머가 처리할 수 있는 만큼으로 전달 데이터를 제한함으로써 지나치게 빠른 데이터 소스로부터의 데이터 전달 폭주를 피할 수 있는 수단.

#### 자바 스트림 vs 리액티브 스트림

- 둘다 Stream이라는 단어가 이름에 포함되고, 데이터로 작업하기 위한 API를 제공.
- 자바 스트림
    - 대개 동기화되어 있고, 한정된 데이터를 작업을 수행.
- 리액티브 스트림
    - 무한 데이터셋을 비롯해서 어떤 크기 데이터셋이건 비동기 처리를 지원.
    - 실시간으로 데이터를 처리하여, 백 프레셔를 사용해서 데이터 전달 폭주를 막는다.

#### 리액티브 스트림

- 리액티브 스트림은 4개의 인터페이스로 요약할 수 있다.
- Publisher(발행자)
- Subscriber(구독자)
- Subscription(구독)
- Processor(프로세서)

#### Publisher

```java
public interface Publisher<T> {
	void subscribe(Subscriber<? super T> subscriber);
}
```

- Publisher 인터페이스에는 Subscriber가 Publisher를 구독 신청할 수 있는 subscribe() 메소드 한 개가 선언되어 있다.
- 그리고 Subscriber가 구독 신청되면 Publisher로부터 이벤트를 수신할 수 있다.
- 이 이벤트 들은 Subscriber 인터페이스의 메소드를 통해 전송된다.

#### Subscriber

```java
public interface Subscriber<T> {
	void onSubscribe(Subscription sub);

	void onNext(T item);

	void onError(Throwable ex);

	void onComplete();
}
```

- Subscriber가 수신할 첫번째 이벤트는 onSubscribe()의 호출을 통해 이루어짐.
- Publisher가 onSubscirbe()를 호출할 때 이 메소드의 인자로 Subscription 객체를 Subscriber에 전달.
- Subscriber는 Subscription 객체를 통해서 구독을 관리할 수 있다.

#### Subscription

```java
public interface Subscription {
	void request(long n);

	void cancel();
}
```

- Subscriber는 request()를 호출하여 전송되는 데이터를 요청하거나, 또는 더 이상 데이터를 수신하지 않고 구독을 취소한다느 것을 나타내기 위해 cancel()을 호출할 수 있다.
- request()를 호출할 때, Subscriber는 받고자 하는 데이터 항목 수를 나타내는 long 타입의 값을 인자로 전달하며, 바로 이것이 백프레셔이며, Subscriber가 처리할 수 있는 것보다 더 많은
  데이터를 Publisher가 전송하는 것을 막아줌.
- 요청된 수의 데이터를 Publisher가 전송한 후에 Subscriber는 다시 request()를 호출하여 더 많은 요청을 할 수 있다.
- Subscriber의 데이터 요청이 완료되면 데이터가 스트림을 통해 전달되기 시작한다. 이떄 onNext() 메소드가 호출되어 Publsiher가 전송하는 데이터가 Subscriber에게 전달되며, 만일 에러가
  생길 때는 onError()가 호출된다.
- 그리고 Publisher에서 전송할 데이터가 없고 더 이상의 데이터를 생성하지 않는다면 Publisher가 onComplete()를 호출하여 작업이 끝났다고 Subscriber에게 알려준다.

Processor 인터페이스는 다음과 같이 Subscribrer 인터페이스와 Publisher 인터페이스를 결합한 것이다.

```java
public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {

}
```

- Subscriber 역할로 Processor는 데이터를 수신하고 처리한다.
- 그 다음에 역할을 바꾸어 Publisher 역할로 처리 결과를 자신의 Subscriber들에게 발행한다.

## 리액터 시작하기

리액티브 프로그래밍은 일련의 작업 단계를 기술하는 것이 아니라, 데이터가 전달될 파이프라인을 구성하는 것. 그리고 이 파이프라인을 통해 데이터가 전달되는 동안 어떤 형태로든 변경 또는 사용될 수 있다.

### Mono vs Flux

리액터의 두 가지 핵심 타입

- Mono
    - 0, 1또는 다수의 데이터를 갖는 파이프라인
- Flux
    - 하나의 데이터 항목만 갖는 데이터셋에 최적화된 리액티브 타입

### 리액티브 플로우의 다이어그램

- 리액티브 플로우는 마블 다이어그램으로 나타내곤 한다. 마블 다이어그램의 젱리 위에는 Flux 나 Mono를 통해 전달되는 데이터의 타임라인을 나타내고, 중앙에는 오퍼레이션을 제일 밑에는 결과로 생성되는 Flux나
  Mono의 타임라인을 나타낸다.

#### 리액터 의존성 추가하기

```xml

<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
</dependency>

<dependency>
<groupId>io.projectreactor</groupId>
<artifactId>reactor-test</artifactId>
<scope>test</scope>
</dependency> # 테스트를 지원해주는 모듈 의존성
```

- 스프링 부트는 의존성 관리를 자동으로 해주므로 해당 의존성에 <version> 요소를 지정할 필요가 없다.

## 리액티브 오퍼레이션 적용하기

FLux와 Mono는 리액터가 제공하는 가장 핵심적인 구성요소이다. 그리고 Flux, Mono가 제공하는 오퍼레이션들은 두 타입을 함께 결합하여 데이터가 전달될 수 있는 파이프라인을 생성한다.

#### Flux, Mono 오퍼레이션 분류

- 생성 오퍼레이션
- 조합 오퍼레이션
- 변환 오퍼레이션
- 로직 오퍼레이션

### 리액티브 타입 생성하기

- 생성 오퍼레이션
- 스프링에서 리액티브 타입을 사용할 떄는 리퍼지터리나 서비스로부터 Flux나 Mono가 제공되므로 우리의 리액티브 타입을 생성할 필요가 없다.
- 그러나 데이터를 발행하는 새로운 리액티브 발행자를 생성해야 할 때가 있다.

```java
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
```

### 리액티브 타입 조합하기

두 개의 리액티브 타입을 결합해야 하거나 하나의 Flux를 두개 이상의 리액티브 타입으로 분할해야 하는 경우

```java
public class FluxMergingTests {

	@Test
	void mergeFluxes() {
		Flux<String> characterFlux = Flux
				.just("Garfield", "Kojak", "Barbossa")
				.delayElements(Duration.ofMillis(500));
		Flux<String> foodFlux = Flux
				.just("Lasagna", "Lollipops", "Apples")
				.delaySubscription(Duration.ofMillis(250))
				.delayElements(Duration.ofMillis(500));

		// 두개의 Flux를 조합 (단, 소스 Flux들의 값이 완벽하게 번갈아 방출되게 보장할 수 없음)
		Flux<String> mergedFlux = characterFlux.mergeWith(foodFlux);

		mergedFlux.subscribe(System.out::println);

		StepVerifier.create(mergedFlux)
				.expectNext("Garfield")
				.expectNext("Lasagna")
				.expectNext("Kojak")
				.expectNext("Lollipops")
				.expectNext("Barbossa")
				.expectNext("Apples")
				.verifyComplete();
	}

	@Test
	public void zipFluxes() {
		Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbossa");
		Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples");

		// 각 Flux 소스로부터 한 항목씩 번갈아 가져와 새로운 Flux를 생성
		Flux<Tuple2<String, String>> zippedFlux = Flux.zip(characterFlux, foodFlux);

		StepVerifier.create(zippedFlux)
				.expectNextMatches(p ->
						p.getT1().equals("Garfield") &&
								p.getT2().equals("Lasagna"))
				.expectNextMatches(p ->
						p.getT1().equals("Kojak") &&
								p.getT2().equals("Lollipops"))
				.expectNextMatches(p ->
						p.getT1().equals("Barbossa") &&
								p.getT2().equals("Apples"))
				.verifyComplete();
	}

	@Test
	void zipFluxesToObject() {
		Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbossa");
		Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples");

		Flux<String> zippedFlux = Flux.zip(characterFlux, foodFlux, (c, f) -> c + " eats " + f);

		StepVerifier.create(zippedFlux)
				.expectNext("Garfield eats Lasagna")
				.expectNext("Kojak eats Lollipops")
				.expectNext("Barbossa eats Apples")
				.verifyComplete();
	}

	@Test
	void firstFlux() {
		Flux<String> slowFlux = Flux.just("tortoise", "snail", "sloth")
				.delaySubscription(Duration.ofMillis(100));
		Flux<String> fastFlux = Flux.just("hare", "cheetah", "squirrel");

		// 먼저 값을 방출하는 리액티브 타입 선택
		Flux<String> firstFlux = Flux.firstWithSignal(slowFlux, fastFlux);

		StepVerifier.create(firstFlux)
				.expectNext("hare")
				.expectNext("cheetah")
				.expectNext("squirrel")
				.verifyComplete();
	}

}


```