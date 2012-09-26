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
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
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

public class SetupQuarterlyCrossSiteIndicatorByDistrictReport {
	
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
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = createCrossSiteReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd,
		    "PIH_Quaterly_Cross_Region_Indicator_Form.xls", "PIH Quarterly Indicator Form (Excel)_", null);
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,dataset:PIH_Quarterly_Individual_District_Indicator Data Set");
		design.setProperties(props);
		h.saveReportDesign(design);
		
		//		ReportDefinition rd2 = createGraphReportDefinition();
		//		ReportDesign design2 = h.createRowPerPatientXlsOverviewReportDesign(rd2, "PIH_Quaterly_Cross_Graph_Form.xls", "PIH Quarterly Indicator Graph (Excel)_", null);
		//		Properties props2 = new Properties();
		//		props2.put("repeatingSections", "sheet:1,column:4,dataset:PIH_Quarterly_Individual_District_Indicator Graph Data Set");
		//		design2.setProperties(props2);
		//		h.saveReportDesign(design2);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PIH Quarterly Indicator Form (Excel)_".equals(rd.getName())
			        || "PIH Quarterly Indicator Graph (Excel)_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("PIH-Boston Indicators-Quarterly");
		//h.purgeReportDefinition("PIH_Quarterly_Individual_District_Indicator_Graph");
	}
	
	private ReportDefinition createCrossSiteReportDefinition() {
		// PIH Quarterly Cross Site Indicator Report
		ReportDefinition rd = new ReportDefinition();
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");
		rd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		
		rd.setName("PIH-Boston Indicators-Quarterly");
		
		rd.addDataSetDefinition(createDataSet(),
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));
		
		h.saveReportDefinition(rd);
		
		return rd;
	}
	
	//	private ReportDefinition createGraphReportDefinition() {
	//		// PIH Quarterly Cross Site Indicator Report
	//		
	//		ReportDefinition rd = new ReportDefinition();
	//		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
	////		Properties properties = new Properties();
	////		properties.setProperty("hierarchyFields", "countyDistrict:District");
	////		rd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
	//		rd.addParameter(new Parameter("location", "Health Center", Location.class));
	//		rd.addParameter(new Parameter("quarters", "Number of Quarters", Integer.class));
	//		
	//		rd.setName("PIH_Quarterly_Individual_District_Indicator_Graph");
	//		
	//		rd.addDataSetDefinition(createGraphDataSet(), ParameterizableUtil.createParameterMappings("endDate=${endDate},location=${location},quarters=${quarters}"));
	//		
	//		h.saveReportDefinition(rd);
	//		
	//		return rd;
	//	}
	
	private LocationHierachyIndicatorDataSetDefinition createDataSet() {
		
		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(createBaseDataSet());
		ldsd.setName("PIH_Quarterly_Individual_District_Indicator Data Set");
		ldsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		return ldsd;
	}
	
	//	private PeriodIndicatorDataSetDefinition createGraphDataSet()
	//	{
	//		
	//		PeriodIndicatorDataSetDefinition pidsd = new PeriodIndicatorDataSetDefinition(createBaseDataSet());
	//		pidsd.setName("PIH_Quarterly_Individual_District_Indicator Graph Data Set");
	//		pidsd.addParameter(new Parameter("endDate", "End Date", Date.class));
	//		pidsd.addParameter(new Parameter("location", "HealthCenter", Location.class));
	//		pidsd.addParameter(new Parameter("quarters", "Number of Quarters", Integer.class));
	//		
	//		return pidsd;
	//	}
	
	private CohortIndicatorDataSetDefinition createBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("PIH_Quarterly_Individual_District_Indicator Cohort Data Set");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		createIndicators(dsd);
		return dsd;
	}
	
	private void createIndicators(CohortIndicatorDataSetDefinition dsd) {
		
		InProgramCohortDefinition inAnyHIVProgram = Cohorts.createInProgramParameterizableByDate(
		    "hivQD: In All HIV Programs", hivPrograms, "onOrBefore");
		
		InProgramCohortDefinition inAnyHIVAfterStartDate = Cohorts.createInProgramParameterizableByDate(
		    "hivQD: new Patients enrolled in HIV Program during period", hivPrograms, onOrAfterOnOrBefore);
		
		InProgramCohortDefinition inAdultOrPediHIVProgram = Cohorts.createInProgramParameterizableByDate(
		    "hivQD: In AdultOrPedi HIV Programs", hivProgramsExcPMTCT, "onOrBefore");
		
		InProgramCohortDefinition inAdultOrPediHIVOnDateProgram = Cohorts.createInProgramParameterizableByDate(
		    "hivQD: In AdultOrPedi HIV Programs on Date", hivProgramsExcPMTCT, "onDate");
		
		AgeCohortDefinition over15Cohort = Cohorts.createOver15AgeCohort("ageQD: Over 15");
		AgeCohortDefinition under15Cohort = Cohorts.createUnder15AgeCohort("ageQD: Under 15");
		
		InStateCohortDefinition preArt = Cohorts.createInCurrentState("hivQD: preArt", preArtWorkflowStates);
		
		InStateCohortDefinition artOnOrBefore = Cohorts.createInCurrentState("hivQD: ever on ART", artWorkflowStates,
		    "onOrBefore");
		
		InStateCohortDefinition artCurrently = Cohorts.createInCurrentState("hivQD: currently on ART", artWorkflowStates);
		
		InStateCohortDefinition artStartedInPeriod = Cohorts.createInCurrentState("hivQD: started on ART",
		    artWorkflowStates, onOrAfterOnOrBefore);
		
		EncounterCohortDefinition encounter = Cohorts.createEncounterParameterizedByDate("encounterQD: visit in period",
		    onOrAfterOnOrBefore, hivEncounterTypes);
		
		NumericObsCohortDefinition cd4 = Cohorts.createNumericObsCohortDefinition("obsQD: CD4 count recorded",
		    onOrAfterOnOrBefore, cd4Concept, 0, null, TimeModifier.ANY);
		
		NumericObsCohortDefinition weight = Cohorts.createNumericObsCohortDefinition("obsQD: weight recorded",
		    onOrAfterOnOrBefore, weightConcept, 0, null, TimeModifier.ANY);
		
		CompositionCohortDefinition inHIVUnder15 = new CompositionCohortDefinition();
		inHIVUnder15.setName("hivQD: In all programs under 15");
		inHIVUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		inHIVUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		inHIVUnder15.getSearches().put("2",
		    new Mapped(inAnyHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		inHIVUnder15.setCompositionString("NOT 1 AND 2");
		
		CompositionCohortDefinition inHIVOver15 = new CompositionCohortDefinition();
		inHIVOver15.setName("hivQD: In all programs over 15");
		inHIVOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		inHIVOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		inHIVOver15.getSearches().put("2",
		    new Mapped(inAnyHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		inHIVOver15.setCompositionString("1 AND 2");
		
		CompositionCohortDefinition newInHIVUnder15 = new CompositionCohortDefinition();
		newInHIVUnder15.setName("hivQD: new in any Hiv program in period under 15");
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
		newInHIVOver15.setName("hivQD: new in any Hiv program in period over 15");
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
		followingUnder15.setName("hivQD: preArt in Hiv program in period under 15");
		followingUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		followingUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		followingUnder15.getSearches().put("2",
		    new Mapped(preArt, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		followingUnder15.getSearches().put("3",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		followingUnder15.setCompositionString("NOT 1 AND 2 AND 3");
		
		CompositionCohortDefinition followingOver15 = new CompositionCohortDefinition();
		followingOver15.setName("hivQD: preArt in Hiv program in period over 15");
		followingOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		followingOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		followingOver15.getSearches().put("2",
		    new Mapped(preArt, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		followingOver15.getSearches().put("3",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		followingOver15.setCompositionString("1 AND 2 AND 3");
		
		CompositionCohortDefinition preArtWithAVisitUnder15 = new CompositionCohortDefinition();
		preArtWithAVisitUnder15.setName("hivQD: preArt in Hiv program with a visit in period - 3 months and under 15");
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
		preArtActiveUnder15.setName("hivQD: Active preArt in Hiv program with a visit in period - 12 months and under 15");
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
		preArtActiveOver15.setName("hivQD: Active preArt in Hiv program with a visit in period - 12 months and over 15");
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
		artActiveOver15.setName("hivQD: Active art in Hiv program with a visit in period - 12 months and over 15");
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
		artActiveUnder15.setName("hivQD: Active art in Hiv program with a visit in period - 12 months and under 15");
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
		preArtWithAVisitOver15.setName("hivQD: preArt in Hiv program with a visit in period -3 months and over 15");
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
		artUnder15.setName("hivQD: ever taking ART before end date and under 15");
		artUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		artUnder15.getSearches().put("2",
		    new Mapped(artOnOrBefore, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		artUnder15.getSearches().put("3",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		artUnder15.setCompositionString("NOT 1 AND 2 AND 3");
		
		CompositionCohortDefinition artOver15 = new CompositionCohortDefinition();
		artOver15.setName("hivQD: ever taking ART before end date and over 15");
		artOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		artOver15.getSearches().put("2",
		    new Mapped(artOnOrBefore, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		artOver15.getSearches().put("3",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		artOver15.setCompositionString("1 AND 2 AND 3");
		
		CompositionCohortDefinition currentArtUnder15 = new CompositionCohortDefinition();
		currentArtUnder15.setName("hivQD: currently taking ART before end date and under 15");
		currentArtUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		currentArtUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		currentArtUnder15.getSearches().put("2",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		currentArtUnder15.getSearches().put("3",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		currentArtUnder15.setCompositionString("NOT 1 AND 2 AND 3");
		
		CompositionCohortDefinition currentArtOver15 = new CompositionCohortDefinition();
		currentArtOver15.setName("hivQD: currently taking ART before end date and over 15");
		currentArtOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		currentArtOver15.getSearches().put("1",
		    new Mapped(under15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		currentArtOver15.getSearches().put("2",
		    new Mapped(artCurrently, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		currentArtOver15.getSearches().put("3",
		    new Mapped(inAdultOrPediHIVOnDateProgram, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
		currentArtOver15.setCompositionString("Not 1 AND 2 AND 3");
		
		CompositionCohortDefinition artWithAVisitUnder15 = new CompositionCohortDefinition();
		artWithAVisitUnder15.setName("hivQD: on ART in Hiv program with a visit in period -3 months and under 15");
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
		artWithAVisitOver15.setName("hivQD: on ART in Hiv program with a visit in period -3 months and over 15");
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
		startedArtOver15.setName("hivQD: started taking ART in period over 15");
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
		startedArtUnder15.setName("hivQD: started taking ART in period under 15");
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
		preArtWithACD4Under15.setName("hivQD: preArt in Hiv program with a CD4 count in period -3 months and under 15");
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
		preArtWithACD4Over15.setName("hivQD: preArt in Hiv program with a CD4 count in period -3 months and over 15");
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
		artWithACD4Over15.setName("hivQD: on Art in Hiv program with a CD4 count in period -3 months and over 15");
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
		artWithACD4Under15.setName("hivQD: on Art in Hiv program with a CD4 count in period - 3 months and under 15");
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
		hivWithAWeightUnder15.setName("hivQD: weight recorded in period under 15");
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
		hivWithAWeightOver15.setName("hivQD: weight recorded in period over 15");
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
		hivOver15.setName("hivQD: HIV Adult or Pedi and over 15");
		hivOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivOver15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		hivOver15.getSearches().put("2",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		hivOver15.setCompositionString("1 AND 2");
		
		CompositionCohortDefinition hivUnder15 = new CompositionCohortDefinition();
		hivUnder15.setName("hivQD: HIV Adult or Pedi and under 15");
		hivUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivUnder15.getSearches().put("1",
		    new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${endDate}")));
		hivUnder15.getSearches().put("2",
		    new Mapped(inAdultOrPediHIVProgram, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
		hivUnder15.setCompositionString("NOT 1 AND 2");
		
		CompositionCohortDefinition hivVisitOver15 = new CompositionCohortDefinition();
		hivVisitOver15.setName("hivQD: in Hiv program with a visit in period and over 15");
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
		hivVisitUnder15.setName("hivQD: in Hiv program with a visit in period and under 15");
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
		
		CohortIndicator one = Indicators.newCountIndicator("hivQD: In all programs over 15_", inHIVOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator two = Indicators.newCountIndicator("hivQD: In all programs under 15_", inHIVUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator three = Indicators.newCountIndicator("hivQD: new in any Hiv program in period over 15_", newInHIVOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator four = Indicators.newCountIndicator("hivQD: new in any Hiv program in period under 15_", newInHIVUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator five = Indicators.newCountIndicator("hivQD: preArt in Hiv program in period over 15_", followingOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator six = Indicators.newCountIndicator("hivQD: preArt in Hiv program in period under 15_", followingUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator seven = Indicators.newCountIndicator(
		    "hivQD: preArt in Hiv program with a visit in period -3 months and over 15_", preArtWithAVisitOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator eight = Indicators.newCountIndicator(
		    "hivQD: preArt in Hiv program with a visit in period -3 months and under 15_", preArtWithAVisitUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator nine = Indicators.newCountIndicator("hivQD: ever taking ART before end date and over 15_", artOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator ten = Indicators.newCountIndicator("hivQD: ever taking ART before end date and under 15_", artUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator eleven = Indicators.newCountIndicator("hivQD: currently taking ART before end date and over 15_",
		    currentArtOver15, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator twelve = Indicators.newCountIndicator("hivQD: currently taking ART before end date and under 15_",
		    currentArtUnder15, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		CohortIndicator thirteen = Indicators.newCountIndicator(
		    "hivQD: on ART in Hiv program with a visit in period -3 months and over 15_", artWithAVisitOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator fourteen = Indicators.newCountIndicator(
		    "hivQD: on ART in Hiv program with a visit in period -3 months and under 15_", artWithAVisitUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator fifteen = Indicators.newCountIndicator("hivQD: started taking ART in period over 15_", startedArtOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator sixteen = Indicators.newCountIndicator("hivQD: started taking ART in period under 15_", startedArtUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator seventeen = Indicators.newCountIndicator(
		    "hivQD: preArt in Hiv program with a CD4 count in period -3 months and over 15_", preArtWithACD4Over15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator eighteen = Indicators.newCountIndicator(
		    "hivQD: preArt in Hiv program with a CD4 count in period -3 months and under 15_", preArtWithACD4Under15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator nineteen = Indicators.newCountIndicator(
		    "hivQD: on Art in Hiv program with a CD4 count in period -3 months and over 15_", artWithACD4Over15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twenty = Indicators.newCountIndicator(
		    "hivQD: on Art in Hiv program with a CD4 count in period -3 months and under 15_", artWithACD4Under15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twentyone = Indicators.newCountIndicator("hivQD: weight recorded in period over 15_", hivWithAWeightOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twentytwo = Indicators.newCountIndicator("hivQD: weight recorded in period under 15_", hivWithAWeightUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twentythree = Indicators.newCountIndicator("hivQD: HIV visit and over 15_", hivVisitOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twentyfour = Indicators.newCountIndicator("hivQD: HIV visit and under 15_", hivVisitUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twentyfive = Indicators.newCountIndicator("hivQD: Active art under 15_", artActiveUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twentysix = Indicators.newCountIndicator("hivQD: Active art over 15_", artActiveOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twentyseven = Indicators.newCountIndicator("hivQD: Active preArt over 15_", preArtActiveOver15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		CohortIndicator twentyeight = Indicators.newCountIndicator("hivQD: Active preArt under 15_", preArtActiveUnder15,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}"));
		
		dsd.addColumn("1", "In All HIV Programs Over 15",
		    new Mapped(one, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("2", "In All HIV Programs Under 15",
		    new Mapped(two, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("3", "New In All HIV Programs Over 15",
		    new Mapped(three, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("4", "New In All HIV Programs Under 15",
		    new Mapped(four, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("5", "Pre ART Over 15",
		    new Mapped(five, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("6", "Pre ART Under 15",
		    new Mapped(six, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("7", "Pre ART Visit in last 2 quarters Over 15",
		    new Mapped(seven, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("8", "Pre ART Visit in last 2 quarters Under 15",
		    new Mapped(eight, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("9", "Ever started on Art at end of review and Over 15",
		    new Mapped(nine, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("10", "Ever started on Art at end of review and Under 15",
		    new Mapped(ten, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("11", "Currently on Art at end of review and Over 15",
		    new Mapped(eleven, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("12", "Currently on Art at end of review and Under 15",
		    new Mapped(twelve, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("13", "Currently on Art and had a visit in last 2 quarters and Over 15", new Mapped(thirteen,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("14", "Currently on Art and had a visit in last 2 quarters and Under 15", new Mapped(fourteen,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("15", "Started on Art and Over 15",
		    new Mapped(fifteen, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("16", "Started on Art and Under 15",
		    new Mapped(sixteen, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("17", "Pre ART and had a cd4 recorded in last 2 quarters and Over 15", new Mapped(seventeen,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("18", "Pre ART and had a cd4 recorded in last 2 quarters and Under 15", new Mapped(eighteen,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("19", "ART and had a cd4 recorded in last 2 quarters and Over 15", new Mapped(nineteen,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("20", "ART and had a cd4 recorded in last 2 quarters and Under 15", new Mapped(twenty,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("21", "HIV and had a weight recorded in last quarters and Over 15", new Mapped(twentyone,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn("22", "HIV and had a weight recorded in last quarters and Under 15", new Mapped(twentytwo,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
		dsd.addColumn(
		    "23",
		    "HIV visit and Over 15",
		    new Mapped(twentythree, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn(
		    "24",
		    "HIV and Under 15",
		    new Mapped(twentyfour, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn(
		    "25",
		    "Active ART and under 15",
		    new Mapped(twentyfive, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn(
		    "26",
		    "Active ART and over 15",
		    new Mapped(twentysix, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn(
		    "27",
		    "Active PreART and over 15",
		    new Mapped(twentyseven, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn(
		    "28",
		    "Active PreART and under 15",
		    new Mapped(twentyeight, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		
		
	}
	
	private void setUpProperties() {
		hivProgram = gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		pediHivProgram = gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		pmtctProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		pmtctCombinedMotherProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_MOTHER_PROGRAM);
		
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
	}
}
