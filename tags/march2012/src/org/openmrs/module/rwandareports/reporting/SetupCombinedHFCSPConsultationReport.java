package org.openmrs.module.rwandareports.reporting;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CurrentOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfBirth;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfNextTestDueFromBirth;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfObsAfterDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.EvaluateDefinitionForOtherPersonData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RetrievePersonByRelationship;
import org.openmrs.module.rwandareports.filter.BorFStateFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupCombinedHFCSPConsultationReport {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//Properties retrieved from global variables
	private Program pmtctCombined;
	
	private ProgramWorkflow feedingState;
	
	private Concept seroConcept;
	
	private Concept dbsConcept;
	
	private Concept childSerologyConcept;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "PMTCTCombinedClinicConsultationSheet.xls",
		    "PMTCTCombinedClinicConsultationSheet.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:4,dataset:dataSet");
		
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PMTCTCombinedClinicConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Combined Clinic Consultation sheet");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Combined Clinic Consultation sheet");
		
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),
		    ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName(reportDefinition.getName() + " Data Set");
		
		InProgramCohortDefinition inPMTCTProgram = Cohorts.createInProgramParameterizableByDate(
		    "pmtct: Combined Clinic In Program", pmtctCombined);
		dataSetDefinition.addFilter(inPMTCTProgram, ParameterizableUtil.createParameterMappings("onDate=${now}"));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("InfantId"), new HashMap<String, Object>());
		
		RetrievePersonByRelationship mother = RowPerPatientColumns.getMother();
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMotherRelationship("MotherName"), new HashMap<String, Object>());
		
		EvaluateDefinitionForOtherPersonData motherId = RowPerPatientColumns.getDefinitionForOtherPerson("MotherId", mother,
		    RowPerPatientColumns.getIMBId("InfantId"));
		dataSetDefinition.addColumn(motherId, new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfBirth("birthdate", "ddMMMyyyy", "ddMMMyyyy"),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAgeInMonths("ageInMonths"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(
		    RowPerPatientColumns.getStateOfPatient("FeedingGroup", pmtctCombined, feedingState, new BorFStateFilter()),
		    new HashMap<String, Object>());
		
		DateOfNextTestDueFromBirth firstDbs = new DateOfNextTestDueFromBirth();
		firstDbs.setTimeUnit(Calendar.WEEK_OF_YEAR);
		firstDbs.setTimeIncrement(6);
		firstDbs.setName("firstDBSDue");
		firstDbs.setDateFormat("ddMMMyy");
		dataSetDefinition.addColumn(firstDbs, new HashMap<String, Object>());
		
		DateOfBirth dob = new DateOfBirth();
		
		dataSetDefinition.addColumn(
		    RowPerPatientColumns.getObsValueAfterDateOfOtherDefinition("firstDBSTest", dbsConcept, childSerologyConcept, dob, "ddMMMyy"),
		    new HashMap<String, Object>());
		
		DateOfObsAfterDateOfOtherDefinition firstDbsDate = RowPerPatientColumns.getDateOfObsAfterDateOfOtherDefinition(
		    "firstDBSDate", dbsConcept, childSerologyConcept, dob);
		
		dataSetDefinition.addColumn(
		    RowPerPatientColumns.getObsValueAfterDateOfOtherDefinition("confDBSTest", dbsConcept, childSerologyConcept, firstDbsDate, "ddMMMyy"),
		    new HashMap<String, Object>());
		
		DateOfNextTestDueFromBirth firstSero = new DateOfNextTestDueFromBirth();
		firstSero.setTimeUnit(Calendar.MONTH);
		firstSero.setTimeIncrement(9);
		firstSero.setName("firstSeroDue");
		firstSero.setDateFormat("ddMMMyy");
		dataSetDefinition.addColumn(firstSero, new HashMap<String, Object>());
		
		DateOfNextTestDueFromBirth secondSero = new DateOfNextTestDueFromBirth();
		secondSero.setTimeUnit(Calendar.MONTH);
		secondSero.setTimeIncrement(9);
		secondSero.setName("secondSeroDue");
		secondSero.setDateFormat("ddMMMyy");
		dataSetDefinition.addColumn(secondSero, new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(
		    RowPerPatientColumns.getObsValueAfterDateOfOtherDefinition("firstSeroTest", seroConcept, childSerologyConcept, dob, "ddMMMyy"),
		    new HashMap<String, Object>());
		
		DateOfObsAfterDateOfOtherDefinition firstSeroDate = RowPerPatientColumns.getDateOfObsAfterDateOfOtherDefinition(
		    "firstSeroDate", seroConcept, childSerologyConcept, dob);
		new DateOfObsAfterDateOfOtherDefinition();
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getObsValueAfterDateOfOtherDefinition("secondSeroTest",
		    seroConcept, childSerologyConcept, firstSeroDate, "ddMMMyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextRDV", "ddMMMyy"),
		    new HashMap<String, Object>());
		
		MostRecentObservation cd4Test = RowPerPatientColumns.getMostRecentCD4("CD4Test", "ddMMMyy");
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDefinitionForOtherPerson("motherCD4", mother, cd4Test),
		    new HashMap<String, Object>());
		
		CurrentOrdersRestrictedByConceptSet artDrugs = RowPerPatientColumns.getCurrentARTOrders("Regimen", "ddMMMyy", null);
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDefinitionForOtherPerson("motherART", mother, artDrugs),
		    new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
		
	}
	
	private void setupProperties() {
		pmtctCombined = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM);
		
		feedingState = gp.getProgramWorkflow(GlobalPropertiesManagement.FEEDING_GROUP_WORKFLOW,
		    GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM);
		
		seroConcept = gp.getConcept(GlobalPropertiesManagement.SERO_TEST);
		
		dbsConcept = gp.getConcept(GlobalPropertiesManagement.DBS_CONCEPT);
		
		childSerologyConcept = gp.getConcept(GlobalPropertiesManagement.CHILD_SEROLOGY_CONSTRUCT);
	}
}
