package org.openmrs.module.rwandareports.dataset.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.reporting.indicator.service.IndicatorService;
import org.openmrs.module.rwandareports.dataset.EncounterIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.indicator.EncounterIndicator;

@Handler(supports={EncounterIndicatorDataSetDefinition.class})
public class EncounterIndicatorDataSetEvaluator implements DataSetEvaluator {
	
	
	public EncounterIndicatorDataSetEvaluator() { }
	
	/**
	 * @throws EvaluationException 
	 * @see DataSetEvaluator#evaluate(DataSetDefinition, EvaluationContext)
	 * @should evaluate a EncounterIndicatorDataSetDefinition
	 */
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		
		context = ObjectUtil.nvl(context, new EvaluationContext());
		if (context == null) {
			context = new EvaluationContext();
		}
		
		EncounterIndicatorDataSetDefinition edsd = (EncounterIndicatorDataSetDefinition)dataSetDefinition;
		
		SimpleDataSet ret = new SimpleDataSet(dataSetDefinition, context);
	
		for(EncounterIndicator ei: edsd.getColumns())
		{
			IndicatorResult result = Context.getService(IndicatorService.class).evaluate(ei, context);
			DataSetRow row = new DataSetRow();
			row.addColumnValue(new DataSetColumn(ei.getName(), ei.getName(), Number.class), result.getValue());
			ret.addRow(row);
		}
		
		return ret;
	}
}
