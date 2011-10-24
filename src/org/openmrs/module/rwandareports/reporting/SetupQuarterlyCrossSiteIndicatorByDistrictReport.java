package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;


public class SetupQuarterlyCrossSiteIndicatorByDistrictReport {
	
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupQuarterlyCrossSiteIndicatorByDistrictReport(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		setUpGlobalProperties();
		
		createCohortDefinitions();
		createCompositionCohortDefinitions();
		createIndicators();
		ReportDefinition rd = createReportDefinition();
		h.createXlsOverview(rd, "PIH_Quaterly_Cross_Region_Indicator_Form.xls", "PIH Quarterly District Indicator Form (Excel)_", null);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PIH Quarterly District Indicator Form (Excel)_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(PeriodIndicatorReportDefinition.class, "PIH_Quarterly_Individual_District_Indicator");
		
		h.purgeDefinition(DataSetDefinition.class, "PIH_Quarterly_Individual_District_Indicator Data Set");
		
		h.purgeDefinition(CohortIndicator.class, "hivQD: In all programs over 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: In all programs under 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: new in any Hiv program in period over 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: new in any Hiv program in period under 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: preArt in Hiv program in period over 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: preArt in Hiv program in period under 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: preArt in Hiv program with a visit in period -3 months and over 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: preArt in Hiv program with a visit in period -3 months and under 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: ever taking ART before end date and over 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: ever taking ART before end date and under 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: currently taking ART before end date and over 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: currently taking ART before end date and under 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: on ART in Hiv program with a visit in period -3 months and over 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: on ART in Hiv program with a visit in period -3 months and under 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: started taking ART in period over 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: started taking ART in period under 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: preArt in Hiv program with a CD4 count in period and over 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: preArt in Hiv program with a CD4 count in period -3 months and under 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: on Art in Hiv program with a CD4 count in period -3 months and over 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: on Art in Hiv program with a CD4 count in period -3 months and under 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: preArt in Hiv program with a CD4 count in period -3 months and over 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: weight recorded in period over 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: weight recorded in period under 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: HIV visit and over 15_");
		h.purgeDefinition(CohortIndicator.class, "hivQD: HIV visit and under 15_");
		
		h.purgeDefinition(CohortDefinition.class, "hivQDLocation: Patients at location");
		h.purgeDefinition(CohortDefinition.class, "hivQD: In All HIV Programs");
		h.purgeDefinition(CohortDefinition.class, "hivQD: In AdultOrPedi HIV Programs");
		h.purgeDefinition(CohortDefinition.class, "hivQD: In AdultOrPedi HIV Programs On Date");
		h.purgeDefinition(CohortDefinition.class, "hivQD: new Patients enrolled in HIV Program during period");
		h.purgeDefinition(CohortDefinition.class, "ageQD: Over 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: preArt");
		h.purgeDefinition(CohortDefinition.class, "hivQD: ever on ART");
		h.purgeDefinition(CohortDefinition.class, "hivQD: currently on ART");
		h.purgeDefinition(CohortDefinition.class, "hivQD: started on ART");
		h.purgeDefinition(CohortDefinition.class, "encounterQD: visit in period");
		h.purgeDefinition(CohortDefinition.class, "obsQD: CD4 count recorded");
		h.purgeDefinition(CohortDefinition.class, "obsQD: weight recorded");
		h.purgeDefinition(CohortDefinition.class, "hivQD: In all programs under 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: In all programs over 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: new in any Hiv program in period under 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: new in any Hiv program in period over 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: preArt in Hiv program in period under 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: preArt in Hiv program in period over 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: preArt in Hiv program with a visit in period - 3 months and under 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: preArt in Hiv program with a visit in period -3 months and over 15");
		
		h.purgeDefinition(CohortDefinition.class, "hivQD: ever taking ART before end date and under 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: ever taking ART before end date and over 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: currently taking ART before end date and under 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: currently taking ART before end date and over 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: on ART in Hiv program with a visit in period -3 months and under 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: on ART in Hiv program with a visit in period -3 months and over 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: started taking ART in period over 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: started taking ART in period under 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: preArt in Hiv program with a CD4 count in period -3 months and under 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: preArt in Hiv program with a CD4 count in period -3 months and over 15");
		
		h.purgeDefinition(CohortDefinition.class, "hivQD: on Art in Hiv program with a CD4 count in period - 3 months and under 15");
		                                        
		h.purgeDefinition(CohortDefinition.class, "hivQD: on Art in Hiv program with a CD4 count in period -3 months and over 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: weight recorded in period under 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: weight recorded in period over 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: HIV Adult or Pedi and over 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: HIV Adult or Pedi and under 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: in Hiv program with a visit in period and under 15");
		h.purgeDefinition(CohortDefinition.class, "hivQD: in Hiv program with a visit in period and over 15");
	}
	
	
	private ReportDefinition createReportDefinition() {
		// PIH Quarterly Cross Site Indicator Report
		
		
		PeriodIndicatorReportDefinition rd = new PeriodIndicatorReportDefinition();
		rd.removeParameter(ReportingConstants.START_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.END_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.LOCATION_PARAMETER);
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		Properties properties = new Properties();
		//properties.setProperty("customHandler", "org.openmrs.module.htmlwidgets.web.handler.LocationHierarchyHandler");
		properties.setProperty("hierarchyField", "countyDistrict");
		rd.addParameter(new Parameter("location", "District", LocationHierarchy.class, properties));
		
		rd.setName("PIH_Quarterly_Individual_District_Indicator");
		
		rd.setupDataSetDefinition();
		
		rd.setBaseCohortDefinition(h.cohortDefinition("hivQDLocation: Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
		rd.addIndicator("1", "In All HIV Programs Over 15", h.cohortIndicator("hivQD: In all programs over 15_"));
		rd.addIndicator("2", "In All HIV Programs Under 15", h.cohortIndicator("hivQD: In all programs under 15_"));
		rd.addIndicator("3", "New In All HIV Programs Over 15", h.cohortIndicator("hivQD: new in any Hiv program in period over 15_"));
		rd.addIndicator("4", "New In All HIV Programs Under 15", h.cohortIndicator("hivQD: new in any Hiv program in period under 15_"));
		rd.addIndicator("5", "Pre ART Over 15", h.cohortIndicator("hivQD: preArt in Hiv program in period over 15_"));
		rd.addIndicator("6", "Pre ART Under 15", h.cohortIndicator("hivQD: preArt in Hiv program in period under 15_"));
		rd.addIndicator("7", "Pre ART Visit in last 2 quarters Over 15", h.cohortIndicator("hivQD: preArt in Hiv program with a visit in period -3 months and over 15_"));
		rd.addIndicator("8", "Pre ART Visit in last 2 quarters Under 15", h.cohortIndicator("hivQD: preArt in Hiv program with a visit in period -3 months and under 15_"));
		rd.addIndicator("9", "Ever started on Art at end of review and Over 15", h.cohortIndicator("hivQD: ever taking ART before end date and over 15_"));
		rd.addIndicator("10", "Ever started on Art at end of review and Under 15", h.cohortIndicator("hivQD: ever taking ART before end date and under 15_"));
		rd.addIndicator("11", "Currently on Art at end of review and Over 15", h.cohortIndicator("hivQD: currently taking ART before end date and over 15_"));
		rd.addIndicator("12", "Currently on Art at end of review and Under 15", h.cohortIndicator("hivQD: currently taking ART before end date and under 15_"));
		rd.addIndicator("13", "Currently on Art and had a visit in last 2 quarters and Over 15", h.cohortIndicator("hivQD: on ART in Hiv program with a visit in period -3 months and over 15_"));
		rd.addIndicator("14", "Currently on Art and had a visit in last 2 quarters and Under 15", h.cohortIndicator("hivQD: on ART in Hiv program with a visit in period -3 months and under 15_"));
		rd.addIndicator("15", "Started on Art and Over 15", h.cohortIndicator("hivQD: started taking ART in period over 15_"));
		rd.addIndicator("16", "Started on Art and Under 15", h.cohortIndicator("hivQD: started taking ART in period under 15_"));
		rd.addIndicator("17", "Pre ART and had a cd4 recorded in last 2 quarters and Over 15", h.cohortIndicator("hivQD: preArt in Hiv program with a CD4 count in period -3 months and over 15_"));
		rd.addIndicator("18", "Pre ART and had a cd4 recorded in last 2 quarters and Under 15", h.cohortIndicator("hivQD: preArt in Hiv program with a CD4 count in period -3 months and under 15_"));
		rd.addIndicator("19", "ART and had a cd4 recorded in last 2 quarters and Over 15", h.cohortIndicator("hivQD: on Art in Hiv program with a CD4 count in period -3 months and over 15_"));
		rd.addIndicator("20", "ART and had a cd4 recorded in last 2 quarters and Under 15", h.cohortIndicator("hivQD: on Art in Hiv program with a CD4 count in period -3 months and under 15_"));
		rd.addIndicator("21", "HIV and had a weight recorded in last quarters and Over 15", h.cohortIndicator("hivQD: weight recorded in period over 15_"));
		rd.addIndicator("22", "HIV and had a weight recorded in last quarters and Under 15", h.cohortIndicator("hivQD: weight recorded in period under 15_"));
		rd.addIndicator("23", "HIV visit and Over 15", h.cohortIndicator("hivQD: HIV visit and over 15_"));
		rd.addIndicator("24", "HIV and Under 15", h.cohortIndicator("hivQD: HIV visit and under 15_"));
		
		h.replaceReportDefinition(rd);
		
		return rd;
	}
	
	private void createIndicators() {
		
		h.newCountIndicator("hivQD: In all programs over 15_", "hivQD: In all programs over 15", "endDate=${endDate}");
		h.newCountIndicator("hivQD: In all programs under 15_", "hivQD: In all programs under 15", "endDate=${endDate}");
		h.newCountIndicator("hivQD: new in any Hiv program in period over 15_", "hivQD: new in any Hiv program in period over 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: new in any Hiv program in period under 15_", "hivQD: new in any Hiv program in period under 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: preArt in Hiv program in period over 15_", "hivQD: preArt in Hiv program in period over 15", "endDate=${endDate}");
		h.newCountIndicator("hivQD: preArt in Hiv program in period under 15_", "hivQD: preArt in Hiv program in period under 15", "endDate=${endDate}");
		h.newCountIndicator("hivQD: preArt in Hiv program with a visit in period -3 months and over 15_", "hivQD: preArt in Hiv program with a visit in period -3 months and over 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: preArt in Hiv program with a visit in period -3 months and under 15_", "hivQD: preArt in Hiv program with a visit in period - 3 months and under 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: ever taking ART before end date and over 15_", "hivQD: ever taking ART before end date and over 15", "endDate=${endDate}");
		h.newCountIndicator("hivQD: ever taking ART before end date and under 15_", "hivQD: ever taking ART before end date and under 15", "endDate=${endDate}");
		h.newCountIndicator("hivQD: currently taking ART before end date and over 15_", "hivQD: currently taking ART before end date and over 15", "endDate=${endDate}");
		h.newCountIndicator("hivQD: currently taking ART before end date and under 15_", "hivQD: currently taking ART before end date and under 15", "endDate=${endDate}");
		h.newCountIndicator("hivQD: on ART in Hiv program with a visit in period -3 months and over 15_", "hivQD: on ART in Hiv program with a visit in period -3 months and over 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: on ART in Hiv program with a visit in period -3 months and under 15_", "hivQD: on ART in Hiv program with a visit in period -3 months and under 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: started taking ART in period over 15_", "hivQD: started taking ART in period over 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: started taking ART in period under 15_", "hivQD: started taking ART in period under 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: preArt in Hiv program with a CD4 count in period -3 months and over 15_", "hivQD: preArt in Hiv program with a CD4 count in period -3 months and over 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: preArt in Hiv program with a CD4 count in period -3 months and under 15_", "hivQD: preArt in Hiv program with a CD4 count in period -3 months and under 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: on Art in Hiv program with a CD4 count in period -3 months and over 15_", "hivQD: on Art in Hiv program with a CD4 count in period -3 months and over 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: on Art in Hiv program with a CD4 count in period -3 months and under 15_", "hivQD: on Art in Hiv program with a CD4 count in period - 3 months and under 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: weight recorded in period over 15_", "hivQD: weight recorded in period over 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: weight recorded in period under 15_", "hivQD: weight recorded in period under 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: HIV visit and over 15_", "hivQD: in Hiv program with a visit in period and over 15", "endDate=${endDate},startDate=${startDate}");
		h.newCountIndicator("hivQD: HIV visit and under 15_", "hivQD: in Hiv program with a visit in period and under 15", "endDate=${endDate},startDate=${startDate}");
	}
	
	private void createCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.voided = 0 and pa.value in (select location_id from location where retired = 0 and county_district = :location)");
		location.setName("hivQDLocation: Patients at location");
		location.addParameter(new Parameter("location", "location", LocationHierarchy.class));
		h.replaceCohortDefinition(location);
		
		InProgramCohortDefinition inAnyHIVProgram = new InProgramCohortDefinition();
		inAnyHIVProgram.setName("hivQD: In All HIV Programs");
		inAnyHIVProgram.addParameter(new Parameter("onOrBefore", "endDate", Date.class));
		List<Program> hivPrograms = new ArrayList<Program>();
		Program adult = Context.getProgramWorkflowService().getProgramByName(properties.get("HIV_PROGRAM"));
		if(adult != null)
		{
			hivPrograms.add(adult);
		}
		Program pedi = Context.getProgramWorkflowService().getProgramByName(properties.get("PEDIATRIC_HIV_PROGRAM"));
		if(pedi != null)
		{
			hivPrograms.add(pedi);
		}
		Program pmtct = Context.getProgramWorkflowService().getProgram(Integer.parseInt(properties.get("PMTCT_PROGRAM")));
		if(pmtct != null)
		{
			hivPrograms.add(pmtct);
		}
		inAnyHIVProgram.setPrograms(hivPrograms);
		h.replaceCohortDefinition(inAnyHIVProgram);
		
		InProgramCohortDefinition inAnyHIVAfterStartDate = new InProgramCohortDefinition();
		inAnyHIVAfterStartDate.setName("hivQD: new Patients enrolled in HIV Program during period");
		inAnyHIVAfterStartDate.addParameter(new Parameter("onOrBefore", "endDate", Date.class));
		inAnyHIVAfterStartDate.addParameter(new Parameter("onOrAfter", "startDate", Date.class));
		inAnyHIVAfterStartDate.setPrograms(hivPrograms);
		h.replaceCohortDefinition(inAnyHIVAfterStartDate);
		
		InProgramCohortDefinition inAdultOrPediHIVProgram = new InProgramCohortDefinition();
		inAdultOrPediHIVProgram.setName("hivQD: In AdultOrPedi HIV Programs");
		inAdultOrPediHIVProgram.addParameter(new Parameter("onOrBefore", "endDate", Date.class));
		hivPrograms = new ArrayList<Program>();
		if(adult != null)
		{
			hivPrograms.add(adult);
		}
		if(pedi != null)
		{
			hivPrograms.add(pedi);
		}
		inAdultOrPediHIVProgram.setPrograms(hivPrograms);
		h.replaceCohortDefinition(inAdultOrPediHIVProgram);

		InProgramCohortDefinition inAdultOrPediHIVOnDateProgram = new InProgramCohortDefinition();
		inAdultOrPediHIVOnDateProgram.setName("hivQD: In AdultOrPedi HIV Programs on Date");
		inAdultOrPediHIVOnDateProgram.addParameter(new Parameter("onDate", "endDate", Date.class));
		hivPrograms = new ArrayList<Program>();
		if(adult != null)
		{
			hivPrograms.add(adult);
		}
		if(pedi != null)
		{
			hivPrograms.add(pedi);
		}
		inAdultOrPediHIVOnDateProgram.setPrograms(hivPrograms);
		h.replaceCohortDefinition(inAdultOrPediHIVOnDateProgram);
		
		AgeCohortDefinition over15Cohort = new AgeCohortDefinition();
		over15Cohort.setName("ageQD: Over 15");
		over15Cohort.setMinAge(new Integer(15));
		over15Cohort.addParameter(new Parameter("effectiveDate", "endDate", Date.class));
		h.replaceCohortDefinition(over15Cohort);
		
		InStateCohortDefinition preArt = new InStateCohortDefinition();
		preArt.setName("hivQD: preArt");
		preArt.addParameter(new Parameter("onDate", "endDate", Date.class));
		List<ProgramWorkflowState> states = new ArrayList<ProgramWorkflowState>();
		ProgramWorkflowState adultPreArt = adult.getWorkflowByName(properties.get("HIV_WORKFLOW_STATUS")).getState(properties.get("PRE_ART_STATE"));
		if(adultPreArt != null)
		{
			states.add(adultPreArt);
		}
		ProgramWorkflowState pediPreArt = h.workflowState(properties.get("PEDIATRIC_HIV_PROGRAM"), properties.get("HIV_WORKFLOW_STATUS"), properties.get("PRE_ART_STATE"));
		if(pediPreArt != null)
		{
			states.add(pediPreArt);
		}
		preArt.setStates(states);
		h.replaceCohortDefinition(preArt);
		
		InStateCohortDefinition artOnOrBefore = new InStateCohortDefinition();
		artOnOrBefore.setName("hivQD: ever on ART");
		artOnOrBefore.addParameter(new Parameter("onOrBefore", "endDate", Date.class));
		states = new ArrayList<ProgramWorkflowState>();
		ProgramWorkflowState adultArt = adult.getWorkflowByName(properties.get("HIV_WORKFLOW_STATUS")).getState(properties.get("HIV_ON_ART_STATE"));
		if(adultArt != null)
		{
			states.add(adultArt);
		}
		ProgramWorkflowState pediArt = h.workflowState(properties.get("PEDIATRIC_HIV_PROGRAM"), properties.get("HIV_WORKFLOW_STATUS"), properties.get("HIV_ON_ART_STATE"));
		if(pediArt != null)
		{
			states.add(pediArt);
		}
		artOnOrBefore.setStates(states);
		h.replaceCohortDefinition(artOnOrBefore);
		
		InStateCohortDefinition artCurrently = new InStateCohortDefinition();
		artCurrently.setName("hivQD: currently on ART");
		artCurrently.addParameter(new Parameter("onDate", "endDate", Date.class));
		artCurrently.setStates(states);
		h.replaceCohortDefinition(artCurrently);
		
		InStateCohortDefinition artStartedInPeriod = new InStateCohortDefinition();
		artStartedInPeriod.setName("hivQD: started on ART");
		artStartedInPeriod.addParameter(new Parameter("onOrBefore", "endDate", Date.class));
		artStartedInPeriod.addParameter(new Parameter("onOrAfter", "startDate", Date.class));
		artStartedInPeriod.setStates(states);
		h.replaceCohortDefinition(artStartedInPeriod);
		
		EncounterCohortDefinition encounter = new EncounterCohortDefinition();
		encounter.setName("encounterQD: visit in period");
		encounter.addParameter(new Parameter("onOrBefore", "endDate", Date.class));
		encounter.addParameter(new Parameter("onOrAfter", "startDate", Date.class));
		List<EncounterType> encounters = new ArrayList<EncounterType>();
		String allEncounters = properties.get("HIV_ENCOUNTER_TYPES");
		String[] encounterTypes = allEncounters.split(":");
		for(String e: encounterTypes)
		{
			EncounterType type = Context.getEncounterService().getEncounterType(e);
			if(type != null)
			{
				encounters.add(type);
			}
		}
		encounter.setEncounterTypeList(encounters);
		h.replaceCohortDefinition(encounter);
		
		NumericObsCohortDefinition cd4 = new NumericObsCohortDefinition();
		cd4.setName("obsQD: CD4 count recorded");
		cd4.addParameter(new Parameter("onOrBefore", "endDate", Date.class));
		cd4.addParameter(new Parameter("onOrAfter", "startDate", Date.class));
		cd4.setQuestion(Context.getConceptService().getConcept(new Integer(properties.get("CD4_CONCEPT"))));
		cd4.setTimeModifier(TimeModifier.ANY);
		h.replaceCohortDefinition(cd4);
		
		NumericObsCohortDefinition weight = new NumericObsCohortDefinition();
		weight.setName("obsQD: weight recorded");
		weight.addParameter(new Parameter("onOrBefore", "endDate", Date.class));
		weight.addParameter(new Parameter("onOrAfter", "startDate", Date.class));
		weight.setTimeModifier(TimeModifier.ANY);
		weight.setQuestion(Context.getConceptService().getConcept(new Integer(properties.get("WEIGHT_CONCEPT"))));
		h.replaceCohortDefinition(weight);	
	}
	
	private void createCompositionCohortDefinitions()
	{
		CompositionCohortDefinition inHIVUnder15 = new CompositionCohortDefinition();
		inHIVUnder15.setName("hivQD: In all programs under 15");
		inHIVUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		inHIVUnder15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		inHIVUnder15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: In All HIV Programs"), h.parameterMap("onOrBefore", "${endDate}")));
		inHIVUnder15.setCompositionString("NOT 1 AND 2");
		h.replaceCohortDefinition(inHIVUnder15);
		
		CompositionCohortDefinition inHIVOver15 = new CompositionCohortDefinition();
		inHIVOver15.setName("hivQD: In all programs over 15");
		inHIVOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		inHIVOver15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		inHIVOver15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: In All HIV Programs"), h.parameterMap("onOrBefore", "${endDate}")));
		inHIVOver15.setCompositionString("1 AND 2");
		h.replaceCohortDefinition(inHIVOver15);

		CompositionCohortDefinition newInHIVUnder15 = new CompositionCohortDefinition();
		newInHIVUnder15.setName("hivQD: new in any Hiv program in period under 15");
		newInHIVUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		newInHIVUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		newInHIVUnder15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		newInHIVUnder15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: new Patients enrolled in HIV Program during period"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		newInHIVUnder15.getSearches().put(
			"3",
			new Mapped(h.cohortDefinition("hivQD: In All HIV Programs"), h.parameterMap("onOrBefore", "${startDate-1d}")));
		newInHIVUnder15.setCompositionString("NOT 1 AND 2 AND NOT 3");
		h.replaceCohortDefinition(newInHIVUnder15);
		
		CompositionCohortDefinition newInHIVOver15 = new CompositionCohortDefinition();
		newInHIVOver15.setName("hivQD: new in any Hiv program in period over 15");
		newInHIVOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		newInHIVOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		newInHIVOver15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		newInHIVOver15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: new Patients enrolled in HIV Program during period"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		newInHIVOver15.getSearches().put(
			"3",
			new Mapped(h.cohortDefinition("hivQD: In All HIV Programs"), h.parameterMap("onOrBefore", "${startDate-1d}")));
		newInHIVOver15.setCompositionString("1 AND 2 AND NOT 3");
		h.replaceCohortDefinition(newInHIVOver15);

