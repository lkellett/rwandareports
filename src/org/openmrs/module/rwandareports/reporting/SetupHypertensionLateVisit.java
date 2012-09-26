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
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfBirthShowingEstimation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObservationInMostRecentEncounterOfType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rwandareports.customcalculator.DaysLate;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupHypertensionLateVisit {
	
	protected final static Log log = LogFactory.getLog(SetupHypertensionLateVisit.class);
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//Properties retrieved from global variables
	private Program hypertensionProgram;
   
    private EncounterType hypertensionflowsheet;
    private Form hypertensionRDVForm;
    private Form hypertensionDDBForm;
    
    private List<Form> hypertensionForms = new ArrayList<Form>();
	
    public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "HypertensionLateVisit.xls",
		    "XLSHypertensionLateVisit", null);
		
		Properties props = new Properties();
		props.put(
		    "repeatingSections",
		    "sheet:1,row:8,dataset:dataSet");
		
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XLSHypertensionLateVisit".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("NCD-Hypertension Late Visit");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("NCD-Hypertension Late Visit");
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),
		    ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
	   
		DateFormatFilter dateFilter = new DateFormatFilter();
		dateFilter.setFinalDateFormat("yyyy/MM/dd");
	
		RowPerPatientDataSetDefinition dataSetDefinition1 = new RowPerPatientDataSetDefinition();
		dataSetDefinition1.setName("LateVisit");
	
		dataSetDefinition1.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in " + hypertensionProgram.getName(), hypertensionProgram), ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
	                
	    dataSetDefinition1.addFilter(Cohorts.createPatientsLateForVisit(hypertensionForms, hypertensionflowsheet), ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
	  
	     //==================================================================
        //                 Columns of report settings
        //==================================================================
	      
        MultiplePatientDataDefinitions imbType = RowPerPatientColumns.getIMBId("IMB ID");
        dataSetDefinition1.addColumn(imbType, new HashMap<String, Object>());
    
        PatientProperty givenName = RowPerPatientColumns.getFirstNameColumn("familyName");
        dataSetDefinition1.addColumn(givenName, new HashMap<String, Object>());
        
        PatientProperty familyName = RowPerPatientColumns.getFamilyNameColumn("givenName");
        dataSetDefinition1.addColumn(familyName, new HashMap<String, Object>());
        
        MostRecentObservation lastphonenumber = RowPerPatientColumns.getMostRecentPatientPhoneNumber("telephone", null);
		dataSetDefinition1.addColumn(lastphonenumber, new HashMap<String, Object>());

        PatientProperty gender = RowPerPatientColumns.getGender("Sex");
        dataSetDefinition1.addColumn(gender, new HashMap<String, Object>());
        
        DateOfBirthShowingEstimation birthdate = RowPerPatientColumns.getDateOfBirth("Date of Birth", null, null);
        dataSetDefinition1.addColumn(birthdate, new HashMap<String, Object>());
        
        dataSetDefinition1.addColumn(RowPerPatientColumns.getNextVisitInMostRecentEncounterOfTypes("nextVisit",hypertensionflowsheet,
						new ObservationInMostRecentEncounterOfType(),null),new HashMap<String, Object>());
		
        CustomCalculationBasedOnMultiplePatientDataDefinitions numberofdaysLate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
        numberofdaysLate.addPatientDataToBeEvaluated(RowPerPatientColumns.getNextVisitInMostRecentEncounterOfTypes("nextVisit",hypertensionflowsheet,
				new ObservationInMostRecentEncounterOfType(),dateFilter),new HashMap<String, Object>());
        numberofdaysLate.setName("numberofdaysLate");
        numberofdaysLate.setCalculator(new DaysLate());
		dataSetDefinition1.addColumn(numberofdaysLate, new HashMap<String, Object>());
		
		MostRecentObservation systolic = RowPerPatientColumns.getMostRecentSystolicPB("systolic", "@ddMMMyy");
		dataSetDefinition1.addColumn(systolic, new HashMap<String, Object>());
		
		MostRecentObservation diastolic = RowPerPatientColumns.getMostRecentDiastolicPB("diastolic", "@ddMMMyy");
		dataSetDefinition1.addColumn(diastolic, new HashMap<String, Object>());

        PatientAddress address1 = RowPerPatientColumns.getPatientAddress("Address", true, true, true, true);
        dataSetDefinition1.addColumn(address1, new HashMap<String, Object>());
        
        PatientRelationship accompagnateur = RowPerPatientColumns.getAccompRelationship("AccompName");
        dataSetDefinition1.addColumn(accompagnateur, new HashMap<String, Object>());
        
        dataSetDefinition1.addParameter(new Parameter("location", "Location", Location.class));
        dataSetDefinition1.addParameter(new Parameter("endDate", "End Date", Date.class));
        
        Map<String, Object> mappings = new HashMap<String, Object>();
        mappings.put("location", "${location}");
        mappings.put("endDate", "${endDate}");
		
        reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition1, mappings);
		
	}
	
	private void setupProperties() {
		
		hypertensionProgram = gp.getProgram(GlobalPropertiesManagement.HYPERTENSION_PROGRAM);
        
		hypertensionflowsheet = gp.getEncounterType(GlobalPropertiesManagement.HYPERTENSION_ENCOUNTER);
        
        hypertensionRDVForm = gp.getForm(GlobalPropertiesManagement.HYPERTENSION_FLOW_VISIT);

        hypertensionDDBForm = gp.getForm(GlobalPropertiesManagement.HYPERTENSION_DDB);
        
        hypertensionForms.add(hypertensionDDBForm);
        hypertensionForms.add(hypertensionRDVForm);
	}	
}