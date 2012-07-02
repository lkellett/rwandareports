package org.openmrs.module.rwandareports.reporting;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.orderextension.DrugRegimen;
import org.openmrs.module.orderextension.api.OrderExtensionService;
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
		
		//reportDefinition.addParameter(new Parameter("Patient", "patient", Patient.class));
		//reportDefinition.addParameter(new Parameter("DrugRegimen", "drugRegimen", DrugRegimen.class));
		//reportDefinition.setBaseCohortDefinition(Cohorts.createPatientCohort("patientCohort"), ParameterizableUtil.createParameterMappings("patient=${patient}"));
		reportDefinition.setBaseCohortDefinition(Cohorts.createPatientCohort("patientCohort"), new HashMap<String, Object>());
		
		createDataSetDefinitions(reportDefinition);
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinitions(ReportDefinition reportDefinition) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();		
		dataSetDefinition.setName("demoDataSet");
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentWeight("RecentWeight", "dd/MM/yy"),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentHeight("RecentHeight", "dd/MM/yy"),new HashMap<String, Object>());
		    
		dataSetDefinition.addColumn(RowPerPatientColumns.getStateOfPatient("intent", oncologyProgram, treatmentIntent, null), new HashMap<String, Object>());
		
		//TODO: needs to be passed in as parameters
		Integer regimen = new Integer(343);
		FirstDrugRegimenCycle fdc = new FirstDrugRegimenCycle();
		fdc.setName("firstCycle");
		fdc.setRegimen(regimen);
		dataSetDefinition.addColumn(fdc, new HashMap<String, Object>());
		
		DrugRegimenInformation info = RowPerPatientColumns.getDrugRegimenInformation("regimenInfo");
		info.setRegimen(regimen);
		dataSetDefinition.addColumn(info, new HashMap<String, Object>());
		
		//Premedication dataSet
		ExtendedDrugOrderDataSetDefinition premedicationDS = new ExtendedDrugOrderDataSetDefinition();
		premedicationDS.setIndication(premedication);
		premedicationDS.setDrugRegimen(regimen);
		
		//Chemotherapy dataSet
		ExtendedDrugOrderDataSetDefinition chemotherapyDS = new ExtendedDrugOrderDataSetDefinition();
		chemotherapyDS.setIndication(chemotherapy);
		chemotherapyDS.setDrugRegimen(regimen);
		
		//Postmedication dataSet
		ExtendedDrugOrderDataSetDefinition postmedicationDS = new ExtendedDrugOrderDataSetDefinition();
		postmedicationDS.setIndication(postmedication);
		postmedicationDS.setDrugRegimen(regimen);
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		
		reportDefinition.addDataSetDefinition("demoDataSet", dataSetDefinition, mappings);
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
