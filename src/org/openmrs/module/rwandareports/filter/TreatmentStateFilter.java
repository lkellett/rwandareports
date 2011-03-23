package org.openmrs.module.rwandareports.filter;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;

public class TreatmentStateFilter implements ResultFilter {
  private String stateResult;
	public String filter(String treatmentState) {
		
		if(treatmentState.equals("ON ANTIRETROVIRALS"))			
			stateResult="ART";
		else if(treatmentState.equals("FOLLOWING"))
			stateResult="Pre-ART";
		else if(treatmentState.equals("GROUP FOLLOWING"))
			stateResult="-";
		else
		{
			String[] wordsState=treatmentState.split(" ");
			stateResult=wordsState[(wordsState.length)-1];			
		}		
		return stateResult;
	}

}
