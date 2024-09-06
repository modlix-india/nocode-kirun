package com.fincity.nocode.kirun.engine.function.system.date;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Date;

import com.fincity.nocode.kirun.engine.function.reactive.ReactiveFunction;
import com.fincity.nocode.kirun.engine.model.FunctionSignature;
import com.fincity.nocode.kirun.engine.namespaces.Namespaces;
import com.fincity.nocode.kirun.engine.reactive.ReactiveRepository;
import com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil;

import static com.fincity.nocode.kirun.engine.util.date.GetTimeInMillisUtil.getEpochTime;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DateFunctionRepository implements ReactiveRepository<ReactiveFunction> {

	private static final Map<String, ReactiveFunction> REPO_MAP = Map.ofEntries(

	        AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetDate", "date",
	                inputDate -> getRequiredField(inputDate, Calendar.DATE)),

	        AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetDay", "day",
	                inputDate -> getRequiredField(inputDate, Calendar.DAY_OF_WEEK)),

	        AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetFullYear", "year",
	                inputDate -> getRequiredField(inputDate, Calendar.YEAR)),

			AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetMonth", "month",
	                inputDate -> getRequiredField(inputDate, Calendar.MONTH)),

	        AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetHours", "hours",
	                inputDate -> getRequiredField(inputDate, Calendar.HOUR_OF_DAY)),

			AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetMinutes", "minutes",
	                inputDate -> getRequiredField(inputDate, Calendar.MINUTE)),
			
			AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetSeconds", "seconds",
					inputDate -> getRequiredField(inputDate, Calendar.SECOND)),
					
			AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetMilliSeconds", "millis",
					inputDate -> getRequiredField(inputDate, Calendar.MILLISECOND)),

			AbstractDateFunction.ofEntryDateAndIntegerWithOutputName("GetTime", "time", 
					GetTimeInMillisUtil::getEpochTime)
				

	);

	private static final List<String> FILTERABLE_NAMES = REPO_MAP.values()
	        .stream()
	        .map(ReactiveFunction::getSignature)
	        .map(FunctionSignature::getFullName)
	        .toList();

	private static int getRequiredField(String inputDate, int field) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(getEpochTime(inputDate)));
		System.out.println(cal);
		return cal.get(field);
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
