package org.openmrs.module.rwandareports.filter;

import org.openmrs.api.context.Context;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;


public class DiscordantCoupleFilter implements ResultFilter {
	
	private String workflowName = Context.getAdministrationService().getGlobalProperty("reports.pmtctDiscordantCoupleWorkflowState");
	
	public String filter(String state) {
		if(state.toLowerCase().contains(workflowName.toLowerCase()))
		{
			return "Yes";
		}
		else
		{
			return "No";
		}
	}
	
}