		CompositionCohortDefinition followingUnder15 = new CompositionCohortDefinition();
		followingUnder15.setName("hivQD: preArt in Hiv program in period under 15");
		followingUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		followingUnder15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		followingUnder15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: preArt"), h.parameterMap("onDate", "${endDate}")));
		followingUnder15.getSearches().put(
			"3",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs on Date"), h.parameterMap("onDate", "${endDate}")));
		followingUnder15.setCompositionString("NOT 1 AND 2 AND 3");
		h.replaceCohortDefinition(followingUnder15);
		
		CompositionCohortDefinition followingOver15 = new CompositionCohortDefinition();
		followingOver15.setName("hivQD: preArt in Hiv program in period over 15");
		followingOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		followingOver15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		followingOver15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: preArt"), h.parameterMap("onDate", "${endDate}")));
		followingOver15.getSearches().put(
			"3",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs on Date"), h.parameterMap("onDate", "${endDate}")));
		followingOver15.setCompositionString("1 AND 2 AND 3");
		h.replaceCohortDefinition(followingOver15);
		
		CompositionCohortDefinition preArtWithAVisitUnder15 = new CompositionCohortDefinition();
		preArtWithAVisitUnder15.setName("hivQD: preArt in Hiv program with a visit in period - 3 months and under 15");
		preArtWithAVisitUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		preArtWithAVisitUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		preArtWithAVisitUnder15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		preArtWithAVisitUnder15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: preArt"), h.parameterMap("onDate", "${endDate}")));
		preArtWithAVisitUnder15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("encounterQD: visit in period"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate-3m}")));
		preArtWithAVisitUnder15.getSearches().put(
			"4",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs on Date"), h.parameterMap("onDate", "${endDate}")));
		preArtWithAVisitUnder15.getSearches().put(
		    "5",
		    new Mapped(h.cohortDefinition("obsQD: weight recorded"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate-3m}")));
		preArtWithAVisitUnder15.setCompositionString("NOT 1 AND 2 AND 4 AND(3 OR 5)");
		h.replaceCohortDefinition(preArtWithAVisitUnder15);
		
		CompositionCohortDefinition preArtWithAVisitOver15 = new CompositionCohortDefinition();
		preArtWithAVisitOver15.setName("hivQD: preArt in Hiv program with a visit in period -3 months and over 15");
		preArtWithAVisitOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		preArtWithAVisitOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		preArtWithAVisitOver15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		preArtWithAVisitOver15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: preArt"), h.parameterMap("onDate", "${endDate}")));
		preArtWithAVisitOver15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("encounterQD: visit in period"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate-3m}")));
		preArtWithAVisitOver15.getSearches().put(
			"4",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs on Date"), h.parameterMap("onDate", "${endDate}")));
		preArtWithAVisitOver15.getSearches().put(
		    "5",
		    new Mapped(h.cohortDefinition("obsQD: weight recorded"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate-3m}")));
		preArtWithAVisitOver15.setCompositionString("1 AND 2 AND 4 AND(3 OR 5)");
		h.replaceCohortDefinition(preArtWithAVisitOver15);
		
		CompositionCohortDefinition artUnder15 = new CompositionCohortDefinition();
		artUnder15.setName("hivQD: ever taking ART before end date and under 15");
		artUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artUnder15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		artUnder15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: ever on ART"), h.parameterMap("onOrBefore", "${endDate}")));
		artUnder15.getSearches().put(
			"3",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs"), h.parameterMap("onOrBefore", "${endDate}")));
		artUnder15.setCompositionString("NOT 1 AND 2 AND 3");
		h.replaceCohortDefinition(artUnder15);
		
		CompositionCohortDefinition artOver15 = new CompositionCohortDefinition();
		artOver15.setName("hivQD: ever taking ART before end date and over 15");
		artOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artOver15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		artOver15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: ever on ART"), h.parameterMap("onOrBefore", "${endDate}")));
		artOver15.getSearches().put(
			"3",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs"), h.parameterMap("onOrBefore", "${endDate}")));
		artOver15.setCompositionString("1 AND 2 AND 3");
		h.replaceCohortDefinition(artOver15);
		
		CompositionCohortDefinition currentArtUnder15 = new CompositionCohortDefinition();
		currentArtUnder15.setName("hivQD: currently taking ART before end date and under 15");
		currentArtUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		currentArtUnder15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		currentArtUnder15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: currently on ART"), h.parameterMap("onDate", "${endDate}")));
		currentArtUnder15.getSearches().put(
			"3",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs On Date"), h.parameterMap("onDate", "${endDate}")));
		currentArtUnder15.setCompositionString("NOT 1 AND 2 AND 3");
		h.replaceCohortDefinition(currentArtUnder15);
		
		CompositionCohortDefinition currentArtOver15 = new CompositionCohortDefinition();
		currentArtOver15.setName("hivQD: currently taking ART before end date and over 15");
		currentArtOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		currentArtOver15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		currentArtOver15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: currently on ART"), h.parameterMap("onDate", "${endDate}")));
		currentArtOver15.getSearches().put(
			"3",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs On Date"), h.parameterMap("onDate", "${endDate}")));
		currentArtOver15.setCompositionString("1 AND 2 AND 3");
		h.replaceCohortDefinition(currentArtOver15);
		
		CompositionCohortDefinition artWithAVisitUnder15 = new CompositionCohortDefinition();
		artWithAVisitUnder15.setName("hivQD: on ART in Hiv program with a visit in period -3 months and under 15");
		artWithAVisitUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artWithAVisitUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		artWithAVisitUnder15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		artWithAVisitUnder15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: currently on ART"), h.parameterMap("onDate", "${endDate}")));
		artWithAVisitUnder15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("encounterQD: visit in period"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		artWithAVisitUnder15.getSearches().put(
			"4",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs On Date"), h.parameterMap("onDate", "${endDate}")));
		artWithAVisitUnder15.getSearches().put(
		    "5",
		    new Mapped(h.cohortDefinition("obsQD: weight recorded"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		artWithAVisitUnder15.setCompositionString("NOT 1 AND 2 AND 4 AND(3 OR 5)");
		h.replaceCohortDefinition(artWithAVisitUnder15);
		
		CompositionCohortDefinition artWithAVisitOver15 = new CompositionCohortDefinition();
		artWithAVisitOver15.setName("hivQD: on ART in Hiv program with a visit in period -3 months and over 15");
		artWithAVisitOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artWithAVisitOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		artWithAVisitOver15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		artWithAVisitOver15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: currently on ART"), h.parameterMap("onDate", "${endDate}")));
		artWithAVisitOver15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("encounterQD: visit in period"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		artWithAVisitOver15.getSearches().put(
		    "5",
		    new Mapped(h.cohortDefinition("obsQD: weight recorded"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		artWithAVisitOver15.getSearches().put(
			"4",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs On Date"), h.parameterMap("onDate", "${endDate}")));
		artWithAVisitOver15.setCompositionString("1 AND 2 AND 4 AND(3 OR 5)");
		h.replaceCohortDefinition(artWithAVisitOver15);
		
		CompositionCohortDefinition startedArtOver15 = new CompositionCohortDefinition();
		startedArtOver15.setName("hivQD: started taking ART in period over 15");
		startedArtOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		startedArtOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		startedArtOver15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		startedArtOver15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: started on ART"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		startedArtOver15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("hivQD: currently on ART"), h.parameterMap("onDate", "${startDate-1d}")));
		startedArtOver15.getSearches().put(
			"4",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs"), h.parameterMap("onOrBefore", "${endDate}")));
		startedArtOver15.setCompositionString("1 AND 2 AND NOT 3 AND 4");
		h.replaceCohortDefinition(startedArtOver15);
		
		CompositionCohortDefinition startedArtUnder15 = new CompositionCohortDefinition();
		startedArtUnder15.setName("hivQD: started taking ART in period under 15");
		startedArtUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		startedArtUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		startedArtUnder15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		startedArtUnder15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: started on ART"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		startedArtUnder15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("hivQD: currently on ART"), h.parameterMap("onDate", "${startDate-1d}")));
		startedArtUnder15.getSearches().put(
			"4",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs"), h.parameterMap("onOrBefore", "${endDate}")));
		startedArtUnder15.setCompositionString("NOT 1 AND 2 AND NOT 3 AND 4");
		h.replaceCohortDefinition(startedArtUnder15);
		
		CompositionCohortDefinition preArtWithACD4Under15 = new CompositionCohortDefinition();
		preArtWithACD4Under15.setName("hivQD: preArt in Hiv program with a CD4 count in period -3 months and under 15");
		preArtWithACD4Under15.addParameter(new Parameter("endDate", "endDate", Date.class));
		preArtWithACD4Under15.addParameter(new Parameter("startDate", "startDate", Date.class));
		preArtWithACD4Under15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		preArtWithACD4Under15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: preArt"), h.parameterMap("onDate", "${endDate}")));
		preArtWithACD4Under15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("obsQD: CD4 count recorded"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate-3m}")));
		preArtWithACD4Under15.getSearches().put(
			"4",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs On Date"), h.parameterMap("onDate", "${endDate}")));
		preArtWithACD4Under15.setCompositionString("NOT 1 AND 2 AND 3 AND 4");
		h.replaceCohortDefinition(preArtWithACD4Under15);
		
		CompositionCohortDefinition preArtWithACD4Over15 = new CompositionCohortDefinition();
		preArtWithACD4Over15.setName("hivQD: preArt in Hiv program with a CD4 count in period -3 months and over 15");
		preArtWithACD4Over15.addParameter(new Parameter("endDate", "endDate", Date.class));
		preArtWithACD4Over15.addParameter(new Parameter("startDate", "startDate", Date.class));
		preArtWithACD4Over15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		preArtWithACD4Over15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: preArt"), h.parameterMap("onDate", "${endDate}")));
		preArtWithACD4Over15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("obsQD: CD4 count recorded"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate-3m}")));
		preArtWithACD4Over15.getSearches().put(
			"4",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs On Date"), h.parameterMap("onDate", "${endDate}")));
		preArtWithACD4Over15.setCompositionString("1 AND 2 AND 3 AND 4");
		h.replaceCohortDefinition(preArtWithACD4Over15);
		
		CompositionCohortDefinition artWithACD4Over15 = new CompositionCohortDefinition();
		artWithACD4Over15.setName("hivQD: on Art in Hiv program with a CD4 count in period -3 months and over 15");
		artWithACD4Over15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artWithACD4Over15.addParameter(new Parameter("startDate", "startDate", Date.class));
		artWithACD4Over15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		artWithACD4Over15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: currently on ART"), h.parameterMap("onDate", "${endDate}")));
		artWithACD4Over15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("obsQD: CD4 count recorded"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate-3m}")));
		artWithACD4Over15.getSearches().put(
			"4",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs On Date"), h.parameterMap("onDate", "${endDate}")));
		artWithACD4Over15.setCompositionString("1 AND 2 AND 3 AND 4");
		h.replaceCohortDefinition(artWithACD4Over15);
		
		CompositionCohortDefinition artWithACD4Under15 = new CompositionCohortDefinition();
		artWithACD4Under15.setName("hivQD: on Art in Hiv program with a CD4 count in period - 3 months and under 15");
		artWithACD4Under15.addParameter(new Parameter("endDate", "endDate", Date.class));
		artWithACD4Under15.addParameter(new Parameter("startDate", "startDate", Date.class));
		artWithACD4Under15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		artWithACD4Under15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: currently on ART"), h.parameterMap("onDate", "${endDate}")));
		artWithACD4Under15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("obsQD: CD4 count recorded"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate-3m}")));
		artWithACD4Under15.getSearches().put(
			"4",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs On Date"), h.parameterMap("onDate", "${endDate}")));
		artWithACD4Under15.setCompositionString("NOT 1 AND 2 AND 3 AND 4");
		h.replaceCohortDefinition(artWithACD4Under15);
		
		CompositionCohortDefinition hivWithAWeightUnder15 = new CompositionCohortDefinition();
		hivWithAWeightUnder15.setName("hivQD: weight recorded in period under 15");
		hivWithAWeightUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivWithAWeightUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		hivWithAWeightUnder15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		hivWithAWeightUnder15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs"), h.parameterMap("onOrBefore", "${endDate}")));
		hivWithAWeightUnder15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("obsQD: weight recorded"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		hivWithAWeightUnder15.setCompositionString("NOT 1 AND 2 AND 3");
		h.replaceCohortDefinition(hivWithAWeightUnder15);
		
		CompositionCohortDefinition hivWithAWeightOver15 = new CompositionCohortDefinition();
		hivWithAWeightOver15.setName("hivQD: weight recorded in period over 15");
		hivWithAWeightOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivWithAWeightOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		hivWithAWeightOver15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		hivWithAWeightOver15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs"), h.parameterMap("onOrBefore", "${endDate}")));
		hivWithAWeightOver15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("obsQD: weight recorded"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		hivWithAWeightOver15.setCompositionString("1 AND 2 AND 3");
		h.replaceCohortDefinition(hivWithAWeightOver15);
		
		CompositionCohortDefinition hivOver15 = new CompositionCohortDefinition();
		hivOver15.setName("hivQD: HIV Adult or Pedi and over 15");
		hivOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivOver15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		hivOver15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs"), h.parameterMap("onOrBefore", "${endDate}")));
		hivOver15.setCompositionString("1 AND 2");
		h.replaceCohortDefinition(hivOver15);
		
		CompositionCohortDefinition hivUnder15 = new CompositionCohortDefinition();
		hivUnder15.setName("hivQD: HIV Adult or Pedi and under 15");
		hivUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivUnder15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		hivUnder15.getSearches().put(
		    "2",
		    new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs"), h.parameterMap("onOrBefore", "${endDate}")));
		hivUnder15.setCompositionString("NOT 1 AND 2");
		h.replaceCohortDefinition(hivUnder15);
		
		CompositionCohortDefinition hivVisitOver15 = new CompositionCohortDefinition();
		hivVisitOver15.setName("hivQD: in Hiv program with a visit in period and over 15");
		hivVisitOver15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivVisitOver15.addParameter(new Parameter("startDate", "startDate", Date.class));
		hivVisitOver15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		hivVisitOver15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("encounterQD: visit in period"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		hivVisitOver15.getSearches().put(
			"2",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs"), h.parameterMap("onOrBefore", "${endDate}")));
		hivVisitOver15.getSearches().put(
		    "4",
		    new Mapped(h.cohortDefinition("obsQD: weight recorded"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		hivVisitOver15.setCompositionString("1 AND 2 AND (3 OR 4)");
		h.replaceCohortDefinition(hivVisitOver15);
		
		CompositionCohortDefinition hivVisitUnder15 = new CompositionCohortDefinition();
		hivVisitUnder15.setName("hivQD: in Hiv program with a visit in period and under 15");
		hivVisitUnder15.addParameter(new Parameter("endDate", "endDate", Date.class));
		hivVisitUnder15.addParameter(new Parameter("startDate", "startDate", Date.class));
		hivVisitUnder15.getSearches().put(
		    "1",new Mapped(h.cohortDefinition("ageQD: Over 15"), h.parameterMap("effectiveDate", "${endDate}")));
		hivVisitUnder15.getSearches().put(
		    "3",
		    new Mapped(h.cohortDefinition("encounterQD: visit in period"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		hivVisitUnder15.getSearches().put(
			"2",
			new Mapped(h.cohortDefinition("hivQD: In AdultOrPedi HIV Programs"), h.parameterMap("onOrBefore", "${endDate}")));
		hivVisitUnder15.getSearches().put(
		    "4",
		    new Mapped(h.cohortDefinition("obsQD: weight recorded"), h.parameterMap("onOrBefore", "${endDate}", "onOrAfter", "${startDate}")));
		hivVisitUnder15.setCompositionString("NOT 1 AND 2 AND (3 OR 4)");
		h.replaceCohortDefinition(hivVisitUnder15);
	}
	
	private void setUpGlobalProperties()
	{
		properties = new HashMap<String, String>();
		
		String hivProgram = Context.getAdministrationService().getGlobalProperty("reports.hivprogramname");
		properties.put("HIV_PROGRAM", hivProgram);
		
		String workflowStatus = Context.getAdministrationService().getGlobalProperty("reports.hivworkflowstatus");
		properties.put("HIV_WORKFLOW_STATUS", workflowStatus);
		
		String onARTState = Context.getAdministrationService().getGlobalProperty("reports.hivonartstate");
		properties.put("HIV_ON_ART_STATE", onARTState);
		
		String weightConcept = Context.getAdministrationService().getGlobalProperty("reports.weightConcept");
		properties.put("WEIGHT_CONCEPT", weightConcept);
		
		String cd4Concept = Context.getAdministrationService().getGlobalProperty("reports.cd4Concept");
		properties.put("CD4_CONCEPT", cd4Concept);
		
		String pediProgram = Context.getAdministrationService().getGlobalProperty("reports.pedihivprogramname");
		properties.put("PEDIATRIC_HIV_PROGRAM", pediProgram);
		
		String pmtctProgram = Context.getAdministrationService().getGlobalProperty("reports.pmtctprogramname");
		properties.put("PMTCT_PROGRAM", pmtctProgram);
		
		String preARTState = Context.getAdministrationService().getGlobalProperty("reports.hivpreartstate");
		properties.put("PRE_ART_STATE", preARTState);
		
		String hivEncounterTypes = Context.getAdministrationService().getGlobalProperty("reports.hivencountertypes");
		properties.put("HIV_ENCOUNTER_TYPES", hivEncounterTypes);
	}
	
}
