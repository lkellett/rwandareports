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
public class DataEntryDelayDataSetDefinition extends BaseDataSetDefinition {
	
	//***** PROPERTIES *****
	@ConfigurationProperty
	private AllLocation location;
	
	
	//***** CONSTRUCTORS *****
	
	/**
	 * Default Constructor
	 */
	public DataEntryDelayDataSetDefinition() {
		
	}
	
	//***** INSTANCE METHODS *****
	
	
	//***** PROPERTY ACCESS *****

	public AllLocation getLocation() {
		return location;
	}

	public void setLocation(AllLocation location) {
		this.location = location;
	}
}
