package org.openmrs.module.rwandareports.dataset.evaluator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.MultiPeriodIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.MultiPeriodIndicatorDataSetDefinition.Iteration;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.rwandareports.dataset.PeriodIndicatorDataSetDefinition;

@Handler(supports={PeriodIndicatorDataSetDefinition.class})
public class PeriodIndicatorDataSetEvaluator implements DataSetEvaluator {
	
	
	public PeriodIndicatorDataSetEvaluator() { }
	
	/**
	 * @throws EvaluationException 
	 * @see DataSetEvaluator#evaluate(DataSetDefinition, EvaluationContext)
	 * @should evaluate a MultiPeriodIndicatorDataSetDefinition
	 */
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		
		List<Iteration> iterations = new ArrayList<Iteration>();
		
		context = ObjectUtil.nvl(context, new EvaluationContext());
		if (context == null) {
			context = new EvaluationContext();
		}
		
		PeriodIndicatorDataSetDefinition pidsd = (PeriodIndicatorDataSetDefinition)dataSetDefinition;
	
		MultiPeriodIndicatorDataSetDefinition mpdsd = new MultiPeriodIndicatorDataSetDefinition();
		mpdsd.setBaseDefinition(pidsd.getBaseDefinition());
		mpdsd.setName(pidsd.getName());
		
		Calendar endDate = Calendar.getInstance();
		endDate.setTime(pidsd.getEndDate());
		Calendar startDate = Calendar.getInstance();
		startDate.setTime(endDate.getTime());
		startDate.add(Calendar.MONTH, -3);
		
		for(int i = 0; i <= pidsd.getQuarters(); i++)
		{
			System.out.println(startDate.getTime());
			System.out.println(endDate.getTime());
			Iteration iter = new Iteration(startDate.getTime(), endDate.getTime(), pidsd.getLocation());
			iterations.add(iter);
			
			startDate.add(Calendar.MONTH, -3);
			endDate.add(Calendar.MONTH, -3);
		}
		
		for(int i=pidsd.getQuarters() -1; i >= 0; i--)
		{
			mpdsd.addIteration(iterations.get(i));
		}
		
		EvaluationContext ec = EvaluationContext.clone(context);
		
		return Context.getService(DataSetDefinitionService.class).evaluate(mpdsd, ec);
	}
	
}
