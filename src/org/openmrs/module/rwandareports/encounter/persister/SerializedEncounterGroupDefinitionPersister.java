package org.openmrs.module.rwandareports.encounter.persister;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.rwandareports.encounter.EncounterGroup;
import org.openmrs.module.rwandareports.encounter.definition.EncounterGroupDefinition;
import org.openmrs.module.rwandareports.serializer.RwandaReportsSerializedDefinitionService;




/**
 * This class provides access to persisted {@link EncounterGroup}s, 
 * and exposes them as a {@link EncounterGroupDefinition}
 */
@Handler(supports={EncounterGroupDefinition.class},order=100)
public class SerializedEncounterGroupDefinitionPersister implements EncounterGroupDefinitionPersister{
	
protected static Log log = LogFactory.getLog(SerializedEncounterGroupDefinitionPersister.class);
	
    //****************
    // Constructor
    //****************
	protected SerializedEncounterGroupDefinitionPersister() { }
	
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
     * @see EncounterGroupDefinitionPersister#getEncounterGroupDefinition(Integer)
     */
    public EncounterGroupDefinition getEncounterGroupDefinition(Integer id) {
    	return getService().getDefinition(EncounterGroupDefinition.class, id);
    }
    
	/**
     * @see EncounterGroupDefinitionPersister#getEncounterGroupDefinitionByUuid(String)
     */
    public EncounterGroupDefinition getEncounterGroupDefinitionByUuid(String uuid) {
     	return getService().getDefinitionByUuid(EncounterGroupDefinition.class, uuid);
    }

	/**
     * @see EncounterGroupDefinitionPersister#getAllEncounterGroupDefinitions(boolean)
     */
    public List<EncounterGroupDefinition> getAllEncounterGroupDefinitions(boolean includeRetired) {
     	return getService().getAllDefinitions(EncounterGroupDefinition.class, includeRetired);
    }
    
	/**
	 * @see EncounterGroupDefinitionPersister#getNumberOfEncounterGroupDefinitions(boolean)
	 */
	public int getNumberOfEncounterGroupDefinitions(boolean includeRetired) {
    	return getService().getNumberOfDefinitions(EncounterGroupDefinition.class, includeRetired);
	}

	/**
     * @see EncounterGroupDefinitionPersister#getEncounterGroupDefinitionByName(String, boolean)
     */
    public List<EncounterGroupDefinition> getEncounterGroupDefinitions(String name, boolean exactMatchOnly) {
    	return getService().getDefinitions(EncounterGroupDefinition.class, name, exactMatchOnly);
    }
    
	/**
     * @see EncounterGroupDefinitionPersister#saveEncounterGroupDefinition(EncounterGroupDefinition)
     */
    public EncounterGroupDefinition saveEncounterGroupDefinition(EncounterGroupDefinition encounterGroupDefinition) {
     	return getService().saveDefinition(encounterGroupDefinition);
    }

	/**
     * @see EncounterGroupDefinitionPersister#purgeEncounterGroupDefinition(EncounterGroupDefinition)
     */
    public void purgeEncounterGroupDefinition(EncounterGroupDefinition encounterGroupDefinition) {
    	getService().purgeDefinition(encounterGroupDefinition);
    }
	
	
}
