package org.openmrs.module.rwandareports.customcalculators;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.DateResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.ObservationResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientAttributeResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class BreastFeedingOrFormula implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {

		PatientAttributeResult bOrF = new PatientAttributeResult(null, null);
		
		Date date = (Date)context.getParameterValue("date");
		
		for(PatientDataResult result: results)
		{
			if(result.getName().equals("decisionDate"))
			{
				DateResult decisionDate = (DateResult)result;
				
				Calendar todaysDate = Calendar.getInstance();
				todaysDate.setTime(date);
				if(decisionDate.getValue() != null)
				{
					Calendar decDate = Calendar.getInstance();
					decDate.setTime(decisionDate.getValue());
					
					if(todaysDate.after(decDate))
					{
						bOrF.setValue("B");
					}
					else
					{
						bOrF.setValue("F");
					}
				}
			}
			
			if(result.getName().equals("CD4Test"))
			{
				ObservationResult cd4 = (ObservationResult)result;
				
				if(cd4.getValue() != null)
				{
					int cd4Val = Integer.parseInt(cd4.getValue());
					if(cd4Val < 350)
					{
						bOrF.setValue(null);
						return bOrF;
					}
				}
			}
		}
		
		return bOrF;
	}
}