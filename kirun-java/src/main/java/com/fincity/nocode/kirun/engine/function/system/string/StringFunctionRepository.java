package com.fincity.nocode.kirun.engine.function.system.string;

import java.util.Map;

import com.fincity.nocode.kirun.engine.Repository;
import com.fincity.nocode.kirun.engine.function.Function;
import com.fincity.nocode.kirun.engine.function.system.AbstractUnaryFunction;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;

public class StringFunctionRepository implements Repository<Function> {

	private static final Map<String, Function> repoMap = Map.ofEntries(
			AbstractUnaryFunction.ofEntryString("Trim", String::trim),
			AbstractUnaryFunction.ofEntryString("LowerCase", String::toLowerCase),
			AbstractUnaryFunction.ofEntryString("UpperCase", String::toUpperCase),
			AbstractUnaryFunction.ofEntryStringBooleanOutput("IsBlank", String::isBlank),
			AbstractUnaryFunction.ofEntryStringBooleanOutput("IsEmpty", String::isEmpty),

			AbstractBinaryStringFunction.ofEntryAsStringBooleanOutput("Contains", String::contains),
			AbstractBinaryStringFunction.ofEntryAsStringBooleanOutput("EndsWith", String::endsWith),
			AbstractBinaryStringFunction.ofEntryAsStringBooleanOutput("EqualsIgnoreCase", String::equalsIgnoreCase),
			AbstractBinaryStringFunction.ofEntryAsStringBooleanOutput("Matches", String::matches),
			AbstractBinaryStringFunction.ofEntryAsStringIntegerOutput("IndexOf", String::indexOf),
			AbstractBinaryStringFunction.ofEntryAsStringIntegerOutput("LastIndexOf", String::lastIndexOf),
			AbstractBinaryStringFunction.ofEntryAsStringAndIntegerStringOutput("Repeat", String::repeat),

			AbstractTertiaryStringFunction.ofEntryAsStringStringIntegerIntegerOutput("IndexOfWithStartPoint",
					String::indexOf),
			AbstractTertiaryStringFunction.ofEntryAsStringStringIntegerIntegerOutput("LastIndexOfWithStartPoint",
					String::lastIndexOf),
			AbstractTertiaryStringFunction.ofEntryAsStringStringStringStringOutput("Replace", String::replace),
			AbstractTertiaryStringFunction.ofEntryAsStringStringStringStringOutput("ReplaceFirst",
					String::replaceFirst),
			AbstractTertiaryStringFunction.ofEntryAsStringIntegerIntegerStringOutput("SubString", String::substring));

	@Override
	public Function find(String namespace, String name) {
		if (!namespace.equals(Namespaces.STRING)) {
			return null;
		}
		return repoMap.get(name);
	}

}
