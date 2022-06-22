package com.fincity.nocode.kirun.engine.util.string;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class StringUtilTest {

	@Test
	void testSplitAtFirstOccurance() {

		assertArrayEquals(new String[] { null, null }, StringUtil.splitAtFirstOccurance(null, 'c'));
		assertArrayEquals(new String[] {"abcd", null}, StringUtil.splitAtFirstOccurance("abcd", 's'));
		assertArrayEquals(new String[] {"abcd", "f"}, StringUtil.splitAtFirstOccurance("abcdef", 'e'));
		assertArrayEquals(new String[] {"abcde", ""}, StringUtil.splitAtFirstOccurance("abcdef", 'f'));
		
	}

}
