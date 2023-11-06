package com.fincity.nocode.kirun.engine.util.date;

public class GetTimeZoneOffsetUtil {
	
	private GetTimeZoneOffsetUtil() {
		
	}

	 public static int getOffset(String inputDate) {
	       
	        if (inputDate.contains("Z") || inputDate.contains("+00:00") || inputDate.contains("-00:00")) {
	            return 0;
	        } else {

	            String[] hourMinutes = inputDate.contains("+")
	                    ? inputDate.substring(inputDate.lastIndexOf("+") + 1).split(":")
	                    : inputDate.substring(inputDate.lastIndexOf("-") + 1).split(":");

	            int offset = inputDate.contains("+")
	                    ? -1 * (Integer.parseInt(hourMinutes[0]) * 60 + Integer.parseInt(hourMinutes[1]))
	                    : 1 * (Integer.parseInt(hourMinutes[0]) * 60 + Integer.parseInt(hourMinutes[1]));

	            return offset;
	        }
	    }
	}














