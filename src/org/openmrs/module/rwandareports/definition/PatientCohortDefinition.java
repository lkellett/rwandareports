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

import org.openmrs.Patient;
import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

/**
 * 
 */
public class PatientCohortDefinition extends BaseCohortDefinition {

    private static final long serialVersionUID = 1L;
    
	//***** PROPERTIES *****

	@ConfigurationProperty(required=false)
	private Patient patient;
	
	@ConfigurationProperty(required=false)
	private String patientId;
	
	

	//***** CONSTRUCTORS *****

	/**
	 * Default constructor
	 */
	public PatientCohortDefinition() {
		super();
	}
	
	//***** INSTANCE METHODS *****
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {

		return "Individual patient cohort";
	}

	//***** PROPERTY ACCESS *****
	
    public Patient getPatient() {
    	return patient;
    }

	
    public void setPatient(Patient patient) {
    	this.patient = patient;
    }
	
    public String getPatientId() {
    	return patientId;
    }

    public void setPatientId(String patientId) {
    	this.patientId = patientId;
    }
}
