package org.openmrs.module.rwandareports.customcalculator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.NumberResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.ObservationResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.StringResult;

public class DaysLate implements CustomCalculation {
	
protected Log log = LogFactory.getLog(DaysLate.class);
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		NumberResult nr=new NumberResult(results.get(0).getPatientData(), context);
		StringResult sr =  new StringResult(results.get(0).getPatientData(), context);
		for(PatientDataResult result: results)
		{
			if(result.getName().equals("nextVisit"))
			{
				ObservationResult nextVisit = (ObservationResult)result;
				
				if(nextVisit.getDateOfObservation() != null)
				{
	
					long diff=0;
					DateFormat dateformat = new SimpleDateFormat("yyy/MM/dd");
					
					//try to get the return visit date
					Date returnVisitDate = null;
					try {
						returnVisitDate = dateformat.parse(nextVisit.getValue());
					} catch (ParseException e) {
						log.error("Could not parse return visit date", e);
						//just tell the user that the difference between dates is not available
						sr.setValue("Not available");
						return sr;
					}
					Calendar nextVisitDate = Calendar.getInstance();
					nextVisitDate.setTime(returnVisitDate);
					
					Date endDate=(Date)context.getParameterValue("endDate");
					Calendar today = Calendar.getInstance();
					today.setTime(endDate);
			
					//difference in days between the endDate and the return visit date
					diff= today.getTimeInMillis()-nextVisitDate.getTimeInMillis();
					long d = diff / (24 * 60 * 60 * 1000);
					nr.setValue(d);
					return nr;
				}
			}
		}
		
		sr.setValue("Not available");
		return sr;
	}
}
