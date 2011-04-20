package org.openmrs.module.rwandareports.encounter.query;

import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.rwandareports.encounter.EncounterGroup;
import org.openmrs.module.rwandareports.encounter.definition.EncounterGroupDefinition;

public interface EncounterGroupDefinitionEvaluator {

	
	public EncounterGroup evaluate(EncounterGroupDefinition encounterGroupDefinition, EvaluationContext context) throws EvaluationException;
	
	
}
