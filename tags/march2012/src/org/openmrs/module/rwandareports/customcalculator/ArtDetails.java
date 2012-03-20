package org.openmrs.module.rwandareports.customcalculator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.DrugOrder;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientAttributeResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class ArtDetails implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMMyy");
	
		PatientAttributeResult art = new PatientAttributeResult(null, null);
		
		StringBuilder resultString = new StringBuilder();
		
		Date startDate = null;
		Date changeDate = null;
		List<DrugOrder> changeOrder = null;
		
		for(PatientDataResult result: results)
		{
			if(result.getName().equals("Start ART Regimen"))
			{
				DrugOrder startOrder = (DrugOrder)result.getValue();
				if(startOrder != null)
				{
					startDate = startOrder.getStartDate();
				}
			}
			
			if(result.getName().equals("Regimen"))
			{
				changeOrder = (List<DrugOrder>)result.getValue();
				if(changeOrder != null && changeOrder.get(0) != null)
				{
					changeDate = changeOrder.get(0).getStartDate();
				}
			}
		}
		
		if(changeOrder != null)
		{
			for(DrugOrder order: changeOrder)
			{
				String drugName = order.getDrug().getName();
				
				while(drugName.indexOf("(") > -1)
				{
					int initialBracket = drugName.indexOf("(");
					int finalBracket = drugName.indexOf(")", initialBracket);
					
					if(finalBracket > -1 && finalBracket < drugName.length() - 1)
					{
						drugName = drugName.substring(0, initialBracket) + "+" + drugName.substring(finalBracket + 1);
					}
					else
					{
						drugName = drugName.substring(0, initialBracket);
					}
				}
				
				resultString.append(drugName);
				resultString.append(" (");
				resultString.append(order.getDose());
				resultString.append(" ");
				String units = order.getUnits();
				if(units != null)
				{
					units = units.replace("tab(s)", "Co");
					resultString.append(units);
				}
				
				resultString.append(",");
				String freq = order.getFrequency();
				if(freq != null)
				{
					freq = freq.replace("day x 7 days/week", "j");
					resultString.append(freq);
				}
				
				resultString.append(")");
				resultString.append("\n");
			}
			
			if(changeDate != null && startDate != null && !startDate.equals(changeDate))
			{
				resultString.append(sdf.format(startDate));
				resultString.append("\n");
			}
			
			if(changeDate != null)
			{
				resultString.append(sdf.format(changeDate));
			}
		}
		
		
		art.setValue(resultString.substring(0));
		
		return art;
	}
}
