package com.fincity.nocode.kirun.engine.repository.reactive;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunctionWrapper;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunFunctionRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class KIRunReactiveFunctionRepository implements ReactiveRepository<ReactiveFunction> {

	private KIRunFunctionRepository funRepo;

	public KIRunReactiveFunctionRepository() {

		this.funRepo = new KIRunFunctionRepository();
	}

	@Override
	public Mono<ReactiveFunction> find(String namespace, String name) {

		return Mono.justOrEmpty(this.funRepo.find(namespace, name))
		        .map(e -> new ReactiveFunctionWrapper(e, this.funRepo));
	}

	@Override
	public Flux<String> filter(String name) {
		return Flux.fromIterable(this.funRepo.filter(name));
	}

}
