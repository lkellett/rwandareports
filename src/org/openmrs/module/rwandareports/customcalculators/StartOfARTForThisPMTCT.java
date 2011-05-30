package org.openmrs.module.rwandareports.customcalculators;

import java.util.List;

import org.openmrs.DrugOrder;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculation;
import org.openmrs.module.rowperpatientreports.patientdata.result.AllDrugOrdersResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.DateResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;

public class StartOfARTForThisPMTCT implements CustomCalculation{

	public PatientDataResult calculateResult(List<PatientDataResult> results, EvaluationContext context) {
		
		DateResult pmtctStart = null;
		AllDrugOrdersResult allDrugs = null;
		
		
		
		DateResult startArt = new DateResult(null, null);
		
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
			if(result.getName().equals("motherRegistration"))
			{
				List<PatientDataResult> value = (List<PatientDataResult>)result.getValue();
				if(value != null && value.size() > 0)
				{
					pmtctStart = (DateResult)value.get(0);
				}
			}
		}
		
		//first we filter to remove art that wasn't active during the current pmtct program
		if(allDrugs != null && allDrugs.getValue() != null && allDrugs.getValue().size() > 0 && pmtctStart != null && pmtctStart.getValue() != null)
		{
			DrugOrder drug = null;
			for(DrugOrder drO: allDrugs.getValue())
			{
				if(drO.isCurrent(pmtctStart.getValue()))
				{
					if(drug == null || drO.getStartDate().before(drug.getStartDate()))
					{
						drug = drO;
					}
				}
			}
			
			if(drug != null)
			{
				startArt.setValue(drug.getStartDate());
			}
		}
		
		return startArt;
	}
}
