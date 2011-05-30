package org.openmrs.module.rwandareports.filter;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;


public class RemoveDecimalFilter implements ResultFilter {
	
	public String filter(String result) {
		if(result != null && result.indexOf(".") > -1)
		{
			result = result.substring(0, result.indexOf(".")).trim();
		}
		
		return result;
	}	
}
