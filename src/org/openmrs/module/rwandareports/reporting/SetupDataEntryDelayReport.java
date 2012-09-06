package org.openmrs.module.rwandareports.reporting;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InverseCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateDiff;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateDiff.DateDiffType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfBirthShowingEstimation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentEncounterType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;
import org.openmrs.module.rwandareports.customcalculator.BMI;
import org.openmrs.module.rwandareports.customcalculator.BMICalculation;
import org.openmrs.module.rwandareports.customcalculator.DeclineHighestCD4;
import org.openmrs.module.rwandareports.customcalculator.DifferenceBetweenLastTwoObs;
import org.openmrs.module.rwandareports.dataset.DataEntryDelayDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.filter.GroupStateFilter;
import org.openmrs.module.rwandareports.filter.LastEncounterFilter;
import org.openmrs.module.rwandareports.filter.TreatmentStateFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

public class SetupDataEntryDelayReport {
	
	protected final static Log log = LogFactory.getLog(SetupDataEntryDelayReport.class);
	
	private GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	Helper h = new Helper();
	
	public void setup() throws Exception {
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "DataEntryDelay.xls", "XlsDataEntryDelay",
		    null);
		
		createDataSetDefinition(rd);
		
		h.saveReportDefinition(rd);
		
		Properties props = new Properties();
		props.put(
		    "repeatingSections",
		    "sheet:1,dataset:dataSet|sheet:1,row:9,dataset:summary|sheet:1,row:15,dataset:Adult HIV Flowsheet|sheet:1,row:21,dataset:Pediatric HIV Flowsheet|sheet:1,row:27,dataset:ASTHMA VISIT|sheet:1,row:33,dataset:DIABETES VISIT|sheet:1,row:40,dataset:EPILEPSY VISIT|sheet:1,row:46,dataset:Heart Failure Flowsheet|sheet:1,row:52,dataset:HYPERTENSION VISIT|sheet:1,row:58,dataset:Inpatient Oncology|sheet:1,row:64,dataset:Outpatient Oncology|sheet:1,row:70,dataset:delayCases");
		
	
		
		design.setProperties(props);
		h.saveReportDesign(design);
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsDataEntryDelay".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Data Entry Delay Report");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Data Entry Delay Report");
		
		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");
		reportDefinition.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		reportDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition rd) {
		
		DataEntryDelayDataSetDefinition dataEntryDelay = new DataEntryDelayDataSetDefinition();
		dataEntryDelay.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dataEntryDelay.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataEntryDelay.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.ADULT_FLOWSHEET_ENCOUNTER));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.PEDI_FLOWSHEET_ENCOUNTER));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.ASTHMA_VISIT));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.DIABETES_VISIT));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.EPILEPSY_VISIT));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.HEART_FAILURE_ENCOUNTER));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.HYPERTENSION_ENCOUNTER));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.INPATIENT_ONCOLOGY_ENCOUNTER));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.OUTPATIENT_ONCOLOGY_ENCOUNTER));
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate+1d}");
		mappings.put("startDate", "${startDate}");
		
		rd.addDataSetDefinition("dataSet", dataEntryDelay, mappings);
	}
}
