package org.openmrs.module.rwandareports.customevaluator;

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
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.StringResult;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;


public class HIVResearchExtractionExclusions implements CustomEvaluator {
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	public PatientDataResult evaluate(Patient patient, String name, EvaluationContext context) {
		
		StringResult result = new StringResult(null, context);
		result.setName(name);
		result.setDefinition(name);
		
		Program pmtct = gp.getProgram(GlobalPropertiesManagement.PMTCT);
		
		Program adultHiv = gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		Program pediHiv = gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		ProgramWorkflow treatmentStatusAdult = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		ProgramWorkflow treatmentStatusPedi = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		ProgramWorkflowState onArt = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		ProgramWorkflowState onArtPedi = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		ProgramWorkflowState following = gp.getProgramWorkflowState(GlobalPropertiesManagement.FOLLOWING_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		ProgramWorkflowState followingPedi = gp.getProgramWorkflowState(GlobalPropertiesManagement.FOLLOWING_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		PatientProgram pmtctP = null;
		PatientProgram adultHivP = null;
		PatientProgram pediHivP = null;
		
		List<PatientProgram> programs = Context.getProgramWorkflowService().getPatientPrograms(patient,
		    null, null, null, null, null, false);
		
		for(PatientProgram pp: programs)
		{
			if(!pp.isVoided())
			{
				if(pp.getProgram().getProgramId().equals(pmtct.getProgramId()))
				{
					if(pmtctP == null)
					{
						pmtctP = pp;
					}
					else if(pp.getDateEnrolled() != null)
					{
						if(pmtctP.getDateEnrolled() == null || pp.getDateEnrolled().after(pmtctP.getDateEnrolled()));
						{
							pmtctP = pp;
						}
					}
				}
				
				if(pp.getProgram().getProgramId().equals(adultHiv.getProgramId()))
				{
					if(adultHivP == null)
					{
						adultHivP = pp;
					}
					else if(pp.getDateEnrolled() != null)
					{
						if(adultHivP.getDateEnrolled() == null || pp.getDateEnrolled().after(adultHivP.getDateEnrolled()));
						{
							adultHivP = pp;
						}
					}
				}
				
				if(pp.getProgram().getProgramId().equals(pediHiv.getProgramId()))
				{
					if(pediHivP == null)
					{
						pediHivP = pp;
					}
					else if(pp.getDateEnrolled() != null)
					{
						if(pediHivP.getDateEnrolled() == null || pp.getDateEnrolled().after(pediHivP.getDateEnrolled()));
						{
							pediHivP = pp;
						}
					}
				}
			}
		}
		
		if(pmtctP != null)
		{
			if(pmtctP.getActive())
			{
				if(adultHivP == null && pediHivP == null)
				{
					result.setValue("Exclude");
				}
				else if(adultHivP != null)
				{
					PatientState state = adultHivP.getCurrentState(treatmentStatusAdult);
					if(state != null && state.getState() != null)
					{
						
						if(state.getState().getId().equals(onArt.getId()))
						{
							if(state.getStartDate() == null || pmtctP.getDateEnrolled() == null)
							{
								result.setValue("Check chart");
							}
							else if(!state.getStartDate().before(pmtctP.getDateEnrolled()))
							{
								result.setValue("Exclude");
							}
						}
					}
				}
				else if(pediHivP != null)
				{
					PatientState state = pediHivP.getCurrentState(treatmentStatusPedi);
					if(state != null && state.getState() != null)
					{
						if(state.getState().getId().equals(onArtPedi.getId()) && !state.getStartDate().before(pmtctP.getDateEnrolled()))
						{
							result.setValue("Exclude");
						}
					}
				}
			}
			else
			{
				if(adultHivP != null)
				{	
					if(adultHivP.getActive())
					{
						PatientState state = adultHivP.getCurrentState(treatmentStatusAdult);
						
						if(state != null && state.getState() != null)
						{
							if(state.getState().getId().equals(following.getId()))
							{
								result.setValue("Exclude");
							}
						}
					}
					else if(adultHivP.getDateCompleted().after(pmtctP.getDateCompleted()))
					{
						List<PatientState> states = adultHivP.statesInWorkflow(treatmentStatusAdult, false);
						
						if(states.size() > 0 && states.get(states.size()-1).getState().getId().equals(following.getId()))
						{
							result.setValue("Exclude");
						}
						else if(states.size() > 1)
						{
							if(!states.get(states.size()-1).getState().getId().equals(following.getId()) && !states.get(states.size()-1).getState().getId().equals(onArt.getId()))
							{
								if(states.get(states.size()-2).getState().getId().equals(following.getId()))
								{
									result.setValue("Exclude");
								}
							}
						}
					}
				}
				else if(pediHivP != null)
				{	
					if(pediHivP.getActive())
					{
						PatientState state = pediHivP.getCurrentState(treatmentStatusPedi);
						
						if(state.getState().getId().equals(followingPedi.getId()))
						{
							result.setValue("Exclude");
						}
					}
					else if(pediHivP.getDateCompleted().after(pmtctP.getDateCompleted()))
					{
						List<PatientState> states = pediHivP.statesInWorkflow(treatmentStatusPedi, false);
						
						if(states.get(states.size()-1).getState().getId().equals(followingPedi.getId()))
						{
							result.setValue("Exclude");
						}
						else if(states.size() > 1)
						{
							if(!states.get(states.size()-1).getState().getId().equals(followingPedi.getId()) && !states.get(states.size()-1).getState().getId().equals(onArtPedi.getId()))
							{
								if(states.get(states.size()-2).getState().getId().equals(followingPedi.getId()))
								{
									result.setValue("Exclude");
								}
							}
						}
					}
				}
				
			}
		}
		
		return result;
	}
	
}
