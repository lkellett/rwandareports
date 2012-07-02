package org.openmrs.module.rwandareports.definition;

import org.openmrs.module.rowperpatientreports.patientdata.definition.BasePatientData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;

public class FirstDrugRegimenCycle extends BasePatientData implements RowPerPatientData {

	private Integer regimen = null;
	
    public Integer getRegimen() {
    	return regimen;
    }
	
    public void setRegimen(Integer regimen) {
    	this.regimen = regimen;
    }
}