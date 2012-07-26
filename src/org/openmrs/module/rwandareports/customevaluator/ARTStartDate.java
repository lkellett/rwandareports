package org.openmrs.module.rwandareports.customevaluator;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomEvaluator;
import org.openmrs.module.rowperpatientreports.patientdata.result.DateResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;


public class ARTStartDate implements CustomEvaluator {
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	public PatientDataResult evaluate(Patient patient, String name, EvaluationContext context) {
		
		return calculateArtStart(patient, name, context);
		
	}
	
	public DateResult calculateArtStart(Patient patient, String name, EvaluationContext context)
	{
		DateResult result = new DateResult(null, context);
		result.setFormat("dd-MMM-yyyy");
		result.setName(name);
		result.setDefinition(name);
		
		Program pmtct = gp.getProgram(GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		
		Program adultHiv = gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		Program pediHiv = gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		ProgramWorkflow treatmentStatusAdult = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		ProgramWorkflow treatmentStatusPedi = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		ProgramWorkflow treatmentStatusPmtct = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		
		ProgramWorkflowState onArt = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		ProgramWorkflowState onArtPedi = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		ProgramWorkflowState onArtPmtct = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
	
		List<PatientProgram> programs = Context.getProgramWorkflowService().getPatientPrograms(patient,
		    null, null, null, null, null, false);
		
		
		List<PatientState> states = new ArrayList<PatientState>();
		
		for(PatientProgram pp: programs)
		{
			if(!pp.isVoided())
			{
				if(pp.getProgram().getProgramId().equals(pmtct.getProgramId()))
				{
					List<PatientState> ps = pp.statesInWorkflow(treatmentStatusPmtct, false);
					for(PatientState pps: ps)
					{
						if(pps.getState().getId().equals(onArtPmtct.getId()))
						{
							states.add(pps);
						}
					}
				}
				
				if(pp.getProgram().getProgramId().equals(adultHiv.getProgramId()))
				{
					List<PatientState> ps = pp.statesInWorkflow(treatmentStatusAdult, false);
					for(PatientState pps: ps)
					{
						if(pps.getState().getId().equals(onArt.getId()))
						{
							states.add(pps);
						}
					}
				}
				
				if(pp.getProgram().getProgramId().equals(pediHiv.getProgramId()))
				{
					List<PatientState> ps = pp.statesInWorkflow(treatmentStatusPedi, false);
					for(PatientState pps: ps)
					{
						if(pps.getState().getId().equals(onArtPedi.getId()))
						{
							states.add(pps);
						}
					}
				}
			}
		}
		
		PatientState earliest = null;
		PatientState latest = null;
		
		for(PatientState ps: states)
		{
			if(ps.getStartDate() != null && (earliest == null || ps.getStartDate().before(earliest.getStartDate())))
			{
				earliest = ps;
			}
			if(ps.getStartDate() != null && (latest == null || ps.getStartDate().before(latest.getStartDate())))
			{
				latest = ps;
			}
		}
		
		if(earliest != null && !earliest.getPatientProgram().getProgram().getProgramId().equals(pmtct.getProgramId()))
		{
			result.setValue(earliest.getStartDate());
		}
		else if(latest != null && !latest.getPatientProgram().getProgram().getProgramId().equals(pmtct.getProgramId()))
		{
			result.setValue(latest.getStartDate());
		}
		
		return result;
	}
	
}
