package com.fincity.nocode.kirun.engine.runtime.util.string;

public class StringFormatter {

	public static String format(String formatString, Object... params) {

		if (params == null || params.length == 0)
			return formatString;

		StringBuilder sb = new StringBuilder();
		int ind = 0;
		char chr = 0;
		char prevchar = chr;
		final int length = formatString.length();

		for (int i = 0; i < length; i++) {
			chr = formatString.charAt(i);

			if (chr == '$' && prevchar == '\\')
				sb.setCharAt(i - 1, chr);
			else if (chr == '$' && ind < params.length)
				sb.append(params[ind++]);
			else
				sb.append(chr);

			prevchar = chr;
		}

		return sb.toString();
	}

	private StringFormatter() {
	}
}
