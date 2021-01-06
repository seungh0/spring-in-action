# 4. 스프링 시큐리티기

## 4-1. 스프링 시큐리티 활성화하기

- 스프링 애플리케이션에서 스프링 시큐리티를 사용하기 위해서는 스프링 부트 스타터 시큐리티 의존성을 빌드 명세에 추가해야 한다.

```xml

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
<groupId>org.springframework.security</groupId>
<artifactId>spring-security-test</artifactId>
<scope>test</scope>
</dependency>
```

- 의존성을 추가하면 스프링 애플리케이션이 시작되면 스프링이 우리 프로젝트의 classpath에 있는 스프링 시큐리티 라이브러리를 찾아 기본적인 보안 구성을 설정해 준다.

### 기본적인 보안 구성 (security starter를 프로젝트 빌드 파일에 추가했을 경우)

- 모든 HTTP 요청 경로는 인증되어야 한다.
- 어떤 특정 역할이나 권한이 없다.
- 로그인 페이지가 따로 없다.
- 스프링 시큐리티의 HTTP 기본 인증을 사용해서 인증된다.
- 사용자는 하나만 있으며, 이름은 user이고 비밀번호는 암호화해줌.

### 최소한 필요한 스프링 시큐리티 구성

- 스프링 시큐리티의 HTTP 인증 대화상자 대신 우리의 로그인 페이지로 인증한다.
- 다수의 사용자를 제공하며, 새로운 타코 클라우드 고객이 사용자로 등록할 수 있는 페이지가 있어야 함.
- 서로 다른 HTTP 요청 경로마다 서로 다른 보안 규칙을 적용한다. (랜딩 페이지 등에는 인증이 필요하지 않음)

### SecurityConfig

- SecurityConfig 클래스 (WebSecurityConfigurerAdapter의 서브 클래스)
- 사용자의 HTTP 요청 경로에 대해 접근 제한과 같은 보안 관련 처리 설정.

```java

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	// 사용자 인증 정보를 구성하는 메소드 (상세 내용은 아래에 추가)
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication()
				.withUser("user1")
				.password("{noop}password1")
				.authorities("ROLE_USER")
				.and()
				.withUser("user2")
				.password("{noop}password2")
				.authorities("ROLE_USER");
	}

	// HTTP 보안을 구성하는 메소드 (상세 내용은 아래에 추가)
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
				.antMatchers("/design", "orders")
				.access("hasRole('ROLE_USER')")
				.antMatchers("/", "/**").access("permitAll")
				.and()
				.httpBasic();
	}

}

```

## 4-2. 스프링 시큐리티 구성하

### 스프링 시큐리티에서 제공하는 사용자 스토어 구성 방법

1. 인메모리 사용자 스토어
    - 테스트 목적이나 간단한 애플리케이션에서는 편리.
    - 사용자의 정보의 추가나 변경이 쉽지 않음.
    - 사용자의 추가, 삭제, 변경을 해야 한다면 보안 구성 코드를 변경한 후 애플리케이션을 다시 빌드하고 배포, 설치 해야함.

```java

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	...

	public SecurityConfig(UserDetailsService userRepositoryUserDetailsService) {
		this.userRepositoryUserDetailsService = userRepositoryUserDetailsService;
	}
	
    ...
}


```

2. JDBC 기반 사용자 스토어

```java

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	...

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
				.jdbcAuthentication()
				.dataSource(dataSource);
	}
	
	...

}


```

```java

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	...

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
				.jdbcAuthentication()
				.dataSource(dataSource)
				.usersByUsernameQuery("select username, password, enabled from users where username=?")
				.authoritiesByUsernameQuery("select username, authority from authorities where username=?")
	}
	
	...

}

```

- 스프링 시큐리티의 것과 다른 데이터베이스를 사용한다면, 스프링 스큐리티의 SQL 쿼리를 커스터마이징 할 수 있음.

```java

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	...

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
				.jdbcAuthentication()
				.dataSource(dataSource)
				.usersByUsernameQuery("select username, password, enabled from users where username=?")
				.authoritiesByUsernameQuery("select username, authority from authorities where username=?")
				.passwordEncoder(new BCryptPasswordEncoder()); // 비밀번호 암호화(encoder)를 지정.
	}
	...

}

```

