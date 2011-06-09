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

public class DDR implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		PatientAttributeResult ddr = new PatientAttributeResult(null, null);
		
		StringBuffer ddrOutput = new StringBuffer();
		ddrOutput.append(" ");
		
		boolean needToCal = true;
		
		for(PatientDataResult result: results)
		{
			if(result.getName().equals("ddr"))
			{
				ObservationResult ddrResult = (ObservationResult)result;
				
				if(ddrResult.getValue() != null && ddrResult.getValue().trim().length() > 0)
				{
					//ddrOutput.append("R: ");
					ddrOutput.append(ddrResult.getValue());
					ddrOutput.append(" ");
					needToCal = false;
				}
			}
		}
		
		if(needToCal)
		{
			for(PatientDataResult result: results)
			{
				if(result.getName().equals("dpa"))
				{
					ObservationResult dpaResult = (ObservationResult)result;
					
					if(dpaResult.getValue() != null && dpaResult.getValue().trim().length() > 0)
					{
						SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
						Calendar dpaDate = Calendar.getInstance();
							
						try {
							dpaDate.setTime(sdf.parse(dpaResult.getValue().trim()));
							dpaDate.add(Calendar.DAY_OF_YEAR, -280);
							
							//ddrOutput.append("C: ");
							ddrOutput.append(sdf.format(dpaDate.getTime()));
							ddrOutput.append(" ");
						}catch (ParseException e) {
							log.debug("Unable to parse DPA date", e);
						}
					}
				}
			}
		}
		
		ddr.setValue(ddrOutput.toString().trim());
		return ddr;		
	}
}
