package tacos.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tacos.User;
import tacos.data.UserRepository;

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