- passwordEncoder() 메소드는 스프링 시큐리티의 PasswordEncoder 인터페이스를 구현하는 어떤 객체도 인자로 받을 수 있음.
- BCryptPasswordEncoder, NoOpPasswordEncoder(암호화 X), Pbkdf2PasswordEncoder 외에 커스터 마이징 가능.

3. LDAP 기반 사용자 스토어

#### LDAP 이란?

<img src="https://ldap.or.kr/wp-content/uploads/2017/07/%EC%BA%A1%EC%B2%982.png">

- LDAP의 요청처리는, 사용자 혹은 응용프로그램에서 요청을 보내면 LDAP을 통해 LDAP 서버에 전달 됩니다. 서버는 요청을 처리 후 다시 LDAP을 통해 요청자에게 결과를 전송합니다. DAP와 다르게
  TCP/IP 상에서 운영됩니다.

출처: LDAP 이란? [DSMENTORING TECH] https://ldap.or.kr/?p=595

LDAP는 여기까지만 하고 패스....

4. 커스텀 사용자 명세 서비스

#### 사용자 도메인 객체와 퍼스스턴스 정의

```java
/**
 * User 클래스는 스프링 시큐리티의 UserDetails 인터페이스를 구현.
 */
@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@RequiredArgsConstructor
public class User implements UserDetails {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private final String username;

	private final String password;

	private final String fullname;

	private final String street;

	private final String city;

	private final String state;

	private final String zip;

	private final String phoneNumber;

	/**
	 * 해당 사용자에게 부여된 권한을 저장한 컬렉션을 반환
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}

```

```java
public interface UserRepository extends CrudRepository<User, Long> {

	User findByUsername(String username);

}

```

#### 커스텀 사용자 명세 서비스 정의

```java

@Service // 스프링이 컴포넌트 스캔을 해준다는 것을 의미.
public class UserRepositoryUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	// UserRepositoryUserDetailsService에 생성자를 통해 UserRepository 인스턴스가 주입된다.
	public UserRepositoryUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username); // 주입된 UserRepository 인스턴스의 findByUsername()을 호출해 User을 찾는다.

		// 유저를 찾지 못했을 경우
		if (user == null) {
			throw new UsernameNotFoundException(String.format("User %s not found", username));
		}
		// 유저를 찾은 경우 유저를 반환
		return user;
	}

}

```

#### Spring Security 설정

```java

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private UserDetailsService userRepositoryUserDetailsService;

	@Bean
	public PasswordEncoder encoder() {
		return new BCryptPasswordEncoder();
	}

	public SecurityConfig(UserDetailsService userRepositoryUserDetailsService) {
		this.userRepositoryUserDetailsService = userRepositoryUserDetailsService;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth
				.userDetailsService(userRepositoryUserDetailsService)
				.passwordEncoder(encoder()); // encoder()에 @Bean 어노테이션이 지정되었으므로, encoder() 메소드가 생성한 BCryptPasswordEncoder 인스턴스가 스프링 애플리케이션 컨텍스트에 등록, 관리되며 이 인스턴스가 애플리케이션 컨텍스트로부터 주입되어 반환 됨.
		// 따라서 우리가 원하는 종류의 PasswordEncoder 빈 객체를 스프링의 관리하에 사용할 수 있다.
	}

}
```

## 4-3. 웹 요청 보안 처리하기

- 홈페이지, 로그인 등 특정 페이지는 인증되지 않은 모든 사용자가 사용할 수 있어야 한다.
- 이러한 보안 규칙을 구성하려면 configure(HttpSecurity http) 메소드를 오버라이딩 해야함.

```java

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	...

	// HTTP 보을 구성하는 메소드
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		...
	}
	...

}


```

### HttpSecurity를 사용해서 구성할 수 있는 것

- HTTP 요청 처리를 허용하기 전에 충족되어야 할 특정 보안 조건을 구성.
- 커스텀 로그인 페이지 구성
- 사용자가 애플리케이션의 로그아웃을 할 수 있도록 함.
- CSRF 공격으로부터 보호하도록 구성

```java

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	...

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
				.antMatchers("/design", "orders")
				.hasRole("ROLE_USER")
				.antMatchers("/", "/**").access("permitAll");
	}
	...

}

```

