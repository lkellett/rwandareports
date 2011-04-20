package org.openmrs.module.rwandareports.encounter.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.openmrs.module.reporting.IllegalDatabaseAccessException;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.ReflectionUtil;
import org.openmrs.module.reporting.definition.DefinitionUtil;
import org.openmrs.module.reporting.definition.service.BaseDefinitionService;
import org.openmrs.module.reporting.definition.service.DefinitionService;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.caching.Caching;
import org.openmrs.module.reporting.evaluation.caching.CachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.NoCachingStrategy;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.rwandareports.encounter.EncounterGroup;
import org.openmrs.module.rwandareports.encounter.definition.EncounterGroupDefinition;
import org.openmrs.module.rwandareports.encounter.persister.EncounterGroupDefinitionPersister;
import org.openmrs.module.rwandareports.encounter.query.EncounterGroupDefinitionEvaluator;
import org.openmrs.util.HandlerUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Transactional
@Service
public class EncounterGroupDefinitionServiceImpl extends BaseDefinitionService<EncounterGroupDefinition> implements EncounterGroupDefinitionService {
	
	private static Log log = LogFactory.getLog(EncounterGroupDefinitionServiceImpl.class);
	
	/**
	 * @see DefinitionService#getDefinitionType()
	 */
	public Class<EncounterGroupDefinition> getDefinitionType() {
		return EncounterGroupDefinition.class;
	}

	/**
	 * @see DefinitionService#getDefinitionTypes()z
	 */
	@SuppressWarnings("unchecked")
	public List<Class<? extends EncounterGroupDefinition>> getDefinitionTypes() {
		List<Class<? extends EncounterGroupDefinition>> ret = new ArrayList<Class<? extends EncounterGroupDefinition>>();
		for (EncounterGroupDefinitionEvaluator e : HandlerUtil.getHandlersForType(EncounterGroupDefinitionEvaluator.class, null)) {
			Handler handlerAnnotation = e.getClass().getAnnotation(Handler.class);
			if (handlerAnnotation != null) {
				Class<?>[] types = handlerAnnotation.supports();
				if (types != null) {
					for (Class<?> type : types) {
						ret.add((Class<? extends EncounterGroupDefinition>) type);
					}
				}
			}
		}
		return ret;
	}
	
	/**
	 * @see DefinitionService#getDefinition(Class, Integer)
	 */
	@SuppressWarnings("unchecked")
	public <D extends EncounterGroupDefinition> D getDefinition(Class<D> type, Integer id) throws APIException {
		return (D) getPersister(type).getEncounterGroupDefinition(id);
	}
	
	/**
	 * @see DefinitionService#getDefinitionByUuid(String)
	 */
	public EncounterGroupDefinition getDefinitionByUuid(String uuid) throws APIException {
		for (EncounterGroupDefinitionPersister p : getAllPersisters()) {
			EncounterGroupDefinition cd = p.getEncounterGroupDefinitionByUuid(uuid);
			if (cd != null) {
				return cd;
			}
		}
		return null;
	}
	
	/**
	 * @see DefinitionService#getAllDefinitions(boolean)
	 */
	public List<EncounterGroupDefinition> getAllDefinitions(boolean includeRetired) {
		List<EncounterGroupDefinition> ret = new ArrayList<EncounterGroupDefinition>();
		for (EncounterGroupDefinitionPersister p : getAllPersisters()) {
			ret.addAll(p.getAllEncounterGroupDefinitions(includeRetired));
		}
		return ret;
	}
	
	/**
	 * @see DefinitionService#getNumberOfDefinitions(boolean)
	 */
	public int getNumberOfDefinitions(boolean includeRetired) {
		int i = 0;
		for (EncounterGroupDefinitionPersister p : getAllPersisters()) {
			i += p.getNumberOfEncounterGroupDefinitions(includeRetired);
		}
		return i;
	}

	/**
	 * @see DefinitionService#getDefinitions(String, boolean)
	 */
	public List<EncounterGroupDefinition> getDefinitions(String name, boolean exactMatchOnly) {
		List<EncounterGroupDefinition> ret = new ArrayList<EncounterGroupDefinition>();
		for (EncounterGroupDefinitionPersister p : getAllPersisters()) {
			ret.addAll(p.getEncounterGroupDefinitions(name, exactMatchOnly));
		}
		return ret;
	}

	/**
	 * @see DefinitionService#saveDefinition(Definition)
	 */
	@Transactional
	@SuppressWarnings("unchecked")
	public <D extends EncounterGroupDefinition> D saveDefinition(D definition) throws APIException {
		
		//We would like to validate definitions before saving them, but currently the UI workflow 
		//sometimes saves definitions with just a name and description before displaying them for editing.
		//ValidateUtil.validate(definition);
		
		log.debug("Saving cohort definition: " + definition + " of type " + definition.getClass());
		return (D) getPersister(definition.getClass()).saveEncounterGroupDefinition(definition);
	}
	
