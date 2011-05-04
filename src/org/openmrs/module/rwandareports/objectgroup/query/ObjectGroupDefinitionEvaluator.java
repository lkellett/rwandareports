package org.openmrs.module.rwandareports.objectgroup.query;

import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.rwandareports.objectgroup.ObjectGroup;
import org.openmrs.module.rwandareports.objectgroup.definition.ObjectGroupDefinition;

public interface ObjectGroupDefinitionEvaluator {

	
	public ObjectGroup evaluate(ObjectGroupDefinition objectGroupDefinition, EvaluationContext context) throws EvaluationException;
	
	
}
