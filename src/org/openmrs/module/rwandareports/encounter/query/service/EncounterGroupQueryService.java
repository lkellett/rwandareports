package org.openmrs.module.rwandareports.encounter.query.service;

import java.util.Map;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.rwandareports.encounter.EncounterGroup;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly=true)
public interface EncounterGroupQueryService extends OpenmrsService {

	
	public EncounterGroup executeSqlQuery(String sqlQuery, Map<String,Object> paramMap);
	
	
}
