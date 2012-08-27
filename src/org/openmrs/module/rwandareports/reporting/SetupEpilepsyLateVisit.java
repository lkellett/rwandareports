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
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObservationInMostRecentEncounterOfType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rwandareports.customcalculator.DaysLate;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupEpilepsyLateVisit {
	
	protected final static Log log = LogFactory.getLog(SetupEpilepsyLateVisit.class);
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//Properties retrieved from global variables
	private Program epilepsyProgram;
	private Concept nextVisitConcept;
    private EncounterType epilepsyVisit;
    private int epilepsyRDVFormId;
    private int epilepsyDDBFormId;
	
    
    
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "EpilepsyLateVisit.xls",
		    "EpilepsyLateVisit.xls_", null);
		
		Properties props = new Properties();
		props.put(
		    "repeatingSections",
		    "sheet:1,row:9,dataset:dataSet");
		
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("EpilepsyLateVisit.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Epilepsy Late Visit");
	}
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Epilepsy Late Visit");
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
	
		// in PMTCT Program dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName("Patients Who have missed their visit by more than a week dataSetDefinition");
	
		SqlCohortDefinition patientsNotVoided = Cohorts.createPatientsNotVoided();
		dataSetDefinition.addFilter(patientsNotVoided, new HashMap<String, Object>());
				
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in "+epilepsyProgram.getName(), epilepsyProgram), ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		SqlCohortDefinition latevisit=new SqlCohortDefinition("select o.person_id from obs o, (select * from (select * from encounter where form_id in ("+epilepsyDDBFormId+","+epilepsyRDVFormId+") and voided=0 order by encounter_datetime desc) as e group by e.patient_id) as last_encounters, (select * from (select * from encounter where encounter_type="+epilepsyVisit.getEncounterTypeId()+" and voided=0 order by encounter_datetime desc) as e group by e.patient_id) as last_asthmaVisit where last_encounters.encounter_id=o.encounter_id and last_encounters.encounter_datetime<o.value_datetime and o.voided=0 and o.concept_id="+nextVisitConcept.getConceptId()+" and DATEDIFF(:endDate,o.value_datetime)>7 and (not last_asthmaVisit.encounter_datetime > o.value_datetime) and last_asthmaVisit.patient_id=o.person_id ");
	      latevisit.addParameter(new Parameter("endDate","endDate",Date.class));
              
        dataSetDefinition.addFilter(latevisit, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));

	    //==================================================================
        //                 Columns of report settings
        //==================================================================
	      
        MultiplePatientDataDefinitions imbType = RowPerPatientColumns.getIMBId("IMB ID");
        dataSetDefinition.addColumn(imbType, new HashMap<String, Object>());
    
        PatientProperty givenName = RowPerPatientColumns.getFirstNameColumn("familyName");
        dataSetDefinition.addColumn(givenName, new HashMap<String, Object>());
        
        PatientProperty familyName = RowPerPatientColumns.getFamilyNameColumn("givenName");
        dataSetDefinition.addColumn(familyName, new HashMap<String, Object>());
        
        MostRecentObservation lastphonenumber = RowPerPatientColumns.getMostRecentPatientPhoneNumber("telephone", null);
		dataSetDefinition.addColumn(lastphonenumber, new HashMap<String, Object>());

        PatientProperty gender = RowPerPatientColumns.getGender("Sex");
        dataSetDefinition.addColumn(gender, new HashMap<String, Object>());
        
        DateOfBirthShowingEstimation birthdate = RowPerPatientColumns.getDateOfBirth("Date of Birth", null, null);
        dataSetDefinition.addColumn(birthdate, new HashMap<String, Object>());
        
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", null, null),
		    new HashMap<String, Object>());
		
        CustomCalculationBasedOnMultiplePatientDataDefinitions numberofdaysLate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
        numberofdaysLate.addPatientDataToBeEvaluated(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", null, dateFilter),
		    new HashMap<String, Object>());
        numberofdaysLate.setName("numberofdaysLate");
        numberofdaysLate.setCalculator(new DaysLate());
		dataSetDefinition.addColumn(numberofdaysLate, new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getSeizureInMostRecentEncounterOfType(
			    "seizure", epilepsyVisit, new ObservationInMostRecentEncounterOfType()), new HashMap<String, Object>());
		
        PatientAddress address = RowPerPatientColumns.getPatientAddress("Address", true, true, true, true);
        dataSetDefinition.addColumn(address, new HashMap<String, Object>());
        
        PatientRelationship accompagnateur = RowPerPatientColumns.getAccompRelationship("AccompName");
        dataSetDefinition.addColumn(accompagnateur, new HashMap<String, Object>());
       
        dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
        dataSetDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
        
        Map<String, Object> mappings = new HashMap<String, Object>();
        mappings.put("location", "${location}");
        mappings.put("endDate", "${endDate}");
		
        reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
		
	}
	
	private void setupProperties() {
		
	 epilepsyProgram = gp.getProgram(GlobalPropertiesManagement.EPILEPSY_PROGRAM);
     epilepsyVisit = gp.getEncounterType(GlobalPropertiesManagement.EPILEPSY_VISIT);
     epilepsyRDVFormId=gp.getForm(GlobalPropertiesManagement.EPILEPSY_RENDEVOUS_VISIT_FORM).getFormId();
     epilepsyDDBFormId=gp.getForm(GlobalPropertiesManagement.EPILEPSY_DDB).getFormId();
     nextVisitConcept=gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE);  
       
        
        
 }
	
	
}