package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
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
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rwandareports.customcalculator.DaysLate;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.filter.LastEncounterFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupNCDLateVisitandLTFUReport {
	
	protected final static Log log = LogFactory.getLog(SetupNCDLateVisitandLTFUReport.class);
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	// properties retrieved from global variables
	private List<Program> diseases;
	
	private List<EncounterType> clinicalEncoutersExcLab;
	
	private Concept returnVisitDateConcept;
	
	public void setup() throws Exception {
		setupPrograms();
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "NCDLateVisitAndLTFUSheet.xls",
		    "NCDLateVisitAndLTFUSheet.xls_", null);
		Properties props = new Properties();
		props.put(
		    "repeatingSections",
		    "sheet:1,row:13,dataset:LATE_VISITdataset0|sheet:1,row:15,dataset:LATE_VISITdataset1|sheet:1,row:17,dataset:LATE_VISITdataset2|sheet:1,row:19,dataset:LATE_VISITdataset3|sheet:1,row:21,dataset:LATE_VISITdataset4|sheet:2,row:13,dataset:LTFUdataset0|sheet:2,row:15,dataset:LTFUdataset1|sheet:2,row:17,dataset:LTFUdataset2|sheet:2,row:19,dataset:LTFUdataset3|sheet:2,row:21,dataset:LTFUdataset4");
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("NCDLateVisitAndLTFUSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("NCD Late Visit and Lost to Follow Up");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("NCD Late Visit and Lost to Follow Up");
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));
		reportDefinition.addParameter(new Parameter("endDate", "Date", Date.class));
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),
		    ParameterizableUtil.createParameterMappings("location=${location}"));
		
		for (Program program : diseases) {
			for (FilterType filterType : FilterType.values()) {
				
				createDataSetDefinition(reportDefinition, program, filterType, diseases.indexOf(program));
			}
		}
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition, Program program, FilterType filterType,
	                                     int datasetIndex) {
		// Create new dataset definition
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName(program.getName() + " Data Set");
		dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition.addParameter(new Parameter("endDate", "enDate", Date.class));
		
		// Add Filters
		if (filterType.equals(FilterType.LATE_VISIT))
			addLateVisitFilter(dataSetDefinition, program);
		
		if (filterType.equals(FilterType.LTFU))
			addLTFUFilter(dataSetDefinition, program);
		
		DateFormatFilter dateFilter = new DateFormatFilter();
		dateFilter.setFinalDateFormat("yyyy/MM/dd");
		
		// Add Columns
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getGender("Sex"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getCurrentPatientProgram("currentProgram", program),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("AccompName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getRecentEncounterType("Last visit type", clinicalEncoutersExcLab,
		    new LastEncounterFilter()), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(
		    RowPerPatientColumns.getLastWeekMostRecentReturnVisitDate("nextVisit", null, dateFilter),
		    new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions daysLate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		daysLate.addPatientDataToBeEvaluated(
		    RowPerPatientColumns.getLastWeekMostRecentReturnVisitDate("nextVisit", null, dateFilter),
		    new HashMap<String, Object>());
		daysLate.setName("daysLate");
		daysLate.setCalculator(new DaysLate());
		dataSetDefinition.addColumn(daysLate, new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition(filterType + "dataset" + datasetIndex, dataSetDefinition, mappings);
		
	}
	
	private RowPerPatientDataSetDefinition addLateVisitFilter(RowPerPatientDataSetDefinition dataSetDefinition,
	                                                          Program program) {
		
		// only patients who are not LTFU
		EncounterCohortDefinition patientsWithClinicalEncountersWithoutLabTest = Cohorts.createEncounterParameterizedByDate(
		    "patientsWithClinicalEncountersWithoutLabTest", "onOrAfter", clinicalEncoutersExcLab);
		
		dataSetDefinition.addFilter(patientsWithClinicalEncountersWithoutLabTest,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		
		// only patient enrolled in the current program
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate(program.getName() + "Cohort", program),
			ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		DateObsCohortDefinition dueThatWeek = Cohorts.createDateObsCohortDefinition(returnVisitDateConcept, RangeComparator.GREATER_EQUAL, RangeComparator.LESS_THAN, TimeModifier.ANY);
		
		List<String> parameterNames = new ArrayList<String>();
		parameterNames.add("onOrAfter");
		parameterNames.add("onOrBefore");
		
		EncounterCohortDefinition patientsWithClinicalEncountersBetweenDates = Cohorts.createEncounterParameterizedByDate(
		    "patientsWithClinicalEncountersLastWeek", parameterNames, clinicalEncoutersExcLab);
		
		// only patients who had a scheduled visit last week and don't have an encounter that week
		CompositionCohortDefinition patientsWithoutClinicalEncounters = new CompositionCohortDefinition();
		patientsWithoutClinicalEncounters.setName("patientsWithoutClinicalEncountersCompositionCohort");
		patientsWithoutClinicalEncounters.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithoutClinicalEncounters.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithoutClinicalEncounters.getSearches().put(
		    "patientsWithClinicalEncountersLastWeek",
		    new Mapped<CohortDefinition>(patientsWithClinicalEncountersBetweenDates, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		
		patientsWithoutClinicalEncounters.getSearches().put(
		    "patientsDueLastweek",
		    new Mapped<CohortDefinition>(dueThatWeek, ParameterizableUtil
		            .createParameterMappings("value1=${onOrAfter},value2=${onOrBefore}")));
		
		patientsWithoutClinicalEncounters
		        .setCompositionString("patientsDueLastweek AND (NOT patientsWithClinicalEncountersLastWeek)");
		
		dataSetDefinition.addFilter(patientsWithoutClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-7d},onOrBefore=${endDate}"));
		return dataSetDefinition;
	}
	
	private RowPerPatientDataSetDefinition addLTFUFilter(RowPerPatientDataSetDefinition dataSetDefinition, Program program) {
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate(program.getName() + "Cohort", program),
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		// Patients without Any clinical Encounter(Test lab excluded) in last
		// twelve months.
		EncounterCohortDefinition patientsWithClinicalEncountersWithoutLabTest = Cohorts.createEncounterParameterizedByDate(
		    "patientsWithClinicalEncounters", "onOrAfter", clinicalEncoutersExcLab);
		
		CompositionCohortDefinition patientsWithoutClinicalEncounters = new CompositionCohortDefinition();
		patientsWithoutClinicalEncounters.setName("patientsWithoutClinicalEncounters");
		patientsWithoutClinicalEncounters.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithoutClinicalEncounters.getSearches().put(
		    "patientsWithClinicalEncounters",
		    new Mapped<CohortDefinition>(patientsWithClinicalEncountersWithoutLabTest, ParameterizableUtil
		            .createParameterMappings("onOrAfter=${onOrAfter}")));
		patientsWithoutClinicalEncounters.setCompositionString("NOT patientsWithClinicalEncounters");
		
		dataSetDefinition.addFilter(patientsWithoutClinicalEncounters,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m}"));
		return dataSetDefinition;
	}
	
	private List<Program> setupPrograms() {
		clinicalEncoutersExcLab = gp.getEncounterTypeList(GlobalPropertiesManagement.CLINICAL_ENCOUNTER_TYPES_EXC_LAB_TEST);
		returnVisitDateConcept = gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE);
		diseases = new ArrayList<Program>();
		diseases.add(gp.getProgram(GlobalPropertiesManagement.DM_PROGRAM));
		diseases.add(gp.getProgram(GlobalPropertiesManagement.CRD_PROGRAM));
		diseases.add(gp.getProgram(GlobalPropertiesManagement.HEART_FAILURE_PROGRAM_NAME));
		diseases.add(gp.getProgram(GlobalPropertiesManagement.HYPERTENSION_PROGRAM));
		diseases.add(gp.getProgram(GlobalPropertiesManagement.EPILEPSY_PROGRAM));
		return diseases;
	}
	
	// we'll do two types of filtering: late visit filtering and lost to follow
	// up filtering
	private enum FilterType {
		/**
		 * Indicates late visit filter type
		 */
		LATE_VISIT,
		/**
		 * Indicates lost to follow up filter type
		 */
		LTFU;
		
	}
}
