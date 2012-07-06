package org.openmrs.module.rwandareports.definition;

import org.openmrs.module.rowperpatientreports.patientdata.definition.BasePatientData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;

public class FirstDrugRegimenCycle extends BasePatientData implements RowPerPatientData {

	private String regimen = null;
	
    public String getRegimen() {
    	return regimen;
    }
	
    public void setRegimen(String regimen) {
    	this.regimen = regimen;
    }
}