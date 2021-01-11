package tacos.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tacos.data.TacoRepository;
import tacos.domain.Taco;

@RestController
@RequestMapping("/design")
@CrossOrigin(origins = "*")
public class DesignTacoController {

	private final TacoRepository tacoRepository;

	public DesignTacoController(TacoRepository tacoRepository) {
		this.tacoRepository = tacoRepository;
	}

	@GetMapping("/recent")
	public Flux<Taco> recentTacos() {
		return tacoRepository.findAll().take(12);
	}

	@GetMapping("/{id}")
	public Mono<Taco> tacoById(@PathVariable("id") Long id) {
		return tacoRepository.findById(id);
	}

	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Taco> postTaco(@RequestBody Taco taco) {
		return tacoRepository.save(taco);
	}

}
