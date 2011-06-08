package org.openmrs.module.rwandareports.customcalculators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.ObservationResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientAttributeResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class DPA implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		PatientAttributeResult dpa = new PatientAttributeResult(null, null);
		
		StringBuffer dpaOutput = new StringBuffer();
		dpaOutput.append(" ");
		
		
		for(PatientDataResult result: results)
		{
			if(result.getName().equals("dpa"))
			{
				ObservationResult dpaResult = (ObservationResult)result;
				
				if(dpaResult.getValue() != null && dpaResult.getValue().trim().length() > 0)
				{
					dpaOutput.append("R: ");
					dpaOutput.append(dpaResult.getValue());
					dpaOutput.append(" ");
				}
			}
			
			if(result.getName().equals("ddr"))
			{
				ObservationResult ddrResult = (ObservationResult)result;
				
				if(ddrResult.getValue() != null && ddrResult.getValue().trim().length() > 0)
				{
					SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
					Calendar ddrDate = Calendar.getInstance();
						
					try {
						ddrDate.setTime(sdf.parse(ddrResult.getValue().trim()));
						ddrDate.add(Calendar.DAY_OF_YEAR, 280);
						
						dpaOutput.append("C: ");
						dpaOutput.append(sdf.format(ddrDate.getTime()));
						dpaOutput.append(" ");
					}catch (ParseException e) {
						log.debug("Unable to parse DDR date", e);
					}
				}
			}
		}
		
		dpa.setValue(dpaOutput.toString().trim());
		return dpa;		
	}
}
