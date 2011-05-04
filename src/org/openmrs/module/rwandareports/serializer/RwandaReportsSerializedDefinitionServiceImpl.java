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
package org.openmrs.module.rwandareports.serializer;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.OpenmrsObject;
import org.openmrs.api.db.SerializedObject;
import org.openmrs.api.db.SerializedObjectDAO;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.reporting.definition.DefinitionSummary;
import org.openmrs.module.reporting.definition.service.SerializedDefinitionService;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.rwandareports.objectgroup.persister.ObjectGroupDefinitionPersister;
import org.openmrs.serialization.OpenmrsSerializer;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 */
@Transactional
public class RwandaReportsSerializedDefinitionServiceImpl extends BaseOpenmrsService implements RwandaReportsSerializedDefinitionService {
	
	protected static Log log = LogFactory.getLog(RwandaReportsSerializedDefinitionServiceImpl.class);
	
    //****************
    // Constructor
    //****************
	private RwandaReportsSerializedDefinitionServiceImpl() { }
	
    //****************
    // Properties
    //****************
	
	private SerializedObjectDAO dao = null;
	private OpenmrsSerializer serializer = null;
	
    /**
     * @return the dao
     */
    public SerializedObjectDAO getDao() {
    	return dao;
    }

    /**
     * @param dao the dao to set
     */
    public void setDao(SerializedObjectDAO dao) {
    	this.dao = dao;
    }
    
    /**
     * @return the serializer
     */
    public OpenmrsSerializer getSerializer() {
    	return serializer;
    }
	
    /**
     * @param serializer the serializer to set
     */
    public void setSerializer(OpenmrsSerializer serializer) {
    	this.serializer = serializer;
    }
	
	
    //****************
    // Instance methods
    //****************
    
	/** 
	 * @see SerializedDefinitionService#getSupportedDefinitionTypes()
	 */
    @SuppressWarnings("unchecked")
	public List<Class<Definition>> getSupportedDefinitionTypes() {
		List<Class<Definition>> d = new ArrayList<Class<Definition>>();
		for (Class<? extends OpenmrsObject> c : dao.getSupportedTypes()) {
			if (Definition.class.isAssignableFrom(c)) {
				d.add((Class<Definition>)c);
			}
		}
		return d;
	}

	/**
     * @see SerializedDefinitionService#getDefinition(Class, Integer)
     */
    public <T extends Definition> T getDefinition(Class<T> definitionType, Integer id) {
    	SerializedObject so = dao.getSerializedObject(id);
    	try {
    		return dao.convertSerializedObject(definitionType, so);
    	}
    	catch (Exception e) {
    		log.warn("Unable to deserialize Definition: " + so, e);
    		return null;
    	}
    }

	/**
     * @see SerializedDefinitionService#getDefinitionByUuid(Class, String)
     */
    public <T extends Definition> T getDefinitionByUuid(Class<T> definitionType, String uuid) {
    	SerializedObject so = dao.getSerializedObjectByUuid(uuid);
    	try {
    		return dao.convertSerializedObject(definitionType, so);
    	}
    	catch (Exception e) {
    		log.warn("Unable to deserialize Definition : " + so, e);
    		return null;
    	}
    }

    /**
     * @see SerializedDefinitionService#getAllDefinitions(Class, boolean)
     */
    public <T extends Definition> List<T> getAllDefinitions(Class<T> definitionType, boolean includeRetired) {
    	List<T> ret = new ArrayList<T>();
    	for (SerializedObject so : dao.getAllSerializedObjects(definitionType, includeRetired)) {
        	try {
        		ret.add(dao.convertSerializedObject(definitionType, so));
        	}
        	catch (Exception e) {
        		log.warn("Unable to deserialize Definition: " + so, e);
        	}
    	}
    	return ret;
    }
    
    /**
     * @see SerializedDefinitionService#getAllDefinitionSummaries(Class, boolean)
     */
    public <T extends Definition> List<DefinitionSummary> getAllDefinitionSummaries(Class<T> definitionType,
                                                                                    boolean includeRetired) {
    	List<DefinitionSummary> ret = new ArrayList<DefinitionSummary>();
    	for (SerializedObject so : dao.getAllSerializedObjects(definitionType, includeRetired)) {
    		ret.add(new DefinitionSummary(so));
    	}
    	return ret;
    }
    
    /**
     * @see SerializedDefinitionService#getInvalidDefinitions(boolean)
     */
    public List<SerializedObject> getInvalidDefinitions(boolean includeRetired) {
    	List<SerializedObject> ret = new ArrayList<SerializedObject>();
    	for (Class<Definition> clazz : getSupportedDefinitionTypes()) {
    		ret.addAll(getInvalidDefinitions(clazz, includeRetired));
    	}
    	return ret;
    }
    
    /**
     * @see SerializedDefinitionService#getInvalidDefinitions(Class, boolean)
     */
    public <T extends Definition> List<SerializedObject> getInvalidDefinitions(Class<T> definitionType, boolean includeRetired) {
    	List<SerializedObject> ret = new ArrayList<SerializedObject>();
    	for (SerializedObject so : dao.getAllSerializedObjects(definitionType, includeRetired)) {
        	try {
        		dao.convertSerializedObject(definitionType, so);
        	}
        	catch (Exception e) {
        		ret.add(so);
        	}
    	}
    	return ret;
    }
    
    /**
     * @see SerializedDefinitionService#getNumberOfDefinitions(Class, boolean)
     */
	public <T extends Definition> int getNumberOfDefinitions(Class<T> definitionType, boolean includeRetired) {
		return dao.getAllSerializedObjects(definitionType, includeRetired).size();
	}

	/**
     * @see ObjectGroupDefinitionPersister#getObjectGroupDefinitionByName(String, boolean)
     */
	public <T extends Definition> List<T> getDefinitions(Class<T> definitionType, String name, boolean exactMatchOnly) {
    	List<T> ret = new ArrayList<T>();
    	for (SerializedObject so : dao.getAllSerializedObjectsByName(definitionType, name, exactMatchOnly)) {
        	try {
        		ret.add(dao.convertSerializedObject(definitionType, so));
        	}
        	catch (Exception e) {
        		log.warn("Unable to deserialize Definition: " + so, e);
        	}
    	}
    	return ret;
    }

	/**
	 * @see SerializedDefinitionService#saveDefinition(Definition)
	 */
    public <T extends Definition> T saveDefinition(T definition) {
    	return dao.saveObject(definition, serializer);
    }

    /**
     * @see SerializedDefinitionService#purgeDefinition(Definition)
     */
    public <T extends Definition> void purgeDefinition(T definition) {
    	dao.purgeObject(definition.getId());
    }
    
	/** 
	 * @see SerializedDefinitionService#purgeDefinition(String)
	 */
	public void purgeDefinition(String uuid) {
		SerializedObject obj = dao.getSerializedObjectByUuid(uuid);
		dao.purgeObject(obj.getId());
	}

	/** 
	 * @see SerializedDefinitionService#getSerializedDefinitionByUuid(String)
	 */
	public SerializedObject getSerializedDefinitionByUuid(String uuid) {
		return dao.getSerializedObjectByUuid(uuid);
	}

	/** 
	 * @see SerializedDefinitionService#saveSerializedDefinition(SerializedObject)
	 */
	public void saveSerializedDefinition(SerializedObject serializedDefinition) {
		Definition d = dao.convertSerializedObject(Definition.class, serializedDefinition);
		dao.saveObject(d);
	}
}
