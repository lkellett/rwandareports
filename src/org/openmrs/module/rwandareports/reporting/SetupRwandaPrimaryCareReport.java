package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
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
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
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
import org.openmrs.module.reportingobjectgroup.objectgroup.definition.ObjectGroupDefinition;
import org.openmrs.module.reportingobjectgroup.objectgroup.definition.SqlObjectGroupDefinition;
import org.openmrs.module.reportingobjectgroup.objectgroup.indicator.ObjectGroupIndicator;
import org.openmrs.module.reportingobjectgroup.report.definition.RollingDailyPeriodIndicatorReportDefinition;
import org.openmrs.module.rwandareports.PrimaryCareReportConstants;
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

		h.purgeDefinition(ObjectGroupDefinition.class, "peakHours");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientsrequestPrimCare");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientsrequestPrimCare");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestVCTProgram");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestVCTProgram");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestAntenatalClinic");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestAntenatalClinic");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malepatientRequestFamilyPlaningServices");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalepatientRequestFamilyPlaningServices");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestMutuelleService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestMutuelleService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestAccountingOfficeService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestAccountingOfficeService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestAdultIllnessService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestChildIllnessService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestInfectiousDiseasesService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestSocialWorkerService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestAdultIllnessService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestChildIllnessService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestInfectiousDiseasesService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestSocialWorkerService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestPMTCTService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestLabService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestPharmacyService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestMaternityService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestHospitalizationService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"femalePatientRequestVaccinationService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestPMTCTService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestLabService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestPharmacyService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestMaternityService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestHospitalizationService");
		h.purgeDefinition(ObjectGroupDefinition.class,
				"malePatientRequestVaccinationService");
		h.purgeDefinition(ObjectGroupDefinition.class,
		"patientsUnder5WithTemperatureGreaterThanNormalInVitals");
		h.purgeDefinition(ObjectGroupDefinition.class,
		"patientsUnder5WithTemperatureInVitals");
		h.purgeDefinition(ObjectGroupDefinition.class,
		"patientsUnder5WithoutTemperatureInVitals");
		h.purgeDefinition(ObjectGroupDefinition.class,
		"patientsUnder5InRegistration");
		
		h.purgeDefinition(ObjectGroupDefinition.class, "ageBreakdownByGender");
		   

		h.purgeDefinition(ObjectGroupIndicator.class,
				"peakHoursAndPeakDaysIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestPrimCareInRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestPrimCareInRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestVCTProgramInRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestVCTProgramInRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestAntenatalClinicInRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestAntenatalClinicInRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestMutuelleServiceRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestMutuelleServiceRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestAdultIllnessServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestAdultIllnessServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestChildIllnessServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestChildIllnessServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestInfectiousDiseasesServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestInfectiousDiseasesServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestSocialWorkerServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestSocialWorkerServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestPMTCTServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestPMTCTServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestLabServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestLabServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestPharmacyServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestPharmacyServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestMaternityServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestMaternityServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestHospitalizationServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestHospitalizationServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"femalePatientsrequestVaccinationServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
				"malePatientsrequestVaccinationServiceIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
		"allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
		"patientsWithTemperatureGreaterThanNormalInVitalsIndicator");
		h.purgeDefinition(ObjectGroupIndicator.class,
		"patientsWithoutTemperatureInVitalsIndicator");		 
		
		
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

		// 2.1 Percent of patients who DO have an observation for
		// temperature in the vitals  (changed from no not have a change, hence the slightly misnamed vars
	
		
		SqlObjectGroupDefinition patientsUnder5WithoutTemperatureInVitals = new SqlObjectGroupDefinition();
		patientsUnder5WithoutTemperatureInVitals
				.setName("patientsUnder5WithoutTemperatureInVitals");
