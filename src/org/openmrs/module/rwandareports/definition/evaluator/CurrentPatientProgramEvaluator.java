package org.openmrs.module.rwandareports.definition.evaluator;

import java.util.List;

import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;
import org.openmrs.module.rowperpatientreports.patientdata.evaluator.RowPerPatientDataEvaluator;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rwandareports.definition.CurrentPatientProgram;
import org.openmrs.module.rwandareports.definition.result.CurrentPatientProgramResult;

@Handler(supports={CurrentPatientProgram.class})
public class CurrentPatientProgramEvaluator implements RowPerPatientDataEvaluator{

	public PatientDataResult evaluate(RowPerPatientData patientData, EvaluationContext context) {
	    
		CurrentPatientProgramResult ppr = new CurrentPatientProgramResult(patientData, context);
		CurrentPatientProgram pp = (CurrentPatientProgram) patientData;
		List<PatientProgram> programs = Context.getProgramWorkflowService().getPatientPrograms(pp.getPatient(), null, null, null, null, null, false);
		//just check if the passed-in program is present in the current patient programs
		Program p= pp.getCurrentPatientProgram();
		for (PatientProgram patientProgram : programs) {
			if(p != null && patientProgram.getProgram().equals(p))
			{		
					ppr.setValue(p.getName());
			}
		}
			
		return ppr;
    }

}
