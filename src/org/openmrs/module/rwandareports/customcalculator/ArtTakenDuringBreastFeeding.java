package org.openmrs.module.rwandareports.customcalculator;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.DrugOrder;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.AllDrugOrdersResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.DateResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientAttributeResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientPropertyResult;

public class ArtTakenDuringBreastFeeding implements CustomCalculation{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		PatientPropertyResult birthDate = null;
		DateResult dateWeaned = null;
		AllDrugOrdersResult allDrugs = null;
		
		PatientAttributeResult art = new PatientAttributeResult(null, null);
		
		for(PatientDataResult result: results)
		{
			if(result.getName().equals("ArtDrugs"))
			{
				List<PatientDataResult> value = (List<PatientDataResult>)result.getValue();
				if(value != null && value.size() > 0)
				{
					allDrugs = (AllDrugOrdersResult)value.get(0);
				}
			}
			if(result.getName().equals("birthdate"))
			{
				birthDate = (PatientPropertyResult)result;
			}
			if(result.getName().equals("dateWeaned"))
			{
				dateWeaned = (DateResult)result;
			}
		}
		
		//first we filter to remove art that wasn't active during the current pmtct program
		if(allDrugs != null && allDrugs.getValue() != null && allDrugs.getValue().size() > 0 && birthDate != null && birthDate.getValue() != null)
		{
			StringBuilder drugs = new StringBuilder();
			for(DrugOrder drO: allDrugs.getValue())
			{
				if(drO.isCurrent((Date)birthDate.getValue()))
				{
					if(dateWeaned != null && dateWeaned.getValue() != null)
					{
						if(drO.isCurrent(dateWeaned.getValue()))
						{
							drugs.append(" ");
							try{
								drugs.append(drO.getDrug().getName());
							}
							catch(Exception e)
							{
								log.info("Unable to retrieve drug name", e);
							}
						}
					}
					else
					{
						drugs.append(" ");
						try{
							drugs.append(drO.getDrug().getName());
						}
						catch(Exception e)
						{
							log.info("Unable to retrieve drug name", e);
						}
						
					}
				}
			}
			
			art.setValue(drugs.toString().trim());
		}
		
		return art;
	}
}
