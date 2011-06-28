package org.openmrs.module.rwandareports.customcalculators;

import java.math.BigDecimal;
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

public class TBAlerts implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		PatientAttributeResult alert = new PatientAttributeResult(null, null);
		
		StringBuffer alerts = new StringBuffer();
		
		double height = 0;
		double weight = 0;
		
		for(PatientDataResult result: results)
		{
			if(result.getName().equals("weightObs"))
			{
				AllObservationValuesResult wt = (AllObservationValuesResult)result;
				
				if(wt.getValue() != null)
				{
					int decline = calculateDecline(wt.getValue());
					
					if(decline > 0)
					{
						alerts.append("WT decline(");
						alerts.append(decline);
						alerts.append(").\n");
					}
					
					if(wt.getValue().size() > 0)
					{
						weight = wt.getValue().get(wt.getValue().size()-1).getValueNumeric();
					}
				}
			}
			
			if(result.getName().equals("RecentHeight"))
			{
				ObservationResult heightOb = (ObservationResult)result;
				
				if(heightOb.getValue() == null || heightOb.getValue().trim().length() == 0)
				{
					alerts.append("No height recorded.\n");
				}
				else
				{
					height = Double.parseDouble(heightOb.getValue());
				}
			}
		}
		
		if(height > 0 && weight > 0)
		{
			double bmi = weight/(height/100*height/100);
			int decimalPlace = 1;
			BigDecimal bd = new BigDecimal( Double.toString(bmi) );
			bd = bd.setScale( decimalPlace, BigDecimal.ROUND_HALF_UP );
			
			if(bmi < 16)
			{
				alerts.append("Very low BMI (" + bd.doubleValue()  + ").\n");
			}
			else if(bmi < 18.5)
			{
				alerts.append("Low BMI (" + bd.doubleValue()  + ").\n");
			}
				
		}
		
		alert.setValue(alerts.toString().trim());
		return alert;
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
