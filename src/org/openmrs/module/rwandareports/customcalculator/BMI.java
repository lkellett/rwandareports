package org.openmrs.module.rwandareports.customcalculator;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.AllObservationValuesResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.ObservationResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientAttributeResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class BMI implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		PatientAttributeResult res = new PatientAttributeResult(null, null);
		
		
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
					if(wt.getValue().size() > 0)
					{
						weight = wt.getValue().get(wt.getValue().size()-1).getValueNumeric();
					}
				}
			}
			
			if(result.getName().equals("RecentHeight"))
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
			
			res.setValue(Double.toString(bmi));	
		}
		
		return res;
	}
}
