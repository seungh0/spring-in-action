# 스프링 인 액션

## 3. 데이터로 작업하기

### 3.1 JDBC를 사용해서 데이터 읽고 쓰기

- 스프링의 JDBC 지원은 JdbcTemplate 클래스에 기반을 둠.
- JDBCTemplate은 JDBC를 사용할 때 요구되는 모든 형식적이고 상투적인 코드없이 개발자가 관계형 데이터베이스에 대한 SQL 연산을 수행할 수 있는 방법을 제공.

- JDBCTemplate을 사용하지 않고 데이터베이스 쿼리하면 데이터베이스 Connection 생성, 명령문 생성, 연결과 명령문 및 결과 세트를 닫고 클린업 하는 코드들로 쿼리 코드가 둘러싸여 복잡.

JDBCTemplate 및 H2 데이터베이스 의존성 추가

- 우리 프로젝트의 classpath에 추가 => 스프링 부트의 JDBC starter의 의존성을 빌드 명세에 추가하면 간단.

```xml

<properties>
    ...
    <h2.version>1.4.196</h2.version>
</properties>

<dependencies>
...
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
...
</dependencies>

```

### Repository 인터페이스 정의하기

```java
public interface IngredientRepository {

	Iterable<Ingredient> findAll();

	Ingredient findById(String id);

	Ingredient save(Ingredient ingredient);

}

```

### JDBCTemplate을 사용해서 데이터베이스 쿼리하기.

```java

@Repository
public class JdbcTacoRepository implements TacoRepository {

	private final JdbcTemplate jdbcTemplate;

	public JdbcTacoRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Taco save(Taco taco) {
		long tacoId = saveTacoInfo(taco);
		taco.setId(tacoId);
		for (Ingredient ingredient : taco.getIngredients()) {
			saveIngredientToTaco(ingredient, tacoId);
		}
		return taco;
	}

	private long saveTacoInfo(Taco taco) {
		taco.setCreatedAt(new Date());
		PreparedStatementCreator psc = new PreparedStatementCreatorFactory("insert into Taco (name, createdAt) values(?, ?)", Types.VARCHAR, Types.TIMESTAMP)
				.newPreparedStatementCreator(Arrays.asList(taco.getName(), new Timestamp(taco.getCreatedAt().getTime())));
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(psc, keyHolder);
		return keyHolder.getKey().longValue();
	}

	private void saveIngredientToTaco(Ingredient ingredient, long tacoId) {
		jdbcTemplate.update("insert into Taco_Ingredients (taco, ingredient) values (?, ?)", tacoId, ingredient.getId());
	}

}
```

### *.sql

- 애플리케이션 classpath의 루트 경로에 있으면 애플리케이션이 시작될때 *.sql 파일의 SQL이 사용중인 데이터베이스에서 자동으로 실행된다.
- src/main/resources

### JdbcTemplate을 이용해서 id 및 createdAt 저장

```java

@Repository // 스프링 컴포넌트 검색에서 이 클래스를 자동으로 찾아서 스프링 애플리케이션 컨텍스트의 빈으로 생성해 줌.
public class JdbcIngredientRepository implements IngredientRepository {

	private JdbcTemplate jdbcTemplate;

	@Autowired // JdbcIngredientRepository 빈이 생성되면, 스프링이 해당 빈을 JDBCTemplate에 주입한다. (사실 @Autowired 안해줘도 되지만 책 따라함)
	public JdbcIngredientRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Iterable<Ingredient> findAll() {
		return jdbcTemplate.query("select id, name, type from Ingredient", this::mapRowToIngredient);
	}

	private Ingredient mapRowToIngredient(ResultSet resultSet, int rowNum) throws SQLException {
		return new Ingredient(resultSet.getString("id"), resultSet.getString("name"), Ingredient.Type.valueOf(resultSet.getString("type")));
	}

	@Override
	public Ingredient findById(String id) {
		return jdbcTemplate.queryForObject("select id, name, type from Ingredient where id=?", this::mapRowToIngredient, id);
	}

	@Override
	public Ingredient save(Ingredient ingredient) {
		jdbcTemplate.update("insert into Ingredient (id, name, type) values (?, ?, ?)", ingredient.getId(), ingredient.getName(), ingredient.getType().toString());
		return ingredient;
	}

}

```

## SimpleJdbcInsert

- 주문 데이터를 Taco_Order 테이블에 저장하는 것은 물론이고, 해당 주문의 각 타코에 대한 id도 Taco_Order_Tacos 테이블에 저장해야 함.
- 그러나 이 경우는 복잡한 PreparedStatementCreator 대신 SimpleJdbcInesrt를 사용할 수 있음.
- SimpleJdbcInsert는 데이터를 더 쉽게 테이블에 추가하기 위해 JdbcTemplate을 래핑한 객체.

```java

@Repository
public class JdbcOrderRepository implements OrderRepository {

	private final SimpleJdbcInsert orderInserter; // Taco_Order 테이블에 주문 데이터를 추가하기 위해 구성, 이떄 Order 객체의 id 속성 값은 데이터베이스가 생성해 주는 것을 사용.
	private final SimpleJdbcInsert orderTacoInserters; // Taco_Order_Tacos 테이블에 해당 주문 id 및 이것과 연관된 타코들의 id를 추가하기 위해 구성.
	private final ObjectMapper objectMapper;

	public JdbcOrderRepository(JdbcTemplate jdbcTemplate) {
		this.orderInserter = new SimpleJdbcInsert(jdbcTemplate)
				.withTableName("Taco_Order")
				.usingGeneratedKeyColumns("id");
		this.orderTacoInserters = new SimpleJdbcInsert(jdbcTemplate)
				.withTableName("Taco_Order_Tacos");
		this.objectMapper = new ObjectMapper();
	}

	@Override
	public Order save(Order order) {
		order.setPlacedAt(new Date());
		long orderId = saveOrderDetails(order);
		order.setId(orderId);
		List<Taco> tacos = order.getTacos();
		for (Taco taco : tacos) {
			saveTacoToOrder(taco, orderId);
		}
		return order;
	}

	private long saveOrderDetails(Order order) {
		@SuppressWarnings("unchecked")
		Map<String, Object> values = objectMapper.convertValue(order, Map.class);
		return orderInserter.executeAndReturnKey(values)
				.longValue();
	}

	private void saveTacoToOrder(Taco taco, long orderId) {
		Map<String, Object> values = new HashMap<>();
		values.put("tacoOrder", orderId);
		values.put("taco", taco.getId());
		orderTacoInserters.execute(values);
	}

}

```

### Converter

```java

@Component // 스프링에 의해 자동 생성 및 주입되는 빈으로 생성.
public class IngredientByIdConverter implements Converter<String, Ingredient> {

	private final IngredientRepository ingredientRepository;

	public IngredientByIdConverter(IngredientRepository ingredientRepository) {
		this.ingredientRepository = ingredientRepository;
	}

	@Override
	public Ingredient convert(String id) {
		return ingredientRepository.findById(id);
	}

}

```

- 데이터의 타입을 변환해주는 컨버터.
- 우리가 Converter에 지정한 타입 변환이 필요할때 convert() 메소드가 자동 호출됨.
- 우리 애플리케이션에서는 String 타입의 식자재 ID를 사용해서 데이터베이스에 저장된 특정 식자제 데이터를 읽은 후 Ingredient 객체로 변환하기 위해서 컨버터가 사용.
