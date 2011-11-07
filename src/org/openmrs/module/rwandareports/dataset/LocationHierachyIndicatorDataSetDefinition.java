package org.openmrs.module.rwandareports.dataset;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.rwandareports.widget.AllLocation;


/**
 *
 */
public class LocationHierachyIndicatorDataSetDefinition extends BaseDataSetDefinition {
	
	//***** PROPERTIES *****
	
	@ConfigurationProperty
	private DataSetDefinition baseDefinition;
	
	@ConfigurationProperty
	private AllLocation location;
	
	
	//***** CONSTRUCTORS *****
	
	/**
	 * Default Constructor
	 */
	public LocationHierachyIndicatorDataSetDefinition() {
		
	}
	
	/**
	 * Base Constructor
	 */
	public LocationHierachyIndicatorDataSetDefinition(DataSetDefinition baseDefinition) {
		this();
		this.baseDefinition = baseDefinition;
	}
	
	//***** INSTANCE METHODS *****
	
	
	//***** PROPERTY ACCESS *****
	
    /**
     * @return the baseDefinition
     */
    public DataSetDefinition getBaseDefinition() {
    	return baseDefinition;
    }
	
    /**
     * @param baseDefinition the baseDefinition to set
     */
    public void setBaseDefinition(DataSetDefinition baseDefinition) {
    	this.baseDefinition = baseDefinition;
    }

	public AllLocation getLocation() {
		return location;
	}

	public void setLocation(AllLocation location) {
		this.location = location;
	}
}