//		patientsUnder5WithoutTemperatureInVitals
//				.setQuery(
//						"select distinct encounterQuery.encounter_id,  encounterQuery.patient_id from (select e.encounter_id, e.patient_id from encounter e, person p, patient pa where e.encounter_id not in (select e.encounter_id from encounter e, obs o where e.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and e.encounter_id = o.encounter_id and o.voided = 0 and o.concept_id = "
//						+ RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.TEMPERATURE)
//						+ " and e.encounter_type = "
//						+ vitalsEncTypeId
//						+ ") and e.voided = 0 and encounter_type = "
//						+ vitalsEncTypeId
//						+ " and e.patient_id = p.person_id and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5	and p.voided = 0 and p.person_id = pa.patient_id and pa.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate UNION select e.encounter_id, e.patient_id from encounter e left join (select encounter_id, patient_id, encounter_datetime from encounter e where encounter_type = "
//						+ vitalsEncTypeId
//						+ " and voided = 0 and encounter_datetime > :startDate and encounter_datetime <= :endDate) eTmp on eTmp.patient_id = e.patient_id, person p, patient pa where eTmp.encounter_id is null and e.encounter_type = "
//						+ registrationEncTypeId
//						+ " and e.voided = 0 and e.patient_id = p.person_id and p.voided = 0 and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5 and p.person_id = pa.patient_id and pa.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate ) encounterQuery" +
//
//								"");
		patientsUnder5WithoutTemperatureInVitals.setQuery("select distinct e.encounter_id, e.patient_id from encounter e left join ( select distinct  e.encounter_id, e.patient_id, e.encounter_datetime from encounter e, person p, patient pa, obs o where e.encounter_id = o.encounter_id and o.voided = 0 and o.concept_id =  " + RwandaReportsUtil.getConceptIdFromUuid(PrimaryCareReportConstants.TEMPERATURE) + " and e.encounter_type =  " + vitalsEncTypeId + " and e.voided = 0 and e.patient_id = p.person_id and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5	and p.voided = 0 and p.person_id = pa.patient_id and pa.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and e.location_id = :location) eTmp on eTmp.patient_id = e.patient_id, person p, patient pat where day(eTmp.encounter_datetime) = day(e.encounter_datetime) and e.encounter_type = " + registrationEncTypeId + " and e.voided = 0 and p.voided = 0 and pat.voided = 0 and e.patient_id = p.person_id and p.person_id = pat.patient_id and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and e.location_id = :location");
		patientsUnder5WithoutTemperatureInVitals.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsUnder5WithoutTemperatureInVitals.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		h.replaceObjectGroupDefinition(patientsUnder5WithoutTemperatureInVitals);

	

		SqlObjectGroupDefinition patientsUnder5InRegistration = new SqlObjectGroupDefinition();
		patientsUnder5InRegistration.setName("patientsUnder5InRegistration");

		patientsUnder5InRegistration
				.setQuery("select e.encounter_id, e.patient_id from encounter e, person p, patient pa where e.voided = 0 and e.encounter_type = "+registrationEncTypeId+" and e.patient_id = p.person_id and p.voided = 0 and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5 and e.patient_id = pa.patient_id and pa.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and e.location_id = :location");

		patientsUnder5InRegistration.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientsUnder5InRegistration.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		h.replaceObjectGroupDefinition(patientsUnder5InRegistration);

			
		 ObjectGroupIndicator patientsWithoutTemperatureInVitalsIndicator = ObjectGroupIndicator.newFractionIndicator
			 ("patientsWithoutTemperatureInVitalsIndicator",new
			 Mapped<SqlObjectGroupDefinition
			 >(patientsUnder5WithoutTemperatureInVitals,
			 ParameterizableUtil.createParameterMappings
			 ("startDate=${startDate},endDate=${endDate}")), new
			 Mapped<SqlObjectGroupDefinition>(patientsUnder5InRegistration,
			 ParameterizableUtil
			 .createParameterMappings("startDate=${startDate},endDate=${endDate}"
			 )), null);
		 patientsWithoutTemperatureInVitalsIndicator.addParameter(new
		 Parameter("startDate", "startDate", Date.class));
		 patientsWithoutTemperatureInVitalsIndicator.addParameter(new
		 Parameter("endDate", "endDate", Date.class));
		 h.replaceDefinition(patientsWithoutTemperatureInVitalsIndicator);
		 
// 2.2 Percent of children under 5 who did have observation for

// temperature, and actually had a fever (were sick, temperature was higher than normal)
	
		 SqlObjectGroupDefinition patientsUnder5WithTemperatureGreaterThanNormalInVitals = new SqlObjectGroupDefinition();
		patientsUnder5WithTemperatureGreaterThanNormalInVitals
				.setName("patientsUnder5WithTemperatureGreaterThanNormalInVitals");
		patientsUnder5WithTemperatureGreaterThanNormalInVitals
				.setQuery("select e.encounter_id, e.patient_id from encounter e, person p, patient pa,obs o where e.voided = 0 and e.encounter_id = o.encounter_id and o.voided = 0 and o.concept_id = "
						+ RwandaReportsUtil
								.getConceptIdFromUuid(PrimaryCareReportConstants.TEMPERATURE)
						+ " and o.value_numeric > 37.0 and e.encounter_type = "
						+ vitalsEncTypeId
						+ " and e.patient_id = p.person_id and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5 and p.voided = 0 and p.person_id = pa.patient_id and pa.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and e.location_id = :location");
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceObjectGroupDefinition(patientsUnder5WithTemperatureGreaterThanNormalInVitals);

		
		SqlObjectGroupDefinition patientsUnder5WithTemperatureInVitals=new SqlObjectGroupDefinition();
		patientsUnder5WithTemperatureInVitals.setName("patientsUnder5WithTemperatureInVitals");
		patientsUnder5WithTemperatureInVitals.setQuery("select e.encounter_id, e.patient_id from encounter e, person p,patient pa,obs o where e.voided = 0 and e.encounter_id = o.encounter_id and o.voided = 0 and o.concept_id = "
				+ RwandaReportsUtil
				.getConceptIdFromUuid(PrimaryCareReportConstants.TEMPERATURE)
		+ " and e.encounter_type = "
		+ vitalsEncTypeId
		+ " and e.patient_id = p.person_id and (YEAR(:endDate)-YEAR(p.birthdate)) - (RIGHT(:endDate,5)<RIGHT(p.birthdate,5)) < 5 and p.voided = 0 and p.person_id = pa.patient_id and pa.voided = 0 and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and e.location_id = :location");
		patientsUnder5WithTemperatureInVitals.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsUnder5WithTemperatureInVitals.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		
		ObjectGroupIndicator patientsWithTemperatureGreaterThanNormalInVitalsIndicator = ObjectGroupIndicator.newFractionIndicator
		 ("patientsWithTemperatureGreaterThanNormalInVitalsIndicator",new
		 Mapped<SqlObjectGroupDefinition
		 >(patientsUnder5WithTemperatureGreaterThanNormalInVitals,
		 ParameterizableUtil.createParameterMappings
		 ("startDate=${startDate},endDate=${endDate}")), new
		 Mapped<SqlObjectGroupDefinition>(patientsUnder5WithTemperatureInVitals,
		 ParameterizableUtil
		 .createParameterMappings("startDate=${startDate},endDate=${endDate}"
		 )), null);
		patientsWithTemperatureGreaterThanNormalInVitalsIndicator.addParameter(new
	 Parameter("startDate", "startDate", Date.class));
		patientsWithTemperatureGreaterThanNormalInVitalsIndicator.addParameter(new
	 Parameter("endDate", "endDate", Date.class));
	 h.replaceDefinition(patientsWithTemperatureGreaterThanNormalInVitalsIndicator);
		
		
