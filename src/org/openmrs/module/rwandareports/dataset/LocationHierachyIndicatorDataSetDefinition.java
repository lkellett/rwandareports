package org.openmrs.module.rwandareports.dataset;

import java.util.ArrayList;
import java.util.List;

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
	private List<DataSetDefinition> baseDefinition = new ArrayList<DataSetDefinition>();
	
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
		this.baseDefinition.add(baseDefinition);
	}
	
	//***** INSTANCE METHODS *****
	
	
	//***** PROPERTY ACCESS *****
	
    /**
     * @return the baseDefinition
     */
    public List<DataSetDefinition> getBaseDefinition() {
    	return baseDefinition;
    }
	
    /**
     * @param baseDefinition the baseDefinition to set
     */
    public void setBaseDefinition(List<DataSetDefinition> baseDefinition) {
    	this.baseDefinition = baseDefinition;
    }
    
    public void addBaseDefinition(DataSetDefinition baseDefinition) {
    	this.baseDefinition.add(baseDefinition);
    }

	public AllLocation getLocation() {
		return location;
	}

	public void setLocation(AllLocation location) {
		this.location = location;
	}
}
