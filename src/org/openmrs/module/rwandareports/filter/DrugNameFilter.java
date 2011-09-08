package org.openmrs.module.rwandareports.filter;

import org.openmrs.DrugOrder;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;

public class DrugNameFilter implements ResultFilter {
	
	private String finalDateFormat = null;
	
	public Object filter(Object value) {
		DrugOrder drugOrder = (DrugOrder)value;
		String result = drugOrder.getDrug().getName();
		
		while(result.indexOf("(") > -1)
		{
			int initialBracket = result.indexOf("(");
			int finalBracket = result.indexOf(")", initialBracket);
			
			if(finalBracket > -1 && finalBracket < result.length() - 1)
			{
				result = result.substring(0, initialBracket) + "+" + result.substring(finalBracket + 1);
			}
			else
			{
				result = result.substring(0, initialBracket);
			}
		}
		
		return result;
	}


	public String getFinalDateFormat() {
		return finalDateFormat;
	}

	public void setFinalDateFormat(String finalDateFormat) {
		this.finalDateFormat = finalDateFormat;
	}
	
	
}
