package org.openmrs.module.rwandareports.filter;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;


public class RemoveDecimalFilter implements ResultFilter {
	
	public Object filter(Object value) {
		String result = (String)value;
		if(result != null && result.indexOf(".") > -1)
		{
			result = result.substring(0, result.indexOf(".")).trim();
		}
		
		return result;
	}

	public Object filterWhenNull() {
	    // TODO Auto-generated method stub
	    return null;
    }	
}
