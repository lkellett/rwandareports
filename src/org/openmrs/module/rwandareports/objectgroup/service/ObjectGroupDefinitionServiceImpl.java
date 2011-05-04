package org.openmrs.module.rwandareports.objectgroup.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.api.APIException;
import org.openmrs.module.reporting.IllegalDatabaseAccessException;
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
import org.openmrs.module.rwandareports.objectgroup.ObjectGroup;
import org.openmrs.module.rwandareports.objectgroup.definition.ObjectGroupDefinition;
import org.openmrs.module.rwandareports.objectgroup.persister.ObjectGroupDefinitionPersister;
import org.openmrs.module.rwandareports.objectgroup.query.ObjectGroupDefinitionEvaluator;
import org.openmrs.util.HandlerUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Transactional
@Service
public class ObjectGroupDefinitionServiceImpl extends BaseDefinitionService<ObjectGroupDefinition> implements ObjectGroupDefinitionService {
	
	private static Log log = LogFactory.getLog(ObjectGroupDefinitionServiceImpl.class);
	
	/**
	 * @see DefinitionService#getDefinitionType()
	 */
	public Class<ObjectGroupDefinition> getDefinitionType() {
		return ObjectGroupDefinition.class;
	}

	/**
	 * @see DefinitionService#getDefinitionTypes()z
	 */
	@SuppressWarnings("unchecked")
	public List<Class<? extends ObjectGroupDefinition>> getDefinitionTypes() {
		List<Class<? extends ObjectGroupDefinition>> ret = new ArrayList<Class<? extends ObjectGroupDefinition>>();
		for (ObjectGroupDefinitionEvaluator e : HandlerUtil.getHandlersForType(ObjectGroupDefinitionEvaluator.class, null)) {
			Handler handlerAnnotation = e.getClass().getAnnotation(Handler.class);
			if (handlerAnnotation != null) {
				Class<?>[] types = handlerAnnotation.supports();
				if (types != null) {
					for (Class<?> type : types) {
						ret.add((Class<? extends ObjectGroupDefinition>) type);
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
	public <D extends ObjectGroupDefinition> D getDefinition(Class<D> type, Integer id) throws APIException {
		return (D) getPersister(type).getObjectGroupDefinition(id);
	}
	
	/**
	 * @see DefinitionService#getDefinitionByUuid(String)
	 */
	public ObjectGroupDefinition getDefinitionByUuid(String uuid) throws APIException {
		for (ObjectGroupDefinitionPersister p : getAllPersisters()) {
			ObjectGroupDefinition cd = p.getObjectGroupDefinitionByUuid(uuid);
			if (cd != null) {
				return cd;
			}
		}
		return null;
	}
	
	/**
	 * @see DefinitionService#getAllDefinitions(boolean)
	 */
	public List<ObjectGroupDefinition> getAllDefinitions(boolean includeRetired) {
		List<ObjectGroupDefinition> ret = new ArrayList<ObjectGroupDefinition>();
		for (ObjectGroupDefinitionPersister p : getAllPersisters()) {
			ret.addAll(p.getAllObjectGroupDefinitions(includeRetired));
		}
		return ret;
	}
	
	/**
	 * @see DefinitionService#getNumberOfDefinitions(boolean)
	 */
	public int getNumberOfDefinitions(boolean includeRetired) {
		int i = 0;
		for (ObjectGroupDefinitionPersister p : getAllPersisters()) {
			i += p.getNumberOfObjectGroupDefinitions(includeRetired);
		}
		return i;
	}

	/**
	 * @see DefinitionService#getDefinitions(String, boolean)
	 */
	public List<ObjectGroupDefinition> getDefinitions(String name, boolean exactMatchOnly) {
		List<ObjectGroupDefinition> ret = new ArrayList<ObjectGroupDefinition>();
		for (ObjectGroupDefinitionPersister p : getAllPersisters()) {
			ret.addAll(p.getObjectGroupDefinitions(name, exactMatchOnly));
		}
		return ret;
	}

	/**
	 * @see DefinitionService#saveDefinition(Definition)
	 */
	@Transactional
	@SuppressWarnings("unchecked")
	public <D extends ObjectGroupDefinition> D saveDefinition(D definition) throws APIException {
		
		//We would like to validate definitions before saving them, but currently the UI workflow 
		//sometimes saves definitions with just a name and description before displaying them for editing.
		//ValidateUtil.validate(definition);
		
		log.debug("Saving definition: " + definition + " of type " + definition.getClass());
		return (D) getPersister(definition.getClass()).saveObjectGroupDefinition(definition);
	}
	
	/**
	 * @see DefinitionService#purgeDefinition(Definition)
	 */
	public void purgeDefinition(ObjectGroupDefinition definition) {
		getPersister(definition.getClass()).purgeObjectGroupDefinition(definition);
	}

	/**
	 * 	This is the main method which should be used to evaluate an ObjectGroupDefinition
	 *  - retrieves all evaluation parameter values from the class and the EvaluationContext
	 *  - checks whether an EncoungerGroup  with this configuration exists in the cache (if caching is supported)
	 *  - returns the cached ObjectGroup if found
	 *  - otherwise, delegates to the appropriate DefinitionEvaluator and evaluates the result
	 *  - caches the result (if caching is supported)
	 * 
	 */
	public EvaluatedObjectGroup evaluate(ObjectGroupDefinition definition, EvaluationContext context) throws EvaluationException {
		

		ObjectGroupDefinitionEvaluator evaluator = HandlerUtil.getPreferredHandler(ObjectGroupDefinitionEvaluator.class, definition.getClass());
		if (evaluator == null) {
			throw new APIException("No ObjectGroupDefinitionEvaluator found for (" + definition.getClass() + ") " + definition.getName());
		}
		
		
		ObjectGroupDefinition clonedDefinition = DefinitionUtil.clone(definition);
		for (Parameter p : clonedDefinition.getParameters()) {
			Object value = p.getDefaultValue();
			if (context != null && context.containsParameter(p.getName())) {
				value = context.getParameterValue(p.getName());
			}
			ReflectionUtil.setPropertyValue(clonedDefinition, p.getName(), value);
		}
		
		// Retrieve from cache if possible, otherwise evaluate
		ObjectGroup c = null;
		if (context != null) {
			Caching caching = clonedDefinition.getClass().getAnnotation(Caching.class);
			if (caching != null && caching.strategy() != NoCachingStrategy.class) {
				try {
					CachingStrategy strategy = caching.strategy().newInstance();
					String cacheKey = strategy.getCacheKey(clonedDefinition);
					if (cacheKey != null) {
						c = (ObjectGroup) context.getFromCache(cacheKey);
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
			c = ObjectGroup.intersect(c, context.getBaseCohort());
		}
		
		return new EvaluatedObjectGroup(c, clonedDefinition, context);
	}

	/**
	 * @see BaseDefinitionService#evaluate(Mapped, EvaluationContext)
	 */
	@Override
	public EvaluatedObjectGroup evaluate(Mapped<? extends ObjectGroupDefinition> definition, EvaluationContext context) throws EvaluationException {
		return (EvaluatedObjectGroup) super.evaluate(definition, context);
	}

	/**
	 * Returns the ObjectGroupDefinitionPersister for the passed Definition
	 * @param definition
	 * @return the ObjectGroupDefinitionPersister for the passed ObjectGroupDefinition
	 * @throws APIException if no matching persister is found
	 */
	protected ObjectGroupDefinitionPersister getPersister(Class<? extends ObjectGroupDefinition> definition) {
		ObjectGroupDefinitionPersister persister = HandlerUtil.getPreferredHandler(ObjectGroupDefinitionPersister.class, definition);
		if (persister == null) {
			throw new APIException("No DefinitionPersister found for <" + definition + ">");
		}
		return persister;
	}
	
	/**
	 * @return all ObjectGroupDefinitionPersister
	 */
	protected List<ObjectGroupDefinitionPersister> getAllPersisters() {	
		return HandlerUtil.getHandlersForType(ObjectGroupDefinitionPersister.class, null);
	}
		
}
