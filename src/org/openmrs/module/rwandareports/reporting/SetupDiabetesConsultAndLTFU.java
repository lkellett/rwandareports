package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateDiff.DateDiffType;
import org.openmrs.module.rwandareports.customcalculator.DiabetesAlerts;
import org.openmrs.module.rwandareports.customcalculator.OnInsulin;
import org.openmrs.module.rwandareports.filter.AccompagnateurStatusFilter;
import org.openmrs.module.rwandareports.filter.DrugDosageFrequencyFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupDiabetesConsultAndLTFU {
	protected final static Log log = LogFactory.getLog(SetupDiabetesConsultAndLTFU.class);

	Helper h = new Helper();
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	private List<Form> diabetesRendezvousForms = new ArrayList<Form>(); //Diabetes forms which can be used to set a next return visit date
	
	//properties retrieved from global variables
	private Program diabetesProgram;
	private List<EncounterType> diabetesEncouters;
		
	public void setup() throws Exception {
		setupPrograms();
		
		ReportDefinition consultReportDefinition = createConsultReportDefinition();	
		ReportDefinition ltfuReportDefinition = createLTFUReportDefinition();
		
		ReportDesign consultReporDesign = h.createRowPerPatientXlsOverviewReportDesign(consultReportDefinition, "DiabetesConsultSheet.xls","DiabetesConsultSheet.xls_", null);	
		ReportDesign ltfuReporDesign = h.createRowPerPatientXlsOverviewReportDesign(ltfuReportDefinition, "DiabetesLTFUSheet.xls","DiabetesLTFUSheet.xls_", null);	
		
		Properties consultProps = new Properties();
		consultProps.put("repeatingSections", "sheet:1,row:9,dataset:dataset1");
		
		Properties ltfuProps = new Properties();
		ltfuProps.put("repeatingSections", "sheet:1,row:8,dataset:dataset2");
		
		consultReporDesign.setProperties(consultProps);
		ltfuReporDesign.setProperties(ltfuProps);
		
		h.saveReportDesign(consultReporDesign);
		h.saveReportDesign(ltfuReporDesign);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("DiabetesConsultSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
			if ("DiabetesLTFUSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Diabetes Consult");
		h.purgeReportDefinition("Diabetes Late Visit");
		
	}
	
	private ReportDefinition createConsultReportDefinition() {

		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Diabetes Consult");	
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));	
		reportDefinition.addParameter(new Parameter("endDate", "Monday", Date.class));
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),
			    ParameterizableUtil.createParameterMappings("location=${location}"));

		createConsultDataSetDefinition (reportDefinition,diabetesProgram);	
		h.saveReportDefinition(reportDefinition);

		return reportDefinition;
	}
	
	private ReportDefinition createLTFUReportDefinition() {

		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Diabetes Late Visit");	
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));	
		reportDefinition.addParameter(new Parameter("endDate", "Date", Date.class));
		createLTFUDataSetDefinition(reportDefinition,diabetesProgram);	

		h.saveReportDefinition(reportDefinition);

		return reportDefinition;
	}
	
	private void createConsultDataSetDefinition(ReportDefinition reportDefinition,Program program) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName("Diabetes Consult Dataset");
		dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition.addParameter(new Parameter("endDate", "enDate", Date.class));
		
		//Add filters
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate(program.getName()+"Cohort", program), ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		dataSetDefinition.addFilter(Cohorts.getMondayToSundayPatientReturnVisit(diabetesRendezvousForms), ParameterizableUtil.createParameterMappings("end=${endDate+7d},start=${endDate}"));
				
		//Add Columns
         
        addCommonColumns(dataSetDefinition);
        
		dataSetDefinition.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());
		dataSetDefinition.addColumn(RowPerPatientColumns.getCurrentDiabetesOrders("Regimen", "@ddMMMyy", new DrugDosageFrequencyFilter()), new HashMap<String, Object>());
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("Has accompagnateur", new AccompagnateurStatusFilter()), new HashMap<String, Object>());
		
		//Calculation definitions
				
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(RowPerPatientColumns.getMostRecentHbA1c("RecentHbA1c", "@ddMMMyy"), new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(RowPerPatientColumns.getMostRecentCreatinine("RecentCreatinine", "@ddMMMyy"), new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(RowPerPatientColumns.getMostRecentSBP("RecentSBP", "@ddMMMyy"), new HashMap<String, Object>());
		alert.setCalculator(new DiabetesAlerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());
				
		CustomCalculationBasedOnMultiplePatientDataDefinitions onInsulin = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		onInsulin.addPatientDataToBeEvaluated(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());
		onInsulin.setName("onInsuline");
		onInsulin.setCalculator(new OnInsulin(diabetesEncouters));
		dataSetDefinition.addColumn(onInsulin, new HashMap<String, Object>());
								
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("dataset1", dataSetDefinition, mappings);
		
		
	}
	
	private void createLTFUDataSetDefinition(ReportDefinition reportDefinition,Program program) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName("Diabetes LTFU");
		dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition.addParameter(new Parameter("endDate", "enDate", Date.class));
		
		//Add filters (patients enrolled in the diabetes program)
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate(program.getName()+"Cohort", program), ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		//patients with late visits
		dataSetDefinition.addFilter(Cohorts.getPatientsWithLateVisitBasedOnReturnDateConcept("patientsWithoutDiabetesEncounters", gp.getEncounterType(GlobalPropertiesManagement.DIABETES_VISIT)),ParameterizableUtil.createParameterMappings("endDate=${endDate-7d}"));	
		
		//Add Columns
		addCommonColumns(dataSetDefinition);
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getGender("Sex"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentPatientPhoneNumber("Phone Number",null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfBirth("Date of Birth", null, null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("Date of missed appointment", null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("Address", true, true, true, true), new HashMap<String, Object>());
						
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("AccompName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDifferenceSinceLastEncounter("Days since last Visit", diabetesEncouters, DateDiffType.DAYS), new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("dataset2", dataSetDefinition, mappings);
		
	}


	private void setupPrograms() {
		diabetesRendezvousForms.add(gp.getForm(GlobalPropertiesManagement.DIABETES_DDB_FORM)) ;
		diabetesRendezvousForms.add(gp.getForm(GlobalPropertiesManagement.DIABETES_RDV_FORM)) ;
		diabetesProgram = gp.getProgram(GlobalPropertiesManagement.DM_PROGRAM);
		diabetesEncouters = gp.getEncounterTypeList(GlobalPropertiesManagement.DIABETES_VISIT);
	}
	
	//Add common columns for the two datasets
	private void addCommonColumns(RowPerPatientDataSetDefinition dataSetDefinition){
		
        dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
     	
     	dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
     	
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentHbA1c("RecentHbA1c", "@ddMMMyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentCreatinine("RecentCreatinine", "@ddMMMyy"), new HashMap<String, Object>());
	}
	
}