// 2.3 Percent of all registered patients under 5 who had a fever

	
		ObjectGroupIndicator allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator = ObjectGroupIndicator.newFractionIndicator
		 ("allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator",new
		 Mapped<SqlObjectGroupDefinition
		 >(patientsUnder5WithTemperatureGreaterThanNormalInVitals,
		 ParameterizableUtil.createParameterMappings
		 ("startDate=${startDate},endDate=${endDate}")), new
		 Mapped<SqlObjectGroupDefinition>(patientsUnder5InRegistration,
		 ParameterizableUtil
		 .createParameterMappings("startDate=${startDate},endDate=${endDate}"
		 )), null);
		allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator.addParameter(new
	 Parameter("startDate", "startDate", Date.class));
		allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator.addParameter(new
	 Parameter("endDate", "endDate", Date.class));
	 h.replaceDefinition(allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator);

	
		// ========================================================================
		// 3. Registration Speed during Peak Hours
		// ========================================================================

		// 8 to 10, monday to friday
		SqlObjectGroupDefinition peakHours = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(peakHours);

		// number of weekdays between startDate and stopDate / 2

		ObjectGroupIndicator peakHoursAndPeakDaysIndicator = ObjectGroupIndicator
				.newDailyDivisionIndicator(
						"peakHoursIndicator",
						new Mapped<ObjectGroupDefinition>(
								peakHours,
								ParameterizableUtil
										.createParameterMappings("startDate=${startDate},endDate=${endDate},startTime=08:00:00,endTime=10:00:00")),
						Integer.valueOf(2),
						ObjectGroupIndicator.IndicatorType.PER_WEEKDAYS, null);
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
						+ " and e.voided=0 and e.location_id = :location group by e.patient_id) as patientregistrationtimes where timesofregistration=1 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
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
						+ " and e.voided=0 and e.location_id = :location group by e.patient_id) as patientregistrationtimes where timesofregistration=2 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
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
						+ " and e.voided=0 and e.location_id = :location group by e.patient_id) as patientregistrationtimes where timesofregistration=3 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
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
						+ " and e.voided=0 and e.location_id = :location group by e.patient_id) as patientregistrationtimes where timesofregistration>3 and encounter_datetime>= :startDate and encounter_datetime<= :endDate ");
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

		
		SqlObjectGroupDefinition ageBreakdownByGender = new SqlObjectGroupDefinition();
		ageBreakdownByGender.setName("ageBreakdownByGender");
		ageBreakdownByGender
				.setQuery("select e.encounter_id, e.patient_id from encounter e, person p, patient pat  where e.voided = 0 and p.voided = 0 and pat.voided = 0  and e.encounter_type = "+registrationEncTypeId+"  and e.patient_id = p.person_id and e.patient_id = pat.patient_id and (YEAR(e.encounter_datetime)-YEAR(p.birthdate)) - (RIGHT(e.encounter_datetime,5)<RIGHT(p.birthdate,5)) < :maxAgeExclusive and (YEAR(e.encounter_datetime)-YEAR(p.birthdate)) - (RIGHT(e.encounter_datetime,5)<RIGHT(p.birthdate,5)) >= :minAgeInclusive and e.encounter_datetime > :startDate and e.encounter_datetime <= :endDate and p.gender = :gender and e.location_id = :location");
		ageBreakdownByGender.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		ageBreakdownByGender.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		ageBreakdownByGender.addParameter(new Parameter("maxAgeExclusive",
				"maxAgeExclusive", Integer.class));
		ageBreakdownByGender.addParameter(new Parameter("minAgeInclusive",
				"minAgeInclusive", Integer.class));
		ageBreakdownByGender.addParameter(new Parameter("gender",
				"gender", String.class));
		h.replaceObjectGroupDefinition(ageBreakdownByGender);
		
//		"6.1.m", "Male with age (0-1)",
//		maleWithRegistrationAndAgeZeroToOneIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeZeroToOneIndicator = ObjectGroupIndicator.newCountIndicator("Male with age (0-1)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=M,maxAgeExclusive=1,minAgeInclusive=0,startDate=${startDate},endDate=${endDate}")), 
				null);
		maleWithRegistrationAndAgeZeroToOneIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeZeroToOneIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(maleWithRegistrationAndAgeZeroToOneIndicator);
		
//rd.addIndicator("6.1.f", "Female with age (0-1)",
//		femaleWithRegistrationAndAgeZeroToOneIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeZeroToOneIndicator = ObjectGroupIndicator.newCountIndicator("Female with age (0-1)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=F,maxAgeExclusive=1,minAgeInclusive=0,startDate=${startDate},endDate=${endDate}")), 
				null);
		femaleWithRegistrationAndAgeZeroToOneIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeZeroToOneIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(femaleWithRegistrationAndAgeZeroToOneIndicator);
		
		
