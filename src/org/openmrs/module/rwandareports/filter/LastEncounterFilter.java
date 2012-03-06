package org.openmrs.module.rwandareports.filter;

import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;

public class LastEncounterFilter implements ResultFilter {
private String lastVist;
public Object filter(Object value) {
	String lastEncounterName = (String)value;
		if(lastEncounterName.equals("ADULT INITIAL VISIT"))
			lastVist="ADULT INITIAL";
		else if(lastEncounterName.equals("ADULT RETURN VISIT"))
			lastVist="ADULT RETURN";
		else if(lastEncounterName.equals("PEDIATRIC INITIAL VISIT"))
			lastVist="PEDI INITIAL";
		else if(lastEncounterName.equals("PEDIATRIC RETURN VISIT"))
			lastVist="PEDI RETURN";
		else
			lastVist=lastEncounterName;
		return lastVist;
	}
public Object filterWhenNull() {
	// TODO Auto-generated method stub
	return null;
}

}
