package com.fincity.nocode.kirun.engine.runtime.graph;

import java.util.Set;

public interface GraphVertexType<K> {

	public K getUniqueKey();
	
	public Set<String> getDepenedencies();
}