//rd.addIndicator("6.2.m", "Male with age (1-2)",
//		maleWithRegistrationAndAgeOneToTwoIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeOneToTwoIndicator = ObjectGroupIndicator.newCountIndicator("Male with age (1-2)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=M,maxAgeExclusive=2,minAgeInclusive=1,startDate=${startDate},endDate=${endDate}")), 
				null);
		maleWithRegistrationAndAgeOneToTwoIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeOneToTwoIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(maleWithRegistrationAndAgeOneToTwoIndicator);
		
//rd.addIndicator("6.2.f", "Female with age (1-2)",
//		femaleWithRegistrationAndAgeOneToTwoIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeOneToTwoIndicator = ObjectGroupIndicator.newCountIndicator("Female with age (1-2)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=F,maxAgeExclusive=2,minAgeInclusive=1,startDate=${startDate},endDate=${endDate}")), 
				null);
		femaleWithRegistrationAndAgeOneToTwoIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeOneToTwoIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(femaleWithRegistrationAndAgeOneToTwoIndicator);
		
		
		
//rd.addIndicator("6.3.m", "Male with age (2-3)",
//		maleWithRegistrationAndAgeTwoToThreeIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeTwoToThreeIndicator = ObjectGroupIndicator.newCountIndicator("Male with age (2-3)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=M,maxAgeExclusive=3,minAgeInclusive=2,startDate=${startDate},endDate=${endDate}")), 
				null);
		maleWithRegistrationAndAgeTwoToThreeIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeTwoToThreeIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(maleWithRegistrationAndAgeTwoToThreeIndicator);
		
		
		
//rd.addIndicator("6.3.f", "Female with age (2-3)",
//		femaleWithRegistrationAndAgeTwoToThreeIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeTwoToThreeIndicator = ObjectGroupIndicator.newCountIndicator("Female with age (2-3)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=F,maxAgeExclusive=3,minAgeInclusive=2,startDate=${startDate},endDate=${endDate}")), 
				null);
		femaleWithRegistrationAndAgeTwoToThreeIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeTwoToThreeIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(femaleWithRegistrationAndAgeTwoToThreeIndicator);
		
		
//rd.addIndicator("6.4.m", "Male with age (3-4)",
//		maleWithRegistrationAndAgeThreeToFourIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeThreeToFourIndicator = ObjectGroupIndicator.newCountIndicator("Male with age (3-4)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=M,maxAgeExclusive=4,minAgeInclusive=3,startDate=${startDate},endDate=${endDate}")), 
				null);
		maleWithRegistrationAndAgeThreeToFourIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeThreeToFourIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(maleWithRegistrationAndAgeThreeToFourIndicator);
		
		
//rd.addIndicator("6.4.f", "Female with age (3-4)",
//		femaleWithRegistrationAndAgeThreeToFourIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeThreeToFourIndicator = ObjectGroupIndicator.newCountIndicator("Female with age (3-4)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=F,maxAgeExclusive=4,minAgeInclusive=3,startDate=${startDate},endDate=${endDate}")), 
				null);
		femaleWithRegistrationAndAgeThreeToFourIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeThreeToFourIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(femaleWithRegistrationAndAgeThreeToFourIndicator);
		
		
//rd.addIndicator("6.5.m", "Male with age (4-5)",
//		maleWithRegistrationAndAgeFourToFiveIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeFourToFiveIndicator = ObjectGroupIndicator.newCountIndicator("Male with age (4-5)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=M,maxAgeExclusive=5,minAgeInclusive=4,startDate=${startDate},endDate=${endDate}")), 
				null);
		maleWithRegistrationAndAgeFourToFiveIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeFourToFiveIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(maleWithRegistrationAndAgeFourToFiveIndicator);
		
		
//rd.addIndicator("6.5.f", "Female with age (4-5)",
//		femaleWithRegistrationAndAgeFourToFiveIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeFourToFiveIndicator = ObjectGroupIndicator.newCountIndicator("Female with age (4-5)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=F,maxAgeExclusive=5,minAgeInclusive=4,startDate=${startDate},endDate=${endDate}")), 
				null);
		femaleWithRegistrationAndAgeFourToFiveIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeFourToFiveIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(femaleWithRegistrationAndAgeFourToFiveIndicator);
		
		
//rd.addIndicator("6.6.m", "Male with age (5-15)",
//		maleWithRegistrationAndAgeFiveToFifteenIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeFiveToFifteenIndicator = ObjectGroupIndicator.newCountIndicator("Male with age (5-15)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=M,maxAgeExclusive=15,minAgeInclusive=5,startDate=${startDate},endDate=${endDate}")), 
				null);
		maleWithRegistrationAndAgeFiveToFifteenIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeFiveToFifteenIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(maleWithRegistrationAndAgeFiveToFifteenIndicator);
		
		
//rd.addIndicator("6.6.f", "Female with age (5-15)",
//		femaleWithRegistrationAndAgeFiveToFifteenIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeFiveToFifteenIndicator = ObjectGroupIndicator.newCountIndicator("Female with age (5-15)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=F,maxAgeExclusive=15,minAgeInclusive=5,startDate=${startDate},endDate=${endDate}")), 
				null);
		femaleWithRegistrationAndAgeFiveToFifteenIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeFiveToFifteenIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(femaleWithRegistrationAndAgeFiveToFifteenIndicator);
		
