package org.openmrs.module.rwandareports.reporting;

//@author: NEZA Guillaine
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.rwandareports.renderer.TracNetCustomRenderer;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.Indicators;

public class SetupTracNetRwandaReportBySite {

	protected final static Log log = LogFactory
			.getLog(SetupTracNetRwandaReportBySite.class);

	Helper h = new Helper();

	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	private Program adulthivProgram;
	private Program pediatrichivProgram;
	private Program pmtctpregnantProgram;
	private Program pmtctcombinedInfant;
	private Program pmtctcombinedMother;
	private List<String> onOrAfterOnOrBeforeParamterNames = new ArrayList<String>();
	private Concept patientstartedarvforprophylaxis;
	private Concept patientonbloodorbloodexposure;
	private Concept sexualAssault;
	private Concept sexualContactWithPospartner;
	private Concept hivTestDone;
	private Concept positiveStatus;
	private Concept negativeStatus;
	private Concept undeterminateStatus;
	private Concept pregnancystatusConcept;
	private Concept yesStatusToPregnant;
	private Concept programthatOrderedtest;
	private Concept maternityward;
	private Concept birthlocationtype;
	private Concept athospital;
	private Concept athealthcenter;
	private Concept athome;
	private Concept infantFeedingMethod;
	private Concept breastFeedExclusively;
	private Concept usingFormula;
	private List<EncounterType> clinicalEncoutersExcLab;
	private Concept tbScreeningtest;
	private Concept reasonForExitingCare;
	private Concept patientsDied;
	public Concept transferIn;
	public Concept transferOut;
	public Concept whostage;
	public Concept whostage4p;
	public Concept whostage3p;
	public Concept whostage2p;
	public Concept whostage1p;
	public Concept whostageinconue;
	public Concept whostage4adlt;
	public Concept whostage3adlt;
	public Concept whostage2adlt;
	public Concept whostage1adlt;
	private ProgramWorkflowState folowingState;
	private ProgramWorkflowState pregnatWithPregnancyStatus;
	private ProgramWorkflowState pregnantOnArtstate;
	private ProgramWorkflowState gaveBirthinpmtctstate;
	private Concept opportunisticInfection;
	private Concept stiScreenedTb;
	private Concept reasonTherapeuticFailed;
	private Concept pooradherence;
	private Concept notestDone;
	private Concept rprTest;
	private Concept reactiveAnswer;
	private Concept familyplaning;
	private Concept condoms;
	private Concept injectable;
	private Concept orals;
	private Concept disposition;
	private Concept referedforFp;

	public void setup() throws Exception {

		setUpProperties();
		ReportDefinition rd = createReportDefinition();
		h.createXlsCalendarOverview(rd,"Mohrwandatracnetreporttemplate.xls","Xlstracnetreporttemplate", null);
		createCustomWebRenderer(rd, "TracNetWebRenderer");
	}

	private void createCustomWebRenderer(ReportDefinition rd, String name)
			throws IOException {
		final ReportDesign design = new ReportDesign();
		design.setName(name);
		design.setReportDefinition(rd);
		design.setRendererType(TracNetCustomRenderer.class);

		ReportService rs = Context.getService(ReportService.class);
		rs.saveReportDesign(design);

	}

