package com.fincity.nocode.kirun.engine.function.system.date;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Date;

import com.fincity.nocode.kirun.engine.exception.KIRuntimeException;
import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.util.date.DateTimePatternUtil;
import com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil;

import static com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil.getEpochTime;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DateFunctionRepository implements ReactiveRepository<ReactiveFunction> {

	private static final Map<String, ReactiveFunction> REPO_MAP = Map.ofEntries(

			AbstractDateFunction.ofEntryDateAndBooleanOutput("IsLeapYear", inputDate -> {
				int year = getRequiredField(inputDate, Calendar.YEAR);
				return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
			}),

	        AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetDate", inputDate -> getRequiredField(inputDate, Calendar.DATE)),

	        AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetDay", inputDate -> getRequiredField(inputDate, Calendar.DAY_OF_WEEK) - 1),

	        AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetFullYear", inputDate -> getRequiredField(inputDate, Calendar.YEAR)),

			AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetMonth", inputDate -> getRequiredField(inputDate, Calendar.MONTH)) ,

	        AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetHours", inputDate -> getRequiredField(inputDate, Calendar.HOUR_OF_DAY)),

			AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetMinutes", inputDate -> getRequiredField(inputDate, Calendar.MINUTE)),
			
			AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetSeconds", inputDate -> getRequiredField(inputDate, Calendar.SECOND)),
					
			AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetMilliSeconds", inputDate -> getRequiredField(inputDate, Calendar.MILLISECOND)),

			AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetTime", GetTimeInMillisUtil::getEpochTime),

			AbstractDateFunction.ofEntryDateWithIntegerUnitWithOutputName("AddTime", (inputDate, value, unit ) -> changeAmountToUnit(inputDate, unit , value, true)),

			AbstractDateFunction.ofEntryDateWithIntegerUnitWithOutputName("SubtractTime", (inputDate, value, unit ) -> changeAmountToUnit(inputDate, unit , value, false))			
	);

	private static final List<String> FILTERABLE_NAMES = REPO_MAP.values()
	        .stream()
	        .map(ReactiveFunction::getSignature)
	        .map(FunctionSignature::getFullName)
	        .toList();

	private static int getRequiredField(String inputDate, int field) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTime(new Date(getEpochTime(inputDate)));
		return cal.get(field);
	}


	private static String changeValueInGivenField(String inputDate, String unit, int value){

		ZonedDateTime zdt = ZonedDateTime.parse(inputDate);

		switch (unit) {
			case "MILLISECOND" : 
				zdt.withNano(value * 1000000);
				break;
			case "SECOND" : 
				zdt.withSecond(value);
				break;
			case "MINUTE" : 
				zdt.withMinute(value);
				break;
			case "HOUR" : 
				zdt.withHour(value);
				break;
			case "DAY" : 
				zdt.withDayOfMonth(value);
				break;
			case "MONTH" : 
				zdt.withMonth(value);
				break;
			case "YEAR" : 
				zdt.withYear(value);
				break;
		}

		return zdt.format(DateTimePatternUtil.getPattern(inputDate));

	}

	private static String changeAmountToUnit(String inputDate, String unit, int value, boolean isAdd){

		ZonedDateTime zdt = ZonedDateTime.parse(inputDate);

		ChronoUnit fieldName = switch (unit) {
					case "MILLISECOND" -> ChronoUnit.MILLIS;
					case "SECOND" -> ChronoUnit.SECONDS;
					case "MINUTE" -> ChronoUnit.MINUTES;
					case "HOUR" -> ChronoUnit.HOURS;
					case "DAY" -> ChronoUnit.DAYS;
					case "MONTH" -> ChronoUnit.MONTHS;
					case "YEAR" -> ChronoUnit.YEARS;
					default -> throw new KIRuntimeException("No such unit: " + unit);};

		ZonedDateTime result = isAdd ? zdt.plus(value, fieldName): zdt.minus(value, fieldName);

		return result.format(DateTimePatternUtil.getPattern(inputDate));
	}

	@Override
	public Mono<ReactiveFunction> find(String namespace, String name) {

		if (!namespace.equals(Namespaces.DATE))
			return Mono.empty();

		return Mono.just(REPO_MAP.get(name));
	}

	@Override
	public Flux<String> filter(String name) {

		return Flux.fromIterable(FILTERABLE_NAMES)
		        .filter(e -> e.toLowerCase()
		                .indexOf(name.toLowerCase()) != -1);
	}

}
