package org.openmrs.module.rwandareports.customcalculator;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.ObservationResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientAttributeResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;

public class DiabetesAlerts implements CustomCalculation {
	protected Log log = LogFactory.getLog(this.getClass());

	public PatientDataResult calculateResult(List<PatientDataResult> results,
			EvaluationContext context) {

		PatientAttributeResult alert = new PatientAttributeResult(null, null);

		StringBuffer alerts = new StringBuffer();
		
		Date endDate=(Date)context.getParameterValue("endDate");
		Calendar c1 = Calendar.getInstance();
		c1.setTime(endDate);
		c1.add(Calendar.MONTH, -6);

		for (PatientDataResult result : results) {
			
			if (result.getName().equals("RecentHbA1c")) {
				ObservationResult HbA1c = (ObservationResult)result;
				if(HbA1c.getValue() == null || c1.getTime().compareTo(HbA1c.getDateOfObservation()) == 1)
				{
					alerts.append("no HbA1c in last 6 months\n");
				}
			}
			if (result.getName().equals("RecentCreatinine")) {
				ObservationResult creatinine = (ObservationResult)result;
				if(creatinine.getValue() == null || c1.getTime().compareTo(creatinine.getDateOfObservation()) == 1)
				{
					alerts.append("no Creatinine in last 6 months\n");
				}
			}
			if (result.getName().equals("RecentSBP")) {
				ObservationResult sbp = (ObservationResult) result;
				if (sbp.getValue() != null) {
					try {
						double value = Double.parseDouble(sbp.getValue());
						if (value > 160.0) {
							alerts.append("high BP\n");
						}
					} catch (Exception e) {
					  log.error("Could not parse obs value to double "+e);
					}
					

				}
				//just put this in here to make sure it is executed only once
				if (patientHasDiabetesDDBForm(result) == false) {
					alerts.append("no DDB\n");
				}
			}				
		}
		
		alert.setValue(alerts.toString());
		return alert;
	}
	
	private boolean patientHasDiabetesDDBForm(PatientDataResult result){
		try {
			int formId = Integer.parseInt(Context.getAdministrationService().getGlobalProperty(GlobalPropertiesManagement.DIABETES_DONNEES_DE_BASE));
			Patient p = result.getPatientData().getPatient();
			List<Encounter> patientEncounters =Context.getEncounterService().getEncountersByPatient(p);
			
		    for (Encounter encounter : patientEncounters) {
				if (encounter != null && encounter.getForm() !=null) {
					if(encounter.getForm().getFormId() == formId){	
						return true;
					} 				
				}
			}
		} catch (NumberFormatException e) {
			 log.error("Could not parse value of "+ GlobalPropertiesManagement.DIABETES_DONNEES_DE_BASE+ "to integer");
		}
			
		return false;
	}

}
