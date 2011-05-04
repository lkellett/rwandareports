package org.openmrs.module.rwandareports.objectgroup.service;

import org.openmrs.module.reporting.common.Fraction;
import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.indicator.IndicatorResult;
import org.openmrs.module.rwandareports.objectgroup.ObjectGroup;
import org.openmrs.module.rwandareports.objectgroup.indicator.ObjectGroupIndicator;
import org.openmrs.module.rwandareports.objectgroup.indicator.ObjectGroupIndicator.IndicatorType;

public class ObjectGroupIndicatorResult implements IndicatorResult {
	
	
private static final long serialVersionUID = 1L;
    
    //***** PROPERTIES *****

    private ObjectGroupIndicator indicator;
    private EvaluationContext context;
    
    private ObjectGroup objectGroup;
    private ObjectGroup denominatorObjectGroup;
    private int perDay = 1;
    private int numDays;
    //private Map<Integer, Number> logicResults = new HashMap<Integer, Number>(); // patient id -> logic value

    //***** CONSTRUCTORS *****
    
    /**
     * Default Constructor
     */
    public ObjectGroupIndicatorResult() {
    	super();
    }
    
    public Integer getNumDays() {
		return numDays;
	}

	public void setNumDays(Integer numDays) {
		this.numDays = numDays;
	}

	public static Number getResultValue(ObjectGroupIndicatorResult objectGroupIndicatorResult, ObjectGroup...filters) {
    	
    	IndicatorType type = objectGroupIndicatorResult.getDefinition().getType();
    	ObjectGroup numerator = objectGroupIndicatorResult.getObjectGroup();
    	ObjectGroup denominator = objectGroupIndicatorResult.getDenominatorObjectGroup();
    	
    	
//    	Map<Integer, Number> logicVals = new HashMap<Integer, Number>(ObjectGroupIndicatorResult.getLogicResults());
    	

    	if (filters != null) {
	    	for (ObjectGroup filter : filters) {
	    		if (filter != null) {
		    		numerator = ObjectGroup.intersect(numerator, filter);
		    		if (type == IndicatorType.FRACTION) {
		    			denominator = ObjectGroup.intersect(denominator, filter);
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
    		return new Fraction(n, (objectGroupIndicatorResult.getNumDays() * objectGroupIndicatorResult.getPerDay()));
    	}
    	
    	
//    	else if (type == IndicatorType.LOGIC) {
//    		Class<? extends Aggregator> aggregator = ObjectGroupIndicatorResult.getDefinition().getAggregator();
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
    	return ObjectGroupIndicatorResult.getResultValue(this);
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
	public ObjectGroupIndicator getDefinition() {
		return indicator;
	}

	/**
	 * @return the indicator
	 */
	public ObjectGroupIndicator getIndicator() {
		return indicator;
	}

	/**
	 * @param indicator the indicator to set
	 */
	public void setIndicator(ObjectGroupIndicator indicator) {
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



	public ObjectGroup getObjectGroup() {
		return objectGroup;
	}

	public void setObjectGroup(ObjectGroup objectGroup) {
		this.objectGroup = objectGroup;
	}

	public void setPerDay(int perDay) {
		this.perDay = perDay;
	}

	public void setNumDays(int numDays) {
		this.numDays = numDays;
	}

	public ObjectGroup getDenominatorObjectGroup() {
		return denominatorObjectGroup;
	}

	public void setDenominatorObjectGroup(
			ObjectGroup denominatorObjectGroup) {
		this.denominatorObjectGroup = denominatorObjectGroup;
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