//rd.addIndicator("6.7.m", "Male with age (15+)",
//		maleWithRegistrationAndAgeFifteenAndPlusIndicator);
		
		ObjectGroupIndicator maleWithRegistrationAndAgeFifteenAndPlusIndicator = ObjectGroupIndicator.newCountIndicator("Male with age (15+)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=M,maxAgeExclusive=150,minAgeInclusive=15,startDate=${startDate},endDate=${endDate}")), 
				null);
		maleWithRegistrationAndAgeFifteenAndPlusIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeFifteenAndPlusIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(maleWithRegistrationAndAgeFifteenAndPlusIndicator);
		
		
//rd.addIndicator("6.7.f", "Female with age (15+)",
//		femaleWithRegistrationAndAgeFifteenAndPlusIndicator);
		
		ObjectGroupIndicator femaleWithRegistrationAndAgeFifteenAndPlusIndicator = ObjectGroupIndicator.newCountIndicator("Female with age (15+)", 
				new Mapped<SqlObjectGroupDefinition>(ageBreakdownByGender, ParameterizableUtil.createParameterMappings("gender=F,maxAgeExclusive=150,minAgeInclusive=15,startDate=${startDate},endDate=${endDate}")), 
				null);
		femaleWithRegistrationAndAgeFifteenAndPlusIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeFifteenAndPlusIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		h.replaceDefinition(femaleWithRegistrationAndAgeFifteenAndPlusIndicator);

		

		// ========================================================================
		// 7. Primary care service requested
		// ========================================================================

		// 7.1.f Female Total number of patient requested primary care

		SqlObjectGroupDefinition femalePatientsrequestPrimCare = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientsrequestPrimCare);

		ObjectGroupIndicator femalePatientsrequestPrimCareInRegistrationIndicator = new ObjectGroupIndicator();
		femalePatientsrequestPrimCareInRegistrationIndicator
				.setName("femalePatientsrequestPrimCareInRegistrationIndicator");
		femalePatientsrequestPrimCareInRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestPrimCareInRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestPrimCareInRegistrationIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientsrequestPrimCare,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestPrimCareInRegistrationIndicator);

		// 7.1.m Female Total number of patient requested primary care

		SqlObjectGroupDefinition malePatientsrequestPrimCare = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientsrequestPrimCare);

		ObjectGroupIndicator malePatientsrequestPrimCareInRegistrationIndicator = new ObjectGroupIndicator();
		malePatientsrequestPrimCareInRegistrationIndicator
				.setName("malePatientsrequestPrimCareInRegistrationIndicator");
		malePatientsrequestPrimCareInRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestPrimCareInRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestPrimCareInRegistrationIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientsrequestPrimCare,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestPrimCareInRegistrationIndicator);

		// 7.2.f Female Number of patients requested VCT PROGRAM

		SqlObjectGroupDefinition femalePatientRequestVCTProgram = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestVCTProgram);

		ObjectGroupIndicator femalePatientsrequestVCTProgramInRegistrationIndicator = new ObjectGroupIndicator();
		femalePatientsrequestVCTProgramInRegistrationIndicator
				.setName("femalePatientsrequestVCTProgramInRegistrationIndicator");
		femalePatientsrequestVCTProgramInRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestVCTProgramInRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestVCTProgramInRegistrationIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestVCTProgram,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestVCTProgramInRegistrationIndicator);

		// 7.2.m Male Number of patients requested VCT PROGRAM
		SqlObjectGroupDefinition malePatientRequestVCTProgram = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestVCTProgram);

		ObjectGroupIndicator malePatientsrequestVCTProgramInRegistrationIndicator = new ObjectGroupIndicator();
		malePatientsrequestVCTProgramInRegistrationIndicator
				.setName("malePatientsrequestVCTProgramInRegistrationIndicator");
		malePatientsrequestVCTProgramInRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestVCTProgramInRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestVCTProgramInRegistrationIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestVCTProgram,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestVCTProgramInRegistrationIndicator);

		// 7.3.f Female Number of patients requested ANTENATAL CLINIC

		SqlObjectGroupDefinition femalePatientRequestAntenatalClinic = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestAntenatalClinic);

		ObjectGroupIndicator femalePatientsrequestAntenatalClinicInRegistrationIndicator = new ObjectGroupIndicator();
		femalePatientsrequestAntenatalClinicInRegistrationIndicator
				.setName("femalePatientsrequestAntenatalClinicInRegistrationIndicator");
		femalePatientsrequestAntenatalClinicInRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestAntenatalClinicInRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestAntenatalClinicInRegistrationIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestAntenatalClinic,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestAntenatalClinicInRegistrationIndicator);

		// 7.3.m Male Number of patients requested ANTENATAL CLINIC
		SqlObjectGroupDefinition malePatientRequestAntenatalClinic = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestAntenatalClinic);

		ObjectGroupIndicator malePatientsrequestAntenatalClinicInRegistrationIndicator = new ObjectGroupIndicator();
		malePatientsrequestAntenatalClinicInRegistrationIndicator
				.setName("malePatientsrequestAntenatalClinicInRegistrationIndicator");
		malePatientsrequestAntenatalClinicInRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestAntenatalClinicInRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestAntenatalClinicInRegistrationIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestAntenatalClinic,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestAntenatalClinicInRegistrationIndicator);

		// 7.4.f Female Number of patients requested FAMILY PLANNING SERVICES
		SqlObjectGroupDefinition femalepatientRequestFamilyPlaningServices = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalepatientRequestFamilyPlaningServices);

		ObjectGroupIndicator femalePatientsrequestFamilyPlaningServicesRegistrationIndicator = new ObjectGroupIndicator();
		femalePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.setName("femalePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		femalePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalepatientRequestFamilyPlaningServices,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestFamilyPlaningServicesRegistrationIndicator);

		// 7.4.m Male Number of patients requested FAMILY PLANNING SERVICES
		SqlObjectGroupDefinition malepatientRequestFamilyPlaningServices = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malepatientRequestFamilyPlaningServices);

		ObjectGroupIndicator malePatientsrequestFamilyPlaningServicesRegistrationIndicator = new ObjectGroupIndicator();
		malePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.setName("malePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		malePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestFamilyPlaningServicesRegistrationIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malepatientRequestFamilyPlaningServices,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestFamilyPlaningServicesRegistrationIndicator);

		// 7.5.f Female Number of patients requested MUTUELLE SERVICE

		SqlObjectGroupDefinition femalePatientRequestMutuelleService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestMutuelleService);

		ObjectGroupIndicator femalePatientsrequestMutuelleServiceRegistrationIndicator = new ObjectGroupIndicator();
		femalePatientsrequestMutuelleServiceRegistrationIndicator
				.setName("femalePatientsrequestMutuelleServiceRegistrationIndicator");
		femalePatientsrequestMutuelleServiceRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestMutuelleServiceRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestMutuelleServiceRegistrationIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestMutuelleService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestMutuelleServiceRegistrationIndicator);

		// 7.5.m Male Number of patients requested MUTUELLE SERVICE

		SqlObjectGroupDefinition malePatientRequestMutuelleService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestMutuelleService);

		ObjectGroupIndicator malePatientsrequestMutuelleServiceRegistrationIndicator = new ObjectGroupIndicator();
		malePatientsrequestMutuelleServiceRegistrationIndicator
				.setName("malePatientsrequestMutuelleServiceRegistrationIndicator");
		malePatientsrequestMutuelleServiceRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestMutuelleServiceRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestMutuelleServiceRegistrationIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestMutuelleService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestMutuelleServiceRegistrationIndicator);

		// 7.6.f Female Number of patients requested ACCOUNTING OFFICE SERVICE

		SqlObjectGroupDefinition femalePatientRequestAccountingOfficeService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestAccountingOfficeService);

		ObjectGroupIndicator femalePatientsrequestAccountingOfficeServiceRegistrationIndicator = new ObjectGroupIndicator();
		femalePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.setName("femalePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		femalePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestAccountingOfficeService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestAccountingOfficeServiceRegistrationIndicator);

		// 7.6.m Male Number of patients requested ACCOUNTING OFFICE SERVICE

		SqlObjectGroupDefinition malePatientRequestAccountingOfficeService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestAccountingOfficeService);

		ObjectGroupIndicator malePatientsrequestAccountingOfficeServiceRegistrationIndicator = new ObjectGroupIndicator();
		malePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.setName("malePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		malePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestAccountingOfficeServiceRegistrationIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestAccountingOfficeService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestAccountingOfficeServiceRegistrationIndicator);

		// 7.7.f Female Number of patients requested INTEGRATED MANAGEMENT OF
		// ADULT ILLNESS SERVICE

		SqlObjectGroupDefinition femalePatientRequestAdultIllnessService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestAdultIllnessService);

		ObjectGroupIndicator femalePatientsrequestAdultIllnessServiceIndicator = new ObjectGroupIndicator();
		femalePatientsrequestAdultIllnessServiceIndicator
				.setName("femalePatientsrequestAdultIllnessServiceIndicator");
		femalePatientsrequestAdultIllnessServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestAdultIllnessServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestAdultIllnessServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestAdultIllnessService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestAdultIllnessServiceIndicator);

		// 7.7.m Male Number of patients requested INTEGRATED MANAGEMENT OF
		// ADULT ILLNESS SERVICE
		SqlObjectGroupDefinition malePatientRequestAdultIllnessService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestAdultIllnessService);

		ObjectGroupIndicator malePatientsrequestAdultIllnessServiceIndicator = new ObjectGroupIndicator();
		malePatientsrequestAdultIllnessServiceIndicator
				.setName("malePatientsrequestAdultIllnessServiceIndicator");
		malePatientsrequestAdultIllnessServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestAdultIllnessServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestAdultIllnessServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestAdultIllnessService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestAdultIllnessServiceIndicator);

		// 7.8.f Female Number of patients requested INTEGRATED MANAGEMENT OF
		// CHILDHOOD ILLNESS Service

		SqlObjectGroupDefinition femalePatientRequestChildIllnessService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestChildIllnessService);

		ObjectGroupIndicator femalePatientsrequestChildIllnessServiceIndicator = new ObjectGroupIndicator();
		femalePatientsrequestChildIllnessServiceIndicator
				.setName("femalePatientsrequestChildIllnessServiceIndicator");
		femalePatientsrequestChildIllnessServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestChildIllnessServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestChildIllnessServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestChildIllnessService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestChildIllnessServiceIndicator);

		// 7.8.m Male Number of patients requested INTEGRATED MANAGEMENT OF
		// CHILDHOOD ILLNESS Service
		SqlObjectGroupDefinition malePatientRequestChildIllnessService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestChildIllnessService);

		ObjectGroupIndicator malePatientsrequestChildIllnessServiceIndicator = new ObjectGroupIndicator();
		malePatientsrequestChildIllnessServiceIndicator
				.setName("malePatientsrequestChildIllnessServiceIndicator");
		malePatientsrequestChildIllnessServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestChildIllnessServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestChildIllnessServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestChildIllnessService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestChildIllnessServiceIndicator);

		// 7.9.f Female Number of patients requested INFECTIOUS DISEASES CLINIC
		// SERVICE

		SqlObjectGroupDefinition femalePatientRequestInfectiousDiseasesService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestInfectiousDiseasesService);

		ObjectGroupIndicator femalePatientsrequestInfectiousDiseasesServiceIndicator = new ObjectGroupIndicator();
		femalePatientsrequestInfectiousDiseasesServiceIndicator
				.setName("femalePatientsrequestInfectiousDiseasesServiceIndicator");
		femalePatientsrequestInfectiousDiseasesServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestInfectiousDiseasesServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestInfectiousDiseasesServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestInfectiousDiseasesService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestInfectiousDiseasesServiceIndicator);

		// 7.9.m Male Number of patients requested INFECTIOUS DISEASES CLINIC
		// SERVICE

		SqlObjectGroupDefinition malePatientRequestInfectiousDiseasesService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestInfectiousDiseasesService);

		ObjectGroupIndicator malePatientsrequestInfectiousDiseasesServiceIndicator = new ObjectGroupIndicator();
		malePatientsrequestInfectiousDiseasesServiceIndicator
				.setName("malePatientsrequestInfectiousDiseasesServiceIndicator");
		malePatientsrequestInfectiousDiseasesServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestInfectiousDiseasesServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestInfectiousDiseasesServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestInfectiousDiseasesService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestInfectiousDiseasesServiceIndicator);

		// 7.10.f Female Number of patients requested SOCIAL WORKER SERVICE

		SqlObjectGroupDefinition femalePatientRequestSocialWorkerService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestSocialWorkerService);

		ObjectGroupIndicator femalePatientsrequestSocialWorkerServiceIndicator = new ObjectGroupIndicator();
		femalePatientsrequestSocialWorkerServiceIndicator
				.setName("femalePatientsrequestSocialWorkerServiceIndicator");
		femalePatientsrequestSocialWorkerServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestSocialWorkerServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestSocialWorkerServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestSocialWorkerService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestSocialWorkerServiceIndicator);

		// 7.10.m Male Number of patients requested SOCIAL WORKER SERVICE
		SqlObjectGroupDefinition malePatientRequestSocialWorkerService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestSocialWorkerService);

		ObjectGroupIndicator malePatientsrequestSocialWorkerServiceIndicator = new ObjectGroupIndicator();
		malePatientsrequestSocialWorkerServiceIndicator
				.setName("malePatientsrequestSocialWorkerServiceIndicator");
		malePatientsrequestSocialWorkerServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestSocialWorkerServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestSocialWorkerServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestSocialWorkerService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestSocialWorkerServiceIndicator);

		// 7.11.f Female Number of patients requested PREVENTION OF MOTHER TO
		// CHILD TRANSMISSION SERVICE

		SqlObjectGroupDefinition femalePatientRequestPMTCTService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestPMTCTService);

		ObjectGroupIndicator femalePatientsrequestPMTCTServiceIndicator = new ObjectGroupIndicator();
		femalePatientsrequestPMTCTServiceIndicator
				.setName("femalePatientsrequestPMTCTServiceIndicator");
		femalePatientsrequestPMTCTServiceIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientsrequestPMTCTServiceIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		femalePatientsrequestPMTCTServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestPMTCTService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestPMTCTServiceIndicator);

		// 7.11.f Male Number of patients requested PREVENTION OF MOTHER TO
		// CHILD TRANSMISSION SERVICE

		SqlObjectGroupDefinition malePatientRequestPMTCTService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestPMTCTService);

		ObjectGroupIndicator malePatientsrequestPMTCTServiceIndicator = new ObjectGroupIndicator();
		malePatientsrequestPMTCTServiceIndicator
				.setName("malePatientsrequestPMTCTServiceIndicator");
		malePatientsrequestPMTCTServiceIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientsrequestPMTCTServiceIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		malePatientsrequestPMTCTServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestPMTCTService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestPMTCTServiceIndicator);

		// 7.12.f. Female Number of patients requested LABORATORY SERVICE

		SqlObjectGroupDefinition femalePatientRequestLabService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestLabService);

		ObjectGroupIndicator femalePatientsrequestLabServiceIndicator = new ObjectGroupIndicator();
		femalePatientsrequestLabServiceIndicator
				.setName("femalePatientsrequestLabServiceIndicator");
		femalePatientsrequestLabServiceIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		femalePatientsrequestLabServiceIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		femalePatientsrequestLabServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestLabService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestLabServiceIndicator);

		// 7.12.m Male Number of patients requested LABORATORY SERVICE
		SqlObjectGroupDefinition malePatientRequestLabService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestLabService);

		ObjectGroupIndicator malePatientsrequestLabServiceIndicator = new ObjectGroupIndicator();
		malePatientsrequestLabServiceIndicator
				.setName("malePatientsrequestLabServiceIndicator");
		malePatientsrequestLabServiceIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientsrequestLabServiceIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		malePatientsrequestLabServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestLabService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestLabServiceIndicator);

		// 7.13.f. Female Number of patients requested PHARMACY SERVICES

		SqlObjectGroupDefinition femalePatientRequestPharmacyService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestPharmacyService);

		ObjectGroupIndicator femalePatientsrequestPharmacyServiceIndicator = new ObjectGroupIndicator();
		femalePatientsrequestPharmacyServiceIndicator
				.setName("femalePatientsrequestPharmacyServiceIndicator");
		femalePatientsrequestPharmacyServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestPharmacyServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestPharmacyServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestPharmacyService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestPharmacyServiceIndicator);

		// 7.13.m Male Number of patients requested PHARMACY SERVICE

		SqlObjectGroupDefinition malePatientRequestPharmacyService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestPharmacyService);

		ObjectGroupIndicator malePatientsrequestPharmacyServiceIndicator = new ObjectGroupIndicator();
		malePatientsrequestPharmacyServiceIndicator
				.setName("malePatientsrequestPharmacyServiceIndicator");
		malePatientsrequestPharmacyServiceIndicator.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		malePatientsrequestPharmacyServiceIndicator.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		malePatientsrequestPharmacyServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestPharmacyService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestPharmacyServiceIndicator);
		// 7.14.f. Female Number of patients requested MATERNITY SERVICES

		SqlObjectGroupDefinition femalePatientRequestMaternityService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestMaternityService);

		ObjectGroupIndicator femalePatientsrequestMaternityServiceIndicator = new ObjectGroupIndicator();
		femalePatientsrequestMaternityServiceIndicator
				.setName("femalePatientsrequestMaternityServiceIndicator");
		femalePatientsrequestMaternityServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestMaternityServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestMaternityServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestMaternityService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestMaternityServiceIndicator);

		// 7.14.m Male Number of patients requested MATERNITY SERVICE

		SqlObjectGroupDefinition malePatientRequestMaternityService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestMaternityService);

		ObjectGroupIndicator malePatientsrequestMaternityServiceIndicator = new ObjectGroupIndicator();
		malePatientsrequestMaternityServiceIndicator
				.setName("malePatientsrequestMaternityServiceIndicator");
		malePatientsrequestMaternityServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestMaternityServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestMaternityServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestMaternityService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestMaternityServiceIndicator);

		// 7.15.f Female Number of patients requested HOSPITALIZATION SERVICE

		SqlObjectGroupDefinition femalePatientRequestHospitalizationService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestHospitalizationService);

		ObjectGroupIndicator femalePatientsrequestHospitalizationServiceIndicator = new ObjectGroupIndicator();
		femalePatientsrequestHospitalizationServiceIndicator
				.setName("femalePatientsrequestHospitalizationServiceIndicator");
		femalePatientsrequestHospitalizationServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestHospitalizationServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestHospitalizationServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestHospitalizationService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestHospitalizationServiceIndicator);

		// 7.15.m Male Number of patients requested HOSPITALIZATION SERVICE

		SqlObjectGroupDefinition malePatientRequestHospitalizationService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestHospitalizationService);

		ObjectGroupIndicator malePatientsrequestHospitalizationServiceIndicator = new ObjectGroupIndicator();
		malePatientsrequestHospitalizationServiceIndicator
				.setName("malePatientsrequestHospitalizationServiceIndicator");
		malePatientsrequestHospitalizationServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestHospitalizationServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestHospitalizationServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestHospitalizationService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestHospitalizationServiceIndicator);

		// 7.16.f Female Number of patients requested VACCINATION SERVICE

		SqlObjectGroupDefinition femalePatientRequestVaccinationService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(femalePatientRequestVaccinationService);

		ObjectGroupIndicator femalePatientsrequestVaccinationServiceIndicator = new ObjectGroupIndicator();
		femalePatientsrequestVaccinationServiceIndicator
				.setName("femalePatientsrequestVaccinationServiceIndicator");
		femalePatientsrequestVaccinationServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		femalePatientsrequestVaccinationServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestVaccinationServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						femalePatientRequestVaccinationService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(femalePatientsrequestVaccinationServiceIndicator);

		// 7.16.m Male Number of patients requested VACCINATION SERVICE

		SqlObjectGroupDefinition malePatientRequestVaccinationService = new SqlObjectGroupDefinition();
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
		h.replaceObjectGroupDefinition(malePatientRequestVaccinationService);

		ObjectGroupIndicator malePatientsrequestVaccinationServiceIndicator = new ObjectGroupIndicator();
		malePatientsrequestVaccinationServiceIndicator
				.setName("malePatientsrequestVaccinationServiceIndicator");
		malePatientsrequestVaccinationServiceIndicator
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		malePatientsrequestVaccinationServiceIndicator
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestVaccinationServiceIndicator
				.setObjectGroupDefinition(new Mapped<SqlObjectGroupDefinition>(
						malePatientRequestVaccinationService,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(malePatientsrequestVaccinationServiceIndicator);

		

		// add global filter to the report

		rd.addIndicator(
				"2.1",
				"Percent of patients under 5 who do not have an observation for temperature in the vitals",
				patientsWithoutTemperatureInVitalsIndicator);
		rd.addIndicator(
				"2.2",
				"Percent of children under 5 who did have observation for temperature, and actually had a fever",
				patientsWithTemperatureGreaterThanNormalInVitalsIndicator);
	     rd.addIndicator("2.3","Percent of all registered patients under 5 who had a fever",allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator);

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
