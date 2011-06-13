package org.openmrs.module.rwandareports.filter;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;


public class PMTCTDbsTestOrderedFilter implements ResultFilter {
	
	private String initialConceptName;
	private String confirmConceptName;
	
	public Object filter(Object value) {
		String obsResult = (String)value;
		if(obsResult.contains(initialConceptName + ":") || obsResult.contains(confirmConceptName + ":"))
		{
			return "Yes";
		}
		else
		{
			return "No";
		}
	}

	public String getInitialConceptName() {
		return initialConceptName;
	}

	public void setInitialConceptName(String initialConceptName) {
		this.initialConceptName = initialConceptName;
	}

	public String getConfirmConceptName() {
		return confirmConceptName;
	}

	public void setConfirmConceptName(String confirmConceptName) {
		this.confirmConceptName = confirmConceptName;
	}
	
}
