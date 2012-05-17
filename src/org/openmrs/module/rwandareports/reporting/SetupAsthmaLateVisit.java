package org.openmrs.module.rwandareports.reporting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
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
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rwandareports.customcalculator.DaysLate;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupAsthmaLateVisit {
	
	protected final static Log log = LogFactory.getLog(SetupAsthmaLateVisit.class);
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//Properties retrieved from global variables
	private Program asthmaProgram;
    private Concept nextVisitConcept;
    private int asthmaDDBFormId;
    private EncounterType asthmaflowsheet;
    public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "AsthmaLateVisitTemplate.xls",
		    "XlsAsthmaLateVisitTemplate.xls_", null);
		
		Properties props = new Properties();
		props.put(
		    "repeatingSections",
		    "sheet:1,row:9,dataset:asthmaLateVisit");
		
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsAsthmaLateVisitTemplate".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Asthma/COPD Late Visit");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Asthma/COPD Late Visit");
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
	
		// in PMTCT Program  dataset definition 
				RowPerPatientDataSetDefinition dataSetDefinition1 = new RowPerPatientDataSetDefinition();
				dataSetDefinition1.setName("Patients Who have missed their visit by more than a week dataSetDefinition");
	
				SqlCohortDefinition patientsNotVoided = Cohorts.createPatientsNotVoided();
				dataSetDefinition1.addFilter(patientsNotVoided, new HashMap<String, Object>());
				
				dataSetDefinition1.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in "+asthmaProgram.getName(), asthmaProgram), ParameterizableUtil.createParameterMappings("onDate=${now}"));
		        
		  SqlCohortDefinition latevisit=new SqlCohortDefinition("select o.person_id from obs o, (select * from " +
		  		"(select * from encounter where encounter_type="+asthmaflowsheet.getEncounterTypeId()+" or form_id="+asthmaDDBFormId+" and voided=0 order by encounter_datetime desc) " +
		  		"as e group by patient_id) as last_encounters where last_encounters.encounter_id=o.encounter_id and last_encounters.encounter_datetime<o.value_datetime and o.voided=0 " +
		  		"and o.concept_id="+nextVisitConcept.getConceptId()+" and DATEDIFF(:endDate,o.value_datetime)>7 ");
	      latevisit.addParameter(new Parameter("endDate","endDate",Date.class));
	                
	      dataSetDefinition1.addFilter(latevisit, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
	  
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
        
		dataSetDefinition1.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", null, null),
		    new HashMap<String, Object>());
		
        CustomCalculationBasedOnMultiplePatientDataDefinitions numberofdaysLate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
        numberofdaysLate.addPatientDataToBeEvaluated(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", null, dateFilter),
		    new HashMap<String, Object>());
        numberofdaysLate.setName("numberofdaysLate");
        numberofdaysLate.setCalculator(new DaysLate());
		dataSetDefinition1.addColumn(numberofdaysLate, new HashMap<String, Object>());
		
        MostRecentObservation lastpeakflow = RowPerPatientColumns.getMostRecentPeakFlow("Most recent peakflow", "@ddMMMyy");
		dataSetDefinition1.addColumn(lastpeakflow, new HashMap<String, Object>());

        PatientAddress address1 = RowPerPatientColumns.getPatientAddress("Address", true, true, true, true);
        dataSetDefinition1.addColumn(address1, new HashMap<String, Object>());
        
        PatientRelationship accompagnateur = RowPerPatientColumns.getAccompRelationship("AccompName");
        dataSetDefinition1.addColumn(accompagnateur, new HashMap<String, Object>());
        
        dataSetDefinition1.addParameter(new Parameter("location", "Location", Location.class));
        dataSetDefinition1.addParameter(new Parameter("endDate", "End Date", Date.class));
        
        Map<String, Object> mappings = new HashMap<String, Object>();
        mappings.put("location", "${location}");
        mappings.put("endDate", "${endDate}");
		
        reportDefinition.addDataSetDefinition("asthmaLateVisit", dataSetDefinition1, mappings);
		
	}
	
	private void setupProperties() {
		
		asthmaProgram = gp.getProgram(GlobalPropertiesManagement.CHRONIC_RESPIRATORY_PROGRAM);
        
		asthmaflowsheet = gp.getEncounterType(GlobalPropertiesManagement.ASTHMA_VISIT);
        
        asthmaDDBFormId=gp.getForm(GlobalPropertiesManagement.ASTHMA_DDB).getFormId();
        
        nextVisitConcept=gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE);
 }
	
	
}