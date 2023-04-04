package com.fincity.nocode.kirun.engine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HybridRepository<T> implements Repository<T> {

	private Repository<T>[] repos;

	@SafeVarargs
	public HybridRepository(Repository<T>... repositories) {

		this.repos = repositories;
	}

	@Override
	public T find(String namespace, String name) {

		for (Repository<T> repo : this.repos) {
			T s = repo.find(namespace, name);
			if (s != null)
				return s;
		}

		return null;
	}

	@Override
	public List<String> filter(String name) {

		Set<String> result = new HashSet<>();

		for (var repo : this.repos) {
			result.addAll(repo.filter(name));
		}

		return List.copyOf(result);
	}
}
