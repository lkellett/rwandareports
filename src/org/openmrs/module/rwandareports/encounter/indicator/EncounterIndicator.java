package org.openmrs.module.rwandareports.encounter.indicator;

import java.util.Map;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.BaseIndicator;
import org.openmrs.module.reporting.indicator.aggregation.Aggregator;
import org.openmrs.module.rwandareports.encounter.definition.EncounterGroupDefinition;

/**
 * 
 * Represents an Indicator that can be built out of EncounterGroups.
 * 
 * Behaves similarly to a CohortIndicator, except that there is additional 'per day' functionality, that allows you to put a number of days in the denominator, thus creating a 'per day' indicator
 * Additionally, there is a perHourDenominator property that allows you to multiply the denominator by a number of hours, creating the possibility of a 'per hour' indicator
 * 
 * @author dthomas
 *
 */
public class EncounterIndicator extends BaseIndicator {

	private static final long serialVersionUID = 1L;
	
	/**
     * Enumerated Indicator Types
     * PER_DAY, PER_WEEKDAYS allow you to divide by the number of days, or the number of weekdays between startDate and endDate parameters.
     * 
     */
    public enum IndicatorType {
//    	COUNT, FRACTION, LOGIC
    	COUNT, FRACTION, PER_DAY, PER_WEEKDAYS
    }
    
    //***** PROPERTIES *****
    
    private IndicatorType type = IndicatorType.COUNT;
    private Mapped<? extends EncounterGroupDefinition> encounterGroupDefinition;
    private Mapped<? extends EncounterGroupDefinition> denominator;
    private Mapped<? extends EncounterGroupDefinition> locationFilter;
    private Integer perHourDenominator = 1;


	public Integer getPerHourDenominator() {
		return perHourDenominator;
	}

	public void setPerHourDenominator(Integer perHourDenominator) {
		this.perHourDenominator = perHourDenominator;
	}

	private Class<? extends Aggregator> aggregator;
    //private String logicExpression;

    //***** CONSTRUCTORS *****
    
    /**
     * Default Constructor
     */
    public EncounterIndicator() {
    	super();
    	addParameter(ReportingConstants.LOCATION_PARAMETER);
    }
    
    /**
     * Default Constructor with name
     */
    public EncounterIndicator(String name) {
    	this();
    	setName(name);
    }
    
    //***** FACTORY METHODS *****
    
    /**
     * Constructs a new Count Indicator
     */
    public static EncounterIndicator newCountIndicator(String name,
    												Mapped<? extends EncounterGroupDefinition> encounterGroupDefinition, 
    												Mapped<? extends EncounterGroupDefinition> locationFilter) {
    	EncounterIndicator ci = new EncounterIndicator(name);
    	ci.setType(IndicatorType.COUNT);
    	ci.setEncounterGroupDefinition(encounterGroupDefinition);
    	ci.setLocationFilter(locationFilter);
    	return ci;
    }
    
    /**
     * Constructs a new Fraction Indicator
     */
    public static EncounterIndicator newFractionIndicator(String name,
    												   Mapped<? extends EncounterGroupDefinition> numerator, 
    												   Mapped<? extends EncounterGroupDefinition> denominator, 
    												   Mapped<? extends EncounterGroupDefinition> locationFilter) {
    	EncounterIndicator ci = new EncounterIndicator(name);
    	ci.setType(IndicatorType.FRACTION);
    	ci.setEncounterGroupDefinition(numerator);
    	ci.setDenominator(denominator);
    	ci.setLocationFilter(locationFilter);
    	return ci;
    }
    
    /**
     * Constructs a new Fraction Indicator
     */
    public static EncounterIndicator newDailyDivisionIndicator(String name,
    												   Mapped<? extends EncounterGroupDefinition> numerator, 
    												   Integer perHourDenominator,  EncounterIndicator.IndicatorType type,
    												   Mapped<? extends EncounterGroupDefinition> locationFilter) {
    	EncounterIndicator ci = new EncounterIndicator(name);
    	ci.setType(type);
    	ci.setEncounterGroupDefinition(numerator);
    	ci.setPerHourDenominator(perHourDenominator);
    	ci.setLocationFilter(locationFilter);
    	return ci;
    }
    
//    /**
//     * Constructs a new Logic Indicator
//     */
//    public static EncounterIndicator newLogicIndicator(String name,
//    												Mapped<? extends EncounterGroupDefinition> encounterGroupDefinition,  
//    												Mapped<? extends EncounterGroupDefinition> locationFilter,
//    												Class<? extends Aggregator> aggregator,
//    												String logicExpression) {
//    	EncounterIndicator ci = new EncounterIndicator(name);
//    	ci.setType(IndicatorType.LOGIC);
//    	ci.setEncounterGroupDefinition(encounterGroupDefinition);
//    	ci.setLocationFilter(locationFilter);
//    	ci.setAggregator(aggregator);
//    	//ci.setLogicExpression(logicExpression);
//    	return ci;
//    }
	
