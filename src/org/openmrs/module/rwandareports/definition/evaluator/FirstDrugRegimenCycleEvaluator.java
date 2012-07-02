package org.openmrs.module.rwandareports.definition.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.orderextension.DrugRegimen;
import org.openmrs.module.orderextension.api.OrderExtensionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;
import org.openmrs.module.rowperpatientreports.patientdata.evaluator.RowPerPatientDataEvaluator;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.StringResult;
import org.openmrs.module.rwandareports.definition.FirstDrugRegimenCycle;

@Handler(supports = { FirstDrugRegimenCycle.class })
public class FirstDrugRegimenCycleEvaluator implements RowPerPatientDataEvaluator {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult evaluate(RowPerPatientData patientData, EvaluationContext context) {
		
		StringResult par = new StringResult(patientData, context);
		FirstDrugRegimenCycle pd = (FirstDrugRegimenCycle) patientData;
		
		if(pd.getRegimen() != null)
		{
			DrugRegimen regimen = Context.getService(OrderExtensionService.class).getDrugRegimen(pd.getRegimen());
			if (regimen.isCyclical() && regimen.getCycleNumber() > 1) {
				par.setValue("true");
			} else {
				par.setValue("false");
			}
		}
		return par;
	}
}
