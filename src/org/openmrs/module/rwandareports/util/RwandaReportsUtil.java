package org.openmrs.module.rwandareports.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class RwandaReportsUtil {
	
	public static Calendar findSundayBeforeOrEqualToStartDate(Date startDate){
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(startDate);
		for (int i = 0; i <=7; i++){
			if (Calendar.SUNDAY == cal.get(Calendar.DAY_OF_WEEK)){
				return (Calendar) cal;
			}
			cal.add(Calendar.DATE, -1);
		}
		throw new RuntimeException("Unable to find first Sunday before or equal to startDate parameter.");
	}
	
	
	public static boolean isWeekday(Date startDate){
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(startDate);
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
				|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
			return false;
		return true;
			
	}
}
