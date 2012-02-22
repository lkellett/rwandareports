package org.openmrs.module.rwandareports.customcalculator;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.AllObservationValuesResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.DrugOrdersResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.StringResult;

public class DeclineHighestCD4 implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	private int daysBefore = 0;
	
	private String initiationArt;
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		StringResult res = new StringResult(null, context);
		
		Date artInitiation = null;
		for(PatientDataResult result: results)
		{
			
			if(result.getName().equals(initiationArt))
			{
				DrugOrdersResult artDateResult = (DrugOrdersResult)result;
				if(artDateResult.getValue() != null)
				{
					artInitiation = artDateResult.getValue().getStartDate();
				}
			}
		}
		
		if(artInitiation != null)
		{
			Calendar artAdjustment = Calendar.getInstance();
			artAdjustment.setTime(artInitiation);
			artAdjustment.add(Calendar.DAY_OF_YEAR, -daysBefore);
			artInitiation = artAdjustment.getTime();
			
			Obs highest = null;
			Obs mostRecent = null;
			for(PatientDataResult result: results)
			{
				if(result.getName().equals("allCD4Obs"))
				{
					AllObservationValuesResult cd4Values = (AllObservationValuesResult)result;
					
					for(Obs cd4: cd4Values.getValue())
					{
						if(cd4.getObsDatetime().after(artInitiation))
						{
							if((highest == null && cd4.getValueNumeric() != null) || (cd4.getValueNumeric() != null && cd4.getValueNumeric() > highest.getValueNumeric()))
							{
								highest = cd4;
							}
							
							if((mostRecent == null && cd4.getValueNumeric() != null) || (cd4.getValueNumeric() != null && cd4.getObsDatetime().after(highest.getObsDatetime())))
							{
								mostRecent = cd4;
							}
						}
					}
					
					if(mostRecent != null && highest != null)
					{
						double percentDecline = 100 - ((mostRecent.getValueNumeric()/highest.getValueNumeric())*100);
						
						if(percentDecline > 50)
						{
							DecimalFormat twoDigit = new DecimalFormat("#,##0.00");//formats to 2
							
							String resString = twoDigit.format(percentDecline) + "% CD4 decline since highest CD4 since art initiation";
							if(daysBefore > 0)
							{
								resString = resString + " or " + daysBefore + " days before art initiation"; 
							}
							
							res.setValue(resString);
						}
					}
				}
			}
		}
		
		return res;	
	}

	
    public int getDaysBefore() {
    	return daysBefore;
    }

	
    public void setDaysBefore(int daysBefore) {
    	this.daysBefore = daysBefore;
    }

	
    public String getInitiationArt() {
    	return initiationArt;
    }

	
    public void setInitiationArt(String initiationArt) {
    	this.initiationArt = initiationArt;
    }
}
