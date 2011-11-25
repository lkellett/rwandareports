package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reportingobjectgroup.objectgroup.definition.SqlObjectGroupDefinition;
import org.openmrs.module.reportingobjectgroup.objectgroup.indicator.ObjectGroupIndicator;
import org.openmrs.module.reportingobjectgroup.report.definition.RollingDailyPeriodIndicatorReportDefinition;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.Indicators;

public class SetupRwandaPrimaryCareReport {
	
	protected final static Log log = LogFactory.getLog(SetupRwandaPrimaryCareReport.class);
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	// properties
	private EncounterType registration;
	
	private EncounterType vitals;
	
	private List<String> onOrBeforeonOrAfterParameterNames = new ArrayList<String>();
	
	private Concept rwandaInsuranceType;
	
	private Concept mutuelle;
	
	private Concept rama;
	
	private Concept mmi;
	
	private Concept mediplan;
	
	private Concept corar;
	
	private Concept none;
	
	private Concept primaryCareServiceRequested;
	
	private Concept vctProgram;
	
	private Concept antenatalClinic;
	
	private Concept familyPlanningServices;
	
	private Concept mutuelleServices;
	
	private Concept accountingOfficeServices;
	
	private Concept integratedManagementOfAdultIllnessServices;
	
	private Concept integratedManagementOfChildhoodIllnessServices;
	
	private Concept infectiousDiseasesClinicService;
	
	private Concept socialWorkerService;
	
	private Concept pmtctService;
	
	private Concept laboratoryService;
	
	private Concept pharmacyService;
	
	private Concept maternityService;
	
	private Concept hospitalizationService;
	
	private Concept vaccinationService;
	
