# 스프링 인 액션

## 4. 스프링 시큐리티

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

### 스프링 시큐리티에서 제공하는 사용자 스토어 구성 방법

1. 인메모리 사용자 스토어
    - 테스트 목적이나 간단한 애플리케이션에서는 편리.
    - 사용자의 정보의 추가나 변경이 쉽지 않음.
    - 사용자의 추가, 삭제, 변경을 해야 한다면 보안 구성 코드를 변경한 후 애플리케이션을 다시 빌드하고 배포, 설치 해야함.

```java
@Override
protected void configure(AuthenticationManagerBuilder auth)throws Exception{
		auth.inMemoryAuthentication()
		.withUser("user1")
		.password("{noop}password1") // {noop}를 지정하여 비밀번호를 암호화 하지 않았음.
		.authorities("ROLE_USER") // .roles("USER")
		.and()
		.withUser("user2") // .withUser()로 여러 사용자를 지정할 수 있음.
		.password("{noop}password2")
		.authorities("ROLE_USER");
		}
```

2. JDBC 기반 사용자 스토어

```java
@Override
protected void configure(AuthenticationManagerBuilder auth)throws Exception{
		auth
		.jdbcAuthentication()
		.dataSource(dataSource);
		}

```

```java
@Override
protected void configure(AuthenticationManagerBuilder auth)throws Exception{
		auth
		.jdbcAuthentication()
		.dataSource(dataSource)
		.usersByUsernameQuery("select username, password, enabled from users where username=?")
		.authoritiesByUsernameQuery("select username, authority from authorities where username=?")
		}
```

- 스프링 시큐리티의 것과 다른 데이터베이스를 사용한다면, 스프링 스큐리티의 SQL 쿼리를 커스터마이징 할 수 있음.

```java
@Override
protected void configure(AuthenticationManagerBuilder auth)throws Exception{
		auth
		.jdbcAuthentication()
		.dataSource(dataSource)
		.usersByUsernameQuery("select username, password, enabled from users where username=?")
		.authoritiesByUsernameQuery("select username, authority from authorities where username=?")
		.passwordEncoder(new BCryptPasswordEncoder()); // 비밀번호 암호화(encoder)를 지정.
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

	private final String fullName;

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

#### 사용자 등록하기
```java
@Controller
@RequestMapping("/register")
public class RegistrationController {

	private UserRepository userRepository;

	private PasswordEncoder passwordEncoder;

	public RegistrationController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping
	public String registerForm() {
		return "registration";
	}

	@PostMapping
	public String processRegistration(RegistrationForm form) {
		userRepository.save(form.toUser(passwordEncoder));
		return "redirect:/login";
	}

}
``````
```