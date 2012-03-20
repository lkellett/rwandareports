package org.openmrs.module.rwandareports.customcalculator;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.DateResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.ObservationResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class NextCD4 implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		DateResult desDate = new DateResult(null, null);
		
		for(PatientDataResult result: results)
		{
			if(result.getName().equals("CD4Test"))
			{
				ObservationResult cd4Test = (ObservationResult)result;
				
				if(cd4Test.getDateOfObservation() != null)
				{
					Calendar decisionDate = Calendar.getInstance();
					decisionDate.setTime(cd4Test.getDateOfObservation());
					
					decisionDate.add(Calendar.MONTH, 6);
					
					desDate.setValue(decisionDate.getTime());
					
					desDate.setFormat("dd-MMM-yyyy");
				}
			}
		}
		
		return desDate;
	}
}
