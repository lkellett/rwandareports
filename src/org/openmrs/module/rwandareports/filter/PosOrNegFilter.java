package org.openmrs.module.rwandareports.filter;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;


public class PosOrNegFilter implements ResultFilter {
	
	public Object filter(Object value) {
		String enrollment = (String)value;
		
		if(enrollment.equals("Not Enrolled"))
		{
			return "Neg";
		}
		else if(enrollment.contains("Enrolled"))
		{
			return "Pos";
		}
		
		return enrollment;
	}
	
}
