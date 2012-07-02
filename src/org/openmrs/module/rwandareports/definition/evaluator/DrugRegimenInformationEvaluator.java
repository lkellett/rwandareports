package org.openmrs.module.rwandareports.definition.evaluator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.orderextension.DrugRegimen;
import org.openmrs.module.orderextension.ExtendedDrugOrder;
import org.openmrs.module.orderextension.api.OrderExtensionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;
import org.openmrs.module.rowperpatientreports.patientdata.evaluator.RowPerPatientDataEvaluator;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.StringResult;
import org.openmrs.module.rwandareports.definition.DrugRegimenInformation;

@Handler(supports={DrugRegimenInformation.class})
public class DrugRegimenInformationEvaluator implements RowPerPatientDataEvaluator{

	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult evaluate(RowPerPatientData patientData, EvaluationContext context) {
	    
		StringResult par = new StringResult(patientData, context);
		DrugRegimenInformation pd = (DrugRegimenInformation)patientData;
	
		Integer regimenId = pd.getRegimen();

		DrugRegimen regimen = null;
		
		if(regimenId != null)
		{
			regimen = Context.getService(OrderExtensionService.class).getDrugRegimen(pd.getRegimen());
		}
		else
		{
			List<ExtendedDrugOrder> drugOrders = Context.getService(OrderExtensionService.class).getExtendedDrugOrders(pd.getPatient(), pd.getIndication(), pd.getAsOfDate(), pd.getUntilDate());
			
			for(ExtendedDrugOrder eo: drugOrders)
			{
				if(eo.getGroup() != null)
				{
					if(eo.getGroup() instanceof DrugRegimen)
					{
						regimen = (DrugRegimen)eo.getGroup();
						break;
					}
				}
			}
		}
		
		if(regimen != null)
		{
			StringBuilder result = new StringBuilder();
			if(regimen.isCyclical())
			{
				Integer maxCycleNum = Context.getService(OrderExtensionService.class).getMaxNumberOfCyclesForRegimen(pd.getPatient(), regimen);
				
				result.append("Cycle #: ");
				result.append(regimen.getCycleNumber());
				result.append(" of ");
				result.append(maxCycleNum.toString()); 
			}
			
			result.append(" Regimen: ");
			result.append(regimen.getName());
			
			if(pd.isShowStartDate())
			{
				Date startDate = regimen.getFirstDrugOrderStartDate();
				SimpleDateFormat format = new SimpleDateFormat(pd.getDateFormat());
				result.append(" Administration start date: ");
				result.append(format.format(startDate));
			}
			
			par.setValue(result.toString());
		}
		return par;
    }
}
 