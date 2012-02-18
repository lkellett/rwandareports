package org.openmrs.module.rwandareports.definition.result;

import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;
import org.openmrs.module.rowperpatientreports.patientdata.result.BasePatientDataResult;


public class CurrentPatientProgramResult extends BasePatientDataResult {
	
	private String value;
	
	public CurrentPatientProgramResult(RowPerPatientData patientData, EvaluationContext ec) {
	    super(patientData, ec);
    }

	public Class<?> getColumnClass() {
		return String.class;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isMultiple() {
		return false;
	}

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
    	this.value = value;
    }
    
    
    public String getValueAsString() {
	    return value;
    }
}
