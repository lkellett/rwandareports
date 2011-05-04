package org.openmrs.module.rwandareports.objectgroup.persister;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.rwandareports.objectgroup.ObjectGroup;
import org.openmrs.module.rwandareports.objectgroup.definition.ObjectGroupDefinition;
import org.openmrs.module.rwandareports.serializer.RwandaReportsSerializedDefinitionService;




/**
 * This class provides access to persisted {@link ObjectGroup}s, 
 * and exposes them as a {@link ObjectGroupDefinition}
 */
@Handler(supports={ObjectGroupDefinition.class},order=100)
public class SerializedObjectGroupDefinitionPersister implements ObjectGroupDefinitionPersister{
	
protected static Log log = LogFactory.getLog(SerializedObjectGroupDefinitionPersister.class);
	
    //****************
    // Constructor
    //****************
	protected SerializedObjectGroupDefinitionPersister() { }
	
    //****************
    // Instance methods
    //****************
	
	/**
	 * Utility method that returns the SerializedDefinitionService
	 */
	public RwandaReportsSerializedDefinitionService getService() {
		return Context.getService(RwandaReportsSerializedDefinitionService.class);
	}

	/**
     * @see ObjectGroupDefinitionPersister#getObjectGroupDefinition(Integer)
     */
    public ObjectGroupDefinition getObjectGroupDefinition(Integer id) {
    	return getService().getDefinition(ObjectGroupDefinition.class, id);
    }
    
	/**
     * @see ObjectGroupDefinitionPersister#getObjectGroupDefinitionByUuid(String)
     */
    public ObjectGroupDefinition getObjectGroupDefinitionByUuid(String uuid) {
     	return getService().getDefinitionByUuid(ObjectGroupDefinition.class, uuid);
    }

	/**
     * @see ObjectGroupDefinitionPersister#getAllObjectGroupDefinitions(boolean)
     */
    public List<ObjectGroupDefinition> getAllObjectGroupDefinitions(boolean includeRetired) {
     	return getService().getAllDefinitions(ObjectGroupDefinition.class, includeRetired);
    }
    
	/**
	 * @see ObjectGroupDefinitionPersister#getNumberOfObjectGroupDefinitions(boolean)
	 */
	public int getNumberOfObjectGroupDefinitions(boolean includeRetired) {
    	return getService().getNumberOfDefinitions(ObjectGroupDefinition.class, includeRetired);
	}

	/**
     * @see ObjectGroupDefinitionPersister#getObjectGroupDefinitionByName(String, boolean)
     */
    public List<ObjectGroupDefinition> getObjectGroupDefinitions(String name, boolean exactMatchOnly) {
    	return getService().getDefinitions(ObjectGroupDefinition.class, name, exactMatchOnly);
    }
    
	/**
     * @see ObjectGroupDefinitionPersister#saveObjectGroupDefinition(ObjectGroupDefinition)
     */
    public ObjectGroupDefinition saveObjectGroupDefinition(ObjectGroupDefinition objectGroupDefinition) {
     	return getService().saveDefinition(objectGroupDefinition);
    }

	/**
     * @see ObjectGroupDefinitionPersister#purgeObjectGroupDefinition(ObjectGroupDefinition)
     */
    public void purgeObjectGroupDefinition(ObjectGroupDefinition objectGroupDefinition) {
    	getService().purgeDefinition(objectGroupDefinition);
    }
	
	
}
