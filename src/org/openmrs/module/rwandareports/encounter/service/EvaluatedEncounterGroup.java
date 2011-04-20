package org.openmrs.module.rwandareports.encounter.service;

import java.util.HashSet;

import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rwandareports.encounter.EncounterGroup;
import org.openmrs.module.rwandareports.encounter.definition.EncounterGroupDefinition;

public class EvaluatedEncounterGroup extends EncounterGroup implements Evaluated<EncounterGroupDefinition> {

	
private static final long serialVersionUID = 1L;
	
	//***********************
	// PROPERTIES
	//***********************
	
	private EncounterGroupDefinition definition;
	private EvaluationContext context;
	
	//***********************
	// CONSTRUCTORS
	//***********************
	
	/**
	 * Default Constructor
	 */
	public EvaluatedEncounterGroup() {
		super();
	}
	
	/**
	 * Full Constructor
	 */
	public EvaluatedEncounterGroup(EncounterGroup c, EncounterGroupDefinition definition, EvaluationContext context) {
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
	public EncounterGroupDefinition getDefinition() {
		return definition;
	}

	/**
	 * @param definition the definition to set
	 */
	public void setDefinition(EncounterGroupDefinition definition) {
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
