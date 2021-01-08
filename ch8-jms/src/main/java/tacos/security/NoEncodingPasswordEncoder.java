package tacos.security;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호를 암호화하지 않는 클래스 (테스트를 위함.)
 */
public class NoEncodingPasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(CharSequence charSequence) {
		return charSequence.toString();
	}

	@Override
	public boolean matches(CharSequence charSequence, String s) {
		return charSequence.toString().equals(s);
	}

}
