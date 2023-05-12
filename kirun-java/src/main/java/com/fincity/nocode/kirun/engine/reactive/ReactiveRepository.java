package com.fincity.nocode.kirun.engine.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveRepository<T> {

	public Mono<T> find(String namespace, String name);
	public Flux<String> filter(String name);
}
