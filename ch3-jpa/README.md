# 스프링 인 액션

## 3. 데이터로 작업하기

## 3.2 스프링 데이터 JPA를 사용해서 데이터 저장하고 사용하기

- 스프링 데이터 프로젝트는 여러 개의 하위 프로젝트로 구성되는 다수 규모가 큰 프로젝트.
- 대부분의 하위 프로젝트는 다양한 데이터베이스 유형을 사용한 데이터 퍼세스턴스에 초점을 둠.
    - 스프링 데이터 JPA: 관계형 데이터베이스의 JPA Persistence
    - 스프링 데이터 MongoDB: 몽고 문서형 데이터베이스의 Persistence
    - 스프링 데이터 Neo4: Neo4j 그래프 데이터베이스의 Persistence
    - 스프링 데이터 Redis: Redis 키-값 스토어의 Persistence
    - 스프링 데이터 Cassandra: 카산드ㅏ 데이터베이스의 Persistence

- 스프링 데이터에서는 Repository 인터페이스를 기반으로 이 인터페이스를 구현하는 Repository를 자동 생성해준다.

### 스프링 데이터 JPA를 프로젝트에 추가하기

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

- JPA 스타터를 통해서 스프링 부트 애플리케이션에서 사용할 수 있다.
- 이 스타터 의존성에는 스프링 데이터 JPA는 물론이고, JPA를 구현한 Hibernate까지도 포함된다.

### 만일 다른 JPA 구현 라이브러리 (EclipseLink)를 사용한다면 아래와 같이 의존성 추가.

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
    <exclusions>
        <exclusion>
            <groupId>hibernate-entitymanager</groupId>
            <artifactId>org.hibernate</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
<groupId>org.eclipse.persistence</groupId>
<artifactId>eclipselink</artifactId>
<version>2.5.2</version>
</dependency>

```

### 도메인 객체에 애노테이션 추가하기

```java

@Data // 사실 안쓰는걸 추천..
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

```

### JPA Repository 선언하기

```java
public interface IngredientRepository extends CrudRepository<Ingredient, String> {

}

```

- 애플리케이션이 시작될 때, 스프링 데이터 JPA가 각 인터페이스 구현채를 자동으로 생성해줌.

### JPA Repository 커스터마이징하기.

```java
List<Order> findByDeliveryZip(String deliveryZip);
```

본질적으로 스프링 데이터는 일종의 DSL을 정의하고 있어서 퍼시스턴스에 관한 내용이 리퍼지터리 메소드의 시그니처에 표현된다.
