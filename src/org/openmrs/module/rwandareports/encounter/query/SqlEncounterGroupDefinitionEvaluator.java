package org.openmrs.module.rwandareports.encounter.query;

import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rwandareports.encounter.EncounterGroup;
import org.openmrs.module.rwandareports.encounter.definition.EncounterGroupDefinition;
import org.openmrs.module.rwandareports.encounter.definition.SqlEncounterGroupDefinition;
import org.openmrs.module.rwandareports.encounter.query.service.EncounterGroupQueryService;




@Handler(supports={SqlEncounterGroupDefinition.class})
public class SqlEncounterGroupDefinitionEvaluator implements EncounterGroupDefinitionEvaluator {


	/**
	 * Default Constructor
	 */
	public SqlEncounterGroupDefinitionEvaluator() {}
	

    public EncounterGroup evaluate(EncounterGroupDefinition encounterGroupDefinition, EvaluationContext context) {
    	SqlEncounterGroupDefinition sqlEncounterGroupDefinition = (SqlEncounterGroupDefinition) encounterGroupDefinition;
    	
    	
    	EncounterGroupQueryService egs = Context.getService(EncounterGroupQueryService.class);
    	EncounterGroup c = egs.executeSqlQuery(sqlEncounterGroupDefinition.getQuery(), context.getParameterValues());
    	System.out.println("HERE SqlEncounterGroupDefinitionEvaluator before intersect with base cohort: " + c.size());
    	if (context.getBaseCohort() != null) {
    		c = EncounterGroup.intersect(c, context.getBaseCohort());
    	}
    	System.out.println("HERE SqlEncounterGroupDefinitionEvaluator after instersect with base cohort: " + c.size());
    	return c;
    }
	
}
