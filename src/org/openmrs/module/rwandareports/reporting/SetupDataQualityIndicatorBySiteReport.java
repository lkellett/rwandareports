package org.openmrs.module.rwandareports.reporting;

import java.util.Date;

import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.Indicators;

public class SetupDataQualityIndicatorBySiteReport {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		createReportDefinition();
	}
	
	public void delete() {
		
		h.purgeReportDefinition("DataQualityIndicatorReport");
	}
	
	private ReportDefinition createReportDefinition() {
		
		PeriodIndicatorReportDefinition rd = new PeriodIndicatorReportDefinition();
		rd.removeParameter(ReportingConstants.START_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.END_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.LOCATION_PARAMETER);
		rd.addParameter(new Parameter("location", "Location", Location.class));
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		rd.setName("DataQualityIndicatorReport");
		
		rd.setupDataSetDefinition();
		
		rd.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort(),
		    ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createIndicators(rd);
		
		h.saveReportDefinition(rd);
		
		return rd;
	}
	
	private void createIndicators(PeriodIndicatorReportDefinition reportDefinition) {
		
		SqlCohortDefinition multEncounters = new SqlCohortDefinition();
		String multEncountersSql = "select patient_id from encounter where voided = 0  and encounter_datetime > :startDate and encounter_datetime < :endDate and encounter_type in (";
		multEncountersSql = multEncountersSql + Context.getAdministrationService().getGlobalProperty(GlobalPropertiesManagement.CLINICAL_ENCOUNTER_TYPES_EXC_LAB_TEST);
		multEncountersSql = multEncountersSql
		        + ") group by patient_id, encounter_datetime, encounter_type having count(patient_id) > 1";
		
		multEncounters.setQuery(multEncountersSql);
		multEncounters.addParameter(new Parameter("endDate", "endDate", Date.class));
		multEncounters.addParameter(new Parameter("startDate", "startDate", Date.class));
		multEncounters.setName("DQ: Multiple encounters on the one date");
		
		CohortIndicator i = Indicators.newCountIndicator("DQ: Patients with multiple encounters on the same day", multEncounters,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		reportDefinition.addIndicator("1", "Patients with multiple encounters on the same day", i);
	}
	
	private void setUpProperties() {
		
	}
	
}
