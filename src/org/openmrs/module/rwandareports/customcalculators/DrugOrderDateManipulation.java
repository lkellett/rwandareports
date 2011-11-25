package org.openmrs.module.rwandareports.customcalculators;

import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.DateResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.DrugOrdersResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class DrugOrderDateManipulation implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public DrugOrderDateManipulation(String name, Integer dateChange, Integer dateUnit, String dateFormat)
	{
		this.name = name;
		this.dateChange = dateChange;
		this.dateUnit = dateUnit;
		this.dateFormat = dateFormat;
	}
	
	private Integer dateChange;
	private Integer dateUnit;
	private String name;
	private String dateFormat;
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		DateResult dateResult = new DateResult(null, null);
		
		for(PatientDataResult result: results)
		{
			if(result.getName().equals(name))
			{
				DrugOrdersResult start = (DrugOrdersResult)result;
				
				if(start != null && start.getValue() != null && start.getValue().getStartDate() != null)
				{
					Calendar adjustedDate = Calendar.getInstance();
					adjustedDate.setTime(start.getValue().getStartDate());
					
					adjustedDate.add(dateUnit, dateChange);
					
					dateResult.setValue(adjustedDate.getTime());
					
					dateResult.setFormat(dateFormat);
				}
			}
		}
		
		return dateResult;
	}
}
