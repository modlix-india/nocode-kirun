package com.fincity.nocode.kirun.engine;

@FunctionalInterface
public interface Repository<T> {

	public T find(String namespace, String name);
}
