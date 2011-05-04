package org.openmrs.module.rwandareports.objectgroup.query.service;

import java.util.Map;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.rwandareports.objectgroup.ObjectGroup;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true)
public interface ObjectGroupQueryService extends OpenmrsService {

	
	public ObjectGroup executeSqlQuery(String sqlQuery, Map<String,Object> paramMap);
	
	
}
