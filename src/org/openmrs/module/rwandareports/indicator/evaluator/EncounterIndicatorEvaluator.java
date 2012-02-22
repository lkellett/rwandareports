package org.openmrs.module.rwandareports.indicator.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.indicator.Indicator;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.reporting.indicator.evaluator.IndicatorEvaluator;
import org.openmrs.module.reporting.query.encounter.EncounterQueryResult;
import org.openmrs.module.reporting.query.encounter.service.EncounterQueryService;
import org.openmrs.module.rwandareports.indicator.EncounterIndicator;
import org.openmrs.module.rwandareports.indicator.EncounterIndicatorResult;

@Handler(supports={EncounterIndicator.class})
public class EncounterIndicatorEvaluator implements IndicatorEvaluator {
	
	public IndicatorResult evaluate(Indicator indicator, EvaluationContext context) throws EvaluationException {
		
		EncounterIndicator ei = (EncounterIndicator) indicator;
    	
    	EncounterIndicatorResult result = new EncounterIndicatorResult();
    	result.setContext(context);
    	result.setIndicator(ei);
		
		EncounterQueryService eqs = Context.getService(EncounterQueryService.class);
		
		EncounterQueryResult eqr = eqs.evaluate(ei.getEncounterQuery(), context);
		
		result.setResult(eqr);
		
		return result;
	}
}
