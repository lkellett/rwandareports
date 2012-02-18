package org.openmrs.module.rwandareports.customcalculator;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.NumberResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.StringResult;

public class DateDiffInDaysSinceLastDiabetesVisit implements CustomCalculation {

	protected Log log = LogFactory.getLog(DateDiffInDaysSinceLastDiabetesVisit.class);
	private List<EncounterType> diabetesEncouters;


	public DateDiffInDaysSinceLastDiabetesVisit(List<EncounterType> diabetesEncouters) {
		this.diabetesEncouters = diabetesEncouters;
	}

	public PatientDataResult calculateResult(List<PatientDataResult> results,EvaluationContext context) {
			
		NumberResult nr=new NumberResult(null, null);
		StringResult sr = new StringResult(null, null);
		
		for (PatientDataResult result : results) {
			if (result.getName().equals("age")) {

				Patient p = result.getPatientData().getPatient();
				List<Encounter> patientEncounters = Context.getEncounterService().getEncounters(p, null, null, null, null, diabetesEncouters, null, false);
				
				if (patientEncounters.size() >=1) {
					Encounter recentEncounter = patientEncounters.get(0);  //the first encounter in the List should be the most recent one.
					long diff=0;

					Date lastVisitDate = recentEncounter.getEncounterDatetime();
					Calendar c1 = Calendar.getInstance();
					c1.setTime(lastVisitDate);
					
					Date endDate=(Date)context.getParameterValue("endDate");
					Calendar c2 = Calendar.getInstance();
					c2.setTime(endDate);
					
					diff= c2.getTimeInMillis()-c1.getTimeInMillis();
					diff = diff / (24 * 60 * 60 * 1000);
					nr.setValue(diff);
					return nr;
				}else{
					sr.setValue("-"); //if the patient has no encounter (We can't calculate the difference)
					return sr;
				}
				

			}
		}

		return sr;
	}
}
