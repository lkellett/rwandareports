package org.openmrs.module.rwandareports.dataset;

import org.openmrs.Concept;
import org.openmrs.module.orderextension.DrugRegimen;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;


/**
 *
 */
public class ExtendedDrugOrderDataSetDefinition extends BaseDataSetDefinition {
	
	
	//***** PROPERTIES *****
	
	@ConfigurationProperty
	Integer drugRegimen = null;
	
	@ConfigurationProperty
	Concept indication = null;
	
	
	//***** CONSTRUCTORS *****
	
	/**
	 * Default Constructor
	 */
	public ExtendedDrugOrderDataSetDefinition() {
		
	}
	
    public Integer getDrugRegimen() {
    	return drugRegimen;
    }
	
    public void setDrugRegimen(Integer drugRegimen) {
    	this.drugRegimen = drugRegimen;
    }
	
    public Concept getIndication() {
    	return indication;
    }

    public void setIndication(Concept indication) {
    	this.indication = indication;
    }
}
