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

public class HIVAdultAlerts implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		PatientAttributeResult alert = new PatientAttributeResult(null, null);
		
		StringBuffer alerts = new StringBuffer();
		
		for(PatientDataResult result: results)
		{
			
			if(result.getName().equals("CD4Test"))
			{
				AllObservationValuesResult cd4 = (AllObservationValuesResult)result;
				
				if(cd4.getValue() != null)
				{
					int decline = calculateDecline(cd4.getValue());
					
					if(decline > 0)
					{
						alerts.append(" CD4 decline(");
						alerts.append(decline);
						alerts.append(") ");
					}
					
					Obs lastCd4 = null;
					
					if(cd4.getValue().size() > 0)
					{
						lastCd4 = cd4.getValue().get(cd4.getValue().size()-1);
					}
					
					if(lastCd4 == null)
					{
						alerts.append(" No CD4 recorded ");
					}
					else
					{
						Date dateCd4 = lastCd4.getObsDatetime();
						Date date = (Date)context.getParameterValue("date");
						
						int diff = calculateMonthsDifference(date, dateCd4);
						
						if(diff > 12)
						{
							alerts.append(" very late CD4 ");
						}
						else if(diff > 6)
						{
							alerts.append(" late CD4");
						}
						
					}
				}	
			}
			
			if(result.getName().equals("weightObs"))
			{
				AllObservationValuesResult wt = (AllObservationValuesResult)result;
				
				if(wt.getValue() != null)
				{
					int decline = calculateDecline(wt.getValue());
					
					if(decline > 0)
					{
						alerts.append(" wt decline(");
						alerts.append(decline);
						alerts.append(") ");
					}
				}
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
			
				if(decline > 0)
				{
					return (int)decline;
				}
			}
		}
		
		return 0;
	}
}
