package org.openmrs.module.rwandareports.customcalculators;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.DrugOrder;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.AllDrugOrdersResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.DateResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class DecisionDate implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		DateResult desDate = new DateResult(null, null);
		
		for(PatientDataResult result: results)
		{
			if(result.getName().equals("Regimen"))
			{
				AllDrugOrdersResult arvStart = (AllDrugOrdersResult)result;
				
				Date startDate = null;
				if(arvStart.getValue() != null)
				{
					for(DrugOrder o: arvStart.getValue())
					{
						if(startDate == null || startDate.after(o.getStartDate()))
						{
							startDate = o.getStartDate();
						}
					}
				}
				
				if(startDate != null)
				{
					Calendar decisionDate = Calendar.getInstance();
					decisionDate.setTime(startDate);
					
					decisionDate.add(Calendar.WEEK_OF_YEAR, 12);
					
					desDate.setValue(decisionDate.getTime());
					
					desDate.setFormat("dd-MMM-yyyy");
				}
			}
		}
		
		return desDate;
	}
}
