package org.openmrs.module.rwandareports.web.controller;

import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;

public class DQReportModel {

	private DataSetColumn selectedColumn;
	private Cohort selectedCohort;
	private List<Patient> patients;
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
	public List<Patient> getPatients() {
		return patients;
	}
	public void setPatients(List<Patient> patients) {
		this.patients = patients;
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
