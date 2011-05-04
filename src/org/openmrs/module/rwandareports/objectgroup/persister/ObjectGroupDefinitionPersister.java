package org.openmrs.module.rwandareports.objectgroup.persister;

import java.util.List;

import org.openmrs.api.APIException;
import org.openmrs.module.rwandareports.objectgroup.definition.ObjectGroupDefinition;

public interface ObjectGroupDefinitionPersister {


	/**
	 * @param id
	 * @return the objectGroup definition with the given id among those managed by this persister
	 */
	public ObjectGroupDefinition getObjectGroupDefinition(Integer id);
	
	/**
	 * @param uuid
	 * @return the objectGroup definition with the given uuid among those managed by this persister
	 */
	public ObjectGroupDefinition getObjectGroupDefinitionByUuid(String uuid);
	
	/**
	 * @param includeRetired - if true, include retired ObjectGroupDefinitions in the returned list
	 * @return All objectGroup definitions whose persistence is managed by this persister
	 */
	public List<ObjectGroupDefinition> getAllObjectGroupDefinitions(boolean includeRetired);
	
	/**
	 * @param includeRetired indicates whether to also include retired ObjectGroupDefinitions in the count
	 * @return the number of saved objectGroup Definitions
	 */
	public int getNumberOfObjectGroupDefinitions(boolean includeRetired);
	
	/**
	 * Returns a List of {@link ObjectGroupDefinition} whose name contains the passed name.
	 * An empty list will be returned if there are none found. Search is case insensitive.
	 * @param name The search string
	 * @param exactMatchOnly if true will only return exact matches
	 * @throws APIException
	 * @return a List<ObjectGroupDefinition> objects whose name contains the passed name
	 */
	public List<ObjectGroupDefinition> getObjectGroupDefinitions(String name, boolean exactMatchOnly) throws APIException;
	
	/**
	 * Persists a ObjectGroupDefinition, either as a save or update.
	 * @param objectGroupDefinition
	 * @return the ObjectGroupDefinition that was passed in
	 */
	public ObjectGroupDefinition saveObjectGroupDefinition(ObjectGroupDefinition objectGroupDefinition);
	
	/**
	 * Deletes a objectGroup definition from the database.
	 * @param objectGroupDefinition
	 */
	public void purgeObjectGroupDefinition(ObjectGroupDefinition objectGroupDefinition);
	
	
}
