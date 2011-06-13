package org.openmrs.module.rwandareports.filter;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;


public class GroupStateFilter implements ResultFilter {
	
	public Object filter(Object value) {
		String state = (String)value;
		String[] wordsState = state.split(" ");
		return wordsState[(wordsState.length)-1];
	}
	
}
