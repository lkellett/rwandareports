package org.openmrs.module.rwandareports.encounter.service;

import org.openmrs.api.APIException;
import org.openmrs.module.reporting.definition.service.DefinitionService;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.rwandareports.encounter.definition.EncounterGroupDefinition;
import org.springframework.transaction.annotation.Transactional;


@Transactional
public interface EncounterGroupDefinitionService extends DefinitionService<EncounterGroupDefinition> {

	
	/**
	 * @see DefinitionService#evaluate(Definition, EvaluationContext)
	 */
	@Transactional(readOnly = true)
	public EvaluatedEncounterGroup evaluate(EncounterGroupDefinition definition, EvaluationContext context) throws EvaluationException;
	
	/**
	 * @see DefinitionService#evaluate(Mapped<Definition>, EvaluationContext)
	 */
	@Transactional(readOnly = true)
	public EvaluatedEncounterGroup evaluate(Mapped<? extends EncounterGroupDefinition> definition, EvaluationContext context) throws EvaluationException;
	
	
	public EncounterGroupDefinition getDefinitionByUuid(String uuid) throws APIException; 
	
}
