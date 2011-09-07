package org.openmrs.module.rwandareports.customcalculators;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.AllObservationValuesResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.ObservationResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientAttributeResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class Alerts implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		PatientAttributeResult alert = new PatientAttributeResult(null, null);
		
		StringBuffer alerts = new StringBuffer();
		
		for(PatientDataResult result: results)
		{
			
			if(result.getName().equals("CD4Test"))
			{
				ObservationResult cd4 = (ObservationResult)result;
				
				if(cd4.getValue() != null && cd4.getValue().trim().length() > 0)
				{
					int cd4Val = Integer.parseInt(cd4.getValue());
					if(cd4Val < 350)
					{
						if(alerts.length() > 0)
						{
							alerts.append(", ");
						}
						alerts.append("Low CD4");
					}
				}
				
				if(cd4.getDateOfObservation() != null)
				{
					Date dateCd4 = cd4.getDateOfObservation();
					Date date = new Date();
					
					int diff = calculateMonthsDifference(date, dateCd4);
					
					if(diff > 7)
					{
						if(alerts.length() > 0)
						{
							alerts.append(", ");
						}
						alerts.append("Late CD4");
					}
				}
			}
			
			if(result.getName().equals("gestationalAge"))
			{
				if(result.getValue() != null)
				{
					int gestationalAge = Integer.parseInt(result.getValue().toString());
					
					if(gestationalAge > 37 && gestationalAge < 41)
					{
						if(alerts.length() > 0)
						{
							alerts.append(", ");
						}
						alerts.append("Due to deliver");
					}
					if(gestationalAge > 40)
					{
						if(alerts.length() > 0)
						{
							alerts.append(", ");
						}
						alerts.append("Post term");
					}	
				}
			}
			
			if(result.getName().equals("weightObs"))
			{
				AllObservationValuesResult wt = (AllObservationValuesResult)result;
				
				if(wt.getValue() != null)
				{
					int decline = calculateDecline(wt.getValue());
					
					if(decline == 0 )
					{
						alerts.append("No weight gain\n");
					}
					else if(decline > 0){
						alerts.append("Weight decline (");
						alerts.append(decline);
						alerts.append(" kg)\n");
					}
				}
				
				if(wt.getValue() == null || wt.getValue().size() == 0)
				{
					alerts.append("No weight recorded.\n");
				}
			}
			
			if(result.getName().equals("IO") && result.getValue() != null)
			{
				alerts.append("IO reported last visit: " + result.getValue() + "\n");
			}
			
			if(result.getName().equals("SideEffects") && result.getValue() != null)
			{
				alerts.append("Side effects reported last visit: " + result.getValue() + "\n");
			}
		}
		
		alert.setValue(alerts.toString());
		return alert;
	}
	
	private int calculateMonthsDifference(Date observation, Date startingDate)
	{
		int diff = 0;
	
		Calendar obsDate = Calendar.getInstance();	
		obsDate.setTime(observation);
	
		Calendar startDate = Calendar.getInstance();
		startDate.setTime(startingDate);
	
		//find out if there is any difference in years first
		diff = obsDate.get(Calendar.YEAR) - startDate.get(Calendar.YEAR);
		diff = diff * 12;
	
		int monthDiff = obsDate.get(Calendar.MONTH) - startDate.get(Calendar.MONTH);
		diff = diff + monthDiff;
	
		return diff;
	}
	
	private int calculateDecline(List<Obs> obs)
	{
		Obs lastOb = null;
		Obs nextToLastOb = null;
		
		if(obs.size() > 0)
		{
			lastOb = obs.get(obs.size() - 1);
		}
		
		if(obs.size() > 1)
		{
			nextToLastOb = obs.get(obs.size() - 2);
		}
		
		if(lastOb != null && nextToLastOb != null)
		{
			Double firstVal = lastOb.getValueNumeric();
			Double nextToLastVal = nextToLastOb.getValueNumeric();
			
			if(firstVal != null && nextToLastVal != null)
			{
				double decline = nextToLastVal - firstVal;
			
				return (int)decline;
			}
		}
		
		return -1;
	}
}
