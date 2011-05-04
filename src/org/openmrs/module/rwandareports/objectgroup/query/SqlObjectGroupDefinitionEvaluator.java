package org.openmrs.module.rwandareports.objectgroup.query;

import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rwandareports.objectgroup.ObjectGroup;
import org.openmrs.module.rwandareports.objectgroup.definition.ObjectGroupDefinition;
import org.openmrs.module.rwandareports.objectgroup.definition.SqlObjectGroupDefinition;
import org.openmrs.module.rwandareports.objectgroup.query.service.ObjectGroupQueryService;




@Handler(supports={SqlObjectGroupDefinition.class})
public class SqlObjectGroupDefinitionEvaluator implements ObjectGroupDefinitionEvaluator {


	/**
	 * Default Constructor
	 */
	public SqlObjectGroupDefinitionEvaluator() {}
	

    public ObjectGroup evaluate(ObjectGroupDefinition objectGroupDefinition, EvaluationContext context) {
    	SqlObjectGroupDefinition sqlObjectGroupDefinition = (SqlObjectGroupDefinition) objectGroupDefinition;
    	
    	
    	ObjectGroupQueryService egs = Context.getService(ObjectGroupQueryService.class);
    	ObjectGroup c = egs.executeSqlQuery(sqlObjectGroupDefinition.getQuery(), context.getParameterValues());
    	if (context.getBaseCohort() != null) {
    		c = ObjectGroup.intersect(c, context.getBaseCohort());
    	}
    	return c;
    }
	
}
