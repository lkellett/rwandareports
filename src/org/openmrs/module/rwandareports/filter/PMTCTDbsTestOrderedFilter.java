package org.openmrs.module.rwandareports.filter;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;


public class PMTCTDbsTestOrderedFilter implements ResultFilter {
	
	public String filter(String obsResult) {
		if(obsResult.equals("Obs not present"))
		{
			return "Yes";
		}
		else
		{
			return "No";
		}
	}
	
}
