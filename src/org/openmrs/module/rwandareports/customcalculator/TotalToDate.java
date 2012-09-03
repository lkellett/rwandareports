package org.openmrs.module.rwandareports.customcalculator;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.AllObservationValuesResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.StringResult;

public class TotalToDate implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		StringResult totalResult = new StringResult(null, null);
		
		Double total = 0.0;
		DecimalFormat f = new DecimalFormat("0.#");
		
		for(PatientDataResult result: results)
		{
			AllObservationValuesResult resultValues = (AllObservationValuesResult)result;
			if(resultValues != null)
			{
				List<Obs> values = resultValues.getValue();
				
				for(Obs o: values)
				{
					if(o.getValueNumeric() != null)
					{
						total = total + o.getValueNumeric();
					}
				}
			}
		}
		
		if(total > 0)
		{
			totalResult.setValue(f.format(total));
		}
		
		return totalResult;
	}
}
