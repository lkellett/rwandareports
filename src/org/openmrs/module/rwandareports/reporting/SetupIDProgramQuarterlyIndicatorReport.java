package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.SetComparator;
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

public class SetupIDProgramQuarterlyIndicatorReport {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties
	private Program hivProgram;
	
	private Program pediHivProgram;
	
	private Program pmtctProgram;
	
	private Program pmtctCombinedMotherProgram;
	
	private ProgramWorkflowState hivPreArt;
	
	private ProgramWorkflowState pediPreArt;
	
	private ProgramWorkflowState hivArt;
	
	private ProgramWorkflowState pediArt;
	
	private List<Program> hivPrograms = new ArrayList<Program>();
	
	private List<Program> hivProgramsExcPMTCT = new ArrayList<Program>();
	
	private List<ProgramWorkflowState> preArtWorkflowStates = new ArrayList<ProgramWorkflowState>();
	
	private List<ProgramWorkflowState> artWorkflowStates = new ArrayList<ProgramWorkflowState>();
	
	private List<String> onOrAfterOnOrBefore = new ArrayList<String>();
	
	private List<EncounterType> hivEncounterTypes;
	
	private Concept cd4Concept;
	
	private Concept weightConcept;
	
	private EncounterType adultHIVEncounterType;
	
	private Program pmtctCombinedInfantProgram;
	
	private List<ProgramWorkflowState> feedingWorkflowStates = new ArrayList<ProgramWorkflowState>();
	
	private EncounterType exposedInfantEncountertype;
	
	private Concept hivPCR;
	
	private Concept hivPositive;
	
	private Concept seroTest;	
	private Concept ctx;
	private Concept nevirapine;
	
