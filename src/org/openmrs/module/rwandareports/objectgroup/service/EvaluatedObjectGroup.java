package org.openmrs.module.rwandareports.objectgroup.service;

import java.util.HashSet;

import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rwandareports.objectgroup.ObjectGroup;
import org.openmrs.module.rwandareports.objectgroup.definition.ObjectGroupDefinition;

public class EvaluatedObjectGroup extends ObjectGroup implements Evaluated<ObjectGroupDefinition> {

	
private static final long serialVersionUID = 1L;
	
	//***********************
	// PROPERTIES
	//***********************
	
	private ObjectGroupDefinition definition;
	private EvaluationContext context;
	
	//***********************
	// CONSTRUCTORS
	//***********************
	
	/**
	 * Default Constructor
	 */
	public EvaluatedObjectGroup() {
		super();
	}
	
	/**
	 * Full Constructor
	 */
	public EvaluatedObjectGroup(ObjectGroup c, ObjectGroupDefinition definition, EvaluationContext context) {
		super(c == null ? new HashSet<Integer[]>() : c.getMemberIds());
		this.definition = definition;
		this.context = context;
	}
	
	//***********************
	// PROPERTY ACCESS
	//***********************

	/**
	 * @return the definition
	 */
	public ObjectGroupDefinition getDefinition() {
		return definition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(ObjectGroupDefinition definition) {
		this.definition = definition;
	}

	/**
	 * @return the context
	 */
	public EvaluationContext getContext() {
		return context;
	}

	/**
	 * @param context the context to set
	 */
	public void setContext(EvaluationContext context) {
		this.context = context;
	}
}
