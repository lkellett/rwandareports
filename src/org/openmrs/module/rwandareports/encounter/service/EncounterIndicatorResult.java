package org.openmrs.module.rwandareports.encounter.service;

import org.openmrs.module.reporting.common.Fraction;
import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.rwandareports.encounter.EncounterGroup;
import org.openmrs.module.rwandareports.encounter.indicator.EncounterIndicator;
import org.openmrs.module.rwandareports.encounter.indicator.EncounterIndicator.IndicatorType;

public class EncounterIndicatorResult implements IndicatorResult {
	
	
private static final long serialVersionUID = 1L;
    
    //***** PROPERTIES *****

    private EncounterIndicator indicator;
    private EvaluationContext context;
    
    private EncounterGroup encounterGroup;
    private EncounterGroup denominatorEncounterGroup;
    private int perDay = 1;
    private int numDays;
    //private Map<Integer, Number> logicResults = new HashMap<Integer, Number>(); // patient id -> logic value

    //***** CONSTRUCTORS *****
    
    /**
     * Default Constructor
     */
    public EncounterIndicatorResult() {
    	super();
    }
    
    public Integer getNumDays() {
		return numDays;
	}

	public void setNumDays(Integer numDays) {
		this.numDays = numDays;
	}

	public static Number getResultValue(EncounterIndicatorResult encounterIndicatorResult, EncounterGroup...filters) {
    	
    	IndicatorType type = encounterIndicatorResult.getDefinition().getType();
    	EncounterGroup numerator = encounterIndicatorResult.getEncounterGroup();
    	EncounterGroup denominator = encounterIndicatorResult.getEncounterGroup();
    	
    	
//    	Map<Integer, Number> logicVals = new HashMap<Integer, Number>(EncounterIndicatorResult.getLogicResults());
    	

    	if (filters != null) {
	    	for (EncounterGroup filter : filters) {
	    		if (filter != null) {
		    		numerator = EncounterGroup.intersect(numerator, filter);
		    		if (type == IndicatorType.FRACTION) {
		    			denominator = EncounterGroup.intersect(denominator, filter);
		    		}
//		    		else if (type == IndicatorType.LOGIC) {
//		    			logicVals.keySet().retainAll(filter.getMemberIds());
//		    		}
	    		}
	    	}
    	}
    	
    	// Return the appropriate result, given the IndicatorType
    	if (type == IndicatorType.FRACTION) {
    		int n = numerator.getSize();
    		int d = denominator.getSize();
    		return new Fraction(n, d);
    	}
    	else if (type == IndicatorType.PER_DAY || type == IndicatorType.PER_WEEKDAYS){
    		int n = numerator.getSize();
    		return new Fraction(n, (encounterIndicatorResult.getNumDays() * encounterIndicatorResult.getPerDay()));
    	}
    	
    	
//    	else if (type == IndicatorType.LOGIC) {
//    		Class<? extends Aggregator> aggregator = EncounterIndicatorResult.getDefinition().getAggregator();
//        	if (aggregator == null) {
//        		aggregator = CountAggregator.class;
//        	}
//        	return AggregationUtil.aggregate(logicVals.values(), aggregator);
//    	}
    	else { // Assume IndicatorType.COUNT
    		return numerator.getSize();
    	}
    }
    
    public Integer getPerDay() {
		return perDay;
	}

	public void setPerDay(Integer perDay) {
		this.perDay = perDay;
	}

	/**
     * @see IndicatorResult#getValue()
     */
    public Number getValue() {
    	return EncounterIndicatorResult.getResultValue(this);
    }
    
	/** 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		Number value = getValue();
		return (value == null ? "null" : value.toString());
	}
    
    //***** Property Access *****

	/**
	 * @see Evaluated#getDefinition()
	 */
	public EncounterIndicator getDefinition() {
		return indicator;
	}

	/**
	 * @return the indicator
	 */
	public EncounterIndicator getIndicator() {
		return indicator;
	}

	/**
	 * @param indicator the indicator to set
	 */
	public void setIndicator(EncounterIndicator indicator) {
		this.indicator = indicator;
	}

	/**
	 * @return the context
	 */
	public EvaluationContext getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(EvaluationContext context) {
		this.context = context;
	}

	public EncounterGroup getEncounterGroup() {
		return encounterGroup;
	}

	public void setEncounterGroup(EncounterGroup encounterGroup) {
		this.encounterGroup = encounterGroup;
	}

	public EncounterGroup getDenominatorEncounterGroup() {
		return denominatorEncounterGroup;
	}

	public void setDenominatorEncounterGroup(
			EncounterGroup denominatorEncounterGroup) {
		this.denominatorEncounterGroup = denominatorEncounterGroup;
	}

//	/**
//	 * @return the logicResults
//	 */
//	public Map<Integer, Number> getLogicResults() {
//		if (logicResults == null) {
//			logicResults = new HashMap<Integer, Number>();
//		}
//		return logicResults;
//	}
//
//	/**
//	 * @param logicResults the logicResults to set
//	 */
//	public void setLogicResults(Map<Integer, Number> logicResults) {
//		this.logicResults = logicResults;
//	}
//
//	/**
//	 * @param patientId the patientId for which to add a logic result
//	 * @param logicResult the logic result to add
//	 */
//	public void addLogicResult(Integer patientId, Number logicResult) {
//		getLogicResults().put(patientId, logicResult);
//	}
    
    
}
