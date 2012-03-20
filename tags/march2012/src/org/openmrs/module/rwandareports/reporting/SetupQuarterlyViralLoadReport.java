package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.Indicators;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

public class SetupQuarterlyViralLoadReport {
	
	public Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties
	private Program hivProgram;
	
	private Program pediHivProgram;
	
	private Program pmtctProgram;
	
	private Program pmtctCombinedMotherProgram;
	
	private ProgramWorkflowState hivArt;
	
	private ProgramWorkflowState pediArt;
	
	private ProgramWorkflowState pmtctArt;
	
	private List<Program> hivPrograms = new ArrayList<Program>();
	
	private List<ProgramWorkflowState> artWorkflowStates = new ArrayList<ProgramWorkflowState>();
	
	private List<ProgramWorkflowState> artWorkflowStatesIncPMTCT = new ArrayList<ProgramWorkflowState>();
	
	private List<String> onOrAfterOnOrBefore = new ArrayList<String>();
	
	private Concept viralLoadConcept;
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = new ReportDefinition();
    	rd.addParameter(new Parameter("endDate", "End Date", Date.class));
    	
    	Properties properties = new Properties();
    	properties.setProperty("hierarchyFields", "countyDistrict:District");
    	rd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
    	
    	rd.setName("PIH Quarterly Viral Load");
    	
    	rd.addDataSetDefinition(createDataSet(),
    	    ParameterizableUtil.createParameterMappings("endDate=${endDate},location=${location}"));
    	
