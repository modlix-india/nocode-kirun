package com.fincity.nocode.kirun.engine.repository.reactive;

import com.fincity.nocode.kirun.engine.json.schema.Schema;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.repository.KIRunSchemaRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class KIRunReactiveSchemaRepository implements ReactiveRepository<Schema> {

	private KIRunSchemaRepository schemaRepo;

	public KIRunReactiveSchemaRepository() {

		this.schemaRepo = new KIRunSchemaRepository();
	}

	@Override
	public Mono<Schema> find(String namespace, String name) {

		return Mono.justOrEmpty(this.schemaRepo.find(namespace, name));
	}

	@Override
	public Flux<String> filter(String name) {
		return Flux.fromIterable(this.schemaRepo.filter(name));
	}

}
