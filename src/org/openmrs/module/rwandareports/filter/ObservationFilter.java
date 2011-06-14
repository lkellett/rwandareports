package org.openmrs.module.rwandareports.filter;

import java.text.SimpleDateFormat;

import org.openmrs.Obs;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;

public class ObservationFilter implements ResultFilter {
	
	private String finalDateFormat = "ddMMMyy";
	
	public Object filter(Object value) {
		Obs result = (Obs)value;
		
		StringBuilder sb = new StringBuilder();
		
		if(result != null)
		{
			Double val = result.getValueNumeric();
			sb.append(val.intValue());
			sb.append(" @");
			sb.append(new SimpleDateFormat(finalDateFormat).format(result.getObsDatetime()));
			sb.append(" ");	
		}
		
		return sb.toString();
	}

}
