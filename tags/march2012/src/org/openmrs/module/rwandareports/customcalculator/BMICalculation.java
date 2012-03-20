package org.openmrs.module.rwandareports.customcalculator;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.ObservationResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientAttributeResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class BMICalculation implements CustomCalculation{

	private String heightName = "height";
	private String weightName = "weight";
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		PatientAttributeResult res = new PatientAttributeResult(null, null);
		
		double height = 0;
		double weight = 0;
		
		for(PatientDataResult result: results)
		{
			
			if(result.getName().equals(weightName))
			{
				ObservationResult wt = (ObservationResult)result;
				
				if(wt.getValue() != null && wt.getValue().trim().length() != 0)
				{
					weight = Double.parseDouble(wt.getValue());
				}
			}
			
			if(result.getName().equals(heightName))
			{
				ObservationResult heightOb = (ObservationResult)result;
				
				if(heightOb.getValue() != null && heightOb.getValue().trim().length() != 0)
				{
					if(heightOb.getObs().getPerson().getAge(heightOb.getObs().getObsDatetime()) >= 18)
					{
						height = Double.parseDouble(heightOb.getValue());
					}
				}
			}
		}
		
		if(height > 0 && weight > 0)
		{
			double bmi = weight/(height/100*height/100);
			int decimalPlace = 1;
			BigDecimal bd = new BigDecimal( Double.toString(bmi) );
			bd = bd.setScale( decimalPlace, BigDecimal.ROUND_HALF_UP );
			
			res.setValue(bd.toString());	
		}
		
		return res;
	}

	
    public String getHeightName() {
    	return heightName;
    }

	
    public void setHeightName(String heightName) {
    	this.heightName = heightName;
    }

	
    public String getWeightName() {
    	return weightName;
    }

	
    public void setWeightName(String weightName) {
    	this.weightName = weightName;
    }
	
	
}
