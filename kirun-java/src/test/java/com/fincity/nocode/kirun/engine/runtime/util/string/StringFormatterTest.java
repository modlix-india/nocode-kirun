package com.fincity.nocode.kirun.engine.runtime.util.string;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StringFormatterTest {

	@Test
	void test() {
		
		assertEquals("Hello Kiran", StringFormatter.format("Hello $", "Kiran"));
		assertEquals("$Hello Kiran", StringFormatter.format("\\$Hello $", "Kiran"));
		assertEquals("Hi Hello How are you $?", StringFormatter.format("Hi Hello How are you $?"));
		assertEquals("Hi Hello How are you 123$", StringFormatter.format("Hi Hello How are you $$$$", "1", "2", "3"));
		assertEquals("Hi Hello How are you $123", StringFormatter.format("Hi Hello How are you \\$$$$", "1", "2", "3"));
		assertEquals("Hi Hello How are you 12$$", StringFormatter.format("Hi Hello How are you $$$\\$", "1", "2"));
	}

}
