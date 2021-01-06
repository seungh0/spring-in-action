package tacos.data;

import org.springframework.data.repository.CrudRepository;
import tacos.Taco;

public interface TacoRepository extends CrudRepository<Taco, Long> { // 애플리케이션이 시작될 때, 스프링 데이터 JPA가 각 인터페이스 구현채를 자동으로 생성해줌.

}
