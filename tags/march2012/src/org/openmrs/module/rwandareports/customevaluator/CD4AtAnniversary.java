package org.openmrs.module.rwandareports.customevaluator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Person;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomEvaluator;
import org.openmrs.module.rowperpatientreports.patientdata.result.DateResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.StringResult;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;

public class CD4AtAnniversary implements CustomEvaluator {
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	private int anniversary;
	
	public PatientDataResult evaluate(Patient patient, String name, EvaluationContext context) {
		
		Program pmtct = gp.getProgram(GlobalPropertiesManagement.PMTCT);
		
		Program adultHiv = gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		Program pediHiv = gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		ProgramWorkflow treatmentStatusAdult = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW,
		    GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		ProgramWorkflow treatmentStatusPedi = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW,
		    GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		ProgramWorkflow treatmentStatusPmtct = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW,
		    GlobalPropertiesManagement.PMTCT);
		
		ProgramWorkflowState onArt = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		ProgramWorkflowState onArtPedi = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		ProgramWorkflowState onArtPmtct = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE, GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PMTCT);
	
		
		List<Program> hivPrograms = new ArrayList<Program>();
		hivPrograms.add(pmtct);
		hivPrograms.add(adultHiv);
		hivPrograms.add(pediHiv);
		
		StringResult result = new StringResult(null, context);
		result.setName(name);
		result.setDefinition(name);
		
		ARTStartDate startDate = new ARTStartDate();
		
		DateResult artStart = startDate.calculateArtStart(patient, name, context);
		
		if (artStart.getValue() != null) {
			Calendar start = Calendar.getInstance();
			start.setTime(artStart.getValue());
			start.add(Calendar.YEAR, anniversary);
			start.add(Calendar.MONTH, -3);
			
			Calendar end = Calendar.getInstance();
			end.setTime(artStart.getValue());
			end.add(Calendar.YEAR, anniversary);
			end.add(Calendar.MONTH, 3);
			
			Calendar anniv = Calendar.getInstance();
			anniv.setTime(artStart.getValue());
			anniv.add(Calendar.YEAR, anniversary);
			Date annivDate = anniv.getTime();
			
			if (annivDate.before(new Date())) {
				
				List<Person> patientList = new ArrayList<Person>();
				patientList.add(patient);
				
				List<Concept> cd4Concept = new ArrayList<Concept>();
				cd4Concept.add(gp.getConcept(GlobalPropertiesManagement.CD4_TEST));
				
				List<Obs> cd4 = Context.getObsService().getObservations(patientList, null, cd4Concept, null, null, null,
				    null, null, null, start.getTime(), end.getTime(), false);
				
				if (cd4 != null && cd4.size() > 0) {
					if (cd4.size() > 1) {
						Obs annivObs = null;
						
						for (Obs cd4Ob : cd4) {
							if (annivObs == null) {
								annivObs = cd4Ob;
							} else {
								long differenceCurrent;
								long differenceNew;
								if (annivDate.after(cd4Ob.getObsDatetime())) {
									differenceNew = annivDate.getTime() - cd4Ob.getObsDatetime().getTime();
								} else {
									differenceNew = cd4Ob.getObsDatetime().getTime() - annivDate.getTime();
								}
								
								if (annivDate.after(annivObs.getObsDatetime())) {
									differenceCurrent = annivDate.getTime() - annivObs.getObsDatetime().getTime();
								} else {
									differenceCurrent = annivObs.getObsDatetime().getTime() - annivDate.getTime();
								}
								
								if (differenceNew < differenceCurrent) {
									annivObs = cd4Ob;
								}
							}
						}
						
						result.setValue(annivObs.getValueAsString(Context.getLocale()));
						
					} else {
						result.setValue(cd4.get(0).getValueAsString(Context.getLocale()));
					}
				} else {
					List<PatientProgram> patientProgram = Context.getProgramWorkflowService().getPatientPrograms(patient,
					    null, null, annivDate, null, null, false);
					
					PatientProgram closest = null;
					for (PatientProgram pp : patientProgram) {
						if (pp.getActive(annivDate) && hivPrograms.contains(pp.getProgram()) && closest == null) {
							closest = pp;
						} else if (pp.getActive(annivDate) && closest != null && pp.getProgram().equals(adultHiv)) {
							closest = pp;
						} else if (closest == null && hivPrograms.contains(pp.getProgram())) {
							closest = pp;
						} else if (closest != null && hivPrograms.contains(pp.getProgram())
						        && pp.getDateEnrolled().after(closest.getDateEnrolled())) {
							closest = pp;
						}
						
						if (closest != null) {
							PatientState state = closest.getCurrentState(treatmentStatusAdult);
							if (state != null && !state.getState().equals(onArt)) {
								result.setValue(state.getState().getConcept().getName().toString());
							} else {
								state = closest.getCurrentState(treatmentStatusPedi);
								if (state != null && !state.getState().equals(onArtPedi)) {
									result.setValue(state.getState().getConcept().getName().toString());
								} else {
									state = closest.getCurrentState(treatmentStatusPmtct);
									if (state != null && !state.getState().equals(onArtPedi)) {
										result.setValue(state.getState().getConcept().getName().toString());
									}
								}
							}
						}
					}
				}
			}
			else
			{
				result.setValue("Hasn't reached anniversary yet");
			}
		}
		
		return result;
	}
	
	public int getAnniversary() {
		return anniversary;
	}
	
	public void setAnniversary(int anniversary) {
		this.anniversary = anniversary;
	}
	
}
