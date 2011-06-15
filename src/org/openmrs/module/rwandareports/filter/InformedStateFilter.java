package org.openmrs.module.rwandareports.filter;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;


public class InformedStateFilter implements ResultFilter {
	
	public Object filter(Object value) {
		String state = (String)value;
		
		if(state.equals("PATIENT INFORMED OF DIAGNOSIS"))
		{
			return "Oui";
		}
		
		return "Non";
	}
	
}
