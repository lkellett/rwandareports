package org.openmrs.module.rwandareports.objectgroup.query.db;

import java.util.Map;

import org.openmrs.module.rwandareports.objectgroup.ObjectGroup;

public interface ObjectGroupQueryServiceDAO {

	
	 public ObjectGroup executeSqlQuery(String sqlQuery, Map<String,Object> paramMap);
	 
	 
}
