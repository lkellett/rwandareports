package org.openmrs.module.rwandareports.encounter.query.service;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.rwandareports.encounter.EncounterGroup;
import org.openmrs.module.rwandareports.encounter.query.db.EncounterGroupQueryServiceDAO;



public class EncounterGroupQueryServiceImpl extends BaseOpenmrsService implements EncounterGroupQueryService {
	
	protected final Log log = LogFactory.getLog(getClass());

    protected EncounterGroupQueryServiceDAO dao;
    
    public void setEncounterGroupQueryDAO(EncounterGroupQueryServiceDAO dao) {
        this.dao = dao;
    }
    
    public EncounterGroup executeSqlQuery(String sqlQuery, Map<String,Object> paramMap) {
		return dao.executeSqlQuery(sqlQuery, paramMap);
	}

	public void setDao(EncounterGroupQueryServiceDAO dao) {
		this.dao = dao;
	}
    
}
