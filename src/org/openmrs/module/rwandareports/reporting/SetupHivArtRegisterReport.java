package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfWorkflowStateChange;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientData;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition2;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupHivArtRegisterReport {
	
	private Helper h = new Helper();
	
	private boolean pedi = false;
	
	private GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//private HashMap<String, String> properties;
	//properties
	private Program adultHIVProgram;
	
	private Program pediHIVProgram;
	
	private List<ProgramWorkflowState> pediOnART = new ArrayList<ProgramWorkflowState>();
	
	private List<ProgramWorkflowState> adultOnART = new ArrayList<ProgramWorkflowState>();
	
	private Concept onArtConcept;
	
	private Concept weight;
	
	private Concept stage;
	
	private Concept cd4;
	
	private Concept cd4Percentage;
	
	private Concept pregDeliveryDate;
	
	private Concept tbTest;
	
	private Concept ctx;
	
	private Concept tbTreatmentDrugs;
	
	private Concept firstLineArt;
	
	private Concept secondLineArt;
	
	private Concept art;
	
	public SetupHivArtRegisterReport(boolean pedi) {
		
		this.pedi = pedi;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		setUpProperties();
		
		ReportDefinition rd = createReportDefinition();
		if (pedi) {
			//h.createRowPerPatientXlsOverview(rd, "RegisterTemplate_small.xls", "PediHIVArtTemplate.xls_", null);
			ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "RegisterTemplate_small.xls",
			    "PediHIVArtTemplate.xls_", null);
			
			Properties props = new Properties();
			props.put("repeatingSections", "sheet:1,row:7,dataset:dataSet");
			
			design.setProperties(props);
			h.saveReportDesign(design);
		} else {
			//h.createRowPerPatientXlsOverview(rd, "RegisterTemplate_small.xls", "HIVArtTemplate.xls_", null);
			ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "RegisterTemplate_small.xls",
			    "HIVArtTemplate.xls_", null);
			
			Properties props = new Properties();
			props.put("repeatingSections", "sheet:1,row:7,dataset:dataSet");
			
			design.setProperties(props);
			h.saveReportDesign(design);
		}
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if (pedi) {
				if ("PediHIVArtTemplate.xls_".equals(rd.getName())) {
					rs.purgeReportDesign(rd);
				}
			} else {
				if ("HIVArtTemplate.xls_".equals(rd.getName())) {
					rs.purgeReportDesign(rd);
				}
			}
		}
		if (pedi) {
			h.purgeReportDefinition("Pedi HIV ART Register");
		} else {
			h.purgeReportDefinition("Adult HIV ART Register");
		}
		
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		if (pedi) {
			reportDefinition.setName("Pedi HIV ART Register");
		} else {
			reportDefinition.setName("Adult HIV ART Register");
		}
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		
		if (pedi) {
			reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort(),
			    ParameterizableUtil.createParameterMappings("location=${location}"));
		} else {
			reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort(),
			    ParameterizableUtil.createParameterMappings("location=${location}"));
		}
		
		createDataSetDefinition(reportDefinition);
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		// Create new dataset definition 
		HIVARTRegisterDataSetDefinition2 dataSetDefinition = new HIVARTRegisterDataSetDefinition2();
		dataSetDefinition.setName(reportDefinition.getName() + " Data Set");
		
		if (pedi) {
			dataSetDefinition.addFilter(Cohorts.createInProgram("hiv: In Pedi HIV Programs", pediHIVProgram));
		} else {
			dataSetDefinition.addFilter(Cohorts.createInProgram("hiv: In Adult HIV Programs", adultHIVProgram));
		}
		
		if (pedi) {
			dataSetDefinition.addFilter(Cohorts.createInCurrentState("hiv: In Pedi HIV OnART", pediOnART));
		} else {
			dataSetDefinition.addFilter(Cohorts.createInCurrentState("hiv: In Adult HIV OnART", adultOnART));
		}
		
		DateOfWorkflowStateChange startDate = RowPerPatientColumns.getDateOfWorkflowStateChange("Commencement of ART",
		    onArtConcept, null);
		dataSetDefinition.addColumn(startDate);
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDrugOrderForStartOfART("Start ART Regimen"));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("IMB ID"));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getTracnetId("TracNetID"));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getGender("gender"));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfBirth("birthdate", null, null));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAgeAtDateOfOtherDefinition("Age", startDate));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getObsValueAfterDateOfOtherDefinition("WEIGHT (KG)", weight,
		    startDate, null));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getObsValueAfterDateOfOtherDefinition("Initial stage", stage,
		    startDate, null));
		
		List<PatientData> initialCD4 = new ArrayList<PatientData>();
		initialCD4.add(RowPerPatientColumns
		        .getObsValueBeforeDateOfOtherDefinition("Initial CD4 count", cd4, startDate, null));
		initialCD4
		        .add(RowPerPatientColumns.getObsValueAfterDateOfOtherDefinition("Initial CD4 count", cd4, startDate, null));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMultiplePatientDataDefinitions("Initial CD4 count", initialCD4));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getObsValueBeforeDateOfOtherDefinition("CD4 percentage",
		    cd4Percentage, startDate, null));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAllDrugOrdersRestrictedByConcept("CTX treatment", ctx));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAllDrugOrdersRestrictedByConceptSet("TB Treatment",
		    tbTreatmentDrugs));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAllObservationValues("Pregnancy", pregDeliveryDate, null, null,
		    null));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDrugOrderForStartOfARTAfterDate("Initial Regimen", startDate));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAllDrugOrdersRestrictedByConceptSet("First Line Changes",
		    firstLineArt));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAllDrugOrdersRestrictedByConceptSet("Second Line Changes",
		    secondLineArt));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAllDrugOrdersRestrictedByConceptSet("ART Drugs", art));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAllObservationValues("CD", cd4, null, null, null));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAllObservationValues("Stage", stage, null, null, null));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAllObservationValues("TB", tbTest, null, null, null));
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("onDate", "${now}");
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	private void setUpProperties() {
		adultHIVProgram = gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		pediHIVProgram = gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		adultOnART.add(gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM));
		pediOnART.add(gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM));
		
		onArtConcept = gp.getConcept(GlobalPropertiesManagement.ON_ART_TREATMENT_STATUS_CONCEPT);
		weight = gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT);
		stage = gp.getConcept(GlobalPropertiesManagement.STAGE_CONCEPT);
		cd4 = gp.getConcept(GlobalPropertiesManagement.CD4_TEST);
		cd4Percentage = gp.getConcept(GlobalPropertiesManagement.CD4_PERCENTAGE_TEST);
		pregDeliveryDate = gp.getConcept(GlobalPropertiesManagement.PREGNANCY_DELIVERY_DATE);
		tbTest = gp.getConcept(GlobalPropertiesManagement.TB_TEST_CONCEPT);
		
		ctx = gp.getConcept(GlobalPropertiesManagement.CTX);
		tbTreatmentDrugs = gp.getConcept(GlobalPropertiesManagement.TB_TREATMENT_DRUGS);
		firstLineArt = gp.getConcept(GlobalPropertiesManagement.ART_FIRST_LINE_DRUG_SET);
		secondLineArt = gp.getConcept(GlobalPropertiesManagement.ART_SECOND_LINE_DRUG_SET);
		art = gp.getConcept(GlobalPropertiesManagement.ART_DRUGS_SET);
	}
	
}
