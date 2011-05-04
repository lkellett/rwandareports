package org.openmrs.module.rwandareports.objectgroup.service;

import org.openmrs.api.APIException;
import org.openmrs.module.reporting.definition.service.DefinitionService;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.rwandareports.objectgroup.definition.ObjectGroupDefinition;
import org.springframework.transaction.annotation.Transactional;


@Transactional
public interface ObjectGroupDefinitionService extends DefinitionService<ObjectGroupDefinition> {

	
	/**
	 * @see DefinitionService#evaluate(Definition, EvaluationContext)
	 */
	@Transactional(readOnly = true)
	public EvaluatedObjectGroup evaluate(ObjectGroupDefinition definition, EvaluationContext context) throws EvaluationException;
	
	/**
	 * @see DefinitionService#evaluate(Mapped<Definition>, EvaluationContext)
	 */
	@Transactional(readOnly = true)
	public EvaluatedObjectGroup evaluate(Mapped<? extends ObjectGroupDefinition> definition, EvaluationContext context) throws EvaluationException;
	
	
	public ObjectGroupDefinition getDefinitionByUuid(String uuid) throws APIException; 
	
}
