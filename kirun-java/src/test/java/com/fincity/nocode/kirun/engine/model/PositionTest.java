package com.fincity.nocode.kirun.engine.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PositionTest {

	@Test
	void test() {
		
		Position p = new Position();
		p.setLeft(10f);
		p.setTop(-11f);
		
		Position p2 = new Position();
		p2.setLeft(10f);
		p2.setTop(-11f);
		
		assertEquals(p, p2);
	}
}
