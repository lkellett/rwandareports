package org.openmrs.module.rwandareports.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GetDate {

	public static String getCalendarMonthDate(int months){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, months);
		Date monthsBackOrNext = cal.getTime();
		
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
		String stringDate=sdf.format(monthsBackOrNext);
		
		return stringDate;
	}
}