    	h.saveReportDefinition(rd);
		
    	ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd,
		    "PIH_Quarterly_Viral_Load.xls", "PIH Quarterly Viral Load (Excel)", null);
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,dataset:Data Set");
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PIH Quarterly Viral Load (Excel)".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("PIH Quarterly Viral Load");
		
	}
	
	public LocationHierachyIndicatorDataSetDefinition createDataSet() {
		
		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(createBaseDataSet());
		ldsd.setName("Data Set");
		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		return ldsd;
	}
	
	
	private CohortIndicatorDataSetDefinition createBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Cohort Data Set");
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		createIndicators(dsd);
		return dsd;
	}
	
	private void createIndicators(CohortIndicatorDataSetDefinition dsd) {
		
		InProgramCohortDefinition inAnyHIVProgram = Cohorts.createInProgramParameterizableByDate(
		    "hivQD: In All HIV Programs", hivPrograms, "onDate");
		
		AgeCohortDefinition over15Cohort = Cohorts.createOver15AgeCohort("ageQD: Over 15");
		AgeCohortDefinition under15Cohort = Cohorts.createUnder15AgeCohort("ageQD: Under 15");
		
		InStateCohortDefinition artOnOrBefore = Cohorts.createInCurrentState("hivQD: ever on ART", artWorkflowStates,
		    "onOrBefore");
		
		InStateCohortDefinition artCurrently = Cohorts.createInCurrentState("hivQD: currently on ART", artWorkflowStatesIncPMTCT);
		
		NumericObsCohortDefinition viralLoad = Cohorts.createNumericObsCohortDefinition("obsQD: Viral Load recorded",
		    onOrAfterOnOrBefore, viralLoadConcept, 0, null, TimeModifier.ANY);;
		
		NumericObsCohortDefinition viralLoadFailure = Cohorts.createNumericObsCohortDefinition("obsQD: weight recorded",
		    onOrAfterOnOrBefore, viralLoadConcept, 10000, RangeComparator.GREATER_EQUAL, TimeModifier.ANY);
		
		SqlCohortDefinition patientWithAccompagnatuer = new SqlCohortDefinition();
		patientWithAccompagnatuer.setName("patientsWithAccompagnatuer");
		patientWithAccompagnatuer
		        .setQuery("select person_b from relationship where relationship = 1 and voided = 0 and date_created < :endDate");
		patientWithAccompagnatuer.addParameter(new Parameter("endDate", "endDate", Date.class));

		
		CompositionCohortDefinition inHIVUnder15 = new CompositionCohortDefinition();
		inHIVUnder15.setName("hivQD: In all programs under 15 and on ART more than 12 months");
		inHIVUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		inHIVUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		inHIVUnder15.getSearches().put("2",
		    new Mapped(inAnyHIVProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		inHIVUnder15.getSearches().put("3",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		inHIVUnder15.getSearches().put("4",
		    new Mapped(artOnOrBefore, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate-1y}")));
		inHIVUnder15.setCompositionString("NOT 1 AND 2 AND 3 AND 4");
		
		CompositionCohortDefinition inHIVOver15 = new CompositionCohortDefinition();
		inHIVOver15.setName("hivQD: In all programs over 15 and on ART more than 12 months");
		inHIVOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		inHIVOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		inHIVOver15.getSearches().put("2",
		    new Mapped(inAnyHIVProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		inHIVOver15.getSearches().put("3",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		inHIVOver15.getSearches().put("4",
		    new Mapped(artOnOrBefore, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate-1y}")));
		inHIVOver15.setCompositionString("1 AND 2 AND 3 AND 4");
		
		CompositionCohortDefinition eligibleWithViralU15 = new CompositionCohortDefinition();
		eligibleWithViralU15.setName("hivQD: eligible with viral load under 15");
		eligibleWithViralU15.addParameter(new Parameter("endDate", "endDate", Date.class));
		eligibleWithViralU15.getSearches().put("1",
		    new Mapped(inHIVUnder15, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		eligibleWithViralU15.getSearches().put(
		    "2",
		    new Mapped(viralLoad, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-1y}")));
		eligibleWithViralU15.setCompositionString("1 AND 2");
		
		CompositionCohortDefinition eligibleWithViralO15 = new CompositionCohortDefinition();
		eligibleWithViralO15.setName("hivQD: eligible with viral load over 15");
		eligibleWithViralO15.addParameter(new Parameter("endDate", "endDate", Date.class));
		eligibleWithViralO15.getSearches().put("1",
		    new Mapped(inHIVOver15, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		eligibleWithViralO15.getSearches().put(
		    "2",
		    new Mapped(viralLoad, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-1y}")));
		eligibleWithViralO15.setCompositionString("1 AND 2");
		
		CompositionCohortDefinition eligibleWithViralFailureU15 = new CompositionCohortDefinition();
		eligibleWithViralFailureU15.setName("hivQD: eligible with viral load failure under 15");
		eligibleWithViralFailureU15.addParameter(new Parameter("endDate", "endDate", Date.class));
		eligibleWithViralFailureU15.getSearches().put("1",
		    new Mapped(inHIVUnder15, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		eligibleWithViralFailureU15.getSearches().put(
		    "2",
		    new Mapped(viralLoadFailure, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-1y}")));
		eligibleWithViralFailureU15.setCompositionString("1 AND 2");
		
		CompositionCohortDefinition eligibleWithViralFailureO15 = new CompositionCohortDefinition();
		eligibleWithViralFailureO15.setName("hivQD: eligible with viral load failure over 15");
		eligibleWithViralFailureO15.addParameter(new Parameter("endDate", "endDate", Date.class));
		eligibleWithViralFailureO15.getSearches().put("1",
		    new Mapped(inHIVOver15, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		eligibleWithViralFailureO15.getSearches().put(
		    "2",
		    new Mapped(viralLoadFailure, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-1y}")));
		eligibleWithViralFailureO15.setCompositionString("1 AND 2");
		
		CompositionCohortDefinition accompUnder15 = new CompositionCohortDefinition();
		accompUnder15.setName("hivQD: has accompagnatuer and under 15");
		accompUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		accompUnder15.getSearches().put("1",
		    new Mapped(inHIVUnder15, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		accompUnder15.getSearches().put(
		    "2",
		    new Mapped(patientWithAccompagnatuer, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")));
		accompUnder15.setCompositionString("1 AND 2");
		
		CompositionCohortDefinition accompOver15 = new CompositionCohortDefinition();
		accompOver15.setName("hivQD: has accompagnatuer and under 15");
		accompOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		accompOver15.getSearches().put("1",
		    new Mapped(inHIVOver15, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		accompOver15.getSearches().put(
		    "2",
		    new Mapped(patientWithAccompagnatuer, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")));
		accompOver15.setCompositionString("1 AND 2");
		
		
		CohortIndicator one = Indicators.newCountIndicator("hivQD: In all programs under 15 and on ART more than 12 months_", inHIVUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator two = Indicators.newCountIndicator("hivQD: In all programs over 15 and on ART more than 12 months_", inHIVOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator three = Indicators.newCountIndicator("hivQD: eligible with viral load under 15_", eligibleWithViralU15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator four = Indicators.newCountIndicator("hivQD: eligible with viral load over 15_", eligibleWithViralO15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator five = Indicators.newCountIndicator("hivQD: eligible with viral load failure under 15_", eligibleWithViralFailureU15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator six = Indicators.newCountIndicator("hivQD: eligible with viral load failure over 15_", eligibleWithViralFailureO15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator seven = Indicators.newCountIndicator(
		    "hivQD: has accompagnatuer and under 15_", accompUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator eight = Indicators.newCountIndicator(
		    "hivQD: has accompagnatuer and under 15_", accompOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
		dsd.addColumn("1", "Eligible Over 15",
		    new Mapped(two, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("2", "Eligible Under 15",
		    new Mapped(one, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("4", "Viral Load Under 15",
		    new Mapped(three, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("3", "Viral Load Over 15",
		    new Mapped(four, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("6", "Viral Load Failure Over 15",
		    new Mapped(five, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("5", "Viral Load Failure Under 15",
		    new Mapped(six, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("8", "Accomp Under 15",
		    new Mapped(seven, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("7", "Accomp Over 15",
		    new Mapped(eight, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		
	}
	
	private void setUpProperties() {
		hivProgram = gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		pediHivProgram = gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		pmtctProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT);
		pmtctCombinedMotherProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_MOTHER_PROGRAM);
	
		pediArt = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		hivArt = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		pmtctArt = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PMTCT);
		
		
		hivPrograms.add(hivProgram);
		hivPrograms.add(pediHivProgram);
		hivPrograms.add(pmtctProgram);
		hivPrograms.add(pmtctCombinedMotherProgram);
		
		artWorkflowStates.add(pediArt);
		artWorkflowStates.add(hivArt);
		
		artWorkflowStatesIncPMTCT.add(pediArt);
		artWorkflowStatesIncPMTCT.add(hivArt);
		artWorkflowStatesIncPMTCT.add(pmtctArt);
	
		viralLoadConcept = gp.getConcept(GlobalPropertiesManagement.VIRAL_LOAD_TEST);
		
		onOrAfterOnOrBefore.add("onOrAfter");
		onOrAfterOnOrBefore.add("onOrBefore");
	}
}
