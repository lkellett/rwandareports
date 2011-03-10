package org.openmrs.module.rwandareports.filter;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;


public class GroupStateFilter implements ResultFilter {
	
	public String filter(String state) {
		String[] wordsState = state.split(" ");
		return wordsState[(wordsState.length)-1];
	}
	
}
