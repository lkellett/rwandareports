package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.EvaluateDefinitionForOtherPersonData;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RetrievePersonByRelationship;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupPMTCTFoodDistributionReport {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//Properties
	private Program pmtctCombined;
	
	private List<ProgramWorkflowState> feedingStates = new ArrayList<ProgramWorkflowState>();
	
	private ProgramWorkflow feedingStatus;
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "PMTCTFoodDistribution.xls",
		    "PMTCTFoodDistribution.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:4,dataset:dataSet");
		
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PMTCTFoodDistribution.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("HIV-PMTCT Food Package Distribution");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("HIV-PMTCT Food Package Distribution");
		
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
		
		dataSetDefinition.addFilter(Cohorts.createInCurrentState("feeding state: Feeding state of patients", feedingStates),
		    ParameterizableUtil.createParameterMappings("onDate=${now}"));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("InfantId"), new HashMap<String, Object>());
		
		RetrievePersonByRelationship mother = RowPerPatientColumns.getMother();
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMotherRelationship("MotherName"), new HashMap<String, Object>());
		
		EvaluateDefinitionForOtherPersonData motherId = RowPerPatientColumns.getDefinitionForOtherPerson("MotherId", mother,
		    RowPerPatientColumns.getIMBId("InfantId"));
		dataSetDefinition.addColumn(motherId, new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfBirth("DOB", null, null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAgeInMonths("Age in months"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(
		    RowPerPatientColumns.getStateOfPatient("FeedingGroup", pmtctCombined, feedingStatus, null),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("NextVisit", null),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("AccompName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("Address", true, true, true, true),
		    new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	private void setUpProperties() {
		pmtctCombined = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM);
		
		feedingStates.add(gp.getProgramWorkflowState(GlobalPropertiesManagement.BREASTFEEDING_STATE_ONE,
		    GlobalPropertiesManagement.FEEDING_GROUP_WORKFLOW, GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM));
		feedingStates.add(gp.getProgramWorkflowState(GlobalPropertiesManagement.BREASTFEEDING_STATE_TWO,
		    GlobalPropertiesManagement.FEEDING_GROUP_WORKFLOW, GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM));
		feedingStates.add(gp.getProgramWorkflowState(GlobalPropertiesManagement.BREASTFEEDING_STATE_THREE,
		    GlobalPropertiesManagement.FEEDING_GROUP_WORKFLOW, GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM));
		
		feedingStatus = gp.getProgramWorkflow(GlobalPropertiesManagement.FEEDING_GROUP_WORKFLOW,
		    GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM);
	}
}
