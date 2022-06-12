package com.fincity.nocode.kirun.engine.util.string;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;

public class StringUtil {

	private StringUtil() {}
	
	public static int nthIndex(String str, char c, int occurance) {
		return nthIndex(str, c, 0, occurance);
	}
	
	public static int nthIndex(String str, char c, int from, int occurance) {
		
		if (str == null)
			throw new KIRuntimeException("String cannot be null");
		
		if (from < 0 || from >= str.length())
			throw new KIRuntimeException(StringFormatter.format("Cannot search from index : $", from));
		
		if (occurance <= 0 || occurance > str.length())
			throw new KIRuntimeException(StringFormatter.format("Cannot search for occurance : $", occurance));
		
		while (from < str.length()) {
			
			if (str.charAt(from) == c) {
				-- occurance;
				if (occurance == 0)
					return from;
			}
			
			++ from;
		}
		
		return -1;
	}
}
