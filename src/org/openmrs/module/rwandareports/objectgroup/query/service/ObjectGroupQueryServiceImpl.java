package org.openmrs.module.rwandareports.objectgroup.query.service;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.rwandareports.objectgroup.ObjectGroup;
import org.openmrs.module.rwandareports.objectgroup.query.db.ObjectGroupQueryServiceDAO;
import org.springframework.transaction.annotation.Transactional;


@Transactional(readOnly=true)
public class ObjectGroupQueryServiceImpl extends BaseOpenmrsService implements ObjectGroupQueryService {
	
	protected final Log log = LogFactory.getLog(getClass());

    protected ObjectGroupQueryServiceDAO dao;
    
    public void setObjectGroupQueryDAO(ObjectGroupQueryServiceDAO dao) {
        this.dao = dao;
    }
    
    public ObjectGroup executeSqlQuery(String sqlQuery, Map<String,Object> paramMap) {
		return dao.executeSqlQuery(sqlQuery, paramMap);
	}

	public void setDao(ObjectGroupQueryServiceDAO dao) {
		this.dao = dao;
	}
    
}
