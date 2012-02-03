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
package org.openmrs.module.rwandareports.dataset;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.SimplePatientDataSetEvaluator;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;

/**
 * This is a simple example of how one might implement a row-per-Patient DataSetDefinition
 * There are no guarantees that this class will be backwards compatible, or exist in a future
 * release, so should be used with caution
 * @see SimplePatientDataSetEvaluator
 */
public class HIVARTRegisterDataSetDefinition extends BaseDataSetDefinition {

	
	private static final long serialVersionUID = 6405583324151111487L;
	
	@ConfigurationProperty
	private List<RowPerPatientData> columns;
	
	@ConfigurationProperty
	private List<CohortDefinition> filters;
	
	
	
	/**
	 * Constructor
	 */
	public HIVARTRegisterDataSetDefinition() {
		super();
	}
	
	/**
	 * Public constructor with name and description
	 */
	public HIVARTRegisterDataSetDefinition(String name, String description) {
		super(name, description);
	}

	
    /**
     * @return the columns
     */
    public List<RowPerPatientData> getColumns() {
    	if (columns == null) {
			columns = new ArrayList<RowPerPatientData>();
		}
    	return columns;
    }

	
    /**
     * @param columns the columns to set
     */
    public void setColumns(List<RowPerPatientData> columns) {
    	this.columns = columns;
    }

	
	public void addColumn(RowPerPatientData column)
	{
		getColumns().add(column);
	}

	
    /**
     * @return the filters
     */
    public List<CohortDefinition> getFilters() {
    	if (filters == null) {
			filters = new ArrayList<CohortDefinition>();
		}
    	return filters;
    }

	
    /**
     * @param filters the filters to set
     */
    public void setFilters(List<CohortDefinition> filters) {
    	this.filters = filters;
    }

    public void addFilter(CohortDefinition cohort)
    {
    	getFilters().add(cohort);
    }
	
}

