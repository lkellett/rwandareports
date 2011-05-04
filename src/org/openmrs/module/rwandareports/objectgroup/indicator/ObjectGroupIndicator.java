package org.openmrs.module.rwandareports.objectgroup.indicator;

import java.util.Map;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.BaseIndicator;
import org.openmrs.module.reporting.indicator.aggregation.Aggregator;
import org.openmrs.module.rwandareports.objectgroup.definition.ObjectGroupDefinition;

/**
 * 
 * Represents an Indicator that can be built out of ObjectGroups.
 * 
 * Behaves similarly to a CohortIndicator, except that there is additional 'per day' functionality, that allows you to put a number of days in the denominator, thus creating a 'per day' indicator
 * Additionally, there is a perHourDenominator property that allows you to multiply the denominator by a number of hours, creating the possibility of a 'per hour' indicator
 * 
 * @author dthomas
 *
 */
public class ObjectGroupIndicator extends BaseIndicator {

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
    private Mapped<? extends ObjectGroupDefinition> objectGroupDefinition;
    private Mapped<? extends ObjectGroupDefinition> denominator;
    private Mapped<? extends ObjectGroupDefinition> locationFilter;
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
    public ObjectGroupIndicator() {
    	super();
    	addParameter(ReportingConstants.LOCATION_PARAMETER);
    }
    
    /**
     * Default Constructor with name
     */
    public ObjectGroupIndicator(String name) {
    	this();
    	setName(name);
    }
    
    //***** FACTORY METHODS *****
    
    /**
     * Constructs a new Count Indicator
     */
    public static ObjectGroupIndicator newCountIndicator(String name,
    												Mapped<? extends ObjectGroupDefinition> objectGroupDefinition, 
    												Mapped<? extends ObjectGroupDefinition> locationFilter) {
    	ObjectGroupIndicator ci = new ObjectGroupIndicator(name);
    	ci.setType(IndicatorType.COUNT);
    	ci.setObjectGroupDefinition(objectGroupDefinition);
    	ci.setLocationFilter(locationFilter);
    	return ci;
    }
    
    /**
     * Constructs a new Fraction Indicator
     */
    public static ObjectGroupIndicator newFractionIndicator(String name,
    												   Mapped<? extends ObjectGroupDefinition> numerator, 
    												   Mapped<? extends ObjectGroupDefinition> denominator, 
    												   Mapped<? extends ObjectGroupDefinition> locationFilter) {
    	ObjectGroupIndicator ci = new ObjectGroupIndicator(name);
    	ci.setType(IndicatorType.FRACTION);
    	ci.setObjectGroupDefinition(numerator);
    	ci.setDenominator(denominator);
    	ci.setLocationFilter(locationFilter);
    	return ci;
    }
    
    /**
     * Constructs a new Fraction Indicator
     */
    public static ObjectGroupIndicator newDailyDivisionIndicator(String name,
    												   Mapped<? extends ObjectGroupDefinition> numerator, 
    												   Integer perHourDenominator,  ObjectGroupIndicator.IndicatorType type,
    												   Mapped<? extends ObjectGroupDefinition> locationFilter) {
    	ObjectGroupIndicator ci = new ObjectGroupIndicator(name);
    	ci.setType(type);
    	ci.setObjectGroupDefinition(numerator);
    	ci.setPerHourDenominator(perHourDenominator);
    	ci.setLocationFilter(locationFilter);
    	return ci;
    }
    
//    /**
//     * Constructs a new Logic Indicator
//     */
//    public static ObjectGroupIndicator newLogicIndicator(String name,
//    												Mapped<? extends ObjectGroupDefinition> encounterGroupDefinition,  
//    												Mapped<? extends ObjectGroupDefinition> locationFilter,
//    												Class<? extends Aggregator> aggregator,
//    												String logicExpression) {
//    	ObjectGroupIndicator ci = new ObjectGroupIndicator(name);
//    	ci.setType(IndicatorType.LOGIC);
//    	ci.setObjectGroupDefinition(encounterGroupDefinition);
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
    public Mapped<? extends ObjectGroupDefinition> getObjectGroupDefinition() {
    	return objectGroupDefinition;
    }

	/**
     * @param EcounterGroupDefinition the EcounterGroupDefinition to set
     */
    public void setObjectGroupDefinition(Mapped<? extends ObjectGroupDefinition> objectGroupDefinition) {
    	this.objectGroupDefinition = objectGroupDefinition;
    }
    
    /**
     * @param EcounterGroupDefinition the EcounterGroupDefinition to set
     */
    public void setObjectGroupDefinition(ObjectGroupDefinition objectGroupDefinition, Map<String, Object> mappings) {
    	this.objectGroupDefinition = new Mapped<ObjectGroupDefinition>(objectGroupDefinition, mappings);
    }
    
    /**
     * @param EcounterGroupDefinition the EcounterGroupDefinition to set
     */
    public void setObjectGroupDefinition(ObjectGroupDefinition objectGroupDefinition, String mappings) {
    	Map<String, Object> m = ParameterizableUtil.createParameterMappings(mappings);
    	setObjectGroupDefinition(objectGroupDefinition, m);
    }
    
    /**
	 * @return the denominator
	 */
	public Mapped<? extends ObjectGroupDefinition> getDenominator() {
		return denominator;
	}

	/**
	 * @param denominator the denominator to set
	 */
	public void setDenominator(Mapped<? extends ObjectGroupDefinition> denominator) {
		this.denominator = denominator;
	}

    /**
     * @param denominator the denominator to set
     */
    public void setDenominator(ObjectGroupDefinition denominator, Map<String, Object> mappings) {
    	this.denominator = new Mapped<ObjectGroupDefinition>(denominator, mappings);
    }
    
    /**
     * @param denominator the denominator to set
     */
    public void setDenominator(ObjectGroupDefinition denominator, String mappings) {
    	Map<String, Object> m = ParameterizableUtil.createParameterMappings(mappings);
    	setDenominator(denominator, m);
    }

	/**
	 * @return the locationFilter
	 */
	public Mapped<? extends ObjectGroupDefinition> getLocationFilter() {
		return locationFilter;
	}

	/**
	 * @param locationFilter the locationFilter to set
	 */
	public void setLocationFilter(Mapped<? extends ObjectGroupDefinition> locationFilter) {
		this.locationFilter = locationFilter;
	}

    /**
     * @param locationFilter the locationFilter to set
     */
    public void setLocationFilter(ObjectGroupDefinition locationFilter, Map<String, Object> mappings) {
    	this.locationFilter = new Mapped<ObjectGroupDefinition>(locationFilter, mappings);
    }
    
    /**
     * @param locationFilter the locationFilter to set
     */
    public void setLocationFilter(ObjectGroupDefinition locationFilter, String mappings) {
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
