package com.fincity.nocode.kirun.engine;

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

}
