package org.openmrs.module.rwandareports.definition;

import org.openmrs.Program;
import org.openmrs.module.rowperpatientreports.patientdata.definition.BasePatientData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;



public class CurrentPatientProgram extends BasePatientData implements RowPerPatientData {

	private Program currentPatientProgram;

	
    public CurrentPatientProgram(Program program) {
	    super();
	    setCurrentPatientProgram(program);
    }

	public CurrentPatientProgram() {
	    super();
    }



	/**
     * @return the program 
     */
    public Program getCurrentPatientProgram() {
    	return currentPatientProgram;
    }

	
    /**
     * @param program the program to set
     */
    public void setCurrentPatientProgram(Program program) {
    	this.currentPatientProgram = program;
    	if(program != null)
    	{
    		setName(program.getName());
    	}
    }

	
}
