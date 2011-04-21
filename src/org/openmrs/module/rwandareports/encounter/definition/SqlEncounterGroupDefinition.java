package org.openmrs.module.rwandareports.encounter.definition;

import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;




public class SqlEncounterGroupDefinition extends EncounterGroupDefinition {
	
	private static final long serialVersionUID = 1L;
	
	@ConfigurationProperty(required=true)
	private String query;
	

	public SqlEncounterGroupDefinition() {
		super();
	}
	
	/**
	 * 
	 * @param sqlQuery
	 */
	public SqlEncounterGroupDefinition(String query) { 
		this.query = query;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "SQL EncounterGroup Query: [" + ObjectUtil.nvlStr(query, "") + "]";
	}
	
	//***** PROPERTY ACCESS *****

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}
    
}
