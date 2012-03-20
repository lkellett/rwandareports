package org.openmrs.module.rwandareports.dataset;

import java.util.Date;

import org.openmrs.Location;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;


/**
 *
 */
public class PeriodIndicatorDataSetDefinition extends BaseDataSetDefinition {
	
	
	//***** PROPERTIES *****
	
	@ConfigurationProperty
	private CohortIndicatorDataSetDefinition baseDefinition;
	
	@ConfigurationProperty
	private Integer quarters;
	
	@ConfigurationProperty
	private Date endDate;
	
	@ConfigurationProperty
	private Location location;
	
	//***** CONSTRUCTORS *****
	
	/**
	 * Default Constructor
	 */
	public PeriodIndicatorDataSetDefinition() {
		
	}
	
	/**
	 * Base Constructor
	 */
	public PeriodIndicatorDataSetDefinition(CohortIndicatorDataSetDefinition baseDefinition) {
		this();
		this.baseDefinition = baseDefinition;
	}
	
	//***** INSTANCE METHODS *****
	
	
	//***** PROPERTY ACCESS *****
	
    /**
     * @return the baseDefinition
     */
    public CohortIndicatorDataSetDefinition getBaseDefinition() {
    	return baseDefinition;
    }
	
    /**
     * @param baseDefinition the baseDefinition to set
     */
    public void setBaseDefinition(CohortIndicatorDataSetDefinition baseDefinition) {
    	this.baseDefinition = baseDefinition;
    }

	public Integer getQuarters() {
		return quarters;
	}

	public void setQuarters(Integer quarters) {
		this.quarters = quarters;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}
