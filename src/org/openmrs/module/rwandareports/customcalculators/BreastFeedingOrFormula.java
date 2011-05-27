package org.openmrs.module.rwandareports.customcalculators;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.DateResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientAttributeResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class BreastFeedingOrFormula implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results) {
		
		PatientAttributeResult bOrF = new PatientAttributeResult(null, null);
		
		for(PatientDataResult result: results)
		{
			if(result.getName().equals("decisionDate"))
			{
				DateResult decisionDate = (DateResult)result;
				
				Calendar todaysDate = Calendar.getInstance();
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
		}
		
		return bOrF;
	}
}
