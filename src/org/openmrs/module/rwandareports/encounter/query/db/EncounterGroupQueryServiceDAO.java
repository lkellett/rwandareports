package org.openmrs.module.rwandareports.encounter.query.db;

import java.util.Map;

import org.openmrs.module.rwandareports.encounter.EncounterGroup;

public interface EncounterGroupQueryServiceDAO {

	
	 public EncounterGroup executeSqlQuery(String sqlQuery, Map<String,Object> paramMap);
	 
	 
}
