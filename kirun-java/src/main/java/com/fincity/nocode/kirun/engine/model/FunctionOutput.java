package com.fincity.nocode.kirun.engine.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FunctionOutput {

	private List<EventResult> fo;

	private Iterator<EventResult> foIterator;
	private FunctionOutputGenerator generator;

	// How many events have been pulled out with next(). Needed to put a branch back where it was
	// when an execution is resumed: the iterator itself has no readable position.
	private int consumed;

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
			if (foIterator.hasNext()) {
				this.consumed++;
				return foIterator.next();
			}
			return null;
		}

		EventResult er = generator.next();
		if (er != null) {
			this.fo.add(er);
			this.consumed++;
		}
		return er;
	}

	/** How many events have been pulled from this output so far. */
	public int getConsumedCount() {
		return this.consumed;
	}

	/**
	 * Whether events are produced lazily by a generator rather than read from a fixed list. The
	 * distinction matters when resuming: a generator's position is external state (a loop keeps it
	 * in the execution context via LoopCursor), whereas a list's position is only this iterator.
	 */
	public boolean isGeneratorBacked() {
		return this.generator != null;
	}

	/**
	 * Winds a freshly created output forward to a position recorded in a snapshot.
	 *
	 * For a list-backed output that means replaying next() the recorded number of times, which is
	 * safe because the list was fixed up front. For a generator-backed output it is deliberately a
	 * no-op: the generator was rebuilt from the restored execution context and is already at the
	 * right position, so replaying next() here would advance a loop a second time and skip
	 * iterations.
	 */
	public void restoreTo(int consumedCount) {

		if (this.generator != null)
			return;

		this.foIterator = this.fo.iterator();
		this.consumed = 0;

		for (int i = 0; i < consumedCount && this.foIterator.hasNext(); i++)
			this.next();
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
