package org.openmrs.module.rwandareports.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.query.encounter.EncounterQueryResult;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;
import org.openmrs.module.rwandareports.indicator.EncounterIndicator;
import org.openmrs.module.rwandareports.indicator.EncounterIndicatorResult;

public class DQReportModel {

	private DataSetColumn selectedColumn;
	private Cohort selectedCohort;
	private String selectedEncounter;
	private List<Patient> patients;
	private List<Encounter>  encounters;
	private DataSet dataSet;
	private DataSetDefinition dataSetDefinition;
	private List<DataSetDefinition> dataSetDefinitions;
	
	public DataSetColumn getSelectedColumn() {
		return selectedColumn;
	}
	public void setSelectedColumn(DataSetColumn selectedColumn) {
		this.selectedColumn = selectedColumn;
	}
	public Cohort getSelectedCohort() {
		return selectedCohort;
	}
	public void setSelectedCohort(Cohort selectedCohort) {
		this.selectedCohort = selectedCohort;
	}
	
    public String getSelectedEncounter() {
	    return selectedEncounter;
    }
	
    public void setSelectedEncounter(String selectedEncounter) {
	    this.selectedEncounter = selectedEncounter;
    }
	public List<Patient> getPatients() {
		return patients;
	}
	public void setPatients(List<Patient> patients) {
		this.patients = patients;
	}
	
    public List<Encounter>  getEncounters() {
		    return encounters;
	    }
		
	public void setEncounters(List<Encounter> encounter) {
		    this.encounters = encounter;
	    }
	
	public DataSet getDataSet() {
		return dataSet;
	}
	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}
	public DataSetDefinition getDataSetDefinition() {
		return dataSetDefinition;
	}
	public void setDataSetDefinition(DataSetDefinition dataSetDefinition) {
		this.dataSetDefinition = dataSetDefinition;
	}
	public List<DataSetDefinition> getDataSetDefinitions() {
		return dataSetDefinitions;
	}
	public void setDataSetDefinitions(List<DataSetDefinition> dataSetDefinitions) {
		this.dataSetDefinitions = dataSetDefinitions;
	}
	
}
