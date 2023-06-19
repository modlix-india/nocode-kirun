package com.fincity.nocode.kirun.engine.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FunctionOutput {

	private List<EventResult> fo;

	private Iterator<EventResult> foIterator;
	private FunctionOutputGenerator generator;

	public FunctionOutput(List<EventResult> fo) {

		this.fo = fo;
		this.foIterator = this.fo.iterator();
	}

	public FunctionOutput(FunctionOutputGenerator gen) {

		this.fo = new ArrayList<>();
		this.generator = gen;
	}

	public EventResult next() {

		if (this.foIterator != null) {
			if (foIterator.hasNext())
				return foIterator.next();
			return null;
		}

		EventResult er = generator.next();
		if (er != null)
			this.fo.add(er);
		return er;
	}

	public List<EventResult> allResults() {
		return this.fo;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;
		if (!(obj instanceof FunctionOutput))
			return false;

		FunctionOutput other = (FunctionOutput) obj;
		if (this.fo == null)
			return other.fo == null;

		if (this.fo.size() != other.fo.size())
			return false;

		for (int i = 0; i < this.fo.size(); i++) {
			if (!this.fo.get(i).equals(other.fo.get(i)))
				return false;
		}

		return true;
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
