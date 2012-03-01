package org.openmrs.module.rwandareports.customcalculator;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.AllObservationValuesResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.StringResult;
import org.openmrs.module.rwandareports.dataset.comparator.ObsComparatorDesc;

public class DifferenceBetweenLastTwoObs implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		StringResult result = new StringResult(null, null);
		
		if(results.size() > 0)
		{
			AllObservationValuesResult obs = (AllObservationValuesResult)results.get(0);
			List<Obs> allObs = (List<Obs>)obs.getValue();
			
			if(allObs != null)
			{
				Collections.sort(allObs, new ObsComparatorDesc());
			}
			
			if(allObs.size() > 1)
			{
				Obs firstObs = allObs.get(0);
				Obs secondObs = allObs.get(1);
				
				if(firstObs != null && firstObs.getValueNumeric() != null && secondObs != null && secondObs.getValueNumeric() != null)
				{
					Double diff = (firstObs.getValueNumeric() - secondObs.getValueNumeric());
					result.setValue(diff.toString());
				}
			}
		}
		return result;
	}
}
