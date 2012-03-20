package org.openmrs.module.rwandareports.indicator;

import java.util.Set;

import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.indicator.Indicator;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.reporting.query.encounter.EncounterQueryResult;


public class EncounterIndicatorResult implements IndicatorResult{

	private EncounterIndicator indicator;
	private EvaluationContext context;
	
	private EncounterQueryResult result;
	 
    public EncounterIndicator getIndicator() {
    	return indicator;
    }

    public void setIndicator(EncounterIndicator indicator) {
    	this.indicator = indicator;
    }

    public void setContext(EvaluationContext context) {
    	this.context = context;
    }
  
	public EvaluationContext getContext() {
	   return context;
    }

	public Indicator getDefinition() {
	    return indicator;
    }

	public Number getValue() {
		return result.getSize();
    }
    
    public Set<Integer> getMemberIds() {
    	return result.getMemberIds();
    }
	
    public EncounterQueryResult getResult() {
    	return result;
    }

    public void setResult(EncounterQueryResult result) {
    	this.result = result;
    }
}