	/**
	 * @see DefinitionService#purgeDefinition(Definition)
	 */
	public void purgeDefinition(EncounterGroupDefinition definition) {
		getPersister(definition.getClass()).purgeEncounterGroupDefinition(definition);
	}

	/**
	 * 	This is the main method which should be used to evaluate a CohortDefinition
	 *  - retrieves all evaluation parameter values from the class and the EvaluationContext
	 *  - checks whether a cohort with this configuration exists in the cache (if caching is supported)
	 *  - returns the cached cohort if found
	 *  - otherwise, delegates to the appropriate CohortDefinitionEvaluator and evaluates the result
	 *  - caches the result (if caching is supported)
	 * 
	 * Implementing classes should override the evaluateCohort(EvaluationContext) method
	 * @see getCacheKey(EvaluationContext)
     * @see CohortDefinitionEvaluator#evaluate(EvaluationContext)
	 * @see DefinitionService#evaluate(Definition, EvaluationContext)
	 */
	public EvaluatedEncounterGroup evaluate(EncounterGroupDefinition definition, EvaluationContext context) throws EvaluationException {
		
		// Retrieve CohortDefinitionEvaluator which can evaluate this CohortDefinition
		EncounterGroupDefinitionEvaluator evaluator = HandlerUtil.getPreferredHandler(EncounterGroupDefinitionEvaluator.class, definition.getClass());
		if (evaluator == null) {
			throw new APIException("No CohortDefinitionEvaluator found for (" + definition.getClass() + ") " + definition.getName());
		}

		// Clone CohortDefinition and set all properties from the Parameters in the EvaluationContext
		EncounterGroupDefinition clonedDefinition = DefinitionUtil.clone(definition);
		for (Parameter p : clonedDefinition.getParameters()) {
			Object value = p.getDefaultValue();
			if (context != null && context.containsParameter(p.getName())) {
				value = context.getParameterValue(p.getName());
			}
			ReflectionUtil.setPropertyValue(clonedDefinition, p.getName(), value);
		}
		
		// Retrieve from cache if possible, otherwise evaluate
		EncounterGroup c = null;
		if (context != null) {
			Caching caching = clonedDefinition.getClass().getAnnotation(Caching.class);
			if (caching != null && caching.strategy() != NoCachingStrategy.class) {
				try {
					CachingStrategy strategy = caching.strategy().newInstance();
					String cacheKey = strategy.getCacheKey(clonedDefinition);
					if (cacheKey != null) {
						c = (EncounterGroup) context.getFromCache(cacheKey);
					}
					if (c == null) {
						c = evaluator.evaluate(clonedDefinition, context);
						context.addToCache(cacheKey, c);
					}
				}
				catch (IllegalDatabaseAccessException ie) {
					throw ie;
				}
				catch (Exception e) {
					log.warn("An error occurred while attempting to access the cache.", e);
				}
			}
		}
		if (c == null) {
			c = evaluator.evaluate(clonedDefinition, context);
		}
		if (context != null && context.getBaseCohort() != null && c != null) {
			c = EncounterGroup.intersect(c, context.getBaseCohort());
		}
		
		return new EvaluatedEncounterGroup(c, clonedDefinition, context);
	}

	/**
	 * @see BaseDefinitionService#evaluate(Mapped, EvaluationContext)
	 */
	@Override
	public EvaluatedEncounterGroup evaluate(Mapped<? extends EncounterGroupDefinition> definition, EvaluationContext context) throws EvaluationException {
		return (EvaluatedEncounterGroup) super.evaluate(definition, context);
	}

	/**
	 * Returns the CohortDefinitionPersister for the passed CohortDefinition
	 * @param definition
	 * @return the CohortDefinitionPersister for the passed CohortDefinition
	 * @throws APIException if no matching persister is found
	 */
	protected EncounterGroupDefinitionPersister getPersister(Class<? extends EncounterGroupDefinition> definition) {
		EncounterGroupDefinitionPersister persister = HandlerUtil.getPreferredHandler(EncounterGroupDefinitionPersister.class, definition);
		if (persister == null) {
			throw new APIException("No CohortDefinitionPersister found for <" + definition + ">");
		}
		return persister;
	}
	
	/**
	 * @return all CohortDefinitionPersisters
	 */
	protected List<EncounterGroupDefinitionPersister> getAllPersisters() {	
		return HandlerUtil.getHandlersForType(EncounterGroupDefinitionPersister.class, null);
	}
		
}
