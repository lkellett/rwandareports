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
import org.openmrs.module.rwandareports.report.definition.RollingDailyPeriodIndicatorReportDefinition;

public class SetupRwandaPrimaryCareReport {
	protected final static Log log = LogFactory.getLog(SetupRwandaPrimaryCareReport.class);
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupRwandaPrimaryCareReport(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		//setUpGlobalProperties();
		
		
		int registrationEncTypeId=Integer.parseInt(Context.getAdministrationService().getGlobalProperty("primarycarereport.registration.encountertypeid"));
		int vitalsEncTypeId=Integer.parseInt(Context.getAdministrationService().getGlobalProperty("primarycarereport.vitals.encountertypeid"));
        
		EncounterType registration=Context.getEncounterService().getEncounterType(registrationEncTypeId);
		if (registration == null)
			throw new RuntimeException("Are you sure the global property primarycarereport.registration.encountertypeid is set correctly?");
		
		EncounterType vitals=Context.getEncounterService().getEncounterType(vitalsEncTypeId);
		if (vitals == null)
			throw new RuntimeException("Are you sure the global property primarycarereport.vitals.encountertypeid is set correctly?");
		
		createLocationCohortDefinitions(registration);
		//createCompositionCohortDefinitions();
		//createIndicators();
		ReportDefinition rd = createReportDefinition(registration, vitals);
		h.createXlsCalendarOverview(rd, "rwandacalendarprimarycarereporttemplate.xls", "Primary_Care_Report_Template", null);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		//for (ReportDesign rd : rs.getReportDesigns(null, ExcelCalendarTemplateRenderer.class, false)) {
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Primary_Care_Report_Template".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(RollingDailyPeriodIndicatorReportDefinition.class, "Rwanda Primary Care Report");
		
		h.purgeDefinition(DataSetDefinition.class, "Rwanda Primary Care Report Data Set");
		h.purgeDefinition(CohortDefinition.class, "location: Primary Care Patients at location");
		

		h.purgeDefinition(CohortDefinition.class, "patientsWithPrimaryCareRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientsWithPrimaryCareVitals");
		h.purgeDefinition(CohortDefinition.class, "lessThanFive");
		h.purgeDefinition(CohortDefinition.class, "patientsWithTemperatureInVitals");
		h.purgeDefinition(CohortDefinition.class, "patientsUnder5WithoutTemperatureInVitals");
		h.purgeDefinition(CohortDefinition.class, "patientsUnder5InRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientsUnder5WithTemperatureInVitals");
		h.purgeDefinition(CohortDefinition.class, "patientsUnder5WithTemperatureGreaterThanNormalInVitals");
		h.purgeDefinition(CohortDefinition.class, "patientsWithTemperatureGreaterThanNormalInVitals");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestPrimCare");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestPrimCareInRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestPrimCare");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestPrimCareInRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestVCTProgram");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestVCTProgramInRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestVCTProgramInRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestAntenatalClinic");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestAntenatalClinicInRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestAntenatalClinicInRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestFamilyPlaningServices");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestFamilyPlaningServicesRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestFamilyPlaningServicesRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestMutuelleService");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestMutuelleServiceRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestMutuelleServiceRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestAdultIllnessService");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestAdultIllnessServiceRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestAdultIllnessServiceRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestAccountingOfficeService");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestAccountingOfficeServiceRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestAccountingOfficeServiceRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestChildIllnessService");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestChildIllnessServiceRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestChildIllnessServiceRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestInfectiousDiseasesService");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestInfectiousDiseasesServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestInfectiousDiseasesServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestSocialWorkerService");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestSocialWorkerServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestSocialWorkerServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestPMTCTService");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestPMTCTServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestPMTCTServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestLabService");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestLabServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestLabServiceInRegistration");		
		h.purgeDefinition(CohortDefinition.class, "patientRequestPharmacyService");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestPharmacyServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestPharmacyServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestMaternityService");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestMaternityServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestMaternityServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestHospitalizationService");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestHospitalizationServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestHospitalizationServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "patientRequestVaccinationService");
		h.purgeDefinition(CohortDefinition.class, "femalePatientsrequestVaccinationServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "malePatientsrequestVaccinationServiceInRegistration");
		h.purgeDefinition(CohortDefinition.class, "males");
		h.purgeDefinition(CohortDefinition.class, "females");
		h.purgeDefinition(CohortDefinition.class, "zeroToOne");
		h.purgeDefinition(CohortDefinition.class, "oneToTwo");
		h.purgeDefinition(CohortDefinition.class, "twoToThree");
		h.purgeDefinition(CohortDefinition.class, "threeToFour");
		h.purgeDefinition(CohortDefinition.class, "fourToFive");
		h.purgeDefinition(CohortDefinition.class, "fiveToFifteen");
		h.purgeDefinition(CohortDefinition.class, "fifteenAndPlus");
		h.purgeDefinition(CohortDefinition.class, "maleWithRegistrationAndAgeZeroToOne");
		h.purgeDefinition(CohortDefinition.class, "maleWithRegistrationAndAgeOneToTwo");
		h.purgeDefinition(CohortDefinition.class, "maleWithRegistrationAndAgeTwoToThree");
		h.purgeDefinition(CohortDefinition.class, "maleWithRegistrationAndAgeThreeToFour");
		h.purgeDefinition(CohortDefinition.class, "maleWithRegistrationAndAgeFourToFive");
		h.purgeDefinition(CohortDefinition.class, "maleWithRegistrationAndAgeFiveToFifteen");
		h.purgeDefinition(CohortDefinition.class, "maleWithRegistrationAndAgeFifteenAndPlus");
		h.purgeDefinition(CohortDefinition.class, "femaleWithRegistrationAndAgeZeroToOne");
		h.purgeDefinition(CohortDefinition.class, "femaleWithRegistrationAndAgeOneToTwo");
		h.purgeDefinition(CohortDefinition.class, "femaleWithRegistrationAndAgeTwoToThree");
		h.purgeDefinition(CohortDefinition.class, "femaleWithRegistrationAndAgeThreeToFour");
		h.purgeDefinition(CohortDefinition.class, "femaleWithRegistrationAndAgeFourToFive");
		h.purgeDefinition(CohortDefinition.class, "femaleWithRegistrationAndAgeFiveToFifteen");
		h.purgeDefinition(CohortDefinition.class, "femaleWithRegistrationAndAgeFifteenAndPlus");
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
		h.purgeDefinition(CohortDefinition.class, "patientsWithGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMUTUELLEInsAndOneVisit");
		h.purgeDefinition(CohortDefinition.class, "patientsWithRAMAInsAndOneVisit");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMMIInsAndOneVisit");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMEDIPLANInsAndOneVisit");
		h.purgeDefinition(CohortDefinition.class, "patientsWithCORARInsAndOneVisit");
		h.purgeDefinition(CohortDefinition.class, "patientsWithNONEInsAndOneVisit");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMissingInsAndOneVisit");		
		h.purgeDefinition(CohortDefinition.class, "patientsWithMUTUELLEInsAndTwoVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithRAMAInsAndTwoVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMMIInsAndTwoVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMEDIPLANInsAndTwoVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithCORARInsAndTwoVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithNONEInsAndTwoVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMissingInsAndTwoVisits");		
		h.purgeDefinition(CohortDefinition.class, "patientsWithMUTUELLEInsAndThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithRAMAInsAndThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMMIInsAndThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMEDIPLANInsAndThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithCORARInsAndThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithNONEInsAndThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMissingInsAndThreeVisits");		
		h.purgeDefinition(CohortDefinition.class, "patientsWithMUTUELLEInsAndGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithRAMAInsAndGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMMIInsAndGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMEDIPLANInsAndGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithCORARInsAndGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithNONEInsAndGreaterThanThreeVisits");
		h.purgeDefinition(CohortDefinition.class, "patientsWithMissingInsAndGreaterThanThreeVisits");
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
		
		
		
		
		
		
		h.purgeDefinition(CohortIndicator.class, "patientsWithoutTemperatureInVitalsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithTemperatureGreaterThanNormalInVitalsIndicator");
		h.purgeDefinition(CohortIndicator.class, "allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestPrimCareInRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestPrimCareInRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestVCTProgramInRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestVCTProgramInRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestAntenatalClinicInRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestAntenatalClinicInRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestMutuelleServiceRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestMutuelleServiceRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestAdultIllnessServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestAdultIllnessServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestChildIllnessServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestChildIllnessServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestInfectiousDiseasesServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestInfectiousDiseasesServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestSocialWorkerServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestSocialWorkerServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestPMTCTServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestPMTCTServiceIndicator");		      	
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestLabServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestLabServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestPharmacyServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestPharmacyServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestMaternityServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestMaternityServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestHospitalizationServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestHospitalizationServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "femalePatientsrequestVaccinationServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "malePatientsrequestVaccinationServiceIndicator");
		h.purgeDefinition(CohortIndicator.class, "maleWithRegistrationAndAgeZeroToOneIndicator");
		h.purgeDefinition(CohortIndicator.class, "maleWithRegistrationAndAgeOneToTwoIndicator");
		h.purgeDefinition(CohortIndicator.class, "maleWithRegistrationAndAgeTwoToThreeIndicator");
		h.purgeDefinition(CohortIndicator.class, "maleWithRegistrationAndAgeThreeToFourIndicator");
		h.purgeDefinition(CohortIndicator.class, "maleWithRegistrationAndAgeFourToFiveIndicator");
		h.purgeDefinition(CohortIndicator.class, "maleWithRegistrationAndAgeFiveToFifteenIndicator");
		h.purgeDefinition(CohortIndicator.class, "maleWithRegistrationAndAgeFifteenAndPlusIndicator");
		h.purgeDefinition(CohortIndicator.class, "femaleWithRegistrationAndAgeZeroToOneIndicator");
		h.purgeDefinition(CohortIndicator.class, "femaleWithRegistrationAndAgeOneToTwoIndicator");
		h.purgeDefinition(CohortIndicator.class, "femaleWithRegistrationAndAgeTwoToThreeIndicator");
		h.purgeDefinition(CohortIndicator.class, "femaleWithRegistrationAndAgeThreeToFourIndicator");
		h.purgeDefinition(CohortIndicator.class, "femaleWithRegistrationAndAgeFourToFiveIndicator");
		h.purgeDefinition(CohortIndicator.class, "femaleWithRegistrationAndAgeFiveToFifteenIndicator");
		h.purgeDefinition(CohortIndicator.class, "femaleWithRegistrationAndAgeFifteenAndPlusIndicator");
		h.purgeDefinition(CohortIndicator.class, "percentOfPatientsMissingInsIndicator");
		h.purgeDefinition(CohortIndicator.class, "numberOfPatientsMissingInsIndicator");
		h.purgeDefinition(CohortIndicator.class, "percentOfPatientsWithMUTUELLEInsIndicator");
		h.purgeDefinition(CohortIndicator.class, "percentOfPatientsWithRAMAInsIndicator");
		h.purgeDefinition(CohortIndicator.class, "percentOfPatientsWithMMIInsIndicator");
		h.purgeDefinition(CohortIndicator.class, "percentOfPatientsWithMEDIPLANInsIndicator");
		h.purgeDefinition(CohortIndicator.class, "percentOfPatientsWithCORARInsIndicator");
		h.purgeDefinition(CohortIndicator.class, "percentOfPatientsWithNONEInsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithMUTUELLEInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithRAMAInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithMMIInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithMEDIPLANInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithCORARInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithNONEInsAndOneVisitIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithMissingInsAndOneVisitIndicator");		
		h.purgeDefinition(CohortIndicator.class, "patientsWithMissingInsAndOneVisitIndicator");		
		h.purgeDefinition(CohortIndicator.class, "patientsWithMUTUELLEInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithRAMAInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithMMIInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithMEDIPLANInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithCORARInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithNONEInsAndTwoVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithMissingInsAndTwoVisitsIndicator");		
		h.purgeDefinition(CohortIndicator.class, "patientsWithMissingInsAndTwoVisitsIndicator");		
		h.purgeDefinition(CohortIndicator.class, "patientsWithMUTUELLEInsAndThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithRAMAInsAndThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithMMIInsAndThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithMEDIPLANInsAndThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithCORARInsAndThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithNONEInsAndThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithMissingInsAndThreeVisitsIndicator");		
		h.purgeDefinition(CohortIndicator.class, "patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithMMIInsAndGreaterThanThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithCORARInsAndGreaterThanThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithNONEInsAndGreaterThanThreeVisitsIndicator");
		h.purgeDefinition(CohortIndicator.class, "patientsWithMissingInsAndGreaterThanThreeVisitsIndicator");		
		h.purgeDefinition(CohortIndicator.class, "peakHoursAndPeakDaysIndicator");