    //***** Methods *****


	public String toString() {
    	return getName();
    }
    
    //***** Property Access *****

    /**
	 * @return the type
	 */
	public IndicatorType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(IndicatorType type) {
		this.type = type;
	}
    
	/**
     * @return the EcounterGroupDefinition
     */
    public Mapped<? extends EncounterGroupDefinition> getEncounterGroupDefinition() {
    	return encounterGroupDefinition;
    }

	/**
     * @param EcounterGroupDefinition the EcounterGroupDefinition to set
     */
    public void setEncounterGroupDefinition(Mapped<? extends EncounterGroupDefinition> encounterGroupDefinition) {
    	this.encounterGroupDefinition = encounterGroupDefinition;
    }
    
    /**
     * @param EcounterGroupDefinition the EcounterGroupDefinition to set
     */
    public void setEncounterGroupDefinition(EncounterGroupDefinition encounterGroupDefinition, Map<String, Object> mappings) {
    	this.encounterGroupDefinition = new Mapped<EncounterGroupDefinition>(encounterGroupDefinition, mappings);
    }
    
    /**
     * @param EcounterGroupDefinition the EcounterGroupDefinition to set
     */
    public void setEncounterGroupDefinition(EncounterGroupDefinition encounterGroupDefinition, String mappings) {
    	Map<String, Object> m = ParameterizableUtil.createParameterMappings(mappings);
    	setEncounterGroupDefinition(encounterGroupDefinition, m);
    }
    
    /**
	 * @return the denominator
	 */
	public Mapped<? extends EncounterGroupDefinition> getDenominator() {
		return denominator;
	}

	/**
	 * @param denominator the denominator to set
	 */
	public void setDenominator(Mapped<? extends EncounterGroupDefinition> denominator) {
		this.denominator = denominator;
	}

    /**
     * @param denominator the denominator to set
     */
    public void setDenominator(EncounterGroupDefinition denominator, Map<String, Object> mappings) {
    	this.denominator = new Mapped<EncounterGroupDefinition>(denominator, mappings);
    }
    
    /**
     * @param denominator the denominator to set
     */
    public void setDenominator(EncounterGroupDefinition denominator, String mappings) {
    	Map<String, Object> m = ParameterizableUtil.createParameterMappings(mappings);
    	setDenominator(denominator, m);
    }

	/**
	 * @return the locationFilter
	 */
	public Mapped<? extends EncounterGroupDefinition> getLocationFilter() {
		return locationFilter;
	}

	/**
	 * @param locationFilter the locationFilter to set
	 */
	public void setLocationFilter(Mapped<? extends EncounterGroupDefinition> locationFilter) {
		this.locationFilter = locationFilter;
	}

    /**
     * @param locationFilter the locationFilter to set
     */
    public void setLocationFilter(EncounterGroupDefinition locationFilter, Map<String, Object> mappings) {
    	this.locationFilter = new Mapped<EncounterGroupDefinition>(locationFilter, mappings);
    }
    
    /**
     * @param locationFilter the locationFilter to set
     */
    public void setLocationFilter(EncounterGroupDefinition locationFilter, String mappings) {
    	Map<String, Object> m = ParameterizableUtil.createParameterMappings(mappings);
    	setLocationFilter(locationFilter, m);
    }

//	/**
//	 * @return the logicExpression
//	 */
//	public String getLogicExpression() {
//		return logicExpression;
//	}
//
//	/**
//	 * @param logicExpression the logicExpression to set
//	 */
//	public void setLogicExpression(String logicExpression) {
//		this.logicExpression = logicExpression;
//	}

	/**
     * @return the aggregator
     */
    public Class<? extends Aggregator> getAggregator() {
    	return aggregator;
    }
	
    /**
     * @param aggregator the aggregator to set
     */
    public void setAggregator(Class<? extends Aggregator> aggregator) {
    	this.aggregator = aggregator;
    }
    
}
