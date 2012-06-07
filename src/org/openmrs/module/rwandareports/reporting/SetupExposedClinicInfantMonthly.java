package org.openmrs.module.rwandareports.reporting;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InverseCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateDiff;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfBirthShowingEstimation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfNextTestDueFromBirth;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentEncounterType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateDiff.DateDiffType;
import org.openmrs.module.rwandareports.filter.BorFStateFilter;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.filter.LastEncounterFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupExposedClinicInfantMonthly {
	
	protected final static Log log = LogFactory.getLog(SetupExposedClinicInfantMonthly.class);
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//Properties retrieved from global variables
	private Program pmtctinfantProgram;
	private ProgramWorkflow feedingState;
	private List<EncounterType>exposedInfantEncounter;
	private EncounterType exposedInfantEncountertype;
    private Concept cotrimoxazole;
    private Concept nevirapine;
    
    public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "ExposedClinicalinfantMonthly.xls",
		    "ExposedClinicalinfantMonthly.xls_", null);
		
		Properties props = new Properties();
		props.put(
		    "repeatingSections",
		    "sheet:1,row:9,dataset:LatePcrTest|sheet:2,row:9,dataset:Latesero|sheet:3,row:9,dataset:LostoFolowup|sheet:4,row:9,dataset:Less6wNotonNvp|sheet:5,row:9,dataset:More6wNotBactrim");
		
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("ExposedClinicalinfantMonthly".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("PMTCT Combined Clinic Infant Monthly Report");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("PMTCT Combined Clinic Infant Monthly Report");
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
				dataSetDefinition1.setName("Patients With Late PCR test dataSetDefinition");
				
				RowPerPatientDataSetDefinition dataSetDefinition2 = new RowPerPatientDataSetDefinition();
				dataSetDefinition2.setName("Patients with Late Sero test dataSetDefinition");
				
				RowPerPatientDataSetDefinition dataSetDefinition3 = new RowPerPatientDataSetDefinition();
				dataSetDefinition3.setName("Patients with not exposed encounter more than 2 months dataSetDefinition");
	
				RowPerPatientDataSetDefinition dataSetDefinition4 = new RowPerPatientDataSetDefinition();
				dataSetDefinition4.setName("Patients less than 6 weeks on NVP dataSetDefinition");
				
				RowPerPatientDataSetDefinition dataSetDefinition5 = new RowPerPatientDataSetDefinition();
				dataSetDefinition5.setName("Patients more than 6 weeks on Bactrim dataSetDefinition");
	
	
				SqlCohortDefinition patientsNotVoided = Cohorts.createPatientsNotVoided();
				dataSetDefinition1.addFilter(patientsNotVoided, new HashMap<String, Object>());
				dataSetDefinition2.addFilter(patientsNotVoided, new HashMap<String, Object>());
				dataSetDefinition3.addFilter(patientsNotVoided, new HashMap<String, Object>());
				dataSetDefinition4.addFilter(patientsNotVoided, new HashMap<String, Object>());
				dataSetDefinition5.addFilter(patientsNotVoided, new HashMap<String, Object>());
				
				dataSetDefinition1.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in "+pmtctinfantProgram.getName(), pmtctinfantProgram), ParameterizableUtil.createParameterMappings("onDate=${now}"));
				dataSetDefinition2.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in "+pmtctinfantProgram.getName(), pmtctinfantProgram), ParameterizableUtil.createParameterMappings("onDate=${now}"));
				dataSetDefinition3.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in "+pmtctinfantProgram.getName(), pmtctinfantProgram), ParameterizableUtil.createParameterMappings("onDate=${now}"));
				dataSetDefinition4.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in "+pmtctinfantProgram.getName(), pmtctinfantProgram), ParameterizableUtil.createParameterMappings("onDate=${now}"));
				dataSetDefinition5.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in "+pmtctinfantProgram.getName(), pmtctinfantProgram), ParameterizableUtil.createParameterMappings("onDate=${now}"));
				  
               //==================================================================
			  //  1. Patients without PCR and with at least 12 weeks of age
			  //===================================================================
			    
				AgeCohortDefinition at12WeeksOfageorLater = new AgeCohortDefinition();
				at12WeeksOfageorLater.setName("at12WeeksOfageorLater");
				at12WeeksOfageorLater.setMinAge(12);
				at12WeeksOfageorLater.setMinAgeUnit(DurationUnit.WEEKS);
				at12WeeksOfageorLater.addParameter(new Parameter("effectiveDate","effectiveDate", Date.class));
				
				CodedObsCohortDefinition patientWithPcrTest = new CodedObsCohortDefinition();
				patientWithPcrTest.setName("patientWithPcrTest");
				patientWithPcrTest.setTimeModifier(TimeModifier.ANY);
				patientWithPcrTest.setQuestion(Context.getConceptService().getConceptByName("HIV PCR"));
				
				InverseCohortDefinition patientsWithoutPcr=new InverseCohortDefinition(patientWithPcrTest);
				patientsWithoutPcr.setName("patientsWithoutPcr");

				dataSetDefinition1.addFilter(patientsWithoutPcr, null);
				dataSetDefinition1.addFilter(at12WeeksOfageorLater,ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}"));
			
				  //==================================================================
				  //  2. Patients without serotest and with at least 10 months of age
				  //===================================================================
				
				AgeCohortDefinition atleast10monthsOfAge = new AgeCohortDefinition();
				atleast10monthsOfAge.setName("at9monthsOfAge");
				atleast10monthsOfAge.setMinAge(10);
				atleast10monthsOfAge.setMinAgeUnit(DurationUnit.MONTHS);
				atleast10monthsOfAge.addParameter(new Parameter("effectiveDate","effectiveDate", Date.class));
				
				CodedObsCohortDefinition patientWithSeroTest = new CodedObsCohortDefinition();
				patientWithSeroTest.setName("patientWithSeroTest");
				patientWithSeroTest.setTimeModifier(TimeModifier.ANY);
				patientWithSeroTest.setQuestion(Context.getConceptService().getConceptByName("HIV RAPID TEST, QUALITATIVE"));
				
				InverseCohortDefinition patientsWithoutSero=new InverseCohortDefinition(patientWithSeroTest);
				patientsWithoutSero.setName("patientsWithoutSero");

				dataSetDefinition2.addFilter(patientsWithoutSero, null);
				dataSetDefinition2.addFilter(atleast10monthsOfAge,ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}"));
				
				//========================================================================================
				//  3. Patients enrolled in pmtct infant pro and without encounter in more than 2 months
				//=======================================================================================
				 
				SqlCohortDefinition patientsInPMTCTCforLongtime=new SqlCohortDefinition("select DISTINCT patient_id FROM patient_program pp,program p WHERE pp.program_id=p.program_id AND p.name='"+pmtctinfantProgram.getName()+"' AND DATEDIFF(:onDate,pp.date_enrolled) >= 60 AND pp.voided=false AND pp.date_completed is null");
				patientsInPMTCTCforLongtime.addParameter(new Parameter("onDate", "onDate",Date.class));
				
			    SqlCohortDefinition patientsWithExposedInfantEncounter = new SqlCohortDefinition("select DISTINCT p.patient_id FROM encounter en, patient p WHERE en.patient_id=p.patient_id AND en.encounter_type="+exposedInfantEncountertype.getEncounterTypeId()+" AND DATEDIFF(:encDate,en.encounter_datetime) >= 60 AND en.void_reason is null AND p.void_reason is null");
				patientsWithExposedInfantEncounter.addParameter(new Parameter("encDate", "encDate",Date.class));
				
				CompositionCohortDefinition patientsWithouthEncInProgram = new CompositionCohortDefinition();
				patientsWithouthEncInProgram.setName("patientsWithouthEncInProgram");
				patientsWithouthEncInProgram.addParameter(new Parameter("onDate", "onDate", Date.class));
				patientsWithouthEncInProgram.addParameter(new Parameter("encDate", "encDate", Date.class));
				patientsWithouthEncInProgram.getSearches().put("1",new Mapped<CohortDefinition>(patientsInPMTCTCforLongtime, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
				patientsWithouthEncInProgram.getSearches().put("2",new Mapped<CohortDefinition>(patientsWithExposedInfantEncounter, ParameterizableUtil.createParameterMappings("encDate=${encDate}")));
				patientsWithouthEncInProgram.setCompositionString("1 AND 2 ");
				
				dataSetDefinition3.addFilter(patientsWithouthEncInProgram,ParameterizableUtil.createParameterMappings("onDate=${endDate},encDate=${endDate}"));
				
				//=======================================================================================
				//  4. Infants not on NVP susp at less than 6 weeks
				//=======================================================================================
				
				AgeCohortDefinition less6WeeksOfAge = new AgeCohortDefinition();
				less6WeeksOfAge.setName("less6WeeksOfAge");
				less6WeeksOfAge.setMinAge(1);
				less6WeeksOfAge.setMinAgeUnit(DurationUnit.WEEKS);
				less6WeeksOfAge.setMaxAge(5);
				less6WeeksOfAge.setMaxAgeUnit(DurationUnit.WEEKS);
				less6WeeksOfAge.addParameter(new Parameter("effectiveDate","effectiveDate", Date.class));
				
				// On Nevirapine
	
				SqlCohortDefinition onNevirapineOntime = new SqlCohortDefinition("select DISTINCT o.patient_id from orders o,concept c WHERE o.concept_id=c.concept_id AND c.concept_id="+nevirapine.getConceptId()+" AND o.discontinued=0 AND o.voided=0 AND o.start_date<= :onDate");
				onNevirapineOntime.addParameter(new Parameter("onDate", "onDate",Date.class));
				
				CompositionCohortDefinition atlessthan6weeksOnNvpSus = new CompositionCohortDefinition();
				atlessthan6weeksOnNvpSus.setName("atlessthan6weeksOnNvpSus");
				atlessthan6weeksOnNvpSus.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
				atlessthan6weeksOnNvpSus.addParameter(new Parameter("onDate", "onDate", Date.class));
				atlessthan6weeksOnNvpSus.getSearches().put("1",new Mapped<CohortDefinition>(less6WeeksOfAge, ParameterizableUtil.createParameterMappings("effectiveDate=${effectiveDate}")));
				atlessthan6weeksOnNvpSus.getSearches().put("2",new Mapped<CohortDefinition>(onNevirapineOntime, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
				atlessthan6weeksOnNvpSus.setCompositionString("1 AND (NOT 2) ");
				
                dataSetDefinition4.addFilter(atlessthan6weeksOnNvpSus,ParameterizableUtil.createParameterMappings("effectiveDate=${endDate},onDate=${endDate}"));
				
            //==============================================================================
			//  5. Infants not on Bactrim at more than 6 weeks
			//==============================================================================
                
                AgeCohortDefinition atleast6WeeksOfAge = new AgeCohortDefinition();
                atleast6WeeksOfAge.setName("atleast6WeeksOfAge");
                atleast6WeeksOfAge.setMinAge(6);
                atleast6WeeksOfAge.setMinAgeUnit(DurationUnit.WEEKS);
                atleast6WeeksOfAge.addParameter(new Parameter("effectiveDate","effectiveDate", Date.class));
                
             // On Bactrim
				SqlCohortDefinition infantsonBactrim = new SqlCohortDefinition("select DISTINCT o.patient_id from orders o,concept c WHERE o.concept_id=c.concept_id AND c.concept_id="+cotrimoxazole.getConceptId()+" AND o.discontinued=0 AND o.voided=0 AND o.start_date<= :onDate");
				infantsonBactrim.addParameter(new Parameter("onDate", "onDate",Date.class));
				
				CompositionCohortDefinition morethan6weeksOnBactrim = new CompositionCohortDefinition();
				morethan6weeksOnBactrim.setName("morethan6weeksOnBactrim");
				morethan6weeksOnBactrim.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
				morethan6weeksOnBactrim.addParameter(new Parameter("onDate", "onDate", Date.class));
				morethan6weeksOnBactrim.getSearches().put("1",new Mapped<CohortDefinition>(atleast6WeeksOfAge, ParameterizableUtil.createParameterMappings("effectiveDate=${effectiveDate}")));
				morethan6weeksOnBactrim.getSearches().put("2",new Mapped<CohortDefinition>(infantsonBactrim, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
				morethan6weeksOnBactrim.setCompositionString("1 AND (NOT 2) ");
				
                dataSetDefinition5.addFilter(morethan6weeksOnBactrim,ParameterizableUtil.createParameterMappings("effectiveDate=${endDate},onDate=${endDate}"));
				
						
		//==================================================================
        //                 Columns of report settings
        //==================================================================
	    MultiplePatientDataDefinitions imbType = RowPerPatientColumns.getIMBId("IMB ID");
		dataSetDefinition1.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(imbType, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(imbType, new HashMap<String, Object>());
		        		
		PatientProperty givenName = RowPerPatientColumns.getFirstNameColumn("familyName");
        dataSetDefinition1.addColumn(givenName, new HashMap<String, Object>());
        dataSetDefinition2.addColumn(givenName, new HashMap<String, Object>());
        dataSetDefinition3.addColumn(givenName, new HashMap<String, Object>());
        dataSetDefinition4.addColumn(givenName, new HashMap<String, Object>());
        dataSetDefinition5.addColumn(givenName, new HashMap<String, Object>());
        
        PatientProperty familyName = RowPerPatientColumns.getFamilyNameColumn("givenName");
        dataSetDefinition1.addColumn(familyName, new HashMap<String, Object>());
        dataSetDefinition2.addColumn(familyName, new HashMap<String, Object>());
        dataSetDefinition3.addColumn(familyName, new HashMap<String, Object>());
        dataSetDefinition4.addColumn(familyName, new HashMap<String, Object>());
        dataSetDefinition5.addColumn(familyName, new HashMap<String, Object>());
       
        DateOfBirthShowingEstimation birthdate = RowPerPatientColumns.getDateOfBirth("Date of Birth", null, null);
        dataSetDefinition1.addColumn(birthdate, new HashMap<String, Object>());
        dataSetDefinition2.addColumn(birthdate, new HashMap<String, Object>());
        dataSetDefinition3.addColumn(birthdate, new HashMap<String, Object>());
        dataSetDefinition4.addColumn(birthdate, new HashMap<String, Object>());
        dataSetDefinition5.addColumn(birthdate, new HashMap<String, Object>());
        
		dataSetDefinition1.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", null, null),new HashMap<String, Object>());
		dataSetDefinition2.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", null, null),new HashMap<String, Object>());
		dataSetDefinition3.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", null, null),new HashMap<String, Object>());
		dataSetDefinition4.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", null, null),new HashMap<String, Object>());
		dataSetDefinition5.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", null, null),new HashMap<String, Object>());
		
        dataSetDefinition1.addColumn(RowPerPatientColumns.getAgeInMonths("ageInMonths"), new HashMap<String, Object>());
        dataSetDefinition1.addColumn(RowPerPatientColumns.getStateOfPatient("FeedingGroup", pmtctinfantProgram, feedingState, new BorFStateFilter()),new HashMap<String, Object>());
        dataSetDefinition1.addColumn(RowPerPatientColumns.getMotherRelationship("MotherName"), new HashMap<String, Object>());
        
        dataSetDefinition2.addColumn(RowPerPatientColumns.getAgeInMonths("ageInMonths"), new HashMap<String, Object>());
        dataSetDefinition2.addColumn(RowPerPatientColumns.getStateOfPatient("FeedingGroup", pmtctinfantProgram, feedingState, new BorFStateFilter()),new HashMap<String, Object>());
        dataSetDefinition2.addColumn(RowPerPatientColumns.getMotherRelationship("MotherName"), new HashMap<String, Object>());
        
        dataSetDefinition3.addColumn(RowPerPatientColumns.getAgeInMonths("ageInMonths"), new HashMap<String, Object>());
        dataSetDefinition3.addColumn(RowPerPatientColumns.getStateOfPatient("FeedingGroup", pmtctinfantProgram, feedingState, new BorFStateFilter()),new HashMap<String, Object>());
        dataSetDefinition3.addColumn(RowPerPatientColumns.getMotherRelationship("MotherName"), new HashMap<String, Object>());
        
        dataSetDefinition4.addColumn(RowPerPatientColumns.getAgeInMonths("ageInMonths"), new HashMap<String, Object>());
        dataSetDefinition4.addColumn(RowPerPatientColumns.getStateOfPatient("FeedingGroup", pmtctinfantProgram, feedingState, new BorFStateFilter()),new HashMap<String, Object>());
        dataSetDefinition4.addColumn(RowPerPatientColumns.getMotherRelationship("MotherName"), new HashMap<String, Object>());
        
        dataSetDefinition5.addColumn(RowPerPatientColumns.getAgeInMonths("ageInMonths"), new HashMap<String, Object>());
        dataSetDefinition5.addColumn(RowPerPatientColumns.getStateOfPatient("FeedingGroup", pmtctinfantProgram, feedingState, new BorFStateFilter()),new HashMap<String, Object>());
        dataSetDefinition5.addColumn(RowPerPatientColumns.getMotherRelationship("MotherName"), new HashMap<String, Object>());
        
        DateOfNextTestDueFromBirth firstDbsat6weeks = new DateOfNextTestDueFromBirth();
        firstDbsat6weeks.setTimeUnit(Calendar.MONTH);
        firstDbsat6weeks.setTimeIncrement(9);
        firstDbsat6weeks.setName("firstDbsat6weeks");
        firstDbsat6weeks.setDateFormat("ddMMMyy");
		dataSetDefinition1.addColumn(firstDbsat6weeks, new HashMap<String, Object>());
		
        DateOfNextTestDueFromBirth firstDbs = new DateOfNextTestDueFromBirth();
		firstDbs.setTimeUnit(Calendar.MONTH);
		firstDbs.setTimeIncrement(9);
		firstDbs.setName("firstDBSDue");
		firstDbs.setDateFormat("ddMMMyy");
		dataSetDefinition2.addColumn(firstDbs, new HashMap<String, Object>());
		
		RecentEncounterType lastEncounterType = RowPerPatientColumns.getRecentEncounterType("Last visit type",exposedInfantEncounter, new LastEncounterFilter());
		dataSetDefinition1.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition2.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition3.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition4.addColumn(lastEncounterType, new HashMap<String, Object>());
		dataSetDefinition5.addColumn(lastEncounterType, new HashMap<String, Object>());
		
		DateDiff lateVisitInMonth = RowPerPatientColumns.getDifferenceSinceLastEncounter("Late visit in months", exposedInfantEncounter, DateDiffType.MONTHS);
		lateVisitInMonth.addParameter(new Parameter("endDate", "endDate", Date.class));
	    dataSetDefinition3.addColumn(lateVisitInMonth, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
	
        PatientAddress address = RowPerPatientColumns.getPatientAddress("Address", true, true, true, true);
        dataSetDefinition1.addColumn(address, new HashMap<String, Object>());
        dataSetDefinition2.addColumn(address, new HashMap<String, Object>());
        dataSetDefinition3.addColumn(address, new HashMap<String, Object>());
        dataSetDefinition4.addColumn(address, new HashMap<String, Object>());
        dataSetDefinition5.addColumn(address, new HashMap<String, Object>());
        
        PatientRelationship accompagnateur = RowPerPatientColumns.getAccompRelationship("AccompName");
        dataSetDefinition1.addColumn(accompagnateur, new HashMap<String, Object>());
        dataSetDefinition2.addColumn(accompagnateur, new HashMap<String, Object>());
        dataSetDefinition3.addColumn(accompagnateur, new HashMap<String, Object>());
        dataSetDefinition4.addColumn(accompagnateur, new HashMap<String, Object>());
        dataSetDefinition5.addColumn(accompagnateur, new HashMap<String, Object>());
        
        dataSetDefinition1.addParameter(new Parameter("location", "Location", Location.class));
        dataSetDefinition2.addParameter(new Parameter("location", "Location", Location.class));
        dataSetDefinition3.addParameter(new Parameter("location", "Location", Location.class));
        dataSetDefinition4.addParameter(new Parameter("location", "Location", Location.class));
        dataSetDefinition5.addParameter(new Parameter("location", "Location", Location.class));
        
        dataSetDefinition1.addParameter(new Parameter("endDate", "End Date", Date.class));
        dataSetDefinition2.addParameter(new Parameter("endDate", "End Date", Date.class));
        dataSetDefinition3.addParameter(new Parameter("endDate", "End Date", Date.class));
        dataSetDefinition4.addParameter(new Parameter("endDate", "End Date", Date.class));
        dataSetDefinition5.addParameter(new Parameter("endDate", "End Date", Date.class));
        
        Map<String, Object> mappings = new HashMap<String, Object>();
        mappings.put("location", "${location}");
        mappings.put("endDate", "${endDate}");
		
        reportDefinition.addDataSetDefinition("LatePcrTest", dataSetDefinition1, mappings);
        reportDefinition.addDataSetDefinition("Latesero", dataSetDefinition2, mappings);
        reportDefinition.addDataSetDefinition("LostoFolowup", dataSetDefinition3, mappings);
        reportDefinition.addDataSetDefinition("Less6wNotonNvp", dataSetDefinition4, mappings);
        reportDefinition.addDataSetDefinition("More6wNotBactrim", dataSetDefinition5, mappings);
        
		
	}
	
	private void setupProperties() {
		
		pmtctinfantProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM);
		feedingState = gp.getProgramWorkflow(GlobalPropertiesManagement.FEEDING_GROUP_WORKFLOW, GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM);
		exposedInfantEncounter = gp.getEncounterTypeList(GlobalPropertiesManagement.EXPOSED_INFANT_ENCOUNTER);
		exposedInfantEncountertype=gp.getEncounterType(GlobalPropertiesManagement.EXPOSED_INFANT_ENCOUNTER);
		cotrimoxazole=gp.getConcept(GlobalPropertiesManagement.COTRIMOXAZOLE_DRUG);
		nevirapine=gp.getConcept(GlobalPropertiesManagement.NEVIRAPINE_DRUG);
		
	} 
	
	
}