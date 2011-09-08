package org.openmrs.module.rwandareports.filter;

import org.openmrs.DrugOrder;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;

public class DrugDosageFrequencyFilter implements ResultFilter {
	
	private String finalDateFormat = null;
	
	public Object filter(Object value) {
		DrugOrder drugOrder = (DrugOrder)value;
		
		StringBuilder result = new StringBuilder();
		
		if(drugOrder != null && drugOrder.getDrug() != null)
		{
			result.append(drugOrder.getDrug().getName());
			result.append(" ");
			result.append(drugOrder.getDose());
			result.append(drugOrder.getUnits());
			result.append(" ");
			String freq = drugOrder.getFrequency();
			if(freq != null)
			{
				if(freq.indexOf("x") > -1)
				{
					result.append(freq.substring(0, freq.indexOf("x")));
				}
				else
				{
					result.append(freq);
				}
			}
		}
		
		return result.toString();
	}


	public String getFinalDateFormat() {
		return finalDateFormat;
	}

	public void setFinalDateFormat(String finalDateFormat) {
		this.finalDateFormat = finalDateFormat;
	}
	
	
}