- /design, /orders의 요청은 인증된 사용자(ROLE_USER)에게만 허용되고 나머지는 모든 사용자에게 허용
- 이런 규칙을 지정할 때는 순서가 중요함. antMatchers()에서 지정된 경로의 패턴 일치를 검사하므로 먼저 지정된 보안 규칙이 우선적으로 처리 됨.

### 로그인 페이지 및 로그아웃 설정

```java

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	...

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		...
		.formLogin()
				.loginPage("/login") // 커스텀 로그인 페이지. (사용자가 인증되지 않아 로그인이 필요하다고 시큐리티가 판단할 떄 해당 경로로 연결해줌)
				.and()
				.logout()
				.logoutSuccessUrl("/");
	}
	...

}


```

### 해당 경로의 요청을 처리하는 컨트롤러 설정 (뷰 컨트롤러 설정)

```java

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		...
		registry.addViewController("/login");
	}

}
```

### CSRF 공격 방어

- CSRF(Cross-Site Request Forgery) 보안 공격은, 사용자가 웹 사이트에 로그인 한 상태에서 악의적인 코드가 삽입된 페이지를 열명 공격 대상이 되는 웹 사이트에 자동으로 폼이 제출되고 이
  사이트는 위조된 공격 명령이 믿을 수 있는 사용자로부터 제출된 것으로 판단하게 되어 공격에 노출됨.
- CSRF 공격을 막기 위해 애플리케이션에서는 폼의 숨김 필드에 넣을 CSRF 토큰을 생성할 수 있다.
- 그리고 해당 필드에 토큰을 넣은 후, 나중에 서버에서 사용한다.
- CSRF 지원을 비활성화지 말자. (단 REST API 서버로 실행되는 애플리케이션의 경우는 CSRF를 disable 해야 함)
- .csrf().disable()로 비활성 가능.

```java

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	...

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		...
        .and().csrf();
	}
	...

}

```

## 4-4. 사용자 인지하기

사용자가 로그인되었음을 아는 정도로는 충분하지 않을 떄가 있다. 사용자 경험에 맞추려면 그들이 누구인지 아는 것도 중요.

### 사용자가 누구인지 결정하는 방법

- Principal 객체를 컨트롤러 메소드에 주입.
    - 보안과 관련없는 코드가 혼재하는 단점

```java
@PostMapping
public String processOrder(...Principal principal){
		User user=userRepository.findByUsername(principal.getName());
		order.setUesr(user):
}
```

- Authentication 객체를 컨트롤러 메소드에 주입
    - getPrinciplal()은 User 타입으로 변환해야 함.

```java
@PostMapping
public String processOrder(...Authentication authentication){
		User user=(User)authentication.getPrincipal();
		...
		order.setUser(user);
}
```

- @AuthenticationPrincipal 애노테이션을 메소드에 지정.
    - 타입 변환이 필요없고, Authentication과 동일하게 보안 특정 코드만 갖음.

```java
@PostMapping
public String processOrder(@Valid Order order,Errors errors,SessionStatus sessionStatus,@AuthenticationPrincipal User user){
		if(errors.hasErrors()){
		return"orderForm";
		}
		order.setUser(user);
		orderRepository.save(order);
		sessionStatus.setComplete();

		return"redirect:/";
		}
```

- SecurityContextHolder를 사용해서 보안 컨텍스트를 얻는다,
    - 보안 특정 코드가 많은 단점
    - 컨트롤러의 처리 메소드는 물론이고, 애플리케이션의 어디서든 사용할 수 있는 장점.

```java
Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
User user=(User)authentication.getPrincipal();
```

### 정리

- 스프링 시큐리티의 자동-구성은 보안을 시작하는 데 좋은 방법임. 그러나 대부분의 애플리케이션에서는 나름의 보안 요구사항을 충족하기 위해 보안 구성이 필요하다.
- 사용자 정보는 여러 종류의 사용자 스토어에 저장되고 관리될 수 있다. (관계형 데이터베이스, LDAP 등)
- 스프링 시큐리티는 자동으로 CSRF 공격을 방지한다.
- 인증된 사용자에 관한 정보는 SecurityContext 객체를 통해 얻거나, @AuthenticationPrincipal을 사용해서 컨트롤러에 주입하면 된다.