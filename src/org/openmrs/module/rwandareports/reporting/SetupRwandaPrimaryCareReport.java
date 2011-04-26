package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.PrimaryCareReportConstants;
import org.openmrs.module.rwandareports.encounter.definition.EncounterGroupDefinition;
import org.openmrs.module.rwandareports.encounter.definition.SqlEncounterGroupDefinition;
import org.openmrs.module.rwandareports.encounter.indicator.EncounterIndicator;
import org.openmrs.module.rwandareports.report.definition.RollingDailyPeriodIndicatorReportDefinition;
import org.openmrs.module.rwandareports.util.RwandaReportsUtil;

public class SetupRwandaPrimaryCareReport {
	protected final static Log log = LogFactory
			.getLog(SetupRwandaPrimaryCareReport.class);
	Helper h = new Helper();

	private HashMap<String, String> properties;

	public SetupRwandaPrimaryCareReport(Helper helper) {
		h = helper;
	}

	public void setup() throws Exception {

		delete();

		// setUpGlobalProperties();

		int registrationEncTypeId = Integer.parseInt(Context
				.getAdministrationService().getGlobalProperty(
						"primarycarereport.registration.encountertypeid"));
		int vitalsEncTypeId = Integer.parseInt(Context
				.getAdministrationService().getGlobalProperty(
						"primarycarereport.vitals.encountertypeid"));

		EncounterType registration = Context.getEncounterService()
				.getEncounterType(registrationEncTypeId);
		if (registration == null)
			throw new RuntimeException(
					"Are you sure the global property primarycarereport.registration.encountertypeid is set correctly?");

		EncounterType vitals = Context.getEncounterService().getEncounterType(
				vitalsEncTypeId);
		if (vitals == null)
			throw new RuntimeException(
					"Are you sure the global property primarycarereport.vitals.encountertypeid is set correctly?");

		createLocationCohortDefinitions(registration);
		// createCompositionCohortDefinitions();
		// createIndicators();
		ReportDefinition rd = createReportDefinition(registration, vitals);
		h.createXlsCalendarOverview(rd,
				"rwandacalendarprimarycarereporttemplate.xls",
				"Primary_Care_Report_Template", null);
	}

	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		// for (ReportDesign rd : rs.getReportDesigns(null,
		// ExcelCalendarTemplateRenderer.class, false)) {
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Primary_Care_Report_Template".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(RollingDailyPeriodIndicatorReportDefinition.class,
				"Rwanda Primary Care Report");

		h.purgeDefinition(DataSetDefinition.class,
				"Rwanda Primary Care Report Data Set");
		h.purgeDefinition(CohortDefinition.class,
				"location: Primary Care Patients at location");

		h.purgeDefinition(CohortDefinition.class,
				"patientsWithPrimaryCareRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithPrimaryCareVitals");
		h.purgeDefinition(CohortDefinition.class, "lessThanFive");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithTemperatureInVitals");
		h.purgeDefinition(CohortDefinition.class,
				"patientsUnder5WithoutTemperatureInVitals");
		h.purgeDefinition(CohortDefinition.class,
				"patientsUnder5InRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientsUnder5WithTemperatureInVitals");
		h.purgeDefinition(CohortDefinition.class,
				"patientsUnder5WithTemperatureGreaterThanNormalInVitals");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithTemperatureGreaterThanNormalInVitals");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestPrimCare");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestPrimCareInRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestPrimCare");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestPrimCareInRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestVCTProgram");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestVCTProgramInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestVCTProgramInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientRequestAntenatalClinic");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestAntenatalClinicInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestAntenatalClinicInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientRequestFamilyPlaningServices");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestFamilyPlaningServicesRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestFamilyPlaningServicesRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientRequestMutuelleService");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestMutuelleServiceRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestMutuelleServiceRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientRequestAdultIllnessService");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestAdultIllnessServiceRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestAdultIllnessServiceRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientRequestAccountingOfficeService");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestAccountingOfficeServiceRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestAccountingOfficeServiceRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientRequestChildIllnessService");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestChildIllnessServiceRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestChildIllnessServiceRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientRequestInfectiousDiseasesService");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestInfectiousDiseasesServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestInfectiousDiseasesServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientRequestSocialWorkerService");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestSocialWorkerServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestSocialWorkerServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestPMTCTService");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestPMTCTServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestPMTCTServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestLabService");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestLabServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestLabServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientRequestPharmacyService");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestPharmacyServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestPharmacyServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientRequestMaternityService");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestMaternityServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestMaternityServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientRequestHospitalizationService");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestHospitalizationServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestHospitalizationServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"patientRequestVaccinationService");
		h.purgeDefinition(CohortDefinition.class,
				"femalePatientsrequestVaccinationServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class,
				"malePatientsrequestVaccinationServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "males");
		h.purgeDefinition(CohortDefinition.class, "females");
		h.purgeDefinition(CohortDefinition.class, "zeroToOne");
		h.purgeDefinition(CohortDefinition.class, "oneToTwo");
		h.purgeDefinition(CohortDefinition.class, "twoToThree");
		h.purgeDefinition(CohortDefinition.class, "threeToFour");
		h.purgeDefinition(CohortDefinition.class, "fourToFive");
		h.purgeDefinition(CohortDefinition.class, "fiveToFifteen");
		h.purgeDefinition(CohortDefinition.class, "fifteenAndPlus");
		h.purgeDefinition(CohortDefinition.class,
				"maleWithRegistrationAndAgeZeroToOne");
		h.purgeDefinition(CohortDefinition.class,
				"maleWithRegistrationAndAgeOneToTwo");
		h.purgeDefinition(CohortDefinition.class,
				"maleWithRegistrationAndAgeTwoToThree");
		h.purgeDefinition(CohortDefinition.class,
				"maleWithRegistrationAndAgeThreeToFour");
		h.purgeDefinition(CohortDefinition.class,
				"maleWithRegistrationAndAgeFourToFive");
		h.purgeDefinition(CohortDefinition.class,
				"maleWithRegistrationAndAgeFiveToFifteen");
		h.purgeDefinition(CohortDefinition.class,
				"maleWithRegistrationAndAgeFifteenAndPlus");
		h.purgeDefinition(CohortDefinition.class,
				"femaleWithRegistrationAndAgeZeroToOne");
		h.purgeDefinition(CohortDefinition.class,
				"femaleWithRegistrationAndAgeOneToTwo");
		h.purgeDefinition(CohortDefinition.class,
				"femaleWithRegistrationAndAgeTwoToThree");
		h.purgeDefinition(CohortDefinition.class,
				"femaleWithRegistrationAndAgeThreeToFour");
		h.purgeDefinition(CohortDefinition.class,
				"femaleWithRegistrationAndAgeFourToFive");
		h.purgeDefinition(CohortDefinition.class,
				"femaleWithRegistrationAndAgeFiveToFifteen");
		h.purgeDefinition(CohortDefinition.class,
				"femaleWithRegistrationAndAgeFifteenAndPlus");
		h.purgeDefinition(CohortDefinition.class, "MUTUELLEInsCohortDef");
		h.purgeDefinition(CohortDefinition.class, "RAMAInsCohortDef");
		h.purgeDefinition(CohortDefinition.class, "MMIInsCohortDef");
		h.purgeDefinition(CohortDefinition.class, "MEDIPLANInsCohortDef");
		h.purgeDefinition(CohortDefinition.class, "CORARInsCohortDef");
		h.purgeDefinition(CohortDefinition.class, "NONEInsCohortDef");
		h.purgeDefinition(CohortDefinition.class, "patientsMissingIns");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMUTUELLEIns");
		h.purgeDefinition(CohortDefinition.class, "patientsWithRAMAIns");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMMIIns");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMEDIPLANIns");
		h.purgeDefinition(CohortDefinition.class, "patientsWithCORARIns");
		h.purgeDefinition(CohortDefinition.class, "patientsWithNONEIns");
		h.purgeDefinition(CohortDefinition.class, "patientsWithOneVisit");
		h.purgeDefinition(CohortDefinition.class, "patientsWithTwoVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMUTUELLEInsAndOneVisit");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithRAMAInsAndOneVisit");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMMIInsAndOneVisit");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMEDIPLANInsAndOneVisit");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithCORARInsAndOneVisit");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithNONEInsAndOneVisit");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMissingInsAndOneVisit");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMUTUELLEInsAndTwoVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithRAMAInsAndTwoVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMMIInsAndTwoVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMEDIPLANInsAndTwoVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithCORARInsAndTwoVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithNONEInsAndTwoVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMissingInsAndTwoVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMUTUELLEInsAndThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithRAMAInsAndThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMMIInsAndThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMEDIPLANInsAndThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithCORARInsAndThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithNONEInsAndThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMissingInsAndThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMUTUELLEInsAndGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithRAMAInsAndGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMMIInsAndGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMEDIPLANInsAndGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithCORARInsAndGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithNONEInsAndGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class,
				"patientsWithMissingInsAndGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "peakHours");
		h.purgeDefinition(CohortDefinition.class, "peakDays");
		h.purgeDefinition(CohortDefinition.class, "peakHoursAndPeakDays");
		h.purgeDefinition(CohortDefinition.class, "monday");
		h.purgeDefinition(CohortDefinition.class, "tuesday");
		h.purgeDefinition(CohortDefinition.class, "wednesday");
		h.purgeDefinition(CohortDefinition.class, "thursday");
		h.purgeDefinition(CohortDefinition.class, "friday");
		h.purgeDefinition(CohortDefinition.class, "saturday");
		h.purgeDefinition(CohortDefinition.class, "sunday");

		h.purgeDefinition(CohortIndicator.class,
				"patientsWithoutTemperatureInVitalsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithTemperatureGreaterThanNormalInVitalsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestPrimCareInRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestPrimCareInRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestVCTProgramInRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestVCTProgramInRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestAntenatalClinicInRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestAntenatalClinicInRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestMutuelleServiceRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestMutuelleServiceRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestAdultIllnessServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestAdultIllnessServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestChildIllnessServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestChildIllnessServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestInfectiousDiseasesServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestInfectiousDiseasesServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestSocialWorkerServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestSocialWorkerServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestPMTCTServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestPMTCTServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestLabServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestLabServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestPharmacyServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestPharmacyServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestMaternityServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestMaternityServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestHospitalizationServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestHospitalizationServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femalePatientsrequestVaccinationServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"malePatientsrequestVaccinationServiceIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"maleWithRegistrationAndAgeZeroToOneIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"maleWithRegistrationAndAgeOneToTwoIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"maleWithRegistrationAndAgeTwoToThreeIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"maleWithRegistrationAndAgeThreeToFourIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"maleWithRegistrationAndAgeFourToFiveIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"maleWithRegistrationAndAgeFiveToFifteenIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"maleWithRegistrationAndAgeFifteenAndPlusIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femaleWithRegistrationAndAgeZeroToOneIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femaleWithRegistrationAndAgeOneToTwoIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femaleWithRegistrationAndAgeTwoToThreeIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femaleWithRegistrationAndAgeThreeToFourIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femaleWithRegistrationAndAgeFourToFiveIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femaleWithRegistrationAndAgeFiveToFifteenIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"femaleWithRegistrationAndAgeFifteenAndPlusIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"percentOfPatientsMissingInsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"numberOfPatientsMissingInsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"percentOfPatientsWithMUTUELLEInsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"percentOfPatientsWithRAMAInsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"percentOfPatientsWithMMIInsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"percentOfPatientsWithMEDIPLANInsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"percentOfPatientsWithCORARInsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"percentOfPatientsWithNONEInsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMUTUELLEInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithRAMAInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMMIInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMEDIPLANInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithCORARInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithNONEInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMissingInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMissingInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMUTUELLEInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithRAMAInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMMIInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMEDIPLANInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithCORARInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithNONEInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMissingInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMissingInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMUTUELLEInsAndThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithRAMAInsAndThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMMIInsAndThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMEDIPLANInsAndThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithCORARInsAndThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithNONEInsAndThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMissingInsAndThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMMIInsAndGreaterThanThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithCORARInsAndGreaterThanThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithNONEInsAndGreaterThanThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"patientsWithMissingInsAndGreaterThanThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class,
				"peakHoursAndPeakDaysIndicator");

		h.purgeDefinition(EncounterGroupDefinition.class, "peakHours");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientsrequestPrimCare");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientsrequestPrimCare");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestVCTProgram");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestVCTProgram");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestAntenatalClinic");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestAntenatalClinic");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malepatientRequestFamilyPlaningServices");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalepatientRequestFamilyPlaningServices");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestMutuelleService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestMutuelleService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestAccountingOfficeService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestAccountingOfficeService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestAdultIllnessService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestChildIllnessService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestInfectiousDiseasesService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestSocialWorkerService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestAdultIllnessService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestChildIllnessService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestInfectiousDiseasesService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestSocialWorkerService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestPMTCTService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestLabService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestPharmacyService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestMaternityService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestHospitalizationService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"femalePatientRequestVaccinationService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestPMTCTService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestLabService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestPharmacyService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestMaternityService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestHospitalizationService");
		h.purgeDefinition(EncounterGroupDefinition.class,
				"malePatientRequestVaccinationService");

		h.purgeDefinition(EncounterIndicator.class,
				"peakHoursAndPeakDaysIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestPrimCareInRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestPrimCareInRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestVCTProgramInRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestVCTProgramInRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestAntenatalClinicInRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestAntenatalClinicInRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestMutuelleServiceRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestMutuelleServiceRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestAdultIllnessServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestAdultIllnessServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestChildIllnessServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestChildIllnessServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestInfectiousDiseasesServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestInfectiousDiseasesServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestSocialWorkerServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestSocialWorkerServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestPMTCTServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestPMTCTServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestLabServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestLabServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestPharmacyServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestPharmacyServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestMaternityServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestMaternityServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestHospitalizationServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestHospitalizationServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"femalePatientsrequestVaccinationServiceIndicator");
		h.purgeDefinition(EncounterIndicator.class,
				"malePatientsrequestVaccinationServiceIndicator");

	}

	private ReportDefinition createReportDefinition(EncounterType reg,
			EncounterType vitals) {

		// PIH Quarterly Cross Site Indicator Report

		int vitalsEncTypeId = vitals.getEncounterTypeId();
		int registrationEncTypeId = reg.getEncounterTypeId();

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

		List<EncounterType> registrationEncounterType = new ArrayList<EncounterType>();
		registrationEncounterType.add(reg);
		EncounterCohortDefinition patientsWithPrimaryCareRegistration = new EncounterCohortDefinition();
		patientsWithPrimaryCareRegistration
				.setName("patientsWithPrimaryCareRegistration");
		patientsWithPrimaryCareRegistration.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithPrimaryCareRegistration.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithPrimaryCareRegistration
				.setEncounterTypeList(registrationEncounterType);
		h.replaceCohortDefinition(patientsWithPrimaryCareRegistration);

		List<EncounterType> vitalsEncounterType = new ArrayList<EncounterType>();
		vitalsEncounterType.add(vitals);
		EncounterCohortDefinition patientsWithPrimaryCareVitals = new EncounterCohortDefinition();
		patientsWithPrimaryCareVitals.setName("patientsWithPrimaryCareVitals");
		patientsWithPrimaryCareVitals.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientsWithPrimaryCareVitals.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		patientsWithPrimaryCareVitals.setEncounterTypeList(vitalsEncounterType);
		h.replaceCohortDefinition(patientsWithPrimaryCareVitals);

		rd.setBaseRollingQueryExtension(" and  e.encounter_type="
				+ registrationEncTypeId);

		rd.setupDataSetDefinition();
		// ======================================================================================
		// 1st Question
		// ======================================================================================

		// ======================================================================================
		// 2nd Question
		// ======================================================================================

		// 2.1 Percent of patients who do not have an observation for
		// temperature in the vitals

		SqlEncounterGroupDefinition patientsUnder5WithoutTemperatureInVitals = new SqlEncounterGroupDefinition();
		patientsUnder5WithoutTemperatureInVitals
				.setName("patientsWithTemperatureInVitals");
		patientsUnder5WithoutTemperatureInVitals
				.setQuery("select distinct encounterQuery.encounter_id,  encounterQuery.patient_id from (select e.encounter_id, e.patient_id from encounter e, person p, patient pa where e.encounter_id not in (select e.encounter_id from encounter e, obs o where e.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and e.encounter_id = o.encounter_id and o.voided = 0 and o.concept_id = "
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.TEMPERATURE)
						+ " and e.encounter_type = "
						+ vitalsEncTypeId
						+ ") and e.voided = 0 and encounter_type = "
						+ vitalsEncTypeId
						+ " and e.patient_id = p.person_id and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5	and p.voided = 0 and p.person_id = pa.patient_id and pa.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate UNION select e.encounter_id, e.patient_id from encounter e left join (select encounter_id, patient_id, encounter_datetime from encounter e where encounter_type = "
						+ vitalsEncTypeId
						+ " and voided = 0 and encounter_datetime > :startDate and encounter_datetime <= :endDate) eTmp on eTmp.patient_id = e.patient_id, person p, patient pa where eTmp.encounter_id is null and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and e.patient_id = p.person_id and p.voided = 0 and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5 and p.person_id = pa.patient_id and pa.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate ) encounterQuery");
		patientsUnder5WithoutTemperatureInVitals.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsUnder5WithoutTemperatureInVitals.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(patientsUnder5WithoutTemperatureInVitals);

		/*AgeCohortDefinition lessThanFive = new AgeCohortDefinition(null, 5,
				null);
		lessThanFive.setName("lessThanFive");
		h.replaceCohortDefinition(lessThanFive);

		SqlCohortDefinition patientsWithTemperatureInVitals = new SqlCohortDefinition(
				"select e.patient_id from obs o,encounter e where o.encounter_id=e.encounter_id and o.person_id=e.patient_id and e.encounter_type="
						+ vitalsEncTypeId
						+ " and o.concept_id="
						+ PrimaryCareReportConstants.TEMPERATURE_ID
						+ " and o.voided=0 and e.voided=0 ");
		patientsWithTemperatureInVitals
				.setName("patientsWithTemperatureInVitals");
		h.replaceCohortDefinition(patientsWithTemperatureInVitals);*/
		/*
		 * 
		 * CompositionCohortDefinition
		 * patientsUnder5WithoutTemperatureInVitals=new
		 * CompositionCohortDefinition();
		 * patientsUnder5WithoutTemperatureInVitals
		 * .setName("patientsUnder5WithoutTemperatureInVitals");
		 * patientsUnder5WithoutTemperatureInVitals.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientsUnder5WithoutTemperatureInVitals.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * patientsUnder5WithoutTemperatureInVitals.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * patientsUnder5WithoutTemperatureInVitals.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * patientsUnder5WithoutTemperatureInVitals
		 * .getSearches().put("patientsWithTemperatureInVitals", new
		 * Mapped<CohortDefinition>(patientsWithTemperatureInVitals,null));
		 * patientsUnder5WithoutTemperatureInVitals
		 * .getSearches().put("patientsWithPrimaryCareVitals", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareVitals,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * patientsUnder5WithoutTemperatureInVitals
		 * .getSearches().put("lessThanFive", new
		 * Mapped<CohortDefinition>(lessThanFive,null));
		 * patientsUnder5WithoutTemperatureInVitals.setCompositionString(
		 * "patientsWithPrimaryCareVitals AND (NOT patientsWithTemperatureInVitals) AND lessThanFive"
		 * );
		 * h.replaceCohortDefinition(patientsUnder5WithoutTemperatureInVitals);
		 */

		SqlEncounterGroupDefinition patientsUnder5InRegistration = new SqlEncounterGroupDefinition();
		patientsUnder5InRegistration.setName("patientsUnder5InRegistration");

		patientsUnder5InRegistration
				.setQuery("select e.encounter_id, e.patient_id from encounter e, person p, patient pa where e.voided = 0 and e.encounter_type = "+registrationEncTypeId+" and e.patient_id = p.person_id and p.voided = 0 and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5 and e.patient_id = pa.patient_id and pa.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate");

		patientsUnder5InRegistration.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsUnder5InRegistration.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceEncounterGroupDefinition(patientsUnder5InRegistration);

		/*
		 * CompositionCohortDefinition patientsUnder5InRegistration=new
		 * CompositionCohortDefinition();
		 * patientsUnder5InRegistration.setName("patientsUnder5InRegistration");
		 * patientsUnder5InRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientsUnder5InRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * patientsUnder5InRegistration.addParameter(new Parameter("startDate",
		 * "startDate", Date.class));
		 * patientsUnder5InRegistration.addParameter(new Parameter("endDate",
		 * "endDate", Date.class));
		 * patientsUnder5InRegistration.getSearches().put
		 * ("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration
		 * ,ParameterizableUtil.createParameterMappings(
		 * "onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * patientsUnder5InRegistration.getSearches().put("lessThanFive", new
		 * Mapped<CohortDefinition>(lessThanFive,null));
		 * patientsUnder5InRegistration.setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND lessThanFive");
		 * h.replaceCohortDefinition(patientsUnder5InRegistration);
		 */

		/*
		 * CohortIndicator patientsWithoutTemperatureInVitalsIndicator =
		 * CohortIndicator.newFractionIndicator (null,new
		 * Mapped<CohortDefinition>(patientsUnder5WithoutTemperatureInVitals,
		 * ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )), new Mapped<CohortDefinition>(patientsUnder5InRegistration,
		 * ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )), null); patientsWithoutTemperatureInVitalsIndicator.setName(
		 * "patientsWithoutTemperatureInVitalsIndicator");
		 * patientsWithoutTemperatureInVitalsIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * patientsWithoutTemperatureInVitalsIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * h.replaceDefinition(patientsWithoutTemperatureInVitalsIndicator);
		 */
	/*	EncounterIndicator patientsWithoutTemperatureInVitalsIndicator = new EncounterIndicator();
		patientsWithoutTemperatureInVitalsIndicator
				.setName("patientsWithoutTemperatureInVitalsIndicator");
		patientsWithoutTemperatureInVitalsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithoutTemperatureInVitalsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithoutTemperatureInVitalsIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						patientsUnder5InRegistration,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithoutTemperatureInVitalsIndicator);
*/
		
		 EncounterIndicator patientsWithoutTemperatureInVitalsIndicator = EncounterIndicator.newFractionIndicator
			 ("patientsWithoutTemperatureInVitalsIndicator",new
			 Mapped<SqlEncounterGroupDefinition
			 >(patientsUnder5WithoutTemperatureInVitals,
			 ParameterizableUtil.createParameterMappings
			 ("startDate=${startDate},endDate=${endDate}")), new
			 Mapped<SqlEncounterGroupDefinition>(patientsUnder5InRegistration,
			 ParameterizableUtil
			 .createParameterMappings("startDate=${startDate},endDate=${endDate}"
			 )), null);
		 patientsWithoutTemperatureInVitalsIndicator.addParameter(new
		 Parameter("startDate", "startDate", Date.class));
		 patientsWithoutTemperatureInVitalsIndicator.addParameter(new
		 Parameter("endDate", "endDate", Date.class));
		 h.replaceDefinition(patientsWithoutTemperatureInVitalsIndicator);
		 

		// 2.2 Percent of children under 5 who did have observation for
		// temperature, and actually had a fever (were sick, temperature was
		// higher than normal)

		/*SqlCohortDefinition patientsWithTemperatureGreaterThanNormalInVitals = new SqlCohortDefinition(
				"select e.patient_id from obs o,encounter e where o.encounter_id=e.encounter_id and o.person_id=e.patient_id and e.encounter_type="
						+ vitalsEncTypeId
						+ " and o.concept_id="
						+ PrimaryCareReportConstants.TEMPERATURE_ID
						+ " and o.value_numeric>37.0 and o.voided=0 and e.voided=0");
		patientsWithTemperatureGreaterThanNormalInVitals
				.setName("patientsWithTemperatureGreaterThanNormalInVitals");
		h.replaceCohortDefinition(patientsWithTemperatureGreaterThanNormalInVitals);

		CompositionCohortDefinition patientsUnder5WithTemperatureGreaterThanNormalInVitals = new CompositionCohortDefinition();
		patientsUnder5WithTemperatureGreaterThanNormalInVitals
				.setName("patientsUnder5WithTemperatureGreaterThanNormalInVitals");
		patientsUnder5WithTemperatureGreaterThanNormalInVitals
				.addParameter(new Parameter("onOrAfter", "onOrAfter",
						Date.class));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals
				.addParameter(new Parameter("onOrBefore", "onOrBefore",
						Date.class));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals
				.getSearches()
				.put("patientsWithTemperatureGreaterThanNormalInVitals",
						new Mapped<CohortDefinition>(
								patientsWithTemperatureGreaterThanNormalInVitals,
								null));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals
				.getSearches()
				.put("patientsWithPrimaryCareVitals",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareVitals,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.getSearches()
				.put("lessThanFive",
						new Mapped<CohortDefinition>(lessThanFive, null));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals
				.setCompositionString("patientsWithPrimaryCareVitals AND patientsWithTemperatureGreaterThanNormalInVitals AND lessThanFive");
		h.replaceCohortDefinition(patientsUnder5WithTemperatureGreaterThanNormalInVitals);

		CompositionCohortDefinition patientsUnder5WithTemperatureInVitals = new CompositionCohortDefinition();
		patientsUnder5WithTemperatureInVitals
				.setName("patientsUnder5WithTemperatureInVitals");
		patientsUnder5WithTemperatureInVitals.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsUnder5WithTemperatureInVitals.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsUnder5WithTemperatureInVitals.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsUnder5WithTemperatureInVitals.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsUnder5WithTemperatureInVitals.getSearches().put(
				"patientsWithTemperatureInVitals",
				new Mapped<CohortDefinition>(patientsWithTemperatureInVitals,
						null));
		patientsUnder5WithTemperatureInVitals
				.getSearches()
				.put("patientsWithPrimaryCareVitals",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareVitals,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsUnder5WithTemperatureInVitals.getSearches().put("lessThanFive",
				new Mapped<CohortDefinition>(lessThanFive, null));
		patientsUnder5WithTemperatureInVitals
				.setCompositionString("patientsWithPrimaryCareVitals AND patientsWithTemperatureInVitals AND lessThanFive");
		h.replaceCohortDefinition(patientsUnder5WithTemperatureInVitals);

		CohortIndicator patientsWithTemperatureGreaterThanNormalInVitalsIndicator = CohortIndicator
				.newFractionIndicator(
						null,
						new Mapped<CohortDefinition>(
								patientsUnder5WithTemperatureGreaterThanNormalInVitals,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						new Mapped<CohortDefinition>(
								patientsUnder5WithTemperatureInVitals,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						null);
		patientsWithTemperatureGreaterThanNormalInVitalsIndicator
				.setName("patientsWithTemperatureGreaterThanNormalInVitalsIndicator");
		patientsWithTemperatureGreaterThanNormalInVitalsIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithTemperatureGreaterThanNormalInVitalsIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(patientsWithTemperatureGreaterThanNormalInVitalsIndicator);
		// 2.3 Percent of all registered patients under 5 who had a fever

		
		 * CohortIndicator
		 * allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator
		 * = CohortIndicator.newFractionIndicator (null,new
		 * Mapped<CohortDefinition
		 * >(patientsUnder5WithTemperatureGreaterThanNormalInVitals,
		 * ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )), new Mapped<CohortDefinition>(patientsUnder5InRegistration,
		 * ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )), null);
		 * allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator
		 * .setName(
		 * "allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator"
		 * );
		 * allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * h.replaceDefinition
		 * (allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator
		 * );
		 */
		// ========================================================================
		// 3. Registration Speed during Peak Hours
		// ========================================================================

		// Peak Hours (08:00:00 to 10:00:00)
		//
		// SqlCohortDefinition peakHours=new SqlCohortDefinition();
		// peakHours.setName("peakHours");
		// peakHours.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,TIME(e.encounter_datetime) as peakhours,WEEKDAY(e.encounter_datetime) as peakdays FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0) as patientspeakhours where peakhours>= :startTime and peakhours<= :endTime and peakdays<=4 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		// peakHours.addParameter(new Parameter("startDate", "startDate",
		// Date.class));
		// peakHours.addParameter(new Parameter("endDate", "endDate",
		// Date.class));
		// peakHours.addParameter(new Parameter("startTime", "startTime",
		// Date.class));
		// peakHours.addParameter(new Parameter("endTime", "endTime",
		// Date.class));
		// h.replaceCohortDefinition(peakHours);
		//
		// // Peak Days (Monday to Friday)
		//
		// SqlCohortDefinition peakDays=new SqlCohortDefinition();
		// peakDays.setName("peakDays");
		// peakDays.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,WEEKDAY(e.encounter_datetime) as peakdays FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0) as patientspeakdays where peakdays<=4 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		// peakDays.addParameter(new Parameter("startDate", "startDate",
		// Date.class));
		// peakDays.addParameter(new Parameter("endDate", "endDate",
		// Date.class));
		// h.replaceCohortDefinition(peakDays);
		//
		// //
		// CompositionCohortDefinition peakHoursAndPeakDays=new
		// CompositionCohortDefinition();
		// peakHoursAndPeakDays.setName("peakHoursAndPeakDays");
		// peakHoursAndPeakDays.addParameter(new Parameter("startDate",
		// "startDate", Date.class));
		// peakHoursAndPeakDays.addParameter(new Parameter("endDate", "endDate",
		// Date.class));
		// peakHoursAndPeakDays.addParameter(new Parameter("startTime",
		// "startTime", Date.class));
		// peakHoursAndPeakDays.addParameter(new Parameter("endTime", "endTime",
		// Date.class));
		// peakHoursAndPeakDays.getSearches().put("peakHours", new
		// Mapped<CohortDefinition>(peakHours,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},startTime=08:00:00,endTime=10:00:00")));
		// peakHoursAndPeakDays.getSearches().put("peakDays", new
		// Mapped<CohortDefinition>(peakDays,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		// peakHoursAndPeakDays.setCompositionString("peakHours AND peakDays");
		// h.replaceCohortDefinition(peakHoursAndPeakDays);
		//
		//
		// CohortIndicator peakHoursAndPeakDaysIndicator =
		// CohortIndicator.newFractionIndicator
		// (null,new Mapped<CohortDefinition>(peakHoursAndPeakDays,
		// ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},startTime=08:00:00,endTime=10:00:00")),
		// new Mapped<CohortDefinition>(peakDays,
		// ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")),
		// null);
		// peakHoursAndPeakDaysIndicator.setName("peakHoursAndPeakDaysIndicator");
		// peakHoursAndPeakDaysIndicator.addParameter(new Parameter("startDate",
		// "startDate", Date.class));
		// peakHoursAndPeakDaysIndicator.addParameter(new Parameter("endDate",
		// "endDate", Date.class));
		// h.replaceDefinition(peakHoursAndPeakDaysIndicator);

		// 8 to 10, monday to friday
		SqlEncounterGroupDefinition peakHours = new SqlEncounterGroupDefinition();
		peakHours.setName("peakHours");
		peakHours
				.setQuery("select distinct encounter_id, patient_id from encounter where TIME(encounter_datetime) >= :startTime and TIME(encounter_datetime) <= :endTime and WEEKDAY(encounter_datetime) <=4  and encounter_datetime>= :startDate and encounter_datetime<= :endDate and encounter_type = "
						+ registrationEncTypeId
						+ " and voided = 0 and location_id = :location");
		peakHours.addParameter(new Parameter("startDate", "startDate",
				Date.class));
		peakHours.addParameter(new Parameter("endDate", "endDate", Date.class));
		peakHours.addParameter(new Parameter("startTime", "startTime",
				Date.class));
		peakHours.addParameter(new Parameter("endTime", "endTime", Date.class));
		h.replaceEncounterGroupDefinition(peakHours);

		// number of weekdays between startDate and stopDate / 2

		EncounterIndicator peakHoursAndPeakDaysIndicator = EncounterIndicator
				.newDailyDivisionIndicator(
						"peakHoursIndicator",
						new Mapped<EncounterGroupDefinition>(
								peakHours,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate},startTime=08:00:00,endTime=10:00:00")),
						Integer.valueOf(2),
						EncounterIndicator.IndicatorType.PER_WEEKDAYS, null);
		peakHoursAndPeakDaysIndicator.setName("peakHoursAndPeakDaysIndicator");
		peakHoursAndPeakDaysIndicator.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		peakHoursAndPeakDaysIndicator.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		peakHoursAndPeakDaysIndicator.setPerHourDenominator(2);
		h.replaceDefinition(peakHoursAndPeakDaysIndicator);

		// ========================================================================
		// 4. How many registration encounters are paid for by Medical Insurance
		// ========================================================================

		// Mutuelle Insurance cohort definition

		CodedObsCohortDefinition MUTUELLEInsCohortDef = makeCodedObsCohortDefinition(
				PrimaryCareReportConstants.RWANDA_INSURANCE_TYPE,
				PrimaryCareReportConstants.MUTUELLE, SetComparator.IN,
				TimeModifier.ANY);
		MUTUELLEInsCohortDef.setName("MUTUELLEInsCohortDef");
		MUTUELLEInsCohortDef.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		MUTUELLEInsCohortDef.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		h.replaceCohortDefinition(MUTUELLEInsCohortDef);

		CodedObsCohortDefinition RAMAInsCohortDef = makeCodedObsCohortDefinition(
				PrimaryCareReportConstants.RWANDA_INSURANCE_TYPE,
				PrimaryCareReportConstants.RAMA, SetComparator.IN,
				TimeModifier.ANY);
		RAMAInsCohortDef.setName("RAMAInsCohortDef");
		RAMAInsCohortDef.addParameter(new Parameter("onOrAfter", "onOrAfter",
				Date.class));
		RAMAInsCohortDef.addParameter(new Parameter("onOrBefore", "onOrBefore",
				Date.class));
		h.replaceCohortDefinition(RAMAInsCohortDef);

		CodedObsCohortDefinition MMIInsCohortDef = makeCodedObsCohortDefinition(
				PrimaryCareReportConstants.RWANDA_INSURANCE_TYPE,
				PrimaryCareReportConstants.MMI, SetComparator.IN,
				TimeModifier.ANY);
		MMIInsCohortDef.setName("MMIInsCohortDef");
		MMIInsCohortDef.addParameter(new Parameter("onOrAfter", "onOrAfter",
				Date.class));
		MMIInsCohortDef.addParameter(new Parameter("onOrBefore", "onOrBefore",
				Date.class));
		h.replaceCohortDefinition(MMIInsCohortDef);

		CodedObsCohortDefinition MEDIPLANInsCohortDef = makeCodedObsCohortDefinition(
				PrimaryCareReportConstants.RWANDA_INSURANCE_TYPE,
				PrimaryCareReportConstants.MEDIPLAN, SetComparator.IN,
				TimeModifier.ANY);
		MEDIPLANInsCohortDef.setName("MEDIPLANInsCohortDef");
		MEDIPLANInsCohortDef.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		MEDIPLANInsCohortDef.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		h.replaceCohortDefinition(MEDIPLANInsCohortDef);

		CodedObsCohortDefinition CORARInsCohortDef = makeCodedObsCohortDefinition(
				PrimaryCareReportConstants.RWANDA_INSURANCE_TYPE,
				PrimaryCareReportConstants.CORAR, SetComparator.IN,
				TimeModifier.ANY);
		CORARInsCohortDef.setName("CORARInsCohortDef");
		CORARInsCohortDef.addParameter(new Parameter("onOrAfter", "onOrAfter",
				Date.class));
		CORARInsCohortDef.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		h.replaceCohortDefinition(CORARInsCohortDef);

		CodedObsCohortDefinition NONEInsCohortDef = makeCodedObsCohortDefinition(
				PrimaryCareReportConstants.RWANDA_INSURANCE_TYPE,
				PrimaryCareReportConstants.NONE, SetComparator.IN,
				TimeModifier.ANY);
		NONEInsCohortDef.setName("NONEInsCohortDef");
		NONEInsCohortDef.addParameter(new Parameter("onOrAfter", "onOrAfter",
				Date.class));
		NONEInsCohortDef.addParameter(new Parameter("onOrBefore", "onOrBefore",
				Date.class));
		h.replaceCohortDefinition(NONEInsCohortDef);

		// 4.1 Percent of patients who are missing an insurance type in
		// registration encounter

		CompositionCohortDefinition patientsMissingIns = new CompositionCohortDefinition();
		patientsMissingIns.setName("patientsMissingIns");
		patientsMissingIns.addParameter(new Parameter("onOrAfter", "onOrAfter",
				Date.class));
		patientsMissingIns.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		patientsMissingIns.addParameter(new Parameter("startDate", "startDate",
				Date.class));
		patientsMissingIns.addParameter(new Parameter("endDate", "endDate",
				Date.class));
		patientsMissingIns
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns
				.getSearches()
				.put("MUTUELLEInsCohortDef",
						new Mapped<CohortDefinition>(
								MUTUELLEInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns
				.getSearches()
				.put("RAMAInsCohortDef",
						new Mapped<CohortDefinition>(
								RAMAInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns
				.getSearches()
				.put("MMIInsCohortDef",
						new Mapped<CohortDefinition>(
								MMIInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns
				.getSearches()
				.put("MEDIPLANInsCohortDef",
						new Mapped<CohortDefinition>(
								MEDIPLANInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns
				.getSearches()
				.put("CORARInsCohortDef",
						new Mapped<CohortDefinition>(
								CORARInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns
				.getSearches()
				.put("NONEInsCohortDef",
						new Mapped<CohortDefinition>(
								NONEInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns
				.setCompositionString("patientsWithPrimaryCareRegistration AND (NOT(MUTUELLEInsCohortDef OR RAMAInsCohortDef OR MMIInsCohortDef OR MEDIPLANInsCohortDef OR CORARInsCohortDef OR NONEInsCohortDef))");
		h.replaceCohortDefinition(patientsMissingIns);

		CohortIndicator percentOfPatientsMissingInsIndicator = CohortIndicator
				.newFractionIndicator(
						null,
						new Mapped<CohortDefinition>(
								patientsMissingIns,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						null);
		percentOfPatientsMissingInsIndicator
				.setName("percentOfPatientsMissingInsIndicator");
		percentOfPatientsMissingInsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		percentOfPatientsMissingInsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsMissingInsIndicator);

		// 4.2 Number of patients who are missing an insurance type in
		// registration encounter
		CohortIndicator numberOfPatientsMissingInsIndicator = new CohortIndicator();
		numberOfPatientsMissingInsIndicator
				.setName("numberOfPatientsMissingInsIndicator");
		numberOfPatientsMissingInsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		numberOfPatientsMissingInsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		numberOfPatientsMissingInsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsMissingIns,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(numberOfPatientsMissingInsIndicator);

		// 4.3.1 Percent of patients with MUTUELLE insurance in registration
		// encounter

		CompositionCohortDefinition patientsWithMUTUELLEIns = new CompositionCohortDefinition();
		patientsWithMUTUELLEIns.setName("patientsWithMUTUELLEIns");
		patientsWithMUTUELLEIns.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientsWithMUTUELLEIns.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		patientsWithMUTUELLEIns.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithMUTUELLEIns.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithMUTUELLEIns
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEIns
				.getSearches()
				.put("MUTUELLEInsCohortDef",
						new Mapped<CohortDefinition>(
								MUTUELLEInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEIns
				.setCompositionString("patientsWithPrimaryCareRegistration AND MUTUELLEInsCohortDef");
		h.replaceCohortDefinition(patientsWithMUTUELLEIns);

		CohortIndicator percentOfPatientsWithMUTUELLEInsIndicator = CohortIndicator
				.newFractionIndicator(
						null,
						new Mapped<CohortDefinition>(
								patientsWithMUTUELLEIns,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						null);
		percentOfPatientsWithMUTUELLEInsIndicator
				.setName("percentOfPatientsWithMUTUELLEInsIndicator");
		percentOfPatientsWithMUTUELLEInsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		percentOfPatientsWithMUTUELLEInsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsWithMUTUELLEInsIndicator);

		// 4.3.2 Percent of patients with RAMA insurance in registration
		// encounter

		CompositionCohortDefinition patientsWithRAMAIns = new CompositionCohortDefinition();
		patientsWithRAMAIns.setName("patientsWithRAMAIns");
		patientsWithRAMAIns.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientsWithRAMAIns.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		patientsWithRAMAIns.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithRAMAIns.addParameter(new Parameter("endDate", "endDate",
				Date.class));
		patientsWithRAMAIns
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAIns
				.getSearches()
				.put("RAMAInsCohortDef",
						new Mapped<CohortDefinition>(
								RAMAInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAIns
				.setCompositionString("patientsWithPrimaryCareRegistration AND RAMAInsCohortDef");
		h.replaceCohortDefinition(patientsWithRAMAIns);

		CohortIndicator percentOfPatientsWithRAMAInsIndicator = CohortIndicator
				.newFractionIndicator(
						null,
						new Mapped<CohortDefinition>(
								patientsWithRAMAIns,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						null);
		percentOfPatientsWithRAMAInsIndicator
				.setName("percentOfPatientsWithRAMAInsIndicator");
		percentOfPatientsWithRAMAInsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		percentOfPatientsWithRAMAInsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsWithRAMAInsIndicator);

		// 4.3.3 Percent of patients with MMI insurance in registration
		// encounter

		CompositionCohortDefinition patientsWithMMIIns = new CompositionCohortDefinition();
		patientsWithMMIIns.setName("patientsWithMMIIns");
		patientsWithMMIIns.addParameter(new Parameter("onOrAfter", "onOrAfter",
				Date.class));
		patientsWithMMIIns.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		patientsWithMMIIns.addParameter(new Parameter("startDate", "startDate",
				Date.class));
		patientsWithMMIIns.addParameter(new Parameter("endDate", "endDate",
				Date.class));
		patientsWithMMIIns
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIIns
				.getSearches()
				.put("MMIInsCohortDef",
						new Mapped<CohortDefinition>(
								MMIInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIIns
				.setCompositionString("patientsWithPrimaryCareRegistration AND MMIInsCohortDef");
		h.replaceCohortDefinition(patientsWithMMIIns);

		CohortIndicator percentOfPatientsWithMMIInsIndicator = CohortIndicator
				.newFractionIndicator(
						null,
						new Mapped<CohortDefinition>(
								patientsWithMMIIns,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						null);
		percentOfPatientsWithMMIInsIndicator
				.setName("percentOfPatientsWithMMIInsIndicator");
		percentOfPatientsWithMMIInsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		percentOfPatientsWithMMIInsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsWithMMIInsIndicator);

		// 4.3.4 Percent of patients with MEDIPLAN insurance in registration
		// encounter

		CompositionCohortDefinition patientsWithMEDIPLANIns = new CompositionCohortDefinition();
		patientsWithMEDIPLANIns.setName("patientsWithMEDIPLANIns");
		patientsWithMEDIPLANIns.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientsWithMEDIPLANIns.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		patientsWithMEDIPLANIns.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithMEDIPLANIns.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithMEDIPLANIns
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANIns
				.getSearches()
				.put("MEDIPLANInsCohortDef",
						new Mapped<CohortDefinition>(
								MEDIPLANInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANIns
				.setCompositionString("patientsWithPrimaryCareRegistration AND MEDIPLANInsCohortDef");
		h.replaceCohortDefinition(patientsWithMEDIPLANIns);

		CohortIndicator percentOfPatientsWithMEDIPLANInsIndicator = CohortIndicator
				.newFractionIndicator(
						null,
						new Mapped<CohortDefinition>(
								patientsWithMEDIPLANIns,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						null);
		percentOfPatientsWithMEDIPLANInsIndicator
				.setName("percentOfPatientsWithMEDIPLANInsIndicator");
		percentOfPatientsWithMEDIPLANInsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		percentOfPatientsWithMEDIPLANInsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsWithMEDIPLANInsIndicator);

		// 4.3.5 Percent of patients with CORAR insurance in registration
		// encounter

		CompositionCohortDefinition patientsWithCORARIns = new CompositionCohortDefinition();
		patientsWithCORARIns.setName("patientsWithCORARIns");
		patientsWithCORARIns.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientsWithCORARIns.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		patientsWithCORARIns.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithCORARIns.addParameter(new Parameter("endDate", "endDate",
				Date.class));
		patientsWithCORARIns
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARIns
				.getSearches()
				.put("CORARInsCohortDef",
						new Mapped<CohortDefinition>(
								CORARInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARIns
				.setCompositionString("patientsWithPrimaryCareRegistration AND CORARInsCohortDef");
		h.replaceCohortDefinition(patientsWithCORARIns);

		CohortIndicator percentOfPatientsWithCORARInsIndicator = CohortIndicator
				.newFractionIndicator(
						null,
						new Mapped<CohortDefinition>(
								patientsWithCORARIns,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						null);
		percentOfPatientsWithCORARInsIndicator
				.setName("percentOfPatientsWithCORARInsIndicator");
		percentOfPatientsWithCORARInsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		percentOfPatientsWithCORARInsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsWithCORARInsIndicator);

		// 4.3.6 Percent of patients with CORAR insurance in registration
		// encounter

		CompositionCohortDefinition patientsWithNONEIns = new CompositionCohortDefinition();
		patientsWithNONEIns.setName("patientsWithNONEIns");
		patientsWithNONEIns.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientsWithNONEIns.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		patientsWithNONEIns.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithNONEIns.addParameter(new Parameter("endDate", "endDate",
				Date.class));
		patientsWithNONEIns
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEIns
				.getSearches()
				.put("NONEInsCohortDef",
						new Mapped<CohortDefinition>(
								NONEInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEIns
				.setCompositionString("patientsWithPrimaryCareRegistration AND NONEInsCohortDef");
		h.replaceCohortDefinition(patientsWithNONEIns);

		CohortIndicator percentOfPatientsWithNONEInsIndicator = CohortIndicator
				.newFractionIndicator(
						null,
						new Mapped<CohortDefinition>(
								patientsWithNONEIns,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")),
						null);
		percentOfPatientsWithNONEInsIndicator
				.setName("percentOfPatientsWithNONEInsIndicator");
		percentOfPatientsWithNONEInsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		percentOfPatientsWithNONEInsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsWithNONEInsIndicator);

		// ========================================================================
		// 5. For all insurance types, how many patients come back for multiple
		// visits, and how many visits:
		// ========================================================================

		SqlCohortDefinition patientsWithOneVisit = new SqlCohortDefinition();
		patientsWithOneVisit.setName("patientsWithOneVisit");
		patientsWithOneVisit
				.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type="
						+ registrationEncTypeId
						+ " and e.voided=0 group by e.patient_id) as patientregistrationtimes where timesofregistration=1 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		patientsWithOneVisit.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithOneVisit.addParameter(new Parameter("endDate", "endDate",
				Date.class));
		h.replaceCohortDefinition(patientsWithOneVisit);

		SqlCohortDefinition patientsWithTwoVisits = new SqlCohortDefinition();
		patientsWithTwoVisits.setName("patientsWithTwoVisits");
		patientsWithTwoVisits
				.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type="
						+ registrationEncTypeId
						+ " and e.voided=0 group by e.patient_id) as patientregistrationtimes where timesofregistration=2 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		patientsWithTwoVisits.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithTwoVisits.addParameter(new Parameter("endDate", "endDate",
				Date.class));
		h.replaceCohortDefinition(patientsWithTwoVisits);

		SqlCohortDefinition patientsWithThreeVisits = new SqlCohortDefinition();
		patientsWithThreeVisits.setName("patientsWithThreeVisits");
		patientsWithThreeVisits
				.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type="
						+ registrationEncTypeId
						+ " and e.voided=0 group by e.patient_id) as patientregistrationtimes where timesofregistration=3 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		patientsWithThreeVisits.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithThreeVisits.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceCohortDefinition(patientsWithThreeVisits);

		SqlCohortDefinition patientsWithGreaterThanThreeVisits = new SqlCohortDefinition();
		patientsWithGreaterThanThreeVisits
				.setName("patientsWithGreaterThanThreeVisits");
		patientsWithGreaterThanThreeVisits
				.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type="
						+ registrationEncTypeId
						+ " and e.voided=0 group by e.patient_id) as patientregistrationtimes where timesofregistration>3 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		patientsWithGreaterThanThreeVisits.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithGreaterThanThreeVisits.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceCohortDefinition(patientsWithGreaterThanThreeVisits);

		// 5.1.1 Patients with Mutuelle Insurance and 1 visit
		CompositionCohortDefinition patientsWithMUTUELLEInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithMUTUELLEInsAndOneVisit
				.setName("patientsWithMUTUELLEInsAndOneVisit");
		patientsWithMUTUELLEInsAndOneVisit.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithMUTUELLEInsAndOneVisit.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithMUTUELLEInsAndOneVisit.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndOneVisit.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndOneVisit
				.getSearches()
				.put("patientsWithOneVisit",
						new Mapped<CohortDefinition>(
								patientsWithOneVisit,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMUTUELLEInsAndOneVisit
				.getSearches()
				.put("MUTUELLEInsCohortDef",
						new Mapped<CohortDefinition>(
								MUTUELLEInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEInsAndOneVisit
				.setCompositionString("patientsWithOneVisit AND MUTUELLEInsCohortDef");
		h.replaceCohortDefinition(patientsWithMUTUELLEInsAndOneVisit);

		CohortIndicator patientsWithMUTUELLEInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithMUTUELLEInsAndOneVisitIndicator
				.setName("patientsWithMUTUELLEInsAndOneVisitIndicator");
		patientsWithMUTUELLEInsAndOneVisitIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndOneVisitIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndOneVisitIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMUTUELLEInsAndOneVisit,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMUTUELLEInsAndOneVisitIndicator);
		// 5.1.2 Patients with RAMA Insurance and 1 visit
		CompositionCohortDefinition patientsWithRAMAInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithRAMAInsAndOneVisit
				.setName("patientsWithRAMAInsAndOneVisit");
		patientsWithRAMAInsAndOneVisit.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientsWithRAMAInsAndOneVisit.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		patientsWithRAMAInsAndOneVisit.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithRAMAInsAndOneVisit.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithRAMAInsAndOneVisit
				.getSearches()
				.put("patientsWithOneVisit",
						new Mapped<CohortDefinition>(
								patientsWithOneVisit,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithRAMAInsAndOneVisit
				.getSearches()
				.put("RAMAInsCohortDef",
						new Mapped<CohortDefinition>(
								RAMAInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAInsAndOneVisit
				.setCompositionString("patientsWithOneVisit AND RAMAInsCohortDef");
		h.replaceCohortDefinition(patientsWithRAMAInsAndOneVisit);

		CohortIndicator patientsWithRAMAInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithRAMAInsAndOneVisitIndicator
				.setName("patientsWithRAMAInsAndOneVisitIndicator");
		patientsWithRAMAInsAndOneVisitIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithRAMAInsAndOneVisitIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithRAMAInsAndOneVisitIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithRAMAInsAndOneVisit,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithRAMAInsAndOneVisitIndicator);
		// 5.1.3 Patients with MMI Insurance and 1 visit
		CompositionCohortDefinition patientsWithMMIInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithMMIInsAndOneVisit.setName("patientsWithMMIInsAndOneVisit");
		patientsWithMMIInsAndOneVisit.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientsWithMMIInsAndOneVisit.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		patientsWithMMIInsAndOneVisit.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithMMIInsAndOneVisit.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithMMIInsAndOneVisit
				.getSearches()
				.put("patientsWithOneVisit",
						new Mapped<CohortDefinition>(
								patientsWithOneVisit,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMMIInsAndOneVisit
				.getSearches()
				.put("MMIInsCohortDef",
						new Mapped<CohortDefinition>(
								MMIInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIInsAndOneVisit
				.setCompositionString("patientsWithOneVisit AND MMIInsCohortDef");
		h.replaceCohortDefinition(patientsWithMMIInsAndOneVisit);

		CohortIndicator patientsWithMMIInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithMMIInsAndOneVisitIndicator
				.setName("patientsWithMMIInsAndOneVisitIndicator");
		patientsWithMMIInsAndOneVisitIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMMIInsAndOneVisitIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMMIInsAndOneVisitIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMMIInsAndOneVisit,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMMIInsAndOneVisitIndicator);

		// 5.1.4 Patients with MEDIPLAN Insurance and 1 visit
		CompositionCohortDefinition patientsWithMEDIPLANInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithMEDIPLANInsAndOneVisit
				.setName("patientsWithMEDIPLANInsAndOneVisit");
		patientsWithMEDIPLANInsAndOneVisit.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithMEDIPLANInsAndOneVisit.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithMEDIPLANInsAndOneVisit.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndOneVisit.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndOneVisit
				.getSearches()
				.put("patientsWithOneVisit",
						new Mapped<CohortDefinition>(
								patientsWithOneVisit,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMEDIPLANInsAndOneVisit
				.getSearches()
				.put("MEDIPLANInsCohortDef",
						new Mapped<CohortDefinition>(
								MEDIPLANInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANInsAndOneVisit
				.setCompositionString("patientsWithOneVisit AND MEDIPLANInsCohortDef");
		h.replaceCohortDefinition(patientsWithMEDIPLANInsAndOneVisit);

		CohortIndicator patientsWithMEDIPLANInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithMEDIPLANInsAndOneVisitIndicator
				.setName("patientsWithMEDIPLANInsAndOneVisitIndicator");
		patientsWithMEDIPLANInsAndOneVisitIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndOneVisitIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndOneVisitIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMEDIPLANInsAndOneVisit,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMEDIPLANInsAndOneVisitIndicator);

		// 5.1.5 Patients with CORAR Insurance and 1 visit
		CompositionCohortDefinition patientsWithCORARInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithCORARInsAndOneVisit
				.setName("patientsWithCORARInsAndOneVisit");
		patientsWithCORARInsAndOneVisit.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientsWithCORARInsAndOneVisit.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithCORARInsAndOneVisit.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithCORARInsAndOneVisit.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithCORARInsAndOneVisit
				.getSearches()
				.put("patientsWithOneVisit",
						new Mapped<CohortDefinition>(
								patientsWithOneVisit,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithCORARInsAndOneVisit
				.getSearches()
				.put("CORARInsCohortDef",
						new Mapped<CohortDefinition>(
								CORARInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARInsAndOneVisit
				.setCompositionString("patientsWithOneVisit AND CORARInsCohortDef");
		h.replaceCohortDefinition(patientsWithCORARInsAndOneVisit);

		CohortIndicator patientsWithCORARInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithCORARInsAndOneVisitIndicator
				.setName("patientsWithCORARInsAndOneVisitIndicator");
		patientsWithCORARInsAndOneVisitIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithCORARInsAndOneVisitIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithCORARInsAndOneVisitIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithCORARInsAndOneVisit,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithCORARInsAndOneVisitIndicator);

		// 5.1.6 Patients with NONE Insurance and 1 visit
		CompositionCohortDefinition patientsWithNONEInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithNONEInsAndOneVisit
				.setName("patientsWithNONEInsAndOneVisit");
		patientsWithNONEInsAndOneVisit.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientsWithNONEInsAndOneVisit.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		patientsWithNONEInsAndOneVisit.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithNONEInsAndOneVisit.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithNONEInsAndOneVisit
				.getSearches()
				.put("patientsWithOneVisit",
						new Mapped<CohortDefinition>(
								patientsWithOneVisit,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithNONEInsAndOneVisit
				.getSearches()
				.put("NONEInsCohortDef",
						new Mapped<CohortDefinition>(
								NONEInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEInsAndOneVisit
				.setCompositionString("patientsWithOneVisit AND NONEInsCohortDef");
		h.replaceCohortDefinition(patientsWithNONEInsAndOneVisit);

		CohortIndicator patientsWithNONEInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithNONEInsAndOneVisitIndicator
				.setName("patientsWithNONEInsAndOneVisitIndicator");
		patientsWithNONEInsAndOneVisitIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithNONEInsAndOneVisitIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithNONEInsAndOneVisitIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithNONEInsAndOneVisit,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithNONEInsAndOneVisitIndicator);
		// 5.1.7 Patients without Insurance and 1 visit
		CompositionCohortDefinition patientsWithMissingInsAndOneVisit = new CompositionCohortDefinition();
		patientsWithMissingInsAndOneVisit
				.setName("patientsWithMissingInsAndOneVisit");
		patientsWithMissingInsAndOneVisit.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithMissingInsAndOneVisit.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithMissingInsAndOneVisit.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMissingInsAndOneVisit.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithMissingInsAndOneVisit
				.getSearches()
				.put("patientsWithOneVisit",
						new Mapped<CohortDefinition>(
								patientsWithOneVisit,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMissingInsAndOneVisit
				.getSearches()
				.put("patientsMissingIns",
						new Mapped<CohortDefinition>(
								patientsMissingIns,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMissingInsAndOneVisit
				.setCompositionString("patientsWithOneVisit AND patientsMissingIns");
		h.replaceCohortDefinition(patientsWithMissingInsAndOneVisit);

		CohortIndicator patientsWithMissingInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithMissingInsAndOneVisitIndicator
				.setName("patientsWithMissingInsAndOneVisitIndicator");
		patientsWithMissingInsAndOneVisitIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMissingInsAndOneVisitIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMissingInsAndOneVisitIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMissingInsAndOneVisit,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMissingInsAndOneVisitIndicator);

		// 5.2.1 Patients with Mutuelle Insurance and 2 visits
		CompositionCohortDefinition patientsWithMUTUELLEInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithMUTUELLEInsAndTwoVisits
				.setName("patientsWithMUTUELLEInsAndTwoVisits");
		patientsWithMUTUELLEInsAndTwoVisits.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithMUTUELLEInsAndTwoVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithMUTUELLEInsAndTwoVisits.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndTwoVisits.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndTwoVisits
				.getSearches()
				.put("patientsWithTwoVisits",
						new Mapped<CohortDefinition>(
								patientsWithTwoVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMUTUELLEInsAndTwoVisits
				.getSearches()
				.put("MUTUELLEInsCohortDef",
						new Mapped<CohortDefinition>(
								MUTUELLEInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEInsAndTwoVisits
				.setCompositionString("patientsWithTwoVisits AND MUTUELLEInsCohortDef");
		h.replaceCohortDefinition(patientsWithMUTUELLEInsAndTwoVisits);

		CohortIndicator patientsWithMUTUELLEInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithMUTUELLEInsAndTwoVisitsIndicator
				.setName("patientsWithMUTUELLEInsAndTwoVisitsIndicator");
		patientsWithMUTUELLEInsAndTwoVisitsIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithMUTUELLEInsAndTwoVisitsIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndTwoVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMUTUELLEInsAndTwoVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMUTUELLEInsAndTwoVisitsIndicator);
		// 5.2.2 Patients with RAMA Insurance and 2 visits
		CompositionCohortDefinition patientsWithRAMAInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithRAMAInsAndTwoVisits
				.setName("patientsWithRAMAInsAndTwoVisits");
		patientsWithRAMAInsAndTwoVisits.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientsWithRAMAInsAndTwoVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithRAMAInsAndTwoVisits.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithRAMAInsAndTwoVisits.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithRAMAInsAndTwoVisits
				.getSearches()
				.put("patientsWithTwoVisits",
						new Mapped<CohortDefinition>(
								patientsWithTwoVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithRAMAInsAndTwoVisits
				.getSearches()
				.put("RAMAInsCohortDef",
						new Mapped<CohortDefinition>(
								RAMAInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAInsAndTwoVisits
				.setCompositionString("patientsWithTwoVisits AND RAMAInsCohortDef");
		h.replaceCohortDefinition(patientsWithRAMAInsAndTwoVisits);

		CohortIndicator patientsWithRAMAInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithRAMAInsAndTwoVisitsIndicator
				.setName("patientsWithRAMAInsAndTwoVisitsIndicator");
		patientsWithRAMAInsAndTwoVisitsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithRAMAInsAndTwoVisitsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithRAMAInsAndTwoVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithRAMAInsAndTwoVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithRAMAInsAndTwoVisitsIndicator);
		// 5.2.3 Patients with MMI Insurance and 2 visits
		CompositionCohortDefinition patientsWithMMIInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithMMIInsAndTwoVisits
				.setName("patientsWithMMIInsAndTwoVisits");
		patientsWithMMIInsAndTwoVisits.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientsWithMMIInsAndTwoVisits.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		patientsWithMMIInsAndTwoVisits.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithMMIInsAndTwoVisits.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithMMIInsAndTwoVisits
				.getSearches()
				.put("patientsWithTwoVisits",
						new Mapped<CohortDefinition>(
								patientsWithTwoVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMMIInsAndTwoVisits
				.getSearches()
				.put("MMIInsCohortDef",
						new Mapped<CohortDefinition>(
								MMIInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIInsAndTwoVisits
				.setCompositionString("patientsWithTwoVisits AND MMIInsCohortDef");
		h.replaceCohortDefinition(patientsWithMMIInsAndTwoVisits);

		CohortIndicator patientsWithMMIInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithMMIInsAndTwoVisitsIndicator
				.setName("patientsWithMMIInsAndTwoVisitsIndicator");
		patientsWithMMIInsAndTwoVisitsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMMIInsAndTwoVisitsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMMIInsAndTwoVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMMIInsAndTwoVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMMIInsAndTwoVisitsIndicator);

		// 5.2.4 Patients with MEDIPLAN Insurance and 2 visits
		CompositionCohortDefinition patientsWithMEDIPLANInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithMEDIPLANInsAndTwoVisits
				.setName("patientsWithMEDIPLANInsAndTwoVisits");
		patientsWithMEDIPLANInsAndTwoVisits.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithMEDIPLANInsAndTwoVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithMEDIPLANInsAndTwoVisits.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndTwoVisits.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndTwoVisits
				.getSearches()
				.put("patientsWithTwoVisits",
						new Mapped<CohortDefinition>(
								patientsWithTwoVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMEDIPLANInsAndTwoVisits
				.getSearches()
				.put("MEDIPLANInsCohortDef",
						new Mapped<CohortDefinition>(
								MEDIPLANInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANInsAndTwoVisits
				.setCompositionString("patientsWithTwoVisits AND MEDIPLANInsCohortDef");
		h.replaceCohortDefinition(patientsWithMEDIPLANInsAndTwoVisits);

		CohortIndicator patientsWithMEDIPLANInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithMEDIPLANInsAndTwoVisitsIndicator
				.setName("patientsWithMEDIPLANInsAndTwoVisitsIndicator");
		patientsWithMEDIPLANInsAndTwoVisitsIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithMEDIPLANInsAndTwoVisitsIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndTwoVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMEDIPLANInsAndTwoVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMEDIPLANInsAndTwoVisitsIndicator);

		// 5.2.5 Patients with CORAR Insurance and 2 visits
		CompositionCohortDefinition patientsWithCORARInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithCORARInsAndTwoVisits
				.setName("patientsWithCORARInsAndTwoVisits");
		patientsWithCORARInsAndTwoVisits.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithCORARInsAndTwoVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithCORARInsAndTwoVisits.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithCORARInsAndTwoVisits.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithCORARInsAndTwoVisits
				.getSearches()
				.put("patientsWithTwoVisits",
						new Mapped<CohortDefinition>(
								patientsWithTwoVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithCORARInsAndTwoVisits
				.getSearches()
				.put("CORARInsCohortDef",
						new Mapped<CohortDefinition>(
								CORARInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARInsAndTwoVisits
				.setCompositionString("patientsWithTwoVisits AND CORARInsCohortDef");
		h.replaceCohortDefinition(patientsWithCORARInsAndTwoVisits);

		CohortIndicator patientsWithCORARInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithCORARInsAndTwoVisitsIndicator
				.setName("patientsWithCORARInsAndTwoVisitsIndicator");
		patientsWithCORARInsAndTwoVisitsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithCORARInsAndTwoVisitsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithCORARInsAndTwoVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithCORARInsAndTwoVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithCORARInsAndTwoVisitsIndicator);

		// 5.2.6 Patients with NONE Insurance and 2 visits
		CompositionCohortDefinition patientsWithNONEInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithNONEInsAndTwoVisits
				.setName("patientsWithNONEInsAndTwoVisits");
		patientsWithNONEInsAndTwoVisits.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientsWithNONEInsAndTwoVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithNONEInsAndTwoVisits.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsWithNONEInsAndTwoVisits.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithNONEInsAndTwoVisits
				.getSearches()
				.put("patientsWithTwoVisits",
						new Mapped<CohortDefinition>(
								patientsWithTwoVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithNONEInsAndTwoVisits
				.getSearches()
				.put("NONEInsCohortDef",
						new Mapped<CohortDefinition>(
								NONEInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEInsAndTwoVisits
				.setCompositionString("patientsWithTwoVisits AND NONEInsCohortDef");
		h.replaceCohortDefinition(patientsWithNONEInsAndTwoVisits);

		CohortIndicator patientsWithNONEInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithNONEInsAndTwoVisitsIndicator
				.setName("patientsWithNONEInsAndTwoVisitsIndicator");
		patientsWithNONEInsAndTwoVisitsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithNONEInsAndTwoVisitsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithNONEInsAndTwoVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithNONEInsAndTwoVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithNONEInsAndTwoVisitsIndicator);
		// 5.2.7 Patients without Insurance and 2 visits
		CompositionCohortDefinition patientsWithMissingInsAndTwoVisits = new CompositionCohortDefinition();
		patientsWithMissingInsAndTwoVisits
				.setName("patientsWithMissingInsAndTwoVisits");
		patientsWithMissingInsAndTwoVisits.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithMissingInsAndTwoVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithMissingInsAndTwoVisits.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMissingInsAndTwoVisits.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMissingInsAndTwoVisits
				.getSearches()
				.put("patientsWithTwoVisits",
						new Mapped<CohortDefinition>(
								patientsWithTwoVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMissingInsAndTwoVisits
				.getSearches()
				.put("patientsMissingIns",
						new Mapped<CohortDefinition>(
								patientsMissingIns,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMissingInsAndTwoVisits
				.setCompositionString("patientsWithTwoVisits AND patientsMissingIns");
		h.replaceCohortDefinition(patientsWithMissingInsAndTwoVisits);

		CohortIndicator patientsWithMissingInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithMissingInsAndTwoVisitsIndicator
				.setName("patientsWithMissingInsAndTwoVisitsIndicator");
		patientsWithMissingInsAndTwoVisitsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMissingInsAndTwoVisitsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMissingInsAndTwoVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMissingInsAndTwoVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMissingInsAndTwoVisitsIndicator);

		// 5.3.1 Patients with Mutuelle Insurance and 3 visits
		CompositionCohortDefinition patientsWithMUTUELLEInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithMUTUELLEInsAndThreeVisits
				.setName("patientsWithMUTUELLEInsAndThreeVisits");
		patientsWithMUTUELLEInsAndThreeVisits.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithMUTUELLEInsAndThreeVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithMUTUELLEInsAndThreeVisits.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndThreeVisits.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndThreeVisits
				.getSearches()
				.put("patientsWithThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMUTUELLEInsAndThreeVisits
				.getSearches()
				.put("MUTUELLEInsCohortDef",
						new Mapped<CohortDefinition>(
								MUTUELLEInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEInsAndThreeVisits
				.setCompositionString("patientsWithThreeVisits AND MUTUELLEInsCohortDef");
		h.replaceCohortDefinition(patientsWithMUTUELLEInsAndThreeVisits);

		CohortIndicator patientsWithMUTUELLEInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithMUTUELLEInsAndThreeVisitsIndicator
				.setName("patientsWithMUTUELLEInsAndThreeVisitsIndicator");
		patientsWithMUTUELLEInsAndThreeVisitsIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithMUTUELLEInsAndThreeVisitsIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMUTUELLEInsAndThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMUTUELLEInsAndThreeVisitsIndicator);
		// 5.3.2 Patients with RAMA Insurance and 3 visits
		CompositionCohortDefinition patientsWithRAMAInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithRAMAInsAndThreeVisits
				.setName("patientsWithRAMAInsAndThreeVisits");
		patientsWithRAMAInsAndThreeVisits.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithRAMAInsAndThreeVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithRAMAInsAndThreeVisits.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithRAMAInsAndThreeVisits.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithRAMAInsAndThreeVisits
				.getSearches()
				.put("patientsWithThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithRAMAInsAndThreeVisits
				.getSearches()
				.put("RAMAInsCohortDef",
						new Mapped<CohortDefinition>(
								RAMAInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAInsAndThreeVisits
				.setCompositionString("patientsWithThreeVisits AND RAMAInsCohortDef");
		h.replaceCohortDefinition(patientsWithRAMAInsAndThreeVisits);

		CohortIndicator patientsWithRAMAInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithRAMAInsAndThreeVisitsIndicator
				.setName("patientsWithRAMAInsAndThreeVisitsIndicator");
		patientsWithRAMAInsAndThreeVisitsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithRAMAInsAndThreeVisitsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithRAMAInsAndThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithRAMAInsAndThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithRAMAInsAndThreeVisitsIndicator);
		// 5.3.3 Patients with MMI Insurance and 3 visits
		CompositionCohortDefinition patientsWithMMIInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithMMIInsAndThreeVisits
				.setName("patientsWithMMIInsAndThreeVisits");
		patientsWithMMIInsAndThreeVisits.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithMMIInsAndThreeVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithMMIInsAndThreeVisits.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMMIInsAndThreeVisits.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithMMIInsAndThreeVisits
				.getSearches()
				.put("patientsWithThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMMIInsAndThreeVisits
				.getSearches()
				.put("MMIInsCohortDef",
						new Mapped<CohortDefinition>(
								MMIInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIInsAndThreeVisits
				.setCompositionString("patientsWithThreeVisits AND MMIInsCohortDef");
		h.replaceCohortDefinition(patientsWithMMIInsAndThreeVisits);

		CohortIndicator patientsWithMMIInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithMMIInsAndThreeVisitsIndicator
				.setName("patientsWithMMIInsAndThreeVisitsIndicator");
		patientsWithMMIInsAndThreeVisitsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMMIInsAndThreeVisitsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMMIInsAndThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMMIInsAndThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMMIInsAndThreeVisitsIndicator);

		// 5.3.4 Patients with MEDIPLAN Insurance and 3 visits
		CompositionCohortDefinition patientsWithMEDIPLANInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithMEDIPLANInsAndThreeVisits
				.setName("patientsWithMEDIPLANInsAndThreeVisits");
		patientsWithMEDIPLANInsAndThreeVisits.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithMEDIPLANInsAndThreeVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithMEDIPLANInsAndThreeVisits.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndThreeVisits.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndThreeVisits
				.getSearches()
				.put("patientsWithThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMEDIPLANInsAndThreeVisits
				.getSearches()
				.put("MEDIPLANInsCohortDef",
						new Mapped<CohortDefinition>(
								MEDIPLANInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANInsAndThreeVisits
				.setCompositionString("patientsWithThreeVisits AND MEDIPLANInsCohortDef");
		h.replaceCohortDefinition(patientsWithMEDIPLANInsAndThreeVisits);

		CohortIndicator patientsWithMEDIPLANInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithMEDIPLANInsAndThreeVisitsIndicator
				.setName("patientsWithMEDIPLANInsAndThreeVisitsIndicator");
		patientsWithMEDIPLANInsAndThreeVisitsIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithMEDIPLANInsAndThreeVisitsIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMEDIPLANInsAndThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMEDIPLANInsAndThreeVisitsIndicator);

		// 5.3.5 Patients with CORAR Insurance and 3 visits
		CompositionCohortDefinition patientsWithCORARInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithCORARInsAndThreeVisits
				.setName("patientsWithCORARInsAndThreeVisits");
		patientsWithCORARInsAndThreeVisits.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithCORARInsAndThreeVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithCORARInsAndThreeVisits.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithCORARInsAndThreeVisits.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithCORARInsAndThreeVisits
				.getSearches()
				.put("patientsWithThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithCORARInsAndThreeVisits
				.getSearches()
				.put("CORARInsCohortDef",
						new Mapped<CohortDefinition>(
								CORARInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARInsAndThreeVisits
				.setCompositionString("patientsWithThreeVisits AND CORARInsCohortDef");
		h.replaceCohortDefinition(patientsWithCORARInsAndThreeVisits);

		CohortIndicator patientsWithCORARInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithCORARInsAndThreeVisitsIndicator
				.setName("patientsWithCORARInsAndThreeVisitsIndicator");
		patientsWithCORARInsAndThreeVisitsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithCORARInsAndThreeVisitsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithCORARInsAndThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithCORARInsAndThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithCORARInsAndThreeVisitsIndicator);

		// 5.3.6 Patients with NONE Insurance and 3 visits
		CompositionCohortDefinition patientsWithNONEInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithNONEInsAndThreeVisits
				.setName("patientsWithNONEInsAndThreeVisits");
		patientsWithNONEInsAndThreeVisits.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithNONEInsAndThreeVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithNONEInsAndThreeVisits.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithNONEInsAndThreeVisits.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientsWithNONEInsAndThreeVisits
				.getSearches()
				.put("patientsWithThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithNONEInsAndThreeVisits
				.getSearches()
				.put("NONEInsCohortDef",
						new Mapped<CohortDefinition>(
								NONEInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEInsAndThreeVisits
				.setCompositionString("patientsWithThreeVisits AND NONEInsCohortDef");
		h.replaceCohortDefinition(patientsWithNONEInsAndThreeVisits);

		CohortIndicator patientsWithNONEInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithNONEInsAndThreeVisitsIndicator
				.setName("patientsWithNONEInsAndThreeVisitsIndicator");
		patientsWithNONEInsAndThreeVisitsIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithNONEInsAndThreeVisitsIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithNONEInsAndThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithNONEInsAndThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithNONEInsAndThreeVisitsIndicator);
		// 5.3.7 Patients without Insurance and 3 visits
		CompositionCohortDefinition patientsWithMissingInsAndThreeVisits = new CompositionCohortDefinition();
		patientsWithMissingInsAndThreeVisits
				.setName("patientsWithMissingInsAndThreeVisits");
		patientsWithMissingInsAndThreeVisits.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithMissingInsAndThreeVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithMissingInsAndThreeVisits.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMissingInsAndThreeVisits.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMissingInsAndThreeVisits
				.getSearches()
				.put("patientsWithThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMissingInsAndThreeVisits
				.getSearches()
				.put("patientsMissingIns",
						new Mapped<CohortDefinition>(
								patientsMissingIns,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMissingInsAndThreeVisits
				.setCompositionString("patientsWithThreeVisits AND patientsMissingIns");
		h.replaceCohortDefinition(patientsWithMissingInsAndThreeVisits);

		CohortIndicator patientsWithMissingInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithMissingInsAndThreeVisitsIndicator
				.setName("patientsWithMissingInsAndThreeVisitsIndicator");
		patientsWithMissingInsAndThreeVisitsIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithMissingInsAndThreeVisitsIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMissingInsAndThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMissingInsAndThreeVisitsIndicator);

		// 5.4.1 Patients with Mutuelle Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithMUTUELLEInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits
				.setName("patientsWithMUTUELLEInsAndGreaterThanThreeVisits");
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("onOrAfter", "onOrAfter",
						Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("onOrBefore", "onOrBefore",
						Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits
				.getSearches()
				.put("patientsWithGreaterThanThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithGreaterThanThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits
				.getSearches()
				.put("MUTUELLEInsCohortDef",
						new Mapped<CohortDefinition>(
								MUTUELLEInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits
				.setCompositionString("patientsWithGreaterThanThreeVisits AND MUTUELLEInsCohortDef");
		h.replaceCohortDefinition(patientsWithMUTUELLEInsAndGreaterThanThreeVisits);

		CohortIndicator patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator
				.setName("patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator");
		patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMUTUELLEInsAndGreaterThanThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator);

		// 5.4.2 Patients with RAMA Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithRAMAInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithRAMAInsAndGreaterThanThreeVisits
				.setName("patientsWithRAMAInsAndGreaterThanThreeVisits");
		patientsWithRAMAInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("onOrAfter", "onOrAfter",
						Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("onOrBefore", "onOrBefore",
						Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisits
				.getSearches()
				.put("patientsWithGreaterThanThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithGreaterThanThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithRAMAInsAndGreaterThanThreeVisits
				.getSearches()
				.put("RAMAInsCohortDef",
						new Mapped<CohortDefinition>(
								RAMAInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAInsAndGreaterThanThreeVisits
				.setCompositionString("patientsWithGreaterThanThreeVisits AND RAMAInsCohortDef");
		h.replaceCohortDefinition(patientsWithRAMAInsAndGreaterThanThreeVisits);

		CohortIndicator patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator
				.setName("patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator");
		patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithRAMAInsAndGreaterThanThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator);

		// 5.4.3 Patients with MMI Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithMMIInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithMMIInsAndGreaterThanThreeVisits
				.setName("patientsWithMMIInsAndGreaterThanThreeVisits");
		patientsWithMMIInsAndGreaterThanThreeVisits.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisits.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisits.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisits.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisits
				.getSearches()
				.put("patientsWithGreaterThanThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithGreaterThanThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMMIInsAndGreaterThanThreeVisits
				.getSearches()
				.put("MMIInsCohortDef",
						new Mapped<CohortDefinition>(
								MMIInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIInsAndGreaterThanThreeVisits
				.setCompositionString("patientsWithGreaterThanThreeVisits AND MMIInsCohortDef");
		h.replaceCohortDefinition(patientsWithMMIInsAndGreaterThanThreeVisits);

		CohortIndicator patientsWithMMIInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithMMIInsAndGreaterThanThreeVisitsIndicator
				.setName("patientsWithMMIInsAndGreaterThanThreeVisitsIndicator");
		patientsWithMMIInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMMIInsAndGreaterThanThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMMIInsAndGreaterThanThreeVisitsIndicator);

		// 5.4.4 Patients with MEDIPLAN Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithMEDIPLANInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits
				.setName("patientsWithMEDIPLANInsAndGreaterThanThreeVisits");
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("onOrAfter", "onOrAfter",
						Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("onOrBefore", "onOrBefore",
						Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits
				.getSearches()
				.put("patientsWithGreaterThanThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithGreaterThanThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits
				.getSearches()
				.put("MEDIPLANInsCohortDef",
						new Mapped<CohortDefinition>(
								MEDIPLANInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits
				.setCompositionString("patientsWithGreaterThanThreeVisits AND MEDIPLANInsCohortDef");
		h.replaceCohortDefinition(patientsWithMEDIPLANInsAndGreaterThanThreeVisits);

		CohortIndicator patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator
				.setName("patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator");
		patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMEDIPLANInsAndGreaterThanThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator);

		// 5.4.5 Patients with CORAR Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithCORARInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithCORARInsAndGreaterThanThreeVisits
				.setName("patientsWithCORARInsAndGreaterThanThreeVisits");
		patientsWithCORARInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("onOrAfter", "onOrAfter",
						Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("onOrBefore", "onOrBefore",
						Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisits
				.getSearches()
				.put("patientsWithGreaterThanThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithGreaterThanThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithCORARInsAndGreaterThanThreeVisits
				.getSearches()
				.put("CORARInsCohortDef",
						new Mapped<CohortDefinition>(
								CORARInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARInsAndGreaterThanThreeVisits
				.setCompositionString("patientsWithGreaterThanThreeVisits AND CORARInsCohortDef");
		h.replaceCohortDefinition(patientsWithCORARInsAndGreaterThanThreeVisits);

		CohortIndicator patientsWithCORARInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithCORARInsAndGreaterThanThreeVisitsIndicator
				.setName("patientsWithCORARInsAndGreaterThanThreeVisitsIndicator");
		patientsWithCORARInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithCORARInsAndGreaterThanThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithCORARInsAndGreaterThanThreeVisitsIndicator);

		// 5.4.6 Patients with NONE Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithNONEInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithNONEInsAndGreaterThanThreeVisits
				.setName("patientsWithNONEInsAndGreaterThanThreeVisits");
		patientsWithNONEInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("onOrAfter", "onOrAfter",
						Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("onOrBefore", "onOrBefore",
						Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisits
				.getSearches()
				.put("patientsWithGreaterThanThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithGreaterThanThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithNONEInsAndGreaterThanThreeVisits
				.getSearches()
				.put("NONEInsCohortDef",
						new Mapped<CohortDefinition>(
								NONEInsCohortDef,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEInsAndGreaterThanThreeVisits
				.setCompositionString("patientsWithGreaterThanThreeVisits AND NONEInsCohortDef");
		h.replaceCohortDefinition(patientsWithNONEInsAndGreaterThanThreeVisits);

		CohortIndicator patientsWithNONEInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithNONEInsAndGreaterThanThreeVisitsIndicator
				.setName("patientsWithNONEInsAndGreaterThanThreeVisitsIndicator");
		patientsWithNONEInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithNONEInsAndGreaterThanThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithNONEInsAndGreaterThanThreeVisitsIndicator);
		// 5.4.7 Patients without Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithMissingInsAndGreaterThanThreeVisits = new CompositionCohortDefinition();
		patientsWithMissingInsAndGreaterThanThreeVisits
				.setName("patientsWithMissingInsAndGreaterThanThreeVisits");
		patientsWithMissingInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("onOrAfter", "onOrAfter",
						Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("onOrBefore", "onOrBefore",
						Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisits
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisits
				.getSearches()
				.put("patientsWithGreaterThanThreeVisits",
						new Mapped<CohortDefinition>(
								patientsWithGreaterThanThreeVisits,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMissingInsAndGreaterThanThreeVisits
				.getSearches()
				.put("patientsMissingIns",
						new Mapped<CohortDefinition>(
								patientsMissingIns,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMissingInsAndGreaterThanThreeVisits
				.setCompositionString("patientsWithGreaterThanThreeVisits AND patientsMissingIns");
		h.replaceCohortDefinition(patientsWithMissingInsAndGreaterThanThreeVisits);

		CohortIndicator patientsWithMissingInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithMissingInsAndGreaterThanThreeVisitsIndicator
				.setName("patientsWithMissingInsAndGreaterThanThreeVisitsIndicator");
		patientsWithMissingInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisitsIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisitsIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						patientsWithMissingInsAndGreaterThanThreeVisits,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMissingInsAndGreaterThanThreeVisitsIndicator);

		// ========================================================================
		// 6. Age breakdown by gender
		// ========================================================================

		// Gender Cohort definitions
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setName("females");
		females.setFemaleIncluded(true);
		h.replaceCohortDefinition(females);

		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setName("males");
		males.setMaleIncluded(true);
		h.replaceCohortDefinition(males);

		// Age intervals Cohort definitions

		AgeCohortDefinition zeroToOne = new AgeCohortDefinition(0, 1, null);
		zeroToOne.setName("zeroToOne");
		h.replaceCohortDefinition(zeroToOne);

		AgeCohortDefinition oneToTwo = new AgeCohortDefinition(1, 2, null);
		oneToTwo.setName("oneToTwo");
		h.replaceCohortDefinition(oneToTwo);

		AgeCohortDefinition twoToThree = new AgeCohortDefinition(2, 3, null);
		twoToThree.setName("twoToThree");
		h.replaceCohortDefinition(twoToThree);

		AgeCohortDefinition threeToFour = new AgeCohortDefinition(3, 4, null);
		threeToFour.setName("threeToFour");
		h.replaceCohortDefinition(threeToFour);

		AgeCohortDefinition fourToFive = new AgeCohortDefinition(4, 5, null);
		fourToFive.setName("fourToFive");
		h.replaceCohortDefinition(fourToFive);

		AgeCohortDefinition fiveToFifteen = new AgeCohortDefinition(5, 15, null);
		fiveToFifteen.setName("fiveToFifteen");
		h.replaceCohortDefinition(fiveToFifteen);

		AgeCohortDefinition fifteenAndPlus = new AgeCohortDefinition(15, null,
				null);
		fifteenAndPlus.setName("fifteenAndPlus");
		h.replaceCohortDefinition(fifteenAndPlus);

		// 6.1.m Male Patients with Registration and age 0-1

		CompositionCohortDefinition maleWithRegistrationAndAgeZeroToOne = new CompositionCohortDefinition();
		maleWithRegistrationAndAgeZeroToOne
				.setName("maleWithRegistrationAndAgeZeroToOne");
		maleWithRegistrationAndAgeZeroToOne.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		maleWithRegistrationAndAgeZeroToOne.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		maleWithRegistrationAndAgeZeroToOne
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeZeroToOne.getSearches().put("males",
				new Mapped<CohortDefinition>(males, null));
		maleWithRegistrationAndAgeZeroToOne.getSearches().put("zeroToOne",
				new Mapped<CohortDefinition>(zeroToOne, null));
		maleWithRegistrationAndAgeZeroToOne
				.setCompositionString("patientsWithPrimaryCareRegistration AND males AND zeroToOne");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeZeroToOne);

		CohortIndicator maleWithRegistrationAndAgeZeroToOneIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeZeroToOneIndicator
				.setName("maleWithRegistrationAndAgeZeroToOneIndicator");
		maleWithRegistrationAndAgeZeroToOneIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		maleWithRegistrationAndAgeZeroToOneIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeZeroToOneIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						maleWithRegistrationAndAgeZeroToOne,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeZeroToOneIndicator);
		// 6.2.m Male Patients with Registration and age 1-2

		CompositionCohortDefinition maleWithRegistrationAndAgeOneToTwo = new CompositionCohortDefinition();
		maleWithRegistrationAndAgeOneToTwo
				.setName("maleWithRegistrationAndAgeOneToTwo");
		maleWithRegistrationAndAgeOneToTwo.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		maleWithRegistrationAndAgeOneToTwo.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		maleWithRegistrationAndAgeOneToTwo
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeOneToTwo.getSearches().put("males",
				new Mapped<CohortDefinition>(males, null));
		maleWithRegistrationAndAgeOneToTwo.getSearches().put("oneToTwo",
				new Mapped<CohortDefinition>(oneToTwo, null));
		maleWithRegistrationAndAgeOneToTwo
				.setCompositionString("patientsWithPrimaryCareRegistration AND males AND oneToTwo");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeOneToTwo);

		CohortIndicator maleWithRegistrationAndAgeOneToTwoIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeOneToTwoIndicator
				.setName("maleWithRegistrationAndAgeOneToTwoIndicator");
		maleWithRegistrationAndAgeOneToTwoIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		maleWithRegistrationAndAgeOneToTwoIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeOneToTwoIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						maleWithRegistrationAndAgeOneToTwo,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeOneToTwoIndicator);
		// 6.3.m Male Patients with Registration and age 2-3

		CompositionCohortDefinition maleWithRegistrationAndAgeTwoToThree = new CompositionCohortDefinition();
		maleWithRegistrationAndAgeTwoToThree
				.setName("maleWithRegistrationAndAgeTwoToThree");
		maleWithRegistrationAndAgeTwoToThree.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		maleWithRegistrationAndAgeTwoToThree.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		maleWithRegistrationAndAgeTwoToThree
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeTwoToThree.getSearches().put("males",
				new Mapped<CohortDefinition>(males, null));
		maleWithRegistrationAndAgeTwoToThree.getSearches().put("twoToThree",
				new Mapped<CohortDefinition>(twoToThree, null));
		maleWithRegistrationAndAgeTwoToThree
				.setCompositionString("patientsWithPrimaryCareRegistration AND males AND twoToThree");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeTwoToThree);

		CohortIndicator maleWithRegistrationAndAgeTwoToThreeIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeTwoToThreeIndicator
				.setName("maleWithRegistrationAndAgeTwoToThreeIndicator");
		maleWithRegistrationAndAgeTwoToThreeIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		maleWithRegistrationAndAgeTwoToThreeIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeTwoToThreeIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						maleWithRegistrationAndAgeTwoToThree,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeTwoToThreeIndicator);
		// 6.4.m Male Patients with Registration and age 3-4

		CompositionCohortDefinition maleWithRegistrationAndAgeThreeToFour = new CompositionCohortDefinition();
		maleWithRegistrationAndAgeThreeToFour
				.setName("maleWithRegistrationAndAgeThreeToFour");
		maleWithRegistrationAndAgeThreeToFour.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		maleWithRegistrationAndAgeThreeToFour.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		maleWithRegistrationAndAgeThreeToFour
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeThreeToFour.getSearches().put("males",
				new Mapped<CohortDefinition>(males, null));
		maleWithRegistrationAndAgeThreeToFour.getSearches().put("threeToFour",
				new Mapped<CohortDefinition>(threeToFour, null));
		maleWithRegistrationAndAgeThreeToFour
				.setCompositionString("patientsWithPrimaryCareRegistration AND males AND threeToFour");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeThreeToFour);

		CohortIndicator maleWithRegistrationAndAgeThreeToFourIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeThreeToFourIndicator
				.setName("maleWithRegistrationAndAgeThreeToFourIndicator");
		maleWithRegistrationAndAgeThreeToFourIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		maleWithRegistrationAndAgeThreeToFourIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeThreeToFourIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						maleWithRegistrationAndAgeThreeToFour,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeThreeToFourIndicator);
		// 6.5.m Male Patients with Registration and age 4-5

		CompositionCohortDefinition maleWithRegistrationAndAgeFourToFive = new CompositionCohortDefinition();
		maleWithRegistrationAndAgeFourToFive
				.setName("maleWithRegistrationAndAgeFourToFive");
		maleWithRegistrationAndAgeFourToFive.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		maleWithRegistrationAndAgeFourToFive.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		maleWithRegistrationAndAgeFourToFive
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeFourToFive.getSearches().put("males",
				new Mapped<CohortDefinition>(males, null));
		maleWithRegistrationAndAgeFourToFive.getSearches().put("fourToFive",
				new Mapped<CohortDefinition>(fourToFive, null));
		maleWithRegistrationAndAgeFourToFive
				.setCompositionString("patientsWithPrimaryCareRegistration AND males AND fourToFive");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeFourToFive);

		CohortIndicator maleWithRegistrationAndAgeFourToFiveIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeFourToFiveIndicator
				.setName("maleWithRegistrationAndAgeFourToFiveIndicator");
		maleWithRegistrationAndAgeFourToFiveIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		maleWithRegistrationAndAgeFourToFiveIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeFourToFiveIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						maleWithRegistrationAndAgeFourToFive,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeFourToFiveIndicator);
		// 6.6.m Male Patients with Registration and age 5-15

		CompositionCohortDefinition maleWithRegistrationAndAgeFiveToFifteen = new CompositionCohortDefinition();
		maleWithRegistrationAndAgeFiveToFifteen
				.setName("maleWithRegistrationAndAgeFiveToFifteen");
		maleWithRegistrationAndAgeFiveToFifteen.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		maleWithRegistrationAndAgeFiveToFifteen.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		maleWithRegistrationAndAgeFiveToFifteen
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeFiveToFifteen.getSearches().put("males",
				new Mapped<CohortDefinition>(males, null));
		maleWithRegistrationAndAgeFiveToFifteen.getSearches().put(
				"fiveToFifteen",
				new Mapped<CohortDefinition>(fiveToFifteen, null));
		maleWithRegistrationAndAgeFiveToFifteen
				.setCompositionString("patientsWithPrimaryCareRegistration AND males AND fiveToFifteen");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeFiveToFifteen);

		CohortIndicator maleWithRegistrationAndAgeFiveToFifteenIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeFiveToFifteenIndicator
				.setName("maleWithRegistrationAndAgeFiveToFifteenIndicator");
		maleWithRegistrationAndAgeFiveToFifteenIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		maleWithRegistrationAndAgeFiveToFifteenIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeFiveToFifteenIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						maleWithRegistrationAndAgeFiveToFifteen,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeFiveToFifteenIndicator);
		// 6.7.m Male Patients with Registration and age 15+

		CompositionCohortDefinition maleWithRegistrationAndAgeFifteenAndPlus = new CompositionCohortDefinition();
		maleWithRegistrationAndAgeFifteenAndPlus
				.setName("maleWithRegistrationAndAgeFifteenAndPlus");
		maleWithRegistrationAndAgeFifteenAndPlus.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		maleWithRegistrationAndAgeFifteenAndPlus.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		maleWithRegistrationAndAgeFifteenAndPlus
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeFifteenAndPlus.getSearches().put("males",
				new Mapped<CohortDefinition>(males, null));
		maleWithRegistrationAndAgeFifteenAndPlus.getSearches().put(
				"fifteenAndPlus",
				new Mapped<CohortDefinition>(fifteenAndPlus, null));
		maleWithRegistrationAndAgeFifteenAndPlus
				.setCompositionString("patientsWithPrimaryCareRegistration AND males AND fifteenAndPlus");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeFifteenAndPlus);

		CohortIndicator maleWithRegistrationAndAgeFifteenAndPlusIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeFifteenAndPlusIndicator
				.setName("maleWithRegistrationAndAgeFifteenAndPlusIndicator");
		maleWithRegistrationAndAgeFifteenAndPlusIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		maleWithRegistrationAndAgeFifteenAndPlusIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeFifteenAndPlusIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						maleWithRegistrationAndAgeFifteenAndPlus,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeFifteenAndPlusIndicator);
		// 6.1.f Female Patients with Registration and age 0-1

		CompositionCohortDefinition femaleWithRegistrationAndAgeZeroToOne = new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeZeroToOne
				.setName("femaleWithRegistrationAndAgeZeroToOne");
		femaleWithRegistrationAndAgeZeroToOne.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		femaleWithRegistrationAndAgeZeroToOne.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		femaleWithRegistrationAndAgeZeroToOne
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeZeroToOne.getSearches().put("females",
				new Mapped<CohortDefinition>(females, null));
		femaleWithRegistrationAndAgeZeroToOne.getSearches().put("zeroToOne",
				new Mapped<CohortDefinition>(zeroToOne, null));
		femaleWithRegistrationAndAgeZeroToOne
				.setCompositionString("patientsWithPrimaryCareRegistration AND females AND zeroToOne");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeZeroToOne);

		CohortIndicator femaleWithRegistrationAndAgeZeroToOneIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeZeroToOneIndicator
				.setName("femaleWithRegistrationAndAgeZeroToOneIndicator");
		femaleWithRegistrationAndAgeZeroToOneIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femaleWithRegistrationAndAgeZeroToOneIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeZeroToOneIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						femaleWithRegistrationAndAgeZeroToOne,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeZeroToOneIndicator);
		// 6.2.f Female Patients with Registration and age 1-2

		CompositionCohortDefinition femaleWithRegistrationAndAgeOneToTwo = new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeOneToTwo
				.setName("femaleWithRegistrationAndAgeOneToTwo");
		femaleWithRegistrationAndAgeOneToTwo.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		femaleWithRegistrationAndAgeOneToTwo.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		femaleWithRegistrationAndAgeOneToTwo
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeOneToTwo.getSearches().put("females",
				new Mapped<CohortDefinition>(females, null));
		femaleWithRegistrationAndAgeOneToTwo.getSearches().put("oneToTwo",
				new Mapped<CohortDefinition>(oneToTwo, null));
		femaleWithRegistrationAndAgeOneToTwo
				.setCompositionString("patientsWithPrimaryCareRegistration AND females AND oneToTwo");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeOneToTwo);

		CohortIndicator femaleWithRegistrationAndAgeOneToTwoIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeOneToTwoIndicator
				.setName("femaleWithRegistrationAndAgeOneToTwoIndicator");
		femaleWithRegistrationAndAgeOneToTwoIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femaleWithRegistrationAndAgeOneToTwoIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeOneToTwoIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						femaleWithRegistrationAndAgeOneToTwo,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeOneToTwoIndicator);
		// 6.3.f Female Patients with Registration and age 2-3

		CompositionCohortDefinition femaleWithRegistrationAndAgeTwoToThree = new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeTwoToThree
				.setName("femaleWithRegistrationAndAgeTwoToThree");
		femaleWithRegistrationAndAgeTwoToThree.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		femaleWithRegistrationAndAgeTwoToThree.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		femaleWithRegistrationAndAgeTwoToThree
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeTwoToThree.getSearches().put("females",
				new Mapped<CohortDefinition>(females, null));
		femaleWithRegistrationAndAgeTwoToThree.getSearches().put("twoToThree",
				new Mapped<CohortDefinition>(twoToThree, null));
		femaleWithRegistrationAndAgeTwoToThree
				.setCompositionString("patientsWithPrimaryCareRegistration AND females AND twoToThree");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeTwoToThree);

		CohortIndicator femaleWithRegistrationAndAgeTwoToThreeIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeTwoToThreeIndicator
				.setName("femaleWithRegistrationAndAgeTwoToThreeIndicator");
		femaleWithRegistrationAndAgeTwoToThreeIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femaleWithRegistrationAndAgeTwoToThreeIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeTwoToThreeIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						femaleWithRegistrationAndAgeTwoToThree,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeTwoToThreeIndicator);
		// 6.4.f Female Patients with Registration and age 3-4

		CompositionCohortDefinition femaleWithRegistrationAndAgeThreeToFour = new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeThreeToFour
				.setName("femaleWithRegistrationAndAgeThreeToFour");
		femaleWithRegistrationAndAgeThreeToFour.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		femaleWithRegistrationAndAgeThreeToFour.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		femaleWithRegistrationAndAgeThreeToFour
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeThreeToFour.getSearches().put("females",
				new Mapped<CohortDefinition>(females, null));
		femaleWithRegistrationAndAgeThreeToFour.getSearches().put(
				"threeToFour", new Mapped<CohortDefinition>(threeToFour, null));
		femaleWithRegistrationAndAgeThreeToFour
				.setCompositionString("patientsWithPrimaryCareRegistration AND females AND threeToFour");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeThreeToFour);

		CohortIndicator femaleWithRegistrationAndAgeThreeToFourIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeThreeToFourIndicator
				.setName("femaleWithRegistrationAndAgeThreeToFourIndicator");
		femaleWithRegistrationAndAgeThreeToFourIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femaleWithRegistrationAndAgeThreeToFourIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeThreeToFourIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						femaleWithRegistrationAndAgeThreeToFour,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeThreeToFourIndicator);
		// 6.5.f Female Patients with Registration and age 4-5

		CompositionCohortDefinition femaleWithRegistrationAndAgeFourToFive = new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeFourToFive
				.setName("femaleWithRegistrationAndAgeFourToFive");
		femaleWithRegistrationAndAgeFourToFive.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		femaleWithRegistrationAndAgeFourToFive.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		femaleWithRegistrationAndAgeFourToFive
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeFourToFive.getSearches().put("females",
				new Mapped<CohortDefinition>(females, null));
		femaleWithRegistrationAndAgeFourToFive.getSearches().put("fourToFive",
				new Mapped<CohortDefinition>(fourToFive, null));
		femaleWithRegistrationAndAgeFourToFive
				.setCompositionString("patientsWithPrimaryCareRegistration AND females AND fourToFive");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeFourToFive);

		CohortIndicator femaleWithRegistrationAndAgeFourToFiveIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeFourToFiveIndicator
				.setName("femaleWithRegistrationAndAgeFourToFiveIndicator");
		femaleWithRegistrationAndAgeFourToFiveIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femaleWithRegistrationAndAgeFourToFiveIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeFourToFiveIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						femaleWithRegistrationAndAgeFourToFive,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeFourToFiveIndicator);
		// 6.6.f Female Patients with Registration and age 5-15

		CompositionCohortDefinition femaleWithRegistrationAndAgeFiveToFifteen = new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeFiveToFifteen
				.setName("femaleWithRegistrationAndAgeFiveToFifteen");
		femaleWithRegistrationAndAgeFiveToFifteen.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		femaleWithRegistrationAndAgeFiveToFifteen.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		femaleWithRegistrationAndAgeFiveToFifteen
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeFiveToFifteen.getSearches().put("females",
				new Mapped<CohortDefinition>(females, null));
		femaleWithRegistrationAndAgeFiveToFifteen.getSearches().put(
				"fiveToFifteen",
				new Mapped<CohortDefinition>(fiveToFifteen, null));
		femaleWithRegistrationAndAgeFiveToFifteen
				.setCompositionString("patientsWithPrimaryCareRegistration AND females AND fiveToFifteen");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeFiveToFifteen);

		CohortIndicator femaleWithRegistrationAndAgeFiveToFifteenIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeFiveToFifteenIndicator
				.setName("femaleWithRegistrationAndAgeFiveToFifteenIndicator");
		femaleWithRegistrationAndAgeFiveToFifteenIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femaleWithRegistrationAndAgeFiveToFifteenIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeFiveToFifteenIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						femaleWithRegistrationAndAgeFiveToFifteen,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeFiveToFifteenIndicator);
		// 6.7.f Female Patients with Registration and age 15+

		CompositionCohortDefinition femaleWithRegistrationAndAgeFifteenAndPlus = new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeFifteenAndPlus
				.setName("femaleWithRegistrationAndAgeFifteenAndPlus");
		femaleWithRegistrationAndAgeFifteenAndPlus.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		femaleWithRegistrationAndAgeFifteenAndPlus.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		femaleWithRegistrationAndAgeFifteenAndPlus
				.getSearches()
				.put("patientsWithPrimaryCareRegistration",
						new Mapped<CohortDefinition>(
								patientsWithPrimaryCareRegistration,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeFifteenAndPlus.getSearches().put("females",
				new Mapped<CohortDefinition>(females, null));
		femaleWithRegistrationAndAgeFifteenAndPlus.getSearches().put(
				"fifteenAndPlus",
				new Mapped<CohortDefinition>(fifteenAndPlus, null));
		femaleWithRegistrationAndAgeFifteenAndPlus
				.setCompositionString("patientsWithPrimaryCareRegistration AND females AND fifteenAndPlus");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeFifteenAndPlus);

		CohortIndicator femaleWithRegistrationAndAgeFifteenAndPlusIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeFifteenAndPlusIndicator
				.setName("femaleWithRegistrationAndAgeFifteenAndPlusIndicator");
		femaleWithRegistrationAndAgeFifteenAndPlusIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femaleWithRegistrationAndAgeFifteenAndPlusIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeFifteenAndPlusIndicator
				.setCohortDefinition(new Mapped<CohortDefinition>(
						femaleWithRegistrationAndAgeFifteenAndPlus,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeFifteenAndPlusIndicator);

		// ========================================================================
		// 7. Primary care service requested
		// ========================================================================

		// 7.1.f Female Total number of patient requested primary care

		SqlEncounterGroupDefinition femalePatientsrequestPrimCare = new SqlEncounterGroupDefinition();
		femalePatientsrequestPrimCare.setName("femalePatientsrequestPrimCare");
		femalePatientsrequestPrimCare
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientsrequestPrimCare.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		femalePatientsrequestPrimCare.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientsrequestPrimCare);

		EncounterIndicator femalePatientsrequestPrimCareInRegistrationIndicator = new EncounterIndicator();
		femalePatientsrequestPrimCareInRegistrationIndicator
				.setName("femalePatientsrequestPrimCareInRegistrationIndicator");
		femalePatientsrequestPrimCareInRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestPrimCareInRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestPrimCareInRegistrationIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientsrequestPrimCare,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestPrimCareInRegistrationIndicator);

		// 7.1.m Female Total number of patient requested primary care

		SqlEncounterGroupDefinition malePatientsrequestPrimCare = new SqlEncounterGroupDefinition();
		malePatientsrequestPrimCare.setName("malePatientsrequestPrimCare");
		malePatientsrequestPrimCare
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientsrequestPrimCare.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		malePatientsrequestPrimCare.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientsrequestPrimCare);

		EncounterIndicator malePatientsrequestPrimCareInRegistrationIndicator = new EncounterIndicator();
		malePatientsrequestPrimCareInRegistrationIndicator
				.setName("malePatientsrequestPrimCareInRegistrationIndicator");
		malePatientsrequestPrimCareInRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestPrimCareInRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestPrimCareInRegistrationIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientsrequestPrimCare,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestPrimCareInRegistrationIndicator);

		// 7.2.f Female Number of patients requested VCT PROGRAM

		SqlEncounterGroupDefinition femalePatientRequestVCTProgram = new SqlEncounterGroupDefinition();
		femalePatientRequestVCTProgram
				.setName("femalePatientRequestVCTProgram");
		femalePatientRequestVCTProgram
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.VCT_PROGRAM)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestVCTProgram.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		femalePatientRequestVCTProgram.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestVCTProgram);

		EncounterIndicator femalePatientsrequestVCTProgramInRegistrationIndicator = new EncounterIndicator();
		femalePatientsrequestVCTProgramInRegistrationIndicator
				.setName("femalePatientsrequestVCTProgramInRegistrationIndicator");
		femalePatientsrequestVCTProgramInRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestVCTProgramInRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestVCTProgramInRegistrationIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestVCTProgram,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestVCTProgramInRegistrationIndicator);

		// 7.2.m Male Number of patients requested VCT PROGRAM
		SqlEncounterGroupDefinition malePatientRequestVCTProgram = new SqlEncounterGroupDefinition();
		malePatientRequestVCTProgram.setName("malePatientRequestVCTProgram");
		malePatientRequestVCTProgram
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.VCT_PROGRAM)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestVCTProgram.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		malePatientRequestVCTProgram.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestVCTProgram);

		EncounterIndicator malePatientsrequestVCTProgramInRegistrationIndicator = new EncounterIndicator();
		malePatientsrequestVCTProgramInRegistrationIndicator
				.setName("malePatientsrequestVCTProgramInRegistrationIndicator");
		malePatientsrequestVCTProgramInRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestVCTProgramInRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestVCTProgramInRegistrationIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestVCTProgram,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestVCTProgramInRegistrationIndicator);

		// 7.3.f Female Number of patients requested ANTENATAL CLINIC

		SqlEncounterGroupDefinition femalePatientRequestAntenatalClinic = new SqlEncounterGroupDefinition();
		femalePatientRequestAntenatalClinic
				.setName("patientRequestAntenatalClinic");
		femalePatientRequestAntenatalClinic
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.ANTENATAL_CLINIC)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestAntenatalClinic.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientRequestAntenatalClinic.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestAntenatalClinic);

		EncounterIndicator femalePatientsrequestAntenatalClinicInRegistrationIndicator = new EncounterIndicator();
		femalePatientsrequestAntenatalClinicInRegistrationIndicator
				.setName("femalePatientsrequestAntenatalClinicInRegistrationIndicator");
		femalePatientsrequestAntenatalClinicInRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestAntenatalClinicInRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestAntenatalClinicInRegistrationIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestAntenatalClinic,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestAntenatalClinicInRegistrationIndicator);

		// 7.3.m Male Number of patients requested ANTENATAL CLINIC
		SqlEncounterGroupDefinition malePatientRequestAntenatalClinic = new SqlEncounterGroupDefinition();
		malePatientRequestAntenatalClinic
				.setName("malePatientRequestAntenatalClinic");
		malePatientRequestAntenatalClinic
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.ANTENATAL_CLINIC)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestAntenatalClinic.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientRequestAntenatalClinic.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestAntenatalClinic);

		EncounterIndicator malePatientsrequestAntenatalClinicInRegistrationIndicator = new EncounterIndicator();
		malePatientsrequestAntenatalClinicInRegistrationIndicator
				.setName("malePatientsrequestAntenatalClinicInRegistrationIndicator");
		malePatientsrequestAntenatalClinicInRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestAntenatalClinicInRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestAntenatalClinicInRegistrationIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestAntenatalClinic,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestAntenatalClinicInRegistrationIndicator);

		// 7.4.f Female Number of patients requested FAMILY PLANNING SERVICES
		SqlEncounterGroupDefinition femalepatientRequestFamilyPlaningServices = new SqlEncounterGroupDefinition();
		femalepatientRequestFamilyPlaningServices
				.setName("femalepatientRequestFamilyPlaningServices");
		femalepatientRequestFamilyPlaningServices
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.FAMILY_PLANNING_SERVICES)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalepatientRequestFamilyPlaningServices.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalepatientRequestFamilyPlaningServices.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalepatientRequestFamilyPlaningServices);

		EncounterIndicator femalePatientsrequestFamilyPlaningServicesRegistrationIndicator = new EncounterIndicator();
		femalePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.setName("femalePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		femalePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalepatientRequestFamilyPlaningServices,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestFamilyPlaningServicesRegistrationIndicator);

		// 7.4.m Male Number of patients requested FAMILY PLANNING SERVICES
		SqlEncounterGroupDefinition malepatientRequestFamilyPlaningServices = new SqlEncounterGroupDefinition();
		malepatientRequestFamilyPlaningServices
				.setName("malepatientRequestFamilyPlaningServices");
		malepatientRequestFamilyPlaningServices
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid( PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.FAMILY_PLANNING_SERVICES)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malepatientRequestFamilyPlaningServices.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malepatientRequestFamilyPlaningServices.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(malepatientRequestFamilyPlaningServices);

		EncounterIndicator malePatientsrequestFamilyPlaningServicesRegistrationIndicator = new EncounterIndicator();
		malePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.setName("malePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		malePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malepatientRequestFamilyPlaningServices,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestFamilyPlaningServicesRegistrationIndicator);

		// 7.5.f Female Number of patients requested MUTUELLE SERVICE

		SqlEncounterGroupDefinition femalePatientRequestMutuelleService = new SqlEncounterGroupDefinition();
		femalePatientRequestMutuelleService
				.setName("femalePatientRequestMutuelleService");
		femalePatientRequestMutuelleService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.MUTUELLE_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestMutuelleService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientRequestMutuelleService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestMutuelleService);

		EncounterIndicator femalePatientsrequestMutuelleServiceRegistrationIndicator = new EncounterIndicator();
		femalePatientsrequestMutuelleServiceRegistrationIndicator
				.setName("femalePatientsrequestMutuelleServiceRegistrationIndicator");
		femalePatientsrequestMutuelleServiceRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestMutuelleServiceRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestMutuelleServiceRegistrationIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestMutuelleService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestMutuelleServiceRegistrationIndicator);

		// 7.5.m Male Number of patients requested MUTUELLE SERVICE

		SqlEncounterGroupDefinition malePatientRequestMutuelleService = new SqlEncounterGroupDefinition();
		malePatientRequestMutuelleService
				.setName("malePatientRequestMutuelleService");
		malePatientRequestMutuelleService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.MUTUELLE_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestMutuelleService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientRequestMutuelleService.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestMutuelleService);

		EncounterIndicator malePatientsrequestMutuelleServiceRegistrationIndicator = new EncounterIndicator();
		malePatientsrequestMutuelleServiceRegistrationIndicator
				.setName("malePatientsrequestMutuelleServiceRegistrationIndicator");
		malePatientsrequestMutuelleServiceRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestMutuelleServiceRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestMutuelleServiceRegistrationIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestMutuelleService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestMutuelleServiceRegistrationIndicator);

		// 7.6.f Female Number of patients requested ACCOUNTING OFFICE SERVICE

		SqlEncounterGroupDefinition femalePatientRequestAccountingOfficeService = new SqlEncounterGroupDefinition();
		femalePatientRequestAccountingOfficeService
				.setName("femalePatientRequestAccountingOfficeService");
		femalePatientRequestAccountingOfficeService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.ACCOUNTING_OFFICE_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestAccountingOfficeService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientRequestAccountingOfficeService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestAccountingOfficeService);

		EncounterIndicator femalePatientsrequestAccountingOfficeServiceRegistrationIndicator = new EncounterIndicator();
		femalePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.setName("femalePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		femalePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestAccountingOfficeService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestAccountingOfficeServiceRegistrationIndicator);

		// 7.6.m Male Number of patients requested ACCOUNTING OFFICE SERVICE

		SqlEncounterGroupDefinition malePatientRequestAccountingOfficeService = new SqlEncounterGroupDefinition();
		malePatientRequestAccountingOfficeService
				.setName("malePatientRequestAccountingOfficeService");
		malePatientRequestAccountingOfficeService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.ACCOUNTING_OFFICE_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestAccountingOfficeService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientRequestAccountingOfficeService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestAccountingOfficeService);

		EncounterIndicator malePatientsrequestAccountingOfficeServiceRegistrationIndicator = new EncounterIndicator();
		malePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.setName("malePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		malePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestAccountingOfficeService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestAccountingOfficeServiceRegistrationIndicator);

		// 7.7.f Female Number of patients requested INTEGRATED MANAGEMENT OF
		// ADULT ILLNESS SERVICE

		SqlEncounterGroupDefinition femalePatientRequestAdultIllnessService = new SqlEncounterGroupDefinition();
		femalePatientRequestAdultIllnessService
				.setName("femalePatientRequestAdultIllnessService");
		femalePatientRequestAdultIllnessService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.INTEGRATED_MANAGEMENT_OF_ADULT_ILLNESS_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestAdultIllnessService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientRequestAdultIllnessService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestAdultIllnessService);

		EncounterIndicator femalePatientsrequestAdultIllnessServiceIndicator = new EncounterIndicator();
		femalePatientsrequestAdultIllnessServiceIndicator
				.setName("femalePatientsrequestAdultIllnessServiceIndicator");
		femalePatientsrequestAdultIllnessServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestAdultIllnessServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestAdultIllnessServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestAdultIllnessService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestAdultIllnessServiceIndicator);

		// 7.7.m Male Number of patients requested INTEGRATED MANAGEMENT OF
		// ADULT ILLNESS SERVICE
		SqlEncounterGroupDefinition malePatientRequestAdultIllnessService = new SqlEncounterGroupDefinition();
		malePatientRequestAdultIllnessService
				.setName("malePatientRequestAdultIllnessService");
		malePatientRequestAdultIllnessService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.INTEGRATED_MANAGEMENT_OF_ADULT_ILLNESS_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestAdultIllnessService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientRequestAdultIllnessService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestAdultIllnessService);

		EncounterIndicator malePatientsrequestAdultIllnessServiceIndicator = new EncounterIndicator();
		malePatientsrequestAdultIllnessServiceIndicator
				.setName("malePatientsrequestAdultIllnessServiceIndicator");
		malePatientsrequestAdultIllnessServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestAdultIllnessServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestAdultIllnessServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestAdultIllnessService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestAdultIllnessServiceIndicator);

		// 7.8.f Female Number of patients requested INTEGRATED MANAGEMENT OF
		// CHILDHOOD ILLNESS Service

		SqlEncounterGroupDefinition femalePatientRequestChildIllnessService = new SqlEncounterGroupDefinition();
		femalePatientRequestChildIllnessService
				.setName("femalePatientRequestChildIllnessService");
		femalePatientRequestChildIllnessService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.INTEGRATED_MANAGEMENT_OF_CHILDHOOD_ILLNESS)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestChildIllnessService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientRequestChildIllnessService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestChildIllnessService);

		EncounterIndicator femalePatientsrequestChildIllnessServiceIndicator = new EncounterIndicator();
		femalePatientsrequestChildIllnessServiceIndicator
				.setName("femalePatientsrequestChildIllnessServiceIndicator");
		femalePatientsrequestChildIllnessServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestChildIllnessServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestChildIllnessServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestChildIllnessService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestChildIllnessServiceIndicator);

		// 7.8.m Male Number of patients requested INTEGRATED MANAGEMENT OF
		// CHILDHOOD ILLNESS Service
		SqlEncounterGroupDefinition malePatientRequestChildIllnessService = new SqlEncounterGroupDefinition();
		malePatientRequestChildIllnessService
				.setName("malePatientRequestChildIllnessService");
		malePatientRequestChildIllnessService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.INTEGRATED_MANAGEMENT_OF_CHILDHOOD_ILLNESS)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestChildIllnessService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientRequestChildIllnessService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestChildIllnessService);

		EncounterIndicator malePatientsrequestChildIllnessServiceIndicator = new EncounterIndicator();
		malePatientsrequestChildIllnessServiceIndicator
				.setName("malePatientsrequestChildIllnessServiceIndicator");
		malePatientsrequestChildIllnessServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestChildIllnessServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestChildIllnessServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestChildIllnessService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestChildIllnessServiceIndicator);

		// 7.9.f Female Number of patients requested INFECTIOUS DISEASES CLINIC
		// SERVICE

		SqlEncounterGroupDefinition femalePatientRequestInfectiousDiseasesService = new SqlEncounterGroupDefinition();
		femalePatientRequestInfectiousDiseasesService
				.setName("femalePatientRequestInfectiousDiseasesService");
		femalePatientRequestInfectiousDiseasesService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.INFECTIOUS_DISEASES_CLINIC_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestInfectiousDiseasesService
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientRequestInfectiousDiseasesService
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestInfectiousDiseasesService);

		EncounterIndicator femalePatientsrequestInfectiousDiseasesServiceIndicator = new EncounterIndicator();
		femalePatientsrequestInfectiousDiseasesServiceIndicator
				.setName("femalePatientsrequestInfectiousDiseasesServiceIndicator");
		femalePatientsrequestInfectiousDiseasesServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestInfectiousDiseasesServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestInfectiousDiseasesServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestInfectiousDiseasesService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestInfectiousDiseasesServiceIndicator);

		// 7.9.m Male Number of patients requested INFECTIOUS DISEASES CLINIC
		// SERVICE

		SqlEncounterGroupDefinition malePatientRequestInfectiousDiseasesService = new SqlEncounterGroupDefinition();
		malePatientRequestInfectiousDiseasesService
				.setName("malePatientRequestInfectiousDiseasesService");
		malePatientRequestInfectiousDiseasesService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.INFECTIOUS_DISEASES_CLINIC_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestInfectiousDiseasesService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientRequestInfectiousDiseasesService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestInfectiousDiseasesService);

		EncounterIndicator malePatientsrequestInfectiousDiseasesServiceIndicator = new EncounterIndicator();
		malePatientsrequestInfectiousDiseasesServiceIndicator
				.setName("malePatientsrequestInfectiousDiseasesServiceIndicator");
		malePatientsrequestInfectiousDiseasesServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestInfectiousDiseasesServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestInfectiousDiseasesServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestInfectiousDiseasesService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestInfectiousDiseasesServiceIndicator);

		// 7.10.f Female Number of patients requested SOCIAL WORKER SERVICE

		SqlEncounterGroupDefinition femalePatientRequestSocialWorkerService = new SqlEncounterGroupDefinition();
		femalePatientRequestSocialWorkerService
				.setName("femalePatientRequestSocialWorkerService");
		femalePatientRequestSocialWorkerService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.SOCIAL_WORKER_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestSocialWorkerService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientRequestSocialWorkerService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestSocialWorkerService);

		EncounterIndicator femalePatientsrequestSocialWorkerServiceIndicator = new EncounterIndicator();
		femalePatientsrequestSocialWorkerServiceIndicator
				.setName("femalePatientsrequestSocialWorkerServiceIndicator");
		femalePatientsrequestSocialWorkerServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestSocialWorkerServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestSocialWorkerServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestSocialWorkerService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestSocialWorkerServiceIndicator);

		// 7.10.m Male Number of patients requested SOCIAL WORKER SERVICE
		SqlEncounterGroupDefinition malePatientRequestSocialWorkerService = new SqlEncounterGroupDefinition();
		malePatientRequestSocialWorkerService
				.setName("malePatientRequestSocialWorkerService");
		malePatientRequestSocialWorkerService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.SOCIAL_WORKER_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestSocialWorkerService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientRequestSocialWorkerService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestSocialWorkerService);

		EncounterIndicator malePatientsrequestSocialWorkerServiceIndicator = new EncounterIndicator();
		malePatientsrequestSocialWorkerServiceIndicator
				.setName("malePatientsrequestSocialWorkerServiceIndicator");
		malePatientsrequestSocialWorkerServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestSocialWorkerServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestSocialWorkerServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestSocialWorkerService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestSocialWorkerServiceIndicator);

		// 7.11.f Female Number of patients requested PREVENTION OF MOTHER TO
		// CHILD TRANSMISSION SERVICE

		SqlEncounterGroupDefinition femalePatientRequestPMTCTService = new SqlEncounterGroupDefinition();
		femalePatientRequestPMTCTService
				.setName("femalePatientRequestPMTCTService");
		femalePatientRequestPMTCTService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid( PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PREVENTION_OF_MOTHER_TO_CHILD_TRANSMISSION_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestPMTCTService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientRequestPMTCTService.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestPMTCTService);

		EncounterIndicator femalePatientsrequestPMTCTServiceIndicator = new EncounterIndicator();
		femalePatientsrequestPMTCTServiceIndicator
				.setName("femalePatientsrequestPMTCTServiceIndicator");
		femalePatientsrequestPMTCTServiceIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientsrequestPMTCTServiceIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		femalePatientsrequestPMTCTServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestPMTCTService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestPMTCTServiceIndicator);

		// 7.11.f Male Number of patients requested PREVENTION OF MOTHER TO
		// CHILD TRANSMISSION SERVICE

		SqlEncounterGroupDefinition malePatientRequestPMTCTService = new SqlEncounterGroupDefinition();
		malePatientRequestPMTCTService
				.setName("malePatientRequestPMTCTService");
		malePatientRequestPMTCTService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PREVENTION_OF_MOTHER_TO_CHILD_TRANSMISSION_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestPMTCTService.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		malePatientRequestPMTCTService.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestPMTCTService);

		EncounterIndicator malePatientsrequestPMTCTServiceIndicator = new EncounterIndicator();
		malePatientsrequestPMTCTServiceIndicator
				.setName("malePatientsrequestPMTCTServiceIndicator");
		malePatientsrequestPMTCTServiceIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientsrequestPMTCTServiceIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		malePatientsrequestPMTCTServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestPMTCTService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestPMTCTServiceIndicator);

		// 7.12.f. Female Number of patients requested LABORATORY SERVICE

		SqlEncounterGroupDefinition femalePatientRequestLabService = new SqlEncounterGroupDefinition();
		femalePatientRequestLabService
				.setName("femalePatientRequestLabService");
		femalePatientRequestLabService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.LABORATORY_SERVICES)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestLabService.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		femalePatientRequestLabService.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestLabService);

		EncounterIndicator femalePatientsrequestLabServiceIndicator = new EncounterIndicator();
		femalePatientsrequestLabServiceIndicator
				.setName("femalePatientsrequestLabServiceIndicator");
		femalePatientsrequestLabServiceIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientsrequestLabServiceIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		femalePatientsrequestLabServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestLabService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestLabServiceIndicator);

		// 7.12.m Male Number of patients requested LABORATORY SERVICE
		SqlEncounterGroupDefinition malePatientRequestLabService = new SqlEncounterGroupDefinition();
		malePatientRequestLabService.setName("malePatientRequestLabService");
		malePatientRequestLabService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.LABORATORY_SERVICES)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestLabService.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		malePatientRequestLabService.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestLabService);

		EncounterIndicator malePatientsrequestLabServiceIndicator = new EncounterIndicator();
		malePatientsrequestLabServiceIndicator
				.setName("malePatientsrequestLabServiceIndicator");
		malePatientsrequestLabServiceIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientsrequestLabServiceIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		malePatientsrequestLabServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestLabService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestLabServiceIndicator);

		// 7.13.f. Female Number of patients requested PHARMACY SERVICES

		SqlEncounterGroupDefinition femalePatientRequestPharmacyService = new SqlEncounterGroupDefinition();
		femalePatientRequestPharmacyService
				.setName("femalePatientRequestPharmacyService");
		femalePatientRequestPharmacyService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PHARMACY_SERVICES)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestPharmacyService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientRequestPharmacyService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestPharmacyService);

		EncounterIndicator femalePatientsrequestPharmacyServiceIndicator = new EncounterIndicator();
		femalePatientsrequestPharmacyServiceIndicator
				.setName("femalePatientsrequestPharmacyServiceIndicator");
		femalePatientsrequestPharmacyServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestPharmacyServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestPharmacyServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestPharmacyService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestPharmacyServiceIndicator);

		// 7.13.m Male Number of patients requested PHARMACY SERVICE

		SqlEncounterGroupDefinition malePatientRequestPharmacyService = new SqlEncounterGroupDefinition();
		malePatientRequestPharmacyService
				.setName("malePatientRequestPharmacyService");
		malePatientRequestPharmacyService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PHARMACY_SERVICES)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestPharmacyService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientRequestPharmacyService.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestPharmacyService);

		EncounterIndicator malePatientsrequestPharmacyServiceIndicator = new EncounterIndicator();
		malePatientsrequestPharmacyServiceIndicator
				.setName("malePatientsrequestPharmacyServiceIndicator");
		malePatientsrequestPharmacyServiceIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientsrequestPharmacyServiceIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		malePatientsrequestPharmacyServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestPharmacyService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestPharmacyServiceIndicator);
		// 7.14.f. Female Number of patients requested MATERNITY SERVICES

		SqlEncounterGroupDefinition femalePatientRequestMaternityService = new SqlEncounterGroupDefinition();
		femalePatientRequestMaternityService
				.setName("femalePatientRequestMaternityService");
		femalePatientRequestMaternityService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.MATERNITY_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestMaternityService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientRequestMaternityService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestMaternityService);

		EncounterIndicator femalePatientsrequestMaternityServiceIndicator = new EncounterIndicator();
		femalePatientsrequestMaternityServiceIndicator
				.setName("femalePatientsrequestMaternityServiceIndicator");
		femalePatientsrequestMaternityServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestMaternityServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestMaternityServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestMaternityService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestMaternityServiceIndicator);

		// 7.14.m Male Number of patients requested MATERNITY SERVICE

		SqlEncounterGroupDefinition malePatientRequestMaternityService = new SqlEncounterGroupDefinition();
		malePatientRequestMaternityService
				.setName("malePatientRequestMaternityService");
		malePatientRequestMaternityService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.MATERNITY_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestMaternityService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientRequestMaternityService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestMaternityService);

		EncounterIndicator malePatientsrequestMaternityServiceIndicator = new EncounterIndicator();
		malePatientsrequestMaternityServiceIndicator
				.setName("malePatientsrequestMaternityServiceIndicator");
		malePatientsrequestMaternityServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestMaternityServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestMaternityServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestMaternityService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestMaternityServiceIndicator);

		// 7.15.f Female Number of patients requested HOSPITALIZATION SERVICE

		SqlEncounterGroupDefinition femalePatientRequestHospitalizationService = new SqlEncounterGroupDefinition();
		femalePatientRequestHospitalizationService
				.setName("femalePatientRequestHospitalizationService");
		femalePatientRequestHospitalizationService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.HOSPITALIZATION_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestHospitalizationService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientRequestHospitalizationService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestHospitalizationService);

		EncounterIndicator femalePatientsrequestHospitalizationServiceIndicator = new EncounterIndicator();
		femalePatientsrequestHospitalizationServiceIndicator
				.setName("femalePatientsrequestHospitalizationServiceIndicator");
		femalePatientsrequestHospitalizationServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestHospitalizationServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestHospitalizationServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestHospitalizationService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestHospitalizationServiceIndicator);

		// 7.15.m Male Number of patients requested HOSPITALIZATION SERVICE

		SqlEncounterGroupDefinition malePatientRequestHospitalizationService = new SqlEncounterGroupDefinition();
		malePatientRequestHospitalizationService
				.setName("malePatientRequestHospitalizationService");
		malePatientRequestHospitalizationService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.HOSPITALIZATION_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestHospitalizationService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientRequestHospitalizationService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestHospitalizationService);

		EncounterIndicator malePatientsrequestHospitalizationServiceIndicator = new EncounterIndicator();
		malePatientsrequestHospitalizationServiceIndicator
				.setName("malePatientsrequestHospitalizationServiceIndicator");
		malePatientsrequestHospitalizationServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestHospitalizationServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestHospitalizationServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestHospitalizationService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestHospitalizationServiceIndicator);

		// 7.16.f Female Number of patients requested VACCINATION SERVICE

		SqlEncounterGroupDefinition femalePatientRequestVaccinationService = new SqlEncounterGroupDefinition();
		femalePatientRequestVaccinationService
				.setName("femalePatientRequestVaccinationService");
		femalePatientRequestVaccinationService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='F' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.VACCINATION_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		femalePatientRequestVaccinationService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientRequestVaccinationService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(femalePatientRequestVaccinationService);

		EncounterIndicator femalePatientsrequestVaccinationServiceIndicator = new EncounterIndicator();
		femalePatientsrequestVaccinationServiceIndicator
				.setName("femalePatientsrequestVaccinationServiceIndicator");
		femalePatientsrequestVaccinationServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestVaccinationServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestVaccinationServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						femalePatientRequestVaccinationService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestVaccinationServiceIndicator);

		// 7.16.m Male Number of patients requested VACCINATION SERVICE

		SqlEncounterGroupDefinition malePatientRequestVaccinationService = new SqlEncounterGroupDefinition();
		malePatientRequestVaccinationService
				.setName("malePatientRequestVaccinationService");
		malePatientRequestVaccinationService
				.setQuery("select distinct e.encounter_id, e.patient_id from encounter e,obs o,person p where e.patient_id=p.person_id and e.patient_id=o.person_id and p.gender='M' and o.concept_id="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED)
						+ " and o.value_coded="
						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.VACCINATION_SERVICE)
						+ " and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.encounter_type = "
						+ registrationEncTypeId
						+ " and e.voided = 0 and o.voided = 0 and p.voided = 0 and e.location_id = :location");
		malePatientRequestVaccinationService.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientRequestVaccinationService.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceEncounterGroupDefinition(malePatientRequestVaccinationService);

		EncounterIndicator malePatientsrequestVaccinationServiceIndicator = new EncounterIndicator();
		malePatientsrequestVaccinationServiceIndicator
				.setName("malePatientsrequestVaccinationServiceIndicator");
		malePatientsrequestVaccinationServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestVaccinationServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestVaccinationServiceIndicator
				.setEncounterGroupDefinition(new Mapped<SqlEncounterGroupDefinition>(
						malePatientRequestVaccinationService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestVaccinationServiceIndicator);

		/*
		 * SqlCohortDefinition femalePatientsrequestPrimCare=new
		 * SqlCohortDefinition
		 * ("SELECT o.person_id FROM obs o,person p where o.concept_id="
		 * +PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED_ID+
		 * " and p.gender='F' and o.person_id=p.person_id and o.voided=0");
		 * femalePatientsrequestPrimCare
		 * .setName("femalePatientsrequestPrimCare");
		 * h.replaceCohortDefinition(femalePatientsrequestPrimCare);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestPrimCareInRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestPrimCareInRegistration
		 * .setName("femalePatientsrequestPrimCareInRegistration");
		 * femalePatientsrequestPrimCareInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestPrimCareInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestPrimCareInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestPrimCareInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestPrimCareInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestPrimCareInRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestPrimCareInRegistration.setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare"
		 * );
		 * h.replaceCohortDefinition(femalePatientsrequestPrimCareInRegistration
		 * );
		 * 
		 * CohortIndicator femalePatientsrequestPrimCareInRegistrationIndicator
		 * = new CohortIndicator();
		 * femalePatientsrequestPrimCareInRegistrationIndicator
		 * .setName("femalePatientsrequestPrimCareInRegistrationIndicator");
		 * femalePatientsrequestPrimCareInRegistrationIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestPrimCareInRegistrationIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestPrimCareInRegistrationIndicator
		 * .setCohortDefinition(new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCareInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(femalePatientsrequestPrimCareInRegistrationIndicator
		 * ); //7.1.m Female Total number of patient requested primary care
		 * malePatientsrequestPrimCare
		 * malePatientsrequestPrimCareInRegistrationIndicator
		 * SqlCohortDefinition malePatientsrequestPrimCare=new
		 * SqlCohortDefinition
		 * ("SELECT o.person_id FROM obs o,person p where o.concept_id="
		 * +PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED_ID+
		 * " and p.gender='M' and o.person_id=p.person_id and o.voided=0");
		 * malePatientsrequestPrimCare.setName("malePatientsrequestPrimCare");
		 * h.replaceCohortDefinition(malePatientsrequestPrimCare);
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestPrimCareInRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestPrimCareInRegistration
		 * .setName("malePatientsrequestPrimCareInRegistration");
		 * malePatientsrequestPrimCareInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestPrimCareInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestPrimCareInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestPrimCareInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestPrimCareInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestPrimCareInRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestPrimCareInRegistration.setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare"
		 * );
		 * h.replaceCohortDefinition(malePatientsrequestPrimCareInRegistration);
		 * 
		 * CohortIndicator malePatientsrequestPrimCareInRegistrationIndicator =
		 * new CohortIndicator();
		 * malePatientsrequestPrimCareInRegistrationIndicator
		 * .setName("malePatientsrequestPrimCareInRegistrationIndicator");
		 * malePatientsrequestPrimCareInRegistrationIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestPrimCareInRegistrationIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestPrimCareInRegistrationIndicator
		 * .setCohortDefinition(new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCareInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(malePatientsrequestPrimCareInRegistrationIndicator
		 * ); //7.2.f Female Number of patients requested VCT PROGRAM
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestVCTProgram=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants.VCT_PROGRAM, SetComparator.IN,
		 * TimeModifier.ANY);
		 * patientRequestVCTProgram.setName("patientRequestVCTProgram");
		 * patientRequestVCTProgram.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestVCTProgram.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestVCTProgram);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestVCTProgramInRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestVCTProgramInRegistration
		 * .setName("femalePatientsrequestVCTProgramInRegistration");
		 * femalePatientsrequestVCTProgramInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestVCTProgramInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestVCTProgramInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestVCTProgramInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestVCTProgramInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestVCTProgramInRegistration
		 * .getSearches().put("patientRequestVCTProgram", new
		 * Mapped<CohortDefinition
		 * >(patientRequestVCTProgram,ParameterizableUtil.
		 * createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestVCTProgramInRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestVCTProgramInRegistration.setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestVCTProgram"
		 * );
		 * h.replaceCohortDefinition(femalePatientsrequestVCTProgramInRegistration
		 * );
		 * 
		 * CohortIndicator
		 * femalePatientsrequestVCTProgramInRegistrationIndicator = new
		 * CohortIndicator();
		 * femalePatientsrequestVCTProgramInRegistrationIndicator
		 * .setName("femalePatientsrequestVCTProgramInRegistrationIndicator");
		 * femalePatientsrequestVCTProgramInRegistrationIndicator
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestVCTProgramInRegistrationIndicator
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestVCTProgramInRegistrationIndicator
		 * .setCohortDefinition(new
		 * Mapped<CohortDefinition>(femalePatientsrequestVCTProgramInRegistration
		 * ,ParameterizableUtil.createParameterMappings(
		 * "onOrAfter=${startDate},onOrBefore=${endDate}")));
		 * h.replaceDefinition
		 * (femalePatientsrequestVCTProgramInRegistrationIndicator);
		 * 
		 * 
		 * //7.2.m Male Number of patients requested VCT PROGRAM
		 * CompositionCohortDefinition
		 * malePatientsrequestVCTProgramInRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestVCTProgramInRegistration
		 * .setName("malePatientsrequestVCTProgramInRegistration");
		 * malePatientsrequestVCTProgramInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestVCTProgramInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestVCTProgramInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestVCTProgramInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestVCTProgramInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestVCTProgramInRegistration
		 * .getSearches().put("patientRequestVCTProgram", new
		 * Mapped<CohortDefinition
		 * >(patientRequestVCTProgram,ParameterizableUtil.
		 * createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestVCTProgramInRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestVCTProgramInRegistration.setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestVCTProgram"
		 * );
		 * h.replaceCohortDefinition(malePatientsrequestVCTProgramInRegistration
		 * );
		 * 
		 * CohortIndicator malePatientsrequestVCTProgramInRegistrationIndicator
		 * = new CohortIndicator();
		 * malePatientsrequestVCTProgramInRegistrationIndicator
		 * .setName("malePatientsrequestVCTProgramInRegistrationIndicator");
		 * malePatientsrequestVCTProgramInRegistrationIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestVCTProgramInRegistrationIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestVCTProgramInRegistrationIndicator
		 * .setCohortDefinition(new
		 * Mapped<CohortDefinition>(malePatientsrequestVCTProgramInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(malePatientsrequestVCTProgramInRegistrationIndicator
		 * );
		 * 
		 * // 7.3.f Female Number of patients requested ANTENATAL CLINIC
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestAntenatalClinic=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants.ANTENATAL_CLINIC, SetComparator.IN,
		 * TimeModifier.ANY);
		 * patientRequestAntenatalClinic.setName("patientRequestAntenatalClinic"
		 * ); patientRequestAntenatalClinic.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestAntenatalClinic.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestAntenatalClinic);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestAntenatalClinicInRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestAntenatalClinicInRegistration
		 * .setName("femalePatientsrequestAntenatalClinicInRegistration");
		 * femalePatientsrequestAntenatalClinicInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestAntenatalClinicInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestAntenatalClinicInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestAntenatalClinicInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestAntenatalClinicInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestAntenatalClinicInRegistration
		 * .getSearches().put("patientRequestAntenatalClinic", new
		 * Mapped<CohortDefinition
		 * >(patientRequestAntenatalClinic,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestAntenatalClinicInRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestAntenatalClinicInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestAntenatalClinic"
		 * ); h.replaceCohortDefinition(
		 * femalePatientsrequestAntenatalClinicInRegistration);
		 * 
		 * CohortIndicator
		 * femalePatientsrequestAntenatalClinicInRegistrationIndicator = new
		 * CohortIndicator();
		 * femalePatientsrequestAntenatalClinicInRegistrationIndicator
		 * .setName("femalePatientsrequestAntenatalClinicInRegistrationIndicator"
		 * );
		 * femalePatientsrequestAntenatalClinicInRegistrationIndicator.addParameter
		 * (new Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestAntenatalClinicInRegistrationIndicator
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestAntenatalClinicInRegistrationIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * femalePatientsrequestAntenatalClinicInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * ))); h.replaceDefinition(
		 * femalePatientsrequestAntenatalClinicInRegistrationIndicator);
		 * 
		 * // 7.3.m Male Number of patients requested ANTENATAL CLINIC
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestAntenatalClinicInRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestAntenatalClinicInRegistration
		 * .setName("malePatientsrequestAntenatalClinicInRegistration");
		 * malePatientsrequestAntenatalClinicInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestAntenatalClinicInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestAntenatalClinicInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestAntenatalClinicInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestAntenatalClinicInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestAntenatalClinicInRegistration
		 * .getSearches().put("patientRequestAntenatalClinic", new
		 * Mapped<CohortDefinition
		 * >(patientRequestAntenatalClinic,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestAntenatalClinicInRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestAntenatalClinicInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestAntenatalClinic"
		 * ); h.replaceCohortDefinition(
		 * malePatientsrequestAntenatalClinicInRegistration);
		 * 
		 * CohortIndicator
		 * malePatientsrequestAntenatalClinicInRegistrationIndicator = new
		 * CohortIndicator();
		 * malePatientsrequestAntenatalClinicInRegistrationIndicator
		 * .setName("malePatientsrequestAntenatalClinicInRegistrationIndicator"
		 * );
		 * malePatientsrequestAntenatalClinicInRegistrationIndicator.addParameter
		 * (new Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestAntenatalClinicInRegistrationIndicator
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestAntenatalClinicInRegistrationIndicator
		 * .setCohortDefinition(new
		 * Mapped<CohortDefinition>(malePatientsrequestAntenatalClinicInRegistration
		 * ,ParameterizableUtil.createParameterMappings(
		 * "onOrAfter=${startDate},onOrBefore=${endDate}")));
		 * h.replaceDefinition
		 * (malePatientsrequestAntenatalClinicInRegistrationIndicator);
		 * 
		 * // 7.4.f Female Number of patients requested FAMILY PLANNING SERVICES
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestFamilyPlaningServices=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants.FAMILY_PLANNING_SERVICES,
		 * SetComparator.IN, TimeModifier.ANY);
		 * patientRequestFamilyPlaningServices
		 * .setName("patientRequestFamilyPlaningServices");
		 * patientRequestFamilyPlaningServices.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestFamilyPlaningServices.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestFamilyPlaningServices);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestFamilyPlaningServicesRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestFamilyPlaningServicesRegistration
		 * .setName("femalePatientsrequestFamilyPlaningServicesRegistration");
		 * femalePatientsrequestFamilyPlaningServicesRegistration
		 * .addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestFamilyPlaningServicesRegistration
		 * .addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestFamilyPlaningServicesRegistration
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestFamilyPlaningServicesRegistration
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestFamilyPlaningServicesRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestFamilyPlaningServicesRegistration
		 * .getSearches().put("patientRequestFamilyPlaningServices", new
		 * Mapped<CohortDefinition
		 * >(patientRequestFamilyPlaningServices,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestFamilyPlaningServicesRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestFamilyPlaningServicesRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestFamilyPlaningServices"
		 * ); h.replaceCohortDefinition(
		 * femalePatientsrequestFamilyPlaningServicesRegistration);
		 * 
		 * CohortIndicator
		 * femalePatientsrequestFamilyPlaningServicesRegistrationIndicator = new
		 * CohortIndicator();
		 * femalePatientsrequestFamilyPlaningServicesRegistrationIndicator
		 * .setName
		 * ("femalePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		 * femalePatientsrequestFamilyPlaningServicesRegistrationIndicator
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestFamilyPlaningServicesRegistrationIndicator
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestFamilyPlaningServicesRegistrationIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * femalePatientsrequestFamilyPlaningServicesRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * ))); h.replaceDefinition(
		 * femalePatientsrequestFamilyPlaningServicesRegistrationIndicator); //
		 * 7.4.m Male Number of patients requested FAMILY PLANNING SERVICES
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestFamilyPlaningServicesRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestFamilyPlaningServicesRegistration
		 * .setName("malePatientsrequestFamilyPlaningServicesRegistration");
		 * malePatientsrequestFamilyPlaningServicesRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestFamilyPlaningServicesRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestFamilyPlaningServicesRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestFamilyPlaningServicesRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestFamilyPlaningServicesRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestFamilyPlaningServicesRegistration
		 * .getSearches().put("patientRequestFamilyPlaningServices", new
		 * Mapped<CohortDefinition
		 * >(patientRequestFamilyPlaningServices,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestFamilyPlaningServicesRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestFamilyPlaningServicesRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestFamilyPlaningServices"
		 * ); h.replaceCohortDefinition(
		 * malePatientsrequestFamilyPlaningServicesRegistration);
		 * 
		 * CohortIndicator
		 * malePatientsrequestFamilyPlaningServicesRegistrationIndicator = new
		 * CohortIndicator();
		 * malePatientsrequestFamilyPlaningServicesRegistrationIndicator
		 * .setName(
		 * "malePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		 * malePatientsrequestFamilyPlaningServicesRegistrationIndicator
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestFamilyPlaningServicesRegistrationIndicator
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestFamilyPlaningServicesRegistrationIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * malePatientsrequestFamilyPlaningServicesRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * ))); h.replaceDefinition(
		 * malePatientsrequestFamilyPlaningServicesRegistrationIndicator); //
		 * 7.5.f Female Number of patients requested MUTUELLE SERVICE
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestMutuelleService=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants.MUTUELLE_SERVICE, SetComparator.IN,
		 * TimeModifier.ANY);
		 * patientRequestMutuelleService.setName("patientRequestMutuelleService"
		 * ); patientRequestMutuelleService.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestMutuelleService.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestMutuelleService);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestMutuelleServiceRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestMutuelleServiceRegistration
		 * .setName("femalePatientsrequestMutuelleServiceRegistration");
		 * femalePatientsrequestMutuelleServiceRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestMutuelleServiceRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestMutuelleServiceRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestMutuelleServiceRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestMutuelleServiceRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestMutuelleServiceRegistration
		 * .getSearches().put("patientRequestMutuelleService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestMutuelleService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestMutuelleServiceRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestMutuelleServiceRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestMutuelleService"
		 * ); h.replaceCohortDefinition(
		 * femalePatientsrequestMutuelleServiceRegistration);
		 * 
		 * CohortIndicator
		 * femalePatientsrequestMutuelleServiceRegistrationIndicator = new
		 * CohortIndicator();
		 * femalePatientsrequestMutuelleServiceRegistrationIndicator
		 * .setName("femalePatientsrequestMutuelleServiceRegistrationIndicator"
		 * );
		 * femalePatientsrequestMutuelleServiceRegistrationIndicator.addParameter
		 * (new Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestMutuelleServiceRegistrationIndicator
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestMutuelleServiceRegistrationIndicator
		 * .setCohortDefinition(new
		 * Mapped<CohortDefinition>(femalePatientsrequestMutuelleServiceRegistration
		 * ,ParameterizableUtil.createParameterMappings(
		 * "onOrAfter=${startDate},onOrBefore=${endDate}")));
		 * h.replaceDefinition
		 * (femalePatientsrequestMutuelleServiceRegistrationIndicator); // 7.5.m
		 * Male Number of patients requested MUTUELLE SERVICE
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestMutuelleServiceRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestMutuelleServiceRegistration
		 * .setName("malePatientsrequestMutuelleServiceRegistration");
		 * malePatientsrequestMutuelleServiceRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestMutuelleServiceRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestMutuelleServiceRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestMutuelleServiceRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestMutuelleServiceRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestMutuelleServiceRegistration
		 * .getSearches().put("patientRequestMutuelleService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestMutuelleService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestMutuelleServiceRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestMutuelleServiceRegistration.setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestMutuelleService"
		 * );
		 * h.replaceCohortDefinition(malePatientsrequestMutuelleServiceRegistration
		 * );
		 * 
		 * CohortIndicator
		 * malePatientsrequestMutuelleServiceRegistrationIndicator = new
		 * CohortIndicator();
		 * malePatientsrequestMutuelleServiceRegistrationIndicator
		 * .setName("malePatientsrequestMutuelleServiceRegistrationIndicator");
		 * malePatientsrequestMutuelleServiceRegistrationIndicator
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestMutuelleServiceRegistrationIndicator
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestMutuelleServiceRegistrationIndicator
		 * .setCohortDefinition(new
		 * Mapped<CohortDefinition>(malePatientsrequestMutuelleServiceRegistration
		 * ,ParameterizableUtil.createParameterMappings(
		 * "onOrAfter=${startDate},onOrBefore=${endDate}")));
		 * h.replaceDefinition
		 * (malePatientsrequestMutuelleServiceRegistrationIndicator); // 7.6.f
		 * Female Number of patients requested ACCOUNTING OFFICE SERVICE
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestAccountingOfficeService=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants.ACCOUNTING_OFFICE_SERVICE,
		 * SetComparator.IN, TimeModifier.ANY);
		 * patientRequestAccountingOfficeService
		 * .setName("patientRequestAccountingOfficeService");
		 * patientRequestAccountingOfficeService.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestAccountingOfficeService.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestAccountingOfficeService);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestAccountingOfficeServiceRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestAccountingOfficeServiceRegistration
		 * .setName("femalePatientsrequestAccountingOfficeServiceRegistration");
		 * femalePatientsrequestAccountingOfficeServiceRegistration
		 * .addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestAccountingOfficeServiceRegistration
		 * .addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestAccountingOfficeServiceRegistration
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestAccountingOfficeServiceRegistration
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestAccountingOfficeServiceRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestAccountingOfficeServiceRegistration
		 * .getSearches().put("patientRequestAccountingOfficeService", new
		 * Mapped<CohortDefinition>(patientRequestAccountingOfficeService,
		 * ParameterizableUtil.createParameterMappings(
		 * "onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestAccountingOfficeServiceRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestAccountingOfficeServiceRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestAccountingOfficeService"
		 * ); h.replaceCohortDefinition(
		 * femalePatientsrequestAccountingOfficeServiceRegistration);
		 * 
		 * CohortIndicator
		 * femalePatientsrequestAccountingOfficeServiceRegistrationIndicator =
		 * new CohortIndicator();
		 * femalePatientsrequestAccountingOfficeServiceRegistrationIndicator
		 * .setName
		 * ("femalePatientsrequestAccountingOfficeServiceRegistrationIndicator"
		 * ); femalePatientsrequestAccountingOfficeServiceRegistrationIndicator.
		 * addParameter(new Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestAccountingOfficeServiceRegistrationIndicator
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestAccountingOfficeServiceRegistrationIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * femalePatientsrequestAccountingOfficeServiceRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * ))); h.replaceDefinition(
		 * femalePatientsrequestAccountingOfficeServiceRegistrationIndicator);
		 * // 7.6.m Male Number of patients requested ACCOUNTING OFFICE SERVICE
		 * CompositionCohortDefinition
		 * malePatientsrequestAccountingOfficeServiceRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestAccountingOfficeServiceRegistration
		 * .setName("malePatientsrequestAccountingOfficeServiceRegistration");
		 * malePatientsrequestAccountingOfficeServiceRegistration
		 * .addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestAccountingOfficeServiceRegistration
		 * .addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestAccountingOfficeServiceRegistration
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestAccountingOfficeServiceRegistration
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestAccountingOfficeServiceRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestAccountingOfficeServiceRegistration
		 * .getSearches().put("patientRequestAccountingOfficeService", new
		 * Mapped<CohortDefinition>(patientRequestAccountingOfficeService,
		 * ParameterizableUtil.createParameterMappings(
		 * "onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestAccountingOfficeServiceRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestAccountingOfficeServiceRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestAccountingOfficeService"
		 * ); h.replaceCohortDefinition(
		 * malePatientsrequestAccountingOfficeServiceRegistration);
		 * 
		 * CohortIndicator
		 * malePatientsrequestAccountingOfficeServiceRegistrationIndicator = new
		 * CohortIndicator();
		 * malePatientsrequestAccountingOfficeServiceRegistrationIndicator
		 * .setName
		 * ("malePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		 * malePatientsrequestAccountingOfficeServiceRegistrationIndicator
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestAccountingOfficeServiceRegistrationIndicator
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestAccountingOfficeServiceRegistrationIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * malePatientsrequestAccountingOfficeServiceRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * ))); h.replaceDefinition(
		 * malePatientsrequestAccountingOfficeServiceRegistrationIndicator);
		 * 
		 * // 7.7.f Female Number of patients requested INTEGRATED MANAGEMENT OF
		 * ADULT ILLNESS SERVICE
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestAdultIllnessService=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants
		 * .INTEGRATED_MANAGEMENT_OF_ADULT_ILLNESS_SERVICE, SetComparator.IN,
		 * TimeModifier.ANY); patientRequestAdultIllnessService.setName(
		 * "patientRequestAdultIllnessService");
		 * patientRequestAdultIllnessService.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestAdultIllnessService.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestAdultIllnessService);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestAdultIllnessServiceRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestAdultIllnessServiceRegistration
		 * .setName("femalePatientsrequestAdultIllnessServiceRegistration");
		 * femalePatientsrequestAdultIllnessServiceRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestAdultIllnessServiceRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestAdultIllnessServiceRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestAdultIllnessServiceRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestAdultIllnessServiceRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestAdultIllnessServiceRegistration
		 * .getSearches().put("patientRequestAdultIllnessService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestAdultIllnessService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestAdultIllnessServiceRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestAdultIllnessServiceRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestAdultIllnessService"
		 * ); h.replaceCohortDefinition(
		 * femalePatientsrequestAdultIllnessServiceRegistration);
		 * 
		 * CohortIndicator femalePatientsrequestAdultIllnessServiceIndicator =
		 * new CohortIndicator();
		 * femalePatientsrequestAdultIllnessServiceIndicator
		 * .setName("femalePatientsrequestAdultIllnessServiceIndicator");
		 * femalePatientsrequestAdultIllnessServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestAdultIllnessServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestAdultIllnessServiceIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * femalePatientsrequestAdultIllnessServiceRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(femalePatientsrequestAdultIllnessServiceIndicator
		 * );
		 * 
		 * // 7.7.m Male Number of patients requested INTEGRATED MANAGEMENT OF
		 * ADULT ILLNESS SERVICE
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestAdultIllnessServiceRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestAdultIllnessServiceRegistration
		 * .setName("malePatientsrequestAdultIllnessServiceRegistration");
		 * malePatientsrequestAdultIllnessServiceRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestAdultIllnessServiceRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestAdultIllnessServiceRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestAdultIllnessServiceRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestAdultIllnessServiceRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestAdultIllnessServiceRegistration
		 * .getSearches().put("patientRequestAdultIllnessService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestAdultIllnessService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestAdultIllnessServiceRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestAdultIllnessServiceRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestAdultIllnessService"
		 * ); h.replaceCohortDefinition(
		 * malePatientsrequestAdultIllnessServiceRegistration);
		 * 
		 * CohortIndicator malePatientsrequestAdultIllnessServiceIndicator = new
		 * CohortIndicator();
		 * malePatientsrequestAdultIllnessServiceIndicator.setName
		 * ("malePatientsrequestAdultIllnessServiceIndicator");
		 * malePatientsrequestAdultIllnessServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestAdultIllnessServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestAdultIllnessServiceIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * malePatientsrequestAdultIllnessServiceRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(malePatientsrequestAdultIllnessServiceIndicator);
		 * // 7.8.f Female Number of patients requested INTEGRATED MANAGEMENT OF
		 * CHILDHOOD ILLNESS Service
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestChildIllnessService=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants
		 * .INTEGRATED_MANAGEMENT_OF_CHILDHOOD_ILLNESS, SetComparator.IN,
		 * TimeModifier.ANY); patientRequestChildIllnessService.setName(
		 * "patientRequestChildIllnessService");
		 * patientRequestChildIllnessService.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestChildIllnessService.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestChildIllnessService);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestChildIllnessServiceRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestChildIllnessServiceRegistration
		 * .setName("femalePatientsrequestChildIllnessServiceRegistration");
		 * femalePatientsrequestChildIllnessServiceRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestChildIllnessServiceRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestChildIllnessServiceRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestChildIllnessServiceRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestChildIllnessServiceRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestChildIllnessServiceRegistration
		 * .getSearches().put("patientRequestChildIllnessService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestChildIllnessService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestChildIllnessServiceRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestChildIllnessServiceRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestChildIllnessService"
		 * ); h.replaceCohortDefinition(
		 * femalePatientsrequestChildIllnessServiceRegistration);
		 * 
		 * CohortIndicator femalePatientsrequestChildIllnessServiceIndicator =
		 * new CohortIndicator();
		 * femalePatientsrequestChildIllnessServiceIndicator
		 * .setName("femalePatientsrequestChildIllnessServiceIndicator");
		 * femalePatientsrequestChildIllnessServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestChildIllnessServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestChildIllnessServiceIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * femalePatientsrequestChildIllnessServiceRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(femalePatientsrequestChildIllnessServiceIndicator
		 * ); // 7.8.m Male Number of patients requested INTEGRATED MANAGEMENT
		 * OF CHILDHOOD ILLNESS Service
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestChildIllnessServiceRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestChildIllnessServiceRegistration
		 * .setName("malePatientsrequestChildIllnessServiceRegistration");
		 * malePatientsrequestChildIllnessServiceRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestChildIllnessServiceRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestChildIllnessServiceRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestChildIllnessServiceRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestChildIllnessServiceRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestChildIllnessServiceRegistration
		 * .getSearches().put("patientRequestChildIllnessService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestChildIllnessService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestChildIllnessServiceRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestChildIllnessServiceRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestChildIllnessService"
		 * ); h.replaceCohortDefinition(
		 * malePatientsrequestChildIllnessServiceRegistration);
		 * 
		 * CohortIndicator malePatientsrequestChildIllnessServiceIndicator = new
		 * CohortIndicator();
		 * malePatientsrequestChildIllnessServiceIndicator.setName
		 * ("malePatientsrequestChildIllnessServiceIndicator");
		 * malePatientsrequestChildIllnessServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestChildIllnessServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestChildIllnessServiceIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * malePatientsrequestChildIllnessServiceRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(malePatientsrequestChildIllnessServiceIndicator);
		 * // 7.9.f Female Number of patients requested INFECTIOUS DISEASES
		 * CLINIC SERVICE
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestInfectiousDiseasesService=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants.INFECTIOUS_DISEASES_CLINIC_SERVICE,
		 * SetComparator.IN, TimeModifier.ANY);
		 * patientRequestInfectiousDiseasesService
		 * .setName("patientRequestInfectiousDiseasesService");
		 * patientRequestInfectiousDiseasesService.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestInfectiousDiseasesService.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestInfectiousDiseasesService);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestInfectiousDiseasesServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .setName(
		 * "femalePatientsrequestInfectiousDiseasesServiceInRegistration" );
		 * femalePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .addParameter (new Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .getSearches().put("patientRequestInfectiousDiseasesService", new
		 * Mapped<CohortDefinition>(patientRequestInfectiousDiseasesService,
		 * ParameterizableUtil.createParameterMappings(
		 * "onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestInfectiousDiseasesService"
		 * ); h.replaceCohortDefinition(
		 * femalePatientsrequestInfectiousDiseasesServiceInRegistration);
		 * 
		 * CohortIndicator
		 * femalePatientsrequestInfectiousDiseasesServiceIndicator = new
		 * CohortIndicator();
		 * femalePatientsrequestInfectiousDiseasesServiceIndicator
		 * .setName("femalePatientsrequestInfectiousDiseasesServiceIndicator");
		 * femalePatientsrequestInfectiousDiseasesServiceIndicator
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestInfectiousDiseasesServiceIndicator
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestInfectiousDiseasesServiceIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * femalePatientsrequestInfectiousDiseasesServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * ))); h.replaceDefinition(
		 * femalePatientsrequestInfectiousDiseasesServiceIndicator); // 7.9.m
		 * Male Number of patients requested INFECTIOUS DISEASES CLINIC SERVICE
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestInfectiousDiseasesServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .setName("malePatientsrequestInfectiousDiseasesServiceInRegistration"
		 * );
		 * malePatientsrequestInfectiousDiseasesServiceInRegistration.addParameter
		 * (new Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .getSearches().put("patientRequestInfectiousDiseasesService", new
		 * Mapped<CohortDefinition>(patientRequestInfectiousDiseasesService,
		 * ParameterizableUtil.createParameterMappings(
		 * "onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestInfectiousDiseasesServiceInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestInfectiousDiseasesService"
		 * ); h.replaceCohortDefinition(
		 * malePatientsrequestInfectiousDiseasesServiceInRegistration);
		 * 
		 * CohortIndicator malePatientsrequestInfectiousDiseasesServiceIndicator
		 * = new CohortIndicator();
		 * malePatientsrequestInfectiousDiseasesServiceIndicator
		 * .setName("malePatientsrequestInfectiousDiseasesServiceIndicator");
		 * malePatientsrequestInfectiousDiseasesServiceIndicator
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestInfectiousDiseasesServiceIndicator
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestInfectiousDiseasesServiceIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * malePatientsrequestInfectiousDiseasesServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(malePatientsrequestInfectiousDiseasesServiceIndicator
		 * ); // 7.10.f Female Number of patients requested SOCIAL WORKER
		 * SERVICE
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestSocialWorkerService=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants.SOCIAL_WORKER_SERVICE, SetComparator.IN,
		 * TimeModifier.ANY); patientRequestSocialWorkerService.setName(
		 * "patientRequestSocialWorkerService");
		 * patientRequestSocialWorkerService.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestSocialWorkerService.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestSocialWorkerService);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestSocialWorkerServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestSocialWorkerServiceInRegistration
		 * .setName("femalePatientsrequestSocialWorkerServiceInRegistration");
		 * femalePatientsrequestSocialWorkerServiceInRegistration
		 * .addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestSocialWorkerServiceInRegistration
		 * .addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestSocialWorkerServiceInRegistration
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestSocialWorkerServiceInRegistration
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestSocialWorkerServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestSocialWorkerServiceInRegistration
		 * .getSearches().put("patientRequestSocialWorkerService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestSocialWorkerService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestSocialWorkerServiceInRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestSocialWorkerServiceInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestSocialWorkerService"
		 * ); h.replaceCohortDefinition(
		 * femalePatientsrequestSocialWorkerServiceInRegistration);
		 * 
		 * CohortIndicator femalePatientsrequestSocialWorkerServiceIndicator =
		 * new CohortIndicator();
		 * femalePatientsrequestSocialWorkerServiceIndicator
		 * .setName("femalePatientsrequestSocialWorkerServiceIndicator");
		 * femalePatientsrequestSocialWorkerServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestSocialWorkerServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestSocialWorkerServiceIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * femalePatientsrequestSocialWorkerServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(femalePatientsrequestSocialWorkerServiceIndicator
		 * ); // 7.10.f Male Number of patients requested SOCIAL WORKER SERVICE
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestSocialWorkerServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestSocialWorkerServiceInRegistration
		 * .setName("malePatientsrequestSocialWorkerServiceInRegistration");
		 * malePatientsrequestSocialWorkerServiceInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestSocialWorkerServiceInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestSocialWorkerServiceInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestSocialWorkerServiceInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestSocialWorkerServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestSocialWorkerServiceInRegistration
		 * .getSearches().put("patientRequestSocialWorkerService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestSocialWorkerService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestSocialWorkerServiceInRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestSocialWorkerServiceInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestSocialWorkerService"
		 * ); h.replaceCohortDefinition(
		 * malePatientsrequestSocialWorkerServiceInRegistration);
		 * 
		 * CohortIndicator malePatientsrequestSocialWorkerServiceIndicator = new
		 * CohortIndicator();
		 * malePatientsrequestSocialWorkerServiceIndicator.setName
		 * ("malePatientsrequestSocialWorkerServiceIndicator");
		 * malePatientsrequestSocialWorkerServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestSocialWorkerServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestSocialWorkerServiceIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * malePatientsrequestSocialWorkerServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(malePatientsrequestSocialWorkerServiceIndicator);
		 * 
		 * // 7.11.f Female Number of patients requested PREVENTION OF MOTHER TO
		 * CHILD TRANSMISSION SERVICE
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestPMTCTService=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants
		 * .PREVENTION_OF_MOTHER_TO_CHILD_TRANSMISSION_SERVICE,
		 * SetComparator.IN, TimeModifier.ANY);
		 * patientRequestPMTCTService.setName("patientRequestPMTCTService");
		 * patientRequestPMTCTService.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestPMTCTService.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestPMTCTService);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestPMTCTServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestPMTCTServiceInRegistration
		 * .setName("femalePatientsrequestPMTCTServiceInRegistration");
		 * femalePatientsrequestPMTCTServiceInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestPMTCTServiceInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestPMTCTServiceInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestPMTCTServiceInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestPMTCTServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestPMTCTServiceInRegistration
		 * .getSearches().put("patientRequestPMTCTService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestPMTCTService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestPMTCTServiceInRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestPMTCTServiceInRegistration.setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestPMTCTService"
		 * );
		 * h.replaceCohortDefinition(femalePatientsrequestPMTCTServiceInRegistration
		 * );
		 * 
		 * CohortIndicator femalePatientsrequestPMTCTServiceIndicator = new
		 * CohortIndicator();
		 * femalePatientsrequestPMTCTServiceIndicator.setName(
		 * "femalePatientsrequestPMTCTServiceIndicator");
		 * femalePatientsrequestPMTCTServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestPMTCTServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestPMTCTServiceIndicator.setCohortDefinition(new
		 * Mapped
		 * <CohortDefinition>(femalePatientsrequestPMTCTServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * ))); h.replaceDefinition(femalePatientsrequestPMTCTServiceIndicator);
		 * 
		 * // 7.11.f Male Number of patients requested PREVENTION OF MOTHER TO
		 * CHILD TRANSMISSION SERVICE
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestPMTCTServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestPMTCTServiceInRegistration
		 * .setName("malePatientsrequestPMTCTServiceInRegistration");
		 * malePatientsrequestPMTCTServiceInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestPMTCTServiceInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestPMTCTServiceInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestPMTCTServiceInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestPMTCTServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestPMTCTServiceInRegistration
		 * .getSearches().put("patientRequestPMTCTService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestPMTCTService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestPMTCTServiceInRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestPMTCTServiceInRegistration.setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestPMTCTService"
		 * );
		 * h.replaceCohortDefinition(malePatientsrequestPMTCTServiceInRegistration
		 * );
		 * 
		 * CohortIndicator malePatientsrequestPMTCTServiceIndicator = new
		 * CohortIndicator(); malePatientsrequestPMTCTServiceIndicator.setName(
		 * "malePatientsrequestPMTCTServiceIndicator");
		 * malePatientsrequestPMTCTServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestPMTCTServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestPMTCTServiceIndicator.setCohortDefinition(new
		 * Mapped
		 * <CohortDefinition>(malePatientsrequestPMTCTServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * ))); h.replaceDefinition(malePatientsrequestPMTCTServiceIndicator);
		 * //7.12.f. Female Number of patients requested LABORATORY SERVICE
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestLabService=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants.LABORATORY_SERVICES, SetComparator.IN,
		 * TimeModifier.ANY);
		 * patientRequestLabService.setName("patientRequestLabService");
		 * patientRequestLabService.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestLabService.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestLabService);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestLabServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestLabServiceInRegistration
		 * .setName("femalePatientsrequestLabServiceInRegistration");
		 * femalePatientsrequestLabServiceInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestLabServiceInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestLabServiceInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestLabServiceInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestLabServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestLabServiceInRegistration
		 * .getSearches().put("patientRequestLabService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestLabService,ParameterizableUtil.
		 * createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestLabServiceInRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestLabServiceInRegistration.setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestLabService"
		 * );
		 * h.replaceCohortDefinition(femalePatientsrequestLabServiceInRegistration
		 * );
		 * 
		 * CohortIndicator femalePatientsrequestLabServiceIndicator = new
		 * CohortIndicator(); femalePatientsrequestLabServiceIndicator.setName(
		 * "femalePatientsrequestLabServiceIndicator");
		 * femalePatientsrequestLabServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestLabServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestLabServiceIndicator.setCohortDefinition(new
		 * Mapped
		 * <CohortDefinition>(femalePatientsrequestLabServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * ))); h.replaceDefinition(femalePatientsrequestLabServiceIndicator);
		 * //7.12.m Male Number of patients requested LABORATORY SERVICE
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestLabServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestLabServiceInRegistration
		 * .setName("malePatientsrequestLabServiceInRegistration");
		 * malePatientsrequestLabServiceInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestLabServiceInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestLabServiceInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestLabServiceInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestLabServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestLabServiceInRegistration
		 * .getSearches().put("patientRequestLabService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestLabService,ParameterizableUtil.
		 * createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestLabServiceInRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestLabServiceInRegistration.setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestLabService"
		 * );
		 * h.replaceCohortDefinition(malePatientsrequestLabServiceInRegistration
		 * );
		 * 
		 * CohortIndicator malePatientsrequestLabServiceIndicator = new
		 * CohortIndicator(); malePatientsrequestLabServiceIndicator.setName(
		 * "malePatientsrequestLabServiceIndicator");
		 * malePatientsrequestLabServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestLabServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestLabServiceIndicator.setCohortDefinition(new
		 * Mapped<CohortDefinition>(malePatientsrequestLabServiceInRegistration,
		 * ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * ))); h.replaceDefinition(malePatientsrequestLabServiceIndicator);
		 * //7.13.f. Female Number of patients requested PHARMACY SERVICES
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestPharmacyService=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants.PHARMACY_SERVICES, SetComparator.IN,
		 * TimeModifier.ANY);
		 * patientRequestPharmacyService.setName("patientRequestPharmacyService"
		 * ); patientRequestPharmacyService.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestPharmacyService.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestPharmacyService);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestPharmacyServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestPharmacyServiceInRegistration
		 * .setName("femalePatientsrequestPharmacyServiceInRegistration");
		 * femalePatientsrequestPharmacyServiceInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestPharmacyServiceInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestPharmacyServiceInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestPharmacyServiceInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestPharmacyServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestPharmacyServiceInRegistration
		 * .getSearches().put("patientRequestPharmacyService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestPharmacyService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestPharmacyServiceInRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestPharmacyServiceInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestPharmacyService"
		 * ); h.replaceCohortDefinition(
		 * femalePatientsrequestPharmacyServiceInRegistration);
		 * 
		 * CohortIndicator femalePatientsrequestPharmacyServiceIndicator = new
		 * CohortIndicator();
		 * femalePatientsrequestPharmacyServiceIndicator.setName
		 * ("femalePatientsrequestPharmacyServiceIndicator");
		 * femalePatientsrequestPharmacyServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestPharmacyServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestPharmacyServiceIndicator.setCohortDefinition(new
		 * Mapped
		 * <CohortDefinition>(femalePatientsrequestPharmacyServiceInRegistration
		 * ,ParameterizableUtil.createParameterMappings(
		 * "onOrAfter=${startDate},onOrBefore=${endDate}")));
		 * h.replaceDefinition(femalePatientsrequestPharmacyServiceIndicator);
		 * //7.13.m Male Number of patients requested PHARMACY SERVICE
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestPharmacyServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestPharmacyServiceInRegistration
		 * .setName("malePatientsrequestPharmacyServiceInRegistration");
		 * malePatientsrequestPharmacyServiceInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestPharmacyServiceInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestPharmacyServiceInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestPharmacyServiceInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestPharmacyServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestPharmacyServiceInRegistration
		 * .getSearches().put("patientRequestPharmacyService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestPharmacyService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestPharmacyServiceInRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestPharmacyServiceInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestPharmacyService"
		 * ); h.replaceCohortDefinition(
		 * malePatientsrequestPharmacyServiceInRegistration);
		 * 
		 * CohortIndicator malePatientsrequestPharmacyServiceIndicator = new
		 * CohortIndicator();
		 * malePatientsrequestPharmacyServiceIndicator.setName
		 * ("malePatientsrequestPharmacyServiceIndicator");
		 * malePatientsrequestPharmacyServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestPharmacyServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestPharmacyServiceIndicator.setCohortDefinition(new
		 * Mapped
		 * <CohortDefinition>(malePatientsrequestPharmacyServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(malePatientsrequestPharmacyServiceIndicator);
		 * //7.13.f. Female Number of patients requested MATERNITY SERVICES
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestMaternityService=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants.MATERNITY_SERVICE, SetComparator.IN,
		 * TimeModifier.ANY);
		 * patientRequestMaternityService.setName("patientRequestMaternityService"
		 * ); patientRequestMaternityService.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestMaternityService.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestMaternityService);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestMaternityServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestMaternityServiceInRegistration
		 * .setName("femalePatientsrequestMaternityServiceInRegistration");
		 * femalePatientsrequestMaternityServiceInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestMaternityServiceInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestMaternityServiceInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestMaternityServiceInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestMaternityServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestMaternityServiceInRegistration
		 * .getSearches().put("patientRequestMaternityService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestMaternityService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestMaternityServiceInRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestMaternityServiceInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestMaternityService"
		 * ); h.replaceCohortDefinition(
		 * femalePatientsrequestMaternityServiceInRegistration);
		 * 
		 * CohortIndicator femalePatientsrequestMaternityServiceIndicator = new
		 * CohortIndicator();
		 * femalePatientsrequestMaternityServiceIndicator.setName
		 * ("femalePatientsrequestMaternityServiceIndicator");
		 * femalePatientsrequestMaternityServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestMaternityServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestMaternityServiceIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * femalePatientsrequestMaternityServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(femalePatientsrequestMaternityServiceIndicator);
		 * //7.14.m Male Number of patients requested MATERNITY SERVICE
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestMaternityServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestMaternityServiceInRegistration
		 * .setName("malePatientsrequestMaternityServiceInRegistration");
		 * malePatientsrequestMaternityServiceInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestMaternityServiceInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestMaternityServiceInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestMaternityServiceInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestMaternityServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestMaternityServiceInRegistration
		 * .getSearches().put("patientRequestMaternityService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestMaternityService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestMaternityServiceInRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestMaternityServiceInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestMaternityService"
		 * ); h.replaceCohortDefinition(
		 * malePatientsrequestMaternityServiceInRegistration);
		 * 
		 * CohortIndicator malePatientsrequestMaternityServiceIndicator = new
		 * CohortIndicator();
		 * malePatientsrequestMaternityServiceIndicator.setName
		 * ("malePatientsrequestMaternityServiceIndicator");
		 * malePatientsrequestMaternityServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestMaternityServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestMaternityServiceIndicator.setCohortDefinition(new
		 * Mapped
		 * <CohortDefinition>(malePatientsrequestMaternityServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(malePatientsrequestMaternityServiceIndicator);
		 * //7.15.f Female Number of patients requested HOSPITALIZATION SERVICE
		 * 
		 * CodedObsCohortDefinition
		 * patientRequestHospitalizationService=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants.HOSPITALIZATION_SERVICE, SetComparator.IN,
		 * TimeModifier.ANY); patientRequestHospitalizationService.setName(
		 * "patientRequestHospitalizationService");
		 * patientRequestHospitalizationService.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestHospitalizationService.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestHospitalizationService);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestHospitalizationServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestHospitalizationServiceInRegistration
		 * .setName("femalePatientsrequestHospitalizationServiceInRegistration"
		 * );
		 * femalePatientsrequestHospitalizationServiceInRegistration.addParameter
		 * (new Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestHospitalizationServiceInRegistration
		 * .addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestHospitalizationServiceInRegistration
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestHospitalizationServiceInRegistration
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestHospitalizationServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestHospitalizationServiceInRegistration
		 * .getSearches().put("patientRequestHospitalizationService", new
		 * Mapped<CohortDefinition>(patientRequestHospitalizationService,
		 * ParameterizableUtil.createParameterMappings(
		 * "onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestHospitalizationServiceInRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestHospitalizationServiceInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestHospitalizationService"
		 * ); h.replaceCohortDefinition(
		 * femalePatientsrequestHospitalizationServiceInRegistration);
		 * 
		 * CohortIndicator femalePatientsrequestHospitalizationServiceIndicator
		 * = new CohortIndicator();
		 * femalePatientsrequestHospitalizationServiceIndicator
		 * .setName("femalePatientsrequestHospitalizationServiceIndicator");
		 * femalePatientsrequestHospitalizationServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestHospitalizationServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestHospitalizationServiceIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * femalePatientsrequestHospitalizationServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(femalePatientsrequestHospitalizationServiceIndicator
		 * ); //7.15.m Male Number of patients requested HOSPITALIZATION SERVICE
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestHospitalizationServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestHospitalizationServiceInRegistration
		 * .setName("malePatientsrequestHospitalizationServiceInRegistration");
		 * malePatientsrequestHospitalizationServiceInRegistration
		 * .addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestHospitalizationServiceInRegistration
		 * .addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestHospitalizationServiceInRegistration
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestHospitalizationServiceInRegistration
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestHospitalizationServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestHospitalizationServiceInRegistration
		 * .getSearches().put("patientRequestHospitalizationService", new
		 * Mapped<CohortDefinition>(patientRequestHospitalizationService,
		 * ParameterizableUtil.createParameterMappings(
		 * "onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestHospitalizationServiceInRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestHospitalizationServiceInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestHospitalizationService"
		 * ); h.replaceCohortDefinition(
		 * malePatientsrequestHospitalizationServiceInRegistration);
		 * 
		 * CohortIndicator malePatientsrequestHospitalizationServiceIndicator =
		 * new CohortIndicator();
		 * malePatientsrequestHospitalizationServiceIndicator
		 * .setName("malePatientsrequestHospitalizationServiceIndicator");
		 * malePatientsrequestHospitalizationServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestHospitalizationServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestHospitalizationServiceIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * malePatientsrequestHospitalizationServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(malePatientsrequestHospitalizationServiceIndicator
		 * ); // 7.16.f Female Number of patients requested VACCINATION SERVICE
		 * CodedObsCohortDefinition
		 * patientRequestVaccinationService=makeCodedObsCohortDefinition
		 * (PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED,
		 * PrimaryCareReportConstants.VACCINATION_SERVICE, SetComparator.IN,
		 * TimeModifier.ANY); patientRequestVaccinationService.setName(
		 * "patientRequestVaccinationService");
		 * patientRequestVaccinationService.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * patientRequestVaccinationService.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * h.replaceCohortDefinition(patientRequestVaccinationService);
		 * 
		 * CompositionCohortDefinition
		 * femalePatientsrequestVaccinationServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * femalePatientsrequestVaccinationServiceInRegistration
		 * .setName("femalePatientsrequestVaccinationServiceInRegistration");
		 * femalePatientsrequestVaccinationServiceInRegistration
		 * .addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		 * femalePatientsrequestVaccinationServiceInRegistration
		 * .addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		 * femalePatientsrequestVaccinationServiceInRegistration
		 * .addParameter(new Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestVaccinationServiceInRegistration
		 * .addParameter(new Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestVaccinationServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestVaccinationServiceInRegistration
		 * .getSearches().put("patientRequestVaccinationService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestVaccinationService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * femalePatientsrequestVaccinationServiceInRegistration
		 * .getSearches().put("femalePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		 * femalePatientsrequestVaccinationServiceInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestVaccinationService"
		 * ); h.replaceCohortDefinition(
		 * femalePatientsrequestVaccinationServiceInRegistration);
		 * 
		 * CohortIndicator femalePatientsrequestVaccinationServiceIndicator =
		 * new CohortIndicator();
		 * femalePatientsrequestVaccinationServiceIndicator
		 * .setName("femalePatientsrequestVaccinationServiceIndicator");
		 * femalePatientsrequestVaccinationServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * femalePatientsrequestVaccinationServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * femalePatientsrequestVaccinationServiceIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * femalePatientsrequestVaccinationServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(femalePatientsrequestVaccinationServiceIndicator
		 * ); //7.16.m Male Number of patients requested VACCINATION SERVICE
		 * 
		 * CompositionCohortDefinition
		 * malePatientsrequestVaccinationServiceInRegistration=new
		 * CompositionCohortDefinition();
		 * malePatientsrequestVaccinationServiceInRegistration
		 * .setName("malePatientsrequestVaccinationServiceInRegistration");
		 * malePatientsrequestVaccinationServiceInRegistration.addParameter(new
		 * Parameter("onOrAfter","onOrAfter",Date.class));
		 * malePatientsrequestVaccinationServiceInRegistration.addParameter(new
		 * Parameter("onOrBefore","onOrBefore",Date.class));
		 * malePatientsrequestVaccinationServiceInRegistration.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestVaccinationServiceInRegistration.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestVaccinationServiceInRegistration
		 * .getSearches().put("patientsWithPrimaryCareRegistration", new
		 * Mapped<CohortDefinition
		 * >(patientsWithPrimaryCareRegistration,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestVaccinationServiceInRegistration
		 * .getSearches().put("patientRequestVaccinationService", new
		 * Mapped<CohortDefinition
		 * >(patientRequestVaccinationService,ParameterizableUtil
		 * .createParameterMappings
		 * ("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		 * malePatientsrequestVaccinationServiceInRegistration
		 * .getSearches().put("malePatientsrequestPrimCare", new
		 * Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		 * malePatientsrequestVaccinationServiceInRegistration
		 * .setCompositionString(
		 * "patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestVaccinationService"
		 * ); h.replaceCohortDefinition(
		 * malePatientsrequestVaccinationServiceInRegistration);
		 * 
		 * CohortIndicator malePatientsrequestVaccinationServiceIndicator = new
		 * CohortIndicator();
		 * malePatientsrequestVaccinationServiceIndicator.setName
		 * ("malePatientsrequestVaccinationServiceIndicator");
		 * malePatientsrequestVaccinationServiceIndicator.addParameter(new
		 * Parameter("startDate", "startDate", Date.class));
		 * malePatientsrequestVaccinationServiceIndicator.addParameter(new
		 * Parameter("endDate", "endDate", Date.class));
		 * malePatientsrequestVaccinationServiceIndicator
		 * .setCohortDefinition(new Mapped<CohortDefinition>(
		 * malePatientsrequestVaccinationServiceInRegistration
		 * ,ParameterizableUtil
		 * .createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"
		 * )));
		 * h.replaceDefinition(malePatientsrequestVaccinationServiceIndicator);
		 * 
		 * 
		 * SqlCohortDefinition sqltest=new SqlCohortDefinition();
		 * sqltest.setQuery(
		 * "select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type=20 and e.voided=0 group by e.patient_id) as patientregistrationtimes where timesofregistration=2 and encounter_datetime>= :startDate"
		 * ); sqltest.addParameter(new Parameter("startDate", "startDate",
		 * Date.class));
		 * 
		 * CohortIndicator sqltestIndicator=new CohortIndicator();
		 * sqltestIndicator.addParameter(new Parameter("startDate", "startDate",
		 * Date.class)); sqltestIndicator.addParameter(new Parameter("endDate",
		 * "endDate", Date.class)); sqltestIndicator.setCohortDefinition(new
		 * Mapped
		 * <CohortDefinition>(sqltest,ParameterizableUtil.createParameterMappings
		 * ("startDate=${startDate}")));
		 */

		// add global filter to the report

		rd.addIndicator(
				"2.1",
				"Percent of patients under 5 who do not have an observation for temperature in the vitals",
				patientsWithoutTemperatureInVitalsIndicator);
		/*rd.addIndicator(
				"2.2",
				"Percent of children under 5 who did have observation for temperature, and actually had a fever",
				patientsWithTemperatureGreaterThanNormalInVitalsIndicator);
		*/// rd.addIndicator("2.3",
		// "Percent of all registered patients under 5 who had a fever",
		// allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator);

		rd.addIndicator(
				"3.1",
				"Average number of patients registered per hour Mon through Friday between 8 and 10 am",
				peakHoursAndPeakDaysIndicator);

		rd.addIndicator(
				"4.1",
				"Percent of patients who are missing an insurance in registration encounter",
				percentOfPatientsMissingInsIndicator);
		rd.addIndicator(
				"4.2",
				"Number of patients who are missing an insurance in registration encounter",
				numberOfPatientsMissingInsIndicator);
		rd.addIndicator(
				"4.3.1",
				"Percent of patients with MUTUELLE insurance in registration encounter",
				percentOfPatientsWithMUTUELLEInsIndicator);
		rd.addIndicator(
				"4.3.2",
				"Percent of patients with RAMA insurance in registration encounter",
				percentOfPatientsWithRAMAInsIndicator);
		rd.addIndicator(
				"4.3.3",
				"Percent of patients with MMI insurance in registration encounter",
				percentOfPatientsWithMMIInsIndicator);
		rd.addIndicator(
				"4.3.4",
				"Percent of patients with MEDIPLAN insurance in registration encounter",
				percentOfPatientsWithMEDIPLANInsIndicator);
		rd.addIndicator(
				"4.3.5",
				"Percent of patients with CORAR insurance in registration encounter",
				percentOfPatientsWithCORARInsIndicator);
		rd.addIndicator(
				"4.3.6",
				"Percent of patients without (NONE) insurance in registration encounter",
				percentOfPatientsWithNONEInsIndicator);

		rd.addIndicator(
				"5.1.1",
				"Number of patients who only have 1 registration encounter with MUTUELLE Insurance:",
				patientsWithMUTUELLEInsAndOneVisitIndicator);
		rd.addIndicator(
				"5.1.2",
				"Number of patients who only have 1 registration encounter with RAMA Insurance:",
				patientsWithRAMAInsAndOneVisitIndicator);
		rd.addIndicator(
				"5.1.3",
				"Number of patients who only have 1 registration encounter with MMI Insurance:",
				patientsWithMMIInsAndOneVisitIndicator);
		rd.addIndicator(
				"5.1.4",
				"Number of patients who only have 1 registration encounter with MEDIPLAN Insurance:",
				patientsWithMEDIPLANInsAndOneVisitIndicator);
		rd.addIndicator(
				"5.1.5",
				"Number of patients who only have 1 registration encounter with CORAR Insurance:",
				patientsWithCORARInsAndOneVisitIndicator);
		rd.addIndicator(
				"5.1.6",
				"Number of patients who only have 1 registration encounter with NONE Insurance:",
				patientsWithNONEInsAndOneVisitIndicator);
		rd.addIndicator(
				"5.1.7",
				"Number of patients who only have 1 registration encounter missing Insurance:",
				patientsWithMissingInsAndOneVisitIndicator);
		rd.addIndicator(
				"5.2.1",
				"Number of patients who have 2 registration encounters with MUTUELLE Insurance:",
				patientsWithMUTUELLEInsAndTwoVisitsIndicator);
		rd.addIndicator(
				"5.2.2",
				"Number of patients who have 2 registration encounters with RAMA Insurance:",
				patientsWithRAMAInsAndTwoVisitsIndicator);
		rd.addIndicator(
				"5.2.3",
				"Number of patients who have 2 registration encounters with MMI Insurance:",
				patientsWithMMIInsAndTwoVisitsIndicator);
		rd.addIndicator(
				"5.2.4",
				"Number of patients who have 2 registration encounters with MEDIPLAN Insurance:",
				patientsWithMEDIPLANInsAndTwoVisitsIndicator);
		rd.addIndicator(
				"5.2.5",
				"Number of patients who have 2 registration encounters with CORAR Insurance:",
				patientsWithCORARInsAndTwoVisitsIndicator);
		rd.addIndicator(
				"5.2.6",
				"Number of patients who have 2 registration encounters with NONE Insurance:",
				patientsWithNONEInsAndTwoVisitsIndicator);
		rd.addIndicator(
				"5.2.7",
				"Number of patients who have 2 registration encounters missing Insurance:",
				patientsWithMissingInsAndTwoVisitsIndicator);
		rd.addIndicator(
				"5.3.1",
				"Number of patients who have 3 registration encounters with MUTUELLE Insurance:",
				patientsWithMUTUELLEInsAndThreeVisitsIndicator);
		rd.addIndicator(
				"5.3.2",
				"Number of patients who have 3 registration encounters with RAMA Insurance:",
				patientsWithRAMAInsAndThreeVisitsIndicator);
		rd.addIndicator(
				"5.3.3",
				"Number of patients who have 3 registration encounters with MMI Insurance:",
				patientsWithMMIInsAndThreeVisitsIndicator);
		rd.addIndicator(
				"5.3.4",
				"Number of patients who have 3 registration encounters with MEDIPLAN Insurance:",
				patientsWithMEDIPLANInsAndThreeVisitsIndicator);
		rd.addIndicator(
				"5.3.5",
				"Number of patients who have 3 registration encounters with CORAR Insurance:",
				patientsWithCORARInsAndThreeVisitsIndicator);
		rd.addIndicator(
				"5.3.6",
				"Number of patients who have 3 registration encounters with NONE Insurance:",
				patientsWithNONEInsAndThreeVisitsIndicator);
		rd.addIndicator(
				"5.3.7",
				"Number of patients who have 3 registration encounters missing Insurance:",
				patientsWithMissingInsAndThreeVisitsIndicator);
		rd.addIndicator(
				"5.4.1",
				"Number of patients With greater than 3 registration encounters with MUTUELLE Insurance:",
				patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator(
				"5.4.2",
				"Number of patients With greater than 3 registration encounters with RAMA Insurance:",
				patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator(
				"5.4.3",
				"Number of patients With greater than 3 registration encounters with MMI Insurance:",
				patientsWithMMIInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator(
				"5.4.4",
				"Number of patients With greater than 3 registration encounters with MEDIPLAN Insurance:",
				patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator(
				"5.4.5",
				"Number of patients With greater than 3 registration encounters with CORAR Insurance:",
				patientsWithCORARInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator(
				"5.4.6",
				"Number of patients With greater than 3 registration encounters with NONE Insurance:",
				patientsWithNONEInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator(
				"5.4.7",
				"Number of patients With greater than 3 registration encounters missing Insurance:",
				patientsWithMissingInsAndGreaterThanThreeVisitsIndicator);

		rd.addIndicator("6.1.m", "Male with age (0-1)",
				maleWithRegistrationAndAgeZeroToOneIndicator);
		rd.addIndicator("6.1.f", "Female with age (0-1)",
				femaleWithRegistrationAndAgeZeroToOneIndicator);
		rd.addIndicator("6.2.m", "Male with age (1-2)",
				maleWithRegistrationAndAgeOneToTwoIndicator);
		rd.addIndicator("6.2.f", "Female with age (1-2)",
				femaleWithRegistrationAndAgeOneToTwoIndicator);
		rd.addIndicator("6.3.m", "Male with age (2-3)",
				maleWithRegistrationAndAgeTwoToThreeIndicator);
		rd.addIndicator("6.3.f", "Female with age (2-3)",
				femaleWithRegistrationAndAgeTwoToThreeIndicator);
		rd.addIndicator("6.4.m", "Male with age (3-4)",
				maleWithRegistrationAndAgeThreeToFourIndicator);
		rd.addIndicator("6.4.f", "Female with age (3-4)",
				femaleWithRegistrationAndAgeThreeToFourIndicator);
		rd.addIndicator("6.5.m", "Male with age (4-5)",
				maleWithRegistrationAndAgeFourToFiveIndicator);
		rd.addIndicator("6.5.f", "Female with age (4-5)",
				femaleWithRegistrationAndAgeFourToFiveIndicator);
		rd.addIndicator("6.6.m", "Male with age (5-15)",
				maleWithRegistrationAndAgeFiveToFifteenIndicator);
		rd.addIndicator("6.6.f", "Female with age (5-15)",
				femaleWithRegistrationAndAgeFiveToFifteenIndicator);
		rd.addIndicator("6.7.m", "Male with age (15+)",
				maleWithRegistrationAndAgeFifteenAndPlusIndicator);
		rd.addIndicator("6.7.f", "Female with age (15+)",
				femaleWithRegistrationAndAgeFifteenAndPlusIndicator);

		rd.addIndicator("7.1.f",
				"Female number of patient requested primary care",
				femalePatientsrequestPrimCareInRegistrationIndicator);
		rd.addIndicator("7.1.m",
				"Male number of patient requested primary care",
				malePatientsrequestPrimCareInRegistrationIndicator);
		rd.addIndicator("7.2.f",
				"Female Number of patients requested VCT PROGRAM",
				femalePatientsrequestVCTProgramInRegistrationIndicator);
		rd.addIndicator("7.2.m",
				"Male Number of patients requested VCT PROGRAM",
				malePatientsrequestVCTProgramInRegistrationIndicator);
		rd.addIndicator("7.3.f",
				"Female Number of patients requested ANTENATAL CLINIC",
				femalePatientsrequestAntenatalClinicInRegistrationIndicator);
		rd.addIndicator("7.3.m",
				"Male Number of patients requested ANTENATAL CLINIC",
				malePatientsrequestAntenatalClinicInRegistrationIndicator);
		rd.addIndicator("7.4.f",
				"Female Number of patients requested FAMILY PLANNING SERVICES",
				femalePatientsrequestFamilyPlaningServicesRegistrationIndicator);
		rd.addIndicator("7.4.m",
				"Male Number of patients requested FAMILY PLANNING SERVICES",
				malePatientsrequestFamilyPlaningServicesRegistrationIndicator);
		rd.addIndicator("7.5.f",
				"Female Number of patients requested MUTUELLE SERVICE",
				femalePatientsrequestMutuelleServiceRegistrationIndicator);
		rd.addIndicator("7.5.m",
				"Male Number of patients requested MUTUELLE SERVICE",
				malePatientsrequestMutuelleServiceRegistrationIndicator);
		rd.addIndicator(
				"7.6.f",
				"Female Number of patients requested ACCOUNTING OFFICE SERVICE",
				femalePatientsrequestAccountingOfficeServiceRegistrationIndicator);
		rd.addIndicator("7.6.m",
				"Male Number of patients requested ACCOUNTING OFFICE SERVICE",
				malePatientsrequestAccountingOfficeServiceRegistrationIndicator);
		rd.addIndicator(
				"7.7.f",
				"Female Number of patients requested INTEGRATED MANAGEMENT OF ADULT ILLNESS SERVICE",
				femalePatientsrequestAdultIllnessServiceIndicator);
		rd.addIndicator(
				"7.7.m",
				"Male Number of patients requested INTEGRATED MANAGEMENT OF ADULT ILLNESS SERVICE",
				malePatientsrequestAdultIllnessServiceIndicator);
		rd.addIndicator(
				"7.8.f",
				"Female Number of patients requested INTEGRATED MANAGEMENT OF CHILDHOOD ILLNESS",
				femalePatientsrequestChildIllnessServiceIndicator);
		rd.addIndicator(
				"7.8.m",
				"Male Number of patients requested INTEGRATED MANAGEMENT OF CHILDHOOD ILLNESS",
				malePatientsrequestChildIllnessServiceIndicator);
		rd.addIndicator(
				"7.9.f",
				"Female Number of patients requested INFECTIOUS DISEASES CLINIC SERVICE",
				femalePatientsrequestInfectiousDiseasesServiceIndicator);
		rd.addIndicator(
				"7.9.m",
				"Male Number of patients requested INFECTIOUS DISEASES CLINIC SERVICE",
				malePatientsrequestInfectiousDiseasesServiceIndicator);
		rd.addIndicator("7.10.f",
				"Female Number of patients requested SOCIAL WORKER SERVICE",
				femalePatientsrequestSocialWorkerServiceIndicator);
		rd.addIndicator("7.10.m",
				"Male Number of patients requested SOCIAL WORKER SERVICE",
				malePatientsrequestSocialWorkerServiceIndicator);
		rd.addIndicator(
				"7.11.f",
				"Female number of patient requested PREVENTION OF MOTHER TO CHILD TRANSMISSION SERVICE",
				femalePatientsrequestPMTCTServiceIndicator);
		rd.addIndicator(
				"7.11.m",
				"Male number of patient requested PREVENTION OF MOTHER TO CHILD TRANSMISSION SERVICE",
				malePatientsrequestPMTCTServiceIndicator);
		rd.addIndicator("7.12.f",
				"Female Number of patients requested LABORATORY SERVICE",
				femalePatientsrequestLabServiceIndicator);
		rd.addIndicator("7.12.m",
				"Male Number of patients requested LABORATORY SERVICE",
				malePatientsrequestLabServiceIndicator);
		rd.addIndicator("7.13.f",
				"Female Number of patients requested PHARMACY SERVICES",
				femalePatientsrequestPharmacyServiceIndicator);
		rd.addIndicator("7.13.m",
				"Male Number of patients requested PHARMACY SERVICES",
				malePatientsrequestPharmacyServiceIndicator);
		rd.addIndicator("7.14.f",
				"Female Number of patients requested MATERNITY SERVICE",
				femalePatientsrequestMaternityServiceIndicator);
		rd.addIndicator("7.14.m",
				"Male Number of patients requested MATERNITY SERVICE",
				malePatientsrequestMaternityServiceIndicator);
		rd.addIndicator("7.15.f",
				"Female Number of patients requested HOSPITALIZATION SERVICE",
				femalePatientsrequestHospitalizationServiceIndicator);
		rd.addIndicator("7.15.m",
				"Male Number of patients requested HOSPITALIZATION SERVICE",
				malePatientsrequestHospitalizationServiceIndicator);
		rd.addIndicator("7.16.f",
				"Female Number of patients requested VACCINATION SERVICE",
				femalePatientsrequestVaccinationServiceIndicator);
		rd.addIndicator("7.16.m",
				"Male Number of patients requested VACCINATION SERVICE",
				malePatientsrequestVaccinationServiceIndicator);

		rd.setBaseCohortDefinition(
				h.cohortDefinition("location: Primary Care Patients at location"),
				ParameterizableUtil
						.createParameterMappings("location=${location}"));

		h.replaceReportDefinition(rd);

		return rd;
	}

	private void createLocationCohortDefinitions(EncounterType reg) {

		SqlCohortDefinition location = new SqlCohortDefinition();
		// select p.patient_id from encounter where encounter_type_id = 8 and
		// voided = 0 and location = :location
		// location.setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
		location.setQuery("select distinct p.patient_id from encounter e, patient p where p.voided = 0 and p.patient_id = e.patient_id and e.encounter_type = "
				+ reg.getEncounterTypeId()
				+ " and e.voided = 0 and e.location_id = :location ");
		location.setName("location: Primary Care Patients at location");
		location.addParameter(new Parameter("location", "location",
				Location.class));
		h.replaceCohortDefinition(location);
	}

	private CodedObsCohortDefinition makeCodedObsCohortDefinition(
			String question, String value, SetComparator setComparator,
			TimeModifier timeModifier) {
		CodedObsCohortDefinition obsCohortDefinition = new CodedObsCohortDefinition();
		if (question != null)
			obsCohortDefinition.setQuestion(Context.getConceptService()
					.getConceptByUuid(question));
		if (setComparator != null)
			obsCohortDefinition.setOperator(setComparator);
		if (timeModifier != null)
			obsCohortDefinition.setTimeModifier(timeModifier);
		Concept valueCoded = Context.getConceptService()
				.getConceptByUuid(value);
		List<Concept> valueList = new ArrayList<Concept>();
		if (valueCoded != null) {
			valueList.add(valueCoded);
			obsCohortDefinition.setValueList(valueList);
		}
		return obsCohortDefinition;
	}
}
