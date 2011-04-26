package org.openmrs.module.rwandareports.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.openmrs.Concept;
import org.openmrs.api.context.Context;

public class RwandaReportsUtil {
	
	/**
	 * Finds the report equal to or before start date
	 * @param startDate
	 * @return Calendar representing the found Sunday.
	 */
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
	
	
	/**
	 * decides if a day is Mon through Fri
	 * @param startDate
	 * @return true if weekday, else false
	 */
	public static boolean isWeekday(Date startDate){
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(startDate);
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
				|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
			return false;
		return true;
			
	}
	
	public static Integer getConceptIdFromUuid(String uuid){
		Concept c = Context.getConceptService().getConceptByUuid(uuid);
		if (c != null)
			return c.getConceptId();
		else 
			throw new IllegalArgumentException("Unable to find concept for uuid " + uuid + ".  Take a look at PrimaryCareReportConstants in RwandaReports module to see what's missing or wrong.");		
	}
}
