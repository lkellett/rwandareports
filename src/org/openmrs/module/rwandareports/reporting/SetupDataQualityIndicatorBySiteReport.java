package org.openmrs.module.rwandareports.reporting;

import java.util.Date;
import java.util.HashMap;

import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;

public class SetupDataQualityIndicatorBySiteReport {
	
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupDataQualityIndicatorBySiteReport(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		setUpGlobalProperties();
		
		createCohortDefinitions();
		createCompositionCohortDefinitions();
		createIndicators();
		createReportDefinition();
	}
	
	public void delete() {
		
		h.purgeDefinition(PeriodIndicatorReportDefinition.class, "DataQualityIndicatorReport");
		
		h.purgeDefinition(DataSetDefinition.class, "DataQualityIndicatorReport Data Set");
		
		h.purgeDefinition(CohortIndicator.class, "DQ: Patients with multiple encounters on the same day");
		
		h.purgeDefinition(CohortDefinition.class, "DQLocation: Patients at location");
		h.purgeDefinition(CohortDefinition.class, "DQ: Multiple encounters on the one date");
	}
	
	
	private ReportDefinition createReportDefinition() {
		// PIH Quarterly Cross Site Indicator Report
		
		PeriodIndicatorReportDefinition rd = new PeriodIndicatorReportDefinition();
		rd.removeParameter(ReportingConstants.START_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.END_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.LOCATION_PARAMETER);
		rd.addParameter(new Parameter("location", "Location", Location.class));
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		rd.setName("DataQualityIndicatorReport");
		
		rd.setupDataSetDefinition();
		
		rd.setBaseCohortDefinition(h.cohortDefinition("DQLocation: Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
		rd.addIndicator("1", "Patients with multiple encounters on the same day", h.cohortIndicator("DQ: Patients with multiple encounters on the same day"));
		
		h.replaceReportDefinition(rd);
		
		return rd;
	}
	
	private void createIndicators() {
		
		h.newCountIndicator("DQ: Patients with multiple encounters on the same day", "DQ: Multiple encounters on the one date", new HashMap<String,Object>());
	}
	
	private void createCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.voided = 0 and pa.value = :location");
		location.setName("DQLocation: Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
		
		SqlCohortDefinition multEncounters = new SqlCohortDefinition();
		String multEncountersSql = "select patient_id from encounter where voided = 0  and encounter_datetime > :startDate and encounter_datetime < :endDate and encounter_type in (";
		multEncountersSql = multEncountersSql + properties.get("MULT_ENCOUNTERS");
		multEncountersSql = multEncountersSql + ") group by patient_id, encounter_datetime, encounter_type having count(patient_id) > 1";
		
		multEncounters
		        .setQuery(multEncountersSql);
		multEncounters.addParameter(new Parameter("endDate", "endDate", Date.class));
		multEncounters.addParameter(new Parameter("startDate", "startDate", Date.class));
		multEncounters.setName("DQ: Multiple encounters on the one date");
		h.replaceCohortDefinition(multEncounters);
	}
	
	private void createCompositionCohortDefinitions()
	{
		
	}
	
	private void setUpGlobalProperties()
	{
		properties = new HashMap<String, String>();
		
		String multEncounters = Context.getAdministrationService().getGlobalProperty("reports.dataqualitymultipleencounters");
		properties.put("MULT_ENCOUNTERS", multEncounters);
		
	}
	
}
