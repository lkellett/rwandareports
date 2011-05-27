package org.openmrs.module.rwandareports.filter;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;


public class RemoveTimestampFilter implements ResultFilter {
	
	public String filter(String date) {
		if(date != null && date.indexOf("00:") > -1)
		{
			date = date.substring(0, date.indexOf("00:")).trim();
		}
		
		return date;
	}	
}
