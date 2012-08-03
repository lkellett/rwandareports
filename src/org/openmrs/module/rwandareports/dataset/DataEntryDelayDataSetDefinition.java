package org.openmrs.module.rwandareports.dataset;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.EncounterType;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.rwandareports.widget.AllLocation;


/**
 *
 */
public class DataEntryDelayDataSetDefinition extends BaseDataSetDefinition {
	
	//***** PROPERTIES *****
	@ConfigurationProperty
	private AllLocation location;
	
	@ConfigurationProperty
	private List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
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

    public List<EncounterType> getEncounterTypes() {
    	return encounterTypes;
    }

    public void setEncounterTypes(List<EncounterType> encounterTypes) {
    	this.encounterTypes = encounterTypes;
    }
    
    public void addEncounterType(EncounterType encounterType) {
    	encounterTypes.add(encounterType);
    }
}
