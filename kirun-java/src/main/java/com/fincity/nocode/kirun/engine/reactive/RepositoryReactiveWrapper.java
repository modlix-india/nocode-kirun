package com.fincity.nocode.kirun.engine.reactive;

import com.fincity.nocode.kirun.engine.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RepositoryReactiveWrapper<T> implements ReactiveRepository<T> {

	private Repository<T> repo;


	public RepositoryReactiveWrapper(Repository<T> repo) {

		this.repo = repo;
	}

	@Override
	public Mono<T> find(String namespace, String name) {
		
		return Mono.justOrEmpty(this.repo.find(namespace, name));
	}

	@Override
	public Flux<String> filter(String name) {

		return Flux.fromIterable(this.repo.filter(name));
	}
}