	public void delete() {
		ReportService rs = Context.getService(ReportService.class);

		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Xlstracnetreporttemplate".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
			if ("TracNetWebRenderer".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("TracNet Report");
	}

	private ReportDefinition createReportDefinition() {

		// Tracnet Report
		PeriodIndicatorReportDefinition rd = new PeriodIndicatorReportDefinition();
		rd.removeParameter(ReportingConstants.START_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.END_DATE_PARAMETER);
		rd.removeParameter(ReportingConstants.LOCATION_PARAMETER);
		rd.addParameter(new Parameter("location", "Location", Location.class));
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		rd.setName("TracNet Report");
		rd.setupDataSetDefinition();

		// -----------------------------------------------------------------------------------------------------------------------------------------------
		// BASES DEFINITIONS TO USE EVERYWHERE
		// -----------------------------------------------------------------------------------------------------------------------------------------------
		// Gender Cohort definitions
		GenderCohortDefinition female = new GenderCohortDefinition();
		female.setName("female");
		female.setFemaleIncluded(true);

		GenderCohortDefinition male = new GenderCohortDefinition();
		male.setName("male");
		male.setMaleIncluded(true);
		// Programs
		EncounterCohortDefinition patientsWithAnyEncounterCohort = new EncounterCohortDefinition();
		patientsWithAnyEncounterCohort
				.setName("patientsWithAnyEncounterCohort");
		patientsWithAnyEncounterCohort.setEncounterTypeList(Context
				.getEncounterService().getAllEncounterTypes());

		InProgramCohortDefinition adultsHIVProgram = Cohorts
				.createInProgramParameterizableByDate("adultsHIVProgram",
						adulthivProgram);
		adultsHIVProgram.addParameter(new Parameter("onDate", "onDate",
				Date.class));

		InProgramCohortDefinition pediatricsHIVProgram = Cohorts
				.createInProgramParameterizableByDate("pediatricsHIVProgram",
						pediatrichivProgram);
		pediatricsHIVProgram.addParameter(new Parameter("onDate", "onDate",
				Date.class));

		InProgramCohortDefinition inPMTCTpregnantProgram = Cohorts
				.createInProgramParameterizableByDate("inPMTCTpregnantProgram",
						pmtctpregnantProgram);
		inPMTCTpregnantProgram.addParameter(new Parameter("onDate", "onDate",
				Date.class));

		InProgramCohortDefinition PMTCTCombinedInfant = Cohorts
				.createInProgramParameterizableByDate("PMTCTCombinedInfant",
						pmtctcombinedInfant);
		PMTCTCombinedInfant.addParameter(new Parameter("onDate", "onDate",
				Date.class));

		InProgramCohortDefinition PMTCTCombinedMother = Cohorts
				.createInProgramParameterizableByDate("PMTCTCombinedMother",
						pmtctcombinedMother);
		PMTCTCombinedMother.addParameter(new Parameter("onDate", "onDate",
				Date.class));

		// DRUGS
		// Number of patient on ARV drugs
		String patientOnARTDrugs = Context.getAdministrationService()
				.getGlobalProperty("moh.tracnetreport.arvDrugs");
		SqlCohortDefinition onArvsAsOfDate = new SqlCohortDefinition();
		onArvsAsOfDate.setName("onArvsAsOfDate");
		onArvsAsOfDate
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.uuid in ("
						+ patientOnARTDrugs
						+ ") and o.discontinued=0 and o.voided=0 AND o.start_date<= :sqlEnd");
		onArvsAsOfDate.addParameter(new Parameter("sqlEnd", "sqlEnd",
				Date.class));

		SqlCohortDefinition onArvsAsOfDateDuringP = new SqlCohortDefinition();
		onArvsAsOfDateDuringP.setName("onArvsAsOfDateDuringP");
		onArvsAsOfDateDuringP
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.uuid in ("
						+ patientOnARTDrugs
						+ ") and o.discontinued=0 and o.voided=0 and o.start_date>= :sqlStart and o.start_date<= :sqlEnd");
		onArvsAsOfDateDuringP.addParameter(new Parameter("sqlStart",
				"sqlStart", Date.class));
		onArvsAsOfDateDuringP.addParameter(new Parameter("sqlEnd", "sqlEnd",
				Date.class));

		// TB Drugs
		String patientOnTBDrugs = Context.getAdministrationService()
				.getGlobalProperty("tracnet.listOfTBDrugs");
		SqlCohortDefinition patientOnTBTreatment = new SqlCohortDefinition();
		patientOnTBTreatment.setName("patientOnTBTreatment");
		patientOnTBTreatment
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.concept_id in ("
						+ patientOnTBDrugs
						+ ") and o.discontinued=0 and o.voided=0 AND o.start_date>= :tbStart AND o.start_date<= :tbEnd");
		patientOnTBTreatment.addParameter(new Parameter("tbStart", "tbStart",
				Date.class));
		patientOnTBTreatment.addParameter(new Parameter("tbEnd", "tbEnd",
				Date.class));

		// Number of patient on First Line Regimen
		String firstLineRegimen = Context.getAdministrationService()
				.getGlobalProperty("tracnetreport.firstLineRegimen");
		SqlCohortDefinition patientOnFirstLineRegimen = new SqlCohortDefinition();
		patientOnFirstLineRegimen.setName("patientOnFirstLineRegimen");
		patientOnFirstLineRegimen
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.uuid in ("
						+ firstLineRegimen
						+ ") and o.discontinued=0 and o.voided=0 ");
		patientOnFirstLineRegimen.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientOnFirstLineRegimen.addParameter(new Parameter("endDate",
				"endDate", Date.class));

		// Number of patient on Second Line Regimen
		String secondLineRegimen = Context.getAdministrationService()
				.getGlobalProperty("tracnetreport.secondLineRegimen");
		SqlCohortDefinition patientOnSecondLineRegimen = new SqlCohortDefinition();
		patientOnSecondLineRegimen.setName("patientOnSecondLineRegimen");
		patientOnSecondLineRegimen
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.uuid in ("
						+ secondLineRegimen
						+ ") and o.discontinued=0 and o.voided=0 ");
		patientOnSecondLineRegimen.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientOnSecondLineRegimen.addParameter(new Parameter("endDate",
				"endDate", Date.class));

		// Number of patients on prophylaxis drugs
		String prophylaxisDrugs = Context.getAdministrationService()
				.getGlobalProperty("moh.tracnetreport.prophylaxisConceptsIds");
		SqlCohortDefinition startedProphylaxisDrugs = new SqlCohortDefinition();
		startedProphylaxisDrugs.setName("patientOnProphylaxisDrugs");
		startedProphylaxisDrugs
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.uuid in ("
						+ prophylaxisDrugs
						+ ") and o.discontinued=0 and o.voided=0 AND o.start_date>= :proStart AND o.start_date<= :proEnd");
		startedProphylaxisDrugs.addParameter(new Parameter("proStart",
				"proStart", Date.class));
		startedProphylaxisDrugs.addParameter(new Parameter("proEnd", "proEnd",
				Date.class));

		SqlCohortDefinition prophylaxisDrugsAsofDate = new SqlCohortDefinition();
		prophylaxisDrugsAsofDate.setName("prophylaxisDrugsAsofDate");
		prophylaxisDrugsAsofDate
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.uuid in ("
						+ prophylaxisDrugs
						+ ") and o.discontinued=0 and o.voided=0 AND o.start_date<= :proDrugEnd");
		prophylaxisDrugsAsofDate.addParameter(new Parameter("proDrugEnd",
				"proDrugEnd", Date.class));

		// On Nevirapine
		String onNvp = Context.getAdministrationService().getGlobalProperty(
				"tracnet.nevirapine");
		SqlCohortDefinition onAztOnlyOrNevirapine = new SqlCohortDefinition();
		onAztOnlyOrNevirapine.setName("onAztOnlyOrNevirapine");
		onAztOnlyOrNevirapine
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.uuid in ("
						+ onNvp
						+ ") and o.discontinued=0 and o.voided=0 AND o.start_date<= :nvpEnd");
		onAztOnlyOrNevirapine.addParameter(new Parameter("nvpEnd", "nvpEnd",
				Date.class));

		// triple therapy drug
		String nvpazt3tcdrug = Context.getAdministrationService()
				.getGlobalProperty("tracnet.azt3TC");
		SqlCohortDefinition ontriplethrapy = new SqlCohortDefinition();
		ontriplethrapy.setName("ontriplethrapy");
		ontriplethrapy
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.uuid in ("
						+ nvpazt3tcdrug
						+ ") and o.discontinued=0 and o.voided=0 AND o.start_date >=:nvpStart AND o.start_date<= :nvpEnd ");
		ontriplethrapy.addParameter(new Parameter("nvpStart", "nvpStart",
				Date.class));
		ontriplethrapy.addParameter(new Parameter("nvpEnd", "nvpEnd",
				Date.class));

		// bitherapy drug
		String bitherapyDrug = Context.getAdministrationService()
				.getGlobalProperty("tracnet.bitherapy");
		SqlCohortDefinition bitherapy = new SqlCohortDefinition();
		bitherapy.setName("bitherapy");
		bitherapy
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.uuid in ("
						+ bitherapyDrug
						+ ") and o.discontinued=0 and o.voided=0 AND o.start_date >=:biStart AND o.start_date<= :biEnd ");
		bitherapy.addParameter(new Parameter("biStart", "biStart", Date.class));
		bitherapy.addParameter(new Parameter("biEnd", "biEnd", Date.class));

		// on cotrimo
		String onCotrimo = Context.getAdministrationService()
				.getGlobalProperty("tracnet.cotrimoxazole");
		SqlCohortDefinition startedCotrimoXazole = new SqlCohortDefinition();
		startedCotrimoXazole.setName("startedCotrimoXazole");
		startedCotrimoXazole
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.uuid in ("
						+ onCotrimo
						+ ") and o.discontinued=0 AND o.voided=0 AND o.start_date<= :coEnd ");
		startedCotrimoXazole.addParameter(new Parameter("coEnd", "coEnd",
				Date.class));

		// OnCotrimoDuringP
		SqlCohortDefinition startedCotrimoXazoleDuringP = new SqlCohortDefinition();
		startedCotrimoXazoleDuringP.setName("startedCotrimoXazoleDuringP");
		startedCotrimoXazoleDuringP
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.uuid in ("
						+ onCotrimo
						+ ") and o.discontinued=0 and o.voided=0 AND o.start_date >=:coStart AND o.start_date<= :coEnd ");
		startedCotrimoXazoleDuringP.addParameter(new Parameter("coStart",
				"coStart", Date.class));
		startedCotrimoXazoleDuringP.addParameter(new Parameter("coEnd",
				"coEnd", Date.class));

		// Ages Cohort Definitions
		AgeCohortDefinition at6WeeksOfAge = new AgeCohortDefinition();
		at6WeeksOfAge.setName("at6WeeksOfAge");
		at6WeeksOfAge.setMinAge(1);
		at6WeeksOfAge.setMinAgeUnit(DurationUnit.WEEKS);
		at6WeeksOfAge.setMaxAge(6);
		at6WeeksOfAge.setMaxAgeUnit(DurationUnit.WEEKS);
		at6WeeksOfAge.addParameter(new Parameter("effectiveDate",
				"effectiveDate", Date.class));

		AgeCohortDefinition at9monthsOfAge = new AgeCohortDefinition();
		at9monthsOfAge.setName("at9monthsOfAge");
		at9monthsOfAge.setMinAge(7);
		at9monthsOfAge.setMinAgeUnit(DurationUnit.WEEKS);
		at9monthsOfAge.setMaxAge(9);
		at9monthsOfAge.setMaxAgeUnit(DurationUnit.MONTHS);
		at9monthsOfAge.addParameter(new Parameter("effectiveDate",
				"effectiveDate", Date.class));

		AgeCohortDefinition at18monthsOfAge = new AgeCohortDefinition();
		at18monthsOfAge.setName("at18monthsOfAge");
		at18monthsOfAge.setMinAge(10);
		at18monthsOfAge.setMinAgeUnit(DurationUnit.MONTHS);
		at18monthsOfAge.setMaxAge(18);
		at18monthsOfAge.setMaxAgeUnit(DurationUnit.MONTHS);
		at18monthsOfAge.addParameter(new Parameter("effectiveDate",
				"effectiveDate", Date.class));

		AgeCohortDefinition lessThan5 = new AgeCohortDefinition(null, 4, null);
		AgeCohortDefinition underFifteen = new AgeCohortDefinition(null, 14,
				null);
		AgeCohortDefinition patientMoreThan15 = new AgeCohortDefinition(15,
				null, null);
		AgeCohortDefinition age15To49 = new AgeCohortDefinition(15, 49, null);

		// -----USED IN MANY PLACES
		SqlCohortDefinition hivTestWithAnyResult = new SqlCohortDefinition();
		hivTestWithAnyResult.setName("hivTestWithAnyResult");
		hivTestWithAnyResult
				.setQuery("select distinct person_id from obs where concept_id="
						+ GlobalPropertiesManagement.HIV_TEST_DONE
						+ " and voided=0 ");

		CodedObsCohortDefinition hivTestwithPosStatus = Cohorts
				.createCodedObsCohortDefinition("hivTestwithPosStatus",
						hivTestDone, positiveStatus, SetComparator.IN,
						TimeModifier.ANY);
		CodedObsCohortDefinition hivTestwithNegStatus = Cohorts
				.createCodedObsCohortDefinition("hivTestwithNegStatus",
						hivTestDone, negativeStatus, SetComparator.IN,
						TimeModifier.ANY);

		CodedObsCohortDefinition tbScreenngPosTest = Cohorts
				.createCodedObsCohortDefinition("tbScreenngPosTest",
						onOrAfterOnOrBeforeParamterNames, tbScreeningtest,
						positiveStatus, SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition tbTestResultDuringP = new CodedObsCohortDefinition();
		tbTestResultDuringP.setName("tbTestResultDuringP");
		tbTestResultDuringP.setTimeModifier(TimeModifier.LAST);
		tbTestResultDuringP.setQuestion(Context.getConceptService()
				.getConceptByName(
						"RESULT OF TUBERCULOSIS SCREENING, QUALITATIVE"));
		tbTestResultDuringP.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		tbTestResultDuringP.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));

		CodedObsCohortDefinition patientDied = Cohorts
				.createCodedObsCohortDefinition("patientDied",
						onOrAfterOnOrBeforeParamterNames, reasonForExitingCare,
						patientsDied, SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition patientsTransferredOut = Cohorts
				.createCodedObsCohortDefinition("patientsTransferredOut",
						onOrAfterOnOrBeforeParamterNames, reasonForExitingCare,
						transferOut, SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition patienttransferedIn = Cohorts
				.createCodedObsCohortDefinition("patienttransferedIn",
						onOrAfterOnOrBeforeParamterNames, transferIn,
						yesStatusToPregnant, SetComparator.IN,
						TimeModifier.LAST);

		CodedObsCohortDefinition pospregnancyStatus = Cohorts
				.createCodedObsCohortDefinition("pospregnancyStatus",
						onOrAfterOnOrBeforeParamterNames,
						pregnancystatusConcept, yesStatusToPregnant,
						SetComparator.IN, TimeModifier.LAST);
		CodedObsCohortDefinition testinmaternity = Cohorts
				.createCodedObsCohortDefinition("testinmaternity",
						programthatOrderedtest, maternityward,
						SetComparator.IN, TimeModifier.ANY);

		CodedObsCohortDefinition hivUndeterminateDuringperiod = Cohorts
				.createCodedObsCohortDefinition("hivUndeterminateDuringperiod",
						onOrAfterOnOrBeforeParamterNames, hivTestDone,
						undeterminateStatus, SetComparator.IN,
						TimeModifier.LAST);
		CodedObsCohortDefinition hivposthismonth = Cohorts
				.createCodedObsCohortDefinition("hivposthismonth",
						onOrAfterOnOrBeforeParamterNames, hivTestDone,
						positiveStatus, SetComparator.IN, TimeModifier.LAST);
		// WHO STAGES
		CodedObsCohortDefinition whostagefourped = Cohorts
				.createCodedObsCohortDefinition("whostagefourped",
						onOrAfterOnOrBeforeParamterNames, whostage, whostage4p,
						SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition whostagethreeped = Cohorts
				.createCodedObsCohortDefinition("whostagethreeped",
						onOrAfterOnOrBeforeParamterNames, whostage, whostage3p,
						SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition whostagetwoped = Cohorts
				.createCodedObsCohortDefinition("whostagetwoped",
						onOrAfterOnOrBeforeParamterNames, whostage, whostage2p,
						SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition whostageoneped = Cohorts
				.createCodedObsCohortDefinition("whostageoneped",
						onOrAfterOnOrBeforeParamterNames, whostage, whostage1p,
						SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition whostageundeterminate = Cohorts
				.createCodedObsCohortDefinition("whostageundeterminate",
						onOrAfterOnOrBeforeParamterNames, whostage,
						whostageinconue, SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition whostagefourad = Cohorts
				.createCodedObsCohortDefinition("whostagefourad",
						onOrAfterOnOrBeforeParamterNames, whostage,
						whostage4adlt, SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition whostagethreead = Cohorts
				.createCodedObsCohortDefinition("whostagethreead",
						onOrAfterOnOrBeforeParamterNames, whostage,
						whostage3adlt, SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition whostagetwoad = Cohorts
				.createCodedObsCohortDefinition("whostagetwoad",
						onOrAfterOnOrBeforeParamterNames, whostage,
						whostage2adlt, SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition whostageonead = Cohorts
				.createCodedObsCohortDefinition("whostageonead",
						onOrAfterOnOrBeforeParamterNames, whostage,
						whostage1adlt, SetComparator.IN, TimeModifier.LAST);

		SqlCohortDefinition hivTestWithThisMonth = new SqlCohortDefinition();
		hivTestWithThisMonth.setName("hivTestWithThisMonth");
		hivTestWithThisMonth
				.setQuery("select DISTINCT person_id from obs WHERE concept_id="
						+ GlobalPropertiesManagement.HIV_TEST_DONE
						+ " AND obs_datetime>= :startDate AND obs_datetime <= :endDate AND voided=0 ");
		hivTestWithThisMonth.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		hivTestWithThisMonth.addParameter(new Parameter("endDate", "endDate",
				Date.class));

		CodedObsCohortDefinition patientBreastfeedingThisMonth = Cohorts
				.createCodedObsCohortDefinition(
						"patientBreastfeedingThisMonth",
						onOrAfterOnOrBeforeParamterNames, infantFeedingMethod,
						breastFeedExclusively, SetComparator.IN,
						TimeModifier.LAST);

		CodedObsCohortDefinition givingbirthathospital = Cohorts
				.createCodedObsCohortDefinition("givingbirthathospital",
						birthlocationtype, athospital, SetComparator.IN,
						TimeModifier.ANY);
		CodedObsCohortDefinition givingbirthathealthcenter = Cohorts
				.createCodedObsCohortDefinition("givingbirthathealthcenter",
						birthlocationtype, athealthcenter, SetComparator.IN,
						TimeModifier.ANY);
		CodedObsCohortDefinition givingbirthathome = Cohorts
				.createCodedObsCohortDefinition("givingbirthathome",
						birthlocationtype, athome, SetComparator.IN,
						TimeModifier.ANY);

		CodedObsCohortDefinition exitedFromCare = new CodedObsCohortDefinition();
		exitedFromCare.setName("exitedFromCare");
		exitedFromCare.setTimeModifier(TimeModifier.ANY);
		exitedFromCare.setQuestion(Context.getConceptService()
				.getConceptByName("REASON FOR EXITING CARE"));

		CodedObsCohortDefinition patInFplanning = new CodedObsCohortDefinition();
		patInFplanning.setName("patInFplanning");
		patInFplanning.setTimeModifier(TimeModifier.LAST);
		patInFplanning.setQuestion(Context.getConceptService()
				.getConceptByName("METHOD OF FAMILY PLANNING"));
		patInFplanning.addParameter(new Parameter("onOrAfter", "onOrAfter",
				Date.class));
		patInFplanning.addParameter(new Parameter("onOrBefore", "onOrBefore",
				Date.class));

		EncounterCohortDefinition patientsWithClinicalEncountersWithoutLabTest = Cohorts
				.createEncounterParameterizedByDate(
						"patientsWithClinicalEncounters", "onOrAfter",
						clinicalEncoutersExcLab);

		CompositionCohortDefinition ltfDuringPeriodComposition = new CompositionCohortDefinition();
		ltfDuringPeriodComposition
				.setName("Patients who are lost to followup at end of period");
		ltfDuringPeriodComposition.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		ltfDuringPeriodComposition
				.addParameter(ReportingConstants.START_DATE_PARAMETER);
		ltfDuringPeriodComposition
				.getSearches()
				.put("patientsWithClinicalEncountersWithoutLabTest",
						new Mapped<CohortDefinition>(
								patientsWithClinicalEncountersWithoutLabTest,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate-12m}")));
		ltfDuringPeriodComposition
				.setCompositionString("NOT patientsWithClinicalEncountersWithoutLabTest");

		CodedObsCohortDefinition adherenceCounseling = Cohorts
				.createCodedObsCohortDefinition("adherenceCounseling",
						onOrAfterOnOrBeforeParamterNames,
						reasonTherapeuticFailed, pooradherence,
						SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition testForRprDuringP = Cohorts
				.createCodedObsCohortDefinition("testForRprDuringP",
						onOrAfterOnOrBeforeParamterNames, rprTest,
						reactiveAnswer, SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition testForRpr = new CodedObsCohortDefinition();
		testForRpr.setName("testForRpr");
		testForRpr.setTimeModifier(TimeModifier.LAST);
		testForRpr.setQuestion(Context.getConceptService().getConceptByName(
				"RAPID PLASMIN REAGENT"));
		testForRpr.addParameter(new Parameter("onOrAfter", "onOrAfter",
				Date.class));
		testForRpr.addParameter(new Parameter("onOrBefore", "onOrBefore",
				Date.class));

		// -----------------------------------------------------------------------------------------------------------------------------------------------
		// III PEP DATA ELEMENTS
		// -----------------------------------------------------------------------------------------------------------------------------------------------

		// 1 Number of new clients at risk of HIV infection as a result of
		// occupational exposure
		CodedObsCohortDefinition newAtRiskHivOccupationExposure = Cohorts
				.createCodedObsCohortDefinition(
						"newAtRiskHivOccupationExposure",
						onOrAfterOnOrBeforeParamterNames,
						patientstartedarvforprophylaxis,
						patientonbloodorbloodexposure, SetComparator.IN,
						TimeModifier.LAST);
		CodedObsCohortDefinition newAtRiskHivRapeAssault = Cohorts
				.createCodedObsCohortDefinition("newAtRiskHivRapeAssault",
						onOrAfterOnOrBeforeParamterNames,
						patientstartedarvforprophylaxis, sexualAssault,
						SetComparator.IN, TimeModifier.LAST);
		CodedObsCohortDefinition newAtRiskHivOtherNoneOccupationExposure = Cohorts
				.createCodedObsCohortDefinition(
						"newAtRiskHivOtherNoneOccupationExposure",
						onOrAfterOnOrBeforeParamterNames,
						patientstartedarvforprophylaxis,
						sexualContactWithPospartner, SetComparator.IN,
						TimeModifier.LAST);

		CohortIndicator newAtRiskHivOccupationExposureIndicator = Indicators
				.newCohortIndicator(
						"newAtRiskHivOccupationExposureIndicator",
						newAtRiskHivOccupationExposure,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		// 2 Number of new clients at risk of HIV infection as a result of
		// rape/sexual assault

		CohortIndicator newAtRiskHivRapeAssaultIndicator = Indicators
				.newCohortIndicator(
						"newAtRiskHivRapeAssaultIndicator",
						newAtRiskHivRapeAssault,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		// 3 Number of new clients at risk of HIV infection as a result of other
		// non-occupational exposure

		CohortIndicator newAtRiskHivOtherNoneOccupationExposureIndicator = Indicators
				.newCohortIndicator(
						"newAtRiskHivOtherNoneOccupationExposureIndicator",
						newAtRiskHivOtherNoneOccupationExposure,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		// 4 Number of new clients at risk of HIV infection as a result of
		// occupational exposure who received PEP
		CompositionCohortDefinition newAtRiskHivOccupationExposurePepComposition = new CompositionCohortDefinition();
		newAtRiskHivOccupationExposurePepComposition
				.setName("newAtRiskHivOccupationExposurePepComposition");
		newAtRiskHivOccupationExposurePepComposition
				.addParameter(new Parameter("onOrBefore", "onOrBefore",
						Date.class));
		newAtRiskHivOccupationExposurePepComposition
				.addParameter(new Parameter("proStart", "proStart", Date.class));
		newAtRiskHivOccupationExposurePepComposition
				.addParameter(new Parameter("proEnd", "proEnd", Date.class));
		newAtRiskHivOccupationExposurePepComposition
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								newAtRiskHivOccupationExposure,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore}")));
		newAtRiskHivOccupationExposurePepComposition
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								startedProphylaxisDrugs,
								ParameterizableUtil
										.createParameterMappings("proStart=${proStart},proEnd=${proEnd}")));
		newAtRiskHivOccupationExposurePepComposition
				.setCompositionString(" 1 AND 2 ");

		CohortIndicator newAtRiskHivOccupationExposurePepIndicator = Indicators
				.newCohortIndicator(
						"newAtRiskHivOccupationExposurePepIndicator",
						newAtRiskHivOccupationExposurePepComposition,
						ParameterizableUtil
								.createParameterMappings("proStart=${startDate},proEnd=${endDate},onOrBefore=${endDate}"));
		// 5 Number of new clients at risk of HIV infection as a result of
		// rape/sexual assualt who received PEP
		CompositionCohortDefinition sexualAssaultInPep = new CompositionCohortDefinition();
		sexualAssaultInPep.setName("sexualAssaultInPep");
		sexualAssaultInPep.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		sexualAssaultInPep.addParameter(new Parameter("proStart", "proStart",
				Date.class));
		sexualAssaultInPep.addParameter(new Parameter("proEnd", "proEnd",
				Date.class));
		sexualAssaultInPep
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								newAtRiskHivRapeAssault,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore}")));
		sexualAssaultInPep
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								startedProphylaxisDrugs,
								ParameterizableUtil
										.createParameterMappings("proStart=${proStart},proEnd=${proEnd}")));
		sexualAssaultInPep.setCompositionString("1 AND 2");

		CohortIndicator sexualAssaultInPepInd = Indicators
				.newCohortIndicator(
						"sexualAssaultInPepInd",
						sexualAssaultInPep,
						ParameterizableUtil
								.createParameterMappings("proStart=${startDate},proEnd=${endDate},onOrBefore=${endDate}"));

		// 6 Number of new clients at risk of HIV infection as a result of other
		// non-occupational exposure who received PEP

		CompositionCohortDefinition noneoCupExposuretInPep = new CompositionCohortDefinition();
		noneoCupExposuretInPep.setName("noneoCupExposuretInPep");
		noneoCupExposuretInPep.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		noneoCupExposuretInPep.addParameter(new Parameter("proStart",
				"proStart", Date.class));
		noneoCupExposuretInPep.addParameter(new Parameter("proEnd", "proEnd",
				Date.class));
		noneoCupExposuretInPep
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								newAtRiskHivOtherNoneOccupationExposure,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore}")));
		noneoCupExposuretInPep
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								startedProphylaxisDrugs,
								ParameterizableUtil
										.createParameterMappings("proStart=${proStart},proEnd=${proEnd}")));
		noneoCupExposuretInPep.setCompositionString("1 AND 2");

		CohortIndicator noneoCupExposuretInPepInd = Indicators
				.newCohortIndicator(
						"noneoCupExposuretInPepInd",
						noneoCupExposuretInPep,
						ParameterizableUtil
								.createParameterMappings("proStart=${startDate},proEnd=${endDate},onOrBefore=${endDate}"));

		// 7 Number of clients at risk of HIV infection as a result of
		// occupational exposure who were tested 3 months after receiving PEP

		CompositionCohortDefinition newAtRiskHivOccupExpo3MonthAfterPep = new CompositionCohortDefinition();
		newAtRiskHivOccupExpo3MonthAfterPep
				.setName("newAtRiskHivOccupExpo3MonthAfterPep");
		newAtRiskHivOccupExpo3MonthAfterPep.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		newAtRiskHivOccupExpo3MonthAfterPep.addParameter(new Parameter(
				"proDrugEnd", "proDrugEnd", Date.class));
		newAtRiskHivOccupExpo3MonthAfterPep
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								newAtRiskHivOccupationExposure,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore}")));
		newAtRiskHivOccupExpo3MonthAfterPep
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								prophylaxisDrugsAsofDate,
								ParameterizableUtil
										.createParameterMappings("proDrugEnd=${proDrugEnd}")));
		newAtRiskHivOccupExpo3MonthAfterPep.setCompositionString("1 AND 2 ");

		CohortIndicator newAtRiskHivOccupExpo3MonthAfterPepInd = Indicators
				.newCohortIndicator(
						"newAtRiskHivOccupExpo3MonthAfterPepInd",
						newAtRiskHivOccupExpo3MonthAfterPep,
						ParameterizableUtil
								.createParameterMappings("proDrugEnd=${startDate-3m},onOrBefore=${endDate}"));

		// 8 Number of new clients at risk of HIV infection as a result of
		// rape/sexual assault who were tested 3 months after receiving PEP

		CompositionCohortDefinition sexualasaultDrugsAfter3motnhs = new CompositionCohortDefinition();
		sexualasaultDrugsAfter3motnhs.setName("sexualasaultDrugsAfter3motnhs");
		sexualasaultDrugsAfter3motnhs.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		sexualasaultDrugsAfter3motnhs.addParameter(new Parameter("proDrugEnd",
				"proDrugEnd", Date.class));
		sexualasaultDrugsAfter3motnhs
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								newAtRiskHivRapeAssault,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore}")));
		sexualasaultDrugsAfter3motnhs
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								prophylaxisDrugsAsofDate,
								ParameterizableUtil
										.createParameterMappings("proDrugEnd=${proDrugEnd}")));
		sexualasaultDrugsAfter3motnhs.setCompositionString("1 AND 2 ");

		CohortIndicator sexualasaultDrugsAfter3motnhsInd = Indicators
				.newCohortIndicator(
						"sexualasaultDrugsAfter3motnhsInd",
						sexualasaultDrugsAfter3motnhs,
						ParameterizableUtil
								.createParameterMappings("proDrugEnd=${startDate-3m},onOrBefore=${endDate}"));

		// 9 Number of new clients at risk of HIV infection as a result of other
		// non-occupational exposure who were tested 3 months after receiving
		// PEP

		CompositionCohortDefinition onthernoneOccuExpDrugsAfter3motnhs = new CompositionCohortDefinition();
		onthernoneOccuExpDrugsAfter3motnhs
				.setName("onthernoneOccuExpDrugsAfter3motnhs");
		onthernoneOccuExpDrugsAfter3motnhs.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		onthernoneOccuExpDrugsAfter3motnhs.addParameter(new Parameter(
				"proDrugEnd", "proDrugEnd", Date.class));
		onthernoneOccuExpDrugsAfter3motnhs
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								newAtRiskHivOtherNoneOccupationExposure,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore}")));
		onthernoneOccuExpDrugsAfter3motnhs
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								prophylaxisDrugsAsofDate,
								ParameterizableUtil
										.createParameterMappings("proDrugEnd=${proDrugEnd}")));
		onthernoneOccuExpDrugsAfter3motnhs.setCompositionString("1 AND 2 ");

		CohortIndicator onthernoneOccuExpDrugsAfter3motnhsInd = Indicators
				.newCohortIndicator(
						"onthernoneOccuExpDrugsAfter3motnhsInd",
						onthernoneOccuExpDrugsAfter3motnhs,
						ParameterizableUtil
								.createParameterMappings("proDrugEnd=${startDate-3m},onOrBefore=${endDate}"));

		// -----------------------------------------------------------------------------------------------------------------------------------------------
		// PMTCT ANTENATAL DATA ELEMENTS
		// -----------------------------------------------------------------------------------------------------------------------------------------------

		// 1 Number of women with unknown HIV status presenting for first
		// antenatal care consultation

		InStateCohortDefinition womenOnpregnantStatus = Cohorts
				.createInProgramStateParameterizableByDate(
						"womenOnpregnantStatus", pregnatWithPregnancyStatus);
		CodedObsCohortDefinition patientWithNoTest = Cohorts
				.createCodedObsCohortDefinition("patientWithNoTest",
						hivTestDone, notestDone, SetComparator.IN,
						TimeModifier.ANY);

		ProgramEnrollmentCohortDefinition PMTCTpregnantProgramDuringP = Cohorts
				.createProgramEnrollmentParameterizedByStartEndDate(
						"PMTCTpregnantProgramDuringP", pmtctpregnantProgram);
		PMTCTpregnantProgramDuringP.addParameter(new Parameter(
				"enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		PMTCTpregnantProgramDuringP.addParameter(new Parameter(
				"enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));

		CompositionCohortDefinition pregnantWomenwithConsultation = new CompositionCohortDefinition();
		pregnantWomenwithConsultation.setName("pregnantWomenwithConsultation");
		pregnantWomenwithConsultation.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		pregnantWomenwithConsultation.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		pregnantWomenwithConsultation.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		pregnantWomenwithConsultation.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		pregnantWomenwithConsultation.addParameter(new Parameter(
				"enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		pregnantWomenwithConsultation.addParameter(new Parameter(
				"enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		pregnantWomenwithConsultation.getSearches().put("1",
				new Mapped<CohortDefinition>(womenOnpregnantStatus, null));
		pregnantWomenwithConsultation
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								pospregnancyStatus,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		pregnantWomenwithConsultation
				.getSearches()
				.put("3",
						new Mapped<CohortDefinition>(
								PMTCTpregnantProgramDuringP,
								ParameterizableUtil
										.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}")));
		pregnantWomenwithConsultation.setCompositionString("1 AND 2 AND 3");

		CompositionCohortDefinition pregnatComeForFirstTimeWIthnegandNotTestStatus = new CompositionCohortDefinition();
		pregnatComeForFirstTimeWIthnegandNotTestStatus
				.setName("pregnatComeForFirstTimeWIthnegandNotTestStatus");
		pregnatComeForFirstTimeWIthnegandNotTestStatus.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(pregnantWomenwithConsultation,
						null));
		pregnatComeForFirstTimeWIthnegandNotTestStatus.getSearches().put("2",
				new Mapped<CohortDefinition>(patientWithNoTest, null));
		pregnatComeForFirstTimeWIthnegandNotTestStatus.getSearches().put("3",
				new Mapped<CohortDefinition>(hivTestwithNegStatus, null));
		pregnatComeForFirstTimeWIthnegandNotTestStatus
				.setCompositionString("1 AND (2 OR 3)");

		CohortIndicator pregnantWomenwithConsultationIndicator = Indicators
				.newCohortIndicator("pregnantWomenwithConsultationIndicator",
						pregnatComeForFirstTimeWIthnegandNotTestStatus, null);

		// 2 Number of known HIV positive women presenting for first antenatal
		// care consultation

		CohortIndicator pregnantHivWomenwithConsultationInd = Indicators
				.newCohortIndicator("pregnantHivWomenwithConsultationInd",
						pregnantWomenwithConsultation, null);

		// 3 Number of women with unknown HIV status tested for HIV
		// 8 Number of HIV positive pregnant women same just apply dimensions

		CompositionCohortDefinition pregnantWomenUpToDate = new CompositionCohortDefinition();
		pregnantWomenUpToDate.setName("pregnantWomenUpToDate");
		pregnantWomenUpToDate.addParameter(new Parameter("endDate", "endDate",
				Date.class));
		pregnantWomenUpToDate.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		pregnantWomenUpToDate.addParameter(new Parameter("onDate", "onDate",
				Date.class));
		pregnantWomenUpToDate.getSearches().put("1",
				new Mapped<CohortDefinition>(womenOnpregnantStatus, null));
		pregnantWomenUpToDate
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								pospregnancyStatus,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${endDate}")));
		pregnantWomenUpToDate.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(inPMTCTpregnantProgram,
						ParameterizableUtil
								.createParameterMappings("onDate=${endDate}")));
		pregnantWomenUpToDate.setCompositionString(" 1 AND 2 AND 3 ");

		CohortIndicator pregnantWomenUpToDateInd = Indicators
				.newCohortIndicator("pregnantWomenUpToDateInd",
						pregnantWomenUpToDate, null);

		// 4 Number of HIV positive women returning for their results
		// 7 Number of HIV negative women returning for their results

		SqlCohortDefinition patientsReturnedFortheirResults = new SqlCohortDefinition();
		patientsReturnedFortheirResults
				.setName("patientsReturnedFortheirResults");
		patientsReturnedFortheirResults
				.setQuery("select o.person_id from obs o,concept c where o.concept_id=c.concept_id and c.concept_id='"
						+ Context.getAdministrationService().getGlobalProperty(
								"report.dateResultOfHIVTestReceived")
						+ "' and o.voided=0 and o.value_datetime>= :dateOnOrAfter and o.value_datetime<= :dateOnOrBefore");
		patientsReturnedFortheirResults.addParameter(new Parameter(
				"dateOnOrAfter", "dateOnOrAfter", Date.class));
		patientsReturnedFortheirResults.addParameter(new Parameter(
				"dateOnOrBefore", "dateOnOrBefore", Date.class));

		CompositionCohortDefinition womenHivPosReturnRes = new CompositionCohortDefinition();
		womenHivPosReturnRes.setName("womenHivPosReturnRes");
		womenHivPosReturnRes.addParameter(new Parameter("dateOnOrAfter",
				"dateOnOrAfter", Date.class));
		womenHivPosReturnRes.addParameter(new Parameter("dateOnOrBefore",
				"dateOnOrBefore", Date.class));
		womenHivPosReturnRes.getSearches().put("1",
				new Mapped<CohortDefinition>(pregnantWomenUpToDate, null));
		womenHivPosReturnRes
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								patientsReturnedFortheirResults,
								ParameterizableUtil
										.createParameterMappings("dateOnOrAfter=${dateOnOrAfter},dateOnOrBefore=${dateOnOrBefore}")));
		womenHivPosReturnRes.setCompositionString(" 1 AND 2");

		CohortIndicator womenHivPosReturnResIndicator = Indicators
				.newCohortIndicator(
						"womenHivPosReturnResIndicator",
						womenHivPosReturnRes,
						ParameterizableUtil
								.createParameterMappings("dateOnOrAfter=${startDate},dateOnOrBefore=${endDate}"));

		// 5 Number of HIV positive women tested for CD4

		SqlCohortDefinition patientWithCD4CountDuringP = new SqlCohortDefinition();
		patientWithCD4CountDuringP.setName("patientWithCD4CountDuringP");
		patientWithCD4CountDuringP
				.setQuery("select o.person_id from obs o,concept c where o.concept_id=c.concept_id and c.concept_id=5497 and o.obs_datetime>= :cd4OnOrAfter and o.obs_datetime<= :cd4OnOrBefore");
		patientWithCD4CountDuringP.addParameter(new Parameter("cd4OnOrAfter",
				"cd4OnOrAfter", Date.class));
		patientWithCD4CountDuringP.addParameter(new Parameter("cd4OnOrBefore",
				"cd4OnOrBefore", Date.class));

		SqlCohortDefinition patientWithCD4CountEver = new SqlCohortDefinition();
		patientWithCD4CountEver.setName("patientWithCD4CountEver");
		patientWithCD4CountEver
				.setQuery("select o.person_id from obs o,concept c where o.concept_id=c.concept_id and c.concept_id=5497 and o.voided=0 ");

		CompositionCohortDefinition womenWithHivPosAndCD4CountTest = new CompositionCohortDefinition();
		womenWithHivPosAndCD4CountTest
				.setName("womenWithHivPosAndCD4CountTest");
		womenWithHivPosAndCD4CountTest.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		womenWithHivPosAndCD4CountTest.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		womenWithHivPosAndCD4CountTest.addParameter(new Parameter(
				"cd4OnOrAfter", "cd4OnOrAfter", Date.class));
		womenWithHivPosAndCD4CountTest.addParameter(new Parameter(
				"cd4OnOrBefore", "cd4OnOrBefore", Date.class));
		womenWithHivPosAndCD4CountTest.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		womenWithHivPosAndCD4CountTest.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		womenWithHivPosAndCD4CountTest.getSearches().put("1",
				new Mapped<CohortDefinition>(pregnantWomenUpToDate, null));
		womenWithHivPosAndCD4CountTest
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								patientWithCD4CountDuringP,
								ParameterizableUtil
										.createParameterMappings("cd4OnOrAfter=${startDate},cd4OnOrBefore=${endDate}")));
		womenWithHivPosAndCD4CountTest.getSearches().put("3",
				new Mapped<CohortDefinition>(hivTestwithPosStatus, null));
		womenWithHivPosAndCD4CountTest.getSearches().put("4",
				new Mapped<CohortDefinition>(patientWithCD4CountEver, null));
		womenWithHivPosAndCD4CountTest
				.getSearches()
				.put("5",
						new Mapped<CohortDefinition>(
								hivposthismonth,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		womenWithHivPosAndCD4CountTest
				.setCompositionString("1 AND ((2 AND 3) OR (4 AND 5)) ");

		CohortIndicator womenWithHivPosAndCD4CountTestIndicator = Indicators
				.newCohortIndicator("womenWithHivPosAndCD4CountTestIndicator",
						womenWithHivPosAndCD4CountTest, null);

		// 6 Number of HIV positive pregnant women eligible for ARVs treatment

		InStateCohortDefinition womenPregnantOnArtState = Cohorts
				.createInProgramStateParameterizableByDate(
						"womenPregnantOnArtState", pregnantOnArtstate);

		NumericObsCohortDefinition patientWithCd4CountLessThan500 = new NumericObsCohortDefinition();
		patientWithCd4CountLessThan500
				.setName("patientWithCd4CountLessThan500");
		patientWithCd4CountLessThan500.setQuestion(Context.getConceptService()
				.getConceptByName("CD4 COUNT"));
		patientWithCd4CountLessThan500.setTimeModifier(TimeModifier.ANY);
		patientWithCd4CountLessThan500.setOperator1(RangeComparator.LESS_THAN);
		patientWithCd4CountLessThan500.setValue1(500.0);

		CompositionCohortDefinition pregnantWomenEligibleonArtForLife = new CompositionCohortDefinition();
		pregnantWomenEligibleonArtForLife
				.setName("pregnantWomenEligibleonArtForLife");
		pregnantWomenEligibleonArtForLife.getSearches().put("1",
				new Mapped<CohortDefinition>(pregnantWomenUpToDate, null));
		pregnantWomenEligibleonArtForLife.getSearches().put("2",
				new Mapped<CohortDefinition>(womenPregnantOnArtState, null));
		pregnantWomenEligibleonArtForLife.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(patientWithCd4CountLessThan500,
						null));
		pregnantWomenEligibleonArtForLife.getSearches().put("4",
				new Mapped<CohortDefinition>(whostagefourad, null));
		pregnantWomenEligibleonArtForLife.getSearches().put("5",
				new Mapped<CohortDefinition>(whostagethreead, null));
		pregnantWomenEligibleonArtForLife
				.setCompositionString("1 AND 2 AND (3 OR (4 OR 5) )");

		CohortIndicator pregnantWomenEligibleonArtForLifeIndicator = Indicators
				.newCohortIndicator(
						"pregnantWomenEligibleonArtForLifeIndicator",
						pregnantWomenEligibleonArtForLife, null);

		// 9 Number of HIV positive pregnant women given AZT as prophylaxis at
		// 28 weeks

		CompositionCohortDefinition pregnatOnCotrimoAt28Weeks = new CompositionCohortDefinition();
		pregnatOnCotrimoAt28Weeks.setName("pregnatOnCotrimoAt28Weeks");
		pregnatOnCotrimoAt28Weeks.addParameter(new Parameter("coEnd", "coEnd",
				Date.class));
		pregnatOnCotrimoAt28Weeks.getSearches().put("1",
				new Mapped<CohortDefinition>(pregnantWomenUpToDate, null));
		pregnatOnCotrimoAt28Weeks.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(startedCotrimoXazole,
						ParameterizableUtil
								.createParameterMappings("coEnd=${coEnd}")));
		pregnatOnCotrimoAt28Weeks.setCompositionString("1 AND 2");

		CohortIndicator pregnatOnCotrimoAt28WeeksIndicator = Indicators
				.newCohortIndicator("pregnatOnCotrimoAt28WeeksIndicator",
						pregnatOnCotrimoAt28Weeks, ParameterizableUtil
								.createParameterMappings("coEnd=${endDate}"));

		// 10 Number of HIV positive pregnant women given triple therapy as
		// prophylaxis

		NumericObsCohortDefinition patientWithCd4CountMoreThan500 = new NumericObsCohortDefinition();
		patientWithCd4CountMoreThan500
				.setName("patientWithCd4CountMoreThan500");
		patientWithCd4CountMoreThan500.setQuestion(Context.getConceptService()
				.getConceptByName("CD4 COUNT"));
		patientWithCd4CountMoreThan500.setTimeModifier(TimeModifier.ANY);
		patientWithCd4CountMoreThan500
				.setOperator1(RangeComparator.GREATER_THAN);
		patientWithCd4CountMoreThan500.setValue1(500.0);

		CompositionCohortDefinition pregnantWomenEligibleonTripleTherapy = new CompositionCohortDefinition();
		pregnantWomenEligibleonTripleTherapy
				.setName("pregnantWomenEligibleonTripleTherapy");
		pregnantWomenEligibleonTripleTherapy.getSearches().put("1",
				new Mapped<CohortDefinition>(pregnantWomenUpToDate, null));
		pregnantWomenEligibleonTripleTherapy.getSearches().put("2",
				new Mapped<CohortDefinition>(womenPregnantOnArtState, null));
		pregnantWomenEligibleonTripleTherapy.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(patientWithCd4CountMoreThan500,
						null));
		pregnantWomenEligibleonTripleTherapy.getSearches().put("4",
				new Mapped<CohortDefinition>(whostagetwoad, null));
		pregnantWomenEligibleonTripleTherapy.getSearches().put("5",
				new Mapped<CohortDefinition>(whostageonead, null));
		pregnantWomenEligibleonTripleTherapy
				.setCompositionString("1 AND 2 AND (3 OR (4 OR 5))");

		CohortIndicator pregnantWomenEligibleonTripleTherapyIndicator = Indicators
				.newCohortIndicator(
						"pregnantWomenEligibleonTripleTherapyIndicator",
						pregnantWomenEligibleonTripleTherapy, null);

		// 11 Number of HIV positive pregnant women eligible for treatment given
		// ARVs
		// 12 Number of women tested for RPR
		// 13 Number of Pregnant women tested positive for RPR they are the same
		// plus dimensions

		// 14 Number of pregnant women partners tested for HIV

		SqlCohortDefinition menDiscordantTested = new SqlCohortDefinition();
		menDiscordantTested.setName("menDiscordantTested");
		menDiscordantTested
				.setQuery("SELECT DISTINCT rel.person_b FROM relationship rel, relationship_type relt, obs o, person pe WHERE rel.person_a = pe.person_id "
						+ "AND rel.relationship=relt.relationship_type_id AND o.person_id=pe.person_id and pe.gender='M' and pe.voided=0 "
						+ "AND relt.relationship_type_id=10 and rel.relationship=10 and o.concept_id=2169 AND o.obs_datetime>= :startTest AND o.obs_datetime<= :endTest ");
		menDiscordantTested.addParameter(new Parameter("startTest",
				"startTest", Date.class));
		menDiscordantTested.addParameter(new Parameter("endTest", "endTest",
				Date.class));

		CompositionCohortDefinition womenPartnersTested = new CompositionCohortDefinition();
		womenPartnersTested.setName("womenPartnersTested");
		womenPartnersTested.addParameter(new Parameter("startTest",
				"startTest", Date.class));
		womenPartnersTested.addParameter(new Parameter("endTest", "endTest",
				Date.class));
		womenPartnersTested.getSearches().put("1",
				new Mapped<CohortDefinition>(pregnantWomenUpToDate, null));
		womenPartnersTested
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								menDiscordantTested,
								ParameterizableUtil
										.createParameterMappings("startTest=${startTest},endTest=${endTest}")));
		womenPartnersTested.setCompositionString("1 AND 2");

		CohortIndicator womenPartnersTestedInd = Indicators
				.newCohortIndicator(
						"womenPartnersTestedInd",
						womenPartnersTested,
						ParameterizableUtil
								.createParameterMappings("startTest=${startDate},endTest=${endDate}"));

		// 15 Number of HIV negative pregnant women whose partners are tested
		// HIV Positive

		SqlCohortDefinition menDiscordantPos = new SqlCohortDefinition();
		menDiscordantPos.setName("menDiscordantPos");
		menDiscordantPos
				.setQuery("SELECT distinct rel.person_b FROM relationship rel, relationship_type relt, obs o, person pe WHERE rel.person_a = pe.person_id "
						+ "AND rel.relationship=relt.relationship_type_id AND o.person_id=pe.person_id and pe.gender='M' and pe.voided=0 "
						+ "AND relt.relationship_type_id=10 and rel.relationship=10 and o.concept_id=2169 and o.value_coded=703 and o.obs_datetime>= :startPart AND o.obs_datetime<= :endPart ");
		menDiscordantPos.addParameter(new Parameter("startPart", "startPart",
				Date.class));
		menDiscordantPos.addParameter(new Parameter("endPart", "endPart",
				Date.class));

		SqlCohortDefinition womenNegTested = new SqlCohortDefinition();
		womenNegTested.setName("womenNegTested");
		womenNegTested
				.setQuery("SELECT distinct rel.person_b FROM relationship rel, relationship_type relt, obs o, person pe WHERE rel.person_b = pe.person_id "
						+ "AND rel.relationship=relt.relationship_type_id AND o.person_id=pe.person_id and pe.gender='F' and pe.voided=0 "
						+ "AND relt.relationship_type_id=10 and rel.relationship=10 and o.concept_id=2169 and o.value_coded=664 ");

		CompositionCohortDefinition discordantCoupleInPmtct = new CompositionCohortDefinition();
		discordantCoupleInPmtct.setName("discordantCoupleInPmtct");
		discordantCoupleInPmtct.addParameter(new Parameter("startPart",
				"startPart", Date.class));
		discordantCoupleInPmtct.addParameter(new Parameter("endPart",
				"endPart", Date.class));
		discordantCoupleInPmtct.getSearches().put("1",
				new Mapped<CohortDefinition>(pregnantWomenUpToDate, null));
		discordantCoupleInPmtct.getSearches().put("2",
				new Mapped<CohortDefinition>(womenNegTested, null));
		discordantCoupleInPmtct
				.getSearches()
				.put("3",
						new Mapped<CohortDefinition>(
								menDiscordantPos,
								ParameterizableUtil
										.createParameterMappings("startPart=${startPart},endPart=${endPart}")));
		discordantCoupleInPmtct.setCompositionString("1 AND 2 AND 3");

		CohortIndicator discordantCoupleInPmtctInd = Indicators
				.newCohortIndicator(
						"discordantCoupleInPmtctInd",
						discordantCoupleInPmtct,
						ParameterizableUtil
								.createParameterMappings("startPart=${startDate},endPart=${endDate}"));

		// 16 Number of discordant couples

		SqlCohortDefinition womenNegDuringP = new SqlCohortDefinition();
		womenNegDuringP.setName("womenNegDuringP");
		womenNegDuringP
				.setQuery("SELECT distinct rel.person_b FROM relationship rel, relationship_type relt, obs o, person pe WHERE rel.person_b = pe.person_id "
						+ "AND rel.relationship=relt.relationship_type_id AND o.person_id=pe.person_id and pe.gender='F' and pe.voided=0 "
						+ "AND relt.relationship_type_id=10 and rel.relationship=10 and o.concept_id=2169 and o.value_coded=664 and o.obs_datetime>= :partF AND o.obs_datetime<= :partFe ");
		womenNegDuringP
				.addParameter(new Parameter("partF", "partF", Date.class));
		womenNegDuringP.addParameter(new Parameter("partFe", "partFe",
				Date.class));

		CompositionCohortDefinition womenNegDuringPCOmp = new CompositionCohortDefinition();
		womenNegDuringPCOmp.setName("womenNegDuringPCOmp");
		womenNegDuringPCOmp.addParameter(new Parameter("startPart",
				"startPart", Date.class));
		womenNegDuringPCOmp.addParameter(new Parameter("endPart", "endPart",
				Date.class));
		womenNegDuringPCOmp.addParameter(new Parameter("partF", "partF",
				Date.class));
		womenNegDuringPCOmp.addParameter(new Parameter("partFe", "partFe",
				Date.class));
		womenNegDuringPCOmp.getSearches().put("1",
				new Mapped<CohortDefinition>(pregnantWomenUpToDate, null));
		womenNegDuringPCOmp
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								menDiscordantPos,
								ParameterizableUtil
										.createParameterMappings("startPart=${startPart},endPart=${endPart}")));
		womenNegDuringPCOmp
				.getSearches()
				.put("3",
						new Mapped<CohortDefinition>(
								womenNegDuringP,
								ParameterizableUtil
										.createParameterMappings("partF=${partF},partFe=${partFe}")));
		womenNegDuringPCOmp.setCompositionString("1 AND 2 AND 3");

		CohortIndicator womenNegDuringInd = Indicators
				.newCohortIndicator(
						"womenNegDuringInd",
						womenNegDuringPCOmp,
						ParameterizableUtil
								.createParameterMappings("startPart=${startDate},endPart=${endDate},partF=${startDate},partFe=${endDate}"));

		// 17 Number of partners tested HIV positive

		CompositionCohortDefinition menPartnerTestedHivPositiveComp = new CompositionCohortDefinition();
		menPartnerTestedHivPositiveComp
				.setName("menPartnerTestedHivPositiveComp");
		menPartnerTestedHivPositiveComp.addParameter(new Parameter("startPart",
				"startPart", Date.class));
		menPartnerTestedHivPositiveComp.addParameter(new Parameter("endPart",
				"endPart", Date.class));
		menPartnerTestedHivPositiveComp.getSearches().put("1",
				new Mapped<CohortDefinition>(pregnantWomenUpToDate, null));
		menPartnerTestedHivPositiveComp
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								menDiscordantPos,
								ParameterizableUtil
										.createParameterMappings("startPart=${startPart},endPart=${endPart}")));
		menPartnerTestedHivPositiveComp.setCompositionString("1 AND 2");

		CohortIndicator menPartnerTestedHivPositiveInd = Indicators
				.newCohortIndicator(
						"menPartnerTestedHivPositiveInd",
						menPartnerTestedHivPositiveComp,
						ParameterizableUtil
								.createParameterMappings("startPart=${startDate},endPart=${endDate}"));

		// -----------------------------------------------------------------------------------------------------------------------------------------------
		// PMTCT MATERNITY DATA ELEMENTS
		// -----------------------------------------------------------------------------------------------------------------------------------------------

		// 1 Number of deliveries expected at the facility this month

		SqlCohortDefinition estimatedDateOfConfinement = new SqlCohortDefinition();
		estimatedDateOfConfinement.setName("estimatedDateOfConfinement");
		estimatedDateOfConfinement
				.setQuery("select o.person_id from obs o,concept c where o.concept_id=c.concept_id and c.concept_id='"
						+ Context.getAdministrationService().getGlobalProperty(
								"report.estimatedDateOfConfinement")
						+ "' and value_datetime>= :startEst and value_datetime<= :endDate and voided=0");
		estimatedDateOfConfinement.addParameter(new Parameter("startEst",
				"startEst", Date.class));
		estimatedDateOfConfinement.addParameter(new Parameter("endEst",
				"endEst", Date.class));

		CompositionCohortDefinition expectedDeliveriesAmongHivPosWomen = new CompositionCohortDefinition();
		expectedDeliveriesAmongHivPosWomen
				.setName("expectedDeliveriesAmongHivPosWomen");
		expectedDeliveriesAmongHivPosWomen.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		expectedDeliveriesAmongHivPosWomen.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		expectedDeliveriesAmongHivPosWomen.addParameter(new Parameter(
				"startEst", "startEst", Date.class));
		expectedDeliveriesAmongHivPosWomen.addParameter(new Parameter("endEst",
				"endEst", Date.class));
		expectedDeliveriesAmongHivPosWomen.addParameter(new Parameter("onDate",
				"onDate", Date.class));
		expectedDeliveriesAmongHivPosWomen
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								estimatedDateOfConfinement,
								ParameterizableUtil
										.createParameterMappings("startEst=${startDate},endEst=${endDate}")));
		expectedDeliveriesAmongHivPosWomen.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(inPMTCTpregnantProgram,
						ParameterizableUtil
								.createParameterMappings("onDate=${endDate}")));
		expectedDeliveriesAmongHivPosWomen.setCompositionString("1 AND 2 ");

		SqlCohortDefinition dateOfdelivery = new SqlCohortDefinition();
		dateOfdelivery.setName("dateOfdelivery");
		dateOfdelivery
				.setQuery("select o.person_id from obs o,concept c where o.concept_id=c.concept_id and c.concept_id='"
						+ Context.getAdministrationService().getGlobalProperty(
								"reports.dateofdelivery")
						+ "' and value_datetime>= :startDate and value_datetime<= :endDate and voided=0");
		dateOfdelivery.addParameter(new Parameter("startExp", "startExp",
				Date.class));
		dateOfdelivery.addParameter(new Parameter("endExp", "endExp",
				Date.class));

		CompositionCohortDefinition expectedDeliveriesFacilityThisMonth = new CompositionCohortDefinition();
		expectedDeliveriesFacilityThisMonth
				.setName("expectedDeliveriesFacilityThisMonth");
		expectedDeliveriesFacilityThisMonth.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(
						expectedDeliveriesAmongHivPosWomen, null));
		expectedDeliveriesFacilityThisMonth.getSearches().put("2",
				new Mapped<CohortDefinition>(givingbirthathospital, null));
		expectedDeliveriesFacilityThisMonth.getSearches().put("3",
				new Mapped<CohortDefinition>(givingbirthathealthcenter, null));
		expectedDeliveriesFacilityThisMonth
				.setCompositionString(" 1 AND ( 2 OR 3 ) ");

		CohortIndicator expectedDeliveriesFacilityThisMonthIndicator = Indicators
				.newCohortIndicator(
						"expectedDeliveriesFacilityThisMonthIndicator",
						expectedDeliveriesFacilityThisMonth, null);

		// 2 Number of deliveries occurring at the facility this month
		CompositionCohortDefinition delivereyComp = new CompositionCohortDefinition();
		delivereyComp.setName("delivereyComp");
		delivereyComp.addParameter(new Parameter("startDate", "startDate",
				Date.class));
		delivereyComp.addParameter(new Parameter("endDate", "endDate",
				Date.class));
		delivereyComp.addParameter(new Parameter("startExp", "startExp",
				Date.class));
		delivereyComp
				.addParameter(new Parameter("endExp", "endExp", Date.class));
		delivereyComp
				.addParameter(new Parameter("onDate", "onDate", Date.class));
		delivereyComp
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								dateOfdelivery,
								ParameterizableUtil
										.createParameterMappings("startExp=${startDate},endExp=${endDate}")));
		delivereyComp.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(PMTCTCombinedMother,
						ParameterizableUtil
								.createParameterMappings("onDate=${endDate}")));
		delivereyComp.setCompositionString("1 AND 2 ");

		CompositionCohortDefinition deliveryOccuredthismonth = new CompositionCohortDefinition();
		deliveryOccuredthismonth.setName("deliveryOccuredthismonth");
		deliveryOccuredthismonth.getSearches().put("1",
				new Mapped<CohortDefinition>(delivereyComp, null));
		deliveryOccuredthismonth.getSearches().put("2",
				new Mapped<CohortDefinition>(givingbirthathospital, null));
		deliveryOccuredthismonth.getSearches().put("3",
				new Mapped<CohortDefinition>(givingbirthathealthcenter, null));
		deliveryOccuredthismonth.getSearches().put("4",
				new Mapped<CohortDefinition>(givingbirthathome, null));
		deliveryOccuredthismonth.setCompositionString("1 AND ( 2 OR 3 OR 4 ) ");

		CohortIndicator occuringDeliveriesFacilityThisMonthIndicator = Indicators
				.newCohortIndicator(
						"occuringDeliveriesFacilityThisMonthIndicator",
						deliveryOccuredthismonth, null);

		// 3 Number of expected deliveries among HIV positive women
		CohortIndicator expectedDeliveriesAmongHivPosWomenIndicator = Indicators
				.newCohortIndicator(
						"expectedDeliveriesAmongHivPosWomenIndicator",
						expectedDeliveriesAmongHivPosWomen, null);

		// 4 Number of HIV positive women giving birth at the facility

		CompositionCohortDefinition delivereyOccuredAtFosa = new CompositionCohortDefinition();
		delivereyOccuredAtFosa.setName("delivereyOccuredAtFosa");
		delivereyOccuredAtFosa.getSearches().put("1",
				new Mapped<CohortDefinition>(delivereyComp, null));
		delivereyOccuredAtFosa.getSearches().put("2",
				new Mapped<CohortDefinition>(givingbirthathospital, null));
		delivereyOccuredAtFosa.getSearches().put("3",
				new Mapped<CohortDefinition>(givingbirthathealthcenter, null));
		delivereyOccuredAtFosa.setCompositionString("1 AND ( 2 OR 3 ) ");

		CohortIndicator delivereyOccuredAtFosaIndicators = Indicators
				.newCohortIndicator("delivereyOccuredAtFosaIndicators",
						delivereyOccuredAtFosa, null);

		// 5 Number of reported HIV positive women giving birth at home

		CompositionCohortDefinition reportedHivPosGivingBirthAtHome = new CompositionCohortDefinition();
		reportedHivPosGivingBirthAtHome
				.setName("reportedHivPosGivingBirthAtHome");
		reportedHivPosGivingBirthAtHome.getSearches().put("1",
				new Mapped<CohortDefinition>(delivereyComp, null));
		reportedHivPosGivingBirthAtHome.getSearches().put("2",
				new Mapped<CohortDefinition>(givingbirthathome, null));
		reportedHivPosGivingBirthAtHome.setCompositionString("1 AND 2  ");

		CohortIndicator reportedHivPosGivingBirthAtHomeInd = Indicators
				.newCohortIndicator("reportedHivPosGivingBirthAtHomeInd",
						reportedHivPosGivingBirthAtHome, null);

		// 6 Number of HIV positive women given Sd of tripletherapy of
		// AZT+3TC+NVP as prophylaxis during labor
		// 7 Number of women receiving AZT+3TC after delivery

		CompositionCohortDefinition donetestindeliveryroom = new CompositionCohortDefinition();
		donetestindeliveryroom.setName("donetestindeliveryroom");
		donetestindeliveryroom.addParameter(new Parameter("endDate", "endDate",
				Date.class));
		donetestindeliveryroom.addParameter(new Parameter("onDate", "onDate",
				Date.class));
		donetestindeliveryroom.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(inPMTCTpregnantProgram,
						ParameterizableUtil
								.createParameterMappings("onDate=${endDate}")));
		donetestindeliveryroom.getSearches().put("2",
				new Mapped<CohortDefinition>(testinmaternity, null));
		donetestindeliveryroom.setCompositionString(" 1 AND 2 ");

		CohortIndicator donetestindeliveryroomIndi = Indicators
				.newCohortIndicator("donetestindeliveryroomIndi",
						donetestindeliveryroom, null);

		// 8 Number of women with unknown HIV status tested for HIV during labor
		// and delivery

		CompositionCohortDefinition womenUnknownHivStatusTestedDuringLabor1 = new CompositionCohortDefinition();
		womenUnknownHivStatusTestedDuringLabor1
				.setName("womenUnknownHivStatusTestedDuringLabor1");
		womenUnknownHivStatusTestedDuringLabor1.getSearches().put("1",
				new Mapped<CohortDefinition>(donetestindeliveryroom, null));
		womenUnknownHivStatusTestedDuringLabor1.getSearches().put("2",
				new Mapped<CohortDefinition>(pospregnancyStatus, null));
		womenUnknownHivStatusTestedDuringLabor1
				.setCompositionString(" 1 AND 2  ");

		CohortIndicator womenUnknownHivStatusTestedDuringLabor1Indicator = Indicators
				.newCohortIndicator(
						"womenUnknownHivStatusTestedDuringLabor1Indicator",
						womenUnknownHivStatusTestedDuringLabor1, null);

		// 9 Number of women with unknown HIV status tested HIV positive during
		// labor and delivery

		CompositionCohortDefinition womenUnknownHivinitiatedOnTrhitherapy = new CompositionCohortDefinition();
		womenUnknownHivinitiatedOnTrhitherapy
				.setName("womenUnknownHivinitiatedOnTrhitherapy");
		womenUnknownHivinitiatedOnTrhitherapy.addParameter(new Parameter(
				"nvpStart", "nvpStart", Date.class));
		womenUnknownHivinitiatedOnTrhitherapy.addParameter(new Parameter(
				"nvpEnd", "nvpEnd", Date.class));
		womenUnknownHivinitiatedOnTrhitherapy
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								ontriplethrapy,
								ParameterizableUtil
										.createParameterMappings("nvpStart=${nvpStart},nvpEnd=${nvpEnd}")));
		womenUnknownHivinitiatedOnTrhitherapy.getSearches().put("3",
				new Mapped<CohortDefinition>(pregnantWomenUpToDate, null));
		womenUnknownHivinitiatedOnTrhitherapy.setCompositionString("2 AND 3 ");

		CohortIndicator womenUnknownHivinitiatedontrhitherapyInd = Indicators
				.newCohortIndicator(
						"womenUnknownHivinitiatedontrhitherapyInd",
						womenUnknownHivinitiatedOnTrhitherapy,
						ParameterizableUtil
								.createParameterMappings("nvpStart=${startDate},nvpEnd=${endDate}"));

		// 10 Number of pregnant women received a complete course of ART
		// prophylaxis this month

		InStateCohortDefinition pregnantongaveBirthState = Cohorts
				.createInProgramStateParameterizableByDate(
						"pregnantongaveBirthState", gaveBirthinpmtctstate);
		CompositionCohortDefinition pregnantReceivedCompleteCourseThisMonth = new CompositionCohortDefinition();
		pregnantReceivedCompleteCourseThisMonth
				.setName("womenUnknownHivinitiatedOnTrhitherapy");
		pregnantReceivedCompleteCourseThisMonth.addParameter(new Parameter(
				"biStart", "biStart", Date.class));
		pregnantReceivedCompleteCourseThisMonth.addParameter(new Parameter(
				"biEnd", "biEnd", Date.class));
		pregnantReceivedCompleteCourseThisMonth.addParameter(new Parameter(
				"onDate", "onDate", Date.class));
		pregnantReceivedCompleteCourseThisMonth.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(inPMTCTpregnantProgram,
						ParameterizableUtil
								.createParameterMappings("onDate=${onDate}")));
		pregnantReceivedCompleteCourseThisMonth.getSearches().put("3",
				new Mapped<CohortDefinition>(pregnantongaveBirthState, null));
		pregnantReceivedCompleteCourseThisMonth
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								bitherapy,
								ParameterizableUtil
										.createParameterMappings("biStart=${biStart},biEnd=${biEnd}")));
		pregnantReceivedCompleteCourseThisMonth
				.setCompositionString(" 1 AND 2 AND 3 ");

		CohortIndicator pregnantReceivedCompleteCourseThisMonthInd = Indicators
				.newCohortIndicator(
						"pregnantReceivedCompleteCourseThisMonthInd",
						pregnantReceivedCompleteCourseThisMonth,
						ParameterizableUtil
								.createParameterMappings("biStart=${startDate},biEnd=${endDate},onDate=${endDate}"));
		// 11 Nombre de nouveaux nés des mères séropositives ayant reçu ARV
		// prophylaxie à la naissance

		// -----------------------------------------------------------------------------------------------------------------------------------------------
		// PMTCT: HIV INFANT EXPOSED
		// -----------------------------------------------------------------------------------------------------------------------------------------------

		// 1 Number of HIV positive women breastfeeding

		SqlCohortDefinition femalesbreastFeeding = new SqlCohortDefinition();
		femalesbreastFeeding.setName("femalesbreastFeeding");
		femalesbreastFeeding
				.setQuery("select distinct rel.person_a FROM relationship rel, relationship_type relt, obs o, person pe "
						+ "WHERE rel.person_a = pe.person_id AND rel.relationship=relt.relationship_type_id "
						+ "AND o.person_id=pe.person_id and o.concept_id=2169 AND o.value_coded=703 AND pe.gender='f' AND relt.relationship_type_id=13 ");

		SqlCohortDefinition kidsbornToExposedMother = new SqlCohortDefinition();
		kidsbornToExposedMother.setName("kidsbornToExposedMother");
		kidsbornToExposedMother
				.setQuery(" select distinct rel.person_b from relationship rel inner join person pe on rel.person_a = pe.person_id where rel.voided = 0 and pe.voided = 0 ");

		CompositionCohortDefinition infantwithPmtctupTodate = new CompositionCohortDefinition();
		infantwithPmtctupTodate.setName("infantwithPmtctupTodate");
		infantwithPmtctupTodate.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		infantwithPmtctupTodate.addParameter(new Parameter("onDate", "onDate",
				Date.class));
		infantwithPmtctupTodate.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(PMTCTCombinedInfant,
						ParameterizableUtil
								.createParameterMappings("onDate=${endDate}")));
		infantwithPmtctupTodate.setCompositionString(" 1 ");

		CompositionCohortDefinition childBreastFeeding = new CompositionCohortDefinition();
		childBreastFeeding.setName("childBreastFeeding");
		childBreastFeeding.addParameter(new Parameter("onOrAfter", "onOrAfter",
				Date.class));
		childBreastFeeding.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		childBreastFeeding.addParameter(new Parameter("startDate", "startDate",
				Date.class));
		childBreastFeeding.addParameter(new Parameter("endDate", "endDate",
				Date.class));
		childBreastFeeding.getSearches().put("1",
				new Mapped<CohortDefinition>(lessThan5, null));
		childBreastFeeding.getSearches().put("2",
				new Mapped<CohortDefinition>(kidsbornToExposedMother, null));
		childBreastFeeding
				.getSearches()
				.put("3",
						new Mapped<CohortDefinition>(
								patientBreastfeedingThisMonth,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		childBreastFeeding.getSearches().put("4",
				new Mapped<CohortDefinition>(infantwithPmtctupTodate, null));
		childBreastFeeding.setCompositionString(" 1 AND 2 AND 3 AND 4 ");

		CompositionCohortDefinition womenHivPosBreastFeedingComposition = new CompositionCohortDefinition();
		womenHivPosBreastFeedingComposition
				.setName("womenHivPosBreastFeedingComposition");
		womenHivPosBreastFeedingComposition.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		womenHivPosBreastFeedingComposition.addParameter(new Parameter(
				"onDate", "onDate", Date.class));
		womenHivPosBreastFeedingComposition.getSearches().put("1",
				new Mapped<CohortDefinition>(femalesbreastFeeding, null));
		womenHivPosBreastFeedingComposition.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(PMTCTCombinedMother,
						ParameterizableUtil
								.createParameterMappings("onDate=${endDate}")));
		womenHivPosBreastFeedingComposition.getSearches().put("3",
				new Mapped<CohortDefinition>(childBreastFeeding, null));
		womenHivPosBreastFeedingComposition
				.setCompositionString(" 1 AND 2 AND 3 ");

		CohortIndicator womenHivPosBreastFeedingIndicator = Indicators
				.newCohortIndicator("womenHivPosBreastFeedingIndicator",
						womenHivPosBreastFeedingComposition, null);

		// 2 Number of HIV positive women using formula
		CodedObsCohortDefinition usingFormulaThisMonth = Cohorts
				.createCodedObsCohortDefinition("usingFormulaThisMonth",
						onOrAfterOnOrBeforeParamterNames, infantFeedingMethod,
						usingFormula, SetComparator.IN, TimeModifier.LAST);

		CompositionCohortDefinition childUsingFormula = new CompositionCohortDefinition();
		childUsingFormula.setName("childBreastFeeding");
		childUsingFormula.addParameter(new Parameter("onOrAfter", "onOrAfter",
				Date.class));
		childUsingFormula.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		childUsingFormula.addParameter(new Parameter("startDate", "startDate",
				Date.class));
		childUsingFormula.addParameter(new Parameter("endDate", "endDate",
				Date.class));
		childUsingFormula.getSearches().put("1",
				new Mapped<CohortDefinition>(lessThan5, null));
		childUsingFormula.getSearches().put("2",
				new Mapped<CohortDefinition>(kidsbornToExposedMother, null));
		childUsingFormula
				.getSearches()
				.put("3",
						new Mapped<CohortDefinition>(
								usingFormulaThisMonth,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		childUsingFormula.getSearches().put("4",
				new Mapped<CohortDefinition>(infantwithPmtctupTodate, null));
		childUsingFormula.setCompositionString(" 1 AND 2 AND 3 AND 4 ");

		CompositionCohortDefinition womenHivPosUsingFormula = new CompositionCohortDefinition();
		womenHivPosUsingFormula.setName("womenHivPosUsingFormula");
		womenHivPosUsingFormula.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		womenHivPosUsingFormula.addParameter(new Parameter("onDate", "onDate",
				Date.class));
		womenHivPosUsingFormula.getSearches().put("1",
				new Mapped<CohortDefinition>(femalesbreastFeeding, null));
		womenHivPosUsingFormula.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(PMTCTCombinedMother,
						ParameterizableUtil
								.createParameterMappings("onDate=${endDate}")));
		womenHivPosUsingFormula.getSearches().put("3",
				new Mapped<CohortDefinition>(childUsingFormula, null));
		womenHivPosUsingFormula.setCompositionString("1 AND 2 AND 3  ");

		CohortIndicator womenHivPosUsingFormulaIndicator = Indicators
				.newCohortIndicator("womenHivPosUsingFormulaIndicator",
						womenHivPosUsingFormula, null);

		// 3 Number of infants born to HIV positive mothers currently enrolled
		// in the PMTCT program

		SqlCohortDefinition infantBornToHIvMotherExposed = new SqlCohortDefinition();
		infantBornToHIvMotherExposed.setName("hivPosMother");
		infantBornToHIvMotherExposed
				.setQuery("select distinct rel.person_b FROM relationship rel, relationship_type relt, obs o, person pe "
						+ "WHERE rel.person_a = pe.person_id AND rel.relationship=relt.relationship_type_id "
						+ "AND o.person_id=pe.person_id and o.concept_id=2169 AND o.value_coded=703 AND pe.gender='f' AND relt.relationship_type_id=13 ");

		ProgramEnrollmentCohortDefinition PMTCTCombinedInfantDuringP = Cohorts
				.createProgramEnrollmentParameterizedByStartEndDate(
						"PMTCTCombinedInfantDuringP", pmtctcombinedInfant);
		PMTCTCombinedInfantDuringP.addParameter(new Parameter(
				"enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		PMTCTCombinedInfantDuringP.addParameter(new Parameter(
				"enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));

		CompositionCohortDefinition infantwithPmtctupDuringP = new CompositionCohortDefinition();
		infantwithPmtctupDuringP.setName("infantwithPmtctupDuringP");
		infantwithPmtctupDuringP.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		infantwithPmtctupDuringP.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		infantwithPmtctupDuringP.addParameter(new Parameter(
				"enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		infantwithPmtctupDuringP.addParameter(new Parameter(
				"enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		infantwithPmtctupDuringP
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								PMTCTCombinedInfantDuringP,
								ParameterizableUtil
										.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}")));
		infantwithPmtctupDuringP.setCompositionString(" 1 ");

		CompositionCohortDefinition infantBornOnHivPositive = new CompositionCohortDefinition();
		infantBornOnHivPositive.setName("infantBornOnHivPositive");
		infantBornOnHivPositive.getSearches().put("1",
				new Mapped<CohortDefinition>(infantwithPmtctupDuringP, null));
		infantBornOnHivPositive.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								infantBornToHIvMotherExposed, null));
		infantBornOnHivPositive.setCompositionString(" 1 AND 2 ");

		CohortIndicator infantBornOnHivPositiveIndicator = Indicators
				.newCohortIndicator("infantBornOnHivPositiveIndicator",
						infantBornOnHivPositive, null);

		// 4 Number of infants born to HIV negative (in discordantCouple)
		// mothers currently enrolled in the PMTCT program

		SqlCohortDefinition infantBornToHIvNegMotherExposed = new SqlCohortDefinition();
		infantBornToHIvNegMotherExposed
				.setName("infantBornToHIvNegMotherExposed");
		infantBornToHIvNegMotherExposed
				.setQuery("select distinct rel.person_b FROM relationship rel, relationship_type relt, obs o, person pe "
						+ "WHERE rel.person_a = pe.person_id AND rel.relationship=relt.relationship_type_id "
						+ "AND o.person_id=pe.person_id and o.concept_id=2169 AND o.value_coded=664 AND pe.gender='f' AND relt.relationship_type_id=13 ");

		CompositionCohortDefinition infantBornOnHivNegativeMothers = new CompositionCohortDefinition();
		infantBornOnHivNegativeMothers
				.setName("infantBornOnHivNegativeMothers");
		infantBornOnHivNegativeMothers.getSearches().put("1",
				new Mapped<CohortDefinition>(infantwithPmtctupDuringP, null));
		infantBornOnHivNegativeMothers.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(infantBornToHIvNegMotherExposed,
						null));
		infantBornOnHivNegativeMothers.setCompositionString("1 AND 2  ");

		CohortIndicator infantBornOnHivNegativeMothersInd = Indicators
				.newCohortIndicator("infantBornOnHivNegativeMothersInd",
						infantBornOnHivNegativeMothers, null);

		// 5 Number of infants born to HIV positive mothers tested at 6 weeks
		// 6 Number of infants born from HIV positive mothers who tested HIV
		// positive at 6 weeks
		// 7 Number of infants born from HIV positive mothers who are 6 weeks of
		// age this month
		// 6 and 7 are like 5 with differents dimensions

		// 9 Number of infants born from HIV positive mothers tested at 9 months
		// 10 Number of infants born from HIV positive mothers who tested HIV
		// positive at 9 months
		// 11 Number of infants born to HIV positive mothers who are 9 months of
		// age this month
		// 9,10,11 are same to 5 with diferents age dimensions

		// 12 Number of infants born from HIV positive mothers tested at 18
		// months
		// 13 Number of infants born from HIV positive mothers who tested HIV
		// positive at 18 months
		// 14 Number of infants born from HIV positive mothers who are 18 months
		// of age this month
		// 12,13,14 are same to 5 with diferents age dimensions

		// 16 Number of infants born to HIV positive mothers screened for TB
		// this month
		// 17 Number of reported deaths of infants born to HIV positive mothers
		// 16 is the same with 5 with a dimension tbtest
		// 17 is the same with 5 with a dimension died

		CompositionCohortDefinition infantWithBornTohivPosOrNegandTested = new CompositionCohortDefinition();
		infantWithBornTohivPosOrNegandTested
				.setName("infantWithBornTohivPosOrNegandTested");
		infantWithBornTohivPosOrNegandTested.getSearches().put("1",
				new Mapped<CohortDefinition>(infantwithPmtctupTodate, null));
		infantWithBornTohivPosOrNegandTested.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								infantBornToHIvMotherExposed, null));
		infantWithBornTohivPosOrNegandTested.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(infantBornToHIvNegMotherExposed,
						null));
		infantWithBornTohivPosOrNegandTested
				.setCompositionString(" 1 AND (2 OR 3) ");

		CohortIndicator infantWithBornTohivPosOrNegandTestedTested = Indicators
				.newCohortIndicator(
						"infantWithBornTohivPosOrNegandTestedTested",
						infantWithBornTohivPosOrNegandTested, null);

		// 8 Number of infants born from HIV positive mothers who are 6 weeks of
		// age this month and initiated on Cotrimoxazole
		CompositionCohortDefinition infantWith6WeeksOnCotrimo = new CompositionCohortDefinition();
		infantWith6WeeksOnCotrimo.setName("infantWith6WeeksOnCotrimo");
		infantWith6WeeksOnCotrimo.addParameter(new Parameter("coStart",
				"coStart", Date.class));
		infantWith6WeeksOnCotrimo.addParameter(new Parameter("coEnd", "coEnd",
				Date.class));
		infantWith6WeeksOnCotrimo
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								startedCotrimoXazoleDuringP,
								ParameterizableUtil
										.createParameterMappings("coStart=${coStart},coEnd=${coEnd}")));
		infantWith6WeeksOnCotrimo.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(
						infantWithBornTohivPosOrNegandTested, null));
		infantWith6WeeksOnCotrimo.setCompositionString("1 AND 2 ");

		CohortIndicator infantHivPosMothersTestedAt6WeeksIndicatoronCotrimo = Indicators
				.newCohortIndicator(
						"infantHivPosMothersTestedAt6WeeksIndicatoronCotrimo",
						infantWith6WeeksOnCotrimo,
						ParameterizableUtil
								.createParameterMappings("coStart=${startDate},coEnd=${endDate}"));

		// 15 infants exposed who are LOST TO FOLLOW UP
		CompositionCohortDefinition infantsExposedlostToFollowUp = new CompositionCohortDefinition();
		infantsExposedlostToFollowUp.setName("infantsExposedlostToFollowUp");
		infantsExposedlostToFollowUp.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		infantsExposedlostToFollowUp
				.getSearches()
				.put("patientsWithClinicalEncountersWithoutLabTest",
						new Mapped<CohortDefinition>(
								patientsWithClinicalEncountersWithoutLabTest,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${onOrAfter}")));
		infantsExposedlostToFollowUp.getSearches().put(
				"infantWithBornTohivPosOrNegandTested",
				new Mapped<CohortDefinition>(
						infantWithBornTohivPosOrNegandTested, null));
		infantsExposedlostToFollowUp
				.setCompositionString("infantWithBornTohivPosOrNegandTested AND (NOT patientsWithClinicalEncountersWithoutLabTest)");

		CohortIndicator infantHivPosMothersLostFollowupIndicator = Indicators
				.newCohortIndicator(
						"infantHivPosMothersLostFollowupIndicator",
						infantsExposedlostToFollowUp,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate-12m}"));

		// 18, 19 will ask larra, don't know how clinicaly malnourished is
		// captured here

		// -----------------------------------------------------------------------------------------------------------------------------------------------
		// FAMILY PLANNING DATA ELEMENT
		// -----------------------------------------------------------------------------------------------------------------------------------------------

		// 1 Number of HIV positive women expected in family planning at the
		// facility

		CompositionCohortDefinition womenWithFplaningAtFacility = new CompositionCohortDefinition();
		womenWithFplaningAtFacility.setName("womenWithFplaningAtFacility");
		womenWithFplaningAtFacility.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		womenWithFplaningAtFacility.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(
						expectedDeliveriesAmongHivPosWomen, null));
		womenWithFplaningAtFacility
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								patInFplanning,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${onOrBefore}")));
		womenWithFplaningAtFacility.setCompositionString("1 AND 2 ");

		CohortIndicator womenHivPosExpectedFpAtFacilityPeriodIndicator = Indicators
				.newCohortIndicator(
						"womenHivPosExpectedFpAtFacilityPeriodIndicator",
						womenWithFplaningAtFacility,
						ParameterizableUtil
								.createParameterMappings("onOrBefore=${endDate}"));

		// 2 Number of HIV positive women seen in family planning

		CohortIndicator womenSeenInFpPeriodIndicator = Indicators
				.newCohortIndicator(
						"womenSeenInFpPeriodIndicator",
						patInFplanning,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		// 3 Number of HIV positive women partners seen in family planning
		SqlCohortDefinition partnerInFP = new SqlCohortDefinition();
		partnerInFP.setName("partnerInFP");
		partnerInFP
				.setQuery("SELECT DISTINCT rel.person_a FROM relationship rel INNER JOIN person per ON per.person_id = rel.person_b INNER JOIN obs ob ON per.person_id = ob.person_id INNER JOIN patient pat ON per.person_id = pat.patient_id WHERE rel.voided = 0 and rel.relationship=10 and ob.concept_id="
						+ gp.METHOD_OF_FAMILY_PLANNING_ID
						+ " AND ob.obs_datetime>= :startDate and ob.obs_datetime<= :endDate  AND ob.voided = 0 AND ob.void_reason Is null");
		partnerInFP.addParameter(new Parameter("startDate", "startDate",
				Date.class));
		partnerInFP
				.addParameter(new Parameter("endDate", "endDate", Date.class));

		CohortIndicator womenpartnerhivPosInd = Indicators
				.newCohortIndicator(
						"womenpartnerhivPosInd",
						partnerInFP,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}"));

		// 4 Number of HIV positive women who are receiving modern contraceptive
		// methods

		CodedObsCohortDefinition fpUsingCondoms = Cohorts
				.createCodedObsCohortDefinition("fpUsingCondoms",
						onOrAfterOnOrBeforeParamterNames, familyplaning,
						condoms, SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition fpUsingInjectableContraceptives = Cohorts
				.createCodedObsCohortDefinition(
						"fpUsingInjectableContraceptives",
						onOrAfterOnOrBeforeParamterNames, familyplaning,
						injectable, SetComparator.IN, TimeModifier.LAST);

		CodedObsCohortDefinition fpUsingOralContraceptives = Cohorts
				.createCodedObsCohortDefinition("fpUsingOralContraceptives",
						onOrAfterOnOrBeforeParamterNames, familyplaning, orals,
						SetComparator.IN, TimeModifier.LAST);

		CompositionCohortDefinition womenHivPosReceivingModernContraceptiveComposition = new CompositionCohortDefinition();
		womenHivPosReceivingModernContraceptiveComposition
				.setName("womenHivPosReceivingModernContraceptiveComposition");
		womenHivPosReceivingModernContraceptiveComposition
				.addParameter(new Parameter("startDate", "startDate",
						Date.class));
		womenHivPosReceivingModernContraceptiveComposition
				.addParameter(new Parameter("endDate", "endDate", Date.class));
		womenHivPosReceivingModernContraceptiveComposition
				.addParameter(new Parameter("onOrAfter", "onOrAfter",
						Date.class));
		womenHivPosReceivingModernContraceptiveComposition
				.addParameter(new Parameter("onOrBefore", "onOrBefore",
						Date.class));
		womenHivPosReceivingModernContraceptiveComposition
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								fpUsingInjectableContraceptives,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		womenHivPosReceivingModernContraceptiveComposition
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								fpUsingOralContraceptives,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		womenHivPosReceivingModernContraceptiveComposition
				.getSearches()
				.put("3",
						new Mapped<CohortDefinition>(
								fpUsingCondoms,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		womenHivPosReceivingModernContraceptiveComposition
				.setCompositionString(" (1 OR 2 OR 3) ");

		CohortIndicator womenHivPosReceivingModernContraceptivePeriodIndicator = Indicators
				.newCohortIndicator(
						"womenHivPosReceivingModernContraceptivePeriodIndicator",
						womenHivPosReceivingModernContraceptiveComposition,
						null);

		// 5. Number of patients referred for family planning

		CodedObsCohortDefinition referedForFp = Cohorts
				.createCodedObsCohortDefinition("referedForFp",
						onOrAfterOnOrBeforeParamterNames, disposition,
						referedforFp, SetComparator.IN, TimeModifier.LAST);

		CohortIndicator fpReferredForFamilyPlanningCompositionPeriodIndicator = Indicators
				.newCohortIndicator(
						"fpReferredForFamilyPlanningCompositionPeriodIndicator",
						referedForFp,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		// -----------------------------------------------------------------------------------------------------------------------------------------------
		// PRE-ART DATA ELEMENTS
		// -----------------------------------------------------------------------------------------------------------------------------------------------

		// 1 Total number of new pediatric patients (age < 18 months) enrolled
		// in HIV care
		// 2 Total number of new pediatric patients (age < 5 years) enrolled in
		// HIV care
		// 3 Total number of pediatric patients (age < 15 years) enrolled in HIV
		// care
		// 4 Total number of new female pediatric patients (age < 15 years)
		// enrolled in HIV care
		// 5 Total number of new male pediatric patients (age < 15 years)
		// enrolled in HIV care
		// 6 Total number of adult patients (age 15 or more) enrolled in HIV
		// care

		ProgramEnrollmentCohortDefinition adultsHIVProgramDuringP = Cohorts
				.createProgramEnrollmentParameterizedByStartEndDate(
						"adultsHIVProgramDuringP", adulthivProgram);
		adultsHIVProgramDuringP.addParameter(new Parameter("enrolledOnOrAfter",
				"enrolledOnOrAfter", Date.class));
		adultsHIVProgramDuringP.addParameter(new Parameter(
				"enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));

		ProgramEnrollmentCohortDefinition pediatricsHIVProgramDuringP = Cohorts
				.createProgramEnrollmentParameterizedByStartEndDate(
						"pediatricsHIVProgramDuringP", pediatrichivProgram);
		pediatricsHIVProgramDuringP.addParameter(new Parameter(
				"enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		pediatricsHIVProgramDuringP.addParameter(new Parameter(
				"enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));

		CompositionCohortDefinition patientInHivProgramDuringPeriod = new CompositionCohortDefinition();
		patientInHivProgramDuringPeriod
				.setName("patientInHivProgramDuringPeriod");
		patientInHivProgramDuringPeriod.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		patientInHivProgramDuringPeriod.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientInHivProgramDuringPeriod.addParameter(new Parameter(
				"enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientInHivProgramDuringPeriod.addParameter(new Parameter(
				"enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientInHivProgramDuringPeriod
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								adultsHIVProgramDuringP,
								ParameterizableUtil
										.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}")));
		patientInHivProgramDuringPeriod
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								pediatricsHIVProgramDuringP,
								ParameterizableUtil
										.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}")));
		patientInHivProgramDuringPeriod.setCompositionString("(1 OR 2)  ");

		CompositionCohortDefinition pedsInHivCareOnPreArtDrugsDuringPeriod = new CompositionCohortDefinition();
		pedsInHivCareOnPreArtDrugsDuringPeriod
				.setName("pedsInHivCareOnPreArtDrugsDuringPeriod");
		pedsInHivCareOnPreArtDrugsDuringPeriod.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		pedsInHivCareOnPreArtDrugsDuringPeriod.addParameter(new Parameter(
				"sqlEnd", "sqlEnd", Date.class));
		pedsInHivCareOnPreArtDrugsDuringPeriod.addParameter(new Parameter(
				"proDrugEnd", "proDrugEnd", Date.class));
		pedsInHivCareOnPreArtDrugsDuringPeriod.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(patientInHivProgramDuringPeriod,
						null));
		pedsInHivCareOnPreArtDrugsDuringPeriod
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								prophylaxisDrugsAsofDate,
								ParameterizableUtil
										.createParameterMappings("proDrugEnd=${endDate}")));
		pedsInHivCareOnPreArtDrugsDuringPeriod.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(onArvsAsOfDate,
						ParameterizableUtil
								.createParameterMappings("sqlEnd=${endDate}")));
		pedsInHivCareOnPreArtDrugsDuringPeriod.getSearches().put("4",
				new Mapped<CohortDefinition>(patienttransferedIn, null));
		pedsInHivCareOnPreArtDrugsDuringPeriod
				.setCompositionString(" 1 AND 2 AND (NOT 3) AND (NOT 4) ");

		CohortIndicator pedsInHivCareOnPreArtDrugsDuringPeriodInd = Indicators
				.newCohortIndicator(
						"pedsInHivCareOnPreArtDrugsDuringPeriodInd",
						pedsInHivCareOnPreArtDrugsDuringPeriod, null);

		// 7 Total number of new female adult patients (age 15 or more) enrolled
		// in HIV care
		// 8 Total number of new male adult patients (age 15 or more) enrolled
		// in HIV care
		// 9 Total number of pediatric patients (age <18 months) ever enrolled
		// in HIV care
		// 10 Total number of pediatric patients (age <5 years) ever enrolled in
		// HIV care
		// 11 Total number of female pediatric patients (age <15 years) ever
		// enrolled in HIV care
		// 12 Total number of male pediatric patients (age <15 years) ever
		// enrolled in HIV care

		CompositionCohortDefinition pedsAdultHivProgramsCombined = new CompositionCohortDefinition();
		pedsAdultHivProgramsCombined.setName("pedsAdultHivProgramsCombined");
		pedsAdultHivProgramsCombined.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		pedsAdultHivProgramsCombined.addParameter(new Parameter("onDate",
				"onDate", Date.class));
		pedsAdultHivProgramsCombined.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(pediatricsHIVProgram,
						ParameterizableUtil
								.createParameterMappings("onDate=${endDate}")));
		pedsAdultHivProgramsCombined.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(adultsHIVProgram,
						ParameterizableUtil
								.createParameterMappings("onDate=${endDate}")));
		pedsAdultHivProgramsCombined.setCompositionString(" (1 OR 2) ");

		CompositionCohortDefinition pedsEverInHivCareOnPreArtDrugs = new CompositionCohortDefinition();
		pedsEverInHivCareOnPreArtDrugs
				.setName("pedsEverInHivCareOnPreArtDrugs");
		pedsEverInHivCareOnPreArtDrugs.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		pedsEverInHivCareOnPreArtDrugs.addParameter(new Parameter("sqlEnd",
				"sqlEnd", Date.class));
		pedsEverInHivCareOnPreArtDrugs.addParameter(new Parameter("proDrugEnd",
				"proDrugEnd", Date.class));
		pedsEverInHivCareOnPreArtDrugs.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								pedsAdultHivProgramsCombined, null));
		pedsEverInHivCareOnPreArtDrugs
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								prophylaxisDrugsAsofDate,
								ParameterizableUtil
										.createParameterMappings("proDrugEnd=${endDate}")));
		pedsEverInHivCareOnPreArtDrugs.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(onArvsAsOfDate,
						ParameterizableUtil
								.createParameterMappings("sqlEnd=${endDate}")));
		pedsEverInHivCareOnPreArtDrugs
				.setCompositionString(" 1 AND 2 AND (NOT 3)");

		CohortIndicator pedsEverInHivCareOnPreArtDrugsInd = Indicators
				.newCohortIndicator("pedsEverInHivCareOnPreArtDrugsInd",
						pedsEverInHivCareOnPreArtDrugs, null);

		// 13 Total number of female adult patients (age 15 or older) ever
		// enrolled in HIV care

		CompositionCohortDefinition patientInHivOnCotrimoXazole = new CompositionCohortDefinition();
		patientInHivOnCotrimoXazole.setName("patientInHivOnCotrimoXazole");
		patientInHivOnCotrimoXazole.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientInHivOnCotrimoXazole.addParameter(new Parameter("sqlEnd",
				"sqlEnd", Date.class));
		patientInHivOnCotrimoXazole.addParameter(new Parameter("coEnd",
				"coEnd", Date.class));
		patientInHivOnCotrimoXazole.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								pedsAdultHivProgramsCombined, null));
		patientInHivOnCotrimoXazole.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(startedCotrimoXazole,
						ParameterizableUtil
								.createParameterMappings("coEnd=${endDate}")));
		patientInHivOnCotrimoXazole.getSearches().put("3",
				new Mapped<CohortDefinition>(ltfDuringPeriodComposition, null));
		patientInHivOnCotrimoXazole.getSearches().put(
				"4",
				new Mapped<CohortDefinition>(onArvsAsOfDate,
						ParameterizableUtil
								.createParameterMappings("sqlEnd=${endDate}")));
		patientInHivOnCotrimoXazole.getSearches().put("5",
				new Mapped<CohortDefinition>(exitedFromCare, null));
		patientInHivOnCotrimoXazole
				.setCompositionString("1 AND 2 AND (NOT (3 OR 4 OR 5)) ");

		CohortIndicator patientInHivOnCotrimoXazoleInd = Indicators
				.newCohortIndicator("patientInHivOnCotrimoXazoleInd",
						patientInHivOnCotrimoXazole, null);

		// 14 Total number of male adult patients (age 15 or older) ever
		// enrolled in HIV care
		// Number of new patients screened for active TB at enrollment this
		// month
		CompositionCohortDefinition patientInHivScreenedForTbDuringPeriod = new CompositionCohortDefinition();
		patientInHivScreenedForTbDuringPeriod
				.setName("patientInHivScreenedForTbDuringPeriod");
		patientInHivScreenedForTbDuringPeriod.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientInHivScreenedForTbDuringPeriod.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientInHivScreenedForTbDuringPeriod.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientInHivScreenedForTbDuringPeriod.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientInHivScreenedForTbDuringPeriod.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(patientInHivProgramDuringPeriod,
						null));
		patientInHivScreenedForTbDuringPeriod
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								tbTestResultDuringP,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		patientInHivScreenedForTbDuringPeriod.setCompositionString(" 1 AND 2 ");

		CohortIndicator patientInHivScreenedForTbDuringPeriodInd = Indicators
				.newCohortIndicator("patientInHivScreenedForTbDuringPeriodInd",
						patientInHivScreenedForTbDuringPeriod, null);

		// 15 Number of patients on Cotrimoxazole Prophylaxis this month
		// 18 Number of newly enrolled patients (age <15 years) who started TB
		// treatment this month
		// 19 Number of newly enrolled patients (age 15 or more years) who
		// started TB treatment this month
		// 20 Number of PRE-ARV patients who have died this month
		// 21 Number of PRE-ARV patients who have been transferred in this month

		CohortIndicator patientsInHivTreatmentThisMonthInd = Indicators
				.newCohortIndicator("patientsInHivTreatmentThisMonthInd",
						patientInHivProgramDuringPeriod, null);

		// 16 Number of new patients screened for active TB at enrollment this
		// month
		// 17 Number of patients screened TB Positive at enrollment this month

		CompositionCohortDefinition patientsInHivWhoStartedTBTreatmentThisMonth = new CompositionCohortDefinition();
		patientsInHivWhoStartedTBTreatmentThisMonth
				.setName("patientsInHivWhoStartedTBTreatmentThisMonth");
		patientsInHivWhoStartedTBTreatmentThisMonth.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsInHivWhoStartedTBTreatmentThisMonth.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsInHivWhoStartedTBTreatmentThisMonth.addParameter(new Parameter(
				"tbStart", "tbStart", Date.class));
		patientsInHivWhoStartedTBTreatmentThisMonth.addParameter(new Parameter(
				"tbEnd", "tbEnd", Date.class));
		patientsInHivWhoStartedTBTreatmentThisMonth.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(patientInHivProgramDuringPeriod,
						null));
		patientsInHivWhoStartedTBTreatmentThisMonth
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								tbScreenngPosTest,
								ParameterizableUtil
										.createParameterMappings("onOrBefore=${endDate}")));
		patientsInHivWhoStartedTBTreatmentThisMonth
				.getSearches()
				.put("3",
						new Mapped<CohortDefinition>(
								patientOnTBTreatment,
								ParameterizableUtil
										.createParameterMappings("tbStart=${startDate},tbEnd=${endDate}")));
		patientsInHivWhoStartedTBTreatmentThisMonth
				.setCompositionString("1 AND 2 AND 3 ");

		CohortIndicator patientsInHivWhoStartedTBTreatmentThisMonthInd = Indicators
				.newCohortIndicator(
						"patientsInHivWhoStartedTBTreatmentThisMonthInd",
						patientsInHivWhoStartedTBTreatmentThisMonth, null);

		// 22 Number of PRE-ARV patients who have been transferred out this
		// month
		// 23 Number of PRE-ARV patients lost to followup (>3 months)
		CompositionCohortDefinition preArtpatientLostToFolowupThisMonth = new CompositionCohortDefinition();
		preArtpatientLostToFolowupThisMonth
				.setName("preArtpatientLostToFolowupThisMonth");
		preArtpatientLostToFolowupThisMonth.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(patientInHivProgramDuringPeriod,
						null));
		preArtpatientLostToFolowupThisMonth.getSearches().put("2",
				new Mapped<CohortDefinition>(ltfDuringPeriodComposition, null));
		preArtpatientLostToFolowupThisMonth.setCompositionString("1 AND 2 ");

		CohortIndicator preArtpatientLostToFolowupThisMonthInd = Indicators
				.newCohortIndicator("preArtpatientLostToFolowupThisMonthInd",
						preArtpatientLostToFolowupThisMonth, null);

		// -----------------------------------------------------------------------------------------------------------------------------------------------
		// ART DATA ELEMENTS
		// -----------------------------------------------------------------------------------------------------------------------------------------------

		// 1 Total number of pediatric patients (age <18 months) who are
		// currently on ARV treatment
		// 2 Total number of pediatric patients (age <5 years) who are currently
		// on ARV treatment
		// 3 Total number of female pediatric patients (age <15 years) who are
		// currently on ARV treatment
		// 4 Total number of male pediatric patients (age <15 years) who are
		// currently on ARV treatment

		CompositionCohortDefinition artpedOninhivWthGenericStatus = new CompositionCohortDefinition();
		artpedOninhivWthGenericStatus.setName("artpedOninhivWthGenericStatus");
		artpedOninhivWthGenericStatus.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								pedsAdultHivProgramsCombined, null));
		artpedOninhivWthGenericStatus.getSearches().put("2",
				new Mapped<CohortDefinition>(patientsTransferredOut, null));
		artpedOninhivWthGenericStatus.getSearches().put("3",
				new Mapped<CohortDefinition>(patientDied, null));
		artpedOninhivWthGenericStatus
				.setCompositionString("1 AND (NOT 2) AND (NOT 3) ");

		CompositionCohortDefinition artpatientOnArvInProgram = new CompositionCohortDefinition();
		artpatientOnArvInProgram.setName("artpatientOnArvInProgram");
		artpatientOnArvInProgram.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		artpatientOnArvInProgram.addParameter(new Parameter("sqlEnd", "sqlEnd",
				Date.class));
		artpatientOnArvInProgram.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(onArvsAsOfDate,
						ParameterizableUtil
								.createParameterMappings("sqlEnd=${endDate}")));
		artpatientOnArvInProgram.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(artpedOninhivWthGenericStatus,
						null));
		artpatientOnArvInProgram.setCompositionString("1 AND 2 ");

		CohortIndicator artpatientOnArvInProgramInd = Indicators
				.newCohortIndicator("artpatientOnArvInProgramInd",
						artpatientOnArvInProgram, null);

		// 5 Total number of pediatric patients who are on First Line Regimen

		CompositionCohortDefinition artpatientOnFirstlineRegInHiv = new CompositionCohortDefinition();
		artpatientOnFirstlineRegInHiv.setName("artpatientOnFirstlineRegInHiv");
		artpatientOnFirstlineRegInHiv.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		artpatientOnFirstlineRegInHiv.addParameter(new Parameter("sqlEnd",
				"sqlEnd", Date.class));
		artpatientOnFirstlineRegInHiv
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								patientOnFirstLineRegimen,
								ParameterizableUtil
										.createParameterMappings("endDate=${endDate}")));
		artpatientOnFirstlineRegInHiv.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(artpedOninhivWthGenericStatus,
						null));
		artpatientOnFirstlineRegInHiv.getSearches().put("3",
				new Mapped<CohortDefinition>(ltfDuringPeriodComposition, null));
		artpatientOnFirstlineRegInHiv.getSearches().put(
				"4",
				new Mapped<CohortDefinition>(onArvsAsOfDate,
						ParameterizableUtil
								.createParameterMappings("sqlEnd=${endDate}")));
		artpatientOnFirstlineRegInHiv
				.setCompositionString("1 AND 2 AND (NOT 3) AND (NOT 4)  ");

		CohortIndicator artPedspatientOnFirstlineRegInHivInd = Indicators
				.newCohortIndicator("artpatientOnFirstlineRegInHivInd",
						artpatientOnFirstlineRegInHiv, null);

		// 6 Total number of pediatric patients who are on second Line Regimen

		CompositionCohortDefinition artpatientOnSecondlineRegInHiv = new CompositionCohortDefinition();
		artpatientOnSecondlineRegInHiv
				.setName("artpatientOnSecondlineRegInHiv");
		artpatientOnSecondlineRegInHiv.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		artpatientOnSecondlineRegInHiv.addParameter(new Parameter("sqlEnd",
				"sqlEnd", Date.class));
		artpatientOnSecondlineRegInHiv
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								patientOnSecondLineRegimen,
								ParameterizableUtil
										.createParameterMappings("endDate=${endDate}")));
		artpatientOnSecondlineRegInHiv.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(artpedOninhivWthGenericStatus,
						null));
		artpatientOnSecondlineRegInHiv.getSearches().put("3",
				new Mapped<CohortDefinition>(ltfDuringPeriodComposition, null));
		artpatientOnSecondlineRegInHiv.getSearches().put(
				"4",
				new Mapped<CohortDefinition>(onArvsAsOfDate,
						ParameterizableUtil
								.createParameterMappings("sqlEnd=${endDate}")));
		artpatientOnSecondlineRegInHiv
				.setCompositionString("1 AND 2 AND (NOT 3) AND (NOT 4) ");

		CohortIndicator artPedspatientOnSecondlineRegInHivInd = Indicators
				.newCohortIndicator("artPedspatientOnSecondlineRegInHivInd",
						artpatientOnSecondlineRegInHiv, null);

		// 7 Total number of female adult patients (age 15 or older) who are
		// currently on ARV treatment
		// 8 Total number of male adult patients (age 15 or older) who are
		// currently on ARV treatment

		CompositionCohortDefinition artAdultsOninhivWthGenericStatus = new CompositionCohortDefinition();
		artAdultsOninhivWthGenericStatus
				.setName("artAdultsOninhivWthGenericStatus");
		artAdultsOninhivWthGenericStatus.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(artpedOninhivWthGenericStatus,
						null));
		artAdultsOninhivWthGenericStatus.getSearches().put("2",
				new Mapped<CohortDefinition>(ltfDuringPeriodComposition, null));
		artAdultsOninhivWthGenericStatus.setCompositionString("1 AND (NOT 2) ");

		CompositionCohortDefinition artadultspatientOnArvdrugsInHiv = new CompositionCohortDefinition();
		artadultspatientOnArvdrugsInHiv
				.setName("artadultspatientOnArvdrugsInHiv");
		artadultspatientOnArvdrugsInHiv.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		artadultspatientOnArvdrugsInHiv.addParameter(new Parameter("sqlEnd",
				"sqlEnd", Date.class));
		artadultspatientOnArvdrugsInHiv.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(onArvsAsOfDate,
						ParameterizableUtil
								.createParameterMappings("sqlEnd=${endDate}")));
		artadultspatientOnArvdrugsInHiv.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(artAdultsOninhivWthGenericStatus,
						null));
		artadultspatientOnArvdrugsInHiv.setCompositionString("1 AND 2 ");

		CohortIndicator artadultspatientOnArvdrugsInHivInd = Indicators
				.newCohortIndicator("artadultspatientOnArvdrugsInHivInd",
						artadultspatientOnArvdrugsInHiv, null);

		// 9 Total number of adult patients who are on First Line Regimen

		CompositionCohortDefinition artadultspatientOnFirstlineRegInHiv = new CompositionCohortDefinition();
		artadultspatientOnFirstlineRegInHiv
				.setName("artadultspatientOnFirstlineRegInHiv");
		artadultspatientOnFirstlineRegInHiv.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		artadultspatientOnFirstlineRegInHiv
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								patientOnFirstLineRegimen,
								ParameterizableUtil
										.createParameterMappings("endDate=${endDate}")));
		artadultspatientOnFirstlineRegInHiv.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(artAdultsOninhivWthGenericStatus,
						null));
		artadultspatientOnFirstlineRegInHiv.setCompositionString("1 AND 2 ");

		CohortIndicator artadultspatientOnFirstlineRegInHivInd = Indicators
				.newCohortIndicator("artadultspatientOnFirstlineRegInHivInd",
						artadultspatientOnFirstlineRegInHiv, null);

		// 10 Total number of adult patients who are on Second Line Regimen

		CompositionCohortDefinition artadultspatientOnSecondlineRegInHiv = new CompositionCohortDefinition();
		artadultspatientOnSecondlineRegInHiv
				.setName("artadultspatientOnSecondlineRegInHiv");
		artadultspatientOnSecondlineRegInHiv.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		artadultspatientOnSecondlineRegInHiv
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								patientOnSecondLineRegimen,
								ParameterizableUtil
										.createParameterMappings("endDate=${endDate}")));
		artadultspatientOnSecondlineRegInHiv.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(artAdultsOninhivWthGenericStatus,
						null));
		artadultspatientOnSecondlineRegInHiv.setCompositionString("1 AND 2 ");

		CohortIndicator artadultspatientOnSecondlineRegInHivInd = Indicators
				.newCohortIndicator("artadultspatientOnSecondlineRegInHivInd",
						artadultspatientOnSecondlineRegInHiv, null);

		// 11 Number of new pediatric patients (<18 months) starting ARV
		// treatment this month
		// 12 Number of new pediatric patients (age <5 years) starting ARV
		// treatment this month
		// 13 Number of new female pediatric patients (age <15 years) starting
		// ARV treatment this month
		// 14 Number of new female pediatric patients (age <15 years) starting
		// ARV treatment this month

		CompositionCohortDefinition artpedNewOninhivWthWhoStages = new CompositionCohortDefinition();
		artpedNewOninhivWthWhoStages.setName("artpedNewOninhivWthWhoStages");
		artpedNewOninhivWthWhoStages.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		artpedNewOninhivWthWhoStages.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		artpedNewOninhivWthWhoStages.addParameter(new Parameter("sqlStart",
				"sqlStart", Date.class));
		artpedNewOninhivWthWhoStages.addParameter(new Parameter("sqlEnd",
				"sqlEnd", Date.class));
		artpedNewOninhivWthWhoStages.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(patientInHivProgramDuringPeriod,
						null));
		artpedNewOninhivWthWhoStages
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								onArvsAsOfDateDuringP,
								ParameterizableUtil
										.createParameterMappings("sqlStart=${startDate},sqlEnd=${endDate}")));
		artpedNewOninhivWthWhoStages.setCompositionString("1 AND 2");

		CompositionCohortDefinition artpedNewOninhivWthGenericStatus = new CompositionCohortDefinition();
		artpedNewOninhivWthGenericStatus
				.setName("artpedNewOninhivWthGenericStatus");
		artpedNewOninhivWthGenericStatus.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								artpedNewOninhivWthWhoStages, null));
		artpedNewOninhivWthGenericStatus.getSearches().put("2",
				new Mapped<CohortDefinition>(patienttransferedIn, null));
		artpedNewOninhivWthGenericStatus.setCompositionString("1 AND (NOT 2) ");

		CohortIndicator artpedNewOninhivWthGenericStatusInd = Indicators
				.newCohortIndicator("artpedNewOninhivWthGenericStatusInd",
						artpedNewOninhivWthGenericStatus, null);

		// 15 Number of new pediatric patients who are WHO stage 4 this month
		// 16 Number of new pediatric patients who are WHO stage 3 this month
		// 17 Number of new pediatric patients who are WHO stage 2 this month
		// 18 Number of new pediatric patients who are WHO stage 1 this month
		// 19 Number of new pediatric patients whose WHO Stage is undefined this
		// month
		// 22 Number of new adult patients who are WHO stage 4 this month
		// 23 Number of new adult patients who are WHO stage 3 this month
		// 24 Number of new adult patients who are WHO stage 2 this month
		// 25 Number of new adult patients who are WHO stage 1 this month
		// 26 Number of new adult patients who are WHO stage undefined this
		// month
		// only dimensions will be diferent

		CohortIndicator artpedNewOninhivWthWhoStagesInd = Indicators
				.newCohortIndicator("artpedNewOninhivWthWhoStagesInd",
						artpedNewOninhivWthWhoStages, null);

		// 20 Number of new female adult patients (age 15 or more) starting ARV
		// treatment this month
		// 21 Number of new male adult patients (age 15 or more) starting ARV
		// treatment this month

		CompositionCohortDefinition artadultsNewOninhivWithGenericStatus = new CompositionCohortDefinition();
		artadultsNewOninhivWithGenericStatus
				.setName("artadultsNewOninhivWithGenericStatus");
		artadultsNewOninhivWithGenericStatus.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								artpedNewOninhivWthWhoStages, null));
		artadultsNewOninhivWithGenericStatus.getSearches().put("2",
				new Mapped<CohortDefinition>(patienttransferedIn, null));
		artadultsNewOninhivWithGenericStatus
				.setCompositionString("1 AND (NOT 2) ");

		CohortIndicator artadultsNewOninhivWithGenericStatusInd = Indicators
				.newCohortIndicator("artadultsNewOninhivWithGenericStatusInd",
						artadultsNewOninhivWithGenericStatus, null);

		// 27 Number of ARV patients (age <15) who have had their treatment
		// interrupted this month
		// 28 Number of ARV patients (age 15 or more) who have had their
		// treatment interrupted this month

		SqlCohortDefinition stoppedArvDuringP = new SqlCohortDefinition();
		stoppedArvDuringP.setName("stoppedArvDuringP");
		stoppedArvDuringP
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.uuid in ("
						+ patientOnARTDrugs
						+ ") and o.discontinued=1 and o.voided=0 and  o.start_date>=:startArt and o.start_date<= :stopArt");
		stoppedArvDuringP.addParameter(new Parameter("startArt", "startArt",
				Date.class));
		stoppedArvDuringP.addParameter(new Parameter("stopArt", "stopArt",
				Date.class));

		CompositionCohortDefinition stoppedArvsDuringPeriodComposition = new CompositionCohortDefinition();
		stoppedArvsDuringPeriodComposition
				.setName("Patients who stoped ART during the period");
		stoppedArvsDuringPeriodComposition.addParameter(new Parameter(
				"startDate", "StartDate", Date.class));
		stoppedArvsDuringPeriodComposition.addParameter(new Parameter(
				"endDate", "EndDate", Date.class));
		stoppedArvsDuringPeriodComposition.addParameter(new Parameter(
				"startArt", "startArt", Date.class));
		stoppedArvsDuringPeriodComposition.addParameter(new Parameter(
				"stopArt", "stopArt", Date.class));
		stoppedArvsDuringPeriodComposition.addParameter(new Parameter("sqlEnd",
				"sqlEnd", Date.class));
		stoppedArvsDuringPeriodComposition
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								stoppedArvDuringP,
								ParameterizableUtil
										.createParameterMappings("startArt=${startDate},stopArt=${endDate}")));
		stoppedArvsDuringPeriodComposition.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(onArvsAsOfDate,
						ParameterizableUtil
								.createParameterMappings("sqlEnd=${endDate}")));
		stoppedArvsDuringPeriodComposition
				.setCompositionString("1 AND (NOT 2)");

		CompositionCohortDefinition artpedsWhoseDrugsinteruptedThismont = new CompositionCohortDefinition();
		artpedsWhoseDrugsinteruptedThismont
				.setName("artpedsWhoseDrugsinteruptedThismont");
		artpedsWhoseDrugsinteruptedThismont.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								pedsAdultHivProgramsCombined, null));
		artpedsWhoseDrugsinteruptedThismont.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(
						stoppedArvsDuringPeriodComposition, null));
		artpedsWhoseDrugsinteruptedThismont.setCompositionString("1 AND 2 ");

		CohortIndicator artpedsWhoseDrugsinteruptedThismontInd = Indicators
				.newCohortIndicator("artpedsWhoseDrugsinteruptedThismontInd",
						artpedsWhoseDrugsinteruptedThismont, null);

		// 29 Number of ARV patients (age <15) who have died this month
		// 30 Number of ARV patients (age 15 or more) who have died this month
		// 35 Number of ARV patients (age <15) who have been transferred out
		// this month
		// 36 Number of ARV patients (age 15 or more) who have been transferred
		// out this month
		// 37 Number of ARV patients (age <15) who have been transferred in this
		// month
		// 38 Number of ARV patients (age 15 or more) who have been transferred
		// in this month

		CompositionCohortDefinition inhivProgamOnARVDiedTrasgerInorOut = new CompositionCohortDefinition();
		inhivProgamOnARVDiedTrasgerInorOut
				.setName("inhivProgamOnARVDiedTrasgerInorOut");
		inhivProgamOnARVDiedTrasgerInorOut.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		inhivProgamOnARVDiedTrasgerInorOut.addParameter(new Parameter("sqlEnd",
				"sqlEnd", Date.class));
		inhivProgamOnARVDiedTrasgerInorOut.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								pedsAdultHivProgramsCombined, null));
		inhivProgamOnARVDiedTrasgerInorOut.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(onArvsAsOfDate,
						ParameterizableUtil
								.createParameterMappings("sqlEnd=${endDate}")));
		inhivProgamOnARVDiedTrasgerInorOut.setCompositionString("1 AND 2");

		CohortIndicator inhivProgamOnARVDiedTrasgerInorOutInd = Indicators
				.newCohortIndicator("inhivProgamOnARVDiedTrasgerInorOutInd",
						inhivProgamOnARVDiedTrasgerInorOut, null);

		// 31 Number of ARV patients (age <15) lost to followup (>3 months)
		// 32 Number of ARV patients (age 15 or more) lost to followup (>3
		// months)

		CompositionCohortDefinition artPatientInHivOnArvLost = new CompositionCohortDefinition();
		artPatientInHivOnArvLost.setName("artPatientInHivOnArvLost");
		artPatientInHivOnArvLost.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(
						inhivProgamOnARVDiedTrasgerInorOut, null));
		artPatientInHivOnArvLost.getSearches().put("2",
				new Mapped<CohortDefinition>(ltfDuringPeriodComposition, null));
		artPatientInHivOnArvLost.getSearches().put("3",
				new Mapped<CohortDefinition>(patientsTransferredOut, null));
		artPatientInHivOnArvLost.setCompositionString("1 AND 2 AND (NOT 3)");

		CohortIndicator artPatientInHivOnArvLostInd = Indicators
				.newCohortIndicator("artPatientInHivOnArvLostInd",
						artPatientInHivOnArvLost, null);

		// 33 Number of male patients on treatment 12 months after initiation of
		// ARVs this month
		// 34 Number of female patients on treatment 12 months after initiation
		// of ARVs this month

		SqlCohortDefinition stoppedAtyearAgo = new SqlCohortDefinition();
		stoppedAtyearAgo.setName("stoppedAtyearAgo");
		stoppedAtyearAgo
				.setQuery("select distinct o.patient_id from orders o,concept c where o.concept_id=c.concept_id  and c.uuid in ("
						+ patientOnARTDrugs
						+ ") and o.discontinued=0 and DATEDIFF( :drugStartDate, o.start_date) > '"
						+ Context.getAdministrationService().getGlobalProperty(
								"rwandareports.tracnetreport.moreThan12Months")
						+ "' and o.voided=0");
		stoppedAtyearAgo.addParameter(new Parameter("drugStartDate",
				"drugStartDate", Date.class));

		CompositionCohortDefinition artPAtientStartedAyearAgo = new CompositionCohortDefinition();
		artPAtientStartedAyearAgo.setName("artPAtientStartedAyearAgo");
		artPAtientStartedAyearAgo.addParameter(new Parameter("drugStartDate",
				"drugStartDate", Date.class));
		artPAtientStartedAyearAgo.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		artPAtientStartedAyearAgo
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								stoppedAtyearAgo,
								ParameterizableUtil
										.createParameterMappings("drugStartDate=${startDate}")));
		artPAtientStartedAyearAgo.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								pedsAdultHivProgramsCombined, null));
		artPAtientStartedAyearAgo.setCompositionString("1 AND 2 ");

		CohortIndicator artPAtientStartedAyearAgoInd = Indicators
				.newCohortIndicator("artPAtientStartedAyearAgoInd",
						artPAtientStartedAyearAgo, null);

		// -----------------------------------------------------------------------------------------------------------------------------------------------
		// STI DATA ELEMENTS
		// -----------------------------------------------------------------------------------------------------------------------------------------------

		// 1 Number of clients who received councelling and screening for STIs
		// this month

		CodedObsCohortDefinition ScreenedForSti = new CodedObsCohortDefinition();
		ScreenedForSti.setName("ScreenedForSti");
		ScreenedForSti.setTimeModifier(TimeModifier.LAST);
		ScreenedForSti
				.setQuestion(Context
						.getConceptService()
						.getConceptByName(
								"CURRENT OPPORTUNISTIC INFECTION OR COMORBIDITY, CONFIRMED OR PRESUMED"));
		ScreenedForSti.addParameter(new Parameter("onOrAfter", "onOrAfter",
				Date.class));
		ScreenedForSti.addParameter(new Parameter("onOrBefore", "onOrBefore",
				Date.class));

		CompositionCohortDefinition patientsCounseledAndScreenedForSti = new CompositionCohortDefinition();
		patientsCounseledAndScreenedForSti
				.setName("patientsCounseledAndScreenedForSti");
		patientsCounseledAndScreenedForSti.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientsCounseledAndScreenedForSti.addParameter(new Parameter(
				"endDate", "endDate", Date.class));
		patientsCounseledAndScreenedForSti.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientsCounseledAndScreenedForSti.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientsCounseledAndScreenedForSti.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								pedsAdultHivProgramsCombined, null));
		patientsCounseledAndScreenedForSti
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								ScreenedForSti,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		patientsCounseledAndScreenedForSti.setCompositionString("1 AND 2");

		CohortIndicator patientsCounseledAndScreenedForStiInd = Indicators
				.newCohortIndicator("patientsCounseledAndScreenedForStiInd",
						patientsCounseledAndScreenedForSti, null);

		// 2 Number of STI cases diagnosed and treated this month

		InStateCohortDefinition treatmentStateCohortDefinition = Cohorts
				.createInProgramStateParameterizableByDate(
						"treatmentStateCohortDefinition", folowingState);

		CompositionCohortDefinition patientsScreenedForStiAndOnTreatment = new CompositionCohortDefinition();
		patientsScreenedForStiAndOnTreatment
				.setName("patientsScreenedForStiAndOnTreatment");
		patientsScreenedForStiAndOnTreatment.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(
						patientsCounseledAndScreenedForSti, null));
		patientsScreenedForStiAndOnTreatment.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(treatmentStateCohortDefinition,
						null));
		patientsScreenedForStiAndOnTreatment.setCompositionString("1 AND 2  ");

		CohortIndicator patientsScreenedForStiAndOnTreatmentInd = Indicators
				.newCohortIndicator("patientsScreenedForStiAndOnTreatmentInd",
						patientsScreenedForStiAndOnTreatment, null);

		// 3 Number of opportunistic infection cases treated, excluding TB, this
		// month

		CodedObsCohortDefinition tbinfectionOpportunistic = Cohorts
				.createCodedObsCohortDefinition("tbinfectionOpportunistic",
						opportunisticInfection, stiScreenedTb,
						SetComparator.IN, TimeModifier.LAST);
		CompositionCohortDefinition tbinfectionOpportunisticComp = new CompositionCohortDefinition();

		tbinfectionOpportunisticComp
				.setName("patientsScreenedForStiAndOnTreatment");
		tbinfectionOpportunisticComp.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(
						patientsCounseledAndScreenedForSti, null));
		tbinfectionOpportunisticComp.getSearches().put("2",
				new Mapped<CohortDefinition>(tbinfectionOpportunistic, null));
		tbinfectionOpportunisticComp.setCompositionString("1 AND ( NOT 2)  ");

		CohortIndicator tbinfectionOpportunisticCompInd = Indicators
				.newCohortIndicator("tbinfectionOpportunisticCompInd",
						tbinfectionOpportunisticComp, null);
		// -----------------------------------------------------------------------------------------------------------------------------------------------
		// MALNUTRITION DATA ELEMENT
		// -----------------------------------------------------------------------------------------------------------------------------------------------

		// Will map those indicators properly in June after the creation of
		// proper Z-Score
		// 1 Number of pediatric patients (age < 5 years) with severe
		// malnutrition this month 1

		NumericObsCohortDefinition weightForAgeCohortDefinition = new NumericObsCohortDefinition();
		weightForAgeCohortDefinition.setName(" weightForAgeCohortDefinition");
		weightForAgeCohortDefinition.setQuestion(Context.getConceptService()
				.getConceptByName("Weight for age z-score"));
		weightForAgeCohortDefinition.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		weightForAgeCohortDefinition.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		weightForAgeCohortDefinition.setTimeModifier(TimeModifier.LAST);
		weightForAgeCohortDefinition.setOperator1(RangeComparator.GREATER_THAN);
		weightForAgeCohortDefinition.setValue1(0.0);

		NumericObsCohortDefinition weightForHeightCohortDefinition = new NumericObsCohortDefinition();
		weightForHeightCohortDefinition
				.setName("weightForHeightCohortDefinition");
		weightForHeightCohortDefinition.setQuestion(Context.getConceptService()
				.getConceptByName("Weight for height percentile"));
		weightForHeightCohortDefinition.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		weightForHeightCohortDefinition.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		weightForHeightCohortDefinition.setTimeModifier(TimeModifier.LAST);
		weightForHeightCohortDefinition
				.setOperator1(RangeComparator.GREATER_THAN);
		weightForHeightCohortDefinition.setValue1(0.0);

		CompositionCohortDefinition pedswithSevereNutrition = new CompositionCohortDefinition();
		pedswithSevereNutrition.setName("pedswithSevereNutrition");
		pedswithSevereNutrition.addParameter(new Parameter("startDate",
				"startDate", Date.class));
		pedswithSevereNutrition.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		pedswithSevereNutrition.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		pedswithSevereNutrition.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));
		pedswithSevereNutrition
				.getSearches()
				.put("1",
						new Mapped<CohortDefinition>(
								weightForAgeCohortDefinition,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		pedswithSevereNutrition
				.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								weightForHeightCohortDefinition,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		pedswithSevereNutrition.setCompositionString("1 OR 2 ");

		CohortIndicator pedswithSevereNutritionInd = Indicators
				.newCohortIndicator("pedswithSevereNutritionInd",
						pedswithSevereNutrition, null);

		// 2 Number of pediatric patients (age < 5 years) with severe
		// malnutrition who received therapeutic or nutritional supplementation
		// this month
		// 3 Number of patients (age < 15 years) who received therapeutic or
		// nutritional supplementation this month
		// 4 Number of patients (age 15 or more years) who received therapeutic
		// or nutritional supplementation this month 1

		CodedObsCohortDefinition patientReceivedAnyFoodPackage = new CodedObsCohortDefinition();
		patientReceivedAnyFoodPackage.setName("patientReceivedAnyFoodPackage");
		patientReceivedAnyFoodPackage.setTimeModifier(TimeModifier.LAST);
		patientReceivedAnyFoodPackage.setQuestion(Context.getConceptService()
				.getConceptByName("PATIENT RECEIVED FOOD PACKAGE"));
		patientReceivedAnyFoodPackage.addParameter(new Parameter("onOrAfter",
				"onOrAfter", Date.class));
		patientReceivedAnyFoodPackage.addParameter(new Parameter("onOrBefore",
				"onOrBefore", Date.class));

		CompositionCohortDefinition patientReceivedAnyFoodPackageComp = new CompositionCohortDefinition();
		patientReceivedAnyFoodPackageComp
				.setName("patientReceivedAnyFoodPackageComp");
		patientReceivedAnyFoodPackageComp.addParameter(new Parameter(
				"startDate", "startDate", Date.class));
		patientReceivedAnyFoodPackageComp.addParameter(new Parameter("endDate",
				"endDate", Date.class));
		patientReceivedAnyFoodPackageComp.addParameter(new Parameter(
				"onOrAfter", "onOrAfter", Date.class));
		patientReceivedAnyFoodPackageComp.addParameter(new Parameter(
				"onOrBefore", "onOrBefore", Date.class));
		patientReceivedAnyFoodPackageComp.getSearches().put("1",
				new Mapped<CohortDefinition>(pedswithSevereNutrition, null));
		patientReceivedAnyFoodPackageComp.getSearches()
				.put("2",
						new Mapped<CohortDefinition>(
								infantBornToHIvMotherExposed, null));
		patientReceivedAnyFoodPackageComp
				.getSearches()
				.put("3",
						new Mapped<CohortDefinition>(
								patientReceivedAnyFoodPackage,
								ParameterizableUtil
										.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}")));
		patientReceivedAnyFoodPackageComp
				.setCompositionString("1 OR 3 AND (NOT 2) ");

		CohortIndicator patientReceivedAnyFoodPackageInd = Indicators
				.newCohortIndicator("patientReceivedAnyFoodPackageInd",
						patientReceivedAnyFoodPackageComp, null);

		CohortIndicator malnourishedWitadherenceCounselingInd = Indicators
				.newCohortIndicator(
						"malnourishedWitadherenceCounselingInd",
						adherenceCounseling,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		// -----------------------------------------------------------------------------------------------------------------------------------------------------------
		// Gender,Age and Program Dimensions
		CohortDefinitionDimension genderDimension = new CohortDefinitionDimension();
		genderDimension.addParameter(ReportingConstants.START_DATE_PARAMETER);
		genderDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
		genderDimension.setName("gender");
		genderDimension.addCohortDefinition("male", male, null);
		genderDimension.addCohortDefinition("female", female, null);

		CohortDefinitionDimension ageDimension = new CohortDefinitionDimension();
		ageDimension.addParameter(ReportingConstants.START_DATE_PARAMETER);
		ageDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
		ageDimension.setName("age");
		ageDimension.addCohortDefinition("at6weeks", at6WeeksOfAge,
				ParameterizableUtil
						.createParameterMappings("effectiveDate=${endDate}"));
		ageDimension.addCohortDefinition("at9months", at9monthsOfAge,
				ParameterizableUtil
						.createParameterMappings("effectiveDate=${endDate}"));
		ageDimension.addCohortDefinition("at18months", at18monthsOfAge,
				ParameterizableUtil
						.createParameterMappings("effectiveDate=${endDate}"));
		ageDimension.addCohortDefinition("lessThan5", lessThan5, null);
		ageDimension.addCohortDefinition("underFifteen", underFifteen, null);
		ageDimension.addCohortDefinition("moreThan15", patientMoreThan15, null);
		ageDimension.addCohortDefinition("age15To49", age15To49, null);

		CohortDefinitionDimension patientStatus = new CohortDefinitionDimension();
		patientStatus.addParameter(ReportingConstants.START_DATE_PARAMETER);
		patientStatus.addParameter(ReportingConstants.END_DATE_PARAMETER);
		patientStatus.setName("status");
		patientStatus.addCohortDefinition("hivWitAnyRes", hivTestWithAnyResult,
				null);
		patientStatus.addCohortDefinition("hivPos", hivTestwithPosStatus, null);
		patientStatus.addCohortDefinition("hivNeg", hivTestwithNegStatus, null);
		patientStatus
				.addCohortDefinition(
						"hivPosDuringPeriod",
						hivposthismonth,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"hivUndet",
						hivUndeterminateDuringperiod,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"hivTestDuringP",
						hivTestWithThisMonth,
						ParameterizableUtil
								.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"died",
						patientDied,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"out",
						patientsTransferredOut,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"comein",
						patienttransferedIn,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"tbpos",
						tbScreenngPosTest,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"tbtest",
						tbTestResultDuringP,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"whostage4p",
						whostagefourped,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"whostage3p",
						whostagethreeped,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"whostage2p",
						whostagetwoped,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"whostage1p",
						whostageoneped,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"whostageX",
						whostageundeterminate,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"whostage4a",
						whostagefourad,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"whostage3a",
						whostagethreead,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"whostage2a",
						whostagetwoad,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"whostage1a",
						whostageonead,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"testRPR",
						testForRpr,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		patientStatus
				.addCohortDefinition(
						"rprPos",
						testForRprDuringP,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		// Add dimensions to the report
		rd.addDimension(
				"gender",
				genderDimension,
				ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		rd.addDimension(
				"age",
				ageDimension,
				ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		rd.addDimension(
				"status",
				patientStatus,
				ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}"));

		// PEP Data Elements
		rd.addIndicator(
				"1.1",
				"rwandareports.tracnetreport.indicator.newAtRiskHivOccupationExposure",
				newAtRiskHivOccupationExposureIndicator);
		rd.addIndicator(
				"1.2",
				"rwandareports.tracnetreport.indicator.newAtRiskHivRapeAssault",
				newAtRiskHivRapeAssaultIndicator);
		rd.addIndicator(
				"1.3",
				"rwandareports.tracnetreport.indicator.newAtRiskHivOtherNoneOccupationExposure",
				newAtRiskHivOtherNoneOccupationExposureIndicator);
		rd.addIndicator(
				"1.4",
				"rwandareports.tracnetreport.indicator.newAtRiskHivOccupationExposurePep",
				newAtRiskHivOccupationExposurePepIndicator);
		rd.addIndicator(
				"1.5",
				"rwandareports.tracnetreport.indicator.newAtRiskHivRapeAssaultPep",
				sexualAssaultInPepInd);
		rd.addIndicator(
				"1.6",
				"rwandareports.tracnetreport.indicator.newAtRiskHivOtherNoneOccupationExposurePep",
				noneoCupExposuretInPepInd);
		rd.addIndicator(
				"1.7",
				"rwandareports.tracnetreport.indicator.newAtRiskHivOccupExpo3MonthAfterPep",
				newAtRiskHivOccupExpo3MonthAfterPepInd, "status=hivWitAnyRes");
		rd.addIndicator(
				"1.8",
				"rwandareports.tracnetreport.indicator.newAtRiskHivRapeAssault3MonthAfterPep",
				sexualasaultDrugsAfter3motnhsInd, "status=hivWitAnyRes");
		rd.addIndicator(
				"1.9",
				"rwandareports.tracnetreport.indicator.newAtRiskHivOtherNoneOccupExpo3MonthAfterPep",
				onthernoneOccuExpDrugsAfter3motnhsInd, "status=hivWitAnyRes");
		// PMTCT CPN
		rd.addIndicator(
				"2.1",
				"rwandareports.tracnetreport.indicator.womenUnknownHivFirstAntenatal",
				pregnantWomenwithConsultationIndicator, "gender=female");
		rd.addIndicator(
				"2.2",
				"rwandareports.tracnetreport.indicator.womenKnownHivPosFirstAntenatal",
				pregnantHivWomenwithConsultationInd,
				"gender=female|status=hivPos");
		rd.addIndicator("2.3",
				"rwandareports.tracnetreport.indicator.womenUnknownHivTested",
				pregnantWomenUpToDateInd, "gender=female|status=hivTestDuringP");
		rd.addIndicator("2.4",
				"rwandareports.tracnetreport.indicator.womenHivPosReturnRes",
				womenHivPosReturnResIndicator, "gender=female|status=hivPos");
		rd.addIndicator("2.5",
				"rwandareports.tracnetreport.indicator.womenHivPosTestedCd4",
				womenWithHivPosAndCD4CountTestIndicator, "gender=female");
		rd.addIndicator(
				"2.6",
				"rwandareports.tracnetreport.indicator.pregnantHivPosEligibleArvs1",
				pregnantWomenEligibleonArtForLifeIndicator,
				"gender=female|status=hivPos");
		rd.addIndicator("2.7",
				"rwandareports.tracnetreport.indicator.negativeWomenReturnRes",
				womenHivPosReturnResIndicator, "gender=female|status=hivNeg");
		rd.addIndicator("2.8",
				"rwandareports.tracnetreport.indicator.pregnantHivPos",
				pregnantWomenUpToDateInd,
				"gender=female|status=hivPosDuringPeriod");
		rd.addIndicator(
				"2.9",
				"rwandareports.tracnetreport.indicator.pregnantHivPosAztProphyAt28Weeks",
				pregnatOnCotrimoAt28WeeksIndicator,
				"gender=female|status=hivPos");
		rd.addIndicator(
				"2.10",
				"rwandareports.tracnetreport.indicator.pregnantHivPosTripleTheraProphy",
				pregnantWomenEligibleonTripleTherapyIndicator,
				"gender=female|status=hivPos");
		// rd.addIndicator("2.11",
		// "rwandareports.tracnetreport.indicator.pregnantHivPosEligibleArvs2",
		// pregnantHivPosEligibleArvs2Indicator);
		rd.addIndicator("2.12",
				"rwandareports.tracnetreport.indicator.womenTestedForRpr",
				pregnantWomenUpToDateInd,
				"gender=female|status=hivPos|status=testRPR");
		rd.addIndicator(
				"2.13",
				"rwandareports.tracnetreport.indicator.pregnantTestedPosForRpr",
				pregnantWomenUpToDateInd,
				"gender=female|status=hivPos|status=rprPos");
		rd.addIndicator(
				"2.14",
				"rwandareports.tracnetreport.indicator.pregnantPartnersTestedForHiv",
				womenPartnersTestedInd);
		rd.addIndicator(
				"2.15",
				"rwandareports.tracnetreport.indicator.hivNegPregnantPartnersTestedHivPos",
				discordantCoupleInPmtctInd);
		rd.addIndicator("2.16",
				"rwandareports.tracnetreport.indicator.discordantCouples1",
				womenNegDuringInd);
		rd.addIndicator("2.17",
				"rwandareports.tracnetreport.indicator.partnersTestedHivPos",
				menPartnerTestedHivPositiveInd);
		// PMTCT MATERNITY
		rd.addIndicator(
				"3.1",
				"rwandareports.tracnetreport.indicator.expectedDeliveriesFacilityThisMonth",
				expectedDeliveriesFacilityThisMonthIndicator, "gender=female");
		rd.addIndicator(
				"3.2",
				"rwandareports.tracnetreport.indicator.occuringDeliveriesFacilityThisMonth",
				occuringDeliveriesFacilityThisMonthIndicator, "gender=female");
		rd.addIndicator(
				"3.3",
				"rwandareports.tracnetreport.indicator.expectedDeliveriesAmongHivPosWomen",
				expectedDeliveriesAmongHivPosWomenIndicator,
				"status=hivPos|gender=female");
		rd.addIndicator(
				"3.4",
				"rwandareports.tracnetreport.indicator.womenHivPosGivingBirthAtFacility",
				delivereyOccuredAtFosaIndicators, "status=hivPos|gender=female");
		rd.addIndicator(
				"3.5",
				"rwandareports.tracnetreport.indicator.reportedHivPosGivingBirthAtHome",
				reportedHivPosGivingBirthAtHomeInd,
				"status=hivPos|gender=female");
		rd.addIndicator(
				"3.6",
				"rwandareports.tracnetreport.indicator.womenHivPosAzt3tcNvpDuringLabor",
				reportedHivPosGivingBirthAtHomeInd);
		rd.addIndicator(
				"3.7",
				"rwandareports.tracnetreport.indicator.womenReceivingAzt3tcAfterDelivery",
				donetestindeliveryroomIndi, "gender=female|status=hivUndet");
		rd.addIndicator(
				"3.8",
				"rwandareports.tracnetreport.indicator.womenUnknownHivStatusTestedDuringLabor1",
				womenUnknownHivStatusTestedDuringLabor1Indicator,
				"gender=female|status=hivPosDuringPeriod");
		rd.addIndicator(
				"3.9",
				"rwandareports.tracnetreport.indicator.womenUnknownHivStatusTestedPosDuringLabor2",
				womenUnknownHivinitiatedontrhitherapyInd,
				"status=hivPos|gender=female");
		rd.addIndicator(
				"3.10",
				"rwandareports.tracnetreport.indicator.pregnantReceivedCompleteCourseThisMonth",
				pregnantReceivedCompleteCourseThisMonthInd,
				"status=hivPos|gender=female");
		// PMTCT HIV INFANT EXPOSED
		rd.addIndicator(
				"4.1",
				"rwandareports.tracnetreport.indicator.womenHivPosBreastFeeding",
				womenHivPosBreastFeedingIndicator,
				"status=hivPos|gender=female");
		rd.addIndicator(
				"4.2",
				"rwandareports.tracnetreport.indicator.womenHivPosUsingFormula",
				womenHivPosUsingFormulaIndicator, "status=hivPos|gender=female");
		rd.addIndicator(
				"4.3",
				"rwandareports.tracnetreport.indicator.infantHivPosMothersEnrolledPmtct",
				infantBornOnHivPositiveIndicator);
		rd.addIndicator(
				"4.4",
				"rwandareports.tracnetreport.indicator.infantBornToHivNegMothersInCoupleDiscordantEnrolledInPMTCT",
				infantBornOnHivNegativeMothersInd);
		rd.addIndicator(
				"4.5",
				"rwandareports.tracnetreport.indicator.infantHivPosMothersTestedAt6Weeks",
				infantWithBornTohivPosOrNegandTestedTested, "age=at6weeks");
		rd.addIndicator(
				"4.6",
				"rwandareports.tracnetreport.indicator.infantHivPosMothersTestedPosAt6Weeks",
				infantWithBornTohivPosOrNegandTestedTested,
				"age=at6weeks|status=hivTestDuringP");
		rd.addIndicator(
				"4.7",
				"rwandareports.tracnetreport.indicator.infantHivPosMothersAged6WeeksThisMonth",
				infantWithBornTohivPosOrNegandTestedTested,
				"age=at6weeks|status=hivPosDuringPeriod");
		rd.addIndicator(
				"4.8",
				"rwandareports.tracnetreport.indicator.infantHivPosMothersAged6WeeksThisMonthonCOtrimo",
				infantHivPosMothersTestedAt6WeeksIndicatoronCotrimo,
				"age=at6weeks");
		rd.addIndicator(
				"4.9",
				"rwandareports.tracnetreport.indicator.infantHivPosMothersTestedAt9Months",
				infantWithBornTohivPosOrNegandTestedTested, "age=at9months");
		rd.addIndicator(
				"4.10",
				"rwandareports.tracnetreport.indicator.infantHivPosMothersTestedPosAt9Months",
				infantWithBornTohivPosOrNegandTestedTested,
				"age=at9months|status=hivTestDuringP");
		rd.addIndicator(
				"4.11",
				"rwandareports.tracnetreport.indicator.infantHivPosMothersAged9MonthsThisMonth",
				infantWithBornTohivPosOrNegandTestedTested,
				"age=at9months|status=hivPosDuringPeriod");
		rd.addIndicator(
				"4.12",
				"rwandareports.tracnetreport.indicator.infantHivPosMothersTestedAt18Months",
				infantWithBornTohivPosOrNegandTestedTested, "age=at18months");
		rd.addIndicator(
				"4.13",
				"rwandareports.tracnetreport.indicator.infantHivPosMothersTestedPosAt18Months",
				infantWithBornTohivPosOrNegandTestedTested,
				"age=at18months|status=hivTestDuringP");
		rd.addIndicator(
				"4.14",
				"rwandareports.tracnetreport.indicator.infantHivPosMothersAgedAt18MonthsThisMonth",
				infantWithBornTohivPosOrNegandTestedTested,
				"age=at18months|status=hivPosDuringPeriod");
		rd.addIndicator(
				"4.15",
				"rwandareports.tracnetreport.indicator.infantHivPosMothersLostFollowup",
				infantHivPosMothersLostFollowupIndicator);
		rd.addIndicator(
				"4.16",
				"rwandareports.tracnetreport.indicator.infantHivPosMothersScreenedTbThisMonth",
				infantWithBornTohivPosOrNegandTestedTested, "status=tbtest");
		rd.addIndicator(
				"4.17",
				"rwandareports.tracnetreport.indicator.reportedDeadInfantHivPosMothers",
				infantWithBornTohivPosOrNegandTestedTested, "status=died");
		// rd.addIndicator("4.18","rwandareports.tracnetreport.indicator.infantHivPosMothersMalnourished",
		// reportedDeadInfantHivPosMothersIndicator);
		// rd.addIndicator("4.19","rwandareports.tracnetreport.indicator.infantHivPosMothersTherapFood",
		// infantHivPosMothersTherapFoodIndocator,"age=lessThan5");
		// PMTCT FAMILY PLANNING
		rd.addIndicator(
				"5.1",
				"rwandareports.tracnetreport.indicator.womenHivPosExpectedFpAtFacility",
				womenHivPosExpectedFpAtFacilityPeriodIndicator,
				"status=hivPos|gender=female");
		rd.addIndicator("5.2",
				"rwandareports.tracnetreport.indicator.womenHivPosSeenInFp",
				womenSeenInFpPeriodIndicator, "status=hivPos|gender=female");
		rd.addIndicator(
				"5.3",
				"rwandareports.tracnetreport.indicator.womenHivPosPartnersSeenInFp",
				womenpartnerhivPosInd, "status=hivPos|gender=female");
		rd.addIndicator(
				"5.4",
				"rwandareports.tracnetreport.indicator.womenHivPosReceivingModernContraceptive",
				womenHivPosReceivingModernContraceptivePeriodIndicator,
				"status=hivPos|gender=female");
		rd.addIndicator(
				"5.5",
				"rwandareports.tracnetreport.indicator.womenHivPosRefferedForFp",
				fpReferredForFamilyPlanningCompositionPeriodIndicator,
				"status=hivPos|gender=female");
		// PRE-ART
		rd.addIndicator(
				"6.1",
				"rwandareports.tracnetreport.indicator.preart.newPedsUnderEighteenMonthsInHivCare",
				pedsInHivCareOnPreArtDrugsDuringPeriodInd, "age=at18months");
		rd.addIndicator(
				"6.2",
				"rwandareports.tracnetreport.indicator.preart.newPedsUnderFiveInHivCare",
				pedsInHivCareOnPreArtDrugsDuringPeriodInd, "age=lessThan5");
		rd.addIndicator(
				"6.3",
				"rwandareports.tracnetreport.indicator.preart.newFemaleUnderFifteenInHivCare",
				pedsInHivCareOnPreArtDrugsDuringPeriodInd,
				"age=underFifteen|gender=female");
		rd.addIndicator(
				"6.4",
				"rwandareports.tracnetreport.indicator.preart.newMaleUnderFifteenInHivCare",
				pedsInHivCareOnPreArtDrugsDuringPeriodInd,
				"age=underFifteen|gender=male");
		rd.addIndicator(
				"6.5",
				"rwandareports.tracnetreport.indicator.preart.newFemaleMoreThanFifteenInHivCare",
				pedsInHivCareOnPreArtDrugsDuringPeriodInd,
				"age=moreThan15|gender=female");
		rd.addIndicator(
				"6.6",
				"rwandareports.tracnetreport.indicator.preart.newMaleMoreThanFifteenInHivCare",
				pedsInHivCareOnPreArtDrugsDuringPeriodInd,
				"age=moreThan15|gender=male");
		rd.addIndicator(
				"6.7",
				"rwandareports.tracnetreport.indicator.preart.pedUnderEighteenMonthsEverInHiv",
				pedsEverInHivCareOnPreArtDrugsInd, "age=at18months");
		rd.addIndicator(
				"6.8",
				"rwandareports.tracnetreport.indicator.preart.pedsUnderFiveEverInHiv",
				pedsEverInHivCareOnPreArtDrugsInd, "age=lessThan5");
		rd.addIndicator(
				"6.9",
				"rwandareports.tracnetreport.indicator.preart.femaleMoreThanFifteenEverInHiv",
				pedsEverInHivCareOnPreArtDrugsInd,
				"age=moreThan15|gender=female");
		rd.addIndicator(
				"6.10",
				"rwandareports.tracnetreport.indicator.preart.femalePedsUnderFifteenEverInHiv",
				pedsEverInHivCareOnPreArtDrugsInd,
				"age=underFifteen|gender=female");
		rd.addIndicator(
				"6.11",
				"rwandareports.tracnetreport.indicator.preart.maleMoreThanFifteenEverInHiv",
				pedsEverInHivCareOnPreArtDrugsInd, "age=moreThan15|gender=male");
		rd.addIndicator(
				"6.12",
				"rwandareports.tracnetreport.indicator.preart.malePedsUnderFifteenEverInHiv",
				pedsEverInHivCareOnPreArtDrugsInd,
				"age=underFifteen|gender=male");
		rd.addIndicator(
				"6.13",
				"rwandareports.tracnetreport.indicator.preart.patientsOnCotrimoProphylaxis",
				patientInHivOnCotrimoXazoleInd);
		rd.addIndicator(
				"6.14",
				"rwandareports.tracnetreport.indicator.preart.patientsActiveTbAtEnrolThisMonth",
				patientInHivScreenedForTbDuringPeriodInd);
		rd.addIndicator(
				"6.15",
				"rwandareports.tracnetreport.indicator.preart.patientsTbPositiveAtEnrolThisMonth",
				patientsInHivTreatmentThisMonthInd, "status=tbpos");
		rd.addIndicator(
				"6.16",
				"rwandareports.tracnetreport.indicator.preart.newEnrolledPedsStartTbTreatThisMonth",
				patientsInHivWhoStartedTBTreatmentThisMonthInd,
				"age=underFifteen");
		rd.addIndicator(
				"6.17",
				"rwandareports.tracnetreport.indicator.preart.newEnrolledAdultsStartTbTreatThisMonth",
				patientsInHivWhoStartedTBTreatmentThisMonthInd,
				"age=moreThan15");
		rd.addIndicator(
				"6.18",
				"rwandareports.tracnetreport.indicator.preart.PatientsInPreARVDiedThisMonth",
				patientsInHivTreatmentThisMonthInd, "status=died");
		rd.addIndicator(
				"6.19",
				"rwandareports.tracnetreport.indicator.preart.PatientsInPreARVTransferredInThisMonth",
				patientsInHivTreatmentThisMonthInd, "status=comein");
		rd.addIndicator(
				"6.20",
				"rwandareports.tracnetreport.indicator.preart.PatientsInPreARVTransferredOutThisMonth",
				patientsInHivTreatmentThisMonthInd, "status=out");
		rd.addIndicator(
				"6.21",
				"rwandareports.tracnetreport.indicator.preart.PatientsInPreARVTLostToFollowUpThisMonth",
				preArtpatientLostToFolowupThisMonthInd);
		// ART DATA ELEMENTS
		rd.addIndicator(
				"7.1",
				"rwandareports.tracnetreport.indicator.art.pedsUnderEighteenMonthsCurrentOnArv",
				artpatientOnArvInProgramInd, "age=at18months");
		rd.addIndicator(
				"7.2",
				"rwandareports.tracnetreport.indicator.art.pedsUnderFiveCurrentOnArv",
				artpatientOnArvInProgramInd, "age=lessThan5");
		rd.addIndicator(
				"7.3",
				"rwandareports.tracnetreport.indicator.art.femalePedsUnderFifteenCurrentOnArv",
				artpatientOnArvInProgramInd, "age=underFifteen|gender=female");
		rd.addIndicator(
				"7.4",
				"rwandareports.tracnetreport.indicator.art.malePedsUnderFifteenCurrentOnArv",
				artpatientOnArvInProgramInd, "age=underFifteen|gender=male");
		rd.addIndicator("7.5",
				"rwandareports.tracnetreport.indicator.art.pedsOnFirstLineReg",
				artPedspatientOnFirstlineRegInHivInd, "age=underFifteen");
		rd.addIndicator(
				"7.6",
				"rwandareports.tracnetreport.indicator.art.pedsOnSecondLineReg",
				artPedspatientOnSecondlineRegInHivInd, "age=underFifteen");
		rd.addIndicator(
				"7.7",
				"rwandareports.tracnetreport.indicator.art.femaleMoreThanFifteenCurrentOnArv",
				artadultspatientOnArvdrugsInHivInd,
				"age=moreThan15|gender=female");
		rd.addIndicator(
				"7.8",
				"rwandareports.tracnetreport.indicator.art.maleMoreThanFifteenCurrentOnArv",
				artadultspatientOnArvdrugsInHivInd,
				"age=moreThan15|gender=male");
		rd.addIndicator(
				"7.9",
				"rwandareports.tracnetreport.indicator.art.adultOnFirstLineReg",
				artadultspatientOnFirstlineRegInHivInd, "age=moreThan15");
		rd.addIndicator(
				"7.10",
				"rwandareports.tracnetreport.indicator.art.adultOnSecondLineReg",
				artadultspatientOnSecondlineRegInHivInd, "age=moreThan15");
		rd.addIndicator(
				"7.11",
				"rwandareports.tracnetreport.indicator.art.newPedsUnderEighteenMonthStartArvThisMonth",
				artpedNewOninhivWthGenericStatusInd, "age=at18months");
		rd.addIndicator(
				"7.12",
				"rwandareports.tracnetreport.indicator.art.newPedsUnderFiveStartArvThisMonth",
				artpedNewOninhivWthGenericStatusInd, "age=lessThan5");
		rd.addIndicator(
				"7.13",
				"rwandareports.tracnetreport.indicator.art.newFemalePedsUnderFifteenStartArvThisMonth",
				artpedNewOninhivWthGenericStatusInd,
				"age=underFifteen|gender=female");
		rd.addIndicator(
				"7.14",
				"rwandareports.tracnetreport.indicator.art.newMalePedsUnderFifteenStartArvThisMonth",
				artpedNewOninhivWthGenericStatusInd,
				"age=underFifteen|gender=male");
		rd.addIndicator(
				"7.15",
				"rwandareports.tracnetreport.indicator.art.newPedsWhoStageFourThisMonth",
				artpedNewOninhivWthWhoStagesInd,
				"age=underFifteen|status=whostage4p");
		rd.addIndicator(
				"7.16",
				"rwandareports.tracnetreport.indicator.art.newPedsWhoStageThreeThisMonth",
				artpedNewOninhivWthWhoStagesInd,
				"age=underFifteen|status=whostage3p");
		rd.addIndicator(
				"7.17",
				"rwandareports.tracnetreport.indicator.art.newPedsWhoStageTwoThisMonth",
				artpedNewOninhivWthWhoStagesInd,
				"age=underFifteen|status=whostage2p");
		rd.addIndicator(
				"7.18",
				"rwandareports.tracnetreport.indicator.art.newPedsWhoStageOneThisMonth",
				artpedNewOninhivWthWhoStagesInd,
				"age=underFifteen|status=whostage1p");
		rd.addIndicator(
				"7.19",
				"rwandareports.tracnetreport.indicator.art.newPedsUndefinedWhoStageThisMonth",
				artpedNewOninhivWthWhoStagesInd,
				"age=underFifteen|status=whostageX");
		rd.addIndicator(
				"7.20",
				"rwandareports.tracnetreport.indicator.art.newFemaleAdultStartiArvThisMonth",
				artadultsNewOninhivWithGenericStatusInd,
				"age=moreThan15|gender=female");
		rd.addIndicator(
				"7.21",
				"rwandareports.tracnetreport.indicator.art.newMaleAdultStartiArvThisMonth",
				artadultsNewOninhivWithGenericStatusInd,
				"age=moreThan15|gender=male");
		rd.addIndicator(
				"7.22",
				"rwandareports.tracnetreport.indicator.art.newAdultWhoStageFourThisMonth",
				artpedNewOninhivWthWhoStagesInd,
				"age=moreThan15|status=whostage4a");
		rd.addIndicator(
				"7.23",
				"rwandareports.tracnetreport.indicator.art.newAdultWhoStageThreeThisMonth",
				artpedNewOninhivWthWhoStagesInd,
				"age=moreThan15|status=whostage3a");
		rd.addIndicator(
				"7.24",
				"rwandareports.tracnetreport.indicator.art.newAdultWhoStageTwoThisMonth",
				artpedNewOninhivWthWhoStagesInd,
				"age=moreThan15|status=whostage2a");
		rd.addIndicator(
				"7.25",
				"rwandareports.tracnetreport.indicator.art.newAdultWhoStageOneThisMonth",
				artpedNewOninhivWthWhoStagesInd,
				"age=moreThan15|status=whostage1a");
		rd.addIndicator(
				"7.26",
				"rwandareports.tracnetreport.indicator.art.newAdultUndefinedWhoStageThisMonth",
				artpedNewOninhivWthWhoStagesInd,
				"age=moreThan15|status=whostageX");
		rd.addIndicator(
				"7.27",
				"rwandareports.tracnetreport.indicator.art.arvPedsFifteenInterruptTreatThisMonth",
				artpedsWhoseDrugsinteruptedThismontInd, "age=underFifteen");
		rd.addIndicator(
				"7.28",
				"rwandareports.tracnetreport.indicator.art.arvAdultFifteenInterruptTreatThisMonth",
				artpedsWhoseDrugsinteruptedThismontInd, "age=moreThan15");
		rd.addIndicator(
				"7.29",
				"rwandareports.tracnetreport.indicator.art.arvPedsDiedThisMonth",
				inhivProgamOnARVDiedTrasgerInorOutInd,
				"age=underFifteen|status=died");
		rd.addIndicator(
				"7.30",
				"rwandareports.tracnetreport.indicator.art.arvAdultDiedThisMonth",
				inhivProgamOnARVDiedTrasgerInorOutInd,
				"age=moreThan15|status=died");
		rd.addIndicator(
				"7.31",
				"rwandareports.tracnetreport.indicator.art.arvPedsLostFollowupMoreThreeMonths",
				artPatientInHivOnArvLostInd, "age=underFifteen");
		rd.addIndicator(
				"7.32",
				"rwandareports.tracnetreport.indicator.art.arvAdultLostFollowupMoreThreeMonths",
				artPatientInHivOnArvLostInd, "age=moreThan15");
		rd.addIndicator(
				"7.33",
				"rwandareports.tracnetreport.indicator.art.maleOnTreatTwelveAfterInitArv",
				artPAtientStartedAyearAgoInd, "gender=male");
		rd.addIndicator(
				"7.34",
				"rwandareports.tracnetreport.indicator.art.femaleOnTreatTwelveAfterInitArv",
				artPAtientStartedAyearAgoInd, "gender=female");
		rd.addIndicator(
				"7.35",
				"rwandareports.tracnetreport.indicator.art.arvPedsTransferredOutThisMonth",
				inhivProgamOnARVDiedTrasgerInorOutInd,
				"age=underFifteen|status=out");
		rd.addIndicator(
				"7.36",
				"rwandareports.tracnetreport.indicator.art.arvAdultTransferredOutThisMonth",
				inhivProgamOnARVDiedTrasgerInorOutInd,
				"age=moreThan15|status=out");
		rd.addIndicator(
				"7.37",
				"rwandareports.tracnetreport.indicator.art.arvPedsTransferredInThisMonth",
				inhivProgamOnARVDiedTrasgerInorOutInd,
				"age=underFifteen|status=comein");
		rd.addIndicator(
				"7.38",
				"rwandareports.tracnetreport.indicator.art.arvAdultTransferreInThisMonth",
				inhivProgamOnARVDiedTrasgerInorOutInd,
				"age=moreThan15|status=comein");
		// STI OPPORTUNISTIC
		rd.addIndicator(
				"8.1",
				"rwandareports.tracnetreport.indicator.clientsCounceledForStiThisMonth",
				patientsCounseledAndScreenedForStiInd,
				"age=moreThan15|status=counVct");
		rd.addIndicator("8.2",
				"rwandareports.tracnetreport.indicator.stiDiagnosedThisMonth",
				patientsScreenedForStiAndOnTreatmentInd, "age=moreThan15");
		rd.addIndicator(
				"8.3",
				"rwandareports.tracnetreport.indicator.opportInfectTreatedExcludeTbThisMonth",
				tbinfectionOpportunisticCompInd);
		// MALNUTRITION DATA ELEMENTS
		rd.addIndicator(
				"9.1",
				"rwandareports.tracnetreport.indicator.pedsUnderFiveSevereMalnutrThisMonth",
				pedswithSevereNutritionInd, "age=lessThan5|status=hivPos");
		rd.addIndicator(
				"9.2",
				"rwandareports.tracnetreport.indicator.pedsUnderFiveSevereMalnutrTheurapThisMonth",
				patientReceivedAnyFoodPackageInd, "age=lessThan5|status=hivPos");
		rd.addIndicator(
				"9.3",
				"rwandareports.tracnetreport.indicator.pedsUnderFifteenSevMalnutrTherapThisMonth",
				patientReceivedAnyFoodPackageInd,
				"age=underFifteen|status=hivPos");
		rd.addIndicator(
				"9.4",
				"rwandareports.tracnetreport.indicator.adultSevereMalnutrTherapThisMonth",
				patientReceivedAnyFoodPackageInd,
				"age=moreThan15|status=hivPos");
		rd.addIndicator(
				"9.5",
				"rwandareports.tracnetreport.indicator.pregnantMalnutrTherapThisMonth",
				malnourishedWitadherenceCounselingInd, "status=hivPos");
		// rd.addIndicator("9.6","rwandareports.tracnetreport.indicator.lactatingMalnutrTherapThisMonth",patientReferedForFamilyPlaningMalnurished,"age=age15To49");

		rd.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort(),
				ParameterizableUtil
						.createParameterMappings("location=${location}"));

		h.saveReportDefinition(rd);

		return rd;
	}

	private void setUpProperties() {
		adulthivProgram = gp
				.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		pediatrichivProgram = gp
				.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		pmtctpregnantProgram = gp
				.getProgram(GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		pmtctcombinedInfant = gp
				.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_INFANT);
		pmtctcombinedMother = gp
				.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_MOTHER_PROGRAM);
		patientstartedarvforprophylaxis = gp
				.getConcept(GlobalPropertiesManagement.REASON_PATIENT_STARTED_ARVS_FOR_PROPHYLAXIS);
		patientonbloodorbloodexposure = gp
				.getConcept(GlobalPropertiesManagement.EXPOSURE_TO_BLOOD_OR_BLOOD_PRODUCTS);
		sexualAssault = gp
				.getConcept(GlobalPropertiesManagement.SEXUAL_ASSAULT);
		sexualContactWithPospartner = gp
				.getConcept(GlobalPropertiesManagement.SEXUAL_CONTACT_WITH_HIV_POSITIVE_PARTNER);
		hivTestDone = gp.getConcept(GlobalPropertiesManagement.HIV_TEST);
		positiveStatus = gp
				.getConcept(GlobalPropertiesManagement.POSITIVE_HIV_TEST_ANSWER);
		negativeStatus = gp
				.getConcept(GlobalPropertiesManagement.NEGATIVE_HIV_TEST_ANSWER);
		notestDone = gp.getConcept(GlobalPropertiesManagement.NO_TEST);
		undeterminateStatus = gp
				.getConcept(GlobalPropertiesManagement.UNDETERMINATE_HIV_TEST_ANSWER);
		pregnancystatusConcept = gp
				.getConcept(GlobalPropertiesManagement.PREGNANCY_STATUS);
		yesStatusToPregnant = gp.getConcept(GlobalPropertiesManagement.YES);
		// noStatutToPregnant=gp.getConcept(GlobalPropertiesManagement.NO);
		programthatOrderedtest = gp
				.getConcept(GlobalPropertiesManagement.PROGRAM_THAT_ORDERED_TEST);
		maternityward = gp
				.getConcept(GlobalPropertiesManagement.MATERNITY_WARD);
		birthlocationtype = gp
				.getConcept(GlobalPropertiesManagement.BIRTH_LOCATION_TYPE);
		athospital = gp.getConcept(GlobalPropertiesManagement.HOSPITAL);
		athealthcenter = gp
				.getConcept(GlobalPropertiesManagement.HEALTH_CENTER);
		athome = gp.getConcept(GlobalPropertiesManagement.HOUSE);
		infantFeedingMethod = gp
				.getConcept(GlobalPropertiesManagement.INFANT_FEEDING_METHOD);
		breastFeedExclusively = gp
				.getConcept(GlobalPropertiesManagement.BREASTFEED_EXCL);
		usingFormula = gp.getConcept(GlobalPropertiesManagement.USING_FORMULA);
		tbScreeningtest = gp
				.getConcept(GlobalPropertiesManagement.TB_SCREENING_TEST);
		reasonForExitingCare = gp
				.getConcept(GlobalPropertiesManagement.REASON_FOR_EXITING_CARE);
		patientsDied = gp.getConcept(GlobalPropertiesManagement.PATIENT_DIED);
		transferOut = gp.getConcept(GlobalPropertiesManagement.TRASNFERED_OUT);
		transferIn = gp.getConcept(GlobalPropertiesManagement.TRASNFERED_IN);
		whostage = gp.getConcept(GlobalPropertiesManagement.WHOSTAGE);
		whostage4p = gp.getConcept(GlobalPropertiesManagement.WHOSTAGE4PED);
		whostage3p = gp.getConcept(GlobalPropertiesManagement.WHOSTAGE3PED);
		whostage2p = gp.getConcept(GlobalPropertiesManagement.WHOSTAGE2PED);
		whostage1p = gp.getConcept(GlobalPropertiesManagement.WHOSTAGE1PED);
		whostageinconue = gp
				.getConcept(GlobalPropertiesManagement.WHOSTAGEUNKOWN);
		whostage4adlt = gp.getConcept(GlobalPropertiesManagement.WHOSTAGE4AD);
		whostage3adlt = gp.getConcept(GlobalPropertiesManagement.WHOSTAGE3AD);
		whostage2adlt = gp.getConcept(GlobalPropertiesManagement.WHOSTAGE2AD);
		whostage1adlt = gp.getConcept(GlobalPropertiesManagement.WHOSTAGE1AD);
		folowingState = gp.getProgramWorkflowState(
				GlobalPropertiesManagement.FOLLOWING_STATE,
				GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW,
				GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		opportunisticInfection = gp
				.getConcept(GlobalPropertiesManagement.CURRENT_OPORTUNISTIC_INFECTION);
		stiScreenedTb = gp.getConcept(GlobalPropertiesManagement.TUBERCULOSIS);
		reasonTherapeuticFailed = gp
				.getConcept(GlobalPropertiesManagement.REASON_THERAPEUTIC_FAILED);
		pooradherence = gp
				.getConcept(GlobalPropertiesManagement.POOR_ADHERENCE);
		pregnatWithPregnancyStatus = gp.getProgramWorkflowState(
				GlobalPropertiesManagement.PATIENT_PREGNANT_STATE,
				GlobalPropertiesManagement.PREGNANCY_STATUS_WORKFLOW,
				GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		pregnantOnArtstate = gp.getProgramWorkflowState(
				GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE,
				GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW,
				GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		rprTest = gp.getConcept(GlobalPropertiesManagement.RPR_TEST);
		reactiveAnswer = gp
				.getConcept(GlobalPropertiesManagement.RPR_REACTIVE_ANWER);
		gaveBirthinpmtctstate = gp.getProgramWorkflowState(
				GlobalPropertiesManagement.GAVE_BIRTH,
				GlobalPropertiesManagement.PREGNANCY_STATUS_WORKFLOW,
				GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		familyplaning = gp
				.getConcept(GlobalPropertiesManagement.METHOD_OF_FAMILY_PLANNING);
		condoms = gp.getConcept(GlobalPropertiesManagement.USING_CONDOMS);
		injectable = gp.getConcept(GlobalPropertiesManagement.USINF_INJECTABLE);
		orals = gp.getConcept(GlobalPropertiesManagement.USING_ORALCONTRAC);
		disposition = gp.getConcept(GlobalPropertiesManagement.DISPOSITION);
		referedforFp = gp.getConcept(GlobalPropertiesManagement.REFERED_FOR_FP);
		;
		onOrAfterOnOrBeforeParamterNames.add("onOrAfter");
		onOrAfterOnOrBeforeParamterNames.add("onOrBefore");

	}
}
