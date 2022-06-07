package com.fincity.nocode.kirun.engine.runtime.util.graph;

import java.util.Set;

public interface GraphVertexType<K> {

	public K getUniqueKey();
	
	public Set<K> getDepenedencies();
}
