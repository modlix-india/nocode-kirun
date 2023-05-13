package com.fincity.nocode.kirun.engine.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ReactiveHybridRepository<T> implements ReactiveRepository<T> {

	private ReactiveRepository<T>[] repos;

	@SafeVarargs
	public ReactiveHybridRepository(ReactiveRepository<T>... repositories) {

		this.repos = repositories;
	}

	@Override
	public Mono<T> find(String namespace, String name) {
		
		return Flux.fromArray(repos).flatMap(e -> Flux.from(e.find(namespace, name))).next();
	}

	@Override
	public Flux<String> filter(String name) {

		return Flux.fromArray(repos).flatMap(e -> e.filter(name));
	}
}