	private Concept methodOfFamilyPlanning;
	private Concept usingCondoms;
	private Concept naturalFamilyPlanning;
	private Concept viralLoadConcept;
	public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = createCrossSiteReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "HIVIndicator.xls", "HIVIndicators", null);
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,dataset:ID_Program_Quarterly_Individual_District_Indicator Data Set");
		design.setProperties(props);
		h.saveReportDesign(design);
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("HIVIndicators".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("ID Program Quarterly Indicators");
		
	}
	
	private ReportDefinition createCrossSiteReportDefinition() {
		// PIH Quarterly Cross Site Indicator Report
		ReportDefinition rd = new ReportDefinition();
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");
		rd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		
		rd.setName("ID Program Quarterly Indicators");
		
		rd.addDataSetDefinition(createDataSet(),
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));
		
		h.saveReportDefinition(rd);
		
		return rd;
	}
	
	private LocationHierachyIndicatorDataSetDefinition createDataSet() {
		
		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(createBaseDataSet());
		ldsd.setName("ID_Program_Quarterly_Individual_District_Indicator Data Set");
		ldsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		return ldsd;
	}
	
	private CohortIndicatorDataSetDefinition createBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("ID_Program_Quarterly_Individual_District_Indicator Cohort Data Set");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		createIndicators(dsd);
		return dsd;
	}
	
	private void createIndicators(CohortIndicatorDataSetDefinition dsd) {
		
		// HIV patients
		
		InProgramCohortDefinition inAnyHIVProgram = Cohorts.createInProgramParameterizableByDate(
		    "HIVQ: In All HIV Programs", hivPrograms, "onOrBefore");
		
		InProgramCohortDefinition inAnyHIVAfterStartDate = Cohorts.createInProgramParameterizableByDate(
		    "HIVQ: new Patients enrolled in HIV Program during period", hivPrograms, onOrAfterOnOrBefore);
		
		InProgramCohortDefinition inAdultOrPediHIVProgram = Cohorts.createInProgramParameterizableByDate(
		    "HIVQ: In AdultOrPedi HIV Programs", hivProgramsExcPMTCT, "onOrBefore");
		
		InProgramCohortDefinition inAdultOrPediHIVOnDateProgram = Cohorts.createInProgramParameterizableByDate(
		    "HIVQ: In AdultOrPedi HIV Programs on Date", hivProgramsExcPMTCT, "onDate");
		
		AgeCohortDefinition over15Cohort = Cohorts.createOver15AgeCohort("ageQD: Over 15");
		AgeCohortDefinition under15Cohort = Cohorts.createUnder15AgeCohort("ageQD: Under 15");
		
		InStateCohortDefinition preArt = Cohorts.createInCurrentState("HIVQ: preArt", preArtWorkflowStates);
		
		InStateCohortDefinition artOnOrBefore = Cohorts.createInCurrentState("HIVQ: ever on ART", artWorkflowStates,
		    "onOrBefore");
		
		InStateCohortDefinition artCurrently = Cohorts.createInCurrentState("HIVQ: currently on ART", artWorkflowStates);
		
		InStateCohortDefinition artStartedInPeriod = Cohorts.createInCurrentState("HIVQ: started on ART", artWorkflowStates,
		    onOrAfterOnOrBefore);
		
		EncounterCohortDefinition encounter = Cohorts.createEncounterParameterizedByDate("encounterQD: visit in period",
		    onOrAfterOnOrBefore, hivEncounterTypes);
		
		NumericObsCohortDefinition cd4 = Cohorts.createNumericObsCohortDefinition("obsQD: CD4 count recorded",
		    onOrAfterOnOrBefore, cd4Concept, 0, null, TimeModifier.ANY);
		
		NumericObsCohortDefinition weight = Cohorts.createNumericObsCohortDefinition("obsQD: weight recorded",
		    onOrAfterOnOrBefore, weightConcept, 0, null, TimeModifier.ANY);
		
		CompositionCohortDefinition inHIVUnder15 = new CompositionCohortDefinition();
		inHIVUnder15.setName("HIVQ: In all programs under 15");
		inHIVUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		inHIVUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		inHIVUnder15.getSearches().put("2",
		    new Mapped(inAnyHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		inHIVUnder15.setCompositionString("NOT 1 AND 2");
		
		CompositionCohortDefinition inHIVOver15 = new CompositionCohortDefinition();
		inHIVOver15.setName("HIVQ: In all programs over 15");
		inHIVOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		inHIVOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		inHIVOver15.getSearches().put("2",
		    new Mapped(inAnyHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		inHIVOver15.setCompositionString("1 AND 2");
		
		CompositionCohortDefinition newInHIVUnder15 = new CompositionCohortDefinition();
		newInHIVUnder15.setName("HIVQ: new in any Hiv program in period under 15");
		newInHIVUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		newInHIVUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		newInHIVUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		newInHIVUnder15.getSearches().put(
		    "2",
		    new Mapped(inAnyHIVAfterStartDate, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		newInHIVUnder15.getSearches().put("3",
		    new Mapped(inAnyHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${startDate-1d}")));
		newInHIVUnder15.setCompositionString("NOT 1 AND 2 AND NOT 3");
		
		CompositionCohortDefinition newInHIVOver15 = new CompositionCohortDefinition();
		newInHIVOver15.setName("HIVQ: new in any Hiv program in period over 15");
		newInHIVOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		newInHIVOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		newInHIVOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		newInHIVOver15.getSearches().put(
		    "2",
		    new Mapped(inAnyHIVAfterStartDate, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		newInHIVOver15.getSearches().put("3",
		    new Mapped(inAnyHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${startDate-1d}")));
		newInHIVOver15.setCompositionString("1 AND 2 AND NOT 3");
		
		CompositionCohortDefinition followingUnder15 = new CompositionCohortDefinition();
		followingUnder15.setName("HIVQ: preArt in Hiv program in period under 15");
		followingUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		followingUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		followingUnder15.getSearches().put("2",
		    new Mapped(preArt, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		followingUnder15.getSearches().put("3",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		followingUnder15.setCompositionString("NOT 1 AND 2 AND 3");
		
		CompositionCohortDefinition followingOver15 = new CompositionCohortDefinition();
		followingOver15.setName("HIVQ: preArt in Hiv program in period over 15");
		followingOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		followingOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		followingOver15.getSearches().put("2",
		    new Mapped(preArt, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		followingOver15.getSearches().put("3",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		followingOver15.setCompositionString("1 AND 2 AND 3");
		
		CompositionCohortDefinition preArtWithAVisitUnder15 = new CompositionCohortDefinition();
		preArtWithAVisitUnder15.setName("HIVQ: preArt in Hiv program with a visit in period - 3 months and under 15");
		preArtWithAVisitUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		preArtWithAVisitUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		preArtWithAVisitUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		preArtWithAVisitUnder15.getSearches().put("2",
		    new Mapped(preArt, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		preArtWithAVisitUnder15.getSearches().put(
		    "3",
		    new Mapped(encounter, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate-3m}")));
		preArtWithAVisitUnder15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		preArtWithAVisitUnder15.getSearches().put(
		    "5",
		    new Mapped(weight, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate-3m}")));
		preArtWithAVisitUnder15.setCompositionString("NOT 1 AND 2 AND 4 AND(3 OR 5)");
		
		CompositionCohortDefinition preArtActiveUnder15 = new CompositionCohortDefinition();
		preArtActiveUnder15.setName("HIVQ: Active preArt in Hiv program with a visit in period - 12 months and under 15");
		preArtActiveUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		preArtActiveUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		preArtActiveUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		preArtActiveUnder15.getSearches().put("2",
		    new Mapped(preArt, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		preArtActiveUnder15.getSearches().put(
		    "3",
		    new Mapped(encounter, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-12m}")));
		preArtActiveUnder15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		preArtActiveUnder15.getSearches().put(
		    "5",
		    new Mapped(weight, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate-3m}")));
		preArtActiveUnder15.setCompositionString("NOT 1 AND 2 AND 4 AND(3 OR 5)");
		
		CompositionCohortDefinition preArtActiveOver15 = new CompositionCohortDefinition();
		preArtActiveOver15.setName("HIVQ: Active preArt in Hiv program with a visit in period - 12 months and over 15");
		preArtActiveOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		preArtActiveOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		preArtActiveOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		preArtActiveOver15.getSearches().put("2",
		    new Mapped(preArt, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		preArtActiveOver15.getSearches().put(
		    "3",
		    new Mapped(encounter, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-12m}")));
		preArtActiveOver15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		preArtActiveOver15.getSearches().put(
		    "5",
		    new Mapped(weight, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate-3m}")));
		preArtActiveOver15.setCompositionString("1 AND 2 AND 4 AND(3 OR 5)");
		
		CompositionCohortDefinition artActiveOver15 = new CompositionCohortDefinition();
		artActiveOver15.setName("HIVQ: Active art in Hiv program with a visit in period - 12 months and over 15");
		artActiveOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artActiveOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		artActiveOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		artActiveOver15.getSearches().put("2",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		artActiveOver15.getSearches().put(
		    "3",
		    new Mapped(encounter, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-12m}")));
		artActiveOver15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		artActiveOver15.getSearches().put(
		    "5",
		    new Mapped(weight, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate-3m}")));
		artActiveOver15.setCompositionString("1 AND 2 AND 4 AND(3 OR 5)");
		
		CompositionCohortDefinition artActiveUnder15 = new CompositionCohortDefinition();
		artActiveUnder15.setName("HIVQ: Active art in Hiv program with a visit in period - 12 months and under 15");
		artActiveUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artActiveUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		artActiveUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		artActiveUnder15.getSearches().put("2",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		artActiveUnder15.getSearches().put(
		    "3",
		    new Mapped(encounter, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-12m}")));
		artActiveUnder15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		artActiveUnder15.getSearches().put(
		    "5",
		    new Mapped(weight, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate-3m}")));
		artActiveUnder15.setCompositionString("NOT 1 AND 2 AND 4 AND(3 OR 5)");
		
		CompositionCohortDefinition preArtWithAVisitOver15 = new CompositionCohortDefinition();
		preArtWithAVisitOver15.setName("HIVQ: preArt in Hiv program with a visit in period -3 months and over 15");
		preArtWithAVisitOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		preArtWithAVisitOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		preArtWithAVisitOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		preArtWithAVisitOver15.getSearches().put("2",
		    new Mapped(preArt, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		preArtWithAVisitOver15.getSearches().put(
		    "3",
		    new Mapped(encounter, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate-3m}")));
		preArtWithAVisitOver15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		preArtWithAVisitOver15.getSearches().put(
		    "5",
		    new Mapped(weight, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate-3m}")));
		preArtWithAVisitOver15.setCompositionString("1 AND 2 AND 4 AND(3 OR 5)");
		
		CompositionCohortDefinition artUnder15 = new CompositionCohortDefinition();
		artUnder15.setName("HIVQ: ever taking ART before end date and under 15");
		artUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		artUnder15.getSearches().put("2",
		    new Mapped(artOnOrBefore, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		artUnder15.getSearches().put("3",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		artUnder15.setCompositionString("NOT 1 AND 2 AND 3");
		
		CompositionCohortDefinition artOver15 = new CompositionCohortDefinition();
		artOver15.setName("HIVQ: ever taking ART before end date and over 15");
		artOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		artOver15.getSearches().put("2",
		    new Mapped(artOnOrBefore, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		artOver15.getSearches().put("3",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		artOver15.setCompositionString("1 AND 2 AND 3");
		
		CompositionCohortDefinition currentArtUnder15 = new CompositionCohortDefinition();
		currentArtUnder15.setName("HIVQ: currently taking ART before end date and under 15");
		currentArtUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		currentArtUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		currentArtUnder15.getSearches().put("2",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		currentArtUnder15.getSearches().put("3",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		currentArtUnder15.setCompositionString("NOT 1 AND 2 AND 3");
		
		CompositionCohortDefinition currentArtOver15 = new CompositionCohortDefinition();
		currentArtOver15.setName("HIVQ: currently taking ART before end date and over 15");
		currentArtOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		currentArtOver15.getSearches().put("1",
		    new Mapped(under15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		currentArtOver15.getSearches().put("2",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		currentArtOver15.getSearches().put("3",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		currentArtOver15.setCompositionString("Not 1 AND 2 AND 3");
		
		CompositionCohortDefinition artWithAVisitUnder15 = new CompositionCohortDefinition();
		artWithAVisitUnder15.setName("HIVQ: on ART in Hiv program with a visit in period -3 months and under 15");
		artWithAVisitUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artWithAVisitUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		artWithAVisitUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		artWithAVisitUnder15.getSearches().put("2",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		artWithAVisitUnder15.getSearches().put(
		    "3",
		    new Mapped(encounter, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		artWithAVisitUnder15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		artWithAVisitUnder15.getSearches().put("5",
		    new Mapped(weight, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		artWithAVisitUnder15.setCompositionString("NOT 1 AND 2 AND 4 AND(3 OR 5)");
		
		CompositionCohortDefinition artWithAVisitOver15 = new CompositionCohortDefinition();
		artWithAVisitOver15.setName("HIVQ: on ART in Hiv program with a visit in period -3 months and over 15");
		artWithAVisitOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artWithAVisitOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		artWithAVisitOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		artWithAVisitOver15.getSearches().put("2",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		artWithAVisitOver15.getSearches().put(
		    "3",
		    new Mapped(encounter, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		artWithAVisitOver15.getSearches().put("5",
		    new Mapped(weight, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		artWithAVisitOver15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		artWithAVisitOver15.setCompositionString("1 AND 2 AND 4 AND(3 OR 5)");
		
		CompositionCohortDefinition startedArtOver15 = new CompositionCohortDefinition();
		startedArtOver15.setName("HIVQ: started taking ART in period over 15");
		startedArtOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		startedArtOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		startedArtOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		startedArtOver15.getSearches().put(
		    "2",
		    new Mapped(artStartedInPeriod, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		startedArtOver15.getSearches().put("3",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${startDate-1d}")));
		startedArtOver15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		startedArtOver15.setCompositionString("1 AND 2 AND NOT 3 AND 4");
		
		CompositionCohortDefinition startedArtUnder15 = new CompositionCohortDefinition();
		startedArtUnder15.setName("HIVQ: started taking ART in period under 15");
		startedArtUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		startedArtUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		startedArtUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		startedArtUnder15.getSearches().put(
		    "2",
		    new Mapped(artStartedInPeriod, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		startedArtUnder15.getSearches().put("3",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${startDate-1d}")));
		startedArtUnder15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		startedArtUnder15.setCompositionString("NOT 1 AND 2 AND NOT 3 AND 4");
		
		CompositionCohortDefinition preArtWithACD4Under15 = new CompositionCohortDefinition();
		preArtWithACD4Under15.setName("HIVQ: preArt in Hiv program with a CD4 count in period -3 months and under 15");
		preArtWithACD4Under15.addParameter(new Parameter("endDate", "endDate", Date.class));
		preArtWithACD4Under15.addParameter(new Parameter("startDate", "startDate", Date.class));
		preArtWithACD4Under15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		preArtWithACD4Under15.getSearches().put("2",
		    new Mapped(preArt, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		preArtWithACD4Under15.getSearches().put("3",
		    new Mapped(cd4, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate-3m}")));
		preArtWithACD4Under15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		preArtWithACD4Under15.setCompositionString("NOT 1 AND 2 AND 3 AND 4");
		
		CompositionCohortDefinition preArtWithACD4Over15 = new CompositionCohortDefinition();
		preArtWithACD4Over15.setName("HIVQ: preArt in Hiv program with a CD4 count in period -3 months and over 15");
		preArtWithACD4Over15.addParameter(new Parameter("endDate", "endDate", Date.class));
		preArtWithACD4Over15.addParameter(new Parameter("startDate", "startDate", Date.class));
		preArtWithACD4Over15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		preArtWithACD4Over15.getSearches().put("2",
		    new Mapped(preArt, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		preArtWithACD4Over15.getSearches().put("3",
		    new Mapped(cd4, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate-3m}")));
		preArtWithACD4Over15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		preArtWithACD4Over15.setCompositionString("1 AND 2 AND 3 AND 4");
		
		CompositionCohortDefinition artWithACD4Over15 = new CompositionCohortDefinition();
		artWithACD4Over15.setName("HIVQ: on Art in Hiv program with a CD4 count in period -3 months and over 15");
		artWithACD4Over15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artWithACD4Over15.addParameter(new Parameter("startDate", "startDate", Date.class));
		artWithACD4Over15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		artWithACD4Over15.getSearches().put("2",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		artWithACD4Over15.getSearches().put("3",
		    new Mapped(cd4, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate-3m}")));
		artWithACD4Over15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		artWithACD4Over15.setCompositionString("1 AND 2 AND 3 AND 4");
		
		CompositionCohortDefinition artWithACD4Under15 = new CompositionCohortDefinition();
		artWithACD4Under15.setName("HIVQ: on Art in Hiv program with a CD4 count in period - 3 months and under 15");
		artWithACD4Under15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artWithACD4Under15.addParameter(new Parameter("startDate", "startDate", Date.class));
		artWithACD4Under15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		artWithACD4Under15.getSearches().put("2",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		artWithACD4Under15.getSearches().put("3",
		    new Mapped(cd4, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate-3m}")));
		artWithACD4Under15.getSearches().put("4",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		artWithACD4Under15.setCompositionString("NOT 1 AND 2 AND 3 AND 4");
		
		CompositionCohortDefinition hivWithAWeightUnder15 = new CompositionCohortDefinition();
		hivWithAWeightUnder15.setName("HIVQ: weight recorded in period under 15");
		hivWithAWeightUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivWithAWeightUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		hivWithAWeightUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		hivWithAWeightUnder15.getSearches().put("2",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		hivWithAWeightUnder15.getSearches().put("3",
		    new Mapped(weight, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		hivWithAWeightUnder15.setCompositionString("NOT 1 AND 2 AND 3");
		
		CompositionCohortDefinition hivWithAWeightOver15 = new CompositionCohortDefinition();
		hivWithAWeightOver15.setName("HIVQ: weight recorded in period over 15");
		hivWithAWeightOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivWithAWeightOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		hivWithAWeightOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		hivWithAWeightOver15.getSearches().put("2",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		hivWithAWeightOver15.getSearches().put("3",
		    new Mapped(weight, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		hivWithAWeightOver15.setCompositionString("1 AND 2 AND 3");
		
		CompositionCohortDefinition hivOver15 = new CompositionCohortDefinition();
		hivOver15.setName("HIVQ: HIV Adult or Pedi and over 15");
		hivOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		hivOver15.getSearches().put("2",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		hivOver15.setCompositionString("1 AND 2");
		
		CompositionCohortDefinition hivUnder15 = new CompositionCohortDefinition();
		hivUnder15.setName("HIVQ: HIV Adult or Pedi and under 15");
		hivUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		hivUnder15.getSearches().put("2",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		hivUnder15.setCompositionString("NOT 1 AND 2");
		
		CompositionCohortDefinition hivVisitOver15 = new CompositionCohortDefinition();
		hivVisitOver15.setName("HIVQ: in Hiv program with a visit in period and over 15");
		hivVisitOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivVisitOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		hivVisitOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		hivVisitOver15.getSearches().put(
		    "3",
		    new Mapped(encounter, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		hivVisitOver15.getSearches().put("2",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		hivVisitOver15.getSearches().put("4",
		    new Mapped(weight, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		hivVisitOver15.setCompositionString("1 AND 2 AND (3 OR 4)");
		
		CompositionCohortDefinition hivVisitUnder15 = new CompositionCohortDefinition();
		hivVisitUnder15.setName("HIVQ: in Hiv program with a visit in period and under 15");
		hivVisitUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivVisitUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		hivVisitUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		hivVisitUnder15.getSearches().put(
		    "3",
		    new Mapped(encounter, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		hivVisitUnder15.getSearches().put("2",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		hivVisitUnder15.getSearches().put("4",
		    new Mapped(weight, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
		hivVisitUnder15.setCompositionString("NOT 1 AND 2 AND (3 OR 4)");
		
		CohortIndicator oneHIV = Indicators.newCountIndicator("HIVQ: In all programs over 15_", inHIVOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator twoHIV = Indicators.newCountIndicator("HIVQ: In all programs under 15_", inHIVUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator threeHIV = Indicators.newCountIndicator("HIVQ: new in any Hiv program in period over 15_",
		    newInHIVOver15, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator fourHIV = Indicators.newCountIndicator("HIVQ: new in any Hiv program in period under 15_",
		    newInHIVUnder15, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator fiveHIV = Indicators.newCountIndicator("HIVQ: preArt in Hiv program in period over 15_",
		    followingOver15, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator sixHIV = Indicators.newCountIndicator("HIVQ: preArt in Hiv program in period under 15_",
		    followingUnder15, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator sevenHIV = Indicators.newCountIndicator(
		    "HIVQ: preArt in Hiv program with a visit in period -3 months and over 15_", preArtWithAVisitOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator eightHIV = Indicators.newCountIndicator(
		    "HIVQ: preArt in Hiv program with a visit in period -3 months and under 15_", preArtWithAVisitUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator nineHIV = Indicators.newCountIndicator("HIVQ: ever taking ART before end date and over 15_",
		    artOver15, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator tenHIV = Indicators.newCountIndicator("HIVQ: ever taking ART before end date and under 15_",
		    artUnder15, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator elevenHIV = Indicators.newCountIndicator("HIVQ: currently taking ART before end date and over 15_",
		    currentArtOver15, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator twelveHIV = Indicators.newCountIndicator("HIVQ: currently taking ART before end date and under 15_",
		    currentArtUnder15, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator thirteenHIV = Indicators.newCountIndicator(
		    "HIVQ: on ART in Hiv program with a visit in period -3 months and over 15_", artWithAVisitOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator fourteenHIV = Indicators.newCountIndicator(
		    "HIVQ: on ART in Hiv program with a visit in period -3 months and under 15_", artWithAVisitUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator fifteenHIV = Indicators.newCountIndicator("HIVQ: started taking ART in period over 15_",
		    startedArtOver15, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator sixteenHIV = Indicators.newCountIndicator("HIVQ: started taking ART in period under 15_",
		    startedArtUnder15, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator seventeenHIV = Indicators.newCountIndicator(
		    "HIVQ: preArt in Hiv program with a CD4 count in period -3 months and over 15_", preArtWithACD4Over15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator eighteenHIV = Indicators.newCountIndicator(
		    "HIVQ: preArt in Hiv program with a CD4 count in period -3 months and under 15_", preArtWithACD4Under15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator nineteenHIV = Indicators.newCountIndicator(
		    "HIVQ: on Art in Hiv program with a CD4 count in period -3 months and over 15_", artWithACD4Over15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twentyHIV = Indicators.newCountIndicator(
		    "HIVQ: on Art in Hiv program with a CD4 count in period -3 months and under 15_", artWithACD4Under15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twentyoneHIV = Indicators.newCountIndicator("HIVQ: weight recorded in period over 15_",
		    hivWithAWeightOver15, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twentytwoHIV = Indicators.newCountIndicator("HIVQ: weight recorded in period under 15_",
		    hivWithAWeightUnder15, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twentythreeHIV = Indicators.newCountIndicator("HIVQ: HIV visit and over 15_", hivVisitOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twentyfourHIV = Indicators.newCountIndicator("HIVQ: HIV visit and under 15_", hivVisitUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		
		dsd.addColumn("HIVQ1A", "In All HIV Programs Over 15",
		    new Mapped(oneHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("HIVQ1C", "In All HIV Programs Under 15",
		    new Mapped(twoHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("HIVQ2A", "New In All HIV Programs Over 15",
		    new Mapped(threeHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("HIVQ2C", "New In All HIV Programs Under 15",
		    new Mapped(fourHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("HIVQ3A", "Pre ART Over 15",
		    new Mapped(fiveHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("HIVQ3C", "Pre ART Under 15",
		    new Mapped(sixHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("HIVQ4A", "Pre ART Visit in last 2 quarters Over 15",
		    new Mapped(sevenHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("HIVQ4C", "Pre ART Visit in last 2 quarters Under 15",
		    new Mapped(eightHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("HIVQ5A", "Ever started on Art at end of review and Over 15",
		    new Mapped(nineHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("HIVQ5C", "Ever started on Art at end of review and Under 15",
		    new Mapped(tenHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("HIVQ6A", "Currently on Art at end of review and Over 15",
		    new Mapped(elevenHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("HIVQ6C", "Currently on Art at end of review and Under 15",
		    new Mapped(twelveHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("HIVQ7A", "Currently on Art and had a visit in last 2 quarters and Over 15", new Mapped(thirteenHIV,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("HIVQ7C", "Currently on Art and had a visit in last 2 quarters and Under 15", new Mapped(fourteenHIV,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn(
		    "HIVQ8A",
		    "Started on Art and Over 15",
		    new Mapped(fifteenHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn(
		    "HIVQ8C",
		    "Started on Art and Under 15",
		    new Mapped(sixteenHIV, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("HIVQ9A", "Pre ART and had a cd4 recorded in last 2 quarters and Over 15", new Mapped(seventeenHIV,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("HIVQ9C", "Pre ART and had a cd4 recorded in last 2 quarters and Under 15", new Mapped(eighteenHIV,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("HIVQ10A", "ART and had a cd4 recorded in last 2 quarters and Over 15", new Mapped(nineteenHIV,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("HIVQ10C", "ART and had a cd4 recorded in last 2 quarters and Under 15", new Mapped(twentyHIV,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("HIVQ11A", "HIV and had a weight recorded in last quarters and Over 15", new Mapped(twentyoneHIV,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("HIVQ11C", "HIV and had a weight recorded in last quarters and Under 15", new Mapped(twentytwoHIV,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn(
		    "HIVQ11AD",
		    "HIV visit and Over 15",
		    new Mapped(twentythreeHIV, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn(
		    "HIVQ11CD",
		    "HIV and Under 15",
		    new Mapped(twentyfourHIV, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		
		//===================================================================
		// PMTCT Pregnant Mother Program
		//===================================================================
		
		ProgramEnrollmentCohortDefinition enrolledInPMTCTPregByStartEndDate = Cohorts
		        .createProgramEnrollmentParameterizedByStartEndDate("Enrolled in PMTCT Pregnancy", pmtctProgram);
		
		InProgramCohortDefinition inPMTCTOnEndDate = Cohorts.createInProgramParameterizableByDate(
		    "In PMTCT Program on End Date", pmtctProgram);
		
		ProgramEnrollmentCohortDefinition enrolledInPMTCTPregOnOrBeforeEndDate = Cohorts.createProgramEnrollment(
		    "Enrolled in PMTCT Pregnancy on or before the end date", pmtctProgram);
		enrolledInPMTCTPregOnOrBeforeEndDate.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore",
		        Date.class));
		
		SqlCohortDefinition activePatientOnArt = Cohorts
		        .getPatientActiveOnArtDrugsByEndDate("Active on ART drugs by End date");
		
		CompositionCohortDefinition activePatientOnArtInPMTCTPreg = new CompositionCohortDefinition();
		activePatientOnArtInPMTCTPreg.setName("active Patient On Art In PMTCT Pregnancy");
		activePatientOnArtInPMTCTPreg.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientOnArtInPMTCTPreg.addParameter(new Parameter("onDate", "onDate", Date.class));
		activePatientOnArtInPMTCTPreg.getSearches().put("1",
		    new Mapped(activePatientOnArt, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		activePatientOnArtInPMTCTPreg.getSearches().put("2",
		    new Mapped(inPMTCTOnEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		activePatientOnArtInPMTCTPreg.setCompositionString(" 1 AND 2");
		
		SqlCohortDefinition patientsWithTwoOrMoreAdultHIVEncounters = Cohorts
		        .getPatientsWithNTimesOrMoreEncountersByStartAndEndDate("patients With Two Or More Adult HIV flowsheet",
		            adultHIVEncounterType, 2);
		
		CompositionCohortDefinition patientsWithTwoOrMoreAdultHIVEncountersInPMTCTPreg = new CompositionCohortDefinition();
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTPreg
		        .setName("patients With Two Or More Adult HIV flowsheet In PMTCT Pregnancy");
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTPreg.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTPreg.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTPreg.addParameter(new Parameter("enrolledOnOrAfter",
		        "enrolledOnOrAfter", Date.class));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTPreg.addParameter(new Parameter("enrolledOnOrBefore",
		        "enrolledOnOrBefore", Date.class));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTPreg.addParameter(new Parameter("onDate", "onDate", Date.class));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTPreg.getSearches().put(
		    "1",
		    new Mapped(patientsWithTwoOrMoreAdultHIVEncounters, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTPreg.getSearches().put("2",
		    new Mapped(inPMTCTOnEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTPreg
		        .getSearches()
		        .put(
		            "3",
		            new Mapped(
		                    enrolledInPMTCTPregByStartEndDate,
		                    ParameterizableUtil
		                            .createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTPreg.setCompositionString(" 1 AND 2 AND (NOT 3)");
		
		CompositionCohortDefinition patientsInPMTCTPregNotEnrolledByStartAndEndDate = new CompositionCohortDefinition();
		patientsInPMTCTPregNotEnrolledByStartAndEndDate
		        .setName("patients In PMTCT Pregnancy but not enrolled between startdate and enddate");
		patientsInPMTCTPregNotEnrolledByStartAndEndDate.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter",
		        Date.class));
		patientsInPMTCTPregNotEnrolledByStartAndEndDate.addParameter(new Parameter("enrolledOnOrBefore",
		        "enrolledOnOrBefore", Date.class));
		patientsInPMTCTPregNotEnrolledByStartAndEndDate.addParameter(new Parameter("onDate", "onDate", Date.class));
		patientsInPMTCTPregNotEnrolledByStartAndEndDate.getSearches().put("2",
		    new Mapped(inPMTCTOnEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		patientsInPMTCTPregNotEnrolledByStartAndEndDate
		        .getSearches()
		        .put(
		            "3",
		            new Mapped(
		                    enrolledInPMTCTPregByStartEndDate,
		                    ParameterizableUtil
		                            .createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsInPMTCTPregNotEnrolledByStartAndEndDate.setCompositionString("2 AND (NOT 3)");
		
		CohortIndicator onePMTCTPreg = Indicators.newCountIndicator("PMTCTPregQ: Enrolled in PMTCT Pregnancy",
		    enrolledInPMTCTPregByStartEndDate,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator twoPMTCTPreg = Indicators.newCountIndicator(
		    "PMTCTPregQ: Enrolled in PMTCT Pregnancy on the end date", inPMTCTOnEndDate,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		CohortIndicator threePMTCTPreg = Indicators.newCountIndicator(
		    "PMTCTPregQ: Enrolled in PMTCT Pregnancy on or before the end date", enrolledInPMTCTPregOnOrBeforeEndDate,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator fourPMTCTPreg = Indicators.newCountIndicator(
		    "PMTCTPregQ: Enrolled in PMTCT Pregnancy on the end date", activePatientOnArtInPMTCTPreg,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		CohortIndicator fivePMTCTPreg = Indicators
		        .newCountIndicator(
		            "PMTCTPregQ: patients With Two Or More Adult HIV flowsheet In PMTCT Pregnancy",
		            patientsWithTwoOrMoreAdultHIVEncountersInPMTCTPreg,
		            ParameterizableUtil
		                    .createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate},onDate=${endDate}"));
		
		CohortIndicator fivePMTCTPregD = Indicators
		        .newCountIndicator(
		            "PMTCTPregQ: patients In PMTCT Pregnancy but not enrolled between startdate and enddate",
		            patientsInPMTCTPregNotEnrolledByStartAndEndDate,
		            ParameterizableUtil
		                    .createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate},onDate=${endDate}"));
		
		dsd.addColumn(
		    "PregQ1",
		    "Enrolled in PMTCT Pregnancy",
		    new Mapped(onePMTCTPreg, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn(
		    "PregQ2",
		    "Enrolled in PMTCT Pregnancy on the end date",
		    new Mapped(twoPMTCTPreg, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("PregQ3", "Enrolled in PMTCT Pregnancy on or before the end date", new Mapped(threePMTCTPreg,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn(
		    "PregQ4",
		    "Enrolled in PMTCT Pregnancy on the end date",
		    new Mapped(fourPMTCTPreg, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("PregQ5", "patients With Two Or More Adult HIV flowsheet In PMTCT Pregnancy", new Mapped(
		        fivePMTCTPreg, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		
		dsd.addColumn("PregQ5D", "patients In PMTCT Pregnancy but not enrolled between startdate and enddate", new Mapped(
		        fivePMTCTPregD, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		
		//=============================================================================================
		// HIV Exposed Infants
		//=============================================================================================
		
		ProgramEnrollmentCohortDefinition enrolledInPMTCTInfantByStartEndDate = Cohorts
		        .createProgramEnrollmentParameterizedByStartEndDate("Enrolled in PMTCT Infant", pmtctCombinedInfantProgram);
		
		InProgramCohortDefinition inPMTCTInfantOnEndDate = Cohorts.createInProgramParameterizableByDate(
		    "In PMTCT Program on End Date", pmtctCombinedInfantProgram);
		
		InStateCohortDefinition inFeedingStatesOnDate = Cohorts.createInCurrentState("In feeding status on end date",
		    feedingWorkflowStates, "onDate");
		
		CompositionCohortDefinition inFeedingStatesOnDateAndinPMTCTInfantOnEndDate = new CompositionCohortDefinition();
		inFeedingStatesOnDateAndinPMTCTInfantOnEndDate
		        .setName("patient in Feeding States On Date And in PMTCT Infant OnEndDate");
		inFeedingStatesOnDateAndinPMTCTInfantOnEndDate.addParameter(new Parameter("onDate", "onDate", Date.class));
		inFeedingStatesOnDateAndinPMTCTInfantOnEndDate.getSearches().put("2",
		    new Mapped(inFeedingStatesOnDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		inFeedingStatesOnDateAndinPMTCTInfantOnEndDate.getSearches().put("3",
		    new Mapped(inPMTCTInfantOnEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		inFeedingStatesOnDateAndinPMTCTInfantOnEndDate.setCompositionString("2 AND 3");
		
		ProgramEnrollmentCohortDefinition enrolledInPMTCTInfantOnOrBeforeEndDate = Cohorts.createProgramEnrollment(
		    "Enrolled in PMTCT Infant on or before the end date", pmtctCombinedInfantProgram);
		enrolledInPMTCTInfantOnOrBeforeEndDate.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore",
		        Date.class));
		
		SqlCohortDefinition patientsWithTwoOrMoreExposedInfantEncounters = Cohorts
		        .getPatientsWithNTimesOrMoreEncountersByStartAndEndDate(
		            "patients With Two Or More Exposed Infant flowsheet", exposedInfantEncountertype, 2);
		
		CompositionCohortDefinition patientsWithTwoOrMoreExposedInfantEncountersInPMTCTInfant = new CompositionCohortDefinition();
		patientsWithTwoOrMoreExposedInfantEncountersInPMTCTInfant
		        .setName("patients With Two Or More Exposed Infant flowsheet In PMTCT Infant");
		patientsWithTwoOrMoreExposedInfantEncountersInPMTCTInfant.addParameter(new Parameter("startDate", "startDate",
		        Date.class));
		patientsWithTwoOrMoreExposedInfantEncountersInPMTCTInfant.addParameter(new Parameter("endDate", "endDate",
		        Date.class));
		patientsWithTwoOrMoreExposedInfantEncountersInPMTCTInfant.addParameter(new Parameter("enrolledOnOrAfter",
		        "enrolledOnOrAfter", Date.class));
		patientsWithTwoOrMoreExposedInfantEncountersInPMTCTInfant.addParameter(new Parameter("enrolledOnOrBefore",
		        "enrolledOnOrBefore", Date.class));
		patientsWithTwoOrMoreExposedInfantEncountersInPMTCTInfant
		        .addParameter(new Parameter("onDate", "onDate", Date.class));
		patientsWithTwoOrMoreExposedInfantEncountersInPMTCTInfant.getSearches().put(
		    "1",
		    new Mapped(patientsWithTwoOrMoreExposedInfantEncounters, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		patientsWithTwoOrMoreExposedInfantEncountersInPMTCTInfant.getSearches().put("2",
		    new Mapped(inPMTCTInfantOnEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		patientsWithTwoOrMoreExposedInfantEncountersInPMTCTInfant
		        .getSearches()
		        .put(
		            "3",
		            new Mapped(
		                    enrolledInPMTCTInfantByStartEndDate,
		                    ParameterizableUtil
		                            .createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsWithTwoOrMoreExposedInfantEncountersInPMTCTInfant.setCompositionString(" 1 AND 2 AND (NOT 3)");
		
		CompositionCohortDefinition patientsInPMTCTInfantNotEnrolledByStartAndEndDate = new CompositionCohortDefinition();
		patientsInPMTCTInfantNotEnrolledByStartAndEndDate
		        .setName("patients In PMTCT infant but not enrolled between startdate and enddate");
		patientsInPMTCTInfantNotEnrolledByStartAndEndDate.addParameter(new Parameter("enrolledOnOrAfter",
		        "enrolledOnOrAfter", Date.class));
		patientsInPMTCTInfantNotEnrolledByStartAndEndDate.addParameter(new Parameter("enrolledOnOrBefore",
		        "enrolledOnOrBefore", Date.class));
		patientsInPMTCTInfantNotEnrolledByStartAndEndDate.addParameter(new Parameter("onDate", "onDate", Date.class));
		patientsInPMTCTInfantNotEnrolledByStartAndEndDate.getSearches().put("2",
		    new Mapped(inPMTCTInfantOnEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		patientsInPMTCTInfantNotEnrolledByStartAndEndDate
		        .getSearches()
		        .put(
		            "3",
		            new Mapped(
		                    enrolledInPMTCTInfantByStartEndDate,
		                    ParameterizableUtil
		                            .createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsInPMTCTInfantNotEnrolledByStartAndEndDate.setCompositionString("2 AND (NOT 3)");
		
		AgeCohortDefinition under13Weeks = new AgeCohortDefinition();
		under13Weeks.setName("under13Weeks");
		under13Weeks.setMaxAge(12);
		under13Weeks.setMaxAgeUnit(DurationUnit.WEEKS);
		under13Weeks.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		
		AgeCohortDefinition over13Weeks = new AgeCohortDefinition();
		over13Weeks.setName("over13Weeks");
		over13Weeks.setMinAge(13);
		over13Weeks.setMinAgeUnit(DurationUnit.WEEKS);
		over13Weeks.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		
		CompositionCohortDefinition patientsInPMTCTInfantTurned13WeeksByStartAndEndDate = new CompositionCohortDefinition();
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDate
		        .setName("patients In PMTCT infant turned 13 but not enrolled between startdate and enddate");
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDate.addParameter(new Parameter("effectiveDate", "effectiveDate",
	        Date.class));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDate.addParameter(new Parameter("onDate", "onDate", Date.class));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDate.getSearches().put("1",
		    new Mapped(inPMTCTInfantOnEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDate.getSearches().put("2",
		    new Mapped(under13Weeks, ParameterizableUtil.createParameterMappings("effectiveDate=${startDate}")));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDate.getSearches().put("3",
		    new Mapped(over13Weeks, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDate.setCompositionString("1 AND 2 AND 3");
		
		CodedObsCohortDefinition patientsWithHivPCRObs = Cohorts.createCodedObsCohortDefinition(
		    "patientsWithHivPCRObs", onOrAfterOnOrBefore, hivPCR, null, SetComparator.IN,
		    TimeModifier.ANY);
		
		CompositionCohortDefinition patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObs = new CompositionCohortDefinition();
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObs
		        .setName("patients In PMTCT infant turned but not enrolled between startdate and enddate with HIV PCR");
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObs.addParameter(new Parameter("onDate", "onDate",
		        Date.class));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObs.addParameter(new Parameter("onOrBefore",
		        "onOrBefore", Date.class));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObs.addParameter(new Parameter("onOrAfter",
		        "onOrAfter", Date.class));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObs.getSearches().put(
		    "1",
		    new Mapped(patientsInPMTCTInfantTurned13WeeksByStartAndEndDate, ParameterizableUtil
		            .createParameterMappings("onDate=${onDate}")));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObs.getSearches().put(
		    "2",
		    new Mapped(patientsWithHivPCRObs, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObs.setCompositionString("1 AND 2");		
		
		CodedObsCohortDefinition patientsWithHivPCRObsAndValuePositive = Cohorts.createCodedObsCohortDefinition(
		    "patientsWithHivPCRObsAndValuePositive", onOrAfterOnOrBefore, hivPCR, hivPositive, SetComparator.IN,
		    TimeModifier.ANY);	
		
		CompositionCohortDefinition patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObsPositive = new CompositionCohortDefinition();
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObsPositive
		        .setName("patients In PMTCT infant turned but not enrolled between startdate and enddate with HIV PCR Positive");
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObsPositive.addParameter(new Parameter("onDate", "onDate",
		        Date.class));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObsPositive.addParameter(new Parameter("onOrBefore",
		        "onOrBefore", Date.class));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObsPositive.addParameter(new Parameter("onOrAfter",
		        "onOrAfter", Date.class));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObsPositive.getSearches().put(
		    "1",
		    new Mapped(patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObs, ParameterizableUtil
		            .createParameterMappings("onDate=${onDate},onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObsPositive.getSearches().put(
		    "2",
		    new Mapped(patientsWithHivPCRObsAndValuePositive, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObsPositive.setCompositionString("1 AND 2");		
		
		
		AgeCohortDefinition under18Monthss = new AgeCohortDefinition();
		under18Monthss.setName("under18Months");
		under18Monthss.setMaxAge(17);
		under18Monthss.setMaxAgeUnit(DurationUnit.MONTHS);
		under18Monthss.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		
		AgeCohortDefinition over18Mothns = new AgeCohortDefinition();
		over18Mothns.setName("over18Months");
		over18Mothns.setMinAge(18);
		over18Mothns.setMinAgeUnit(DurationUnit.MONTHS);
		over18Mothns.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		
		CompositionCohortDefinition patientsInPMTCTInfantTurned18MonthssByStartAndEndDate = new CompositionCohortDefinition();
		patientsInPMTCTInfantTurned18MonthssByStartAndEndDate
		        .setName("patients In PMTCT infant turned 18 month but not enrolled between startdate and enddate");
		patientsInPMTCTInfantTurned18MonthssByStartAndEndDate.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore",
	        Date.class));
		patientsInPMTCTInfantTurned18MonthssByStartAndEndDate.addParameter(new Parameter("effectiveDate", "effectiveDate",
	        Date.class));
		patientsInPMTCTInfantTurned18MonthssByStartAndEndDate.getSearches().put("1",
		    new Mapped(enrolledInPMTCTInfantOnOrBeforeEndDate, ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsInPMTCTInfantTurned18MonthssByStartAndEndDate.getSearches().put("2",
		    new Mapped(under13Weeks, ParameterizableUtil.createParameterMappings("effectiveDate=${startDate}")));
		patientsInPMTCTInfantTurned18MonthssByStartAndEndDate.getSearches().put("3",
		    new Mapped(over13Weeks, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		patientsInPMTCTInfantTurned18MonthssByStartAndEndDate.setCompositionString("1 AND 2 AND 3");
		
		
		
		SqlCohortDefinition serotestAfter16MonthsOfAge=new SqlCohortDefinition();
		serotestAfter16MonthsOfAge.setName("serotestAfter16MonthsOfAge");
		serotestAfter16MonthsOfAge.setQuery("SELECT distinct o.person_id FROM person p, obs o where o.person_id=p.person_id and o.concept_id= "+seroTest.getConceptId()+" and DATEDIFF(o.obs_datetime,p.birthdate)>480 and p.birthdate is not null and o.voided=0 and p.voided=0");
		
		
		CompositionCohortDefinition patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestAfter16MonthsOfAge = new CompositionCohortDefinition();
		patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestAfter16MonthsOfAge
		        .setName("patients In PMTCT infant turned 18 months but not enrolled between startdate and enddate");
		patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestAfter16MonthsOfAge.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore",
	        Date.class));
		patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestAfter16MonthsOfAge.getSearches().put("1",
		    new Mapped(patientsInPMTCTInfantTurned18MonthssByStartAndEndDate, ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestAfter16MonthsOfAge.getSearches().put("2",new Mapped(serotestAfter16MonthsOfAge, null));
		patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestAfter16MonthsOfAge.setCompositionString("1 AND 2");
		
		
		SqlCohortDefinition serotestPositiveAfter16MonthsOfAge=new SqlCohortDefinition();
		serotestPositiveAfter16MonthsOfAge.setName("serotestPositiveAfter16MonthsOfAge");
		serotestPositiveAfter16MonthsOfAge.setQuery("SELECT distinct o.person_id FROM person p, obs o where o.person_id=p.person_id and o.concept_id= "+seroTest.getConceptId()+" and o.value_coded="+hivPositive+" and DATEDIFF(o.obs_datetime,p.birthdate)>480 and p.birthdate is not null and o.voided=0 and p.voided=0");
		
		
		CompositionCohortDefinition patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestPositiveAfter16MonthsOfAge = new CompositionCohortDefinition();
		patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestPositiveAfter16MonthsOfAge
		        .setName("patients In PMTCT infant turned 18 months but not enrolled between startdate and enddate");
		patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestPositiveAfter16MonthsOfAge.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore",
	        Date.class));
		patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestPositiveAfter16MonthsOfAge.getSearches().put("1",
		    new Mapped(patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestAfter16MonthsOfAge, ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestPositiveAfter16MonthsOfAge.getSearches().put("2",new Mapped(serotestPositiveAfter16MonthsOfAge, null));
		patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestPositiveAfter16MonthsOfAge.setCompositionString("1 AND 2");
		
		AgeCohortDefinition over6Weeks = new AgeCohortDefinition();
		over6Weeks.setName("over6Weeks");
		over6Weeks.setMinAge(6);
		over6Weeks.setMinAgeUnit(DurationUnit.WEEKS);
		over6Weeks.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		
		SqlCohortDefinition patientOnCTXByStartAndEndDate=new SqlCohortDefinition();
		patientOnCTXByStartAndEndDate.setName("patientOnCTXByStartAndEndDate");
		patientOnCTXByStartAndEndDate.addParameter(new Parameter("startDate","startDate",Date.class));
		patientOnCTXByStartAndEndDate.addParameter(new Parameter("endDate","endDate",Date.class));
		patientOnCTXByStartAndEndDate.setQuery("select distinct patient_id from orders where concept_id="+ctx.getConceptId()+" and start_date>= :startDate and start_date<= :endDate and voided=0");
		
		
		CompositionCohortDefinition patientOver6WeeksOnCTXAndInPMTCTInfantOnEndDate = new CompositionCohortDefinition();
		patientOver6WeeksOnCTXAndInPMTCTInfantOnEndDate
		        .setName("patient Over 6 Weeks On CTX And in PMTCT Infant OnEndDate");
		patientOver6WeeksOnCTXAndInPMTCTInfantOnEndDate.addParameter(new Parameter("startDate","startDate",Date.class));
		patientOver6WeeksOnCTXAndInPMTCTInfantOnEndDate.addParameter(new Parameter("endDate","endDate",Date.class));
		patientOver6WeeksOnCTXAndInPMTCTInfantOnEndDate.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		patientOver6WeeksOnCTXAndInPMTCTInfantOnEndDate.addParameter(new Parameter("onDate","onDate",Date.class));
		patientOver6WeeksOnCTXAndInPMTCTInfantOnEndDate.getSearches().put("1",
		    new Mapped(over6Weeks, ParameterizableUtil.createParameterMappings("effectiveDate=${startDate}")));
		patientOver6WeeksOnCTXAndInPMTCTInfantOnEndDate.getSearches().put("2",
		    new Mapped(patientOnCTXByStartAndEndDate, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientOver6WeeksOnCTXAndInPMTCTInfantOnEndDate.getSearches().put("3",
		    new Mapped(inPMTCTInfantOnEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		patientOver6WeeksOnCTXAndInPMTCTInfantOnEndDate.setCompositionString("1 AND 2 AND 3");
		
		CompositionCohortDefinition patientOver6WeeksAndInPMTCTInfantOnEndDate = new CompositionCohortDefinition();
		patientOver6WeeksAndInPMTCTInfantOnEndDate
		        .setName("patient Over 6 Weeks And in PMTCT Infant OnEndDate");
		patientOver6WeeksAndInPMTCTInfantOnEndDate.addParameter(new Parameter("startDate","startDate",Date.class));
		patientOver6WeeksAndInPMTCTInfantOnEndDate.addParameter(new Parameter("endDate","endDate",Date.class));
		patientOver6WeeksAndInPMTCTInfantOnEndDate.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		patientOver6WeeksAndInPMTCTInfantOnEndDate.addParameter(new Parameter("onDate","onDate",Date.class));
		patientOver6WeeksAndInPMTCTInfantOnEndDate.getSearches().put("1",
		    new Mapped(over6Weeks, ParameterizableUtil.createParameterMappings("effectiveDate=${startDate}")));
		patientOver6WeeksAndInPMTCTInfantOnEndDate.getSearches().put("3",
		    new Mapped(inPMTCTInfantOnEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		patientOver6WeeksAndInPMTCTInfantOnEndDate.setCompositionString("1 AND 3");
		
		SqlCohortDefinition patientOnNevirapine=new SqlCohortDefinition();
		patientOnNevirapine.setName("patientOnNevirapine");
		patientOnNevirapine.setQuery("select distinct patient_id from orders where concept_id="+nevirapine.getConceptId()+" and voided=0");
		
		
		CompositionCohortDefinition patientOnNevirapineEnrolledInPMTCTInfant = new CompositionCohortDefinition();
		patientOnNevirapineEnrolledInPMTCTInfant
		        .setName("patient On Nevirapine Enrolled In PMTCT Infant");
		patientOnNevirapineEnrolledInPMTCTInfant.addParameter(new Parameter("enrolledOnOrAfter","enrolledOnOrAfter", Date.class));
		patientOnNevirapineEnrolledInPMTCTInfant.addParameter(new Parameter("enrolledOnOrBefore","enrolledOnOrBefore", Date.class));
		patientOnNevirapineEnrolledInPMTCTInfant.getSearches().put("1", new Mapped(patientOnNevirapine, null));
		patientOnNevirapineEnrolledInPMTCTInfant.getSearches().put( "2", new Mapped( enrolledInPMTCTInfantByStartEndDate,ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientOnNevirapineEnrolledInPMTCTInfant.setCompositionString(" 1 AND 2");
		
		
		
		CohortIndicator onePMTCTInfant = Indicators.newCountIndicator("PMTCTInfantQ: Enrolled in PMTCT Infant",
		    enrolledInPMTCTInfantByStartEndDate,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator twoPMTCTInfant = Indicators.newCountIndicator(
		    "PMTCTInfantQ: Enrolled in PMTCT Infant on the end date", inPMTCTInfantOnEndDate,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		CohortIndicator threePMTCTInfant = Indicators
		        .newCountIndicator("PMTCTInfantQ: patient in Feeding States On Date And in PMTCT Infant OnEndDate",
		            inFeedingStatesOnDateAndinPMTCTInfantOnEndDate,
		            ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		CohortIndicator fourPMTCTInfant = Indicators.newCountIndicator(
		    "PMTCTInfantQ: Enrolled in PMTCT Infant on or before the end date", enrolledInPMTCTInfantOnOrBeforeEndDate,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator fivePMTCTInfant = Indicators
		        .newCountIndicator(
		            "PMTCTInfantQ: patients With Two Or More Exposed Infant flowsheet In PMTCT Pregnancy",
		            patientsWithTwoOrMoreExposedInfantEncountersInPMTCTInfant,
		            ParameterizableUtil
		                    .createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate},onDate=${endDate}"));
		
		CohortIndicator fivePMTCTInfantD = Indicators
		        .newCountIndicator(
		            "PMTCTInfantQ: patients In PMTCT Infant but not enrolled between startdate and enddate",
		            patientsInPMTCTInfantNotEnrolledByStartAndEndDate,
		            ParameterizableUtil
		                    .createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate},onDate=${endDate}"));
		
		CohortIndicator sixPMTCTInfantD = Indicators.newCountIndicator(
		    "PMTCTInfantQ: Enrolled in PMTCT Infant on the end date and turned 13 weeks by start and end date",
		    patientsInPMTCTInfantTurned13WeeksByStartAndEndDate,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		CohortIndicator sixPMTCTInfant = Indicators
		        .newCountIndicator(
		            "PMTCTInfantQ: Enrolled in PMTCT Infant on the end date and turned 13 weeks by start and end date with HIV PCR obs",
		            patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObs, ParameterizableUtil
		                    .createParameterMappings("onDate=${endDate},onOrBefore=${endDate},onOrAfter=${startDate}"));
		
		CohortIndicator sevenPMTCTInfant = Indicators
        .newCountIndicator(
            "PMTCTInfantQ: Enrolled in PMTCT Infant on the end date and turned 13 weeks by start and end date with HIV PCR obs positive",
            patientsInPMTCTInfantTurned13WeeksByStartAndEndDateWithHivPCRObsPositive, ParameterizableUtil
                    .createParameterMappings("onDate=${endDate},onOrBefore=${endDate},onOrAfter=${startDate}"));

		
		CohortIndicator eightPMTCTInfantD = Indicators.newCountIndicator(
		    "PMTCTInfantQ: Enrolled in PMTCT Infant on or before the end date who turned 18 months by start and end date", patientsInPMTCTInfantTurned18MonthssByStartAndEndDate,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
		
		
		CohortIndicator eightPMTCTInfant = Indicators.newCountIndicator(
		    "PMTCTInfantQ: Enrolled in PMTCT Infant on or before the end date who turned 18 months by start and end date and sero test recorded after 16 months of age", patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestAfter16MonthsOfAge,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator ninePMTCTInfant = Indicators.newCountIndicator(
		    "PMTCTInfantQ: Enrolled in PMTCT Infant on or before the end date who turned 18 months by start and end date and sero test positive recorded after 16 months of age", patientsInPMTCTInfantTurned18MonthsByStartAndEndDateAndWithserotestPositiveAfter16MonthsOfAge,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
		
		
		
		CohortIndicator tenPMTCTInfant = Indicators.newCountIndicator(
		    "PMTCTInfantQ: Patient over 6 week on ctx Enrolled in PMTCT Infant on the end date", patientOver6WeeksOnCTXAndInPMTCTInfantOnEndDate,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		CohortIndicator tenPMTCTInfantD = Indicators.newCountIndicator(
		    "PMTCTInfantQ: Patient over 6 week Enrolled in PMTCT Infant on the end date", patientOver6WeeksAndInPMTCTInfantOnEndDate,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		
		CohortIndicator elevenPMTCTInfant = Indicators.newCountIndicator("PMTCTInfantQ: Enrolled in PMTCT Infanf ever on Nevirapine",
			patientOnNevirapineEnrolledInPMTCTInfant,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));
		
		
		
		dsd.addColumn(
		    "EIQ1",
		    "Enrolled in PMTCT Infant",
		    new Mapped(onePMTCTInfant, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn(
		    "EIQ2",
		    "Enrolled in PMTCT Infant on the end date",
		    new Mapped(twoPMTCTInfant, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("EIQ3", "patient in Feeding States On Date And in PMTCT Infant OnEndDate", new Mapped(
		        threePMTCTInfant, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("EIQ4", "Enrolled in PMTCT Infant on or before the end date", new Mapped(fourPMTCTInfant,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("EIQ5", "patients With Two Or More Exposed Infant flowsheet In PMTCT Infant", new Mapped(
		        fivePMTCTInfant, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		
		dsd.addColumn("EIQ5D", "patients In PMTCT Infant but not enrolled between startdate and enddate", new Mapped(
		        fivePMTCTInfantD, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		
		dsd.addColumn(
		    "EIQ6",
		    "Enrolled in PMTCT Infant on the end date and turned 13 weeks by start and end date with HIV PCR obs",
		    new Mapped(sixPMTCTInfant, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		
		dsd.addColumn(
		    "EIQ6D",
		    "Enrolled in PMTCT Infant on the end date and turned 13 weeks by start and end date",
		    new Mapped(sixPMTCTInfantD, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		
		
		dsd.addColumn(
		    "EIQ7",
		    "Enrolled in PMTCT Infant on the end date and turned 13 weeks by start and end date with HIV PCR obs positive",
		    new Mapped(sevenPMTCTInfant, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		
		dsd.addColumn(
		    "EIQ8",
		    "Enrolled in PMTCT Infant on or before the end date who turned 18 months by start and end date and sero test recorded after 16 months of age",
		    new Mapped(eightPMTCTInfant, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn(
		    "EIQ8D",
		    "Enrolled in PMTCT Infant on or before the end date who turned 18 months by start and end date",
		    new Mapped(eightPMTCTInfantD, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		
		dsd.addColumn(
		    "EIQ9",
		    "Enrolled in PMTCT Infant on or before the end date who turned 18 months by start and end date and sero test positive recorded after 16 months of age",
		    new Mapped(ninePMTCTInfant, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		
		dsd.addColumn(
		    "EIQ10",
		    "Patient over 6 week on ctx Enrolled in PMTCT Infant on the end date",
		    new Mapped(tenPMTCTInfant, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn(
		    "EIQ10D",
		    "Patient over 6 week Enrolled in PMTCT Infant on the end date",
		    new Mapped(tenPMTCTInfantD, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");				
		
		dsd.addColumn(
		    "EIQ11",
		    "Enrolled in PMTCT Infanf ever on Nevirapine",
		    new Mapped(elevenPMTCTInfant, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		
		

		//===================================================================
		// PMTCT CC Mother Program
		//===================================================================
		
		ProgramEnrollmentCohortDefinition enrolledInPMTCTCCMotherByStartEndDate = Cohorts
		        .createProgramEnrollmentParameterizedByStartEndDate("Enrolled in PMTCT C C Mother", pmtctCombinedMotherProgram);
		
		InProgramCohortDefinition inPMTCTCCMotherOnEndDate = Cohorts.createInProgramParameterizableByDate(
		    "In PMTCT Program C C Mother on End Date", pmtctCombinedMotherProgram);
		
		ProgramEnrollmentCohortDefinition enrolledInPMTCTCCMotherOnOrBeforeEndDate = Cohorts.createProgramEnrollment(
		    "Enrolled in PMTCT C C Mother on or before the end date", pmtctCombinedMotherProgram);
		enrolledInPMTCTCCMotherOnOrBeforeEndDate.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore",
		        Date.class));
		
		/*SqlCohortDefinition activePatientOnArt = Cohorts
		        .getPatientActiveOnArtDrugsByEndDate("Active on ART drugs by End date");*/
		
		CompositionCohortDefinition activePatientOnArtInPMTCTCCMother = new CompositionCohortDefinition();
		activePatientOnArtInPMTCTCCMother.setName("active Patient On Art In PMTCT Pregnancy");
		activePatientOnArtInPMTCTCCMother.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientOnArtInPMTCTCCMother.addParameter(new Parameter("onDate", "onDate", Date.class));
		activePatientOnArtInPMTCTCCMother.getSearches().put("1",
		    new Mapped(activePatientOnArt, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		activePatientOnArtInPMTCTCCMother.getSearches().put("2",
		    new Mapped(inPMTCTCCMotherOnEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		activePatientOnArtInPMTCTCCMother.setCompositionString(" 1 AND 2");
		
		/*SqlCohortDefinition patientsWithTwoOrMoreAdultHIVEncounters = Cohorts
		        .getPatientsWithNTimesOrMoreEncountersByStartAndEndDate("patients With Two Or More Adult HIV flowsheet",
		            adultHIVEncounterType, 2);*/
		
		CompositionCohortDefinition patientsWithTwoOrMoreAdultHIVEncountersInPMTCTCCMother = new CompositionCohortDefinition();
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTCCMother
		        .setName("patients With Two Or More Adult HIV flowsheet In PMTCT C C Mother");
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTCCMother.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTCCMother.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTCCMother.addParameter(new Parameter("enrolledOnOrAfter",
		        "enrolledOnOrAfter", Date.class));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTCCMother.addParameter(new Parameter("enrolledOnOrBefore",
		        "enrolledOnOrBefore", Date.class));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTCCMother.addParameter(new Parameter("onDate", "onDate", Date.class));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTCCMother.getSearches().put(
		    "1",
		    new Mapped(patientsWithTwoOrMoreAdultHIVEncounters, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTCCMother.getSearches().put("2",
		    new Mapped(inPMTCTCCMotherOnEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTCCMother
		        .getSearches()
		        .put(
		            "3",
		            new Mapped(
		                    enrolledInPMTCTCCMotherByStartEndDate,
		                    ParameterizableUtil
		                            .createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsWithTwoOrMoreAdultHIVEncountersInPMTCTCCMother.setCompositionString(" 1 AND 2 AND (NOT 3)");
		
		CompositionCohortDefinition patientsInPMTCTCCMotherNotEnrolledByStartAndEndDate = new CompositionCohortDefinition();
		patientsInPMTCTCCMotherNotEnrolledByStartAndEndDate
		        .setName("patients In PMTCT c c Mother but not enrolled between startdate and enddate");
		patientsInPMTCTCCMotherNotEnrolledByStartAndEndDate.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter",
		        Date.class));
		patientsInPMTCTCCMotherNotEnrolledByStartAndEndDate.addParameter(new Parameter("enrolledOnOrBefore",
		        "enrolledOnOrBefore", Date.class));
		patientsInPMTCTCCMotherNotEnrolledByStartAndEndDate.addParameter(new Parameter("onDate", "onDate", Date.class));
		patientsInPMTCTCCMotherNotEnrolledByStartAndEndDate.getSearches().put("2",
		    new Mapped(inPMTCTCCMotherOnEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		patientsInPMTCTCCMotherNotEnrolledByStartAndEndDate
		        .getSearches()
		        .put(
		            "3",
		            new Mapped(
		                    enrolledInPMTCTCCMotherByStartEndDate,
		                    ParameterizableUtil
		                            .createParameterMappings("enrolledOnOrAfter=${enrolledOnOrAfter},enrolledOnOrBefore=${enrolledOnOrBefore}")));
		patientsInPMTCTCCMotherNotEnrolledByStartAndEndDate.setCompositionString("2 AND (NOT 3)");
		
		
		
		SqlCohortDefinition patientUsingMethodOfFamilyPlanning=new SqlCohortDefinition();
		patientUsingMethodOfFamilyPlanning.setName("patientUsingMethodOfFamilyPlanning");
		patientUsingMethodOfFamilyPlanning.setQuery("select distinct person_id from obs where concept_id="+methodOfFamilyPlanning.getConceptId()+" and value_coded in ("+usingCondoms.getConceptId()+","+naturalFamilyPlanning.getConceptId()+") and obs_datetime>= :startDate and obs_datetime<= :endDate and voided=0");
		patientUsingMethodOfFamilyPlanning.addParameter(new Parameter("startDate","startDate",Date.class));
		patientUsingMethodOfFamilyPlanning.addParameter(new Parameter("endDate","endDate",Date.class));		
		
		
		CompositionCohortDefinition patientsInPMTCTCCMotherUsingMethodOfFamilyPlanning = new CompositionCohortDefinition();
		patientsInPMTCTCCMotherUsingMethodOfFamilyPlanning.setName("patients In PMTCT  C C Mother Using Method Of Family Planning");
		patientsInPMTCTCCMotherUsingMethodOfFamilyPlanning.addParameter(new Parameter("startDate","startDate",Date.class));
		patientsInPMTCTCCMotherUsingMethodOfFamilyPlanning.addParameter(new Parameter("endDate","endDate",Date.class));
		patientsInPMTCTCCMotherUsingMethodOfFamilyPlanning.addParameter(new Parameter("onDate", "onDate", Date.class));
		patientsInPMTCTCCMotherUsingMethodOfFamilyPlanning.getSearches().put("2",new Mapped(inPMTCTCCMotherOnEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
		patientsInPMTCTCCMotherUsingMethodOfFamilyPlanning .getSearches().put( "3",new Mapped(patientUsingMethodOfFamilyPlanning, ParameterizableUtil .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsInPMTCTCCMotherUsingMethodOfFamilyPlanning.setCompositionString("2 AND 3");
		
		
		
		
		
		
		
		CohortIndicator onePMTCTCCMother = Indicators.newCountIndicator("PMTCTCCMotherQ: Enrolled in PMTCT C C Mother",
		    enrolledInPMTCTCCMotherByStartEndDate,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator twoPMTCTCCMother = Indicators.newCountIndicator(
		    "PMTCTCCMotherQ: Enrolled in PMTCT C C Mother on the end date", inPMTCTCCMotherOnEndDate,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		CohortIndicator threePMTCTCCMother = Indicators.newCountIndicator(
		    "PMTCTCCMotherQ: Enrolled in PMTCT C C Mother on or before the end date", enrolledInPMTCTCCMotherOnOrBeforeEndDate,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator fourPMTCTCCMother = Indicators.newCountIndicator(
		    "PMTCTCCMotherQ: Enrolled in PMTCT C C Mother on the end date", activePatientOnArtInPMTCTCCMother,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		CohortIndicator fivePMTCTCCMother = Indicators
		        .newCountIndicator(
		            "PMTCTCCMotherQ: patients With Two Or More Adult HIV flowsheet In PMTCT C C Mother",
		            patientsWithTwoOrMoreAdultHIVEncountersInPMTCTCCMother,
		            ParameterizableUtil
		                    .createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate},onDate=${endDate}"));
		
		CohortIndicator fivePMTCTCCMotherD = Indicators
		        .newCountIndicator(
		            "PMTCTCCMotherQ: patients In PMTCT C C Mother but not enrolled between startdate and enddate",
		            patientsInPMTCTCCMotherNotEnrolledByStartAndEndDate,
		            ParameterizableUtil
		                    .createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate},onDate=${endDate}"));
		
		CohortIndicator sixPMTCTCCMother = Indicators.newCountIndicator(
		    "PMTCTCCMotherQ: Enrolled in PMTCT C C Mother on the end date Using Method Of Family Planning", patientsInPMTCTCCMotherUsingMethodOfFamilyPlanning,
		    ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		
		
		dsd.addColumn(
		    "CCMQ1",
		    "Enrolled in PMTCT C C Mother",
		    new Mapped(onePMTCTCCMother, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn(
		    "CCMQ2",
		    "Enrolled in PMTCT C C Mother on the end date",
		    new Mapped(twoPMTCTCCMother, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("CCMQ3", "Enrolled in PMTCT C C Mother on or before the end date", new Mapped(threePMTCTCCMother,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn(
		    "CCMQ4",
		    "Enrolled in PMTCT C C Mother on the end date",
		    new Mapped(fourPMTCTCCMother, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("CCMQ5", "patients With Two Or More Adult HIV flowsheet In PMTCT C C Mother", new Mapped(
		        fivePMTCTCCMother, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		
		dsd.addColumn("CCMQ5D", "patients In PMTCT C C Mother but not enrolled between startdate and enddate", new Mapped(
		        fivePMTCTCCMotherD, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("CCMQ6", "Enrolled in PMTCT C C Mother on the end date Using Method Of Family Planning", new Mapped(
	        fivePMTCTCCMotherD, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
	    "");
		
		
		
		
		//======================================================================================
		// Viral Load
		//======================================================================================
		
		InProgramCohortDefinition currentlyInAnyHIVProgram = Cohorts.createInProgramParameterizableByDate(
		    "hivQD: In All HIV Programs", hivPrograms, "onDate");
		
		NumericObsCohortDefinition viralLoad = Cohorts.createNumericObsCohortDefinition("obsQD: Viral Load recorded",
		    onOrAfterOnOrBefore, viralLoadConcept, 0, null, TimeModifier.ANY);;
		
		NumericObsCohortDefinition viralLoadFailure = Cohorts.createNumericObsCohortDefinition("obsQD: weight recorded",
		    onOrAfterOnOrBefore, viralLoadConcept, 10000, RangeComparator.GREATER_EQUAL, TimeModifier.ANY);
		
		CompositionCohortDefinition currentlyInHIVUnder15 = new CompositionCohortDefinition();
		currentlyInHIVUnder15.setName("hivQD: In all programs under 15 and on ART more than 12 months");
		currentlyInHIVUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		currentlyInHIVUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		currentlyInHIVUnder15.getSearches().put("2",
		    new Mapped(currentlyInAnyHIVProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		currentlyInHIVUnder15.getSearches().put("3",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		currentlyInHIVUnder15.getSearches().put("4",
		    new Mapped(artOnOrBefore, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate-1y}")));
		currentlyInHIVUnder15.setCompositionString("NOT 1 AND 2 AND 3 AND 4");
		
		CompositionCohortDefinition currentlyInHIVOver15 = new CompositionCohortDefinition();
		currentlyInHIVOver15.setName("hivQD: In all programs over 15 and on ART more than 12 months");
		currentlyInHIVOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		currentlyInHIVOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		currentlyInHIVOver15.getSearches().put("2",
		    new Mapped(currentlyInAnyHIVProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		currentlyInHIVOver15.getSearches().put("3",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		currentlyInHIVOver15.getSearches().put("4",
		    new Mapped(artOnOrBefore, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate-1y}")));
		currentlyInHIVOver15.setCompositionString("1 AND 2 AND 3 AND 4");
		
		CompositionCohortDefinition eligibleWithViralU15 = new CompositionCohortDefinition();
		eligibleWithViralU15.setName("hivQD: eligible with viral load under 15");
		eligibleWithViralU15.addParameter(new Parameter("endDate", "endDate", Date.class));
		eligibleWithViralU15.getSearches().put("1",
		    new Mapped(currentlyInHIVUnder15, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		eligibleWithViralU15.getSearches().put(
		    "2",
		    new Mapped(viralLoad, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-1y}")));
		eligibleWithViralU15.setCompositionString("1 AND 2");
		
		CompositionCohortDefinition eligibleWithViralO15 = new CompositionCohortDefinition();
		eligibleWithViralO15.setName("hivQD: eligible with viral load over 15");
		eligibleWithViralO15.addParameter(new Parameter("endDate", "endDate", Date.class));
		eligibleWithViralO15.getSearches().put("1",
		    new Mapped(currentlyInHIVOver15, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		eligibleWithViralO15.getSearches().put(
		    "2",
		    new Mapped(viralLoad, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-1y}")));
		eligibleWithViralO15.setCompositionString("1 AND 2");
		
		CompositionCohortDefinition eligibleWithViralFailureU15 = new CompositionCohortDefinition();
		eligibleWithViralFailureU15.setName("hivQD: eligible with viral load failure under 15");
		eligibleWithViralFailureU15.addParameter(new Parameter("endDate", "endDate", Date.class));
		eligibleWithViralFailureU15.getSearches().put("1",
		    new Mapped(currentlyInHIVUnder15, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		eligibleWithViralFailureU15.getSearches().put(
		    "2",
		    new Mapped(viralLoadFailure, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-1y}")));
		eligibleWithViralFailureU15.setCompositionString("1 AND 2");
		
		CompositionCohortDefinition eligibleWithViralFailureO15 = new CompositionCohortDefinition();
		eligibleWithViralFailureO15.setName("hivQD: eligible with viral load failure over 15");
		eligibleWithViralFailureO15.addParameter(new Parameter("endDate", "endDate", Date.class));
		eligibleWithViralFailureO15.getSearches().put("1",
		    new Mapped(currentlyInHIVOver15, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		eligibleWithViralFailureO15.getSearches().put(
		    "2",
		    new Mapped(viralLoadFailure, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-1y}")));
		eligibleWithViralFailureO15.setCompositionString("1 AND 2");
		
	
		CohortIndicator one = Indicators.newCountIndicator("hivQD: In all programs under 15 and on ART more than 12 months_", currentlyInHIVUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator two = Indicators.newCountIndicator("hivQD: In all programs over 15 and on ART more than 12 months_", currentlyInHIVOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator three = Indicators.newCountIndicator("hivQD: eligible with viral load under 15_", eligibleWithViralU15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator four = Indicators.newCountIndicator("hivQD: eligible with viral load over 15_", eligibleWithViralO15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator five = Indicators.newCountIndicator("hivQD: eligible with viral load failure under 15_", eligibleWithViralFailureU15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator six = Indicators.newCountIndicator("hivQD: eligible with viral load failure over 15_", eligibleWithViralFailureO15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
		dsd.addColumn("VQ2AD", "Eligible Over 15",
		    new Mapped(two, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("VQ2CD", "Eligible Under 15",
		    new Mapped(one, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("VQ2C", "Viral Load Under 15",
		    new Mapped(three, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("VQ2A", "Viral Load Over 15",
		    new Mapped(four, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("VQ3C", "Viral Load Failure Over 15",
		    new Mapped(five, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("VQ3A", "Viral Load Failure Under 15",
		    new Mapped(six, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		
	}
	
	private void setUpProperties() {
		hivProgram = gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		pediHivProgram = gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		pmtctProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		pmtctCombinedMotherProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_MOTHER_PROGRAM);
		pmtctCombinedInfantProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM);
		
		pediPreArt = gp.getProgramWorkflowState(GlobalPropertiesManagement.FOLLOWING_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		hivPreArt = gp.getProgramWorkflowState(GlobalPropertiesManagement.FOLLOWING_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		pediArt = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		hivArt = gp.getProgramWorkflowState(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
		    GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW, GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		hivPrograms.add(hivProgram);
		hivPrograms.add(pediHivProgram);
		hivPrograms.add(pmtctProgram);
		hivPrograms.add(pmtctCombinedMotherProgram);
		
		hivProgramsExcPMTCT.add(hivProgram);
		hivProgramsExcPMTCT.add(pediHivProgram);
		
		preArtWorkflowStates.add(pediPreArt);
		preArtWorkflowStates.add(hivPreArt);
		
		artWorkflowStates.add(pediArt);
		artWorkflowStates.add(hivArt);
		
		hivEncounterTypes = gp.getEncounterTypeList(GlobalPropertiesManagement.HIV_ENCOUNTER_TYPES);
		
		cd4Concept = gp.getConcept(GlobalPropertiesManagement.CD4_TEST);
		weightConcept = gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT);
		
		onOrAfterOnOrBefore.add("onOrAfter");
		onOrAfterOnOrBefore.add("onOrBefore");
		
		adultHIVEncounterType = gp.getEncounterType(GlobalPropertiesManagement.ADULT_FLOWSHEET_ENCOUNTER);
		exposedInfantEncountertype = gp.getEncounterType(GlobalPropertiesManagement.EXPOSED_INFANT_ENCOUNTER);
		
		feedingWorkflowStates.add(gp.getProgramWorkflowState(
		    GlobalPropertiesManagement.ARTIFICIAL_FEEDING_9_18_MONTHS_STATE,
		    GlobalPropertiesManagement.FEEDING_GROUP_WORKFLOW, GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM));
		feedingWorkflowStates.add(gp.getProgramWorkflowState(GlobalPropertiesManagement.ARTIFICIAL_FEEDING_6_9_MONTHS_STATE,
		    GlobalPropertiesManagement.FEEDING_GROUP_WORKFLOW, GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM));
		feedingWorkflowStates.add(gp.getProgramWorkflowState(GlobalPropertiesManagement.ARTIFICIAL_FEEDING_0_6_MONTHS_STATE,
		    GlobalPropertiesManagement.FEEDING_GROUP_WORKFLOW, GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM));
		
		hivPCR = gp.getConcept(GlobalPropertiesManagement.DBS_CONCEPT);
		
		hivPositive = gp.getConcept(GlobalPropertiesManagement.POSITIVE_HIV_TEST_ANSWER);
		seroTest = gp.getConcept(GlobalPropertiesManagement.SERO_TEST);
		ctx= gp.getConcept(GlobalPropertiesManagement.CTX);
		nevirapine= gp.getConcept(GlobalPropertiesManagement.NEVIRAPINE_DRUG);
		
		methodOfFamilyPlanning= gp.getConcept(GlobalPropertiesManagement.METHOD_OF_FAMILY_PLANNING);
		usingCondoms= gp.getConcept(GlobalPropertiesManagement.USING_CONDOMS);
		naturalFamilyPlanning= gp.getConcept(GlobalPropertiesManagement.NATURAL_FAMILY_PLANNING);	
		viralLoadConcept = gp.getConcept(GlobalPropertiesManagement.VIRAL_LOAD_TEST);
		
	}
}
