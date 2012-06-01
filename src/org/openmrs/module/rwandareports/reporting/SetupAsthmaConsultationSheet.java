package org.openmrs.module.rwandareports.reporting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObservationInMostRecentEncounterOfType;
import org.openmrs.module.rwandareports.customcalculator.AsthmaClassificationAlerts;
import org.openmrs.module.rwandareports.customcalculator.BMI;
import org.openmrs.module.rwandareports.customcalculator.DeclineHighestCD4;
import org.openmrs.module.rwandareports.customcalculator.HIVAdultAlerts;
import org.openmrs.module.rwandareports.dataset.comparator.PMTCTDataSetRowComparator;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.filter.DrugDosageFrequencyFilter;
import org.openmrs.module.rwandareports.filter.DrugNameFilter;
import org.openmrs.module.rwandareports.filter.LastThreeObsFilter;
import org.openmrs.module.rwandareports.filter.LastTwoObsFilter;
import org.openmrs.module.rwandareports.filter.ObservationFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupAsthmaConsultationSheet {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties retrieved from global variables
	private Program asthmaProgram;
	
	//private EncounterType flowsheetAsthmas;
	
	private Form rendevousForm;
	private int asthmaDDBFormId ;
	
	private Concept returnVisitDate;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "AsthmaConsultationSheet.xls",
		    "AsthmaConsultationSheet.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:9,dataset:dataSet");
		design.setProperties(props);
		
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("AsthmaConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Asthma Consultation Sheet");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Asthma Consultation Sheet");
				
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));	
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),ParameterizableUtil.createParameterMappings("location=${location}"));
		reportDefinition.addParameter(new Parameter("endDate", "Monday", Date.class));
		createDataSetDefinition(reportDefinition);
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName("Asthma Consultation Data Set");
		dataSetDefinition.setComparator(new PMTCTDataSetRowComparator());
		dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition.addParameter(new Parameter("endDate", "Monday", Date.class));
		
		//Add filters
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in "+asthmaProgram.getName(), asthmaProgram), ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		dataSetDefinition.addFilter(getMondayToSundayPatientReturnVisit(), ParameterizableUtil.createParameterMappings("end=${endDate+7d},start=${endDate}"));
		
		//dataSetDefinition.addFilter(getMondayToSundayPatientReturnVisit(), ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
		
		DateFormatFilter dateFilter = new DateFormatFilter();
		dateFilter.setFinalDateFormat("dd-MMM-yyyy");
		
		//Add Columns
		
		
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", null, dateFilter), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());		
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getGender("Sex"), new HashMap<String, Object>());		
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentPeakFlow("Last peak flow", "@ddMMMyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getCurrentAsthmaOrders("Regimen", "@ddMMMyy", new DrugDosageFrequencyFilter()),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("Accompagnateur"), new HashMap<String, Object>());
		
		
		AllObservationValues asthmaClassification = RowPerPatientColumns.getAllAsthmaClassificationValues("asthmaClassification", null, new LastTwoObsFilter(),
		    null);
		
		
				
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(asthmaClassification, new HashMap<String, Object>());
		alert.setCalculator(new AsthmaClassificationAlerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());	
		
	
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		//mappings.put("endDate", new Date());
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	private void setupProperties() {
		asthmaProgram = gp.getProgram(GlobalPropertiesManagement.CHRONIC_RESPIRATORY_PROGRAM);
		
	//	flowsheetAsthmas = gp.getEncounterType(GlobalPropertiesManagement.ASTHMA_VISIT);
		
		rendevousForm=gp.getForm(GlobalPropertiesManagement.ASTHMA_RENDEVOUS_VISIT_FORM);
		
		asthmaDDBFormId=gp.getForm(GlobalPropertiesManagement.ASTHMA_DDB).getFormId();
		
		returnVisitDate=gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE);
	}
	
	
	private SqlCohortDefinition getMondayToSundayPatientReturnVisit() {		
		
	    SqlCohortDefinition cohortquery=new SqlCohortDefinition();
	    //cohortquery.setQuery("select o.person_id from obs o,(select * from (select * from encounter where (form_id="+asthmaDDBFormId+" or encounter_type="+flowsheetAsthmas.getEncounterTypeId()+") order by encounter_datetime desc) as ordred_enc group by ordred_enc.patient_id) as last_enc where o.encounter_id=last_enc.encounter_id and last_enc.voided=0 and o.voided=0 and o.concept_id="+returnVisitDate.getConceptId()+" and o.value_datetime>=(select DATE_FORMAT(CURDATE()+(- (select IF(DAYOFWEEK(CURDATE())=1,6,DAYOFWEEK(CURDATE())-2) as sun)),'%Y-%m-%d')) and o.value_datetime<=(select DATE_FORMAT(CURDATE()+(- (select IF(DAYOFWEEK(CURDATE())=1,6,DAYOFWEEK(CURDATE())-2) as sun)+6),'%Y-%m-%d')) order by o.value_datetime");
	    cohortquery.setQuery("select o.person_id from obs o,(select * from (select * from encounter where (form_id="+asthmaDDBFormId+" or form_id="+rendevousForm+") order by encounter_datetime desc) as ordred_enc group by ordred_enc.patient_id) as last_enc where o.encounter_id=last_enc.encounter_id and last_enc.voided=0 and o.voided=0 and o.concept_id="+returnVisitDate.getConceptId()+" and o.value_datetime>= :start and o.value_datetime<= :end order by o.value_datetime");
	    cohortquery.addParameter(new Parameter("start","start",Date.class));
	    cohortquery.addParameter(new Parameter("end","end",Date.class));	    
	    //cohortquery.addParameter(new Parameter("endDate","endDate",Date.class));
	    return cohortquery;
    }	
	
}
