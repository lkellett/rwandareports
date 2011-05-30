package org.openmrs.module.rwandareports.customcalculators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.ObservationResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientAttributeResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class GestationalAge implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		PatientAttributeResult gestAge = new PatientAttributeResult(null, null);
		
		Date date = (Date)context.getParameterValue("date");
		
		for(PatientDataResult result: results)
		{
			if(result.getName().equals("ddr"))
			{
				ObservationResult ddr = (ObservationResult)result;
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
				
				if(ddr.getValue() != null && ddr.getValue().trim().length() > 0)
				{
					Calendar ddrDate = Calendar.getInstance();
					
					try {
						ddrDate.setTime(sdf.parse(ddr.getValue().trim()));
						Calendar todaysDate = Calendar.getInstance();
						todaysDate.setTime(date);
						
						long weeks = todaysDate.getTime().getTime() - ddrDate.getTime().getTime();
						
						weeks = weeks / (24 * 60 * 60 * 1000 * 7);
						
						gestAge.setValue(String.valueOf(weeks));
					} catch (ParseException e) {
						log.debug("Unable to parse DDR date", e);
					}
				}
			}
		}
		
		return gestAge;
	}
}
