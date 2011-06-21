package org.openmrs.module.rwandareports.filter;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;


public class BorFStateFilter implements ResultFilter {
	
	public Object filter(Object value) {
		String state = (String)value;
		
		if(state.contains("Breastfeeding"))
		{
			return "B";
		}
		else if(state.contains("Artificial"))
		{
			return "F";
		}
		
		return state;
	}
	
}
