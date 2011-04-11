package org.openmrs.module.rwandareports.customcalculators;

import java.util.List;

import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.AgeResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.AllObservationValuesResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientAttributeResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class PMTCTInfantDBSDue implements CustomCalculation{

	public PatientDataResult calculateResult(List<PatientDataResult> results) {
		
		AllObservationValuesResult dbsResults = null;
		AgeResult ageInMonths = null;
		
		for(PatientDataResult result: results)
		{
			if(result.getName().equals("DBSObservations"))
			{
				dbsResults = (AllObservationValuesResult)result;
			}
			
			if(result.getName().equals("Age in months"))
			{
				ageInMonths = (AgeResult)result;
			}
		}
		
		PatientAttributeResult result = new PatientAttributeResult(null, null);
		
		if(ageInMonths.getValue() < 0)
		{
			result.setValue("Age undefined");
		}
		else if(dbsResults.getValue() == null)
		{
			result.setValue("Yes");
		}
		else if(ageInMonths.getValue() > 4 && dbsResults.getValue().size() < 1)
		{
			result.setValue("Yes");
		}
		else if(ageInMonths.getValue() > 5 && dbsResults.getValue().size() < 2)
		{
			result.setValue("Yes");
		}
		else if(ageInMonths.getValue() > 10 && dbsResults.getValue().size() < 3)
		{
			result.setValue("Yes");
		}
		else if(ageInMonths.getValue() > 18 && dbsResults.getValue().size() < 4)
		{
			result.setValue("Yes");
		}
		else
		{
			result.setValue("No");
		}
		
		return result;
	}
}
