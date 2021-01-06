package tacos.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;

/**
 * SecurityConfig 클래스
 * - 사용자의 HTTP 요청 경로에 대해 접근 제한과 같은 보안 관련 처리 설정.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private final DataSource dataSource;

	public SecurityConfig(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	//	// 사용지 인증 정보를 구성하는 메소드 (JDBC 기반 사용자 스토어)
//	@Override
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth
//				.jdbcAuthentication()
//				.dataSource(dataSource)
//				.usersByUsernameQuery(
//						"select username, password, enabled from users where username=?")
//				.authoritiesByUsernameQuery(
//						"select username, authority from authorities where username=?")
////				.passwordEncoder(new BCryptPasswordEncoder());
//				.passwordEncoder(new NoEncodingPasswordEncoder()); // 비밀번호 암호화(encoder)를 지정.
//	}

//	// 사용자 인증 정보를 구성하는 메소드 (인메모리 사용자 스토어)
//	@Override
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication()
//				.withUser("user1")
//				.password("{noop}password1") // {noop}를 지정하여 비밀번호를 암호화 하지 않았음.
//				.authorities("ROLE_USER") // .roles("USER")
//				.and()
//				.withUser("user2") // .withUser()로 여러 사용자를 지정할 수 있음.
//				.password("{noop}password2")
//				.authorities("ROLE_USER");
//	}

	// HTTP 보안을 구성하는 메소드
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
