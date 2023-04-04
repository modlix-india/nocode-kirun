package com.fincity.nocode.kirun.engine;

import java.util.List;

public interface Repository<T> {

	public T find(String namespace, String name);
	public List<String> filter(String name);
}
