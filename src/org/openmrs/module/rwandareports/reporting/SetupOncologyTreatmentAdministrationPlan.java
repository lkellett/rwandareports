package org.openmrs.module.rwandareports.reporting;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.ExtendedDrugOrderDataSetDefinition;
import org.openmrs.module.rwandareports.definition.DrugRegimenInformation;
import org.openmrs.module.rwandareports.definition.FirstDrugRegimenCycle;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupOncologyTreatmentAdministrationPlan {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	Program oncologyProgram;
	
	ProgramWorkflow treatmentIntent;
	
	Concept premedication;
	
	Concept chemotherapy;
	
	Concept postmedication;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "TreatmentAdministrationPlan.xls",
		    "TreatmentAdministrationPlan.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:19,dataset:premedication|sheet:1,row:21,dataset:chemotherapy|sheet:1,row:23,dataset:postmedication");
		design.setProperties(props);
		
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("TreatmentAdministrationPlan.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Chemotherapy Treatment Administration Plan");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Chemotherapy Treatment Administration Plan");
		
		reportDefinition.addParameter(new Parameter("patientId", "patientId", String.class));
		reportDefinition.addParameter(new Parameter("regimenId", "regimenId", String.class));
		
		reportDefinition.setBaseCohortDefinition(Cohorts.createPatientCohort("patientCohort"), ParameterizableUtil.createParameterMappings("patientId=${patientId}"));
		
		createDataSetDefinitions(reportDefinition);
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinitions(ReportDefinition reportDefinition) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.addParameter(new Parameter("regimen", "regimen", String.class));
		dataSetDefinition.setName("demoDataSet");
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentWeight("RecentWeight", "dd/MM/yy"),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentHeight("RecentHeight", "dd/MM/yy"),new HashMap<String, Object>());
		    
		dataSetDefinition.addColumn(RowPerPatientColumns.getStateOfPatient("intent", oncologyProgram, treatmentIntent, null), new HashMap<String, Object>());
		
		FirstDrugRegimenCycle fdc = new FirstDrugRegimenCycle();
		fdc.setName("firstCycle");
		fdc.addParameter(new Parameter("regimen", "regimen", String.class));
		dataSetDefinition.addColumn(fdc, ParameterizableUtil.createParameterMappings("regimen=${regimen}"));
		
		DrugRegimenInformation info = RowPerPatientColumns.getDrugRegimenInformation("regimenInfo");
		info.addParameter(new Parameter("regimen", "regimen", String.class));
		dataSetDefinition.addColumn(info, ParameterizableUtil.createParameterMappings("regimen=${regimen}"));
		
		//Premedication dataSet
		ExtendedDrugOrderDataSetDefinition premedicationDS = new ExtendedDrugOrderDataSetDefinition();
		premedicationDS.setIndication(premedication);
		premedicationDS.addParameter(new Parameter("drugRegimen", "drugRegimen", String.class));
		
		//Chemotherapy dataSet
		ExtendedDrugOrderDataSetDefinition chemotherapyDS = new ExtendedDrugOrderDataSetDefinition();
		chemotherapyDS.setIndication(chemotherapy);
		chemotherapyDS.addParameter(new Parameter("drugRegimen", "drugRegimen", String.class));
		
		//Postmedication dataSet
		ExtendedDrugOrderDataSetDefinition postmedicationDS = new ExtendedDrugOrderDataSetDefinition();
		postmedicationDS.setIndication(postmedication);
		postmedicationDS.addParameter(new Parameter("drugRegimen", "drugRegimen", String.class));
		
		Map<String, Object> mappings = ParameterizableUtil.createParameterMappings("drugRegimen=${regimenId}");
		Map<String, Object> mappings2 = ParameterizableUtil.createParameterMappings("regimen=${regimenId}");
		
		reportDefinition.addDataSetDefinition("demoDataSet", dataSetDefinition, mappings2);
		reportDefinition.addDataSetDefinition("premedication", premedicationDS, mappings);
		reportDefinition.addDataSetDefinition("chemotherapy", chemotherapyDS, mappings);
		reportDefinition.addDataSetDefinition("postmedication", postmedicationDS, mappings);
	}
	
	private void setupProperties() {
		oncologyProgram = gp.getProgram(GlobalPropertiesManagement.ONCOLOGY_PROGRAM);
		
		treatmentIntent = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_INTENT_WORKFLOW,
		    GlobalPropertiesManagement.ONCOLOGY_PROGRAM);
		
		premedication = gp.getConcept(GlobalPropertiesManagement.PREMEDICATION);
		chemotherapy = gp.getConcept(GlobalPropertiesManagement.CHEMOTHERAPY);
		postmedication = gp.getConcept(GlobalPropertiesManagement.POSTMEDICATION);
	}
}
