package org.openmrs.module.rwandareports.filter;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;

public class AccompagnateurStatusFilter implements ResultFilter{
	private String result;
	public Object filter(Object value) {
		String accompagnateurName = (String)value;
		if (accompagnateurName.length() > 0) 
		result = "Y";
		else
		result = "N";
		
		return result.toString();
	}

	public Object filterWhenNull() {
		// TODO Auto-generated method stub
		return null;
	}

}
