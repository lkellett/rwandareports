package org.openmrs.module.rwandareports.dataset;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.rwandareports.indicator.EncounterIndicator;


/**
 *
 */
public class EncounterIndicatorDataSetDefinition extends BaseDataSetDefinition {
	
	
	//***** PROPERTIES *****
	
	@ConfigurationProperty
	List<EncounterIndicator> columns = new ArrayList<EncounterIndicator>();
	
	
	//***** CONSTRUCTORS *****
	
	/**
	 * Default Constructor
	 */
	public EncounterIndicatorDataSetDefinition() {
		
	}

	public void addColumn(EncounterIndicator indicator)
	{
		columns.add(indicator);
	}
	
    public List<EncounterIndicator> getColumns() {
    	return columns;
    }

    public void setColumns(List<EncounterIndicator> columns) {
    	this.columns = columns;
    }
}
