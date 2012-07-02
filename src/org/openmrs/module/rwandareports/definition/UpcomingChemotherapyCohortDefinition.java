/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.rwandareports.definition;

import java.util.Date;

import org.openmrs.Concept;
import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

/**
 * 
 */
public class UpcomingChemotherapyCohortDefinition extends BaseCohortDefinition {

    private static final long serialVersionUID = 1L;
    
	//***** PROPERTIES *****	
	
	@ConfigurationProperty(required=false)
	private Date asOfDate;
	
	@ConfigurationProperty(required=false)
	private Date untilDate;
	
	@ConfigurationProperty(required=true)
	private Concept chemotherapyIndication;

	//***** CONSTRUCTORS *****

	/**
	 * Default constructor
	 */
	public UpcomingChemotherapyCohortDefinition() {
		super();
	}
	
	//***** INSTANCE METHODS *****
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {

		StringBuilder ret = new StringBuilder();
		ret.append("Patients that have a chemotherapy cylce within the next 7 days.");		
		return ret.toString();
	}

	//***** PROPERTY ACCESS *****
    
	public Date getAsOfDate() {
		return asOfDate;
	}

	public void setAsOfDate(Date asOfDate) {
		this.asOfDate = asOfDate;
	}

    public Date getUntilDate() {
    	return untilDate;
    }
    
    public void setUntilDate(Date untilDate) {
    	this.untilDate = untilDate;
    }

    public Concept getChemotherapyIndication() {
    	return chemotherapyIndication;
    }
    
    public void setChemotherapyIndication(Concept chemotherapyIndication) {
    	this.chemotherapyIndication = chemotherapyIndication;
    }
}