	private Concept temperature;
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = createReportDefinition(registration, vitals);
		h.createXlsCalendarOverview(rd, "rwandacalendarprimarycarereporttemplate.xls", "Primary_Care_Report_Template", null);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Primary_Care_Report_Template".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Rwanda Primary Care Report");
	}
	
	private ReportDefinition createReportDefinition(EncounterType reg, EncounterType vitals) {
		
		// PIH Quarterly Cross Site Indicator Report
		RollingDailyPeriodIndicatorReportDefinition rd = new RollingDailyPeriodIndicatorReportDefinition();
		rd.removeParameter(ReportingConstants.START_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.END_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.LOCATION_PARAMETER);
		rd.addParameter(new Parameter("location", "Location", Location.class));
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		rd.setRollingBaseReportQueryType(RollingDailyPeriodIndicatorReportDefinition.RollingBaseReportQueryType.ENCOUNTER);
		rd.setName("Rwanda Primary Care Report");
		
		// Creation of Vitals and Registration Encounter types during report
		// period
		EncounterCohortDefinition patientsWithPrimaryCareRegistration = Cohorts.createEncounterParameterizedByDate(
		    "patientsWithPrimaryCareRegistration", onOrBeforeonOrAfterParameterNames, registration);
		
		rd.setupDataSetDefinition();
		// ======================================================================================
		// 1st Question
		// ======================================================================================
		
		// ======================================================================================
		// 2nd Question
		// ======================================================================================
		
		// 2.1 Percent of patients who DO have an observation for
		// temperature in the vitals (changed from no not have a change, hence
		// the slightly misnamed vars
		
		SqlObjectGroupDefinition patientsUnder5WithoutTemperatureInVitals = new SqlObjectGroupDefinition();
		patientsUnder5WithoutTemperatureInVitals.setName("patientsUnder5WithoutTemperatureInVitals");
		patientsUnder5WithoutTemperatureInVitals
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e left join ( select distinct  e.encounter_id, e.patient_id, e.encounter_datetime from encounter e, person p, patient pa, obs o where e.encounter_id = o.encounter_id and o.voided = 0 and o.concept_id =  "
		                + temperature.getId()
		                + " and e.encounter_type =  "
		                + vitals.getEncounterTypeId()
		                + " and e.voided = 0 and e.patient_id = p.person_id and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5	and p.voided = 0 and p.person_id = pa.patient_id and pa.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and e.location_id = :location) eTmp on eTmp.patient_id = e.patient_id, person p, patient pat where day(eTmp.encounter_datetime) = day(e.encounter_datetime) and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and p.voided = 0 and pat.voided = 0 and e.patient_id = p.person_id and p.person_id = pat.patient_id and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and e.location_id = :location");
		patientsUnder5WithoutTemperatureInVitals.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsUnder5WithoutTemperatureInVitals.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		SqlObjectGroupDefinition patientsUnder5InRegistration = new SqlObjectGroupDefinition();
		patientsUnder5InRegistration.setName("patientsUnder5InRegistration");
		patientsUnder5InRegistration
		        .setQuery("select e.encounter_id, e.patient_id from encounter e, person p, patient pa where e.voided = 0 and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.patient_id = p.person_id and p.voided = 0 and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5 and e.patient_id = pa.patient_id and pa.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and e.location_id = :location");
		patientsUnder5InRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsUnder5InRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator patientsWithoutTemperatureInVitalsIndicator = Indicators.newFractionIndicatorObjectGroupIndicator(
		    "patientsWithoutTemperatureInVitalsIndicator", patientsUnder5WithoutTemperatureInVitals,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"),

		    patientsUnder5InRegistration,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 2.2 Percent of children under 5 who did have observation for
		
		// temperature, and actually had a fever (were sick, temperature was
		// higher than normal)
		
		SqlObjectGroupDefinition patientsUnder5WithTemperatureGreaterThanNormalInVitals = new SqlObjectGroupDefinition();
		patientsUnder5WithTemperatureGreaterThanNormalInVitals
		        .setName("patientsUnder5WithTemperatureGreaterThanNormalInVitals");
		patientsUnder5WithTemperatureGreaterThanNormalInVitals
		        .setQuery("select e.encounter_id, e.patient_id from encounter e, person p, patient pa,obs o where e.voided = 0 and e.encounter_id = o.encounter_id and o.voided = 0 and o.concept_id = "
		                + temperature.getId()
		                + " and o.value_numeric > 37.0 and e.encounter_type = "
		                + vitals.getEncounterTypeId()
		                + " and e.patient_id = p.person_id and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5 and p.voided = 0 and p.person_id = pa.patient_id and pa.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and e.location_id = :location");
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.addParameter(new Parameter("startDate", "startDate",
		        Date.class));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		SqlObjectGroupDefinition patientsUnder5WithTemperatureInVitals = new SqlObjectGroupDefinition();
		patientsUnder5WithTemperatureInVitals.setName("patientsUnder5WithTemperatureInVitals");
		patientsUnder5WithTemperatureInVitals
		        .setQuery("select e.encounter_id, e.patient_id from encounter e, person p,patient pa,obs o where e.voided = 0 and e.encounter_id = o.encounter_id and o.voided = 0 and o.concept_id = "
		                + temperature.getId()
		                + " and e.encounter_type = "
		                + vitals.getEncounterTypeId()
		                + " and e.patient_id = p.person_id and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5 and p.voided = 0 and p.person_id = pa.patient_id and pa.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and e.location_id = :location");
		patientsUnder5WithTemperatureInVitals.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsUnder5WithTemperatureInVitals.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator patientsWithTemperatureGreaterThanNormalInVitalsIndicator = Indicators
		        .newFractionIndicatorObjectGroupIndicator("patientsWithTemperatureGreaterThanNormalInVitalsIndicator",
		            patientsUnder5WithTemperatureGreaterThanNormalInVitals,
		            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"),
		            patientsUnder5WithTemperatureInVitals,
		            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 2.3 Percent of all registered patients under 5 who had a fever
		
		ObjectGroupIndicator allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator = Indicators
		        .newFractionIndicatorObjectGroupIndicator(
		            "allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator",
		            patientsUnder5WithTemperatureGreaterThanNormalInVitals,
		            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"),
		            patientsUnder5InRegistration,
		            ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// ========================================================================
		// 3. Registration Speed during Peak Hours
		// ========================================================================
		
		// 8 to 10, monday to friday
		SqlObjectGroupDefinition peakHours = new SqlObjectGroupDefinition();
		peakHours.setName("peakHours");
		peakHours
		        .setQuery("select distinct encounter_id, patient_id from encounter where TIME(encounter_datetime) >= :startTime and TIME(encounter_datetime) <= :endTime and WEEKDAY(encounter_datetime) <=4  and encounter_datetime>= :startDate and encounter_datetime<= :endDate and encounter_type = "
		                + registration.getEncounterTypeId() + " and voided = 0 and location_id = :location");
		peakHours.addParameter(new Parameter("startDate", "startDate", Date.class));
		peakHours.addParameter(new Parameter("endDate", "endDate", Date.class));
		peakHours.addParameter(new Parameter("startTime", "startTime", Date.class));
		peakHours.addParameter(new Parameter("endTime", "endTime", Date.class));
		
		// number of weekdays between startDate and stopDate / 2
		
		ObjectGroupIndicator peakHoursAndPeakDaysIndicator = Indicators
		        .newDailyDivisionIndicatorPerWeekDays(
		            "peakHoursIndicator",
		            peakHours,
		            ParameterizableUtil
		                    .createParameterMappings("startDate=${startDate},endDate=${endDate},startTime=08:00:00,endTime=10:00:00"),
		            Integer.valueOf(2));
		
		// ========================================================================
		// 4. How many registration encounters are paid for by Medical Insurance
		// ========================================================================
		
		// Mutuelle Insurance cohort definition
		
		CodedObsCohortDefinition MUTUELLEInsCohortDef = Cohorts.createCodedObsCohortDefinition("MUTUELLEInsCohortDef",
		    onOrBeforeonOrAfterParameterNames, rwandaInsuranceType, mutuelle, SetComparator.IN, TimeModifier.ANY);
		
		CodedObsCohortDefinition RAMAInsCohortDef = Cohorts.createCodedObsCohortDefinition("RAMAInsCohortDef",
		    onOrBeforeonOrAfterParameterNames, rwandaInsuranceType, rama, SetComparator.IN, TimeModifier.ANY);
		
		CodedObsCohortDefinition MMIInsCohortDef = Cohorts.createCodedObsCohortDefinition("MMIInsCohortDef",
		    onOrBeforeonOrAfterParameterNames, rwandaInsuranceType, mmi, SetComparator.IN, TimeModifier.ANY);
		
		CodedObsCohortDefinition MEDIPLANInsCohortDef = Cohorts.createCodedObsCohortDefinition("MEDIPLANInsCohortDef",
		    onOrBeforeonOrAfterParameterNames, rwandaInsuranceType, mediplan, SetComparator.IN, TimeModifier.ANY);
		
		CodedObsCohortDefinition CORARInsCohortDef = Cohorts.createCodedObsCohortDefinition("CORARInsCohortDef",
		    onOrBeforeonOrAfterParameterNames, rwandaInsuranceType, corar, SetComparator.IN, TimeModifier.ANY);
		
		CodedObsCohortDefinition NONEInsCohortDef = Cohorts.createCodedObsCohortDefinition("NONEInsCohortDef",
		    onOrBeforeonOrAfterParameterNames, rwandaInsuranceType, none, SetComparator.IN, TimeModifier.ANY);
		
		// 4.1 Percent of patients who are missing an insurance type in
		// registration encounter
		
		CompositionCohortDefinition patientsMissingIns = new CompositionCohortDefinition();
		patientsMissingIns.setName("patientsMissingIns");
		patientsMissingIns.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsMissingIns.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsMissingIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsMissingIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsMissingIns.getSearches().put(
		    "patientsWithPrimaryCareRegistration",
		    new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns.getSearches().put(
		    "MUTUELLEInsCohortDef",
		    new Mapped<CohortDefinition>(MUTUELLEInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns.getSearches().put(
		    "RAMAInsCohortDef",
		    new Mapped<CohortDefinition>(RAMAInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns.getSearches().put(
		    "MMIInsCohortDef",
		    new Mapped<CohortDefinition>(MMIInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns.getSearches().put(
		    "MEDIPLANInsCohortDef",
		    new Mapped<CohortDefinition>(MEDIPLANInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns.getSearches().put(
		    "CORARInsCohortDef",
		    new Mapped<CohortDefinition>(CORARInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns.getSearches().put(
		    "NONEInsCohortDef",
		    new Mapped<CohortDefinition>(NONEInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns
		        .setCompositionString("patientsWithPrimaryCareRegistration AND (NOT(MUTUELLEInsCohortDef OR RAMAInsCohortDef OR MMIInsCohortDef OR MEDIPLANInsCohortDef OR CORARInsCohortDef OR NONEInsCohortDef))");
		
		CohortIndicator percentOfPatientsMissingInsIndicator = Indicators.newFractionIndicator(
		    "percentOfPatientsMissingInsIndicator", patientsMissingIns,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		    patientsWithPrimaryCareRegistration,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// 4.2 Number of patients who are missing an insurance type in
		// registration encounter
		CohortIndicator numberOfPatientsMissingInsIndicator = Indicators.newCohortIndicator("numberOfPatientsMissingInsIndicator",
		    patientsMissingIns, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// 4.3.1 Percent of patients with MUTUELLE insurance in registration
		// encounter
		
		CompositionCohortDefinition patientsWithMUTUELLEIns = new CompositionCohortDefinition();
		patientsWithMUTUELLEIns.setName("patientsWithMUTUELLEIns");
		patientsWithMUTUELLEIns.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMUTUELLEIns.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMUTUELLEIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEIns.getSearches().put(
		    "patientsWithPrimaryCareRegistration",
		    new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEIns.getSearches().put(
		    "MUTUELLEInsCohortDef",
		    new Mapped<CohortDefinition>(MUTUELLEInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEIns.setCompositionString("patientsWithPrimaryCareRegistration AND MUTUELLEInsCohortDef");
		
		CohortIndicator percentOfPatientsWithMUTUELLEInsIndicator = Indicators.newFractionIndicator(
		    "percentOfPatientsWithMUTUELLEInsIndicator", patientsWithMUTUELLEIns,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		    patientsWithPrimaryCareRegistration,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// 4.3.2 Percent of patients with RAMA insurance in registration
		// encounter
		
		CompositionCohortDefinition patientsWithRAMAIns = new CompositionCohortDefinition();
		patientsWithRAMAIns.setName("patientsWithRAMAIns");
		patientsWithRAMAIns.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithRAMAIns.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithRAMAIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAIns.getSearches().put(
		    "patientsWithPrimaryCareRegistration",
		    new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAIns.getSearches().put(
		    "RAMAInsCohortDef",
		    new Mapped<CohortDefinition>(RAMAInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAIns.setCompositionString("patientsWithPrimaryCareRegistration AND RAMAInsCohortDef");
		
		CohortIndicator percentOfPatientsWithRAMAInsIndicator = Indicators.newFractionIndicator(
		    "percentOfPatientsWithRAMAInsIndicator", patientsWithRAMAIns,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		    patientsWithPrimaryCareRegistration,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// 4.3.3 Percent of patients with MMI insurance in registration
		// encounter
		
		CompositionCohortDefinition patientsWithMMIIns = new CompositionCohortDefinition();
		patientsWithMMIIns.setName("patientsWithMMIIns");
		patientsWithMMIIns.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMMIIns.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMMIIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIIns.getSearches().put(
		    "patientsWithPrimaryCareRegistration",
		    new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIIns.getSearches().put(
		    "MMIInsCohortDef",
		    new Mapped<CohortDefinition>(MMIInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIIns.setCompositionString("patientsWithPrimaryCareRegistration AND MMIInsCohortDef");
		
		CohortIndicator percentOfPatientsWithMMIInsIndicator = Indicators.newFractionIndicator(
		    "percentOfPatientsWithMMIInsIndicator", patientsWithMMIIns,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		    patientsWithPrimaryCareRegistration,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// 4.3.4 Percent of patients with MEDIPLAN insurance in registration
		// encounter
		
		CompositionCohortDefinition patientsWithMEDIPLANIns = new CompositionCohortDefinition();
		patientsWithMEDIPLANIns.setName("patientsWithMEDIPLANIns");
		patientsWithMEDIPLANIns.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMEDIPLANIns.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMEDIPLANIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANIns.getSearches().put(
		    "patientsWithPrimaryCareRegistration",
		    new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANIns.getSearches().put(
		    "MEDIPLANInsCohortDef",
		    new Mapped<CohortDefinition>(MEDIPLANInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANIns.setCompositionString("patientsWithPrimaryCareRegistration AND MEDIPLANInsCohortDef");
		
		CohortIndicator percentOfPatientsWithMEDIPLANInsIndicator = Indicators.newFractionIndicator(
		    "percentOfPatientsWithMEDIPLANInsIndicator", patientsWithMEDIPLANIns,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		    patientsWithPrimaryCareRegistration,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// 4.3.5 Percent of patients with CORAR insurance in registration
		// encounter
		
		CompositionCohortDefinition patientsWithCORARIns = new CompositionCohortDefinition();
		patientsWithCORARIns.setName("patientsWithCORARIns");
		patientsWithCORARIns.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithCORARIns.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithCORARIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARIns.getSearches().put(
		    "patientsWithPrimaryCareRegistration",
		    new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARIns.getSearches().put(
		    "CORARInsCohortDef",
		    new Mapped<CohortDefinition>(CORARInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARIns.setCompositionString("patientsWithPrimaryCareRegistration AND CORARInsCohortDef");
		
		CohortIndicator percentOfPatientsWithCORARInsIndicator = Indicators.newFractionIndicator(
		    "percentOfPatientsWithCORARInsIndicator", patientsWithCORARIns,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		    patientsWithPrimaryCareRegistration,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// 4.3.6 Percent of patients with CORAR insurance in registration
		// encounter
		
		CompositionCohortDefinition patientsWithNONEIns = new CompositionCohortDefinition();
		patientsWithNONEIns.setName("patientsWithNONEIns");
		patientsWithNONEIns.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithNONEIns.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithNONEIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEIns.getSearches().put(
		    "patientsWithPrimaryCareRegistration",
		    new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEIns.getSearches().put(
		    "NONEInsCohortDef",
		    new Mapped<CohortDefinition>(NONEInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEIns.setCompositionString("patientsWithPrimaryCareRegistration AND NONEInsCohortDef");
		
		CohortIndicator percentOfPatientsWithNONEInsIndicator = Indicators.newFractionIndicator(
		    "percentOfPatientsWithNONEInsIndicator", patientsWithNONEIns,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"),
		    patientsWithPrimaryCareRegistration,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		// ========================================================================
		// 5. For all insurance types, how many patients come back for multiple
		// visits, and how many visits:
		// ========================================================================
		
		SqlCohortDefinition patientsWithOneVisit = new SqlCohortDefinition();
		patientsWithOneVisit.setName("patientsWithOneVisit");
		patientsWithOneVisit
		        .setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type="
		                + registration.getEncounterTypeId()
		                + " and e.voided=0 and e.location_id = :location group by e.patient_id) as patientregistrationtimes where timesofregistration=1 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		patientsWithOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		SqlCohortDefinition patientsWithTwoVisits = new SqlCohortDefinition();
		patientsWithTwoVisits.setName("patientsWithTwoVisits");
		patientsWithTwoVisits
		        .setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type="
		                + registration.getEncounterTypeId()
		                + " and e.voided=0 and e.location_id = :location group by e.patient_id) as patientregistrationtimes where timesofregistration=2 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		patientsWithTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		SqlCohortDefinition patientsWithThreeVisits = new SqlCohortDefinition();
		patientsWithThreeVisits.setName("patientsWithThreeVisits");
		patientsWithThreeVisits
		        .setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type="
		                + registration.getEncounterTypeId()
		                + " and e.voided=0 and e.location_id = :location group by e.patient_id) as patientregistrationtimes where timesofregistration=3 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		patientsWithThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		SqlCohortDefinition patientsWithGreaterThanThreeVisits = new SqlCohortDefinition();
		patientsWithGreaterThanThreeVisits.setName("patientsWithGreaterThanThreeVisits");
		patientsWithGreaterThanThreeVisits
		        .setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type="
		                + registration.getEncounterTypeId()
		                + " and e.voided=0 and e.location_id = :location group by e.patient_id) as patientregistrationtimes where timesofregistration>3 and encounter_datetime>= :startDate and encounter_datetime<= :endDate ");
		patientsWithGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		// 5.1.1 Patients with Mutuelle Insurance and 1 visit
		CompositionCohortDefinition patientsWithMUTUELLEInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithMUTUELLEInsAndOneVisit.setName("patientsWithMUTUELLEInsAndOneVisit");
		patientsWithMUTUELLEInsAndOneVisit.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMUTUELLEInsAndOneVisit.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMUTUELLEInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndOneVisit.getSearches().put(
		    "patientsWithOneVisit",
		    new Mapped<CohortDefinition>(patientsWithOneVisit, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMUTUELLEInsAndOneVisit.getSearches().put(
		    "MUTUELLEInsCohortDef",
		    new Mapped<CohortDefinition>(MUTUELLEInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEInsAndOneVisit.setCompositionString("patientsWithOneVisit AND MUTUELLEInsCohortDef");
		
		CohortIndicator patientsWithMUTUELLEInsAndOneVisitIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMUTUELLEInsAndOneVisitIndicator",
		            patientsWithMUTUELLEInsAndOneVisit,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.1.2 Patients with RAMA Insurance and 1 visit
		CompositionCohortDefinition patientsWithRAMAInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithRAMAInsAndOneVisit.setName("patientsWithRAMAInsAndOneVisit");
		patientsWithRAMAInsAndOneVisit.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithRAMAInsAndOneVisit.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithRAMAInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndOneVisit.getSearches().put(
		    "patientsWithOneVisit",
		    new Mapped<CohortDefinition>(patientsWithOneVisit, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithRAMAInsAndOneVisit.getSearches().put(
		    "RAMAInsCohortDef",
		    new Mapped<CohortDefinition>(RAMAInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAInsAndOneVisit.setCompositionString("patientsWithOneVisit AND RAMAInsCohortDef");
		
		CohortIndicator patientsWithRAMAInsAndOneVisitIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithRAMAInsAndOneVisitIndicator",
		            patientsWithRAMAInsAndOneVisit,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.1.3 Patients with MMI Insurance and 1 visit
		CompositionCohortDefinition patientsWithMMIInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithMMIInsAndOneVisit.setName("patientsWithMMIInsAndOneVisit");
		patientsWithMMIInsAndOneVisit.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMMIInsAndOneVisit.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMMIInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIInsAndOneVisit.getSearches().put(
		    "patientsWithOneVisit",
		    new Mapped<CohortDefinition>(patientsWithOneVisit, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMMIInsAndOneVisit.getSearches().put(
		    "MMIInsCohortDef",
		    new Mapped<CohortDefinition>(MMIInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIInsAndOneVisit.setCompositionString("patientsWithOneVisit AND MMIInsCohortDef");
		
		CohortIndicator patientsWithMMIInsAndOneVisitIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMMIInsAndOneVisitIndicator",
		            patientsWithMMIInsAndOneVisit,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.1.4 Patients with MEDIPLAN Insurance and 1 visit
		CompositionCohortDefinition patientsWithMEDIPLANInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithMEDIPLANInsAndOneVisit.setName("patientsWithMEDIPLANInsAndOneVisit");
		patientsWithMEDIPLANInsAndOneVisit.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMEDIPLANInsAndOneVisit.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMEDIPLANInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndOneVisit.getSearches().put(
		    "patientsWithOneVisit",
		    new Mapped<CohortDefinition>(patientsWithOneVisit, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMEDIPLANInsAndOneVisit.getSearches().put(
		    "MEDIPLANInsCohortDef",
		    new Mapped<CohortDefinition>(MEDIPLANInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANInsAndOneVisit.setCompositionString("patientsWithOneVisit AND MEDIPLANInsCohortDef");
		
		CohortIndicator patientsWithMEDIPLANInsAndOneVisitIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMEDIPLANInsAndOneVisitIndicator",
		            patientsWithMEDIPLANInsAndOneVisit,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.1.5 Patients with CORAR Insurance and 1 visit
		CompositionCohortDefinition patientsWithCORARInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithCORARInsAndOneVisit.setName("patientsWithCORARInsAndOneVisit");
		patientsWithCORARInsAndOneVisit.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithCORARInsAndOneVisit.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithCORARInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndOneVisit.getSearches().put(
		    "patientsWithOneVisit",
		    new Mapped<CohortDefinition>(patientsWithOneVisit, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithCORARInsAndOneVisit.getSearches().put(
		    "CORARInsCohortDef",
		    new Mapped<CohortDefinition>(CORARInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARInsAndOneVisit.setCompositionString("patientsWithOneVisit AND CORARInsCohortDef");
		
		CohortIndicator patientsWithCORARInsAndOneVisitIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithCORARInsAndOneVisitIndicator",
		            patientsWithCORARInsAndOneVisit,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.1.6 Patients with NONE Insurance and 1 visit
		CompositionCohortDefinition patientsWithNONEInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithNONEInsAndOneVisit.setName("patientsWithNONEInsAndOneVisit");
		patientsWithNONEInsAndOneVisit.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithNONEInsAndOneVisit.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithNONEInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndOneVisit.getSearches().put(
		    "patientsWithOneVisit",
		    new Mapped<CohortDefinition>(patientsWithOneVisit, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithNONEInsAndOneVisit.getSearches().put(
		    "NONEInsCohortDef",
		    new Mapped<CohortDefinition>(NONEInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEInsAndOneVisit.setCompositionString("patientsWithOneVisit AND NONEInsCohortDef");
		
		CohortIndicator patientsWithNONEInsAndOneVisitIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithNONEInsAndOneVisitIndicator",
		            patientsWithNONEInsAndOneVisit,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.1.7 Patients without Insurance and 1 visit
		CompositionCohortDefinition patientsWithMissingInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithMissingInsAndOneVisit.setName("patientsWithMissingInsAndOneVisit");
		patientsWithMissingInsAndOneVisit.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMissingInsAndOneVisit.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMissingInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMissingInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndOneVisit.getSearches().put(
		    "patientsWithOneVisit",
		    new Mapped<CohortDefinition>(patientsWithOneVisit, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMissingInsAndOneVisit.getSearches().put(
		    "patientsMissingIns",
		    new Mapped<CohortDefinition>(patientsMissingIns, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMissingInsAndOneVisit.setCompositionString("patientsWithOneVisit AND patientsMissingIns");
		
		CohortIndicator patientsWithMissingInsAndOneVisitIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMissingInsAndOneVisitIndicator",
		            patientsWithMissingInsAndOneVisit,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.2.1 Patients with Mutuelle Insurance and 2 visits
		CompositionCohortDefinition patientsWithMUTUELLEInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithMUTUELLEInsAndTwoVisits.setName("patientsWithMUTUELLEInsAndTwoVisits");
		patientsWithMUTUELLEInsAndTwoVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMUTUELLEInsAndTwoVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMUTUELLEInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndTwoVisits.getSearches().put(
		    "patientsWithTwoVisits",
		    new Mapped<CohortDefinition>(patientsWithTwoVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMUTUELLEInsAndTwoVisits.getSearches().put(
		    "MUTUELLEInsCohortDef",
		    new Mapped<CohortDefinition>(MUTUELLEInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND MUTUELLEInsCohortDef");
		
		CohortIndicator patientsWithMUTUELLEInsAndTwoVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMUTUELLEInsAndTwoVisitsIndicator",
		            patientsWithMUTUELLEInsAndTwoVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.2.2 Patients with RAMA Insurance and 2 visits
		CompositionCohortDefinition patientsWithRAMAInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithRAMAInsAndTwoVisits.setName("patientsWithRAMAInsAndTwoVisits");
		patientsWithRAMAInsAndTwoVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithRAMAInsAndTwoVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithRAMAInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndTwoVisits.getSearches().put(
		    "patientsWithTwoVisits",
		    new Mapped<CohortDefinition>(patientsWithTwoVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithRAMAInsAndTwoVisits.getSearches().put(
		    "RAMAInsCohortDef",
		    new Mapped<CohortDefinition>(RAMAInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND RAMAInsCohortDef");
		
		CohortIndicator patientsWithRAMAInsAndTwoVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithRAMAInsAndTwoVisitsIndicator",
		            patientsWithRAMAInsAndTwoVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.2.3 Patients with MMI Insurance and 2 visits
		CompositionCohortDefinition patientsWithMMIInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithMMIInsAndTwoVisits.setName("patientsWithMMIInsAndTwoVisits");
		patientsWithMMIInsAndTwoVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMMIInsAndTwoVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMMIInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIInsAndTwoVisits.getSearches().put(
		    "patientsWithTwoVisits",
		    new Mapped<CohortDefinition>(patientsWithTwoVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMMIInsAndTwoVisits.getSearches().put(
		    "MMIInsCohortDef",
		    new Mapped<CohortDefinition>(MMIInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND MMIInsCohortDef");
		
		CohortIndicator patientsWithMMIInsAndTwoVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMMIInsAndTwoVisitsIndicator",
		            patientsWithMMIInsAndTwoVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.2.4 Patients with MEDIPLAN Insurance and 2 visits
		CompositionCohortDefinition patientsWithMEDIPLANInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithMEDIPLANInsAndTwoVisits.setName("patientsWithMEDIPLANInsAndTwoVisits");
		patientsWithMEDIPLANInsAndTwoVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMEDIPLANInsAndTwoVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMEDIPLANInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndTwoVisits.getSearches().put(
		    "patientsWithTwoVisits",
		    new Mapped<CohortDefinition>(patientsWithTwoVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMEDIPLANInsAndTwoVisits.getSearches().put(
		    "MEDIPLANInsCohortDef",
		    new Mapped<CohortDefinition>(MEDIPLANInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND MEDIPLANInsCohortDef");
		
		CohortIndicator patientsWithMEDIPLANInsAndTwoVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMEDIPLANInsAndTwoVisitsIndicator",
		            patientsWithMEDIPLANInsAndTwoVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.2.5 Patients with CORAR Insurance and 2 visits
		CompositionCohortDefinition patientsWithCORARInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithCORARInsAndTwoVisits.setName("patientsWithCORARInsAndTwoVisits");
		patientsWithCORARInsAndTwoVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithCORARInsAndTwoVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithCORARInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndTwoVisits.getSearches().put(
		    "patientsWithTwoVisits",
		    new Mapped<CohortDefinition>(patientsWithTwoVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithCORARInsAndTwoVisits.getSearches().put(
		    "CORARInsCohortDef",
		    new Mapped<CohortDefinition>(CORARInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND CORARInsCohortDef");
		
		CohortIndicator patientsWithCORARInsAndTwoVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithCORARInsAndTwoVisitsIndicator",
		            patientsWithCORARInsAndTwoVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.2.6 Patients with NONE Insurance and 2 visits
		CompositionCohortDefinition patientsWithNONEInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithNONEInsAndTwoVisits.setName("patientsWithNONEInsAndTwoVisits");
		patientsWithNONEInsAndTwoVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithNONEInsAndTwoVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithNONEInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndTwoVisits.getSearches().put(
		    "patientsWithTwoVisits",
		    new Mapped<CohortDefinition>(patientsWithTwoVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithNONEInsAndTwoVisits.getSearches().put(
		    "NONEInsCohortDef",
		    new Mapped<CohortDefinition>(NONEInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND NONEInsCohortDef");
		
		CohortIndicator patientsWithNONEInsAndTwoVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithNONEInsAndTwoVisitsIndicator",
		            patientsWithNONEInsAndTwoVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.2.7 Patients without Insurance and 2 visits
		CompositionCohortDefinition patientsWithMissingInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithMissingInsAndTwoVisits.setName("patientsWithMissingInsAndTwoVisits");
		patientsWithMissingInsAndTwoVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMissingInsAndTwoVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMissingInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMissingInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndTwoVisits.getSearches().put(
		    "patientsWithTwoVisits",
		    new Mapped<CohortDefinition>(patientsWithTwoVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMissingInsAndTwoVisits.getSearches().put(
		    "patientsMissingIns",
		    new Mapped<CohortDefinition>(patientsMissingIns, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMissingInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND patientsMissingIns");
		
		CohortIndicator patientsWithMissingInsAndTwoVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMissingInsAndTwoVisitsIndicator",
		            patientsWithMissingInsAndTwoVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.3.1 Patients with Mutuelle Insurance and 3 visits
		CompositionCohortDefinition patientsWithMUTUELLEInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithMUTUELLEInsAndThreeVisits.setName("patientsWithMUTUELLEInsAndThreeVisits");
		patientsWithMUTUELLEInsAndThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMUTUELLEInsAndThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMUTUELLEInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndThreeVisits.getSearches().put(
		    "patientsWithThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMUTUELLEInsAndThreeVisits.getSearches().put(
		    "MUTUELLEInsCohortDef",
		    new Mapped<CohortDefinition>(MUTUELLEInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND MUTUELLEInsCohortDef");
		
		CohortIndicator patientsWithMUTUELLEInsAndThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMUTUELLEInsAndThreeVisitsIndicator",
		            patientsWithMUTUELLEInsAndThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.3.2 Patients with RAMA Insurance and 3 visits
		CompositionCohortDefinition patientsWithRAMAInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithRAMAInsAndThreeVisits.setName("patientsWithRAMAInsAndThreeVisits");
		patientsWithRAMAInsAndThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithRAMAInsAndThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithRAMAInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndThreeVisits.getSearches().put(
		    "patientsWithThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithRAMAInsAndThreeVisits.getSearches().put(
		    "RAMAInsCohortDef",
		    new Mapped<CohortDefinition>(RAMAInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND RAMAInsCohortDef");
		
		CohortIndicator patientsWithRAMAInsAndThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithRAMAInsAndThreeVisitsIndicator",
		            patientsWithRAMAInsAndThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.3.3 Patients with MMI Insurance and 3 visits
		CompositionCohortDefinition patientsWithMMIInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithMMIInsAndThreeVisits.setName("patientsWithMMIInsAndThreeVisits");
		patientsWithMMIInsAndThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMMIInsAndThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMMIInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIInsAndThreeVisits.getSearches().put(
		    "patientsWithThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMMIInsAndThreeVisits.getSearches().put(
		    "MMIInsCohortDef",
		    new Mapped<CohortDefinition>(MMIInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND MMIInsCohortDef");
		
		CohortIndicator patientsWithMMIInsAndThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMMIInsAndThreeVisitsIndicator",
		            patientsWithMMIInsAndThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.3.4 Patients with MEDIPLAN Insurance and 3 visits
		CompositionCohortDefinition patientsWithMEDIPLANInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithMEDIPLANInsAndThreeVisits.setName("patientsWithMEDIPLANInsAndThreeVisits");
		patientsWithMEDIPLANInsAndThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMEDIPLANInsAndThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMEDIPLANInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndThreeVisits.getSearches().put(
		    "patientsWithThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMEDIPLANInsAndThreeVisits.getSearches().put(
		    "MEDIPLANInsCohortDef",
		    new Mapped<CohortDefinition>(MEDIPLANInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND MEDIPLANInsCohortDef");
		
		CohortIndicator patientsWithMEDIPLANInsAndThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMEDIPLANInsAndThreeVisitsIndicator",
		            patientsWithMEDIPLANInsAndThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.3.5 Patients with CORAR Insurance and 3 visits
		CompositionCohortDefinition patientsWithCORARInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithCORARInsAndThreeVisits.setName("patientsWithCORARInsAndThreeVisits");
		patientsWithCORARInsAndThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithCORARInsAndThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithCORARInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndThreeVisits.getSearches().put(
		    "patientsWithThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithCORARInsAndThreeVisits.getSearches().put(
		    "CORARInsCohortDef",
		    new Mapped<CohortDefinition>(CORARInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND CORARInsCohortDef");
		
		CohortIndicator patientsWithCORARInsAndThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithCORARInsAndThreeVisitsIndicator",
		            patientsWithCORARInsAndThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.3.6 Patients with NONE Insurance and 3 visits
		CompositionCohortDefinition patientsWithNONEInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithNONEInsAndThreeVisits.setName("patientsWithNONEInsAndThreeVisits");
		patientsWithNONEInsAndThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithNONEInsAndThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithNONEInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndThreeVisits.getSearches().put(
		    "patientsWithThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithNONEInsAndThreeVisits.getSearches().put(
		    "NONEInsCohortDef",
		    new Mapped<CohortDefinition>(NONEInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND NONEInsCohortDef");
		
		CohortIndicator patientsWithNONEInsAndThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithNONEInsAndThreeVisitsIndicator",
		            patientsWithNONEInsAndThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.3.7 Patients without Insurance and 3 visits
		CompositionCohortDefinition patientsWithMissingInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithMissingInsAndThreeVisits.setName("patientsWithMissingInsAndThreeVisits");
		patientsWithMissingInsAndThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMissingInsAndThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMissingInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMissingInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndThreeVisits.getSearches().put(
		    "patientsWithThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMissingInsAndThreeVisits.getSearches().put(
		    "patientsMissingIns",
		    new Mapped<CohortDefinition>(patientsMissingIns, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMissingInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND patientsMissingIns");
		
		CohortIndicator patientsWithMissingInsAndThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMissingInsAndThreeVisitsIndicator",
		            patientsWithMissingInsAndThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.4.1 Patients with Mutuelle Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithMUTUELLEInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.setName("patientsWithMUTUELLEInsAndGreaterThanThreeVisits");
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.getSearches().put(
		    "patientsWithGreaterThanThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.getSearches().put(
		    "MUTUELLEInsCohortDef",
		    new Mapped<CohortDefinition>(MUTUELLEInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits
		        .setCompositionString("patientsWithGreaterThanThreeVisits AND MUTUELLEInsCohortDef");
		
		CohortIndicator patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator",
		            patientsWithMUTUELLEInsAndGreaterThanThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.4.2 Patients with RAMA Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithRAMAInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithRAMAInsAndGreaterThanThreeVisits.setName("patientsWithRAMAInsAndGreaterThanThreeVisits");
		patientsWithRAMAInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisits.getSearches().put(
		    "patientsWithGreaterThanThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithRAMAInsAndGreaterThanThreeVisits.getSearches().put(
		    "RAMAInsCohortDef",
		    new Mapped<CohortDefinition>(RAMAInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAInsAndGreaterThanThreeVisits
		        .setCompositionString("patientsWithGreaterThanThreeVisits AND RAMAInsCohortDef");
		
		CohortIndicator patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator",
		            patientsWithRAMAInsAndGreaterThanThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.4.3 Patients with MMI Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithMMIInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithMMIInsAndGreaterThanThreeVisits.setName("patientsWithMMIInsAndGreaterThanThreeVisits");
		patientsWithMMIInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisits.getSearches().put(
		    "patientsWithGreaterThanThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMMIInsAndGreaterThanThreeVisits.getSearches().put(
		    "MMIInsCohortDef",
		    new Mapped<CohortDefinition>(MMIInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIInsAndGreaterThanThreeVisits
		        .setCompositionString("patientsWithGreaterThanThreeVisits AND MMIInsCohortDef");
		
		CohortIndicator patientsWithMMIInsAndGreaterThanThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMMIInsAndGreaterThanThreeVisitsIndicator",
		            patientsWithMMIInsAndGreaterThanThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.4.4 Patients with MEDIPLAN Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithMEDIPLANInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.setName("patientsWithMEDIPLANInsAndGreaterThanThreeVisits");
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.getSearches().put(
		    "patientsWithGreaterThanThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.getSearches().put(
		    "MEDIPLANInsCohortDef",
		    new Mapped<CohortDefinition>(MEDIPLANInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits
		        .setCompositionString("patientsWithGreaterThanThreeVisits AND MEDIPLANInsCohortDef");
		
		CohortIndicator patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator",
		            patientsWithMEDIPLANInsAndGreaterThanThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.4.5 Patients with CORAR Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithCORARInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithCORARInsAndGreaterThanThreeVisits.setName("patientsWithCORARInsAndGreaterThanThreeVisits");
		patientsWithCORARInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisits.getSearches().put(
		    "patientsWithGreaterThanThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithCORARInsAndGreaterThanThreeVisits.getSearches().put(
		    "CORARInsCohortDef",
		    new Mapped<CohortDefinition>(CORARInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARInsAndGreaterThanThreeVisits
		        .setCompositionString("patientsWithGreaterThanThreeVisits AND CORARInsCohortDef");
		
		CohortIndicator patientsWithCORARInsAndGreaterThanThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithCORARInsAndGreaterThanThreeVisitsIndicator",
		            patientsWithCORARInsAndGreaterThanThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.4.6 Patients with NONE Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithNONEInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithNONEInsAndGreaterThanThreeVisits.setName("patientsWithNONEInsAndGreaterThanThreeVisits");
		patientsWithNONEInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisits.getSearches().put(
		    "patientsWithGreaterThanThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithNONEInsAndGreaterThanThreeVisits.getSearches().put(
		    "NONEInsCohortDef",
		    new Mapped<CohortDefinition>(NONEInsCohortDef, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEInsAndGreaterThanThreeVisits
		        .setCompositionString("patientsWithGreaterThanThreeVisits AND NONEInsCohortDef");
		
		CohortIndicator patientsWithNONEInsAndGreaterThanThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithNONEInsAndGreaterThanThreeVisitsIndicator",
		            patientsWithNONEInsAndGreaterThanThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// 5.4.7 Patients without Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithMissingInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithMissingInsAndGreaterThanThreeVisits.setName("patientsWithMissingInsAndGreaterThanThreeVisits");
		patientsWithMissingInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisits.getSearches().put(
		    "patientsWithGreaterThanThreeVisits",
		    new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMissingInsAndGreaterThanThreeVisits.getSearches().put(
		    "patientsMissingIns",
		    new Mapped<CohortDefinition>(patientsMissingIns, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMissingInsAndGreaterThanThreeVisits
		        .setCompositionString("patientsWithGreaterThanThreeVisits AND patientsMissingIns");
		
		CohortIndicator patientsWithMissingInsAndGreaterThanThreeVisitsIndicator = Indicators
		        .newCohortIndicator(
		            "patientsWithMissingInsAndGreaterThanThreeVisitsIndicator",
		            patientsWithMissingInsAndGreaterThanThreeVisits,
		            ParameterizableUtil
		                    .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		
		// ========================================================================
		// 6. Age breakdown by gender
		// ========================================================================
		
		SqlObjectGroupDefinition ageBreakdownByGender = new SqlObjectGroupDefinition();
		ageBreakdownByGender.setName("ageBreakdownByGender");
		ageBreakdownByGender
		        .setQuery("select e.encounter_id, e.patient_id from encounter e, person p, patient pat  where e.voided = 0 and p.voided = 0 and pat.voided = 0  and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + "  and e.patient_id = p.person_id and e.patient_id = pat.patient_id and (YEAR(e.encounter_datetime)-YEAR(p.birthdate)) - (RIGHT(e.encounter_datetime,5)<RIGHT(p.birthdate,5)) < :maxAgeExclusive and (YEAR(e.encounter_datetime)-YEAR(p.birthdate)) - (RIGHT(e.encounter_datetime,5)<RIGHT(p.birthdate,5)) >= :minAgeInclusive and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and p.gender = :gender and e.location_id = :location");
		ageBreakdownByGender.addParameter(new Parameter("startDate", "startDate", Date.class));
		ageBreakdownByGender.addParameter(new Parameter("endDate", "endDate", Date.class));
		ageBreakdownByGender.addParameter(new Parameter("maxAgeExclusive", "maxAgeExclusive", Integer.class));
		ageBreakdownByGender.addParameter(new Parameter("minAgeInclusive", "minAgeInclusive", Integer.class));
		ageBreakdownByGender.addParameter(new Parameter("gender", "gender", String.class));
		
		// "6.1.m", "Male with age (0-1)",
		// maleWithRegistrationAndAgeZeroToOneIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeZeroToOneIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Male with age (0-1)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=M,maxAgeExclusive=1,minAgeInclusive=0,startDate=${startDate},endDate=${endDate}"));
		
		// rd.addIndicator("6.1.f", "Female with age (0-1)",
		// femaleWithRegistrationAndAgeZeroToOneIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeZeroToOneIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Female with age (0-1)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=F,maxAgeExclusive=1,minAgeInclusive=0,startDate=${startDate},endDate=${endDate}"));
		
		// rd.addIndicator("6.2.m", "Male with age (1-2)",
		// maleWithRegistrationAndAgeOneToTwoIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeOneToTwoIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Male with age (1-2)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=M,maxAgeExclusive=2,minAgeInclusive=1,startDate=${startDate},endDate=${endDate}"));
		
		// rd.addIndicator("6.2.f", "Female with age (1-2)",
		// femaleWithRegistrationAndAgeOneToTwoIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeOneToTwoIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Female with age (1-2)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=F,maxAgeExclusive=2,minAgeInclusive=1,startDate=${startDate},endDate=${endDate}"));
		
		// rd.addIndicator("6.3.m", "Male with age (2-3)",
		// maleWithRegistrationAndAgeTwoToThreeIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeTwoToThreeIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Male with age (2-3)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=M,maxAgeExclusive=3,minAgeInclusive=2,startDate=${startDate},endDate=${endDate}"));
		
		// rd.addIndicator("6.3.f", "Female with age (2-3)",
		// femaleWithRegistrationAndAgeTwoToThreeIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeTwoToThreeIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Female with age (2-3)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=F,maxAgeExclusive=3,minAgeInclusive=2,startDate=${startDate},endDate=${endDate}"));
		
		// rd.addIndicator("6.4.m", "Male with age (3-4)",
		// maleWithRegistrationAndAgeThreeToFourIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeThreeToFourIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Male with age (3-4)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=M,maxAgeExclusive=4,minAgeInclusive=3,startDate=${startDate},endDate=${endDate}"));
		
		// rd.addIndicator("6.4.f", "Female with age (3-4)",
		// femaleWithRegistrationAndAgeThreeToFourIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeThreeToFourIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Female with age (3-4)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=F,maxAgeExclusive=4,minAgeInclusive=3,startDate=${startDate},endDate=${endDate}"));
		
		// rd.addIndicator("6.5.m", "Male with age (4-5)",
		// maleWithRegistrationAndAgeFourToFiveIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeFourToFiveIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Male with age (4-5)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=M,maxAgeExclusive=5,minAgeInclusive=4,startDate=${startDate},endDate=${endDate}"));
		
		// rd.addIndicator("6.5.f", "Female with age (4-5)",
		// femaleWithRegistrationAndAgeFourToFiveIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeFourToFiveIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Female with age (4-5)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=F,maxAgeExclusive=5,minAgeInclusive=4,startDate=${startDate},endDate=${endDate}"));
		
		// rd.addIndicator("6.6.m", "Male with age (5-15)",
		// maleWithRegistrationAndAgeFiveToFifteenIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeFiveToFifteenIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Male with age (5-15)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=M,maxAgeExclusive=15,minAgeInclusive=5,startDate=${startDate},endDate=${endDate}"));
		
		// rd.addIndicator("6.6.f", "Female with age (5-15)",
		// femaleWithRegistrationAndAgeFiveToFifteenIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeFiveToFifteenIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Female with age (5-15)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=F,maxAgeExclusive=15,minAgeInclusive=5,startDate=${startDate},endDate=${endDate}"));
		// rd.addIndicator("6.7.m", "Male with age (15+)",
		// maleWithRegistrationAndAgeFifteenAndPlusIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeFifteenAndPlusIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Male with age (15+)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=M,maxAgeExclusive=150,minAgeInclusive=15,startDate=${startDate},endDate=${endDate}"));
		
		// rd.addIndicator("6.7.f", "Female with age (15+)",
		// femaleWithRegistrationAndAgeFifteenAndPlusIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeFifteenAndPlusIndicator = Indicators
		        .newCountIndicatorObjectGroupIndicator(
		            "Female with age (15+)",
		            ageBreakdownByGender,
		            ParameterizableUtil
		                    .createParameterMappings("gender=F,maxAgeExclusive=150,minAgeInclusive=15,startDate=${startDate},endDate=${endDate}"));
		
		// ========================================================================
		// 7. Primary care service requested
		// ========================================================================
		
		// 7.1.f Female Total number of patient requested primary care
		
		SqlObjectGroupDefinition femalePatientsrequestPrimCare = new SqlObjectGroupDefinition();
		femalePatientsrequestPrimCare.setName("femalePatientsrequestPrimCare");
		femalePatientsrequestPrimCare
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientsrequestPrimCare.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestPrimCare.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestPrimCareInRegistrationIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestPrimCareInRegistrationIndicator", femalePatientsrequestPrimCare,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.1.m Female Total number of patient requested primary care
		
		SqlObjectGroupDefinition malePatientsrequestPrimCare = new SqlObjectGroupDefinition();
		malePatientsrequestPrimCare.setName("malePatientsrequestPrimCare");
		malePatientsrequestPrimCare
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientsrequestPrimCare.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestPrimCare.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestPrimCareInRegistrationIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestPrimCareInRegistrationIndicator", malePatientsrequestPrimCare,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.2.f Female Number of patients requested VCT PROGRAM
		
		SqlObjectGroupDefinition femalePatientRequestVCTProgram = new SqlObjectGroupDefinition();
		femalePatientRequestVCTProgram.setName("femalePatientRequestVCTProgram");
		femalePatientRequestVCTProgram
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + vctProgram.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestVCTProgram.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestVCTProgram.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestVCTProgramInRegistrationIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestVCTProgramInRegistrationIndicator", femalePatientRequestVCTProgram,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.2.m Male Number of patients requested VCT PROGRAM
		SqlObjectGroupDefinition malePatientRequestVCTProgram = new SqlObjectGroupDefinition();
		malePatientRequestVCTProgram.setName("malePatientRequestVCTProgram");
		malePatientRequestVCTProgram
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + vctProgram.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestVCTProgram.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestVCTProgram.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestVCTProgramInRegistrationIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestVCTProgramInRegistrationIndicator", malePatientRequestVCTProgram,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.3.f Female Number of patients requested ANTENATAL CLINIC
		
		SqlObjectGroupDefinition femalePatientRequestAntenatalClinic = new SqlObjectGroupDefinition();
		femalePatientRequestAntenatalClinic.setName("patientRequestAntenatalClinic");
		femalePatientRequestAntenatalClinic
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + antenatalClinic.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestAntenatalClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestAntenatalClinic.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestAntenatalClinicInRegistrationIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestAntenatalClinicInRegistrationIndicator", femalePatientRequestAntenatalClinic,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.3.m Male Number of patients requested ANTENATAL CLINIC
		SqlObjectGroupDefinition malePatientRequestAntenatalClinic = new SqlObjectGroupDefinition();
		malePatientRequestAntenatalClinic.setName("malePatientRequestAntenatalClinic");
		malePatientRequestAntenatalClinic
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + antenatalClinic.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestAntenatalClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestAntenatalClinic.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestAntenatalClinicInRegistrationIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestAntenatalClinicInRegistrationIndicator", malePatientRequestAntenatalClinic,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.4.f Female Number of patients requested FAMILY PLANNING SERVICES
		SqlObjectGroupDefinition femalepatientRequestFamilyPlaningServices = new SqlObjectGroupDefinition();
		femalepatientRequestFamilyPlaningServices.setName("femalepatientRequestFamilyPlaningServices");
		femalepatientRequestFamilyPlaningServices
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + familyPlanningServices.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalepatientRequestFamilyPlaningServices.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalepatientRequestFamilyPlaningServices.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestFamilyPlaningServicesRegistrationIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestFamilyPlaningServicesRegistrationIndicator", femalepatientRequestFamilyPlaningServices,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.4.m Male Number of patients requested FAMILY PLANNING SERVICES
		SqlObjectGroupDefinition malepatientRequestFamilyPlaningServices = new SqlObjectGroupDefinition();
		malepatientRequestFamilyPlaningServices.setName("malepatientRequestFamilyPlaningServices");
		malepatientRequestFamilyPlaningServices
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + familyPlanningServices.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malepatientRequestFamilyPlaningServices.addParameter(new Parameter("startDate", "startDate", Date.class));
		malepatientRequestFamilyPlaningServices.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestFamilyPlaningServicesRegistrationIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestFamilyPlaningServicesRegistrationIndicator", malepatientRequestFamilyPlaningServices,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.5.f Female Number of patients requested MUTUELLE SERVICE
		
		SqlObjectGroupDefinition femalePatientRequestMutuelleService = new SqlObjectGroupDefinition();
		femalePatientRequestMutuelleService.setName("femalePatientRequestMutuelleService");
		femalePatientRequestMutuelleService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + mutuelleServices.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestMutuelleService.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestMutuelleService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestMutuelleServiceRegistrationIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestMutuelleServiceRegistrationIndicator", femalePatientRequestMutuelleService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.5.m Male Number of patients requested MUTUELLE SERVICE
		
		SqlObjectGroupDefinition malePatientRequestMutuelleService = new SqlObjectGroupDefinition();
		malePatientRequestMutuelleService.setName("malePatientRequestMutuelleService");
		malePatientRequestMutuelleService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + mutuelleServices.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestMutuelleService.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestMutuelleService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestMutuelleServiceRegistrationIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestMutuelleServiceRegistrationIndicator", malePatientRequestMutuelleService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.6.f Female Number of patients requested ACCOUNTING OFFICE SERVICE
		
		SqlObjectGroupDefinition femalePatientRequestAccountingOfficeService = new SqlObjectGroupDefinition();
		femalePatientRequestAccountingOfficeService.setName("femalePatientRequestAccountingOfficeService");
		femalePatientRequestAccountingOfficeService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + accountingOfficeServices.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestAccountingOfficeService.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestAccountingOfficeService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestAccountingOfficeServiceRegistrationIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestAccountingOfficeServiceRegistrationIndicator",
		    femalePatientRequestAccountingOfficeService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.6.m Male Number of patients requested ACCOUNTING OFFICE SERVICE
		
		SqlObjectGroupDefinition malePatientRequestAccountingOfficeService = new SqlObjectGroupDefinition();
		malePatientRequestAccountingOfficeService.setName("malePatientRequestAccountingOfficeService");
		malePatientRequestAccountingOfficeService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + accountingOfficeServices.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestAccountingOfficeService.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestAccountingOfficeService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestAccountingOfficeServiceRegistrationIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestAccountingOfficeServiceRegistrationIndicator", malePatientRequestAccountingOfficeService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.7.f Female Number of patients requested INTEGRATED MANAGEMENT OF
		// ADULT ILLNESS SERVICE
		
		SqlObjectGroupDefinition femalePatientRequestAdultIllnessService = new SqlObjectGroupDefinition();
		femalePatientRequestAdultIllnessService.setName("femalePatientRequestAdultIllnessService");
		femalePatientRequestAdultIllnessService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + integratedManagementOfAdultIllnessServices.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestAdultIllnessService.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestAdultIllnessService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestAdultIllnessServiceIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestAdultIllnessServiceIndicator", femalePatientRequestAdultIllnessService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.7.m Male Number of patients requested INTEGRATED MANAGEMENT OF
		// ADULT ILLNESS SERVICE
		SqlObjectGroupDefinition malePatientRequestAdultIllnessService = new SqlObjectGroupDefinition();
		malePatientRequestAdultIllnessService.setName("malePatientRequestAdultIllnessService");
		malePatientRequestAdultIllnessService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + integratedManagementOfAdultIllnessServices.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestAdultIllnessService.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestAdultIllnessService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestAdultIllnessServiceIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestAdultIllnessServiceIndicator", malePatientRequestAdultIllnessService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.8.f Female Number of patients requested INTEGRATED MANAGEMENT OF
		// CHILDHOOD ILLNESS Service
		
		SqlObjectGroupDefinition femalePatientRequestChildIllnessService = new SqlObjectGroupDefinition();
		femalePatientRequestChildIllnessService.setName("femalePatientRequestChildIllnessService");
		femalePatientRequestChildIllnessService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + integratedManagementOfChildhoodIllnessServices.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestChildIllnessService.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestChildIllnessService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestChildIllnessServiceIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestChildIllnessServiceIndicator", femalePatientRequestChildIllnessService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.8.m Male Number of patients requested INTEGRATED MANAGEMENT OF
		// CHILDHOOD ILLNESS Service
		SqlObjectGroupDefinition malePatientRequestChildIllnessService = new SqlObjectGroupDefinition();
		malePatientRequestChildIllnessService.setName("malePatientRequestChildIllnessService");
		malePatientRequestChildIllnessService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + integratedManagementOfChildhoodIllnessServices.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestChildIllnessService.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestChildIllnessService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestChildIllnessServiceIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestChildIllnessServiceIndicator", malePatientRequestChildIllnessService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.9.f Female Number of patients requested INFECTIOUS DISEASES CLINIC
		// SERVICE
		
		SqlObjectGroupDefinition femalePatientRequestInfectiousDiseasesService = new SqlObjectGroupDefinition();
		femalePatientRequestInfectiousDiseasesService.setName("femalePatientRequestInfectiousDiseasesService");
		femalePatientRequestInfectiousDiseasesService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + infectiousDiseasesClinicService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestInfectiousDiseasesService.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestInfectiousDiseasesService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestInfectiousDiseasesServiceIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestInfectiousDiseasesServiceIndicator", femalePatientRequestInfectiousDiseasesService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.9.m Male Number of patients requested INFECTIOUS DISEASES CLINIC
		// SERVICE
		
		SqlObjectGroupDefinition malePatientRequestInfectiousDiseasesService = new SqlObjectGroupDefinition();
		malePatientRequestInfectiousDiseasesService.setName("malePatientRequestInfectiousDiseasesService");
		malePatientRequestInfectiousDiseasesService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + infectiousDiseasesClinicService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestInfectiousDiseasesService.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestInfectiousDiseasesService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestInfectiousDiseasesServiceIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestInfectiousDiseasesServiceIndicator", malePatientRequestInfectiousDiseasesService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.10.f Female Number of patients requested SOCIAL WORKER SERVICE
		
		SqlObjectGroupDefinition femalePatientRequestSocialWorkerService = new SqlObjectGroupDefinition();
		femalePatientRequestSocialWorkerService.setName("femalePatientRequestSocialWorkerService");
		femalePatientRequestSocialWorkerService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + socialWorkerService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestSocialWorkerService.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestSocialWorkerService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestSocialWorkerServiceIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestSocialWorkerServiceIndicator", femalePatientRequestSocialWorkerService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.10.m Male Number of patients requested SOCIAL WORKER SERVICE
		SqlObjectGroupDefinition malePatientRequestSocialWorkerService = new SqlObjectGroupDefinition();
		malePatientRequestSocialWorkerService.setName("malePatientRequestSocialWorkerService");
		malePatientRequestSocialWorkerService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + socialWorkerService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestSocialWorkerService.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestSocialWorkerService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestSocialWorkerServiceIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestSocialWorkerServiceIndicator", malePatientRequestSocialWorkerService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.11.f Female Number of patients requested PREVENTION OF MOTHER TO
		// CHILD TRANSMISSION SERVICE
		
		SqlObjectGroupDefinition femalePatientRequestPMTCTService = new SqlObjectGroupDefinition();
		femalePatientRequestPMTCTService.setName("femalePatientRequestPMTCTService");
		femalePatientRequestPMTCTService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + pmtctService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestPMTCTService.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestPMTCTService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestPMTCTServiceIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestPMTCTServiceIndicator", femalePatientRequestPMTCTService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.11.f Male Number of patients requested PREVENTION OF MOTHER TO
		// CHILD TRANSMISSION SERVICE
		
		SqlObjectGroupDefinition malePatientRequestPMTCTService = new SqlObjectGroupDefinition();
		malePatientRequestPMTCTService.setName("malePatientRequestPMTCTService");
		malePatientRequestPMTCTService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + pmtctService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestPMTCTService.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestPMTCTService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestPMTCTServiceIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestPMTCTServiceIndicator", malePatientRequestPMTCTService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.12.f. Female Number of patients requested LABORATORY SERVICE
		
		SqlObjectGroupDefinition femalePatientRequestLabService = new SqlObjectGroupDefinition();
		femalePatientRequestLabService.setName("femalePatientRequestLabService");
		femalePatientRequestLabService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + laboratoryService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestLabService.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestLabService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestLabServiceIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestLabServiceIndicator", femalePatientRequestLabService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.12.m Male Number of patients requested LABORATORY SERVICE
		SqlObjectGroupDefinition malePatientRequestLabService = new SqlObjectGroupDefinition();
		malePatientRequestLabService.setName("malePatientRequestLabService");
		malePatientRequestLabService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + laboratoryService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestLabService.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestLabService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestLabServiceIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestLabServiceIndicator", malePatientRequestLabService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.13.f. Female Number of patients requested PHARMACY SERVICES
		
		SqlObjectGroupDefinition femalePatientRequestPharmacyService = new SqlObjectGroupDefinition();
		femalePatientRequestPharmacyService.setName("femalePatientRequestPharmacyService");
		femalePatientRequestPharmacyService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + pharmacyService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestPharmacyService.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestPharmacyService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestPharmacyServiceIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestPharmacyServiceIndicator", femalePatientRequestPharmacyService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.13.m Male Number of patients requested PHARMACY SERVICE
		
		SqlObjectGroupDefinition malePatientRequestPharmacyService = new SqlObjectGroupDefinition();
		malePatientRequestPharmacyService.setName("malePatientRequestPharmacyService");
		malePatientRequestPharmacyService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + pharmacyService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestPharmacyService.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestPharmacyService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestPharmacyServiceIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestPharmacyServiceIndicator", malePatientRequestPharmacyService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		// 7.14.f. Female Number of patients requested MATERNITY SERVICES
		
		SqlObjectGroupDefinition femalePatientRequestMaternityService = new SqlObjectGroupDefinition();
		femalePatientRequestMaternityService.setName("femalePatientRequestMaternityService");
		femalePatientRequestMaternityService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + maternityService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestMaternityService.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestMaternityService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestMaternityServiceIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestMaternityServiceIndicator", femalePatientRequestMaternityService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.14.m Male Number of patients requested MATERNITY SERVICE
		
		SqlObjectGroupDefinition malePatientRequestMaternityService = new SqlObjectGroupDefinition();
		malePatientRequestMaternityService.setName("malePatientRequestMaternityService");
		malePatientRequestMaternityService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + maternityService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestMaternityService.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestMaternityService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestMaternityServiceIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestMaternityServiceIndicator", malePatientRequestMaternityService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.15.f Female Number of patients requested HOSPITALIZATION SERVICE
		
		SqlObjectGroupDefinition femalePatientRequestHospitalizationService = new SqlObjectGroupDefinition();
		femalePatientRequestHospitalizationService.setName("femalePatientRequestHospitalizationService");
		femalePatientRequestHospitalizationService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + hospitalizationService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestHospitalizationService.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestHospitalizationService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestHospitalizationServiceIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestHospitalizationServiceIndicator", femalePatientRequestHospitalizationService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.15.m Male Number of patients requested HOSPITALIZATION SERVICE
		
		SqlObjectGroupDefinition malePatientRequestHospitalizationService = new SqlObjectGroupDefinition();
		malePatientRequestHospitalizationService.setName("malePatientRequestHospitalizationService");
		malePatientRequestHospitalizationService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + hospitalizationService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestHospitalizationService.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestHospitalizationService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestHospitalizationServiceIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestHospitalizationServiceIndicator", malePatientRequestHospitalizationService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.16.f Female Number of patients requested VACCINATION SERVICE
		
		SqlObjectGroupDefinition femalePatientRequestVaccinationService = new SqlObjectGroupDefinition();
		femalePatientRequestVaccinationService.setName("femalePatientRequestVaccinationService");
		femalePatientRequestVaccinationService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + vaccinationService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestVaccinationService.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientRequestVaccinationService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator femalePatientsrequestVaccinationServiceIndicator = Indicators.objectGroupIndicator(
		    "femalePatientsrequestVaccinationServiceIndicator", femalePatientRequestVaccinationService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// 7.16.m Male Number of patients requested VACCINATION SERVICE
		
		SqlObjectGroupDefinition malePatientRequestVaccinationService = new SqlObjectGroupDefinition();
		malePatientRequestVaccinationService.setName("malePatientRequestVaccinationService");
		malePatientRequestVaccinationService
		        .setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
		                + primaryCareServiceRequested.getId()
		                + " and o.value_coded="
		                + vaccinationService.getId()
		                + " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
		                + registration.getEncounterTypeId()
		                + " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestVaccinationService.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientRequestVaccinationService.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		ObjectGroupIndicator malePatientsrequestVaccinationServiceIndicator = Indicators.objectGroupIndicator(
		    "malePatientsrequestVaccinationServiceIndicator", malePatientRequestVaccinationService,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		// add global filter to the report
		
		rd.addIndicator("2.1", "Percent of patients under 5 who do not have an observation for temperature in the vitals",
		    patientsWithoutTemperatureInVitalsIndicator);
		rd.addIndicator("2.2",
		    "Percent of children under 5 who did have observation for temperature, and actually had a fever",
		    patientsWithTemperatureGreaterThanNormalInVitalsIndicator);
		rd.addIndicator("2.3", "Percent of all registered patients under 5 who had a fever",
		    allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator);
		
		rd.addIndicator("3.1", "Average number of patients registered per hour Mon through Friday between 8 and 10 am",
		    peakHoursAndPeakDaysIndicator);
		
		rd.addIndicator("4.1", "Percent of patients who are missing an insurance in registration encounter",
		    percentOfPatientsMissingInsIndicator);
		rd.addIndicator("4.2", "Number of patients who are missing an insurance in registration encounter",
		    numberOfPatientsMissingInsIndicator);
		rd.addIndicator("4.3.1", "Percent of patients with MUTUELLE insurance in registration encounter",
		    percentOfPatientsWithMUTUELLEInsIndicator);
		rd.addIndicator("4.3.2", "Percent of patients with RAMA insurance in registration encounter",
		    percentOfPatientsWithRAMAInsIndicator);
		rd.addIndicator("4.3.3", "Percent of patients with MMI insurance in registration encounter",
		    percentOfPatientsWithMMIInsIndicator);
		rd.addIndicator("4.3.4", "Percent of patients with MEDIPLAN insurance in registration encounter",
		    percentOfPatientsWithMEDIPLANInsIndicator);
		rd.addIndicator("4.3.5", "Percent of patients with CORAR insurance in registration encounter",
		    percentOfPatientsWithCORARInsIndicator);
		rd.addIndicator("4.3.6", "Percent of patients without (NONE) insurance in registration encounter",
		    percentOfPatientsWithNONEInsIndicator);
		
		rd.addIndicator("5.1.1", "Number of patients who only have 1 registration encounter with MUTUELLE Insurance:",
		    patientsWithMUTUELLEInsAndOneVisitIndicator);
		rd.addIndicator("5.1.2", "Number of patients who only have 1 registration encounter with RAMA Insurance:",
		    patientsWithRAMAInsAndOneVisitIndicator);
		rd.addIndicator("5.1.3", "Number of patients who only have 1 registration encounter with MMI Insurance:",
		    patientsWithMMIInsAndOneVisitIndicator);
		rd.addIndicator("5.1.4", "Number of patients who only have 1 registration encounter with MEDIPLAN Insurance:",
		    patientsWithMEDIPLANInsAndOneVisitIndicator);
		rd.addIndicator("5.1.5", "Number of patients who only have 1 registration encounter with CORAR Insurance:",
		    patientsWithCORARInsAndOneVisitIndicator);
		rd.addIndicator("5.1.6", "Number of patients who only have 1 registration encounter with NONE Insurance:",
		    patientsWithNONEInsAndOneVisitIndicator);
		rd.addIndicator("5.1.7", "Number of patients who only have 1 registration encounter missing Insurance:",
		    patientsWithMissingInsAndOneVisitIndicator);
		rd.addIndicator("5.2.1", "Number of patients who have 2 registration encounters with MUTUELLE Insurance:",
		    patientsWithMUTUELLEInsAndTwoVisitsIndicator);
		rd.addIndicator("5.2.2", "Number of patients who have 2 registration encounters with RAMA Insurance:",
		    patientsWithRAMAInsAndTwoVisitsIndicator);
		rd.addIndicator("5.2.3", "Number of patients who have 2 registration encounters with MMI Insurance:",
		    patientsWithMMIInsAndTwoVisitsIndicator);
		rd.addIndicator("5.2.4", "Number of patients who have 2 registration encounters with MEDIPLAN Insurance:",
		    patientsWithMEDIPLANInsAndTwoVisitsIndicator);
		rd.addIndicator("5.2.5", "Number of patients who have 2 registration encounters with CORAR Insurance:",
		    patientsWithCORARInsAndTwoVisitsIndicator);
		rd.addIndicator("5.2.6", "Number of patients who have 2 registration encounters with NONE Insurance:",
		    patientsWithNONEInsAndTwoVisitsIndicator);
		rd.addIndicator("5.2.7", "Number of patients who have 2 registration encounters missing Insurance:",
		    patientsWithMissingInsAndTwoVisitsIndicator);
		rd.addIndicator("5.3.1", "Number of patients who have 3 registration encounters with MUTUELLE Insurance:",
		    patientsWithMUTUELLEInsAndThreeVisitsIndicator);
		rd.addIndicator("5.3.2", "Number of patients who have 3 registration encounters with RAMA Insurance:",
		    patientsWithRAMAInsAndThreeVisitsIndicator);
		rd.addIndicator("5.3.3", "Number of patients who have 3 registration encounters with MMI Insurance:",
		    patientsWithMMIInsAndThreeVisitsIndicator);
		rd.addIndicator("5.3.4", "Number of patients who have 3 registration encounters with MEDIPLAN Insurance:",
		    patientsWithMEDIPLANInsAndThreeVisitsIndicator);
		rd.addIndicator("5.3.5", "Number of patients who have 3 registration encounters with CORAR Insurance:",
		    patientsWithCORARInsAndThreeVisitsIndicator);
		rd.addIndicator("5.3.6", "Number of patients who have 3 registration encounters with NONE Insurance:",
		    patientsWithNONEInsAndThreeVisitsIndicator);
		rd.addIndicator("5.3.7", "Number of patients who have 3 registration encounters missing Insurance:",
		    patientsWithMissingInsAndThreeVisitsIndicator);
		rd.addIndicator("5.4.1", "Number of patients With greater than 3 registration encounters with MUTUELLE Insurance:",
		    patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator("5.4.2", "Number of patients With greater than 3 registration encounters with RAMA Insurance:",
		    patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator("5.4.3", "Number of patients With greater than 3 registration encounters with MMI Insurance:",
		    patientsWithMMIInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator("5.4.4", "Number of patients With greater than 3 registration encounters with MEDIPLAN Insurance:",
		    patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator("5.4.5", "Number of patients With greater than 3 registration encounters with CORAR Insurance:",
		    patientsWithCORARInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator("5.4.6", "Number of patients With greater than 3 registration encounters with NONE Insurance:",
		    patientsWithNONEInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator("5.4.7", "Number of patients With greater than 3 registration encounters missing Insurance:",
		    patientsWithMissingInsAndGreaterThanThreeVisitsIndicator);
		
		rd.addIndicator("6.1.m", "Male with age (0-1)", maleWithRegistrationAndAgeZeroToOneIndicator);
		rd.addIndicator("6.1.f", "Female with age (0-1)", femaleWithRegistrationAndAgeZeroToOneIndicator);
		rd.addIndicator("6.2.m", "Male with age (1-2)", maleWithRegistrationAndAgeOneToTwoIndicator);
		rd.addIndicator("6.2.f", "Female with age (1-2)", femaleWithRegistrationAndAgeOneToTwoIndicator);
		rd.addIndicator("6.3.m", "Male with age (2-3)", maleWithRegistrationAndAgeTwoToThreeIndicator);
		rd.addIndicator("6.3.f", "Female with age (2-3)", femaleWithRegistrationAndAgeTwoToThreeIndicator);
		rd.addIndicator("6.4.m", "Male with age (3-4)", maleWithRegistrationAndAgeThreeToFourIndicator);
		rd.addIndicator("6.4.f", "Female with age (3-4)", femaleWithRegistrationAndAgeThreeToFourIndicator);
		rd.addIndicator("6.5.m", "Male with age (4-5)", maleWithRegistrationAndAgeFourToFiveIndicator);
		rd.addIndicator("6.5.f", "Female with age (4-5)", femaleWithRegistrationAndAgeFourToFiveIndicator);
		rd.addIndicator("6.6.m", "Male with age (5-15)", maleWithRegistrationAndAgeFiveToFifteenIndicator);
		rd.addIndicator("6.6.f", "Female with age (5-15)", femaleWithRegistrationAndAgeFiveToFifteenIndicator);
		rd.addIndicator("6.7.m", "Male with age (15+)", maleWithRegistrationAndAgeFifteenAndPlusIndicator);
		rd.addIndicator("6.7.f", "Female with age (15+)", femaleWithRegistrationAndAgeFifteenAndPlusIndicator);
		
		rd.addIndicator("7.1.f", "Female number of patient requested primary care",
		    femalePatientsrequestPrimCareInRegistrationIndicator);
		rd.addIndicator("7.1.m", "Male number of patient requested primary care",
		    malePatientsrequestPrimCareInRegistrationIndicator);
		rd.addIndicator("7.2.f", "Female Number of patients requested VCT PROGRAM",
		    femalePatientsrequestVCTProgramInRegistrationIndicator);
		rd.addIndicator("7.2.m", "Male Number of patients requested VCT PROGRAM",
		    malePatientsrequestVCTProgramInRegistrationIndicator);
		rd.addIndicator("7.3.f", "Female Number of patients requested ANTENATAL CLINIC",
		    femalePatientsrequestAntenatalClinicInRegistrationIndicator);
		rd.addIndicator("7.3.m", "Male Number of patients requested ANTENATAL CLINIC",
		    malePatientsrequestAntenatalClinicInRegistrationIndicator);
		rd.addIndicator("7.4.f", "Female Number of patients requested FAMILY PLANNING SERVICES",
		    femalePatientsrequestFamilyPlaningServicesRegistrationIndicator);
		rd.addIndicator("7.4.m", "Male Number of patients requested FAMILY PLANNING SERVICES",
		    malePatientsrequestFamilyPlaningServicesRegistrationIndicator);
		rd.addIndicator("7.5.f", "Female Number of patients requested MUTUELLE SERVICE",
		    femalePatientsrequestMutuelleServiceRegistrationIndicator);
		rd.addIndicator("7.5.m", "Male Number of patients requested MUTUELLE SERVICE",
		    malePatientsrequestMutuelleServiceRegistrationIndicator);
		rd.addIndicator("7.6.f", "Female Number of patients requested ACCOUNTING OFFICE SERVICE",
		    femalePatientsrequestAccountingOfficeServiceRegistrationIndicator);
		rd.addIndicator("7.6.m", "Male Number of patients requested ACCOUNTING OFFICE SERVICE",
		    malePatientsrequestAccountingOfficeServiceRegistrationIndicator);
		rd.addIndicator("7.7.f", "Female Number of patients requested INTEGRATED MANAGEMENT OF ADULT ILLNESS SERVICE",
		    femalePatientsrequestAdultIllnessServiceIndicator);
		rd.addIndicator("7.7.m", "Male Number of patients requested INTEGRATED MANAGEMENT OF ADULT ILLNESS SERVICE",
		    malePatientsrequestAdultIllnessServiceIndicator);
		rd.addIndicator("7.8.f", "Female Number of patients requested INTEGRATED MANAGEMENT OF CHILDHOOD ILLNESS",
		    femalePatientsrequestChildIllnessServiceIndicator);
		rd.addIndicator("7.8.m", "Male Number of patients requested INTEGRATED MANAGEMENT OF CHILDHOOD ILLNESS",
		    malePatientsrequestChildIllnessServiceIndicator);
		rd.addIndicator("7.9.f", "Female Number of patients requested INFECTIOUS DISEASES CLINIC SERVICE",
		    femalePatientsrequestInfectiousDiseasesServiceIndicator);
		rd.addIndicator("7.9.m", "Male Number of patients requested INFECTIOUS DISEASES CLINIC SERVICE",
		    malePatientsrequestInfectiousDiseasesServiceIndicator);
		rd.addIndicator("7.10.f", "Female Number of patients requested SOCIAL WORKER SERVICE",
		    femalePatientsrequestSocialWorkerServiceIndicator);
		rd.addIndicator("7.10.m", "Male Number of patients requested SOCIAL WORKER SERVICE",
		    malePatientsrequestSocialWorkerServiceIndicator);
		rd.addIndicator("7.11.f", "Female number of patient requested PREVENTION OF MOTHER TO CHILD TRANSMISSION SERVICE",
		    femalePatientsrequestPMTCTServiceIndicator);
		rd.addIndicator("7.11.m", "Male number of patient requested PREVENTION OF MOTHER TO CHILD TRANSMISSION SERVICE",
		    malePatientsrequestPMTCTServiceIndicator);
		rd.addIndicator("7.12.f", "Female Number of patients requested LABORATORY SERVICE",
		    femalePatientsrequestLabServiceIndicator);
		rd.addIndicator("7.12.m", "Male Number of patients requested LABORATORY SERVICE",
		    malePatientsrequestLabServiceIndicator);
		rd.addIndicator("7.13.f", "Female Number of patients requested PHARMACY SERVICES",
		    femalePatientsrequestPharmacyServiceIndicator);
		rd.addIndicator("7.13.m", "Male Number of patients requested PHARMACY SERVICES",
		    malePatientsrequestPharmacyServiceIndicator);
		rd.addIndicator("7.14.f", "Female Number of patients requested MATERNITY SERVICE",
		    femalePatientsrequestMaternityServiceIndicator);
		rd.addIndicator("7.14.m", "Male Number of patients requested MATERNITY SERVICE",
		    malePatientsrequestMaternityServiceIndicator);
		rd.addIndicator("7.15.f", "Female Number of patients requested HOSPITALIZATION SERVICE",
		    femalePatientsrequestHospitalizationServiceIndicator);
		rd.addIndicator("7.15.m", "Male Number of patients requested HOSPITALIZATION SERVICE",
		    malePatientsrequestHospitalizationServiceIndicator);
		rd.addIndicator("7.16.f", "Female Number of patients requested VACCINATION SERVICE",
		    femalePatientsrequestVaccinationServiceIndicator);
		rd.addIndicator("7.16.m", "Male Number of patients requested VACCINATION SERVICE",
		    malePatientsrequestVaccinationServiceIndicator);
		
		rd.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort(),
		    ParameterizableUtil.createParameterMappings("location=${location}"));
		
		h.saveReportDefinition(rd);
		
		return rd;
	}
	
	private void setUpProperties() {
		registration = gp.getEncounterType(GlobalPropertiesManagement.PRIMARY_CARE_REGISTRATION);
		vitals = gp.getEncounterType(GlobalPropertiesManagement.VITALS);
		
		rwandaInsuranceType = gp.getConcept(GlobalPropertiesManagement.RWANDA_INSURANCE_TYPE);
		mutuelle = gp.getConcept(GlobalPropertiesManagement.MUTUELLE);
		rama = gp.getConcept(GlobalPropertiesManagement.RAMA);
		mmi = gp.getConcept(GlobalPropertiesManagement.MMI);
		mediplan = gp.getConcept(GlobalPropertiesManagement.MEDIPLAN);
		corar = gp.getConcept(GlobalPropertiesManagement.CORAR);
		none = gp.getConcept(GlobalPropertiesManagement.NONE);
		temperature = gp.getConcept(GlobalPropertiesManagement.TEMPERATURE);
		
		primaryCareServiceRequested = gp.getConcept(GlobalPropertiesManagement.PRIMARY_CARE_SERVICE_REQUESTED);
		vctProgram = gp.getConcept(GlobalPropertiesManagement.VCT_PROGRAM);
		antenatalClinic = gp.getConcept(GlobalPropertiesManagement.ANTENATAL_CLINIC);
		familyPlanningServices = gp.getConcept(GlobalPropertiesManagement.FAMILY_PLANNING_SERVICES);
		mutuelleServices = gp.getConcept(GlobalPropertiesManagement.MUTUELLE_SERVICE);
		accountingOfficeServices = gp.getConcept(GlobalPropertiesManagement.ACCOUNTING_OFFICE_SERVICE);
		integratedManagementOfAdultIllnessServices = gp
		        .getConcept(GlobalPropertiesManagement.INTEGRATED_MANAGEMENT_OF_ADULT_ILLNESS_SERVICE);
		integratedManagementOfChildhoodIllnessServices = gp
		        .getConcept(GlobalPropertiesManagement.INTEGRATED_MANAGEMENT_OF_CHILDHOOD_ILLNESS);
		infectiousDiseasesClinicService = gp.getConcept(GlobalPropertiesManagement.INFECTIOUS_DISEASES_CLINIC_SERVICE);
		socialWorkerService = gp.getConcept(GlobalPropertiesManagement.SOCIAL_WORKER_SERVICE);
		pmtctService = gp.getConcept(GlobalPropertiesManagement.PREVENTION_OF_MOTHER_TO_CHILD_TRANSMISSION_SERVICE);
		laboratoryService = gp.getConcept(GlobalPropertiesManagement.LABORATORY_SERVICES);
		pharmacyService = gp.getConcept(GlobalPropertiesManagement.PHARMACY_SERVICES);
		maternityService = gp.getConcept(GlobalPropertiesManagement.MATERNITY_SERVICE);
		hospitalizationService = gp.getConcept(GlobalPropertiesManagement.HOSPITALIZATION_SERVICE);
		vaccinationService = gp.getConcept(GlobalPropertiesManagement.VACCINATION_SERVICE);
		
		onOrBeforeonOrAfterParameterNames.add("onOrAfter");
		onOrBeforeonOrAfterParameterNames.add("onOrBefore");
	}
}
