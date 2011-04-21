package org.openmrs.module.rwandareports.encounter.persister;

import java.util.List;

import org.openmrs.api.APIException;
import org.openmrs.module.rwandareports.encounter.definition.EncounterGroupDefinition;

public interface EncounterGroupDefinitionPersister {


	/**
	 * @param id
	 * @return the encounterGroup definition with the given id among those managed by this persister
	 */
	public EncounterGroupDefinition getEncounterGroupDefinition(Integer id);
	
	/**
	 * @param uuid
	 * @return the encounterGroup definition with the given uuid among those managed by this persister
	 */
	public EncounterGroupDefinition getEncounterGroupDefinitionByUuid(String uuid);
	
	/**
	 * @param includeRetired - if true, include retired EncounterGroupDefinitions in the returned list
	 * @return All encounterGroup definitions whose persistence is managed by this persister
	 */
	public List<EncounterGroupDefinition> getAllEncounterGroupDefinitions(boolean includeRetired);
	
	/**
	 * @param includeRetired indicates whether to also include retired EncounterGroupDefinitions in the count
	 * @return the number of saved encounterGroup Definitions
	 */
	public int getNumberOfEncounterGroupDefinitions(boolean includeRetired);
	
	/**
	 * Returns a List of {@link EncounterGroupDefinition} whose name contains the passed name.
	 * An empty list will be returned if there are none found. Search is case insensitive.
	 * @param name The search string
	 * @param exactMatchOnly if true will only return exact matches
	 * @throws APIException
	 * @return a List<EncounterGroupDefinition> objects whose name contains the passed name
	 */
	public List<EncounterGroupDefinition> getEncounterGroupDefinitions(String name, boolean exactMatchOnly) throws APIException;
	
	/**
	 * Persists a EncounterGroupDefinition, either as a save or update.
	 * @param encounterGroupDefinition
	 * @return the EncounterGroupDefinition that was passed in
	 */
	public EncounterGroupDefinition saveEncounterGroupDefinition(EncounterGroupDefinition encounterGroupDefinition);
	
	/**
	 * Deletes a encounterGroup definition from the database.
	 * @param encounterGroupDefinition
	 */
	public void purgeEncounterGroupDefinition(EncounterGroupDefinition encounterGroupDefinition);
	
	
}