//		h.purgeDefinition(CohortIndicator.class, "monday1Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday2Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday3Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday4Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday5Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday6Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday7Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday8Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday9Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday10Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday11Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday12Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday13Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday14Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday15Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday16Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday17Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday18Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday19Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday20Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday21Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday22Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday23Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday24Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday25Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday26Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday27Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday28Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday29Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday30Indicator");
//		h.purgeDefinition(CohortIndicator.class, "monday31Indicator");	
//		h.purgeDefinition(CohortIndicator.class, "tuesday1Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday2Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday3Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday4Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday5Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday6Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday7Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday8Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday9Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday10Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday11Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday12Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday13Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday14Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday15Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday16Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday17Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday18Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday19Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday20Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday21Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday22Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday23Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday24Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday25Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday26Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday27Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday28Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday29Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday30Indicator");
//		h.purgeDefinition(CohortIndicator.class, "tuesday31Indicator");	
//		h.purgeDefinition(CohortIndicator.class, "wednesday1Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday2Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday3Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday4Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday5Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday6Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday7Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday8Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday9Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday10Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday11Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday12Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday13Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday14Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday15Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday16Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday17Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday18Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday19Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday20Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday21Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday22Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday23Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday24Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday25Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday26Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday27Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday28Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday29Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday30Indicator");
//		h.purgeDefinition(CohortIndicator.class, "wednesday31Indicator");
//		
//		h.purgeDefinition(CohortIndicator.class, "thursday1Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday2Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday3Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday4Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday5Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday6Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday7Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday8Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday9Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday10Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday11Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday12Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday13Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday14Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday15Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday16Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday17Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday18Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday19Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday20Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday21Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday22Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday23Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday24Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday25Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday26Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday27Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday28Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday29Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday30Indicator");
//		h.purgeDefinition(CohortIndicator.class, "thursday31Indicator");
//		
//		h.purgeDefinition(CohortIndicator.class, "friday1Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday2Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday3Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday4Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday5Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday6Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday7Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday8Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday9Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday10Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday11Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday12Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday13Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday14Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday15Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday16Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday17Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday18Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday19Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday20Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday21Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday22Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday23Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday24Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday25Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday26Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday27Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday28Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday29Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday30Indicator");
//		h.purgeDefinition(CohortIndicator.class, "friday31Indicator");	
//
//		h.purgeDefinition(CohortIndicator.class, "saturday1Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday2Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday3Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday4Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday5Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday6Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday7Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday8Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday9Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday10Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday11Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday12Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday13Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday14Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday15Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday16Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday17Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday18Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday19Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday20Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday21Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday22Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday23Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday24Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday25Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday26Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday27Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday28Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday29Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday30Indicator");
//		h.purgeDefinition(CohortIndicator.class, "saturday31Indicator");
//		
//		h.purgeDefinition(CohortIndicator.class, "sunday1Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday2Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday3Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday4Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday5Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday6Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday7Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday8Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday9Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday10Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday11Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday12Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday13Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday14Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday15Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday16Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday17Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday18Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday19Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday20Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday21Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday22Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday23Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday24Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday25Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday26Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday27Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday28Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday29Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday30Indicator");
//		h.purgeDefinition(CohortIndicator.class, "sunday31Indicator");
	}
	
	
	private ReportDefinition createReportDefinition(EncounterType reg, EncounterType vitals) {
		
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
				
		rd.setName("Rwanda Primary Care Report");
		
		rd.setupDataSetDefinition();
		
	//Creation of Vitals and Registration Encounter types during report period
		
		List<EncounterType> registrationEncounterType = new ArrayList<EncounterType>();
		registrationEncounterType.add(reg);
		EncounterCohortDefinition patientsWithPrimaryCareRegistration=new EncounterCohortDefinition();
		patientsWithPrimaryCareRegistration.setName("patientsWithPrimaryCareRegistration");
		patientsWithPrimaryCareRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithPrimaryCareRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithPrimaryCareRegistration.setEncounterTypeList(registrationEncounterType);
		h.replaceCohortDefinition(patientsWithPrimaryCareRegistration);
		
		List<EncounterType> vitalsEncounterType = new ArrayList<EncounterType>();
		vitalsEncounterType.add(vitals);
		EncounterCohortDefinition patientsWithPrimaryCareVitals=new EncounterCohortDefinition();
		patientsWithPrimaryCareVitals.setName("patientsWithPrimaryCareVitals");
		patientsWithPrimaryCareVitals.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithPrimaryCareVitals.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithPrimaryCareVitals.setEncounterTypeList(vitalsEncounterType);
		h.replaceCohortDefinition(patientsWithPrimaryCareVitals);

//======================================================================================
//       1st Question
//======================================================================================
		
		//Monday		
		
//		SqlCohortDefinition monday=new SqlCohortDefinition();
//		monday.setName("monday");
//		monday.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,WEEKDAY(e.encounter_datetime) as peakdays FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0) as patientspeakdays where peakdays=0 and encounter_datetime>= :startDate and encounter_datetime< :endDate");
//		monday.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday.addParameter(new Parameter("endDate", "endDate", Date.class));
//		h.replaceCohortDefinition(monday);
//		
//		
//		CohortIndicator monday1Indicator = new CohortIndicator();
//		monday1Indicator.setName("monday1Indicator");
//		monday1Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday1Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday1Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-30d},endDate=${endDate-29d}")));
//		h.replaceDefinition(monday1Indicator);
//		
//		
//		CohortIndicator monday2Indicator = new CohortIndicator();
//		monday2Indicator.setName("monday2Indicator");
//		monday2Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday2Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday2Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-29d},endDate=${endDate-28d}")));
//		h.replaceDefinition(monday2Indicator);
//		
//		CohortIndicator monday3Indicator = new CohortIndicator();
//		monday3Indicator.setName("monday3Indicator");
//		monday3Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday3Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday3Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-28d},endDate=${endDate-27d}")));
//		h.replaceDefinition(monday3Indicator);
//		
//		
//		CohortIndicator monday4Indicator = new CohortIndicator();
//		monday4Indicator.setName("monday4Indicator");
//		monday4Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday4Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday4Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-27d},endDate=${endDate-26d}")));
//		h.replaceDefinition(monday4Indicator);
//		
//		CohortIndicator monday5Indicator = new CohortIndicator();
//		monday5Indicator.setName("monday5Indicator");
//		monday5Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday5Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday5Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-26d},endDate=${endDate-25d}")));
//		h.replaceDefinition(monday5Indicator);
//		
//		CohortIndicator monday6Indicator = new CohortIndicator();
//		monday6Indicator.setName("monday6Indicator");
//		monday6Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday6Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday6Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-25d},endDate=${endDate-24d}")));
//		h.replaceDefinition(monday6Indicator);
//		
//		CohortIndicator monday7Indicator = new CohortIndicator();
//		monday7Indicator.setName("monday7Indicator");
//		monday7Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday7Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday7Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-24d},endDate=${endDate-23d}")));
//		h.replaceDefinition(monday7Indicator);
//		
//		CohortIndicator monday8Indicator = new CohortIndicator();
//		monday8Indicator.setName("monday8Indicator");
//		monday8Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday8Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday8Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-23d},endDate=${endDate-22d}")));
//		h.replaceDefinition(monday8Indicator);
//		
//		CohortIndicator monday9Indicator = new CohortIndicator();
//		monday9Indicator.setName("monday9Indicator");
//		monday9Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday9Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday9Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-22d},endDate=${endDate-21d}")));
//		h.replaceDefinition(monday9Indicator);
//		
//		CohortIndicator monday10Indicator = new CohortIndicator();
//		monday10Indicator.setName("monday10Indicator");
//		monday10Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday10Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday10Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-21d},endDate=${endDate-20d}")));
//		h.replaceDefinition(monday10Indicator);
//		
//		CohortIndicator monday11Indicator = new CohortIndicator();
//		monday11Indicator.setName("monday11Indicator");
//		monday11Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday11Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday11Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-20d},endDate=${endDate-19d}")));
//		h.replaceDefinition(monday11Indicator);
//		
//		
//		CohortIndicator monday12Indicator = new CohortIndicator();
//		monday12Indicator.setName("monday12Indicator");
//		monday12Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday12Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday12Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-19d},endDate=${endDate-18d}")));
//		h.replaceDefinition(monday12Indicator);
//		
//		CohortIndicator monday13Indicator = new CohortIndicator();
//		monday13Indicator.setName("monday13Indicator");
//		monday13Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday13Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday13Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-18d},endDate=${endDate-17d}")));
//		h.replaceDefinition(monday13Indicator);
//		
//		
//		CohortIndicator monday14Indicator = new CohortIndicator();
//		monday14Indicator.setName("monday14Indicator");
//		monday14Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday14Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday14Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-17d},endDate=${endDate-16d}")));
//		h.replaceDefinition(monday14Indicator);
//		
//		CohortIndicator monday15Indicator = new CohortIndicator();
//		monday15Indicator.setName("monday15Indicator");
//		monday15Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday15Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday15Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-16d},endDate=${endDate-15d}")));
//		h.replaceDefinition(monday15Indicator);
//		
//		CohortIndicator monday16Indicator = new CohortIndicator();
//		monday16Indicator.setName("monday16Indicator");
//		monday16Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday16Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday16Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-15d},endDate=${endDate-14d}")));
//		h.replaceDefinition(monday16Indicator);
//		
//		CohortIndicator monday17Indicator = new CohortIndicator();
//		monday17Indicator.setName("monday17Indicator");
//		monday17Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday17Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday17Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-14d},endDate=${endDate-13d}")));
//		h.replaceDefinition(monday17Indicator);
//		
//		CohortIndicator monday18Indicator = new CohortIndicator();
//		monday18Indicator.setName("monday18Indicator");
//		monday18Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday18Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday18Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-13d},endDate=${endDate-12d}")));
//		h.replaceDefinition(monday18Indicator);
//		
//		CohortIndicator monday19Indicator = new CohortIndicator();
//		monday19Indicator.setName("monday19Indicator");
//		monday19Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday19Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday19Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-12d},endDate=${endDate-11d}")));
//		h.replaceDefinition(monday19Indicator);
//		
//		CohortIndicator monday20Indicator = new CohortIndicator();
//		monday20Indicator.setName("monday20Indicator");
//		monday20Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday20Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday20Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-11d},endDate=${endDate-10d}")));
//		h.replaceDefinition(monday20Indicator);
//		
//		CohortIndicator monday21Indicator = new CohortIndicator();
//		monday21Indicator.setName("monday21Indicator");
//		monday21Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday21Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday21Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-10d},endDate=${endDate-9d}")));
//		h.replaceDefinition(monday21Indicator);
//		
//		
//		CohortIndicator monday22Indicator = new CohortIndicator();
//		monday22Indicator.setName("monday22Indicator");
//		monday22Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday22Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday22Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-9d},endDate=${endDate-8d}")));
//		h.replaceDefinition(monday22Indicator);
//		
//		CohortIndicator monday23Indicator = new CohortIndicator();
//		monday23Indicator.setName("monday23Indicator");
//		monday23Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday23Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday23Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-8d},endDate=${endDate-7d}")));
//		h.replaceDefinition(monday23Indicator);
//		
//		
//		CohortIndicator monday24Indicator = new CohortIndicator();
//		monday24Indicator.setName("monday24Indicator");
//		monday24Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday24Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday24Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-7d},endDate=${endDate-6d}")));
//		h.replaceDefinition(monday24Indicator);
//		
//		CohortIndicator monday25Indicator = new CohortIndicator();
//		monday25Indicator.setName("monday25Indicator");
//		monday25Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday25Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday25Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-6d},endDate=${endDate-5d}")));
//		h.replaceDefinition(monday25Indicator);
//		
//		CohortIndicator monday26Indicator = new CohortIndicator();
//		monday26Indicator.setName("monday26Indicator");
//		monday26Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday26Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday26Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-5d},endDate=${endDate-4d}")));
//		h.replaceDefinition(monday26Indicator);
//		
//		CohortIndicator monday27Indicator = new CohortIndicator();
//		monday27Indicator.setName("monday27Indicator");
//		monday27Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday27Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday27Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-4d},endDate=${endDate-3d}")));
//		h.replaceDefinition(monday27Indicator);
//		
//		CohortIndicator monday28Indicator = new CohortIndicator();
//		monday28Indicator.setName("monday28Indicator");
//		monday28Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday28Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday28Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-3d},endDate=${endDate-2d}")));
//		h.replaceDefinition(monday28Indicator);
//		
//		CohortIndicator monday29Indicator = new CohortIndicator();
//		monday29Indicator.setName("monday29Indicator");
//		monday29Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday29Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday29Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-2d},endDate=${endDate-1d}")));
//		h.replaceDefinition(monday29Indicator);
//		
//		CohortIndicator monday30Indicator = new CohortIndicator();
//		monday30Indicator.setName("monday30Indicator");
//		monday30Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday30Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday30Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate-1d},endDate=${endDate}")));
//		h.replaceDefinition(monday30Indicator);
//		
//		CohortIndicator monday31Indicator = new CohortIndicator();
//		monday31Indicator.setName("monday31Indicator");
//		monday31Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		monday31Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		monday31Indicator.setCohortDefinition(new Mapped<CohortDefinition>(monday,ParameterizableUtil.createParameterMappings("startDate=${endDate},endDate=${endDate+1d}")));
//		h.replaceDefinition(monday31Indicator);
//
//		
////Tuesday
//		SqlCohortDefinition tuesday=new SqlCohortDefinition();
//		tuesday.setName("tuesday");
//		tuesday.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,WEEKDAY(e.encounter_datetime) as peakdays FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0) as patientspeakdays where peakdays=1 and encounter_datetime>= :startDate and encounter_datetime< :endDate");
//		tuesday.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday.addParameter(new Parameter("endDate", "endDate", Date.class));
//		h.replaceCohortDefinition(tuesday);
//		
//		
//		CohortIndicator tuesday1Indicator = new CohortIndicator();
//		tuesday1Indicator.setName("tuesday1Indicator");
//		tuesday1Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday1Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday1Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-30d},endDate=${endDate-29d}")));
//		h.replaceDefinition(tuesday1Indicator);
//		
//		
//		CohortIndicator tuesday2Indicator = new CohortIndicator();
//		tuesday2Indicator.setName("tuesday2Indicator");
//		tuesday2Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday2Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday2Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-29d},endDate=${endDate-28d}")));
//		h.replaceDefinition(tuesday2Indicator);
//		
//		CohortIndicator tuesday3Indicator = new CohortIndicator();
//		tuesday3Indicator.setName("tuesday3Indicator");
//		tuesday3Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday3Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday3Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-28d},endDate=${endDate-27d}")));
//		h.replaceDefinition(tuesday3Indicator);
//		
//		
//		CohortIndicator tuesday4Indicator = new CohortIndicator();
//		tuesday4Indicator.setName("tuesday4Indicator");
//		tuesday4Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday4Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday4Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-27d},endDate=${endDate-26d}")));
//		h.replaceDefinition(tuesday4Indicator);
//		
//		CohortIndicator tuesday5Indicator = new CohortIndicator();
//		tuesday5Indicator.setName("tuesday5Indicator");
//		tuesday5Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday5Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday5Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-26d},endDate=${endDate-25d}")));
//		h.replaceDefinition(tuesday5Indicator);
//		
//		CohortIndicator tuesday6Indicator = new CohortIndicator();
//		tuesday6Indicator.setName("tuesday6Indicator");
//		tuesday6Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday6Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday6Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-25d},endDate=${endDate-24d}")));
//		h.replaceDefinition(tuesday6Indicator);
//		
//		CohortIndicator tuesday7Indicator = new CohortIndicator();
//		tuesday7Indicator.setName("tuesday7Indicator");
//		tuesday7Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday7Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday7Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-24d},endDate=${endDate-23d}")));
//		h.replaceDefinition(tuesday7Indicator);
//		
//		CohortIndicator tuesday8Indicator = new CohortIndicator();
//		tuesday8Indicator.setName("tuesday8Indicator");
//		tuesday8Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday8Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday8Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-23d},endDate=${endDate-22d}")));
//		h.replaceDefinition(tuesday8Indicator);
//		
//		CohortIndicator tuesday9Indicator = new CohortIndicator();
//		tuesday9Indicator.setName("tuesday9Indicator");
//		tuesday9Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday9Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday9Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-22d},endDate=${endDate-21d}")));
//		h.replaceDefinition(tuesday9Indicator);
//		
//		CohortIndicator tuesday10Indicator = new CohortIndicator();
//		tuesday10Indicator.setName("tuesday10Indicator");
//		tuesday10Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday10Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday10Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-21d},endDate=${endDate-20d}")));
//		h.replaceDefinition(tuesday10Indicator);
//		
//		CohortIndicator tuesday11Indicator = new CohortIndicator();
//		tuesday11Indicator.setName("tuesday11Indicator");
//		tuesday11Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday11Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday11Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-20d},endDate=${endDate-19d}")));
//		h.replaceDefinition(tuesday11Indicator);
//		
//		
//		CohortIndicator tuesday12Indicator = new CohortIndicator();
//		tuesday12Indicator.setName("tuesday12Indicator");
//		tuesday12Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday12Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday12Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-19d},endDate=${endDate-18d}")));
//		h.replaceDefinition(tuesday12Indicator);
//		
//		CohortIndicator tuesday13Indicator = new CohortIndicator();
//		tuesday13Indicator.setName("tuesday13Indicator");
//		tuesday13Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday13Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday13Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-18d},endDate=${endDate-17d}")));
//		h.replaceDefinition(tuesday13Indicator);
//		
//		
//		CohortIndicator tuesday14Indicator = new CohortIndicator();
//		tuesday14Indicator.setName("tuesday14Indicator");
//		tuesday14Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday14Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday14Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-17d},endDate=${endDate-16d}")));
//		h.replaceDefinition(tuesday14Indicator);
//		
//		CohortIndicator tuesday15Indicator = new CohortIndicator();
//		tuesday15Indicator.setName("tuesday15Indicator");
//		tuesday15Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday15Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday15Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-16d},endDate=${endDate-15d}")));
//		h.replaceDefinition(tuesday15Indicator);
//		
//		CohortIndicator tuesday16Indicator = new CohortIndicator();
//		tuesday16Indicator.setName("tuesday16Indicator");
//		tuesday16Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday16Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday16Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-15d},endDate=${endDate-14d}")));
//		h.replaceDefinition(tuesday16Indicator);
//		
//		CohortIndicator tuesday17Indicator = new CohortIndicator();
//		tuesday17Indicator.setName("tuesday17Indicator");
//		tuesday17Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday17Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday17Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-14d},endDate=${endDate-13d}")));
//		h.replaceDefinition(tuesday17Indicator);
//		
//		CohortIndicator tuesday18Indicator = new CohortIndicator();
//		tuesday18Indicator.setName("tuesday18Indicator");
//		tuesday18Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday18Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday18Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-13d},endDate=${endDate-12d}")));
//		h.replaceDefinition(tuesday18Indicator);
//		
//		CohortIndicator tuesday19Indicator = new CohortIndicator();
//		tuesday19Indicator.setName("tuesday19Indicator");
//		tuesday19Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday19Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday19Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-12d},endDate=${endDate-11d}")));
//		h.replaceDefinition(tuesday19Indicator);
//		
//		CohortIndicator tuesday20Indicator = new CohortIndicator();
//		tuesday20Indicator.setName("tuesday20Indicator");
//		tuesday20Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday20Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday20Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-11d},endDate=${endDate-10d}")));
//		h.replaceDefinition(tuesday20Indicator);
//		
//		CohortIndicator tuesday21Indicator = new CohortIndicator();
//		tuesday21Indicator.setName("tuesday21Indicator");
//		tuesday21Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday21Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday21Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-10d},endDate=${endDate-9d}")));
//		h.replaceDefinition(tuesday21Indicator);
//		
//		
//		CohortIndicator tuesday22Indicator = new CohortIndicator();
//		tuesday22Indicator.setName("tuesday22Indicator");
//		tuesday22Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday22Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday22Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-9d},endDate=${endDate-8d}")));
//		h.replaceDefinition(tuesday22Indicator);
//		
//		CohortIndicator tuesday23Indicator = new CohortIndicator();
//		tuesday23Indicator.setName("tuesday23Indicator");
//		tuesday23Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday23Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday23Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-8d},endDate=${endDate-7d}")));
//		h.replaceDefinition(tuesday23Indicator);
//		
//		
//		CohortIndicator tuesday24Indicator = new CohortIndicator();
//		tuesday24Indicator.setName("tuesday24Indicator");
//		tuesday24Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday24Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday24Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-7d},endDate=${endDate-6d}")));
//		h.replaceDefinition(tuesday24Indicator);
//		
//		CohortIndicator tuesday25Indicator = new CohortIndicator();
//		tuesday25Indicator.setName("tuesday25Indicator");
//		tuesday25Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday25Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday25Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-6d},endDate=${endDate-5d}")));
//		h.replaceDefinition(tuesday25Indicator);
//		
//		CohortIndicator tuesday26Indicator = new CohortIndicator();
//		tuesday26Indicator.setName("tuesday26Indicator");
//		tuesday26Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday26Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday26Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-5d},endDate=${endDate-4d}")));
//		h.replaceDefinition(tuesday26Indicator);
//		
//		CohortIndicator tuesday27Indicator = new CohortIndicator();
//		tuesday27Indicator.setName("tuesday27Indicator");
//		tuesday27Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday27Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday27Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-4d},endDate=${endDate-3d}")));
//		h.replaceDefinition(tuesday27Indicator);
//		
//		CohortIndicator tuesday28Indicator = new CohortIndicator();
//		tuesday28Indicator.setName("tuesday28Indicator");
//		tuesday28Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday28Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday28Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-3d},endDate=${endDate-2d}")));
//		h.replaceDefinition(tuesday28Indicator);
//		
//		CohortIndicator tuesday29Indicator = new CohortIndicator();
//		tuesday29Indicator.setName("tuesday29Indicator");
//		tuesday29Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday29Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday29Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-2d},endDate=${endDate-1d}")));
//		h.replaceDefinition(tuesday29Indicator);
//		
//		CohortIndicator tuesday30Indicator = new CohortIndicator();
//		tuesday30Indicator.setName("tuesday30Indicator");
//		tuesday30Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday30Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday30Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-1d},endDate=${endDate}")));
//		h.replaceDefinition(tuesday30Indicator);
//		
//		CohortIndicator tuesday31Indicator = new CohortIndicator();
//		tuesday31Indicator.setName("tuesday31Indicator");
//		tuesday31Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		tuesday31Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		tuesday31Indicator.setCohortDefinition(new Mapped<CohortDefinition>(tuesday,ParameterizableUtil.createParameterMappings("startDate=${endDate},endDate=${endDate+1d}")));
//		h.replaceDefinition(tuesday31Indicator);
//		
//		
////Wednesday
//		
//		SqlCohortDefinition wednesday=new SqlCohortDefinition();
//		wednesday.setName("wednesday");
//		wednesday.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,WEEKDAY(e.encounter_datetime) as peakdays FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0) as patientspeakdays where peakdays=2 and encounter_datetime>= :startDate and encounter_datetime< :endDate");
//		wednesday.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday.addParameter(new Parameter("endDate", "endDate", Date.class));
//		h.replaceCohortDefinition(wednesday);
//		
//		
//		CohortIndicator wednesday1Indicator = new CohortIndicator();
//		wednesday1Indicator.setName("wednesday1Indicator");
//		wednesday1Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday1Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday1Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-30d},endDate=${endDate-29d}")));
//		h.replaceDefinition(wednesday1Indicator);
//		
//		
//		CohortIndicator wednesday2Indicator = new CohortIndicator();
//		wednesday2Indicator.setName("wednesday2Indicator");
//		wednesday2Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday2Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday2Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-29d},endDate=${endDate-28d}")));
//		h.replaceDefinition(wednesday2Indicator);
//		
//		CohortIndicator wednesday3Indicator = new CohortIndicator();
//		wednesday3Indicator.setName("wednesday3Indicator");
//		wednesday3Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday3Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday3Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-28d},endDate=${endDate-27d}")));
//		h.replaceDefinition(wednesday3Indicator);
//		
//		
//		CohortIndicator wednesday4Indicator = new CohortIndicator();
//		wednesday4Indicator.setName("wednesday4Indicator");
//		wednesday4Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday4Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday4Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-27d},endDate=${endDate-26d}")));
//		h.replaceDefinition(wednesday4Indicator);
//		
//		CohortIndicator wednesday5Indicator = new CohortIndicator();
//		wednesday5Indicator.setName("wednesday5Indicator");
//		wednesday5Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday5Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday5Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-26d},endDate=${endDate-25d}")));
//		h.replaceDefinition(wednesday5Indicator);
//		
//		CohortIndicator wednesday6Indicator = new CohortIndicator();
//		wednesday6Indicator.setName("wednesday6Indicator");
//		wednesday6Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday6Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday6Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-25d},endDate=${endDate-24d}")));
//		h.replaceDefinition(wednesday6Indicator);
//		
//		CohortIndicator wednesday7Indicator = new CohortIndicator();
//		wednesday7Indicator.setName("wednesday7Indicator");
//		wednesday7Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday7Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday7Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-24d},endDate=${endDate-23d}")));
//		h.replaceDefinition(wednesday7Indicator);
//		
//		CohortIndicator wednesday8Indicator = new CohortIndicator();
//		wednesday8Indicator.setName("wednesday8Indicator");
//		wednesday8Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday8Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday8Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-23d},endDate=${endDate-22d}")));
//		h.replaceDefinition(wednesday8Indicator);
//		
//		CohortIndicator wednesday9Indicator = new CohortIndicator();
//		wednesday9Indicator.setName("wednesday9Indicator");
//		wednesday9Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday9Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday9Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-22d},endDate=${endDate-21d}")));
//		h.replaceDefinition(wednesday9Indicator);
//		
//		CohortIndicator wednesday10Indicator = new CohortIndicator();
//		wednesday10Indicator.setName("wednesday10Indicator");
//		wednesday10Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday10Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday10Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-21d},endDate=${endDate-20d}")));
//		h.replaceDefinition(wednesday10Indicator);
//		
//		CohortIndicator wednesday11Indicator = new CohortIndicator();
//		wednesday11Indicator.setName("wednesday11Indicator");
//		wednesday11Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday11Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday11Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-20d},endDate=${endDate-19d}")));
//		h.replaceDefinition(wednesday11Indicator);
//		
//		
//		CohortIndicator wednesday12Indicator = new CohortIndicator();
//		wednesday12Indicator.setName("wednesday12Indicator");
//		wednesday12Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday12Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday12Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-19d},endDate=${endDate-18d}")));
//		h.replaceDefinition(wednesday12Indicator);
//		
//		CohortIndicator wednesday13Indicator = new CohortIndicator();
//		wednesday13Indicator.setName("wednesday13Indicator");
//		wednesday13Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday13Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday13Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-18d},endDate=${endDate-17d}")));
//		h.replaceDefinition(wednesday13Indicator);
//		
//		
//		CohortIndicator wednesday14Indicator = new CohortIndicator();
//		wednesday14Indicator.setName("wednesday14Indicator");
//		wednesday14Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday14Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday14Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-17d},endDate=${endDate-16d}")));
//		h.replaceDefinition(wednesday14Indicator);
//		
//		CohortIndicator wednesday15Indicator = new CohortIndicator();
//		wednesday15Indicator.setName("wednesday15Indicator");
//		wednesday15Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday15Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday15Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-16d},endDate=${endDate-15d}")));
//		h.replaceDefinition(wednesday15Indicator);
//		
//		CohortIndicator wednesday16Indicator = new CohortIndicator();
//		wednesday16Indicator.setName("wednesday16Indicator");
//		wednesday16Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday16Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday16Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-15d},endDate=${endDate-14d}")));
//		h.replaceDefinition(wednesday16Indicator);
//		
//		CohortIndicator wednesday17Indicator = new CohortIndicator();
//		wednesday17Indicator.setName("wednesday17Indicator");
//		wednesday17Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday17Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday17Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-14d},endDate=${endDate-13d}")));
//		h.replaceDefinition(wednesday17Indicator);
//		
//		CohortIndicator wednesday18Indicator = new CohortIndicator();
//		wednesday18Indicator.setName("wednesday18Indicator");
//		wednesday18Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday18Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday18Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-13d},endDate=${endDate-12d}")));
//		h.replaceDefinition(wednesday18Indicator);
//		
//		CohortIndicator wednesday19Indicator = new CohortIndicator();
//		wednesday19Indicator.setName("wednesday19Indicator");
//		wednesday19Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday19Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday19Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-12d},endDate=${endDate-11d}")));
//		h.replaceDefinition(wednesday19Indicator);
//		
//		CohortIndicator wednesday20Indicator = new CohortIndicator();
//		wednesday20Indicator.setName("wednesday20Indicator");
//		wednesday20Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday20Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday20Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-11d},endDate=${endDate-10d}")));
//		h.replaceDefinition(wednesday20Indicator);
//		
//		CohortIndicator wednesday21Indicator = new CohortIndicator();
//		wednesday21Indicator.setName("wednesday21Indicator");
//		wednesday21Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday21Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday21Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-10d},endDate=${endDate-9d}")));
//		h.replaceDefinition(wednesday21Indicator);
//		
//		
//		CohortIndicator wednesday22Indicator = new CohortIndicator();
//		wednesday22Indicator.setName("wednesday22Indicator");
//		wednesday22Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday22Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday22Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-9d},endDate=${endDate-8d}")));
//		h.replaceDefinition(wednesday22Indicator);
//		
//		CohortIndicator wednesday23Indicator = new CohortIndicator();
//		wednesday23Indicator.setName("wednesday23Indicator");
//		wednesday23Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday23Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday23Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-8d},endDate=${endDate-7d}")));
//		h.replaceDefinition(wednesday23Indicator);
//		
//		
//		CohortIndicator wednesday24Indicator = new CohortIndicator();
//		wednesday24Indicator.setName("wednesday24Indicator");
//		wednesday24Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday24Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday24Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-7d},endDate=${endDate-6d}")));
//		h.replaceDefinition(wednesday24Indicator);
//		
//		CohortIndicator wednesday25Indicator = new CohortIndicator();
//		wednesday25Indicator.setName("wednesday25Indicator");
//		wednesday25Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday25Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday25Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-6d},endDate=${endDate-5d}")));
//		h.replaceDefinition(wednesday25Indicator);
//		
//		CohortIndicator wednesday26Indicator = new CohortIndicator();
//		wednesday26Indicator.setName("wednesday26Indicator");
//		wednesday26Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday26Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday26Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-5d},endDate=${endDate-4d}")));
//		h.replaceDefinition(wednesday26Indicator);
//		
//		CohortIndicator wednesday27Indicator = new CohortIndicator();
//		wednesday27Indicator.setName("wednesday27Indicator");
//		wednesday27Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday27Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday27Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-4d},endDate=${endDate-3d}")));
//		h.replaceDefinition(wednesday27Indicator);
//		
//		CohortIndicator wednesday28Indicator = new CohortIndicator();
//		wednesday28Indicator.setName("wednesday28Indicator");
//		wednesday28Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday28Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday28Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-3d},endDate=${endDate-2d}")));
//		h.replaceDefinition(wednesday28Indicator);
//		
//		CohortIndicator wednesday29Indicator = new CohortIndicator();
//		wednesday29Indicator.setName("wednesday29Indicator");
//		wednesday29Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday29Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday29Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-2d},endDate=${endDate-1d}")));
//		h.replaceDefinition(wednesday29Indicator);
//		
//		CohortIndicator wednesday30Indicator = new CohortIndicator();
//		wednesday30Indicator.setName("wednesday30Indicator");
//		wednesday30Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday30Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday30Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate-1d},endDate=${endDate}")));
//		h.replaceDefinition(wednesday30Indicator);
//		
//		CohortIndicator wednesday31Indicator = new CohortIndicator();
//		wednesday31Indicator.setName("wednesday31Indicator");
//		wednesday31Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		wednesday31Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		wednesday31Indicator.setCohortDefinition(new Mapped<CohortDefinition>(wednesday,ParameterizableUtil.createParameterMappings("startDate=${endDate},endDate=${endDate+1d}")));
//		h.replaceDefinition(wednesday31Indicator);
//		
//		
//		
//		
////Thursday
//		
//		SqlCohortDefinition thursday=new SqlCohortDefinition();
//		thursday.setName("thursday");
//		thursday.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,WEEKDAY(e.encounter_datetime) as peakdays FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0) as patientspeakdays where peakdays=3 and encounter_datetime>= :startDate and encounter_datetime< :endDate");
//		thursday.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday.addParameter(new Parameter("endDate", "endDate", Date.class));
//		h.replaceCohortDefinition(thursday);
//		
//		
//		CohortIndicator thursday1Indicator = new CohortIndicator();
//		thursday1Indicator.setName("thursday1Indicator");
//		thursday1Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday1Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday1Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-30d},endDate=${endDate-29d}")));
//		h.replaceDefinition(thursday1Indicator);
//		
//		
//		CohortIndicator thursday2Indicator = new CohortIndicator();
//		thursday2Indicator.setName("thursday2Indicator");
//		thursday2Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday2Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday2Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-29d},endDate=${endDate-28d}")));
//		h.replaceDefinition(thursday2Indicator);
//		
//		CohortIndicator thursday3Indicator = new CohortIndicator();
//		thursday3Indicator.setName("thursday3Indicator");
//		thursday3Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday3Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday3Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-28d},endDate=${endDate-27d}")));
//		h.replaceDefinition(thursday3Indicator);
//		
//		
//		CohortIndicator thursday4Indicator = new CohortIndicator();
//		thursday4Indicator.setName("thursday4Indicator");
//		thursday4Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday4Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday4Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-27d},endDate=${endDate-26d}")));
//		h.replaceDefinition(thursday4Indicator);
//		
//		CohortIndicator thursday5Indicator = new CohortIndicator();
//		thursday5Indicator.setName("thursday5Indicator");
//		thursday5Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday5Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday5Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-26d},endDate=${endDate-25d}")));
//		h.replaceDefinition(thursday5Indicator);
//		
//		CohortIndicator thursday6Indicator = new CohortIndicator();
//		thursday6Indicator.setName("thursday6Indicator");
//		thursday6Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday6Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday6Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-25d},endDate=${endDate-24d}")));
//		h.replaceDefinition(thursday6Indicator);
//		
//		CohortIndicator thursday7Indicator = new CohortIndicator();
//		thursday7Indicator.setName("thursday7Indicator");
//		thursday7Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday7Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday7Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-24d},endDate=${endDate-23d}")));
//		h.replaceDefinition(thursday7Indicator);
//		
//		CohortIndicator thursday8Indicator = new CohortIndicator();
//		thursday8Indicator.setName("thursday8Indicator");
//		thursday8Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday8Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday8Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-23d},endDate=${endDate-22d}")));
//		h.replaceDefinition(thursday8Indicator);
//		
//		CohortIndicator thursday9Indicator = new CohortIndicator();
//		thursday9Indicator.setName("thursday9Indicator");
//		thursday9Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday9Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday9Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-22d},endDate=${endDate-21d}")));
//		h.replaceDefinition(thursday9Indicator);
//		
//		CohortIndicator thursday10Indicator = new CohortIndicator();
//		thursday10Indicator.setName("thursday10Indicator");
//		thursday10Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday10Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday10Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-21d},endDate=${endDate-20d}")));
//		h.replaceDefinition(thursday10Indicator);
//		
//		CohortIndicator thursday11Indicator = new CohortIndicator();
//		thursday11Indicator.setName("thursday11Indicator");
//		thursday11Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday11Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday11Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-20d},endDate=${endDate-19d}")));
//		h.replaceDefinition(thursday11Indicator);
//		
//		
//		CohortIndicator thursday12Indicator = new CohortIndicator();
//		thursday12Indicator.setName("thursday12Indicator");
//		thursday12Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday12Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday12Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-19d},endDate=${endDate-18d}")));
//		h.replaceDefinition(thursday12Indicator);
//		
//		CohortIndicator thursday13Indicator = new CohortIndicator();
//		thursday13Indicator.setName("thursday13Indicator");
//		thursday13Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday13Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday13Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-18d},endDate=${endDate-17d}")));
//		h.replaceDefinition(thursday13Indicator);
//		
//		
//		CohortIndicator thursday14Indicator = new CohortIndicator();
//		thursday14Indicator.setName("thursday14Indicator");
//		thursday14Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday14Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday14Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-17d},endDate=${endDate-16d}")));
//		h.replaceDefinition(thursday14Indicator);
//		
//		CohortIndicator thursday15Indicator = new CohortIndicator();
//		thursday15Indicator.setName("thursday15Indicator");
//		thursday15Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday15Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday15Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-16d},endDate=${endDate-15d}")));
//		h.replaceDefinition(thursday15Indicator);
//		
//		CohortIndicator thursday16Indicator = new CohortIndicator();
//		thursday16Indicator.setName("thursday16Indicator");
//		thursday16Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday16Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday16Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-15d},endDate=${endDate-14d}")));
//		h.replaceDefinition(thursday16Indicator);
//		
//		CohortIndicator thursday17Indicator = new CohortIndicator();
//		thursday17Indicator.setName("thursday17Indicator");
//		thursday17Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday17Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday17Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-14d},endDate=${endDate-13d}")));
//		h.replaceDefinition(thursday17Indicator);
//		
//		CohortIndicator thursday18Indicator = new CohortIndicator();
//		thursday18Indicator.setName("thursday18Indicator");
//		thursday18Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday18Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday18Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-13d},endDate=${endDate-12d}")));
//		h.replaceDefinition(thursday18Indicator);
//		
//		CohortIndicator thursday19Indicator = new CohortIndicator();
//		thursday19Indicator.setName("thursday19Indicator");
//		thursday19Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday19Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday19Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-12d},endDate=${endDate-11d}")));
//		h.replaceDefinition(thursday19Indicator);
//		
//		CohortIndicator thursday20Indicator = new CohortIndicator();
//		thursday20Indicator.setName("thursday20Indicator");
//		thursday20Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday20Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday20Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-11d},endDate=${endDate-10d}")));
//		h.replaceDefinition(thursday20Indicator);
//		
//		CohortIndicator thursday21Indicator = new CohortIndicator();
//		thursday21Indicator.setName("thursday21Indicator");
//		thursday21Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday21Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday21Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-10d},endDate=${endDate-9d}")));
//		h.replaceDefinition(thursday21Indicator);
//		
//		
//		CohortIndicator thursday22Indicator = new CohortIndicator();
//		thursday22Indicator.setName("thursday22Indicator");
//		thursday22Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday22Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday22Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-9d},endDate=${endDate-8d}")));
//		h.replaceDefinition(thursday22Indicator);
//		
//		CohortIndicator thursday23Indicator = new CohortIndicator();
//		thursday23Indicator.setName("thursday23Indicator");
//		thursday23Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday23Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday23Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-8d},endDate=${endDate-7d}")));
//		h.replaceDefinition(thursday23Indicator);
//		
//		
//		CohortIndicator thursday24Indicator = new CohortIndicator();
//		thursday24Indicator.setName("thursday24Indicator");
//		thursday24Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday24Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday24Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-7d},endDate=${endDate-6d}")));
//		h.replaceDefinition(thursday24Indicator);
//		
//		CohortIndicator thursday25Indicator = new CohortIndicator();
//		thursday25Indicator.setName("thursday25Indicator");
//		thursday25Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday25Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday25Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-6d},endDate=${endDate-5d}")));
//		h.replaceDefinition(thursday25Indicator);
//		
//		CohortIndicator thursday26Indicator = new CohortIndicator();
//		thursday26Indicator.setName("thursday26Indicator");
//		thursday26Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday26Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday26Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-5d},endDate=${endDate-4d}")));
//		h.replaceDefinition(thursday26Indicator);
//		
//		CohortIndicator thursday27Indicator = new CohortIndicator();
//		thursday27Indicator.setName("thursday27Indicator");
//		thursday27Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday27Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday27Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-4d},endDate=${endDate-3d}")));
//		h.replaceDefinition(thursday27Indicator);
//		
//		CohortIndicator thursday28Indicator = new CohortIndicator();
//		thursday28Indicator.setName("thursday28Indicator");
//		thursday28Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday28Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday28Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-3d},endDate=${endDate-2d}")));
//		h.replaceDefinition(thursday28Indicator);
//		
//		CohortIndicator thursday29Indicator = new CohortIndicator();
//		thursday29Indicator.setName("thursday29Indicator");
//		thursday29Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday29Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday29Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-2d},endDate=${endDate-1d}")));
//		h.replaceDefinition(thursday29Indicator);
//		
//		CohortIndicator thursday30Indicator = new CohortIndicator();
//		thursday30Indicator.setName("thursday30Indicator");
//		thursday30Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday30Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday30Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate-1d},endDate=${endDate}")));
//		h.replaceDefinition(thursday30Indicator);
//		
//		CohortIndicator thursday31Indicator = new CohortIndicator();
//		thursday31Indicator.setName("thursday31Indicator");
//		thursday31Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		thursday31Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		thursday31Indicator.setCohortDefinition(new Mapped<CohortDefinition>(thursday,ParameterizableUtil.createParameterMappings("startDate=${endDate},endDate=${endDate+1d}")));
//		h.replaceDefinition(thursday31Indicator);
//		
//
////Friday
//		
//		SqlCohortDefinition friday=new SqlCohortDefinition();
//		friday.setName("friday");
//		friday.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,WEEKDAY(e.encounter_datetime) as peakdays FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0) as patientspeakdays where peakdays=4 and encounter_datetime>= :startDate and encounter_datetime< :endDate");
//		friday.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday.addParameter(new Parameter("endDate", "endDate", Date.class));
//		h.replaceCohortDefinition(friday);
//		
//		
//		CohortIndicator friday1Indicator = new CohortIndicator();
//		friday1Indicator.setName("friday1Indicator");
//		friday1Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday1Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday1Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-30d},endDate=${endDate-29d}")));
//		h.replaceDefinition(friday1Indicator);
//		
//		
//		CohortIndicator friday2Indicator = new CohortIndicator();
//		friday2Indicator.setName("friday2Indicator");
//		friday2Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday2Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday2Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-29d},endDate=${endDate-28d}")));
//		h.replaceDefinition(friday2Indicator);
//		
//		CohortIndicator friday3Indicator = new CohortIndicator();
//		friday3Indicator.setName("friday3Indicator");
//		friday3Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday3Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday3Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-28d},endDate=${endDate-27d}")));
//		h.replaceDefinition(friday3Indicator);
//		
//		
//		CohortIndicator friday4Indicator = new CohortIndicator();
//		friday4Indicator.setName("friday4Indicator");
//		friday4Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday4Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday4Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-27d},endDate=${endDate-26d}")));
//		h.replaceDefinition(friday4Indicator);
//		
//		CohortIndicator friday5Indicator = new CohortIndicator();
//		friday5Indicator.setName("friday5Indicator");
//		friday5Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday5Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday5Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-26d},endDate=${endDate-25d}")));
//		h.replaceDefinition(friday5Indicator);
//		
//		CohortIndicator friday6Indicator = new CohortIndicator();
//		friday6Indicator.setName("friday6Indicator");
//		friday6Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday6Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday6Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-25d},endDate=${endDate-24d}")));
//		h.replaceDefinition(friday6Indicator);
//		
//		CohortIndicator friday7Indicator = new CohortIndicator();
//		friday7Indicator.setName("friday7Indicator");
//		friday7Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday7Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday7Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-24d},endDate=${endDate-23d}")));
//		h.replaceDefinition(friday7Indicator);
//		
//		CohortIndicator friday8Indicator = new CohortIndicator();
//		friday8Indicator.setName("friday8Indicator");
//		friday8Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday8Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday8Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-23d},endDate=${endDate-22d}")));
//		h.replaceDefinition(friday8Indicator);
//		
//		CohortIndicator friday9Indicator = new CohortIndicator();
//		friday9Indicator.setName("friday9Indicator");
//		friday9Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday9Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday9Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-22d},endDate=${endDate-21d}")));
//		h.replaceDefinition(friday9Indicator);
//		
//		CohortIndicator friday10Indicator = new CohortIndicator();
//		friday10Indicator.setName("friday10Indicator");
//		friday10Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday10Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday10Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-21d},endDate=${endDate-20d}")));
//		h.replaceDefinition(friday10Indicator);
//		
//		CohortIndicator friday11Indicator = new CohortIndicator();
//		friday11Indicator.setName("friday11Indicator");
//		friday11Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday11Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday11Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-20d},endDate=${endDate-19d}")));
//		h.replaceDefinition(friday11Indicator);
//		
//		
//		CohortIndicator friday12Indicator = new CohortIndicator();
//		friday12Indicator.setName("friday12Indicator");
//		friday12Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday12Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday12Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-19d},endDate=${endDate-18d}")));
//		h.replaceDefinition(friday12Indicator);
//		
//		CohortIndicator friday13Indicator = new CohortIndicator();
//		friday13Indicator.setName("friday13Indicator");
//		friday13Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday13Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday13Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-18d},endDate=${endDate-17d}")));
//		h.replaceDefinition(friday13Indicator);
//		
//		
//		CohortIndicator friday14Indicator = new CohortIndicator();
//		friday14Indicator.setName("friday14Indicator");
//		friday14Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday14Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday14Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-17d},endDate=${endDate-16d}")));
//		h.replaceDefinition(friday14Indicator);
//		
//		CohortIndicator friday15Indicator = new CohortIndicator();
//		friday15Indicator.setName("friday15Indicator");
//		friday15Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday15Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday15Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-16d},endDate=${endDate-15d}")));
//		h.replaceDefinition(friday15Indicator);
//		
//		CohortIndicator friday16Indicator = new CohortIndicator();
//		friday16Indicator.setName("friday16Indicator");
//		friday16Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday16Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday16Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-15d},endDate=${endDate-14d}")));
//		h.replaceDefinition(friday16Indicator);
//		
//		CohortIndicator friday17Indicator = new CohortIndicator();
//		friday17Indicator.setName("friday17Indicator");
//		friday17Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday17Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday17Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-14d},endDate=${endDate-13d}")));
//		h.replaceDefinition(friday17Indicator);
//		
//		CohortIndicator friday18Indicator = new CohortIndicator();
//		friday18Indicator.setName("friday18Indicator");
//		friday18Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday18Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday18Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-13d},endDate=${endDate-12d}")));
//		h.replaceDefinition(friday18Indicator);
//		
//		CohortIndicator friday19Indicator = new CohortIndicator();
//		friday19Indicator.setName("friday19Indicator");
//		friday19Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday19Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday19Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-12d},endDate=${endDate-11d}")));
//		h.replaceDefinition(friday19Indicator);
//		
//		CohortIndicator friday20Indicator = new CohortIndicator();
//		friday20Indicator.setName("friday20Indicator");
//		friday20Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday20Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday20Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-11d},endDate=${endDate-10d}")));
//		h.replaceDefinition(friday20Indicator);
//		
//		CohortIndicator friday21Indicator = new CohortIndicator();
//		friday21Indicator.setName("friday21Indicator");
//		friday21Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday21Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday21Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-10d},endDate=${endDate-9d}")));
//		h.replaceDefinition(friday21Indicator);
//		
//		
//		CohortIndicator friday22Indicator = new CohortIndicator();
//		friday22Indicator.setName("friday22Indicator");
//		friday22Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday22Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday22Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-9d},endDate=${endDate-8d}")));
//		h.replaceDefinition(friday22Indicator);
//		
//		CohortIndicator friday23Indicator = new CohortIndicator();
//		friday23Indicator.setName("friday23Indicator");
//		friday23Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday23Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday23Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-8d},endDate=${endDate-7d}")));
//		h.replaceDefinition(friday23Indicator);
//		
//		
//		CohortIndicator friday24Indicator = new CohortIndicator();
//		friday24Indicator.setName("friday24Indicator");
//		friday24Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday24Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday24Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-7d},endDate=${endDate-6d}")));
//		h.replaceDefinition(friday24Indicator);
//		
//		CohortIndicator friday25Indicator = new CohortIndicator();
//		friday25Indicator.setName("friday25Indicator");
//		friday25Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday25Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday25Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-6d},endDate=${endDate-5d}")));
//		h.replaceDefinition(friday25Indicator);
//		
//		CohortIndicator friday26Indicator = new CohortIndicator();
//		friday26Indicator.setName("friday26Indicator");
//		friday26Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday26Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday26Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-5d},endDate=${endDate-4d}")));
//		h.replaceDefinition(friday26Indicator);
//		
//		CohortIndicator friday27Indicator = new CohortIndicator();
//		friday27Indicator.setName("friday27Indicator");
//		friday27Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday27Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday27Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-4d},endDate=${endDate-3d}")));
//		h.replaceDefinition(friday27Indicator);
//		
//		CohortIndicator friday28Indicator = new CohortIndicator();
//		friday28Indicator.setName("friday28Indicator");
//		friday28Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday28Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday28Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-3d},endDate=${endDate-2d}")));
//		h.replaceDefinition(friday28Indicator);
//		
//		CohortIndicator friday29Indicator = new CohortIndicator();
//		friday29Indicator.setName("friday29Indicator");
//		friday29Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday29Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday29Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-2d},endDate=${endDate-1d}")));
//		h.replaceDefinition(friday29Indicator);
//		
//		CohortIndicator friday30Indicator = new CohortIndicator();
//		friday30Indicator.setName("friday30Indicator");
//		friday30Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday30Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday30Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate-1d},endDate=${endDate}")));
//		h.replaceDefinition(friday30Indicator);
//		
//		CohortIndicator friday31Indicator = new CohortIndicator();
//		friday31Indicator.setName("friday31Indicator");
//		friday31Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		friday31Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		friday31Indicator.setCohortDefinition(new Mapped<CohortDefinition>(friday,ParameterizableUtil.createParameterMappings("startDate=${endDate},endDate=${endDate+1d}")));
//		h.replaceDefinition(friday31Indicator);		
//		
////saturday
//		
//		SqlCohortDefinition saturday=new SqlCohortDefinition();
//		saturday.setName("saturday");
//		saturday.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,WEEKDAY(e.encounter_datetime) as peakdays FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0) as patientspeakdays where peakdays=5 and encounter_datetime>= :startDate and encounter_datetime< :endDate");
//		saturday.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday.addParameter(new Parameter("endDate", "endDate", Date.class));
//		h.replaceCohortDefinition(saturday);
//		
//		
//		CohortIndicator saturday1Indicator = new CohortIndicator();
//		saturday1Indicator.setName("saturday1Indicator");
//		saturday1Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday1Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday1Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-30d},endDate=${endDate-29d}")));
//		h.replaceDefinition(saturday1Indicator);
//		
//		
//		CohortIndicator saturday2Indicator = new CohortIndicator();
//		saturday2Indicator.setName("saturday2Indicator");
//		saturday2Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday2Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday2Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-29d},endDate=${endDate-28d}")));
//		h.replaceDefinition(saturday2Indicator);
//		
//		CohortIndicator saturday3Indicator = new CohortIndicator();
//		saturday3Indicator.setName("saturday3Indicator");
//		saturday3Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday3Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday3Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-28d},endDate=${endDate-27d}")));
//		h.replaceDefinition(saturday3Indicator);
//		
//		
//		CohortIndicator saturday4Indicator = new CohortIndicator();
//		saturday4Indicator.setName("saturday4Indicator");
//		saturday4Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday4Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday4Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-27d},endDate=${endDate-26d}")));
//		h.replaceDefinition(saturday4Indicator);
//		
//		CohortIndicator saturday5Indicator = new CohortIndicator();
//		saturday5Indicator.setName("saturday5Indicator");
//		saturday5Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday5Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday5Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-26d},endDate=${endDate-25d}")));
//		h.replaceDefinition(saturday5Indicator);
//		
//		CohortIndicator saturday6Indicator = new CohortIndicator();
//		saturday6Indicator.setName("saturday6Indicator");
//		saturday6Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday6Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday6Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-25d},endDate=${endDate-24d}")));
//		h.replaceDefinition(saturday6Indicator);
//		
//		CohortIndicator saturday7Indicator = new CohortIndicator();
//		saturday7Indicator.setName("saturday7Indicator");
//		saturday7Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday7Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday7Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-24d},endDate=${endDate-23d}")));
//		h.replaceDefinition(saturday7Indicator);
//		
//		CohortIndicator saturday8Indicator = new CohortIndicator();
//		saturday8Indicator.setName("saturday8Indicator");
//		saturday8Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday8Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday8Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-23d},endDate=${endDate-22d}")));
//		h.replaceDefinition(saturday8Indicator);
//		
//		CohortIndicator saturday9Indicator = new CohortIndicator();
//		saturday9Indicator.setName("saturday9Indicator");
//		saturday9Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday9Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday9Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-22d},endDate=${endDate-21d}")));
//		h.replaceDefinition(saturday9Indicator);
//		
//		CohortIndicator saturday10Indicator = new CohortIndicator();
//		saturday10Indicator.setName("saturday10Indicator");
//		saturday10Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday10Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday10Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-21d},endDate=${endDate-20d}")));
//		h.replaceDefinition(saturday10Indicator);
//		
//		CohortIndicator saturday11Indicator = new CohortIndicator();
//		saturday11Indicator.setName("saturday11Indicator");
//		saturday11Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday11Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday11Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-20d},endDate=${endDate-19d}")));
//		h.replaceDefinition(saturday11Indicator);
//		
//		
//		CohortIndicator saturday12Indicator = new CohortIndicator();
//		saturday12Indicator.setName("saturday12Indicator");
//		saturday12Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday12Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday12Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-19d},endDate=${endDate-18d}")));
//		h.replaceDefinition(saturday12Indicator);
//		
//		CohortIndicator saturday13Indicator = new CohortIndicator();
//		saturday13Indicator.setName("saturday13Indicator");
//		saturday13Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday13Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday13Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-18d},endDate=${endDate-17d}")));
//		h.replaceDefinition(saturday13Indicator);
//		
//		
//		CohortIndicator saturday14Indicator = new CohortIndicator();
//		saturday14Indicator.setName("saturday14Indicator");
//		saturday14Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday14Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday14Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-17d},endDate=${endDate-16d}")));
//		h.replaceDefinition(saturday14Indicator);
//		
//		CohortIndicator saturday15Indicator = new CohortIndicator();
//		saturday15Indicator.setName("saturday15Indicator");
//		saturday15Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday15Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday15Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-16d},endDate=${endDate-15d}")));
//		h.replaceDefinition(saturday15Indicator);
//		
//		CohortIndicator saturday16Indicator = new CohortIndicator();
//		saturday16Indicator.setName("saturday16Indicator");
//		saturday16Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday16Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday16Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-15d},endDate=${endDate-14d}")));
//		h.replaceDefinition(saturday16Indicator);
//		
//		CohortIndicator saturday17Indicator = new CohortIndicator();
//		saturday17Indicator.setName("saturday17Indicator");
//		saturday17Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday17Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday17Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-14d},endDate=${endDate-13d}")));
//		h.replaceDefinition(saturday17Indicator);
//		
//		CohortIndicator saturday18Indicator = new CohortIndicator();
//		saturday18Indicator.setName("saturday18Indicator");
//		saturday18Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday18Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday18Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-13d},endDate=${endDate-12d}")));
//		h.replaceDefinition(saturday18Indicator);
//		
//		CohortIndicator saturday19Indicator = new CohortIndicator();
//		saturday19Indicator.setName("saturday19Indicator");
//		saturday19Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday19Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday19Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-12d},endDate=${endDate-11d}")));
//		h.replaceDefinition(saturday19Indicator);
//		
//		CohortIndicator saturday20Indicator = new CohortIndicator();
//		saturday20Indicator.setName("saturday20Indicator");
//		saturday20Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday20Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday20Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-11d},endDate=${endDate-10d}")));
//		h.replaceDefinition(saturday20Indicator);
//		
//		CohortIndicator saturday21Indicator = new CohortIndicator();
//		saturday21Indicator.setName("saturday21Indicator");
//		saturday21Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday21Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday21Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-10d},endDate=${endDate-9d}")));
//		h.replaceDefinition(saturday21Indicator);
//		
//		
//		CohortIndicator saturday22Indicator = new CohortIndicator();
//		saturday22Indicator.setName("saturday22Indicator");
//		saturday22Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday22Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday22Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-9d},endDate=${endDate-8d}")));
//		h.replaceDefinition(saturday22Indicator);
//		
//		CohortIndicator saturday23Indicator = new CohortIndicator();
//		saturday23Indicator.setName("saturday23Indicator");
//		saturday23Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday23Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday23Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-8d},endDate=${endDate-7d}")));
//		h.replaceDefinition(saturday23Indicator);
//		
//		
//		CohortIndicator saturday24Indicator = new CohortIndicator();
//		saturday24Indicator.setName("saturday24Indicator");
//		saturday24Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday24Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday24Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-7d},endDate=${endDate-6d}")));
//		h.replaceDefinition(saturday24Indicator);
//		
//		CohortIndicator saturday25Indicator = new CohortIndicator();
//		saturday25Indicator.setName("saturday25Indicator");
//		saturday25Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday25Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday25Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-6d},endDate=${endDate-5d}")));
//		h.replaceDefinition(saturday25Indicator);
//		
//		CohortIndicator saturday26Indicator = new CohortIndicator();
//		saturday26Indicator.setName("saturday26Indicator");
//		saturday26Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday26Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday26Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-5d},endDate=${endDate-4d}")));
//		h.replaceDefinition(saturday26Indicator);
//		
//		CohortIndicator saturday27Indicator = new CohortIndicator();
//		saturday27Indicator.setName("saturday27Indicator");
//		saturday27Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday27Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday27Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-4d},endDate=${endDate-3d}")));
//		h.replaceDefinition(saturday27Indicator);
//		
//		CohortIndicator saturday28Indicator = new CohortIndicator();
//		saturday28Indicator.setName("saturday28Indicator");
//		saturday28Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday28Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday28Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-3d},endDate=${endDate-2d}")));
//		h.replaceDefinition(saturday28Indicator);
//		
//		CohortIndicator saturday29Indicator = new CohortIndicator();
//		saturday29Indicator.setName("saturday29Indicator");
//		saturday29Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday29Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday29Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-2d},endDate=${endDate-1d}")));
//		h.replaceDefinition(saturday29Indicator);
//		
//		CohortIndicator saturday30Indicator = new CohortIndicator();
//		saturday30Indicator.setName("saturday30Indicator");
//		saturday30Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday30Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday30Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate-1d},endDate=${endDate}")));
//		h.replaceDefinition(saturday30Indicator);
//		
//		CohortIndicator saturday31Indicator = new CohortIndicator();
//		saturday31Indicator.setName("saturday31Indicator");
//		saturday31Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		saturday31Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		saturday31Indicator.setCohortDefinition(new Mapped<CohortDefinition>(saturday,ParameterizableUtil.createParameterMappings("startDate=${endDate},endDate=${endDate+1d}")));
//		h.replaceDefinition(saturday31Indicator);
//
//		
////Sunday
//		
//		SqlCohortDefinition sunday=new SqlCohortDefinition();
//		sunday.setName("sunday");
//		sunday.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,WEEKDAY(e.encounter_datetime) as peakdays FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0) as patientspeakdays where peakdays=6 and encounter_datetime>= :startDate and encounter_datetime< :endDate");
//		sunday.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday.addParameter(new Parameter("endDate", "endDate", Date.class));
//		h.replaceCohortDefinition(sunday);
//		
//		
//		CohortIndicator sunday1Indicator = new CohortIndicator();
//		sunday1Indicator.setName("sunday1Indicator");
//		sunday1Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday1Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday1Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-30d},endDate=${endDate-29d}")));
//		h.replaceDefinition(sunday1Indicator);
//		
//		
//		CohortIndicator sunday2Indicator = new CohortIndicator();
//		sunday2Indicator.setName("sunday2Indicator");
//		sunday2Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday2Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday2Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-29d},endDate=${endDate-28d}")));
//		h.replaceDefinition(sunday2Indicator);
//		
//		CohortIndicator sunday3Indicator = new CohortIndicator();
//		sunday3Indicator.setName("sunday3Indicator");
//		sunday3Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday3Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday3Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-28d},endDate=${endDate-27d}")));
//		h.replaceDefinition(sunday3Indicator);
//		
//		
//		CohortIndicator sunday4Indicator = new CohortIndicator();
//		sunday4Indicator.setName("sunday4Indicator");
//		sunday4Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday4Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday4Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-27d},endDate=${endDate-26d}")));
//		h.replaceDefinition(sunday4Indicator);
//		
//		CohortIndicator sunday5Indicator = new CohortIndicator();
//		sunday5Indicator.setName("sunday5Indicator");
//		sunday5Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday5Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday5Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-26d},endDate=${endDate-25d}")));
//		h.replaceDefinition(sunday5Indicator);
//		
//		CohortIndicator sunday6Indicator = new CohortIndicator();
//		sunday6Indicator.setName("sunday6Indicator");
//		sunday6Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday6Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday6Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-25d},endDate=${endDate-24d}")));
//		h.replaceDefinition(sunday6Indicator);
//		
//		CohortIndicator sunday7Indicator = new CohortIndicator();
//		sunday7Indicator.setName("sunday7Indicator");
//		sunday7Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday7Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday7Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-24d},endDate=${endDate-23d}")));
//		h.replaceDefinition(sunday7Indicator);
//		
//		CohortIndicator sunday8Indicator = new CohortIndicator();
//		sunday8Indicator.setName("sunday8Indicator");
//		sunday8Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday8Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday8Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-23d},endDate=${endDate-22d}")));
//		h.replaceDefinition(sunday8Indicator);
//		
//		CohortIndicator sunday9Indicator = new CohortIndicator();
//		sunday9Indicator.setName("sunday9Indicator");
//		sunday9Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday9Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday9Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-22d},endDate=${endDate-21d}")));
//		h.replaceDefinition(sunday9Indicator);
//		
//		CohortIndicator sunday10Indicator = new CohortIndicator();
//		sunday10Indicator.setName("sunday10Indicator");
//		sunday10Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday10Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday10Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-21d},endDate=${endDate-20d}")));
//		h.replaceDefinition(sunday10Indicator);
//		
//		CohortIndicator sunday11Indicator = new CohortIndicator();
//		sunday11Indicator.setName("sunday11Indicator");
//		sunday11Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday11Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday11Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-20d},endDate=${endDate-19d}")));
//		h.replaceDefinition(sunday11Indicator);
//		
//		
//		CohortIndicator sunday12Indicator = new CohortIndicator();
//		sunday12Indicator.setName("sunday12Indicator");
//		sunday12Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday12Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday12Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-19d},endDate=${endDate-18d}")));
//		h.replaceDefinition(sunday12Indicator);
//		
//		CohortIndicator sunday13Indicator = new CohortIndicator();
//		sunday13Indicator.setName("sunday13Indicator");
//		sunday13Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday13Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday13Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-18d},endDate=${endDate-17d}")));
//		h.replaceDefinition(sunday13Indicator);
//		
//		
//		CohortIndicator sunday14Indicator = new CohortIndicator();
//		sunday14Indicator.setName("sunday14Indicator");
//		sunday14Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday14Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday14Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-17d},endDate=${endDate-16d}")));
//		h.replaceDefinition(sunday14Indicator);
//		
//		CohortIndicator sunday15Indicator = new CohortIndicator();
//		sunday15Indicator.setName("sunday15Indicator");
//		sunday15Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday15Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday15Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-16d},endDate=${endDate-15d}")));
//		h.replaceDefinition(sunday15Indicator);
//		
//		CohortIndicator sunday16Indicator = new CohortIndicator();
//		sunday16Indicator.setName("sunday16Indicator");
//		sunday16Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday16Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday16Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-15d},endDate=${endDate-14d}")));
//		h.replaceDefinition(sunday16Indicator);
//		
//		CohortIndicator sunday17Indicator = new CohortIndicator();
//		sunday17Indicator.setName("sunday17Indicator");
//		sunday17Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday17Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday17Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-14d},endDate=${endDate-13d}")));
//		h.replaceDefinition(sunday17Indicator);
//		
//		CohortIndicator sunday18Indicator = new CohortIndicator();
//		sunday18Indicator.setName("sunday18Indicator");
//		sunday18Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday18Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday18Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-13d},endDate=${endDate-12d}")));
//		h.replaceDefinition(sunday18Indicator);
//		
//		CohortIndicator sunday19Indicator = new CohortIndicator();
//		sunday19Indicator.setName("sunday19Indicator");
//		sunday19Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday19Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday19Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-12d},endDate=${endDate-11d}")));
//		h.replaceDefinition(sunday19Indicator);
//		
//		CohortIndicator sunday20Indicator = new CohortIndicator();
//		sunday20Indicator.setName("sunday20Indicator");
//		sunday20Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday20Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday20Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-11d},endDate=${endDate-10d}")));
//		h.replaceDefinition(sunday20Indicator);
//		
//		CohortIndicator sunday21Indicator = new CohortIndicator();
//		sunday21Indicator.setName("sunday21Indicator");
//		sunday21Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday21Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday21Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-10d},endDate=${endDate-9d}")));
//		h.replaceDefinition(sunday21Indicator);
//		
//		
//		CohortIndicator sunday22Indicator = new CohortIndicator();
//		sunday22Indicator.setName("sunday22Indicator");
//		sunday22Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday22Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday22Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-9d},endDate=${endDate-8d}")));
//		h.replaceDefinition(sunday22Indicator);
//		
//		CohortIndicator sunday23Indicator = new CohortIndicator();
//		sunday23Indicator.setName("sunday23Indicator");
//		sunday23Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday23Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday23Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-8d},endDate=${endDate-7d}")));
//		h.replaceDefinition(sunday23Indicator);
//		
//		
//		CohortIndicator sunday24Indicator = new CohortIndicator();
//		sunday24Indicator.setName("sunday24Indicator");
//		sunday24Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday24Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday24Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-7d},endDate=${endDate-6d}")));
//		h.replaceDefinition(sunday24Indicator);
//		
//		CohortIndicator sunday25Indicator = new CohortIndicator();
//		sunday25Indicator.setName("sunday25Indicator");
//		sunday25Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday25Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday25Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-6d},endDate=${endDate-5d}")));
//		h.replaceDefinition(sunday25Indicator);
//		
//		CohortIndicator sunday26Indicator = new CohortIndicator();
//		sunday26Indicator.setName("sunday26Indicator");
//		sunday26Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday26Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday26Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-5d},endDate=${endDate-4d}")));
//		h.replaceDefinition(sunday26Indicator);
//		
//		CohortIndicator sunday27Indicator = new CohortIndicator();
//		sunday27Indicator.setName("sunday27Indicator");
//		sunday27Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday27Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday27Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-4d},endDate=${endDate-3d}")));
//		h.replaceDefinition(sunday27Indicator);
//		
//		CohortIndicator sunday28Indicator = new CohortIndicator();
//		sunday28Indicator.setName("sunday28Indicator");
//		sunday28Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday28Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday28Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-3d},endDate=${endDate-2d}")));
//		h.replaceDefinition(sunday28Indicator);
//		
//		CohortIndicator sunday29Indicator = new CohortIndicator();
//		sunday29Indicator.setName("sunday29Indicator");
//		sunday29Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday29Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday29Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-2d},endDate=${endDate-1d}")));
//		h.replaceDefinition(sunday29Indicator);
//		
//		CohortIndicator sunday30Indicator = new CohortIndicator();
//		sunday30Indicator.setName("sunday30Indicator");
//		sunday30Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday30Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday30Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate-1d},endDate=${endDate}")));
//		h.replaceDefinition(sunday30Indicator);
//		
//		CohortIndicator sunday31Indicator = new CohortIndicator();
//		sunday31Indicator.setName("sunday31Indicator");
//		sunday31Indicator.addParameter(new Parameter("startDate", "startDate", Date.class));
//		sunday31Indicator.addParameter(new Parameter("endDate", "endDate", Date.class));
//		sunday31Indicator.setCohortDefinition(new Mapped<CohortDefinition>(sunday,ParameterizableUtil.createParameterMappings("startDate=${endDate},endDate=${endDate+1d}")));
//		h.replaceDefinition(sunday31Indicator);



//======================================================================================
//       2nd Question
//======================================================================================
		
		
		// 2.1 Percent of patients who do not have an observation for temperature in the vitals


		AgeCohortDefinition lessThanFive = new AgeCohortDefinition(null, 5, null); 
		lessThanFive.setName("lessThanFive");
		h.replaceCohortDefinition(lessThanFive);
		
		SqlCohortDefinition patientsWithTemperatureInVitals=new SqlCohortDefinition("select e.patient_id from obs o,encounter e where o.encounter_id=e.encounter_id and o.person_id=e.patient_id and e.encounter_type="+vitalsEncTypeId+" and o.concept_id="+PrimaryCareReportConstants.TEMPERATURE_ID+" and o.voided=0 and e.voided=0");
		patientsWithTemperatureInVitals.setName("patientsWithTemperatureInVitals");
		h.replaceCohortDefinition(patientsWithTemperatureInVitals);
		
		
		CompositionCohortDefinition patientsUnder5WithoutTemperatureInVitals=new CompositionCohortDefinition();
		patientsUnder5WithoutTemperatureInVitals.setName("patientsUnder5WithoutTemperatureInVitals");
		patientsUnder5WithoutTemperatureInVitals.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsUnder5WithoutTemperatureInVitals.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsUnder5WithoutTemperatureInVitals.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsUnder5WithoutTemperatureInVitals.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsUnder5WithoutTemperatureInVitals.getSearches().put("patientsWithTemperatureInVitals", new Mapped<CohortDefinition>(patientsWithTemperatureInVitals,null));
		patientsUnder5WithoutTemperatureInVitals.getSearches().put("patientsWithPrimaryCareVitals", new Mapped<CohortDefinition>(patientsWithPrimaryCareVitals,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsUnder5WithoutTemperatureInVitals.getSearches().put("lessThanFive", new Mapped<CohortDefinition>(lessThanFive,null));
		patientsUnder5WithoutTemperatureInVitals.setCompositionString("patientsWithPrimaryCareVitals AND (NOT patientsWithTemperatureInVitals) AND lessThanFive");
		h.replaceCohortDefinition(patientsUnder5WithoutTemperatureInVitals);
		
		CompositionCohortDefinition patientsUnder5InRegistration=new CompositionCohortDefinition();
		patientsUnder5InRegistration.setName("patientsUnder5InRegistration");
		patientsUnder5InRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsUnder5InRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsUnder5InRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsUnder5InRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsUnder5InRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsUnder5InRegistration.getSearches().put("lessThanFive", new Mapped<CohortDefinition>(lessThanFive,null));
		patientsUnder5InRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND lessThanFive");
		h.replaceCohortDefinition(patientsUnder5InRegistration);
		
		
		CohortIndicator patientsWithoutTemperatureInVitalsIndicator = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(patientsUnder5WithoutTemperatureInVitals, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsUnder5InRegistration, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		patientsWithoutTemperatureInVitalsIndicator.setName("patientsWithoutTemperatureInVitalsIndicator");
		patientsWithoutTemperatureInVitalsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithoutTemperatureInVitalsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(patientsWithoutTemperatureInVitalsIndicator);
		
//2.2 Percent of children under 5 who did have observation for temperature, and actually had a fever (were sick, temperature was higher than normal)

		SqlCohortDefinition patientsWithTemperatureGreaterThanNormalInVitals=new SqlCohortDefinition("select e.patient_id from obs o,encounter e where o.encounter_id=e.encounter_id and o.person_id=e.patient_id and e.encounter_type="+vitalsEncTypeId+" and o.concept_id="+PrimaryCareReportConstants.TEMPERATURE_ID+" and o.value_numeric>37.0 and o.voided=0 and e.voided=0");
		patientsWithTemperatureGreaterThanNormalInVitals.setName("patientsWithTemperatureGreaterThanNormalInVitals");
		h.replaceCohortDefinition(patientsWithTemperatureGreaterThanNormalInVitals);
		
		CompositionCohortDefinition patientsUnder5WithTemperatureGreaterThanNormalInVitals=new CompositionCohortDefinition();
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.setName("patientsUnder5WithTemperatureGreaterThanNormalInVitals");
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.getSearches().put("patientsWithTemperatureGreaterThanNormalInVitals", new Mapped<CohortDefinition>(patientsWithTemperatureGreaterThanNormalInVitals,null));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.getSearches().put("patientsWithPrimaryCareVitals", new Mapped<CohortDefinition>(patientsWithPrimaryCareVitals,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.getSearches().put("lessThanFive", new Mapped<CohortDefinition>(lessThanFive,null));
		patientsUnder5WithTemperatureGreaterThanNormalInVitals.setCompositionString("patientsWithPrimaryCareVitals AND patientsWithTemperatureGreaterThanNormalInVitals AND lessThanFive");
		h.replaceCohortDefinition(patientsUnder5WithTemperatureGreaterThanNormalInVitals);
		
		CompositionCohortDefinition patientsUnder5WithTemperatureInVitals=new CompositionCohortDefinition();
		patientsUnder5WithTemperatureInVitals.setName("patientsUnder5WithTemperatureInVitals");
		patientsUnder5WithTemperatureInVitals.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsUnder5WithTemperatureInVitals.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsUnder5WithTemperatureInVitals.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsUnder5WithTemperatureInVitals.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsUnder5WithTemperatureInVitals.getSearches().put("patientsWithTemperatureInVitals", new Mapped<CohortDefinition>(patientsWithTemperatureInVitals,null));
		patientsUnder5WithTemperatureInVitals.getSearches().put("patientsWithPrimaryCareVitals", new Mapped<CohortDefinition>(patientsWithPrimaryCareVitals,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsUnder5WithTemperatureInVitals.getSearches().put("lessThanFive", new Mapped<CohortDefinition>(lessThanFive,null));
		patientsUnder5WithTemperatureInVitals.setCompositionString("patientsWithPrimaryCareVitals AND patientsWithTemperatureInVitals AND lessThanFive");
		h.replaceCohortDefinition(patientsUnder5WithTemperatureInVitals);
		
		CohortIndicator patientsWithTemperatureGreaterThanNormalInVitalsIndicator = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(patientsUnder5WithTemperatureGreaterThanNormalInVitals, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsUnder5WithTemperatureInVitals, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		patientsWithTemperatureGreaterThanNormalInVitalsIndicator.setName("patientsWithTemperatureGreaterThanNormalInVitalsIndicator");
		patientsWithTemperatureGreaterThanNormalInVitalsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithTemperatureGreaterThanNormalInVitalsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(patientsWithTemperatureGreaterThanNormalInVitalsIndicator);
//2.3 Percent of all registered patients under 5 who had a fever

		CohortIndicator allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(patientsUnder5WithTemperatureGreaterThanNormalInVitals, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsUnder5InRegistration, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator.setName("allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator");
		allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator);

//========================================================================
// 3. Registration Speed during Peak Hours
//========================================================================
	
// Peak Hours (08:00:00 to 10:00:00)
		
		SqlCohortDefinition peakHours=new SqlCohortDefinition();
		peakHours.setName("peakHours");
		peakHours.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,TIME(e.encounter_datetime) as peakhours,WEEKDAY(e.encounter_datetime) as peakdays FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0) as patientspeakhours where peakhours>= :startTime and peakhours<= :endTime and peakdays<=4 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		peakHours.addParameter(new Parameter("startDate", "startDate", Date.class));
		peakHours.addParameter(new Parameter("endDate", "endDate", Date.class));
		peakHours.addParameter(new Parameter("startTime", "startTime", Date.class));
		peakHours.addParameter(new Parameter("endTime", "endTime", Date.class));
		h.replaceCohortDefinition(peakHours);
		
// Peak Days (Monday to Friday)
		
		SqlCohortDefinition peakDays=new SqlCohortDefinition();
		peakDays.setName("peakDays");
		peakDays.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,WEEKDAY(e.encounter_datetime) as peakdays FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0) as patientspeakdays where peakdays<=4 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		peakDays.addParameter(new Parameter("startDate", "startDate", Date.class));
		peakDays.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceCohortDefinition(peakDays);
		
//
		CompositionCohortDefinition peakHoursAndPeakDays=new CompositionCohortDefinition();
		peakHoursAndPeakDays.setName("peakHoursAndPeakDays");
		peakHoursAndPeakDays.addParameter(new Parameter("startDate", "startDate", Date.class));
		peakHoursAndPeakDays.addParameter(new Parameter("endDate", "endDate", Date.class));
		peakHoursAndPeakDays.addParameter(new Parameter("startTime", "startTime", Date.class));
		peakHoursAndPeakDays.addParameter(new Parameter("endTime", "endTime", Date.class));
		peakHoursAndPeakDays.getSearches().put("peakHours", new Mapped<CohortDefinition>(peakHours,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},startTime=08:00:00,endTime=10:00:00")));
		peakHoursAndPeakDays.getSearches().put("peakDays", new Mapped<CohortDefinition>(peakDays,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		peakHoursAndPeakDays.setCompositionString("peakHours AND peakDays");
		h.replaceCohortDefinition(peakHoursAndPeakDays);
		
		
		CohortIndicator peakHoursAndPeakDaysIndicator = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(peakHoursAndPeakDays, 
				ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},startTime=08:00:00,endTime=10:00:00")), 
				new Mapped<CohortDefinition>(peakDays, 
				ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), 
				null);
		peakHoursAndPeakDaysIndicator.setName("peakHoursAndPeakDaysIndicator");
		peakHoursAndPeakDaysIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		peakHoursAndPeakDaysIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(peakHoursAndPeakDaysIndicator);

		
//========================================================================
// 4. How many registration encounters are paid for by Medical Insurance
//========================================================================
		
		//Mutuelle Insurance cohort definition
		
		CodedObsCohortDefinition MUTUELLEInsCohortDef=makeCodedObsCohortDefinition(PrimaryCareReportConstants.RWANDA_INSURANCE_TYPE, PrimaryCareReportConstants.MUTUELLE, SetComparator.IN, TimeModifier.ANY);
		MUTUELLEInsCohortDef.setName("MUTUELLEInsCohortDef");
		MUTUELLEInsCohortDef.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		MUTUELLEInsCohortDef.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(MUTUELLEInsCohortDef);
		

		CodedObsCohortDefinition RAMAInsCohortDef=makeCodedObsCohortDefinition(PrimaryCareReportConstants.RWANDA_INSURANCE_TYPE, PrimaryCareReportConstants.RAMA, SetComparator.IN, TimeModifier.ANY);
		RAMAInsCohortDef.setName("RAMAInsCohortDef");
		RAMAInsCohortDef.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		RAMAInsCohortDef.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(RAMAInsCohortDef);
		

		CodedObsCohortDefinition MMIInsCohortDef=makeCodedObsCohortDefinition(PrimaryCareReportConstants.RWANDA_INSURANCE_TYPE, PrimaryCareReportConstants.MMI, SetComparator.IN, TimeModifier.ANY);
		MMIInsCohortDef.setName("MMIInsCohortDef");
		MMIInsCohortDef.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		MMIInsCohortDef.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(MMIInsCohortDef);

		CodedObsCohortDefinition MEDIPLANInsCohortDef=makeCodedObsCohortDefinition(PrimaryCareReportConstants.RWANDA_INSURANCE_TYPE, PrimaryCareReportConstants.MEDIPLAN, SetComparator.IN, TimeModifier.ANY);
		MEDIPLANInsCohortDef.setName("MEDIPLANInsCohortDef");
		MEDIPLANInsCohortDef.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		MEDIPLANInsCohortDef.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(MEDIPLANInsCohortDef);

		CodedObsCohortDefinition CORARInsCohortDef=makeCodedObsCohortDefinition(PrimaryCareReportConstants.RWANDA_INSURANCE_TYPE, PrimaryCareReportConstants.CORAR, SetComparator.IN, TimeModifier.ANY);
		CORARInsCohortDef.setName("CORARInsCohortDef");
		CORARInsCohortDef.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		CORARInsCohortDef.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(CORARInsCohortDef);

		CodedObsCohortDefinition NONEInsCohortDef=makeCodedObsCohortDefinition(PrimaryCareReportConstants.RWANDA_INSURANCE_TYPE, PrimaryCareReportConstants.NONE, SetComparator.IN, TimeModifier.ANY);
		NONEInsCohortDef.setName("NONEInsCohortDef");
		NONEInsCohortDef.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		NONEInsCohortDef.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(NONEInsCohortDef);
		
// 4.1 Percent of patients who are missing an insurance type in registration encounter

		CompositionCohortDefinition patientsMissingIns=new CompositionCohortDefinition();
		patientsMissingIns.setName("patientsMissingIns");
		patientsMissingIns.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsMissingIns.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsMissingIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsMissingIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsMissingIns.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns.getSearches().put("MUTUELLEInsCohortDef", new Mapped<CohortDefinition>(MUTUELLEInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns.getSearches().put("RAMAInsCohortDef", new Mapped<CohortDefinition>(RAMAInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns.getSearches().put("MMIInsCohortDef", new Mapped<CohortDefinition>(MMIInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns.getSearches().put("MEDIPLANInsCohortDef", new Mapped<CohortDefinition>(MEDIPLANInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns.getSearches().put("CORARInsCohortDef", new Mapped<CohortDefinition>(CORARInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns.getSearches().put("NONEInsCohortDef", new Mapped<CohortDefinition>(NONEInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsMissingIns.setCompositionString("patientsWithPrimaryCareRegistration AND (NOT(MUTUELLEInsCohortDef OR RAMAInsCohortDef OR MMIInsCohortDef OR MEDIPLANInsCohortDef OR CORARInsCohortDef OR NONEInsCohortDef))");
		h.replaceCohortDefinition(patientsMissingIns);
		
		CohortIndicator percentOfPatientsMissingInsIndicator = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(patientsMissingIns, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		percentOfPatientsMissingInsIndicator.setName("percentOfPatientsMissingInsIndicator");
		percentOfPatientsMissingInsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		percentOfPatientsMissingInsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsMissingInsIndicator);
		
//4.2  Number of patients who are missing an insurance type in registration encounter
		CohortIndicator numberOfPatientsMissingInsIndicator = new CohortIndicator();
		numberOfPatientsMissingInsIndicator.setName("numberOfPatientsMissingInsIndicator");
		numberOfPatientsMissingInsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		numberOfPatientsMissingInsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		numberOfPatientsMissingInsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsMissingIns,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(numberOfPatientsMissingInsIndicator);
		
		
//4.3.1 Percent of patients with MUTUELLE insurance in registration encounter
		
		CompositionCohortDefinition patientsWithMUTUELLEIns=new CompositionCohortDefinition();
		patientsWithMUTUELLEIns.setName("patientsWithMUTUELLEIns");
		patientsWithMUTUELLEIns.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMUTUELLEIns.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMUTUELLEIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEIns.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEIns.getSearches().put("MUTUELLEInsCohortDef", new Mapped<CohortDefinition>(MUTUELLEInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEIns.setCompositionString("patientsWithPrimaryCareRegistration AND MUTUELLEInsCohortDef");
		h.replaceCohortDefinition(patientsWithMUTUELLEIns);
		
		CohortIndicator percentOfPatientsWithMUTUELLEInsIndicator = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(patientsWithMUTUELLEIns, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		percentOfPatientsWithMUTUELLEInsIndicator.setName("percentOfPatientsWithMUTUELLEInsIndicator");
		percentOfPatientsWithMUTUELLEInsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		percentOfPatientsWithMUTUELLEInsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsWithMUTUELLEInsIndicator);

//4.3.2 Percent of patients with RAMA insurance in registration encounter
		
		CompositionCohortDefinition patientsWithRAMAIns=new CompositionCohortDefinition();
		patientsWithRAMAIns.setName("patientsWithRAMAIns");
		patientsWithRAMAIns.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithRAMAIns.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithRAMAIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAIns.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAIns.getSearches().put("RAMAInsCohortDef", new Mapped<CohortDefinition>(RAMAInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAIns.setCompositionString("patientsWithPrimaryCareRegistration AND RAMAInsCohortDef");
		h.replaceCohortDefinition(patientsWithRAMAIns);
		
		CohortIndicator percentOfPatientsWithRAMAInsIndicator = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(patientsWithRAMAIns, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		percentOfPatientsWithRAMAInsIndicator.setName("percentOfPatientsWithRAMAInsIndicator");
		percentOfPatientsWithRAMAInsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		percentOfPatientsWithRAMAInsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsWithRAMAInsIndicator);

//4.3.3 Percent of patients with MMI insurance in registration encounter
		
		CompositionCohortDefinition patientsWithMMIIns=new CompositionCohortDefinition();
		patientsWithMMIIns.setName("patientsWithMMIIns");
		patientsWithMMIIns.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMMIIns.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMMIIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIIns.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIIns.getSearches().put("MMIInsCohortDef", new Mapped<CohortDefinition>(MMIInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIIns.setCompositionString("patientsWithPrimaryCareRegistration AND MMIInsCohortDef");
		h.replaceCohortDefinition(patientsWithMMIIns);
		
		CohortIndicator percentOfPatientsWithMMIInsIndicator = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(patientsWithMMIIns, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		percentOfPatientsWithMMIInsIndicator.setName("percentOfPatientsWithMMIInsIndicator");
		percentOfPatientsWithMMIInsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		percentOfPatientsWithMMIInsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsWithMMIInsIndicator);

//4.3.4 Percent of patients with MEDIPLAN insurance in registration encounter
		
		CompositionCohortDefinition patientsWithMEDIPLANIns=new CompositionCohortDefinition();
		patientsWithMEDIPLANIns.setName("patientsWithMEDIPLANIns");
		patientsWithMEDIPLANIns.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMEDIPLANIns.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMEDIPLANIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANIns.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANIns.getSearches().put("MEDIPLANInsCohortDef", new Mapped<CohortDefinition>(MEDIPLANInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANIns.setCompositionString("patientsWithPrimaryCareRegistration AND MEDIPLANInsCohortDef");
		h.replaceCohortDefinition(patientsWithMEDIPLANIns);
		
		CohortIndicator percentOfPatientsWithMEDIPLANInsIndicator = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(patientsWithMEDIPLANIns, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		percentOfPatientsWithMEDIPLANInsIndicator.setName("percentOfPatientsWithMEDIPLANInsIndicator");
		percentOfPatientsWithMEDIPLANInsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		percentOfPatientsWithMEDIPLANInsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsWithMEDIPLANInsIndicator);

//4.3.5 Percent of patients with CORAR insurance in registration encounter
		
		CompositionCohortDefinition patientsWithCORARIns=new CompositionCohortDefinition();
		patientsWithCORARIns.setName("patientsWithCORARIns");
		patientsWithCORARIns.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithCORARIns.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithCORARIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARIns.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARIns.getSearches().put("CORARInsCohortDef", new Mapped<CohortDefinition>(CORARInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARIns.setCompositionString("patientsWithPrimaryCareRegistration AND CORARInsCohortDef");
		h.replaceCohortDefinition(patientsWithCORARIns);
		
		CohortIndicator percentOfPatientsWithCORARInsIndicator = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(patientsWithCORARIns, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		percentOfPatientsWithCORARInsIndicator.setName("percentOfPatientsWithCORARInsIndicator");
		percentOfPatientsWithCORARInsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		percentOfPatientsWithCORARInsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsWithCORARInsIndicator);

//4.3.6 Percent of patients with CORAR insurance in registration encounter
		
		CompositionCohortDefinition patientsWithNONEIns=new CompositionCohortDefinition();
		patientsWithNONEIns.setName("patientsWithNONEIns");
		patientsWithNONEIns.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithNONEIns.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithNONEIns.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEIns.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEIns.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEIns.getSearches().put("NONEInsCohortDef", new Mapped<CohortDefinition>(NONEInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEIns.setCompositionString("patientsWithPrimaryCareRegistration AND NONEInsCohortDef");
		h.replaceCohortDefinition(patientsWithNONEIns);
		
		CohortIndicator percentOfPatientsWithNONEInsIndicator = CohortIndicator.newFractionIndicator
		(null,new Mapped<CohortDefinition>(patientsWithNONEIns, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration, 
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")), 
				null);
		percentOfPatientsWithNONEInsIndicator.setName("percentOfPatientsWithNONEInsIndicator");
		percentOfPatientsWithNONEInsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		percentOfPatientsWithNONEInsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceDefinition(percentOfPatientsWithNONEInsIndicator);

//========================================================================
// 5. For all insurance types, how many patients come back for multiple visits, and how many visits:
//========================================================================

		SqlCohortDefinition patientsWithOneVisit=new SqlCohortDefinition();
		patientsWithOneVisit.setName("patientsWithOneVisit");
		patientsWithOneVisit.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0 group by e.patient_id) as patientregistrationtimes where timesofregistration=1 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		patientsWithOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceCohortDefinition(patientsWithOneVisit);
		
		SqlCohortDefinition patientsWithTwoVisits=new SqlCohortDefinition();
		patientsWithTwoVisits.setName("patientsWithTwoVisits");
		patientsWithTwoVisits.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0 group by e.patient_id) as patientregistrationtimes where timesofregistration=2 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		patientsWithTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceCohortDefinition(patientsWithTwoVisits);
		
		SqlCohortDefinition patientsWithThreeVisits=new SqlCohortDefinition();
		patientsWithThreeVisits.setName("patientsWithThreeVisits");
		patientsWithThreeVisits.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0 group by e.patient_id) as patientregistrationtimes where timesofregistration=3 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		patientsWithThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceCohortDefinition(patientsWithThreeVisits);
		
		SqlCohortDefinition patientsWithGreaterThanThreeVisits=new SqlCohortDefinition();
		patientsWithGreaterThanThreeVisits.setName("patientsWithGreaterThanThreeVisits");
		patientsWithGreaterThanThreeVisits.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type="+registrationEncTypeId+" and e.voided=0 group by e.patient_id) as patientregistrationtimes where timesofregistration>3 and encounter_datetime>= :startDate and encounter_datetime<= :endDate");
		patientsWithGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		h.replaceCohortDefinition(patientsWithGreaterThanThreeVisits);
		
		
// 5.1.1 Patients with Mutuelle Insurance and 1 visit
		CompositionCohortDefinition patientsWithMUTUELLEInsAndOneVisit=new CompositionCohortDefinition();
		patientsWithMUTUELLEInsAndOneVisit.setName("patientsWithMUTUELLEInsAndOneVisit");
		patientsWithMUTUELLEInsAndOneVisit.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMUTUELLEInsAndOneVisit.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMUTUELLEInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndOneVisit.getSearches().put("patientsWithOneVisit", new Mapped<CohortDefinition>(patientsWithOneVisit,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMUTUELLEInsAndOneVisit.getSearches().put("MUTUELLEInsCohortDef", new Mapped<CohortDefinition>(MUTUELLEInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEInsAndOneVisit.setCompositionString("patientsWithOneVisit AND MUTUELLEInsCohortDef");
		h.replaceCohortDefinition(patientsWithMUTUELLEInsAndOneVisit);
		
		CohortIndicator patientsWithMUTUELLEInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithMUTUELLEInsAndOneVisitIndicator.setName("patientsWithMUTUELLEInsAndOneVisitIndicator");
		patientsWithMUTUELLEInsAndOneVisitIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndOneVisitIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndOneVisitIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMUTUELLEInsAndOneVisit,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMUTUELLEInsAndOneVisitIndicator);
// 5.1.2 Patients with RAMA Insurance and 1 visit
		CompositionCohortDefinition patientsWithRAMAInsAndOneVisit=new CompositionCohortDefinition();
		patientsWithRAMAInsAndOneVisit.setName("patientsWithRAMAInsAndOneVisit");
		patientsWithRAMAInsAndOneVisit.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithRAMAInsAndOneVisit.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithRAMAInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndOneVisit.getSearches().put("patientsWithOneVisit", new Mapped<CohortDefinition>(patientsWithOneVisit,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithRAMAInsAndOneVisit.getSearches().put("RAMAInsCohortDef", new Mapped<CohortDefinition>(RAMAInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAInsAndOneVisit.setCompositionString("patientsWithOneVisit AND RAMAInsCohortDef");
		h.replaceCohortDefinition(patientsWithRAMAInsAndOneVisit);
		
		CohortIndicator patientsWithRAMAInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithRAMAInsAndOneVisitIndicator.setName("patientsWithRAMAInsAndOneVisitIndicator");
		patientsWithRAMAInsAndOneVisitIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAInsAndOneVisitIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndOneVisitIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithRAMAInsAndOneVisit,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithRAMAInsAndOneVisitIndicator);
// 5.1.3 Patients with MMI Insurance and 1 visit
		CompositionCohortDefinition patientsWithMMIInsAndOneVisit=new CompositionCohortDefinition();
		patientsWithMMIInsAndOneVisit.setName("patientsWithMMIInsAndOneVisit");
		patientsWithMMIInsAndOneVisit.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMMIInsAndOneVisit.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMMIInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIInsAndOneVisit.getSearches().put("patientsWithOneVisit", new Mapped<CohortDefinition>(patientsWithOneVisit,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMMIInsAndOneVisit.getSearches().put("MMIInsCohortDef", new Mapped<CohortDefinition>(MMIInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIInsAndOneVisit.setCompositionString("patientsWithOneVisit AND MMIInsCohortDef");
		h.replaceCohortDefinition(patientsWithMMIInsAndOneVisit);
		
		CohortIndicator patientsWithMMIInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithMMIInsAndOneVisitIndicator.setName("patientsWithMMIInsAndOneVisitIndicator");
		patientsWithMMIInsAndOneVisitIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIInsAndOneVisitIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIInsAndOneVisitIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMMIInsAndOneVisit,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMMIInsAndOneVisitIndicator);
		
// 5.1.4 Patients with MEDIPLAN Insurance and 1 visit
		CompositionCohortDefinition patientsWithMEDIPLANInsAndOneVisit=new CompositionCohortDefinition();
		patientsWithMEDIPLANInsAndOneVisit.setName("patientsWithMEDIPLANInsAndOneVisit");
		patientsWithMEDIPLANInsAndOneVisit.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMEDIPLANInsAndOneVisit.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMEDIPLANInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndOneVisit.getSearches().put("patientsWithOneVisit", new Mapped<CohortDefinition>(patientsWithOneVisit,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMEDIPLANInsAndOneVisit.getSearches().put("MEDIPLANInsCohortDef", new Mapped<CohortDefinition>(MEDIPLANInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANInsAndOneVisit.setCompositionString("patientsWithOneVisit AND MEDIPLANInsCohortDef");
		h.replaceCohortDefinition(patientsWithMEDIPLANInsAndOneVisit);
		
		CohortIndicator patientsWithMEDIPLANInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithMEDIPLANInsAndOneVisitIndicator.setName("patientsWithMEDIPLANInsAndOneVisitIndicator");
		patientsWithMEDIPLANInsAndOneVisitIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndOneVisitIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndOneVisitIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMEDIPLANInsAndOneVisit,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMEDIPLANInsAndOneVisitIndicator);

// 5.1.5 Patients with CORAR Insurance and 1 visit
		CompositionCohortDefinition patientsWithCORARInsAndOneVisit=new CompositionCohortDefinition();
		patientsWithCORARInsAndOneVisit.setName("patientsWithCORARInsAndOneVisit");
		patientsWithCORARInsAndOneVisit.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithCORARInsAndOneVisit.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithCORARInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndOneVisit.getSearches().put("patientsWithOneVisit", new Mapped<CohortDefinition>(patientsWithOneVisit,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithCORARInsAndOneVisit.getSearches().put("CORARInsCohortDef", new Mapped<CohortDefinition>(CORARInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARInsAndOneVisit.setCompositionString("patientsWithOneVisit AND CORARInsCohortDef");
		h.replaceCohortDefinition(patientsWithCORARInsAndOneVisit);
		
		CohortIndicator patientsWithCORARInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithCORARInsAndOneVisitIndicator.setName("patientsWithCORARInsAndOneVisitIndicator");
		patientsWithCORARInsAndOneVisitIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARInsAndOneVisitIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndOneVisitIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithCORARInsAndOneVisit,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithCORARInsAndOneVisitIndicator);

// 5.1.6 Patients with NONE Insurance and 1 visit
		CompositionCohortDefinition patientsWithNONEInsAndOneVisit=new CompositionCohortDefinition();
		patientsWithNONEInsAndOneVisit.setName("patientsWithNONEInsAndOneVisit");
		patientsWithNONEInsAndOneVisit.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithNONEInsAndOneVisit.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithNONEInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndOneVisit.getSearches().put("patientsWithOneVisit", new Mapped<CohortDefinition>(patientsWithOneVisit,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithNONEInsAndOneVisit.getSearches().put("NONEInsCohortDef", new Mapped<CohortDefinition>(NONEInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEInsAndOneVisit.setCompositionString("patientsWithOneVisit AND NONEInsCohortDef");
		h.replaceCohortDefinition(patientsWithNONEInsAndOneVisit);
		
		CohortIndicator patientsWithNONEInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithNONEInsAndOneVisitIndicator.setName("patientsWithNONEInsAndOneVisitIndicator");
		patientsWithNONEInsAndOneVisitIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEInsAndOneVisitIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndOneVisitIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithNONEInsAndOneVisit,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithNONEInsAndOneVisitIndicator);
// 5.1.7 Patients without Insurance and 1 visit
		CompositionCohortDefinition patientsWithMissingInsAndOneVisit=new CompositionCohortDefinition();
		patientsWithMissingInsAndOneVisit.setName("patientsWithMissingInsAndOneVisit");
		patientsWithMissingInsAndOneVisit.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMissingInsAndOneVisit.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMissingInsAndOneVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMissingInsAndOneVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndOneVisit.getSearches().put("patientsWithOneVisit", new Mapped<CohortDefinition>(patientsWithOneVisit,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMissingInsAndOneVisit.getSearches().put("patientsMissingIns", new Mapped<CohortDefinition>(patientsMissingIns,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMissingInsAndOneVisit.setCompositionString("patientsWithOneVisit AND patientsMissingIns");
		h.replaceCohortDefinition(patientsWithMissingInsAndOneVisit);
		
		CohortIndicator patientsWithMissingInsAndOneVisitIndicator = new CohortIndicator();
		patientsWithMissingInsAndOneVisitIndicator.setName("patientsWithMissingInsAndOneVisitIndicator");
		patientsWithMissingInsAndOneVisitIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMissingInsAndOneVisitIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndOneVisitIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMissingInsAndOneVisit,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMissingInsAndOneVisitIndicator);

		
// 5.2.1 Patients with Mutuelle Insurance and 2 visits
		CompositionCohortDefinition patientsWithMUTUELLEInsAndTwoVisits=new CompositionCohortDefinition();
		patientsWithMUTUELLEInsAndTwoVisits.setName("patientsWithMUTUELLEInsAndTwoVisits");
		patientsWithMUTUELLEInsAndTwoVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMUTUELLEInsAndTwoVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMUTUELLEInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndTwoVisits.getSearches().put("patientsWithTwoVisits", new Mapped<CohortDefinition>(patientsWithTwoVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMUTUELLEInsAndTwoVisits.getSearches().put("MUTUELLEInsCohortDef", new Mapped<CohortDefinition>(MUTUELLEInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND MUTUELLEInsCohortDef");
		h.replaceCohortDefinition(patientsWithMUTUELLEInsAndTwoVisits);
		
		CohortIndicator patientsWithMUTUELLEInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithMUTUELLEInsAndTwoVisitsIndicator.setName("patientsWithMUTUELLEInsAndTwoVisitsIndicator");
		patientsWithMUTUELLEInsAndTwoVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndTwoVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndTwoVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMUTUELLEInsAndTwoVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMUTUELLEInsAndTwoVisitsIndicator);
// 5.2.2 Patients with RAMA Insurance and 2 visits
		CompositionCohortDefinition patientsWithRAMAInsAndTwoVisits=new CompositionCohortDefinition();
		patientsWithRAMAInsAndTwoVisits.setName("patientsWithRAMAInsAndTwoVisits");
		patientsWithRAMAInsAndTwoVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithRAMAInsAndTwoVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithRAMAInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndTwoVisits.getSearches().put("patientsWithTwoVisits", new Mapped<CohortDefinition>(patientsWithTwoVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithRAMAInsAndTwoVisits.getSearches().put("RAMAInsCohortDef", new Mapped<CohortDefinition>(RAMAInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND RAMAInsCohortDef");
		h.replaceCohortDefinition(patientsWithRAMAInsAndTwoVisits);
		
		CohortIndicator patientsWithRAMAInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithRAMAInsAndTwoVisitsIndicator.setName("patientsWithRAMAInsAndTwoVisitsIndicator");
		patientsWithRAMAInsAndTwoVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAInsAndTwoVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndTwoVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithRAMAInsAndTwoVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithRAMAInsAndTwoVisitsIndicator);
// 5.2.3 Patients with MMI Insurance and 2 visits
		CompositionCohortDefinition patientsWithMMIInsAndTwoVisits=new CompositionCohortDefinition();
		patientsWithMMIInsAndTwoVisits.setName("patientsWithMMIInsAndTwoVisits");
		patientsWithMMIInsAndTwoVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMMIInsAndTwoVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMMIInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIInsAndTwoVisits.getSearches().put("patientsWithTwoVisits", new Mapped<CohortDefinition>(patientsWithTwoVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMMIInsAndTwoVisits.getSearches().put("MMIInsCohortDef", new Mapped<CohortDefinition>(MMIInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND MMIInsCohortDef");
		h.replaceCohortDefinition(patientsWithMMIInsAndTwoVisits);
		
		CohortIndicator patientsWithMMIInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithMMIInsAndTwoVisitsIndicator.setName("patientsWithMMIInsAndTwoVisitsIndicator");
		patientsWithMMIInsAndTwoVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIInsAndTwoVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIInsAndTwoVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMMIInsAndTwoVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMMIInsAndTwoVisitsIndicator);
		
// 5.2.4 Patients with MEDIPLAN Insurance and 2 visits
		CompositionCohortDefinition patientsWithMEDIPLANInsAndTwoVisits=new CompositionCohortDefinition();
		patientsWithMEDIPLANInsAndTwoVisits.setName("patientsWithMEDIPLANInsAndTwoVisits");
		patientsWithMEDIPLANInsAndTwoVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMEDIPLANInsAndTwoVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMEDIPLANInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndTwoVisits.getSearches().put("patientsWithTwoVisits", new Mapped<CohortDefinition>(patientsWithTwoVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMEDIPLANInsAndTwoVisits.getSearches().put("MEDIPLANInsCohortDef", new Mapped<CohortDefinition>(MEDIPLANInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND MEDIPLANInsCohortDef");
		h.replaceCohortDefinition(patientsWithMEDIPLANInsAndTwoVisits);
		
		CohortIndicator patientsWithMEDIPLANInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithMEDIPLANInsAndTwoVisitsIndicator.setName("patientsWithMEDIPLANInsAndTwoVisitsIndicator");
		patientsWithMEDIPLANInsAndTwoVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndTwoVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndTwoVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMEDIPLANInsAndTwoVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMEDIPLANInsAndTwoVisitsIndicator);

// 5.2.5 Patients with CORAR Insurance and 2 visits
		CompositionCohortDefinition patientsWithCORARInsAndTwoVisits=new CompositionCohortDefinition();
		patientsWithCORARInsAndTwoVisits.setName("patientsWithCORARInsAndTwoVisits");
		patientsWithCORARInsAndTwoVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithCORARInsAndTwoVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithCORARInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndTwoVisits.getSearches().put("patientsWithTwoVisits", new Mapped<CohortDefinition>(patientsWithTwoVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithCORARInsAndTwoVisits.getSearches().put("CORARInsCohortDef", new Mapped<CohortDefinition>(CORARInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND CORARInsCohortDef");
		h.replaceCohortDefinition(patientsWithCORARInsAndTwoVisits);
		
		CohortIndicator patientsWithCORARInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithCORARInsAndTwoVisitsIndicator.setName("patientsWithCORARInsAndTwoVisitsIndicator");
		patientsWithCORARInsAndTwoVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARInsAndTwoVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndTwoVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithCORARInsAndTwoVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithCORARInsAndTwoVisitsIndicator);

// 5.2.6 Patients with NONE Insurance and 2 visits
		CompositionCohortDefinition patientsWithNONEInsAndTwoVisits=new CompositionCohortDefinition();
		patientsWithNONEInsAndTwoVisits.setName("patientsWithNONEInsAndTwoVisits");
		patientsWithNONEInsAndTwoVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithNONEInsAndTwoVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithNONEInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndTwoVisits.getSearches().put("patientsWithTwoVisits", new Mapped<CohortDefinition>(patientsWithTwoVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithNONEInsAndTwoVisits.getSearches().put("NONEInsCohortDef", new Mapped<CohortDefinition>(NONEInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND NONEInsCohortDef");
		h.replaceCohortDefinition(patientsWithNONEInsAndTwoVisits);
		
		CohortIndicator patientsWithNONEInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithNONEInsAndTwoVisitsIndicator.setName("patientsWithNONEInsAndTwoVisitsIndicator");
		patientsWithNONEInsAndTwoVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEInsAndTwoVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndTwoVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithNONEInsAndTwoVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithNONEInsAndTwoVisitsIndicator);
// 5.2.7 Patients without Insurance and 2 visits
		CompositionCohortDefinition patientsWithMissingInsAndTwoVisits=new CompositionCohortDefinition();
		patientsWithMissingInsAndTwoVisits.setName("patientsWithMissingInsAndTwoVisits");
		patientsWithMissingInsAndTwoVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMissingInsAndTwoVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMissingInsAndTwoVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMissingInsAndTwoVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndTwoVisits.getSearches().put("patientsWithTwoVisits", new Mapped<CohortDefinition>(patientsWithTwoVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMissingInsAndTwoVisits.getSearches().put("patientsMissingIns", new Mapped<CohortDefinition>(patientsMissingIns,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMissingInsAndTwoVisits.setCompositionString("patientsWithTwoVisits AND patientsMissingIns");
		h.replaceCohortDefinition(patientsWithMissingInsAndTwoVisits);
		
		CohortIndicator patientsWithMissingInsAndTwoVisitsIndicator = new CohortIndicator();
		patientsWithMissingInsAndTwoVisitsIndicator.setName("patientsWithMissingInsAndTwoVisitsIndicator");
		patientsWithMissingInsAndTwoVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMissingInsAndTwoVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndTwoVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMissingInsAndTwoVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMissingInsAndTwoVisitsIndicator);

// 5.3.1 Patients with Mutuelle Insurance and 3 visits
		CompositionCohortDefinition patientsWithMUTUELLEInsAndThreeVisits=new CompositionCohortDefinition();
		patientsWithMUTUELLEInsAndThreeVisits.setName("patientsWithMUTUELLEInsAndThreeVisits");
		patientsWithMUTUELLEInsAndThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMUTUELLEInsAndThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMUTUELLEInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndThreeVisits.getSearches().put("patientsWithThreeVisits", new Mapped<CohortDefinition>(patientsWithThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMUTUELLEInsAndThreeVisits.getSearches().put("MUTUELLEInsCohortDef", new Mapped<CohortDefinition>(MUTUELLEInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND MUTUELLEInsCohortDef");
		h.replaceCohortDefinition(patientsWithMUTUELLEInsAndThreeVisits);
		
		CohortIndicator patientsWithMUTUELLEInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithMUTUELLEInsAndThreeVisitsIndicator.setName("patientsWithMUTUELLEInsAndThreeVisitsIndicator");
		patientsWithMUTUELLEInsAndThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMUTUELLEInsAndThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMUTUELLEInsAndThreeVisitsIndicator);
// 5.3.2 Patients with RAMA Insurance and 3 visits
		CompositionCohortDefinition patientsWithRAMAInsAndThreeVisits=new CompositionCohortDefinition();
		patientsWithRAMAInsAndThreeVisits.setName("patientsWithRAMAInsAndThreeVisits");
		patientsWithRAMAInsAndThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithRAMAInsAndThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithRAMAInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndThreeVisits.getSearches().put("patientsWithThreeVisits", new Mapped<CohortDefinition>(patientsWithThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithRAMAInsAndThreeVisits.getSearches().put("RAMAInsCohortDef", new Mapped<CohortDefinition>(RAMAInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND RAMAInsCohortDef");
		h.replaceCohortDefinition(patientsWithRAMAInsAndThreeVisits);
		
		CohortIndicator patientsWithRAMAInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithRAMAInsAndThreeVisitsIndicator.setName("patientsWithRAMAInsAndThreeVisitsIndicator");
		patientsWithRAMAInsAndThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAInsAndThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithRAMAInsAndThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithRAMAInsAndThreeVisitsIndicator);
// 5.3.3 Patients with MMI Insurance and 3 visits
		CompositionCohortDefinition patientsWithMMIInsAndThreeVisits=new CompositionCohortDefinition();
		patientsWithMMIInsAndThreeVisits.setName("patientsWithMMIInsAndThreeVisits");
		patientsWithMMIInsAndThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMMIInsAndThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMMIInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIInsAndThreeVisits.getSearches().put("patientsWithThreeVisits", new Mapped<CohortDefinition>(patientsWithThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMMIInsAndThreeVisits.getSearches().put("MMIInsCohortDef", new Mapped<CohortDefinition>(MMIInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND MMIInsCohortDef");
		h.replaceCohortDefinition(patientsWithMMIInsAndThreeVisits);
		
		CohortIndicator patientsWithMMIInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithMMIInsAndThreeVisitsIndicator.setName("patientsWithMMIInsAndThreeVisitsIndicator");
		patientsWithMMIInsAndThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIInsAndThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIInsAndThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMMIInsAndThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMMIInsAndThreeVisitsIndicator);
		
// 5.3.4 Patients with MEDIPLAN Insurance and 3 visits
		CompositionCohortDefinition patientsWithMEDIPLANInsAndThreeVisits=new CompositionCohortDefinition();
		patientsWithMEDIPLANInsAndThreeVisits.setName("patientsWithMEDIPLANInsAndThreeVisits");
		patientsWithMEDIPLANInsAndThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMEDIPLANInsAndThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMEDIPLANInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndThreeVisits.getSearches().put("patientsWithThreeVisits", new Mapped<CohortDefinition>(patientsWithThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMEDIPLANInsAndThreeVisits.getSearches().put("MEDIPLANInsCohortDef", new Mapped<CohortDefinition>(MEDIPLANInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND MEDIPLANInsCohortDef");
		h.replaceCohortDefinition(patientsWithMEDIPLANInsAndThreeVisits);
		
		CohortIndicator patientsWithMEDIPLANInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithMEDIPLANInsAndThreeVisitsIndicator.setName("patientsWithMEDIPLANInsAndThreeVisitsIndicator");
		patientsWithMEDIPLANInsAndThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMEDIPLANInsAndThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMEDIPLANInsAndThreeVisitsIndicator);

// 5.3.5 Patients with CORAR Insurance and 3 visits
		CompositionCohortDefinition patientsWithCORARInsAndThreeVisits=new CompositionCohortDefinition();
		patientsWithCORARInsAndThreeVisits.setName("patientsWithCORARInsAndThreeVisits");
		patientsWithCORARInsAndThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithCORARInsAndThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithCORARInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndThreeVisits.getSearches().put("patientsWithThreeVisits", new Mapped<CohortDefinition>(patientsWithThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithCORARInsAndThreeVisits.getSearches().put("CORARInsCohortDef", new Mapped<CohortDefinition>(CORARInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND CORARInsCohortDef");
		h.replaceCohortDefinition(patientsWithCORARInsAndThreeVisits);
		
		CohortIndicator patientsWithCORARInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithCORARInsAndThreeVisitsIndicator.setName("patientsWithCORARInsAndThreeVisitsIndicator");
		patientsWithCORARInsAndThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARInsAndThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithCORARInsAndThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithCORARInsAndThreeVisitsIndicator);

// 5.3.6 Patients with NONE Insurance and 3 visits
		CompositionCohortDefinition patientsWithNONEInsAndThreeVisits=new CompositionCohortDefinition();
		patientsWithNONEInsAndThreeVisits.setName("patientsWithNONEInsAndThreeVisits");
		patientsWithNONEInsAndThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithNONEInsAndThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithNONEInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndThreeVisits.getSearches().put("patientsWithThreeVisits", new Mapped<CohortDefinition>(patientsWithThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithNONEInsAndThreeVisits.getSearches().put("NONEInsCohortDef", new Mapped<CohortDefinition>(NONEInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND NONEInsCohortDef");
		h.replaceCohortDefinition(patientsWithNONEInsAndThreeVisits);
		
		CohortIndicator patientsWithNONEInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithNONEInsAndThreeVisitsIndicator.setName("patientsWithNONEInsAndThreeVisitsIndicator");
		patientsWithNONEInsAndThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEInsAndThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithNONEInsAndThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithNONEInsAndThreeVisitsIndicator);
// 5.3.7 Patients without Insurance and 3 visits
		CompositionCohortDefinition patientsWithMissingInsAndThreeVisits=new CompositionCohortDefinition();
		patientsWithMissingInsAndThreeVisits.setName("patientsWithMissingInsAndThreeVisits");
		patientsWithMissingInsAndThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMissingInsAndThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMissingInsAndThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMissingInsAndThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndThreeVisits.getSearches().put("patientsWithThreeVisits", new Mapped<CohortDefinition>(patientsWithThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMissingInsAndThreeVisits.getSearches().put("patientsMissingIns", new Mapped<CohortDefinition>(patientsMissingIns,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMissingInsAndThreeVisits.setCompositionString("patientsWithThreeVisits AND patientsMissingIns");
		h.replaceCohortDefinition(patientsWithMissingInsAndThreeVisits);
		
		CohortIndicator patientsWithMissingInsAndThreeVisitsIndicator = new CohortIndicator();
		patientsWithMissingInsAndThreeVisitsIndicator.setName("patientsWithMissingInsAndThreeVisitsIndicator");
		patientsWithMissingInsAndThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMissingInsAndThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMissingInsAndThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMissingInsAndThreeVisitsIndicator);

// 5.4.1 Patients with Mutuelle Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithMUTUELLEInsAndGreaterThanThreeVisits=new CompositionCohortDefinition();
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.setName("patientsWithMUTUELLEInsAndGreaterThanThreeVisits");
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.getSearches().put("patientsWithGreaterThanThreeVisits", new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.getSearches().put("MUTUELLEInsCohortDef", new Mapped<CohortDefinition>(MUTUELLEInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisits.setCompositionString("patientsWithGreaterThanThreeVisits AND MUTUELLEInsCohortDef");
		h.replaceCohortDefinition(patientsWithMUTUELLEInsAndGreaterThanThreeVisits);
		
		CohortIndicator patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator.setName("patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator");
		patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMUTUELLEInsAndGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator);
		
		
		
// 5.4.2 Patients with RAMA Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithRAMAInsAndGreaterThanThreeVisits=new CompositionCohortDefinition();
		patientsWithRAMAInsAndGreaterThanThreeVisits.setName("patientsWithRAMAInsAndGreaterThanThreeVisits");
		patientsWithRAMAInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisits.getSearches().put("patientsWithGreaterThanThreeVisits", new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithRAMAInsAndGreaterThanThreeVisits.getSearches().put("RAMAInsCohortDef", new Mapped<CohortDefinition>(RAMAInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithRAMAInsAndGreaterThanThreeVisits.setCompositionString("patientsWithGreaterThanThreeVisits AND RAMAInsCohortDef");
		h.replaceCohortDefinition(patientsWithRAMAInsAndGreaterThanThreeVisits);
	
		CohortIndicator patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator.setName("patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator");
		patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithRAMAInsAndGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator);
				
// 5.4.3 Patients with MMI Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithMMIInsAndGreaterThanThreeVisits=new CompositionCohortDefinition();
		patientsWithMMIInsAndGreaterThanThreeVisits.setName("patientsWithMMIInsAndGreaterThanThreeVisits");
		patientsWithMMIInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisits.getSearches().put("patientsWithGreaterThanThreeVisits", new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMMIInsAndGreaterThanThreeVisits.getSearches().put("MMIInsCohortDef", new Mapped<CohortDefinition>(MMIInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMMIInsAndGreaterThanThreeVisits.setCompositionString("patientsWithGreaterThanThreeVisits AND MMIInsCohortDef");
		h.replaceCohortDefinition(patientsWithMMIInsAndGreaterThanThreeVisits);
		
		CohortIndicator patientsWithMMIInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithMMIInsAndGreaterThanThreeVisitsIndicator.setName("patientsWithMMIInsAndGreaterThanThreeVisitsIndicator");
		patientsWithMMIInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMMIInsAndGreaterThanThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMMIInsAndGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMMIInsAndGreaterThanThreeVisitsIndicator);
		
// 5.4.4 Patients with MEDIPLAN Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithMEDIPLANInsAndGreaterThanThreeVisits=new CompositionCohortDefinition();
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.setName("patientsWithMEDIPLANInsAndGreaterThanThreeVisits");
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.getSearches().put("patientsWithGreaterThanThreeVisits", new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.getSearches().put("MEDIPLANInsCohortDef", new Mapped<CohortDefinition>(MEDIPLANInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisits.setCompositionString("patientsWithGreaterThanThreeVisits AND MEDIPLANInsCohortDef");
		h.replaceCohortDefinition(patientsWithMEDIPLANInsAndGreaterThanThreeVisits);
		
		CohortIndicator patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator.setName("patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator");
		patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMEDIPLANInsAndGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator);

// 5.4.5 Patients with CORAR Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithCORARInsAndGreaterThanThreeVisits=new CompositionCohortDefinition();
		patientsWithCORARInsAndGreaterThanThreeVisits.setName("patientsWithCORARInsAndGreaterThanThreeVisits");
		patientsWithCORARInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisits.getSearches().put("patientsWithGreaterThanThreeVisits", new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithCORARInsAndGreaterThanThreeVisits.getSearches().put("CORARInsCohortDef", new Mapped<CohortDefinition>(CORARInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithCORARInsAndGreaterThanThreeVisits.setCompositionString("patientsWithGreaterThanThreeVisits AND CORARInsCohortDef");
		h.replaceCohortDefinition(patientsWithCORARInsAndGreaterThanThreeVisits);
		
		CohortIndicator patientsWithCORARInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithCORARInsAndGreaterThanThreeVisitsIndicator.setName("patientsWithCORARInsAndGreaterThanThreeVisitsIndicator");
		patientsWithCORARInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithCORARInsAndGreaterThanThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithCORARInsAndGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithCORARInsAndGreaterThanThreeVisitsIndicator);

// 5.4.6 Patients with NONE Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithNONEInsAndGreaterThanThreeVisits=new CompositionCohortDefinition();
		patientsWithNONEInsAndGreaterThanThreeVisits.setName("patientsWithNONEInsAndGreaterThanThreeVisits");
		patientsWithNONEInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisits.getSearches().put("patientsWithGreaterThanThreeVisits", new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithNONEInsAndGreaterThanThreeVisits.getSearches().put("NONEInsCohortDef", new Mapped<CohortDefinition>(NONEInsCohortDef,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithNONEInsAndGreaterThanThreeVisits.setCompositionString("patientsWithGreaterThanThreeVisits AND NONEInsCohortDef");
		h.replaceCohortDefinition(patientsWithNONEInsAndGreaterThanThreeVisits);
		
		CohortIndicator patientsWithNONEInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithNONEInsAndGreaterThanThreeVisitsIndicator.setName("patientsWithNONEInsAndGreaterThanThreeVisitsIndicator");
		patientsWithNONEInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithNONEInsAndGreaterThanThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithNONEInsAndGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithNONEInsAndGreaterThanThreeVisitsIndicator);
// 5.4.7 Patients without Insurance and greater than 3 visits
		CompositionCohortDefinition patientsWithMissingInsAndGreaterThanThreeVisits=new CompositionCohortDefinition();
		patientsWithMissingInsAndGreaterThanThreeVisits.setName("patientsWithMissingInsAndGreaterThanThreeVisits");
		patientsWithMissingInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisits.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisits.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisits.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisits.getSearches().put("patientsWithGreaterThanThreeVisits", new Mapped<CohortDefinition>(patientsWithGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithMissingInsAndGreaterThanThreeVisits.getSearches().put("patientsMissingIns", new Mapped<CohortDefinition>(patientsMissingIns,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsWithMissingInsAndGreaterThanThreeVisits.setCompositionString("patientsWithGreaterThanThreeVisits AND patientsMissingIns");
		h.replaceCohortDefinition(patientsWithMissingInsAndGreaterThanThreeVisits);
		
		CohortIndicator patientsWithMissingInsAndGreaterThanThreeVisitsIndicator = new CohortIndicator();
		patientsWithMissingInsAndGreaterThanThreeVisitsIndicator.setName("patientsWithMissingInsAndGreaterThanThreeVisitsIndicator");
		patientsWithMissingInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisitsIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithMissingInsAndGreaterThanThreeVisitsIndicator.setCohortDefinition(new Mapped<CohortDefinition>(patientsWithMissingInsAndGreaterThanThreeVisits,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		h.replaceDefinition(patientsWithMissingInsAndGreaterThanThreeVisitsIndicator);

			
//========================================================================
//  6. Age breakdown by gender
//========================================================================
		
		//Gender Cohort definitions
		GenderCohortDefinition females=new GenderCohortDefinition();
		females.setName("females");
		females.setFemaleIncluded(true);
		h.replaceCohortDefinition(females);
		
		GenderCohortDefinition males=new GenderCohortDefinition();
		males.setName("males");
		males.setMaleIncluded(true);		
		h.replaceCohortDefinition(males);
		
		//Age intervals Cohort definitions
		
		AgeCohortDefinition zeroToOne=new AgeCohortDefinition(0,1,null);
		zeroToOne.setName("zeroToOne");
		h.replaceCohortDefinition(zeroToOne);
		
		AgeCohortDefinition oneToTwo=new AgeCohortDefinition(1,2,null);
		oneToTwo.setName("oneToTwo");
		h.replaceCohortDefinition(oneToTwo);
		
		AgeCohortDefinition twoToThree=new AgeCohortDefinition(2,3,null);
		twoToThree.setName("twoToThree");
		h.replaceCohortDefinition(twoToThree);
				
		AgeCohortDefinition threeToFour=new AgeCohortDefinition(3,4,null);
		threeToFour.setName("threeToFour");
		h.replaceCohortDefinition(threeToFour);
		
		AgeCohortDefinition fourToFive=new AgeCohortDefinition(4,5,null);
		fourToFive.setName("fourToFive");
		h.replaceCohortDefinition(fourToFive);
		
		AgeCohortDefinition fiveToFifteen=new AgeCohortDefinition(5,15,null);
		fiveToFifteen.setName("fiveToFifteen");
		h.replaceCohortDefinition(fiveToFifteen);
		
		AgeCohortDefinition fifteenAndPlus=new AgeCohortDefinition(15,null,null);
		fifteenAndPlus.setName("fifteenAndPlus");
		h.replaceCohortDefinition(fifteenAndPlus);
		
// 6.1.m Male Patients with Registration and age 0-1
		
		CompositionCohortDefinition maleWithRegistrationAndAgeZeroToOne=new CompositionCohortDefinition();
		maleWithRegistrationAndAgeZeroToOne.setName("maleWithRegistrationAndAgeZeroToOne");
		maleWithRegistrationAndAgeZeroToOne.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		maleWithRegistrationAndAgeZeroToOne.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		maleWithRegistrationAndAgeZeroToOne.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeZeroToOne.getSearches().put("males",new Mapped<CohortDefinition>(males,null));
		maleWithRegistrationAndAgeZeroToOne.getSearches().put("zeroToOne",new Mapped<CohortDefinition>(zeroToOne,null));
		maleWithRegistrationAndAgeZeroToOne.setCompositionString("patientsWithPrimaryCareRegistration AND males AND zeroToOne");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeZeroToOne);
		
		CohortIndicator maleWithRegistrationAndAgeZeroToOneIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeZeroToOneIndicator.setName("maleWithRegistrationAndAgeZeroToOneIndicator");
		maleWithRegistrationAndAgeZeroToOneIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		maleWithRegistrationAndAgeZeroToOneIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeZeroToOneIndicator.setCohortDefinition(new Mapped<CohortDefinition>(maleWithRegistrationAndAgeZeroToOne,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeZeroToOneIndicator);
// 6.2.m Male Patients with Registration and age 1-2
		
		CompositionCohortDefinition maleWithRegistrationAndAgeOneToTwo=new CompositionCohortDefinition();
		maleWithRegistrationAndAgeOneToTwo.setName("maleWithRegistrationAndAgeOneToTwo");
		maleWithRegistrationAndAgeOneToTwo.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		maleWithRegistrationAndAgeOneToTwo.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		maleWithRegistrationAndAgeOneToTwo.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeOneToTwo.getSearches().put("males",new Mapped<CohortDefinition>(males,null));
		maleWithRegistrationAndAgeOneToTwo.getSearches().put("oneToTwo",new Mapped<CohortDefinition>(oneToTwo,null));
		maleWithRegistrationAndAgeOneToTwo.setCompositionString("patientsWithPrimaryCareRegistration AND males AND oneToTwo");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeOneToTwo);
		
		CohortIndicator maleWithRegistrationAndAgeOneToTwoIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeOneToTwoIndicator.setName("maleWithRegistrationAndAgeOneToTwoIndicator");
		maleWithRegistrationAndAgeOneToTwoIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		maleWithRegistrationAndAgeOneToTwoIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeOneToTwoIndicator.setCohortDefinition(new Mapped<CohortDefinition>(maleWithRegistrationAndAgeOneToTwo,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeOneToTwoIndicator);
// 6.3.m Male Patients with Registration and age 2-3
		
		CompositionCohortDefinition maleWithRegistrationAndAgeTwoToThree=new CompositionCohortDefinition();
		maleWithRegistrationAndAgeTwoToThree.setName("maleWithRegistrationAndAgeTwoToThree");
		maleWithRegistrationAndAgeTwoToThree.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		maleWithRegistrationAndAgeTwoToThree.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		maleWithRegistrationAndAgeTwoToThree.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeTwoToThree.getSearches().put("males",new Mapped<CohortDefinition>(males,null));
		maleWithRegistrationAndAgeTwoToThree.getSearches().put("twoToThree",new Mapped<CohortDefinition>(twoToThree,null));
		maleWithRegistrationAndAgeTwoToThree.setCompositionString("patientsWithPrimaryCareRegistration AND males AND twoToThree");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeTwoToThree);
		
		CohortIndicator maleWithRegistrationAndAgeTwoToThreeIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeTwoToThreeIndicator.setName("maleWithRegistrationAndAgeTwoToThreeIndicator");
		maleWithRegistrationAndAgeTwoToThreeIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		maleWithRegistrationAndAgeTwoToThreeIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeTwoToThreeIndicator.setCohortDefinition(new Mapped<CohortDefinition>(maleWithRegistrationAndAgeTwoToThree,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeTwoToThreeIndicator);
// 6.4.m Male Patients with Registration and age 3-4
		
		CompositionCohortDefinition maleWithRegistrationAndAgeThreeToFour=new CompositionCohortDefinition();
		maleWithRegistrationAndAgeThreeToFour.setName("maleWithRegistrationAndAgeThreeToFour");
		maleWithRegistrationAndAgeThreeToFour.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		maleWithRegistrationAndAgeThreeToFour.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		maleWithRegistrationAndAgeThreeToFour.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeThreeToFour.getSearches().put("males",new Mapped<CohortDefinition>(males,null));
		maleWithRegistrationAndAgeThreeToFour.getSearches().put("threeToFour",new Mapped<CohortDefinition>(threeToFour,null));
		maleWithRegistrationAndAgeThreeToFour.setCompositionString("patientsWithPrimaryCareRegistration AND males AND threeToFour");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeThreeToFour);
		
		CohortIndicator maleWithRegistrationAndAgeThreeToFourIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeThreeToFourIndicator.setName("maleWithRegistrationAndAgeThreeToFourIndicator");
		maleWithRegistrationAndAgeThreeToFourIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		maleWithRegistrationAndAgeThreeToFourIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeThreeToFourIndicator.setCohortDefinition(new Mapped<CohortDefinition>(maleWithRegistrationAndAgeThreeToFour,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeThreeToFourIndicator);
// 6.5.m Male Patients with Registration and age 4-5
		
		CompositionCohortDefinition maleWithRegistrationAndAgeFourToFive=new CompositionCohortDefinition();
		maleWithRegistrationAndAgeFourToFive.setName("maleWithRegistrationAndAgeFourToFive");
		maleWithRegistrationAndAgeFourToFive.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		maleWithRegistrationAndAgeFourToFive.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		maleWithRegistrationAndAgeFourToFive.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeFourToFive.getSearches().put("males",new Mapped<CohortDefinition>(males,null));
		maleWithRegistrationAndAgeFourToFive.getSearches().put("fourToFive",new Mapped<CohortDefinition>(fourToFive,null));
		maleWithRegistrationAndAgeFourToFive.setCompositionString("patientsWithPrimaryCareRegistration AND males AND fourToFive");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeFourToFive);
		
		CohortIndicator maleWithRegistrationAndAgeFourToFiveIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeFourToFiveIndicator.setName("maleWithRegistrationAndAgeFourToFiveIndicator");
		maleWithRegistrationAndAgeFourToFiveIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		maleWithRegistrationAndAgeFourToFiveIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeFourToFiveIndicator.setCohortDefinition(new Mapped<CohortDefinition>(maleWithRegistrationAndAgeFourToFive,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeFourToFiveIndicator);
// 6.6.m Male Patients with Registration and age 5-15
		
		CompositionCohortDefinition maleWithRegistrationAndAgeFiveToFifteen=new CompositionCohortDefinition();
		maleWithRegistrationAndAgeFiveToFifteen.setName("maleWithRegistrationAndAgeFiveToFifteen");
		maleWithRegistrationAndAgeFiveToFifteen.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		maleWithRegistrationAndAgeFiveToFifteen.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		maleWithRegistrationAndAgeFiveToFifteen.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeFiveToFifteen.getSearches().put("males",new Mapped<CohortDefinition>(males,null));
		maleWithRegistrationAndAgeFiveToFifteen.getSearches().put("fiveToFifteen",new Mapped<CohortDefinition>(fiveToFifteen,null));
		maleWithRegistrationAndAgeFiveToFifteen.setCompositionString("patientsWithPrimaryCareRegistration AND males AND fiveToFifteen");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeFiveToFifteen);
		
		CohortIndicator maleWithRegistrationAndAgeFiveToFifteenIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeFiveToFifteenIndicator.setName("maleWithRegistrationAndAgeFiveToFifteenIndicator");
		maleWithRegistrationAndAgeFiveToFifteenIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		maleWithRegistrationAndAgeFiveToFifteenIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeFiveToFifteenIndicator.setCohortDefinition(new Mapped<CohortDefinition>(maleWithRegistrationAndAgeFiveToFifteen,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeFiveToFifteenIndicator);
// 6.7.m Male Patients with Registration and age 15+
		
		CompositionCohortDefinition maleWithRegistrationAndAgeFifteenAndPlus=new CompositionCohortDefinition();
		maleWithRegistrationAndAgeFifteenAndPlus.setName("maleWithRegistrationAndAgeFifteenAndPlus");
		maleWithRegistrationAndAgeFifteenAndPlus.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		maleWithRegistrationAndAgeFifteenAndPlus.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		maleWithRegistrationAndAgeFifteenAndPlus.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		maleWithRegistrationAndAgeFifteenAndPlus.getSearches().put("males",new Mapped<CohortDefinition>(males,null));
		maleWithRegistrationAndAgeFifteenAndPlus.getSearches().put("fifteenAndPlus",new Mapped<CohortDefinition>(fifteenAndPlus,null));
		maleWithRegistrationAndAgeFifteenAndPlus.setCompositionString("patientsWithPrimaryCareRegistration AND males AND fifteenAndPlus");
		h.replaceCohortDefinition(maleWithRegistrationAndAgeFifteenAndPlus);
		
		CohortIndicator maleWithRegistrationAndAgeFifteenAndPlusIndicator = new CohortIndicator();
		maleWithRegistrationAndAgeFifteenAndPlusIndicator.setName("maleWithRegistrationAndAgeFifteenAndPlusIndicator");
		maleWithRegistrationAndAgeFifteenAndPlusIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		maleWithRegistrationAndAgeFifteenAndPlusIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		maleWithRegistrationAndAgeFifteenAndPlusIndicator.setCohortDefinition(new Mapped<CohortDefinition>(maleWithRegistrationAndAgeFifteenAndPlus,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(maleWithRegistrationAndAgeFifteenAndPlusIndicator);
// 6.1.f Female Patients with Registration and age 0-1
		
		CompositionCohortDefinition femaleWithRegistrationAndAgeZeroToOne=new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeZeroToOne.setName("femaleWithRegistrationAndAgeZeroToOne");
		femaleWithRegistrationAndAgeZeroToOne.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femaleWithRegistrationAndAgeZeroToOne.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femaleWithRegistrationAndAgeZeroToOne.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeZeroToOne.getSearches().put("females",new Mapped<CohortDefinition>(females,null));
		femaleWithRegistrationAndAgeZeroToOne.getSearches().put("zeroToOne",new Mapped<CohortDefinition>(zeroToOne,null));
		femaleWithRegistrationAndAgeZeroToOne.setCompositionString("patientsWithPrimaryCareRegistration AND females AND zeroToOne");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeZeroToOne);
		
		CohortIndicator femaleWithRegistrationAndAgeZeroToOneIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeZeroToOneIndicator.setName("femaleWithRegistrationAndAgeZeroToOneIndicator");
		femaleWithRegistrationAndAgeZeroToOneIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femaleWithRegistrationAndAgeZeroToOneIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeZeroToOneIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femaleWithRegistrationAndAgeZeroToOne,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeZeroToOneIndicator);
// 6.2.f Female Patients with Registration and age 1-2
		
		CompositionCohortDefinition femaleWithRegistrationAndAgeOneToTwo=new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeOneToTwo.setName("femaleWithRegistrationAndAgeOneToTwo");
		femaleWithRegistrationAndAgeOneToTwo.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femaleWithRegistrationAndAgeOneToTwo.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femaleWithRegistrationAndAgeOneToTwo.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeOneToTwo.getSearches().put("females",new Mapped<CohortDefinition>(females,null));
		femaleWithRegistrationAndAgeOneToTwo.getSearches().put("oneToTwo",new Mapped<CohortDefinition>(oneToTwo,null));
		femaleWithRegistrationAndAgeOneToTwo.setCompositionString("patientsWithPrimaryCareRegistration AND females AND oneToTwo");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeOneToTwo);
		
		CohortIndicator femaleWithRegistrationAndAgeOneToTwoIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeOneToTwoIndicator.setName("femaleWithRegistrationAndAgeOneToTwoIndicator");
		femaleWithRegistrationAndAgeOneToTwoIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femaleWithRegistrationAndAgeOneToTwoIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeOneToTwoIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femaleWithRegistrationAndAgeOneToTwo,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeOneToTwoIndicator);
// 6.3.f Female Patients with Registration and age 2-3
		
		CompositionCohortDefinition femaleWithRegistrationAndAgeTwoToThree=new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeTwoToThree.setName("femaleWithRegistrationAndAgeTwoToThree");
		femaleWithRegistrationAndAgeTwoToThree.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femaleWithRegistrationAndAgeTwoToThree.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femaleWithRegistrationAndAgeTwoToThree.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeTwoToThree.getSearches().put("females",new Mapped<CohortDefinition>(females,null));
		femaleWithRegistrationAndAgeTwoToThree.getSearches().put("twoToThree",new Mapped<CohortDefinition>(twoToThree,null));
		femaleWithRegistrationAndAgeTwoToThree.setCompositionString("patientsWithPrimaryCareRegistration AND females AND twoToThree");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeTwoToThree);
		
		CohortIndicator femaleWithRegistrationAndAgeTwoToThreeIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeTwoToThreeIndicator.setName("femaleWithRegistrationAndAgeTwoToThreeIndicator");
		femaleWithRegistrationAndAgeTwoToThreeIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femaleWithRegistrationAndAgeTwoToThreeIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeTwoToThreeIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femaleWithRegistrationAndAgeTwoToThree,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeTwoToThreeIndicator);
// 6.4.f Female Patients with Registration and age 3-4
		
		CompositionCohortDefinition femaleWithRegistrationAndAgeThreeToFour=new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeThreeToFour.setName("femaleWithRegistrationAndAgeThreeToFour");
		femaleWithRegistrationAndAgeThreeToFour.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femaleWithRegistrationAndAgeThreeToFour.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femaleWithRegistrationAndAgeThreeToFour.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeThreeToFour.getSearches().put("females",new Mapped<CohortDefinition>(females,null));
		femaleWithRegistrationAndAgeThreeToFour.getSearches().put("threeToFour",new Mapped<CohortDefinition>(threeToFour,null));
		femaleWithRegistrationAndAgeThreeToFour.setCompositionString("patientsWithPrimaryCareRegistration AND females AND threeToFour");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeThreeToFour);
		
		CohortIndicator femaleWithRegistrationAndAgeThreeToFourIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeThreeToFourIndicator.setName("femaleWithRegistrationAndAgeThreeToFourIndicator");
		femaleWithRegistrationAndAgeThreeToFourIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femaleWithRegistrationAndAgeThreeToFourIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeThreeToFourIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femaleWithRegistrationAndAgeThreeToFour,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeThreeToFourIndicator);
// 6.5.f Female Patients with Registration and age 4-5
		
		CompositionCohortDefinition femaleWithRegistrationAndAgeFourToFive=new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeFourToFive.setName("femaleWithRegistrationAndAgeFourToFive");
		femaleWithRegistrationAndAgeFourToFive.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femaleWithRegistrationAndAgeFourToFive.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femaleWithRegistrationAndAgeFourToFive.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeFourToFive.getSearches().put("females",new Mapped<CohortDefinition>(females,null));
		femaleWithRegistrationAndAgeFourToFive.getSearches().put("fourToFive",new Mapped<CohortDefinition>(fourToFive,null));
		femaleWithRegistrationAndAgeFourToFive.setCompositionString("patientsWithPrimaryCareRegistration AND females AND fourToFive");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeFourToFive);
		
		CohortIndicator femaleWithRegistrationAndAgeFourToFiveIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeFourToFiveIndicator.setName("femaleWithRegistrationAndAgeFourToFiveIndicator");
		femaleWithRegistrationAndAgeFourToFiveIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femaleWithRegistrationAndAgeFourToFiveIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeFourToFiveIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femaleWithRegistrationAndAgeFourToFive,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeFourToFiveIndicator);
// 6.6.f Female Patients with Registration and age 5-15
		
		CompositionCohortDefinition femaleWithRegistrationAndAgeFiveToFifteen=new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeFiveToFifteen.setName("femaleWithRegistrationAndAgeFiveToFifteen");
		femaleWithRegistrationAndAgeFiveToFifteen.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femaleWithRegistrationAndAgeFiveToFifteen.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femaleWithRegistrationAndAgeFiveToFifteen.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeFiveToFifteen.getSearches().put("females",new Mapped<CohortDefinition>(females,null));
		femaleWithRegistrationAndAgeFiveToFifteen.getSearches().put("fiveToFifteen",new Mapped<CohortDefinition>(fiveToFifteen,null));
		femaleWithRegistrationAndAgeFiveToFifteen.setCompositionString("patientsWithPrimaryCareRegistration AND females AND fiveToFifteen");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeFiveToFifteen);
		
		CohortIndicator femaleWithRegistrationAndAgeFiveToFifteenIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeFiveToFifteenIndicator.setName("femaleWithRegistrationAndAgeFiveToFifteenIndicator");
		femaleWithRegistrationAndAgeFiveToFifteenIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femaleWithRegistrationAndAgeFiveToFifteenIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeFiveToFifteenIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femaleWithRegistrationAndAgeFiveToFifteen,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeFiveToFifteenIndicator);
// 6.7.f Female Patients with Registration and age 15+
		
		CompositionCohortDefinition femaleWithRegistrationAndAgeFifteenAndPlus=new CompositionCohortDefinition();
		femaleWithRegistrationAndAgeFifteenAndPlus.setName("femaleWithRegistrationAndAgeFifteenAndPlus");
		femaleWithRegistrationAndAgeFifteenAndPlus.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femaleWithRegistrationAndAgeFifteenAndPlus.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femaleWithRegistrationAndAgeFifteenAndPlus.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femaleWithRegistrationAndAgeFifteenAndPlus.getSearches().put("females",new Mapped<CohortDefinition>(females,null));
		femaleWithRegistrationAndAgeFifteenAndPlus.getSearches().put("fifteenAndPlus",new Mapped<CohortDefinition>(fifteenAndPlus,null));
		femaleWithRegistrationAndAgeFifteenAndPlus.setCompositionString("patientsWithPrimaryCareRegistration AND females AND fifteenAndPlus");
		h.replaceCohortDefinition(femaleWithRegistrationAndAgeFifteenAndPlus);
		
		CohortIndicator femaleWithRegistrationAndAgeFifteenAndPlusIndicator = new CohortIndicator();
		femaleWithRegistrationAndAgeFifteenAndPlusIndicator.setName("femaleWithRegistrationAndAgeFifteenAndPlusIndicator");
		femaleWithRegistrationAndAgeFifteenAndPlusIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femaleWithRegistrationAndAgeFifteenAndPlusIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femaleWithRegistrationAndAgeFifteenAndPlusIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femaleWithRegistrationAndAgeFifteenAndPlus,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femaleWithRegistrationAndAgeFifteenAndPlusIndicator);
				
	
		
		
		
		//========================================================================
		// 7. Primary care service requested
		//========================================================================
		
//7.1.f Female Total number of patient requested primary care

		SqlCohortDefinition femalePatientsrequestPrimCare=new SqlCohortDefinition("SELECT o.person_id FROM obs o,person p where o.concept_id="+PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED_ID+" and p.gender='F' and o.person_id=p.person_id and o.voided=0");
		femalePatientsrequestPrimCare.setName("femalePatientsrequestPrimCare");
		h.replaceCohortDefinition(femalePatientsrequestPrimCare);
		
		CompositionCohortDefinition femalePatientsrequestPrimCareInRegistration=new CompositionCohortDefinition();
		femalePatientsrequestPrimCareInRegistration.setName("femalePatientsrequestPrimCareInRegistration");
		femalePatientsrequestPrimCareInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestPrimCareInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestPrimCareInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestPrimCareInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestPrimCareInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestPrimCareInRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestPrimCareInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare");
		h.replaceCohortDefinition(femalePatientsrequestPrimCareInRegistration);

		CohortIndicator femalePatientsrequestPrimCareInRegistrationIndicator = new CohortIndicator();
		femalePatientsrequestPrimCareInRegistrationIndicator.setName("femalePatientsrequestPrimCareInRegistrationIndicator");
		femalePatientsrequestPrimCareInRegistrationIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestPrimCareInRegistrationIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestPrimCareInRegistrationIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestPrimCareInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestPrimCareInRegistrationIndicator);
		//7.1.m Female Total number of patient requested primary care

		SqlCohortDefinition malePatientsrequestPrimCare=new SqlCohortDefinition("SELECT o.person_id FROM obs o,person p where o.concept_id="+PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED_ID+" and p.gender='M' and o.person_id=p.person_id and o.voided=0");
		malePatientsrequestPrimCare.setName("malePatientsrequestPrimCare");
		h.replaceCohortDefinition(malePatientsrequestPrimCare);
		
		CompositionCohortDefinition malePatientsrequestPrimCareInRegistration=new CompositionCohortDefinition();
		malePatientsrequestPrimCareInRegistration.setName("malePatientsrequestPrimCareInRegistration");
		malePatientsrequestPrimCareInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestPrimCareInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestPrimCareInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestPrimCareInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestPrimCareInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestPrimCareInRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestPrimCareInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare");
		h.replaceCohortDefinition(malePatientsrequestPrimCareInRegistration);

		CohortIndicator malePatientsrequestPrimCareInRegistrationIndicator = new CohortIndicator();
		malePatientsrequestPrimCareInRegistrationIndicator.setName("malePatientsrequestPrimCareInRegistrationIndicator");
		malePatientsrequestPrimCareInRegistrationIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestPrimCareInRegistrationIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestPrimCareInRegistrationIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestPrimCareInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestPrimCareInRegistrationIndicator);
//7.2.f Female Number of patients requested VCT PROGRAM
		
		CodedObsCohortDefinition patientRequestVCTProgram=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.VCT_PROGRAM, SetComparator.IN, TimeModifier.ANY);
		patientRequestVCTProgram.setName("patientRequestVCTProgram");
		patientRequestVCTProgram.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestVCTProgram.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestVCTProgram);
		
		CompositionCohortDefinition femalePatientsrequestVCTProgramInRegistration=new CompositionCohortDefinition();
		femalePatientsrequestVCTProgramInRegistration.setName("femalePatientsrequestVCTProgramInRegistration");
		femalePatientsrequestVCTProgramInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestVCTProgramInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestVCTProgramInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestVCTProgramInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestVCTProgramInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestVCTProgramInRegistration.getSearches().put("patientRequestVCTProgram", new Mapped<CohortDefinition>(patientRequestVCTProgram,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestVCTProgramInRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestVCTProgramInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestVCTProgram");
		h.replaceCohortDefinition(femalePatientsrequestVCTProgramInRegistration);
		
		CohortIndicator femalePatientsrequestVCTProgramInRegistrationIndicator = new CohortIndicator();
		femalePatientsrequestVCTProgramInRegistrationIndicator.setName("femalePatientsrequestVCTProgramInRegistrationIndicator");
		femalePatientsrequestVCTProgramInRegistrationIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestVCTProgramInRegistrationIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestVCTProgramInRegistrationIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestVCTProgramInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestVCTProgramInRegistrationIndicator);
		
		
		//7.2.m Male Number of patients requested VCT PROGRAM
		CompositionCohortDefinition malePatientsrequestVCTProgramInRegistration=new CompositionCohortDefinition();
		malePatientsrequestVCTProgramInRegistration.setName("malePatientsrequestVCTProgramInRegistration");
		malePatientsrequestVCTProgramInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestVCTProgramInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestVCTProgramInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestVCTProgramInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestVCTProgramInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestVCTProgramInRegistration.getSearches().put("patientRequestVCTProgram", new Mapped<CohortDefinition>(patientRequestVCTProgram,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestVCTProgramInRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestVCTProgramInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestVCTProgram");
		h.replaceCohortDefinition(malePatientsrequestVCTProgramInRegistration);
		
		CohortIndicator malePatientsrequestVCTProgramInRegistrationIndicator = new CohortIndicator();
		malePatientsrequestVCTProgramInRegistrationIndicator.setName("malePatientsrequestVCTProgramInRegistrationIndicator");
		malePatientsrequestVCTProgramInRegistrationIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestVCTProgramInRegistrationIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestVCTProgramInRegistrationIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestVCTProgramInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestVCTProgramInRegistrationIndicator);
		
		// 7.3.f Female Number of patients requested ANTENATAL CLINIC
		
		CodedObsCohortDefinition patientRequestAntenatalClinic=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.ANTENATAL_CLINIC, SetComparator.IN, TimeModifier.ANY);
		patientRequestAntenatalClinic.setName("patientRequestAntenatalClinic");
		patientRequestAntenatalClinic.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestAntenatalClinic.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestAntenatalClinic);
		
		CompositionCohortDefinition femalePatientsrequestAntenatalClinicInRegistration=new CompositionCohortDefinition();
		femalePatientsrequestAntenatalClinicInRegistration.setName("femalePatientsrequestAntenatalClinicInRegistration");
		femalePatientsrequestAntenatalClinicInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestAntenatalClinicInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestAntenatalClinicInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestAntenatalClinicInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestAntenatalClinicInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestAntenatalClinicInRegistration.getSearches().put("patientRequestAntenatalClinic", new Mapped<CohortDefinition>(patientRequestAntenatalClinic,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestAntenatalClinicInRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestAntenatalClinicInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestAntenatalClinic");
		h.replaceCohortDefinition(femalePatientsrequestAntenatalClinicInRegistration);
		
		CohortIndicator femalePatientsrequestAntenatalClinicInRegistrationIndicator = new CohortIndicator();
		femalePatientsrequestAntenatalClinicInRegistrationIndicator.setName("femalePatientsrequestAntenatalClinicInRegistrationIndicator");
		femalePatientsrequestAntenatalClinicInRegistrationIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestAntenatalClinicInRegistrationIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestAntenatalClinicInRegistrationIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestAntenatalClinicInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestAntenatalClinicInRegistrationIndicator);
		
		// 7.3.m Male Number of patients requested ANTENATAL CLINIC
		
		CompositionCohortDefinition malePatientsrequestAntenatalClinicInRegistration=new CompositionCohortDefinition();
		malePatientsrequestAntenatalClinicInRegistration.setName("malePatientsrequestAntenatalClinicInRegistration");
		malePatientsrequestAntenatalClinicInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestAntenatalClinicInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestAntenatalClinicInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestAntenatalClinicInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestAntenatalClinicInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestAntenatalClinicInRegistration.getSearches().put("patientRequestAntenatalClinic", new Mapped<CohortDefinition>(patientRequestAntenatalClinic,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestAntenatalClinicInRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestAntenatalClinicInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestAntenatalClinic");
		h.replaceCohortDefinition(malePatientsrequestAntenatalClinicInRegistration);
		
		CohortIndicator malePatientsrequestAntenatalClinicInRegistrationIndicator = new CohortIndicator();
		malePatientsrequestAntenatalClinicInRegistrationIndicator.setName("malePatientsrequestAntenatalClinicInRegistrationIndicator");
		malePatientsrequestAntenatalClinicInRegistrationIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestAntenatalClinicInRegistrationIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestAntenatalClinicInRegistrationIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestAntenatalClinicInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestAntenatalClinicInRegistrationIndicator);
		
// 7.4.f Female Number of patients requested FAMILY PLANNING SERVICES
		
		CodedObsCohortDefinition patientRequestFamilyPlaningServices=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.FAMILY_PLANNING_SERVICES, SetComparator.IN, TimeModifier.ANY);
		patientRequestFamilyPlaningServices.setName("patientRequestFamilyPlaningServices");
		patientRequestFamilyPlaningServices.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestFamilyPlaningServices.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestFamilyPlaningServices);
		
		CompositionCohortDefinition femalePatientsrequestFamilyPlaningServicesRegistration=new CompositionCohortDefinition();
		femalePatientsrequestFamilyPlaningServicesRegistration.setName("femalePatientsrequestFamilyPlaningServicesRegistration");
		femalePatientsrequestFamilyPlaningServicesRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestFamilyPlaningServicesRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestFamilyPlaningServicesRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestFamilyPlaningServicesRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestFamilyPlaningServicesRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestFamilyPlaningServicesRegistration.getSearches().put("patientRequestFamilyPlaningServices", new Mapped<CohortDefinition>(patientRequestFamilyPlaningServices,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestFamilyPlaningServicesRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestFamilyPlaningServicesRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestFamilyPlaningServices");
		h.replaceCohortDefinition(femalePatientsrequestFamilyPlaningServicesRegistration);
		
		CohortIndicator femalePatientsrequestFamilyPlaningServicesRegistrationIndicator = new CohortIndicator();
		femalePatientsrequestFamilyPlaningServicesRegistrationIndicator.setName("femalePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		femalePatientsrequestFamilyPlaningServicesRegistrationIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestFamilyPlaningServicesRegistrationIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestFamilyPlaningServicesRegistrationIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestFamilyPlaningServicesRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestFamilyPlaningServicesRegistrationIndicator);
// 7.4.m Male Number of patients requested FAMILY PLANNING SERVICES
		
		CompositionCohortDefinition malePatientsrequestFamilyPlaningServicesRegistration=new CompositionCohortDefinition();
		malePatientsrequestFamilyPlaningServicesRegistration.setName("malePatientsrequestFamilyPlaningServicesRegistration");
		malePatientsrequestFamilyPlaningServicesRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestFamilyPlaningServicesRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestFamilyPlaningServicesRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestFamilyPlaningServicesRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestFamilyPlaningServicesRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestFamilyPlaningServicesRegistration.getSearches().put("patientRequestFamilyPlaningServices", new Mapped<CohortDefinition>(patientRequestFamilyPlaningServices,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestFamilyPlaningServicesRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestFamilyPlaningServicesRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestFamilyPlaningServices");
		h.replaceCohortDefinition(malePatientsrequestFamilyPlaningServicesRegistration);
		
		CohortIndicator malePatientsrequestFamilyPlaningServicesRegistrationIndicator = new CohortIndicator();
		malePatientsrequestFamilyPlaningServicesRegistrationIndicator.setName("malePatientsrequestFamilyPlaningServicesRegistrationIndicator");
		malePatientsrequestFamilyPlaningServicesRegistrationIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestFamilyPlaningServicesRegistrationIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestFamilyPlaningServicesRegistrationIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestFamilyPlaningServicesRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestFamilyPlaningServicesRegistrationIndicator);
// 7.5.f Female Number of patients requested MUTUELLE SERVICE
		
		CodedObsCohortDefinition patientRequestMutuelleService=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.MUTUELLE_SERVICE, SetComparator.IN, TimeModifier.ANY);
		patientRequestMutuelleService.setName("patientRequestMutuelleService");
		patientRequestMutuelleService.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestMutuelleService.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestMutuelleService);
		
		CompositionCohortDefinition femalePatientsrequestMutuelleServiceRegistration=new CompositionCohortDefinition();
		femalePatientsrequestMutuelleServiceRegistration.setName("femalePatientsrequestMutuelleServiceRegistration");
		femalePatientsrequestMutuelleServiceRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestMutuelleServiceRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestMutuelleServiceRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestMutuelleServiceRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestMutuelleServiceRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestMutuelleServiceRegistration.getSearches().put("patientRequestMutuelleService", new Mapped<CohortDefinition>(patientRequestMutuelleService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestMutuelleServiceRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestMutuelleServiceRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestMutuelleService");
		h.replaceCohortDefinition(femalePatientsrequestMutuelleServiceRegistration);
		
		CohortIndicator femalePatientsrequestMutuelleServiceRegistrationIndicator = new CohortIndicator();
		femalePatientsrequestMutuelleServiceRegistrationIndicator.setName("femalePatientsrequestMutuelleServiceRegistrationIndicator");
		femalePatientsrequestMutuelleServiceRegistrationIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestMutuelleServiceRegistrationIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestMutuelleServiceRegistrationIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestMutuelleServiceRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestMutuelleServiceRegistrationIndicator);
// 7.5.m Male Number of patients requested MUTUELLE SERVICE
		
		CompositionCohortDefinition malePatientsrequestMutuelleServiceRegistration=new CompositionCohortDefinition();
		malePatientsrequestMutuelleServiceRegistration.setName("malePatientsrequestMutuelleServiceRegistration");
		malePatientsrequestMutuelleServiceRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestMutuelleServiceRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestMutuelleServiceRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestMutuelleServiceRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestMutuelleServiceRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestMutuelleServiceRegistration.getSearches().put("patientRequestMutuelleService", new Mapped<CohortDefinition>(patientRequestMutuelleService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestMutuelleServiceRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestMutuelleServiceRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestMutuelleService");
		h.replaceCohortDefinition(malePatientsrequestMutuelleServiceRegistration);
		
		CohortIndicator malePatientsrequestMutuelleServiceRegistrationIndicator = new CohortIndicator();
		malePatientsrequestMutuelleServiceRegistrationIndicator.setName("malePatientsrequestMutuelleServiceRegistrationIndicator");
		malePatientsrequestMutuelleServiceRegistrationIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestMutuelleServiceRegistrationIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestMutuelleServiceRegistrationIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestMutuelleServiceRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestMutuelleServiceRegistrationIndicator);
// 7.6.f Female Number of patients requested ACCOUNTING OFFICE SERVICE
		
		CodedObsCohortDefinition patientRequestAccountingOfficeService=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.ACCOUNTING_OFFICE_SERVICE, SetComparator.IN, TimeModifier.ANY);
		patientRequestAccountingOfficeService.setName("patientRequestAccountingOfficeService");
		patientRequestAccountingOfficeService.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestAccountingOfficeService.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestAccountingOfficeService);
		
		CompositionCohortDefinition femalePatientsrequestAccountingOfficeServiceRegistration=new CompositionCohortDefinition();
		femalePatientsrequestAccountingOfficeServiceRegistration.setName("femalePatientsrequestAccountingOfficeServiceRegistration");
		femalePatientsrequestAccountingOfficeServiceRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestAccountingOfficeServiceRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestAccountingOfficeServiceRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestAccountingOfficeServiceRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestAccountingOfficeServiceRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestAccountingOfficeServiceRegistration.getSearches().put("patientRequestAccountingOfficeService", new Mapped<CohortDefinition>(patientRequestAccountingOfficeService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestAccountingOfficeServiceRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestAccountingOfficeServiceRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestAccountingOfficeService");
		h.replaceCohortDefinition(femalePatientsrequestAccountingOfficeServiceRegistration);
		
		CohortIndicator femalePatientsrequestAccountingOfficeServiceRegistrationIndicator = new CohortIndicator();
		femalePatientsrequestAccountingOfficeServiceRegistrationIndicator.setName("femalePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		femalePatientsrequestAccountingOfficeServiceRegistrationIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestAccountingOfficeServiceRegistrationIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestAccountingOfficeServiceRegistrationIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestAccountingOfficeServiceRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestAccountingOfficeServiceRegistrationIndicator);
// 7.6.m Male Number of patients requested ACCOUNTING OFFICE SERVICE
		CompositionCohortDefinition malePatientsrequestAccountingOfficeServiceRegistration=new CompositionCohortDefinition();
		malePatientsrequestAccountingOfficeServiceRegistration.setName("malePatientsrequestAccountingOfficeServiceRegistration");
		malePatientsrequestAccountingOfficeServiceRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestAccountingOfficeServiceRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestAccountingOfficeServiceRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestAccountingOfficeServiceRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestAccountingOfficeServiceRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestAccountingOfficeServiceRegistration.getSearches().put("patientRequestAccountingOfficeService", new Mapped<CohortDefinition>(patientRequestAccountingOfficeService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestAccountingOfficeServiceRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestAccountingOfficeServiceRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestAccountingOfficeService");
		h.replaceCohortDefinition(malePatientsrequestAccountingOfficeServiceRegistration);
		
		CohortIndicator malePatientsrequestAccountingOfficeServiceRegistrationIndicator = new CohortIndicator();
		malePatientsrequestAccountingOfficeServiceRegistrationIndicator.setName("malePatientsrequestAccountingOfficeServiceRegistrationIndicator");
		malePatientsrequestAccountingOfficeServiceRegistrationIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestAccountingOfficeServiceRegistrationIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestAccountingOfficeServiceRegistrationIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestAccountingOfficeServiceRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestAccountingOfficeServiceRegistrationIndicator);

// 7.7.f Female Number of patients requested INTEGRATED MANAGEMENT OF ADULT ILLNESS SERVICE
		
		CodedObsCohortDefinition patientRequestAdultIllnessService=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.INTEGRATED_MANAGEMENT_OF_ADULT_ILLNESS_SERVICE, SetComparator.IN, TimeModifier.ANY);
		patientRequestAdultIllnessService.setName("patientRequestAdultIllnessService");
		patientRequestAdultIllnessService.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestAdultIllnessService.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestAdultIllnessService);
		
		CompositionCohortDefinition femalePatientsrequestAdultIllnessServiceRegistration=new CompositionCohortDefinition();
		femalePatientsrequestAdultIllnessServiceRegistration.setName("femalePatientsrequestAdultIllnessServiceRegistration");
		femalePatientsrequestAdultIllnessServiceRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestAdultIllnessServiceRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestAdultIllnessServiceRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestAdultIllnessServiceRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestAdultIllnessServiceRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestAdultIllnessServiceRegistration.getSearches().put("patientRequestAdultIllnessService", new Mapped<CohortDefinition>(patientRequestAdultIllnessService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestAdultIllnessServiceRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestAdultIllnessServiceRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestAdultIllnessService");
		h.replaceCohortDefinition(femalePatientsrequestAdultIllnessServiceRegistration);
		
		CohortIndicator femalePatientsrequestAdultIllnessServiceIndicator = new CohortIndicator();
		femalePatientsrequestAdultIllnessServiceIndicator.setName("femalePatientsrequestAdultIllnessServiceIndicator");
		femalePatientsrequestAdultIllnessServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestAdultIllnessServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestAdultIllnessServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestAdultIllnessServiceRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestAdultIllnessServiceIndicator);
		
// 7.7.m Male Number of patients requested INTEGRATED MANAGEMENT OF ADULT ILLNESS SERVICE
		
		CompositionCohortDefinition malePatientsrequestAdultIllnessServiceRegistration=new CompositionCohortDefinition();
		malePatientsrequestAdultIllnessServiceRegistration.setName("malePatientsrequestAdultIllnessServiceRegistration");
		malePatientsrequestAdultIllnessServiceRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestAdultIllnessServiceRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestAdultIllnessServiceRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestAdultIllnessServiceRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestAdultIllnessServiceRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestAdultIllnessServiceRegistration.getSearches().put("patientRequestAdultIllnessService", new Mapped<CohortDefinition>(patientRequestAdultIllnessService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestAdultIllnessServiceRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestAdultIllnessServiceRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestAdultIllnessService");
		h.replaceCohortDefinition(malePatientsrequestAdultIllnessServiceRegistration);
		
		CohortIndicator malePatientsrequestAdultIllnessServiceIndicator = new CohortIndicator();
		malePatientsrequestAdultIllnessServiceIndicator.setName("malePatientsrequestAdultIllnessServiceIndicator");
		malePatientsrequestAdultIllnessServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestAdultIllnessServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestAdultIllnessServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestAdultIllnessServiceRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestAdultIllnessServiceIndicator);
// 7.8.f Female Number of patients requested INTEGRATED MANAGEMENT OF CHILDHOOD ILLNESS Service
		
		CodedObsCohortDefinition patientRequestChildIllnessService=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.INTEGRATED_MANAGEMENT_OF_CHILDHOOD_ILLNESS, SetComparator.IN, TimeModifier.ANY);
		patientRequestChildIllnessService.setName("patientRequestChildIllnessService");
		patientRequestChildIllnessService.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestChildIllnessService.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestChildIllnessService);
		
		CompositionCohortDefinition femalePatientsrequestChildIllnessServiceRegistration=new CompositionCohortDefinition();
		femalePatientsrequestChildIllnessServiceRegistration.setName("femalePatientsrequestChildIllnessServiceRegistration");
		femalePatientsrequestChildIllnessServiceRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestChildIllnessServiceRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestChildIllnessServiceRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestChildIllnessServiceRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestChildIllnessServiceRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestChildIllnessServiceRegistration.getSearches().put("patientRequestChildIllnessService", new Mapped<CohortDefinition>(patientRequestChildIllnessService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestChildIllnessServiceRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestChildIllnessServiceRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestChildIllnessService");
		h.replaceCohortDefinition(femalePatientsrequestChildIllnessServiceRegistration);
		
		CohortIndicator femalePatientsrequestChildIllnessServiceIndicator = new CohortIndicator();
		femalePatientsrequestChildIllnessServiceIndicator.setName("femalePatientsrequestChildIllnessServiceIndicator");
		femalePatientsrequestChildIllnessServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestChildIllnessServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestChildIllnessServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestChildIllnessServiceRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestChildIllnessServiceIndicator);
// 7.8.m Male Number of patients requested INTEGRATED MANAGEMENT OF CHILDHOOD ILLNESS Service
		
		CompositionCohortDefinition malePatientsrequestChildIllnessServiceRegistration=new CompositionCohortDefinition();
		malePatientsrequestChildIllnessServiceRegistration.setName("malePatientsrequestChildIllnessServiceRegistration");
		malePatientsrequestChildIllnessServiceRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestChildIllnessServiceRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestChildIllnessServiceRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestChildIllnessServiceRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestChildIllnessServiceRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestChildIllnessServiceRegistration.getSearches().put("patientRequestChildIllnessService", new Mapped<CohortDefinition>(patientRequestChildIllnessService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestChildIllnessServiceRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestChildIllnessServiceRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestChildIllnessService");
		h.replaceCohortDefinition(malePatientsrequestChildIllnessServiceRegistration);
		
		CohortIndicator malePatientsrequestChildIllnessServiceIndicator = new CohortIndicator();
		malePatientsrequestChildIllnessServiceIndicator.setName("malePatientsrequestChildIllnessServiceIndicator");
		malePatientsrequestChildIllnessServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestChildIllnessServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestChildIllnessServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestChildIllnessServiceRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestChildIllnessServiceIndicator);
// 7.9.f Female Number of patients requested INFECTIOUS DISEASES CLINIC SERVICE
				
		CodedObsCohortDefinition patientRequestInfectiousDiseasesService=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.INFECTIOUS_DISEASES_CLINIC_SERVICE, SetComparator.IN, TimeModifier.ANY);
		patientRequestInfectiousDiseasesService.setName("patientRequestInfectiousDiseasesService");
		patientRequestInfectiousDiseasesService.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestInfectiousDiseasesService.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestInfectiousDiseasesService);
				
		CompositionCohortDefinition femalePatientsrequestInfectiousDiseasesServiceInRegistration=new CompositionCohortDefinition();
		femalePatientsrequestInfectiousDiseasesServiceInRegistration.setName("femalePatientsrequestInfectiousDiseasesServiceInRegistration");
		femalePatientsrequestInfectiousDiseasesServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestInfectiousDiseasesServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestInfectiousDiseasesServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestInfectiousDiseasesServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestInfectiousDiseasesServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestInfectiousDiseasesServiceInRegistration.getSearches().put("patientRequestInfectiousDiseasesService", new Mapped<CohortDefinition>(patientRequestInfectiousDiseasesService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestInfectiousDiseasesServiceInRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestInfectiousDiseasesServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestInfectiousDiseasesService");
		h.replaceCohortDefinition(femalePatientsrequestInfectiousDiseasesServiceInRegistration);
		
		CohortIndicator femalePatientsrequestInfectiousDiseasesServiceIndicator = new CohortIndicator();
		femalePatientsrequestInfectiousDiseasesServiceIndicator.setName("femalePatientsrequestInfectiousDiseasesServiceIndicator");
		femalePatientsrequestInfectiousDiseasesServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestInfectiousDiseasesServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestInfectiousDiseasesServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestInfectiousDiseasesServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestInfectiousDiseasesServiceIndicator);
// 7.9.m Male Number of patients requested INFECTIOUS DISEASES CLINIC SERVICE
						
		CompositionCohortDefinition malePatientsrequestInfectiousDiseasesServiceInRegistration=new CompositionCohortDefinition();
		malePatientsrequestInfectiousDiseasesServiceInRegistration.setName("malePatientsrequestInfectiousDiseasesServiceInRegistration");
		malePatientsrequestInfectiousDiseasesServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestInfectiousDiseasesServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestInfectiousDiseasesServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestInfectiousDiseasesServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestInfectiousDiseasesServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestInfectiousDiseasesServiceInRegistration.getSearches().put("patientRequestInfectiousDiseasesService", new Mapped<CohortDefinition>(patientRequestInfectiousDiseasesService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestInfectiousDiseasesServiceInRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestInfectiousDiseasesServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestInfectiousDiseasesService");
		h.replaceCohortDefinition(malePatientsrequestInfectiousDiseasesServiceInRegistration);
		
		CohortIndicator malePatientsrequestInfectiousDiseasesServiceIndicator = new CohortIndicator();
		malePatientsrequestInfectiousDiseasesServiceIndicator.setName("malePatientsrequestInfectiousDiseasesServiceIndicator");
		malePatientsrequestInfectiousDiseasesServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestInfectiousDiseasesServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestInfectiousDiseasesServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestInfectiousDiseasesServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestInfectiousDiseasesServiceIndicator);
// 7.10.f Female Number of patients requested SOCIAL WORKER SERVICE
				
		CodedObsCohortDefinition patientRequestSocialWorkerService=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.SOCIAL_WORKER_SERVICE, SetComparator.IN, TimeModifier.ANY);
		patientRequestSocialWorkerService.setName("patientRequestSocialWorkerService");
		patientRequestSocialWorkerService.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestSocialWorkerService.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestSocialWorkerService);
		
		CompositionCohortDefinition femalePatientsrequestSocialWorkerServiceInRegistration=new CompositionCohortDefinition();
		femalePatientsrequestSocialWorkerServiceInRegistration.setName("femalePatientsrequestSocialWorkerServiceInRegistration");
		femalePatientsrequestSocialWorkerServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestSocialWorkerServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestSocialWorkerServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestSocialWorkerServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestSocialWorkerServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestSocialWorkerServiceInRegistration.getSearches().put("patientRequestSocialWorkerService", new Mapped<CohortDefinition>(patientRequestSocialWorkerService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestSocialWorkerServiceInRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestSocialWorkerServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestSocialWorkerService");
		h.replaceCohortDefinition(femalePatientsrequestSocialWorkerServiceInRegistration);
		
		CohortIndicator femalePatientsrequestSocialWorkerServiceIndicator = new CohortIndicator();
		femalePatientsrequestSocialWorkerServiceIndicator.setName("femalePatientsrequestSocialWorkerServiceIndicator");
		femalePatientsrequestSocialWorkerServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestSocialWorkerServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestSocialWorkerServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestSocialWorkerServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestSocialWorkerServiceIndicator);
// 7.10.f Male Number of patients requested SOCIAL WORKER SERVICE
				
		CompositionCohortDefinition malePatientsrequestSocialWorkerServiceInRegistration=new CompositionCohortDefinition();
		malePatientsrequestSocialWorkerServiceInRegistration.setName("malePatientsrequestSocialWorkerServiceInRegistration");
		malePatientsrequestSocialWorkerServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestSocialWorkerServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestSocialWorkerServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestSocialWorkerServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestSocialWorkerServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestSocialWorkerServiceInRegistration.getSearches().put("patientRequestSocialWorkerService", new Mapped<CohortDefinition>(patientRequestSocialWorkerService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestSocialWorkerServiceInRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestSocialWorkerServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestSocialWorkerService");
		h.replaceCohortDefinition(malePatientsrequestSocialWorkerServiceInRegistration);
		
		CohortIndicator malePatientsrequestSocialWorkerServiceIndicator = new CohortIndicator();
		malePatientsrequestSocialWorkerServiceIndicator.setName("malePatientsrequestSocialWorkerServiceIndicator");
		malePatientsrequestSocialWorkerServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestSocialWorkerServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestSocialWorkerServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestSocialWorkerServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestSocialWorkerServiceIndicator);	

// 7.11.f Female Number of patients requested PREVENTION OF MOTHER TO CHILD TRANSMISSION SERVICE
				
		CodedObsCohortDefinition patientRequestPMTCTService=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.PREVENTION_OF_MOTHER_TO_CHILD_TRANSMISSION_SERVICE, SetComparator.IN, TimeModifier.ANY);
		patientRequestPMTCTService.setName("patientRequestPMTCTService");
		patientRequestPMTCTService.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestPMTCTService.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestPMTCTService);
		
		CompositionCohortDefinition femalePatientsrequestPMTCTServiceInRegistration=new CompositionCohortDefinition();
		femalePatientsrequestPMTCTServiceInRegistration.setName("femalePatientsrequestPMTCTServiceInRegistration");
		femalePatientsrequestPMTCTServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestPMTCTServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestPMTCTServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestPMTCTServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestPMTCTServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestPMTCTServiceInRegistration.getSearches().put("patientRequestPMTCTService", new Mapped<CohortDefinition>(patientRequestPMTCTService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestPMTCTServiceInRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestPMTCTServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestPMTCTService");
		h.replaceCohortDefinition(femalePatientsrequestPMTCTServiceInRegistration);
		
		CohortIndicator femalePatientsrequestPMTCTServiceIndicator = new CohortIndicator();
		femalePatientsrequestPMTCTServiceIndicator.setName("femalePatientsrequestPMTCTServiceIndicator");
		femalePatientsrequestPMTCTServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestPMTCTServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestPMTCTServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestPMTCTServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestPMTCTServiceIndicator);

// 7.11.f Male Number of patients requested PREVENTION OF MOTHER TO CHILD TRANSMISSION SERVICE
				
		CompositionCohortDefinition malePatientsrequestPMTCTServiceInRegistration=new CompositionCohortDefinition();
		malePatientsrequestPMTCTServiceInRegistration.setName("malePatientsrequestPMTCTServiceInRegistration");
		malePatientsrequestPMTCTServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestPMTCTServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestPMTCTServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestPMTCTServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestPMTCTServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestPMTCTServiceInRegistration.getSearches().put("patientRequestPMTCTService", new Mapped<CohortDefinition>(patientRequestPMTCTService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestPMTCTServiceInRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestPMTCTServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestPMTCTService");
		h.replaceCohortDefinition(malePatientsrequestPMTCTServiceInRegistration);
		
		CohortIndicator malePatientsrequestPMTCTServiceIndicator = new CohortIndicator();
		malePatientsrequestPMTCTServiceIndicator.setName("malePatientsrequestPMTCTServiceIndicator");
		malePatientsrequestPMTCTServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestPMTCTServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestPMTCTServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestPMTCTServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestPMTCTServiceIndicator);
//7.12.f. Female Number of patients requested LABORATORY SERVICE
				
		CodedObsCohortDefinition patientRequestLabService=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.LABORATORY_SERVICES, SetComparator.IN, TimeModifier.ANY);
		patientRequestLabService.setName("patientRequestLabService");
		patientRequestLabService.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestLabService.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestLabService);
					
		CompositionCohortDefinition femalePatientsrequestLabServiceInRegistration=new CompositionCohortDefinition();
		femalePatientsrequestLabServiceInRegistration.setName("femalePatientsrequestLabServiceInRegistration");
		femalePatientsrequestLabServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestLabServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestLabServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestLabServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestLabServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestLabServiceInRegistration.getSearches().put("patientRequestLabService", new Mapped<CohortDefinition>(patientRequestLabService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestLabServiceInRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestLabServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestLabService");
		h.replaceCohortDefinition(femalePatientsrequestLabServiceInRegistration);
					
		CohortIndicator femalePatientsrequestLabServiceIndicator = new CohortIndicator();
		femalePatientsrequestLabServiceIndicator.setName("femalePatientsrequestLabServiceIndicator");
		femalePatientsrequestLabServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestLabServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestLabServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestLabServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestLabServiceIndicator);
//7.12.m Male Number of patients requested LABORATORY SERVICE
	
		CompositionCohortDefinition malePatientsrequestLabServiceInRegistration=new CompositionCohortDefinition();
		malePatientsrequestLabServiceInRegistration.setName("malePatientsrequestLabServiceInRegistration");
		malePatientsrequestLabServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestLabServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestLabServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestLabServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestLabServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestLabServiceInRegistration.getSearches().put("patientRequestLabService", new Mapped<CohortDefinition>(patientRequestLabService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestLabServiceInRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestLabServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestLabService");
		h.replaceCohortDefinition(malePatientsrequestLabServiceInRegistration);
					
		CohortIndicator malePatientsrequestLabServiceIndicator = new CohortIndicator();
		malePatientsrequestLabServiceIndicator.setName("malePatientsrequestLabServiceIndicator");
		malePatientsrequestLabServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestLabServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestLabServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestLabServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestLabServiceIndicator);
//7.13.f. Female Number of patients requested PHARMACY SERVICES
	
		CodedObsCohortDefinition patientRequestPharmacyService=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.PHARMACY_SERVICES, SetComparator.IN, TimeModifier.ANY);
		patientRequestPharmacyService.setName("patientRequestPharmacyService");
		patientRequestPharmacyService.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestPharmacyService.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestPharmacyService);
				
		CompositionCohortDefinition femalePatientsrequestPharmacyServiceInRegistration=new CompositionCohortDefinition();
		femalePatientsrequestPharmacyServiceInRegistration.setName("femalePatientsrequestPharmacyServiceInRegistration");
		femalePatientsrequestPharmacyServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestPharmacyServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestPharmacyServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestPharmacyServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestPharmacyServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestPharmacyServiceInRegistration.getSearches().put("patientRequestPharmacyService", new Mapped<CohortDefinition>(patientRequestPharmacyService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestPharmacyServiceInRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestPharmacyServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestPharmacyService");
		h.replaceCohortDefinition(femalePatientsrequestPharmacyServiceInRegistration);
					
		CohortIndicator femalePatientsrequestPharmacyServiceIndicator = new CohortIndicator();
		femalePatientsrequestPharmacyServiceIndicator.setName("femalePatientsrequestPharmacyServiceIndicator");
		femalePatientsrequestPharmacyServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestPharmacyServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestPharmacyServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestPharmacyServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestPharmacyServiceIndicator);
//7.13.m Male Number of patients requested PHARMACY SERVICE
	
		CompositionCohortDefinition malePatientsrequestPharmacyServiceInRegistration=new CompositionCohortDefinition();
		malePatientsrequestPharmacyServiceInRegistration.setName("malePatientsrequestPharmacyServiceInRegistration");
		malePatientsrequestPharmacyServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestPharmacyServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestPharmacyServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestPharmacyServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestPharmacyServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestPharmacyServiceInRegistration.getSearches().put("patientRequestPharmacyService", new Mapped<CohortDefinition>(patientRequestPharmacyService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestPharmacyServiceInRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestPharmacyServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestPharmacyService");
		h.replaceCohortDefinition(malePatientsrequestPharmacyServiceInRegistration);
					
		CohortIndicator malePatientsrequestPharmacyServiceIndicator = new CohortIndicator();
		malePatientsrequestPharmacyServiceIndicator.setName("malePatientsrequestPharmacyServiceIndicator");
		malePatientsrequestPharmacyServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestPharmacyServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestPharmacyServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestPharmacyServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestPharmacyServiceIndicator);
//7.13.f. Female Number of patients requested MATERNITY SERVICES
	
		CodedObsCohortDefinition patientRequestMaternityService=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.MATERNITY_SERVICE, SetComparator.IN, TimeModifier.ANY);
		patientRequestMaternityService.setName("patientRequestMaternityService");
		patientRequestMaternityService.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestMaternityService.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestMaternityService);
					
		CompositionCohortDefinition femalePatientsrequestMaternityServiceInRegistration=new CompositionCohortDefinition();
		femalePatientsrequestMaternityServiceInRegistration.setName("femalePatientsrequestMaternityServiceInRegistration");
		femalePatientsrequestMaternityServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestMaternityServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestMaternityServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestMaternityServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestMaternityServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestMaternityServiceInRegistration.getSearches().put("patientRequestMaternityService", new Mapped<CohortDefinition>(patientRequestMaternityService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestMaternityServiceInRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestMaternityServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestMaternityService");
		h.replaceCohortDefinition(femalePatientsrequestMaternityServiceInRegistration);
					
		CohortIndicator femalePatientsrequestMaternityServiceIndicator = new CohortIndicator();
		femalePatientsrequestMaternityServiceIndicator.setName("femalePatientsrequestMaternityServiceIndicator");
		femalePatientsrequestMaternityServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestMaternityServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestMaternityServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestMaternityServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestMaternityServiceIndicator);
//7.14.m Male Number of patients requested MATERNITY SERVICE
		
		CompositionCohortDefinition malePatientsrequestMaternityServiceInRegistration=new CompositionCohortDefinition();
		malePatientsrequestMaternityServiceInRegistration.setName("malePatientsrequestMaternityServiceInRegistration");
		malePatientsrequestMaternityServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestMaternityServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestMaternityServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestMaternityServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestMaternityServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestMaternityServiceInRegistration.getSearches().put("patientRequestMaternityService", new Mapped<CohortDefinition>(patientRequestMaternityService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestMaternityServiceInRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestMaternityServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestMaternityService");
		h.replaceCohortDefinition(malePatientsrequestMaternityServiceInRegistration);
					
		CohortIndicator malePatientsrequestMaternityServiceIndicator = new CohortIndicator();
		malePatientsrequestMaternityServiceIndicator.setName("malePatientsrequestMaternityServiceIndicator");
		malePatientsrequestMaternityServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestMaternityServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestMaternityServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestMaternityServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestMaternityServiceIndicator);
//7.15.f Female Number of patients requested HOSPITALIZATION SERVICE
	
		CodedObsCohortDefinition patientRequestHospitalizationService=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.HOSPITALIZATION_SERVICE, SetComparator.IN, TimeModifier.ANY);
		patientRequestHospitalizationService.setName("patientRequestHospitalizationService");
		patientRequestHospitalizationService.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestHospitalizationService.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestHospitalizationService);
					
		CompositionCohortDefinition femalePatientsrequestHospitalizationServiceInRegistration=new CompositionCohortDefinition();
		femalePatientsrequestHospitalizationServiceInRegistration.setName("femalePatientsrequestHospitalizationServiceInRegistration");
		femalePatientsrequestHospitalizationServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestHospitalizationServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestHospitalizationServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestHospitalizationServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestHospitalizationServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestHospitalizationServiceInRegistration.getSearches().put("patientRequestHospitalizationService", new Mapped<CohortDefinition>(patientRequestHospitalizationService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestHospitalizationServiceInRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestHospitalizationServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestHospitalizationService");
		h.replaceCohortDefinition(femalePatientsrequestHospitalizationServiceInRegistration);
					
		CohortIndicator femalePatientsrequestHospitalizationServiceIndicator = new CohortIndicator();
		femalePatientsrequestHospitalizationServiceIndicator.setName("femalePatientsrequestHospitalizationServiceIndicator");
		femalePatientsrequestHospitalizationServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestHospitalizationServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestHospitalizationServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestHospitalizationServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestHospitalizationServiceIndicator);
//7.15.m Male Number of patients requested HOSPITALIZATION SERVICE
	
		CompositionCohortDefinition malePatientsrequestHospitalizationServiceInRegistration=new CompositionCohortDefinition();
		malePatientsrequestHospitalizationServiceInRegistration.setName("malePatientsrequestHospitalizationServiceInRegistration");
		malePatientsrequestHospitalizationServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestHospitalizationServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestHospitalizationServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestHospitalizationServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestHospitalizationServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestHospitalizationServiceInRegistration.getSearches().put("patientRequestHospitalizationService", new Mapped<CohortDefinition>(patientRequestHospitalizationService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestHospitalizationServiceInRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestHospitalizationServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestHospitalizationService");
		h.replaceCohortDefinition(malePatientsrequestHospitalizationServiceInRegistration);
					
		CohortIndicator malePatientsrequestHospitalizationServiceIndicator = new CohortIndicator();
		malePatientsrequestHospitalizationServiceIndicator.setName("malePatientsrequestHospitalizationServiceIndicator");
		malePatientsrequestHospitalizationServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestHospitalizationServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestHospitalizationServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestHospitalizationServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestHospitalizationServiceIndicator);
// 7.16.f Female Number of patients requested VACCINATION SERVICE
		CodedObsCohortDefinition patientRequestVaccinationService=makeCodedObsCohortDefinition(PrimaryCareReportConstants.PRIMARY_CARE_SERVICE_REQUESTED, PrimaryCareReportConstants.VACCINATION_SERVICE, SetComparator.IN, TimeModifier.ANY);
		patientRequestVaccinationService.setName("patientRequestVaccinationService");
		patientRequestVaccinationService.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		patientRequestVaccinationService.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		h.replaceCohortDefinition(patientRequestVaccinationService);
					
		CompositionCohortDefinition femalePatientsrequestVaccinationServiceInRegistration=new CompositionCohortDefinition();
		femalePatientsrequestVaccinationServiceInRegistration.setName("femalePatientsrequestVaccinationServiceInRegistration");
		femalePatientsrequestVaccinationServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		femalePatientsrequestVaccinationServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		femalePatientsrequestVaccinationServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestVaccinationServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestVaccinationServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestVaccinationServiceInRegistration.getSearches().put("patientRequestVaccinationService", new Mapped<CohortDefinition>(patientRequestVaccinationService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		femalePatientsrequestVaccinationServiceInRegistration.getSearches().put("femalePatientsrequestPrimCare", new Mapped<CohortDefinition>(femalePatientsrequestPrimCare,null));
		femalePatientsrequestVaccinationServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND femalePatientsrequestPrimCare AND patientRequestVaccinationService");
		h.replaceCohortDefinition(femalePatientsrequestVaccinationServiceInRegistration);
					
		CohortIndicator femalePatientsrequestVaccinationServiceIndicator = new CohortIndicator();
		femalePatientsrequestVaccinationServiceIndicator.setName("femalePatientsrequestVaccinationServiceIndicator");
		femalePatientsrequestVaccinationServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		femalePatientsrequestVaccinationServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		femalePatientsrequestVaccinationServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(femalePatientsrequestVaccinationServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(femalePatientsrequestVaccinationServiceIndicator);
//7.16.m Male Number of patients requested VACCINATION SERVICE
	
		CompositionCohortDefinition malePatientsrequestVaccinationServiceInRegistration=new CompositionCohortDefinition();
		malePatientsrequestVaccinationServiceInRegistration.setName("malePatientsrequestVaccinationServiceInRegistration");
		malePatientsrequestVaccinationServiceInRegistration.addParameter(new Parameter("onOrAfter","onOrAfter",Date.class));
		malePatientsrequestVaccinationServiceInRegistration.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		malePatientsrequestVaccinationServiceInRegistration.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestVaccinationServiceInRegistration.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestVaccinationServiceInRegistration.getSearches().put("patientsWithPrimaryCareRegistration", new Mapped<CohortDefinition>(patientsWithPrimaryCareRegistration,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestVaccinationServiceInRegistration.getSearches().put("patientRequestVaccinationService", new Mapped<CohortDefinition>(patientRequestVaccinationService,ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsrequestVaccinationServiceInRegistration.getSearches().put("malePatientsrequestPrimCare", new Mapped<CohortDefinition>(malePatientsrequestPrimCare,null));
		malePatientsrequestVaccinationServiceInRegistration.setCompositionString("patientsWithPrimaryCareRegistration AND malePatientsrequestPrimCare AND patientRequestVaccinationService");
		h.replaceCohortDefinition(malePatientsrequestVaccinationServiceInRegistration);
					
		CohortIndicator malePatientsrequestVaccinationServiceIndicator = new CohortIndicator();
		malePatientsrequestVaccinationServiceIndicator.setName("malePatientsrequestVaccinationServiceIndicator");
		malePatientsrequestVaccinationServiceIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		malePatientsrequestVaccinationServiceIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		malePatientsrequestVaccinationServiceIndicator.setCohortDefinition(new Mapped<CohortDefinition>(malePatientsrequestVaccinationServiceInRegistration,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		h.replaceDefinition(malePatientsrequestVaccinationServiceIndicator);
				
		
		SqlCohortDefinition sqltest=new SqlCohortDefinition();
		sqltest.setQuery("select distinct patient_id from (SELECT e.patient_id,e.encounter_datetime,count(e.encounter_type) as timesofregistration FROM encounter e where e.encounter_type=20 and e.voided=0 group by e.patient_id) as patientregistrationtimes where timesofregistration=2 and encounter_datetime>= :startDate");
		sqltest.addParameter(new Parameter("startDate", "startDate", Date.class));
		
		CohortIndicator sqltestIndicator=new CohortIndicator();
		sqltestIndicator.addParameter(new Parameter("startDate", "startDate", Date.class));
		sqltestIndicator.addParameter(new Parameter("endDate", "endDate", Date.class));
		sqltestIndicator.setCohortDefinition(new Mapped<CohortDefinition>(sqltest,ParameterizableUtil.createParameterMappings("startDate=${startDate}")));
		
		
		// add global filter to the report
		
//		rd.addIndicator("1.1.1", "Monday 1st", monday1Indicator);
//		rd.addIndicator("1.1.2", "Monday 2nd", monday2Indicator);
//		rd.addIndicator("1.1.3", "Monday 3rd", monday3Indicator);
//		rd.addIndicator("1.1.4", "Monday 4th", monday4Indicator);
//		rd.addIndicator("1.1.5", "Monday 5th", monday5Indicator);
//		rd.addIndicator("1.1.6", "Monday 6th", monday6Indicator);
//		rd.addIndicator("1.1.7", "Monday 7th", monday7Indicator);
//		rd.addIndicator("1.1.8", "Monday 8th", monday8Indicator);
//		rd.addIndicator("1.1.9", "Monday 9th", monday9Indicator);
//		rd.addIndicator("1.1.10", "Monday 10th", monday10Indicator);
//		rd.addIndicator("1.1.11", "Monday 11th", monday11Indicator);
//		rd.addIndicator("1.1.12", "Monday 12th", monday12Indicator);
//		rd.addIndicator("1.1.13", "Monday 13th", monday13Indicator);
//		rd.addIndicator("1.1.14", "Monday 14th", monday14Indicator);
//		rd.addIndicator("1.1.15", "Monday 15th", monday15Indicator);
//		rd.addIndicator("1.1.16", "Monday 16th", monday16Indicator);
//		rd.addIndicator("1.1.17", "Monday 17th", monday17Indicator);
//		rd.addIndicator("1.1.18", "Monday 18th", monday18Indicator);
//		rd.addIndicator("1.1.19", "Monday 19th", monday19Indicator);
//		rd.addIndicator("1.1.20", "Monday 20th", monday20Indicator);
//		rd.addIndicator("1.1.21", "Monday 21st", monday21Indicator);
//		rd.addIndicator("1.1.22", "Monday 22nd", monday22Indicator);
//		rd.addIndicator("1.1.23", "Monday 23rd", monday23Indicator);
//		rd.addIndicator("1.1.24", "Monday 24th", monday24Indicator);
//		rd.addIndicator("1.1.25", "Monday 25th", monday25Indicator);
//		rd.addIndicator("1.1.26", "Monday 26th", monday26Indicator);
//		rd.addIndicator("1.1.27", "Monday 27th", monday27Indicator);
//		rd.addIndicator("1.1.28", "Monday 28th", monday28Indicator);
//		rd.addIndicator("1.1.29", "Monday 29th", monday29Indicator);
//		rd.addIndicator("1.1.30", "Monday 30th", monday30Indicator);
//		rd.addIndicator("1.1.31", "Monday 31st", monday31Indicator);
//		
//		rd.addIndicator("1.2.1", "Tuesday 1st", tuesday1Indicator);
//		rd.addIndicator("1.2.2", "Tuesday 2nd", tuesday2Indicator);
//		rd.addIndicator("1.2.3", "Tuesday 3rd", tuesday3Indicator);
//		rd.addIndicator("1.2.4", "Tuesday 4th", tuesday4Indicator);
//		rd.addIndicator("1.2.5", "Tuesday 5th", tuesday5Indicator);
//		rd.addIndicator("1.2.6", "Tuesday 6th", tuesday6Indicator);
//		rd.addIndicator("1.2.7", "Tuesday 7th", tuesday7Indicator);
//		rd.addIndicator("1.2.8", "Tuesday 8th", tuesday8Indicator);
//		rd.addIndicator("1.2.9", "Tuesday 9th", tuesday9Indicator);
//		rd.addIndicator("1.2.10", "Tuesday 10th", tuesday10Indicator);
//		rd.addIndicator("1.2.11", "Tuesday 11th", tuesday11Indicator);
//		rd.addIndicator("1.2.12", "Tuesday 12th", tuesday12Indicator);
//		rd.addIndicator("1.2.13", "Tuesday 13th", tuesday13Indicator);
//		rd.addIndicator("1.2.14", "Tuesday 14th", tuesday14Indicator);
//		rd.addIndicator("1.2.15", "Tuesday 15th", tuesday15Indicator);
//		rd.addIndicator("1.2.16", "Tuesday 16th", tuesday16Indicator);
//		rd.addIndicator("1.2.17", "Tuesday 17th", tuesday17Indicator);
//		rd.addIndicator("1.2.18", "Tuesday 18th", tuesday18Indicator);
//		rd.addIndicator("1.2.19", "Tuesday 19th", tuesday19Indicator);
//		rd.addIndicator("1.2.20", "Tuesday 20th", tuesday20Indicator);
//		rd.addIndicator("1.2.21", "Tuesday 21st", tuesday21Indicator);
//		rd.addIndicator("1.2.22", "Tuesday 22nd", tuesday22Indicator);
//		rd.addIndicator("1.2.23", "Tuesday 23rd", tuesday23Indicator);
//		rd.addIndicator("1.2.24", "Tuesday 24th", tuesday24Indicator);
//		rd.addIndicator("1.2.25", "Tuesday 25th", tuesday25Indicator);
//		rd.addIndicator("1.2.26", "Tuesday 26th", tuesday26Indicator);
//		rd.addIndicator("1.2.27", "Tuesday 27th", tuesday27Indicator);
//		rd.addIndicator("1.2.28", "Tuesday 28th", tuesday28Indicator);
//		rd.addIndicator("1.2.29", "Tuesday 29th", tuesday29Indicator);
//		rd.addIndicator("1.2.30", "Tuesday 30th", tuesday30Indicator);
//		rd.addIndicator("1.2.31", "Tuesday 31st", tuesday31Indicator);
//			
//		rd.addIndicator("1.3.1", "wednesday 1st", wednesday1Indicator);
//		rd.addIndicator("1.3.2", "wednesday 2nd", wednesday2Indicator);
//		rd.addIndicator("1.3.3", "wednesday 3rd", wednesday3Indicator);
//		rd.addIndicator("1.3.4", "wednesday 4th", wednesday4Indicator);
//		rd.addIndicator("1.3.5", "wednesday 5th", wednesday5Indicator);
//		rd.addIndicator("1.3.6", "wednesday 6th", wednesday6Indicator);
//		rd.addIndicator("1.3.7", "wednesday 7th", wednesday7Indicator);
//		rd.addIndicator("1.3.8", "wednesday 8th", wednesday8Indicator);
//		rd.addIndicator("1.3.9", "wednesday 9th", wednesday9Indicator);
//		rd.addIndicator("1.3.10", "wednesday 10th", wednesday10Indicator);
//		rd.addIndicator("1.3.11", "wednesday 11th", wednesday11Indicator);
//		rd.addIndicator("1.3.12", "wednesday 12th", wednesday12Indicator);
//		rd.addIndicator("1.3.13", "wednesday 13th", wednesday13Indicator);
//		rd.addIndicator("1.3.14", "wednesday 14th", wednesday14Indicator);
//		rd.addIndicator("1.3.15", "wednesday 15th", wednesday15Indicator);
//		rd.addIndicator("1.3.16", "wednesday 16th", wednesday16Indicator);
//		rd.addIndicator("1.3.17", "wednesday 17th", wednesday17Indicator);
//		rd.addIndicator("1.3.18", "wednesday 18th", wednesday18Indicator);
//		rd.addIndicator("1.3.19", "wednesday 19th", wednesday19Indicator);
//		rd.addIndicator("1.3.20", "wednesday 20th", wednesday20Indicator);
//		rd.addIndicator("1.3.21", "wednesday 21st", wednesday21Indicator);
//		rd.addIndicator("1.3.22", "wednesday 22nd", wednesday22Indicator);
//		rd.addIndicator("1.3.23", "wednesday 23rd", wednesday23Indicator);
//		rd.addIndicator("1.3.24", "wednesday 24th", wednesday24Indicator);
//		rd.addIndicator("1.3.25", "wednesday 25th", wednesday25Indicator);
//		rd.addIndicator("1.3.26", "wednesday 26th", wednesday26Indicator);
//		rd.addIndicator("1.3.27", "wednesday 27th", wednesday27Indicator);
//		rd.addIndicator("1.3.28", "wednesday 28th", wednesday28Indicator);
//		rd.addIndicator("1.3.29", "wednesday 29th", wednesday29Indicator);
//		rd.addIndicator("1.3.30", "wednesday 30th", wednesday30Indicator);
//		rd.addIndicator("1.3.31", "wednesday 31st", wednesday31Indicator);
//		
//		
//		rd.addIndicator("1.4.1", "thursday 1st", thursday1Indicator);
//		rd.addIndicator("1.4.2", "thursday 2nd", thursday2Indicator);
//		rd.addIndicator("1.4.3", "thursday 3rd", thursday3Indicator);
//		rd.addIndicator("1.4.4", "thursday 4th", thursday4Indicator);
//		rd.addIndicator("1.4.5", "thursday 5th", thursday5Indicator);
//		rd.addIndicator("1.4.6", "thursday 6th", thursday6Indicator);
//		rd.addIndicator("1.4.7", "thursday 7th", thursday7Indicator);
//		rd.addIndicator("1.4.8", "thursday 8th", thursday8Indicator);
//		rd.addIndicator("1.4.9", "thursday 9th", thursday9Indicator);
//		rd.addIndicator("1.4.10", "thursday 10th", thursday10Indicator);
//		rd.addIndicator("1.4.11", "thursday 11th", thursday11Indicator);
//		rd.addIndicator("1.4.12", "thursday 13th", thursday12Indicator);
//		rd.addIndicator("1.4.13", "thursday 13th", thursday13Indicator);
//		rd.addIndicator("1.4.14", "thursday 14th", thursday14Indicator);
//		rd.addIndicator("1.4.15", "thursday 15th", thursday15Indicator);
//		rd.addIndicator("1.4.16", "thursday 16th", thursday16Indicator);
//		rd.addIndicator("1.4.17", "thursday 17th", thursday17Indicator);
//		rd.addIndicator("1.4.18", "thursday 18th", thursday18Indicator);
//		rd.addIndicator("1.4.19", "thursday 19th", thursday19Indicator);
//		rd.addIndicator("1.4.20", "thursday 20th", thursday20Indicator);
//		rd.addIndicator("1.4.21", "thursday 21st", thursday21Indicator);
//		rd.addIndicator("1.4.22", "thursday 22nd", thursday22Indicator);
//		rd.addIndicator("1.4.23", "thursday 23rd", thursday23Indicator);
//		rd.addIndicator("1.4.24", "thursday 24th", thursday24Indicator);
//		rd.addIndicator("1.4.25", "thursday 25th", thursday25Indicator);
//		rd.addIndicator("1.4.26", "thursday 26th", thursday26Indicator);
//		rd.addIndicator("1.4.27", "thursday 27th", thursday27Indicator);
//		rd.addIndicator("1.4.28", "thursday 28th", thursday28Indicator);
//		rd.addIndicator("1.4.29", "thursday 29th", thursday29Indicator);
//		rd.addIndicator("1.4.30", "thursday 30th", thursday30Indicator);
//		rd.addIndicator("1.4.31", "thursday 31st", thursday31Indicator);
//		
//		rd.addIndicator("1.5.1", "friday 1st", friday1Indicator);
//		rd.addIndicator("1.5.2", "friday 2nd", friday2Indicator);
//		rd.addIndicator("1.5.3", "friday 3rd", friday3Indicator);
//		rd.addIndicator("1.5.4", "friday 4th", friday4Indicator);
//		rd.addIndicator("1.5.5", "friday 5th", friday5Indicator);
//		rd.addIndicator("1.5.6", "friday 6th", friday6Indicator);
//		rd.addIndicator("1.5.7", "friday 7th", friday7Indicator);
//		rd.addIndicator("1.5.8", "friday 8th", friday8Indicator);
//		rd.addIndicator("1.5.9", "friday 9th", friday9Indicator);
//		rd.addIndicator("1.5.10", "friday 10th", friday10Indicator);
//		rd.addIndicator("1.5.11", "friday 11th", friday11Indicator);
//		rd.addIndicator("1.5.12", "friday 13th", friday12Indicator);
//		rd.addIndicator("1.5.13", "friday 13th", friday13Indicator);
//		rd.addIndicator("1.5.14", "friday 14th", friday14Indicator);
//		rd.addIndicator("1.5.15", "friday 15th", friday15Indicator);
//		rd.addIndicator("1.5.16", "friday 16th", friday16Indicator);
//		rd.addIndicator("1.5.17", "friday 17th", friday17Indicator);
//		rd.addIndicator("1.5.18", "friday 18th", friday18Indicator);
//		rd.addIndicator("1.5.19", "friday 19th", friday19Indicator);
//		rd.addIndicator("1.5.20", "friday 20th", friday20Indicator);
//		rd.addIndicator("1.5.21", "friday 21st", friday21Indicator);
//		rd.addIndicator("1.5.22", "friday 22nd", friday22Indicator);
//		rd.addIndicator("1.5.23", "friday 23rd", friday23Indicator);
//		rd.addIndicator("1.5.24", "friday 24th", friday24Indicator);
//		rd.addIndicator("1.5.25", "friday 25th", friday25Indicator);
//		rd.addIndicator("1.5.26", "friday 26th", friday26Indicator);
//		rd.addIndicator("1.5.27", "friday 27th", friday27Indicator);
//		rd.addIndicator("1.5.28", "friday 28th", friday28Indicator);
//		rd.addIndicator("1.5.29", "friday 29th", friday29Indicator);
//		rd.addIndicator("1.5.30", "friday 30th", friday30Indicator);
//		rd.addIndicator("1.5.31", "friday 31st", friday31Indicator);
//		
//		rd.addIndicator("1.6.1", "saturday 1st", saturday1Indicator);
//		rd.addIndicator("1.6.2", "saturday 2nd", saturday2Indicator);
//		rd.addIndicator("1.6.3", "saturday 3rd", saturday3Indicator);
//		rd.addIndicator("1.6.4", "saturday 4th", saturday4Indicator);
//		rd.addIndicator("1.6.5", "saturday 5th", saturday5Indicator);
//		rd.addIndicator("1.6.6", "saturday 6th", saturday6Indicator);
//		rd.addIndicator("1.6.7", "saturday 7th", saturday7Indicator);
//		rd.addIndicator("1.6.8", "saturday 8th", saturday8Indicator);
//		rd.addIndicator("1.6.9", "saturday 9th", saturday9Indicator);
//		rd.addIndicator("1.6.10", "saturday 10th", saturday10Indicator);
//		rd.addIndicator("1.6.11", "saturday 11th", saturday11Indicator);
//		rd.addIndicator("1.6.12", "saturday 13th", saturday12Indicator);
//		rd.addIndicator("1.6.13", "saturday 13th", saturday13Indicator);
//		rd.addIndicator("1.6.14", "saturday 14th", saturday14Indicator);
//		rd.addIndicator("1.6.15", "saturday 15th", saturday15Indicator);
//		rd.addIndicator("1.6.16", "saturday 16th", saturday16Indicator);
//		rd.addIndicator("1.6.17", "saturday 17th", saturday17Indicator);
//		rd.addIndicator("1.6.18", "saturday 18th", saturday18Indicator);
//		rd.addIndicator("1.6.19", "saturday 19th", saturday19Indicator);
//		rd.addIndicator("1.6.20", "saturday 20th", saturday20Indicator);
//		rd.addIndicator("1.6.21", "saturday 21st", saturday21Indicator);
//		rd.addIndicator("1.6.22", "saturday 22nd", saturday22Indicator);
//		rd.addIndicator("1.6.23", "saturday 23rd", saturday23Indicator);
//		rd.addIndicator("1.6.24", "saturday 24th", saturday24Indicator);
//		rd.addIndicator("1.6.25", "saturday 25th", saturday25Indicator);
//		rd.addIndicator("1.6.26", "saturday 26th", saturday26Indicator);
//		rd.addIndicator("1.6.27", "saturday 27th", saturday27Indicator);
//		rd.addIndicator("1.6.28", "saturday 28th", saturday28Indicator);
//		rd.addIndicator("1.6.29", "saturday 29th", saturday29Indicator);
//		rd.addIndicator("1.6.30", "saturday 30th", saturday30Indicator);
//		rd.addIndicator("1.6.31", "saturday 31st", saturday31Indicator);
//		
//		rd.addIndicator("1.7.1", "sunday 1st", sunday1Indicator);
//		rd.addIndicator("1.7.2", "sunday 2nd", sunday2Indicator);
//		rd.addIndicator("1.7.3", "sunday 3rd", sunday3Indicator);
//		rd.addIndicator("1.7.4", "sunday 4th", sunday4Indicator);
//		rd.addIndicator("1.7.5", "sunday 5th", sunday5Indicator);
//		rd.addIndicator("1.7.6", "sunday 6th", sunday6Indicator);
//		rd.addIndicator("1.7.7", "sunday 7th", sunday7Indicator);
//		rd.addIndicator("1.7.8", "sunday 8th", sunday8Indicator);
//		rd.addIndicator("1.7.9", "sunday 9th", sunday9Indicator);
//		rd.addIndicator("1.7.10", "sunday 10th", sunday10Indicator);
//		rd.addIndicator("1.7.11", "sunday 11th", sunday11Indicator);
//		rd.addIndicator("1.7.12", "sunday 13th", sunday12Indicator);
//		rd.addIndicator("1.7.13", "sunday 13th", sunday13Indicator);
//		rd.addIndicator("1.7.14", "sunday 14th", sunday14Indicator);
//		rd.addIndicator("1.7.15", "sunday 15th", sunday15Indicator);
//		rd.addIndicator("1.7.16", "sunday 16th", sunday16Indicator);
//		rd.addIndicator("1.7.17", "sunday 17th", sunday17Indicator);
//		rd.addIndicator("1.7.18", "sunday 18th", sunday18Indicator);
//		rd.addIndicator("1.7.19", "sunday 19th", sunday19Indicator);
//		rd.addIndicator("1.7.20", "sunday 20th", sunday20Indicator);
//		rd.addIndicator("1.7.21", "sunday 21st", sunday21Indicator);
//		rd.addIndicator("1.7.22", "sunday 22nd", sunday22Indicator);
//		rd.addIndicator("1.7.23", "sunday 23rd", sunday23Indicator);
//		rd.addIndicator("1.7.24", "sunday 24th", sunday24Indicator);
//		rd.addIndicator("1.7.25", "sunday 25th", sunday25Indicator);
//		rd.addIndicator("1.7.26", "sunday 26th", sunday26Indicator);
//		rd.addIndicator("1.7.27", "sunday 27th", sunday27Indicator);
//		rd.addIndicator("1.7.28", "sunday 28th", sunday28Indicator);
//		rd.addIndicator("1.7.29", "sunday 29th", sunday29Indicator);
//		rd.addIndicator("1.7.30", "sunday 30th", sunday30Indicator);
//		rd.addIndicator("1.7.31", "sunday 31st", sunday31Indicator);
		
		rd.addIndicator("2.1", "Percent of patients who do not have an observation for temperature in the vitals", patientsWithoutTemperatureInVitalsIndicator);
		rd.addIndicator("2.2", "Percent of children under 5 who did have observation for temperature, and actually had a fever", patientsWithTemperatureGreaterThanNormalInVitalsIndicator);
		rd.addIndicator("2.3", "Percent of all registered patients under 5 who had a fever", allRegisteredPatientsWithTemperatureGreaterThanNormalInVitalsIndicator);
		
		rd.addIndicator("3.1", "Average number of patients registered per hour Mon through Friday between 8 and 10 am", peakHoursAndPeakDaysIndicator);
		
		rd.addIndicator("4.1", "Percent of patients who are missing an insurance in registration encounter", percentOfPatientsMissingInsIndicator);
		rd.addIndicator("4.2", "Number of patients who are missing an insurance in registration encounter", numberOfPatientsMissingInsIndicator);
		rd.addIndicator("4.3.1", "Percent of patients with MUTUELLE insurance in registration encounter", percentOfPatientsWithMUTUELLEInsIndicator);
		rd.addIndicator("4.3.2", "Percent of patients with RAMA insurance in registration encounter", percentOfPatientsWithRAMAInsIndicator);
		rd.addIndicator("4.3.3", "Percent of patients with MMI insurance in registration encounter", percentOfPatientsWithMMIInsIndicator);
		rd.addIndicator("4.3.4", "Percent of patients with MEDIPLAN insurance in registration encounter", percentOfPatientsWithMEDIPLANInsIndicator);
		rd.addIndicator("4.3.5", "Percent of patients with CORAR insurance in registration encounter", percentOfPatientsWithCORARInsIndicator);
		rd.addIndicator("4.3.6", "Percent of patients without (NONE) insurance in registration encounter", percentOfPatientsWithNONEInsIndicator);
		
		rd.addIndicator("5.1.1", "Number of patients who only have 1 registration encounter with MUTUELLE Insurance:", patientsWithMUTUELLEInsAndOneVisitIndicator);
		rd.addIndicator("5.1.2", "Number of patients who only have 1 registration encounter with RAMA Insurance:", patientsWithRAMAInsAndOneVisitIndicator);
		rd.addIndicator("5.1.3", "Number of patients who only have 1 registration encounter with MMI Insurance:", patientsWithMMIInsAndOneVisitIndicator);
		rd.addIndicator("5.1.4", "Number of patients who only have 1 registration encounter with MEDIPLAN Insurance:", patientsWithMEDIPLANInsAndOneVisitIndicator);
		rd.addIndicator("5.1.5", "Number of patients who only have 1 registration encounter with CORAR Insurance:", patientsWithCORARInsAndOneVisitIndicator);
		rd.addIndicator("5.1.6", "Number of patients who only have 1 registration encounter with NONE Insurance:", patientsWithNONEInsAndOneVisitIndicator);
		rd.addIndicator("5.1.7", "Number of patients who only have 1 registration encounter missing Insurance:", patientsWithMissingInsAndOneVisitIndicator);
		rd.addIndicator("5.2.1", "Number of patients who have 2 registration encounters with MUTUELLE Insurance:", patientsWithMUTUELLEInsAndTwoVisitsIndicator);
		rd.addIndicator("5.2.2", "Number of patients who have 2 registration encounters with RAMA Insurance:", patientsWithRAMAInsAndTwoVisitsIndicator);
		rd.addIndicator("5.2.3", "Number of patients who have 2 registration encounters with MMI Insurance:", patientsWithMMIInsAndTwoVisitsIndicator);
		rd.addIndicator("5.2.4", "Number of patients who have 2 registration encounters with MEDIPLAN Insurance:", patientsWithMEDIPLANInsAndTwoVisitsIndicator);
		rd.addIndicator("5.2.5", "Number of patients who have 2 registration encounters with CORAR Insurance:", patientsWithCORARInsAndTwoVisitsIndicator);
		rd.addIndicator("5.2.6", "Number of patients who have 2 registration encounters with NONE Insurance:", patientsWithNONEInsAndTwoVisitsIndicator);
		rd.addIndicator("5.2.7", "Number of patients who have 2 registration encounters missing Insurance:", patientsWithMissingInsAndTwoVisitsIndicator);
		rd.addIndicator("5.3.1", "Number of patients who have 3 registration encounters with MUTUELLE Insurance:", patientsWithMUTUELLEInsAndThreeVisitsIndicator);
		rd.addIndicator("5.3.2", "Number of patients who have 3 registration encounters with RAMA Insurance:", patientsWithRAMAInsAndThreeVisitsIndicator);
		rd.addIndicator("5.3.3", "Number of patients who have 3 registration encounters with MMI Insurance:", patientsWithMMIInsAndThreeVisitsIndicator);
		rd.addIndicator("5.3.4", "Number of patients who have 3 registration encounters with MEDIPLAN Insurance:", patientsWithMEDIPLANInsAndThreeVisitsIndicator);
		rd.addIndicator("5.3.5", "Number of patients who have 3 registration encounters with CORAR Insurance:", patientsWithCORARInsAndThreeVisitsIndicator);
		rd.addIndicator("5.3.6", "Number of patients who have 3 registration encounters with NONE Insurance:", patientsWithNONEInsAndThreeVisitsIndicator);
		rd.addIndicator("5.3.7", "Number of patients who have 3 registration encounters missing Insurance:", patientsWithMissingInsAndThreeVisitsIndicator);
		rd.addIndicator("5.4.1", "Number of patients With greater than 3 registration encounters with MUTUELLE Insurance:", patientsWithMUTUELLEInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator("5.4.2", "Number of patients With greater than 3 registration encounters with RAMA Insurance:", patientsWithRAMAInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator("5.4.3", "Number of patients With greater than 3 registration encounters with MMI Insurance:", patientsWithMMIInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator("5.4.4", "Number of patients With greater than 3 registration encounters with MEDIPLAN Insurance:", patientsWithMEDIPLANInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator("5.4.5", "Number of patients With greater than 3 registration encounters with CORAR Insurance:", patientsWithCORARInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator("5.4.6", "Number of patients With greater than 3 registration encounters with NONE Insurance:", patientsWithNONEInsAndGreaterThanThreeVisitsIndicator);
		rd.addIndicator("5.4.7", "Number of patients With greater than 3 registration encounters missing Insurance:", patientsWithMissingInsAndGreaterThanThreeVisitsIndicator);
		
		
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
		
		
		rd.addIndicator("7.1.f", "Female number of patient requested primary care", femalePatientsrequestPrimCareInRegistrationIndicator);
		rd.addIndicator("7.1.m", "Male number of patient requested primary care", malePatientsrequestPrimCareInRegistrationIndicator);
		rd.addIndicator("7.2.f", "Female Number of patients requested VCT PROGRAM", femalePatientsrequestVCTProgramInRegistrationIndicator);
		rd.addIndicator("7.2.m", "Male Number of patients requested VCT PROGRAM", malePatientsrequestVCTProgramInRegistrationIndicator);
		rd.addIndicator("7.3.f", "Female Number of patients requested ANTENATAL CLINIC", femalePatientsrequestAntenatalClinicInRegistrationIndicator);
		rd.addIndicator("7.3.m", "Male Number of patients requested ANTENATAL CLINIC", malePatientsrequestAntenatalClinicInRegistrationIndicator);
		rd.addIndicator("7.4.f", "Female Number of patients requested FAMILY PLANNING SERVICES", femalePatientsrequestFamilyPlaningServicesRegistrationIndicator);
		rd.addIndicator("7.4.m", "Male Number of patients requested FAMILY PLANNING SERVICES", malePatientsrequestFamilyPlaningServicesRegistrationIndicator);
		rd.addIndicator("7.5.f", "Female Number of patients requested MUTUELLE SERVICE", femalePatientsrequestMutuelleServiceRegistrationIndicator);
		rd.addIndicator("7.5.m", "Male Number of patients requested MUTUELLE SERVICE", malePatientsrequestMutuelleServiceRegistrationIndicator);
		rd.addIndicator("7.6.f", "Female Number of patients requested ACCOUNTING OFFICE SERVICE", femalePatientsrequestAccountingOfficeServiceRegistrationIndicator);
		rd.addIndicator("7.6.m", "Male Number of patients requested ACCOUNTING OFFICE SERVICE", malePatientsrequestAccountingOfficeServiceRegistrationIndicator);
		rd.addIndicator("7.7.f", "Female Number of patients requested INTEGRATED MANAGEMENT OF ADULT ILLNESS SERVICE", femalePatientsrequestAdultIllnessServiceIndicator);
		rd.addIndicator("7.7.m", "Male Number of patients requested INTEGRATED MANAGEMENT OF ADULT ILLNESS SERVICE", malePatientsrequestAdultIllnessServiceIndicator);
		rd.addIndicator("7.8.f", "Female Number of patients requested INTEGRATED MANAGEMENT OF CHILDHOOD ILLNESS", femalePatientsrequestChildIllnessServiceIndicator);
		rd.addIndicator("7.8.m", "Male Number of patients requested INTEGRATED MANAGEMENT OF CHILDHOOD ILLNESS", malePatientsrequestChildIllnessServiceIndicator);
		rd.addIndicator("7.9.f", "Female Number of patients requested INFECTIOUS DISEASES CLINIC SERVICE", femalePatientsrequestInfectiousDiseasesServiceIndicator);
		rd.addIndicator("7.9.m", "Male Number of patients requested INFECTIOUS DISEASES CLINIC SERVICE", malePatientsrequestInfectiousDiseasesServiceIndicator);
		rd.addIndicator("7.10.f", "Female Number of patients requested SOCIAL WORKER SERVICE", femalePatientsrequestSocialWorkerServiceIndicator);
		rd.addIndicator("7.10.m", "Male Number of patients requested SOCIAL WORKER SERVICE", malePatientsrequestSocialWorkerServiceIndicator);
		rd.addIndicator("7.11.f", "Female number of patient requested PREVENTION OF MOTHER TO CHILD TRANSMISSION SERVICE", femalePatientsrequestPMTCTServiceIndicator);
		rd.addIndicator("7.11.m", "Male number of patient requested PREVENTION OF MOTHER TO CHILD TRANSMISSION SERVICE", malePatientsrequestPMTCTServiceIndicator);
		rd.addIndicator("7.12.f", "Female Number of patients requested LABORATORY SERVICE", femalePatientsrequestLabServiceIndicator);
		rd.addIndicator("7.12.m", "Male Number of patients requested LABORATORY SERVICE", malePatientsrequestLabServiceIndicator);
		rd.addIndicator("7.13.f", "Female Number of patients requested PHARMACY SERVICES", femalePatientsrequestPharmacyServiceIndicator);
		rd.addIndicator("7.13.m", "Male Number of patients requested PHARMACY SERVICES", malePatientsrequestPharmacyServiceIndicator);
		rd.addIndicator("7.14.f", "Female Number of patients requested MATERNITY SERVICE", femalePatientsrequestMaternityServiceIndicator);
		rd.addIndicator("7.14.m", "Male Number of patients requested MATERNITY SERVICE", malePatientsrequestMaternityServiceIndicator);
		rd.addIndicator("7.15.f", "Female Number of patients requested HOSPITALIZATION SERVICE", femalePatientsrequestHospitalizationServiceIndicator);
		rd.addIndicator("7.15.m", "Male Number of patients requested HOSPITALIZATION SERVICE", malePatientsrequestHospitalizationServiceIndicator);
		rd.addIndicator("7.16.f", "Female Number of patients requested VACCINATION SERVICE", femalePatientsrequestVaccinationServiceIndicator);
		rd.addIndicator("7.16.m", "Male Number of patients requested VACCINATION SERVICE", malePatientsrequestVaccinationServiceIndicator);
		
		rd.setBaseCohortDefinition(h.cohortDefinition("location: Primary Care Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
	    h.replaceReportDefinition(rd);
		
		return rd;
	}
	
		
	private void createLocationCohortDefinitions(EncounterType reg) {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		//    select p.patient_id from encounter where encounter_type_id = 8 and voided = 0 and location = :location
		//location.setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
		location.setQuery("select distinct p.patient_id from encounter e, patient p where p.voided = 0 and p.patient_id = e.patient_id and e.encounter_type = " +  reg.getEncounterTypeId() + " and e.voided = 0 and e.location_id = :location ");
		location.setName("location: Primary Care Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
		}		

	private CodedObsCohortDefinition makeCodedObsCohortDefinition(String question, String value, SetComparator setComparator, TimeModifier timeModifier) {		
		CodedObsCohortDefinition obsCohortDefinition = new CodedObsCohortDefinition();
		if (question != null) obsCohortDefinition.setQuestion(Context.getConceptService().getConceptByUuid(question));
		if (setComparator != null) obsCohortDefinition.setOperator(setComparator);
		if (timeModifier != null) obsCohortDefinition.setTimeModifier(timeModifier);
		Concept valueCoded = Context.getConceptService().getConceptByUuid(value);
		List<Concept> valueList = new ArrayList<Concept>();
		if (valueCoded != null) {
			valueList.add(valueCoded);
			obsCohortDefinition.setValueList(valueList);
		}		
		return obsCohortDefinition;
	}
}
