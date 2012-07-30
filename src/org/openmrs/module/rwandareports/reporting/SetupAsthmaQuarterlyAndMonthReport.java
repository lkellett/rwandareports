package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.dataset.EncounterIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.indicator.EncounterIndicator;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.Indicators;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

public class SetupAsthmaQuarterlyAndMonthReport {
	
	public Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	// properties
	private Program asthmaProgram;
	
	private List<Program> asthmaPrograms = new ArrayList<Program>();
		
	private EncounterType asthmaEncounterType;
	
	private Form DDBform;
	
	private Form rendevousForm;
	
	private List<String> onOrAfterOnOrBefore = new ArrayList<String>();
		
	private List<String> enrolledOnOrAfterOnOrBefore = new ArrayList<String>();
	
	//private List<Concept> peacFlowConcepts = new ArrayList<Concept>();
	
	private Concept peakFlowAfterSalbutamol;
	
	private Concept peakFlowBeforeSalbutamol;
	
	private Concept smokingHistory;
	
	private List<Form> DDBforms = new ArrayList<Form>();
	
	private List<Concept> asthmasMedications = new ArrayList<Concept>();
	
	
	
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		//Monthly report set-up
		ReportDefinition monthlyRd = new ReportDefinition();
		monthlyRd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		monthlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");
		monthlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		
		monthlyRd.setName("Asthma Monthly Indicator Report");
		
		monthlyRd.addDataSetDefinition(createMonthlyLocationDataSet(),
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));
		
		// Quarterly Report Definition: Start
		
		ReportDefinition quarterlyRd = new ReportDefinition();
		quarterlyRd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		quarterlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		
		quarterlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		
		quarterlyRd.setName("Asthma Quarterly Indicator Report");
		
		quarterlyRd.addDataSetDefinition(createQuarterlyLocationDataSet(),
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));
		
		// Quarterly Report Definition: End
		
		ProgramEnrollmentCohortDefinition patientEnrolledInAsthmaProgram = new ProgramEnrollmentCohortDefinition();
		patientEnrolledInAsthmaProgram.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientEnrolledInAsthmaProgram.setPrograms(asthmaPrograms);
		
		monthlyRd.setBaseCohortDefinition(patientEnrolledInAsthmaProgram,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
		
		quarterlyRd.setBaseCohortDefinition(patientEnrolledInAsthmaProgram,
			    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
		
		h.saveReportDefinition(monthlyRd);
		h.saveReportDefinition(quarterlyRd);
		
		ReportDesign monthlyDesign = h.createRowPerPatientXlsOverviewReportDesign(monthlyRd,
		    "Asthma_Indicator_Monthly_Report.xls", "Asthma Indicator Monthly Report (Excel)", null);
		Properties monthlyProps = new Properties();
		monthlyProps.put("repeatingSections", "sheet:1,dataset:Encounter Monthly Data Set");
		
		monthlyDesign.setProperties(monthlyProps);
		h.saveReportDesign(monthlyDesign);
		
		ReportDesign quarterlyDesign = h.createRowPerPatientXlsOverviewReportDesign(quarterlyRd,
		    "Asthma_Indicator_Quarterly_Report.xls", "Asthma Indicator Quarterly Report (Excel)", null);
		Properties quarterlyProps = new Properties();
		quarterlyProps.put("repeatingSections", "sheet:1,dataset:Encounter Quarterly Data Set");
		
		quarterlyDesign.setProperties(quarterlyProps);
		h.saveReportDesign(quarterlyDesign);
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Asthma Indicator Monthly Report (Excel)".equals(rd.getName())
			        || "Asthma Indicator Quarterly Report (Excel)".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Asthma Quarterly Indicator Report");
		h.purgeReportDefinition("Asthma Monthly Indicator Report");
		
	}
	
	// Create Monthly Location Data set
	public LocationHierachyIndicatorDataSetDefinition createMonthlyLocationDataSet() {
		
		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(
		        createMonthlyEncounterBaseDataSet());
		ldsd.addBaseDefinition(createMonthlyBaseDataSet());
		ldsd.setName("Encounter Monthly Data Set");
		ldsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		return ldsd;
	}
	
	private EncounterIndicatorDataSetDefinition createMonthlyEncounterBaseDataSet() {
		
		EncounterIndicatorDataSetDefinition eidsd = new EncounterIndicatorDataSetDefinition();
		
		eidsd.setName("eidsd");
		eidsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		eidsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		createMonthlyIndicators(eidsd);
		return eidsd;
	}
	
	private void createMonthlyIndicators(EncounterIndicatorDataSetDefinition dsd) {
		
		SqlEncounterQuery patientVisitsToAsthmaClinic = new SqlEncounterQuery();
		
		patientVisitsToAsthmaClinic
		        .setQuery("select encounter_id from encounter where encounter_id in(select encounter_id from encounter where (form_id="
		                + rendevousForm.getFormId()
		                + " or form_id="
		                + DDBform.getFormId()
		                + ") and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0 group by encounter_datetime, patient_id)");
		patientVisitsToAsthmaClinic.setName("patientVisitsToAsthmaClinic");
		patientVisitsToAsthmaClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientVisitsToAsthmaClinic.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		EncounterIndicator patientVisitsToAsthmaClinicMonthIndicator = new EncounterIndicator();
		patientVisitsToAsthmaClinicMonthIndicator.setName("patientVisitsToAsthmaClinicMonthIndicator");
		patientVisitsToAsthmaClinicMonthIndicator.setEncounterQuery(new Mapped<EncounterQuery>(patientVisitsToAsthmaClinic,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		dsd.addColumn(patientVisitsToAsthmaClinicMonthIndicator);
		
	}
	
	//Create Quarterly Encounter Data set
	
	public LocationHierachyIndicatorDataSetDefinition createQuarterlyLocationDataSet() {
		
		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(
		        createEncounterQuarterlyBaseDataSet());
		ldsd.addBaseDefinition(createQuarterlyBaseDataSet());
		ldsd.setName("Encounter Quarterly Data Set");
		ldsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		return ldsd;
	}
	
	private EncounterIndicatorDataSetDefinition createEncounterQuarterlyBaseDataSet() {
		
		EncounterIndicatorDataSetDefinition eidsd = new EncounterIndicatorDataSetDefinition();
		
		eidsd.setName("eidsd");
		eidsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		eidsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		createQuarterlyIndicators(eidsd);
		return eidsd;
	}
	
	private void createQuarterlyIndicators(EncounterIndicatorDataSetDefinition dsd) {
		
		//=======================================================================
		//  A1: Total # of patient visits to DM clinic in the last quarter
		//==================================================================
	SqlEncounterQuery patientVisitsToAsthmaClinic = new SqlEncounterQuery();
		
		patientVisitsToAsthmaClinic
		        .setQuery("select encounter_id from encounter where encounter_id in(select encounter_id from encounter where (form_id="
		                + rendevousForm.getFormId()
		                + " or form_id="
		                + DDBform.getFormId()
		                + ") and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0 group by encounter_datetime, patient_id)");
		patientVisitsToAsthmaClinic.setName("patientVisitsToAsthmaClinic");
		patientVisitsToAsthmaClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientVisitsToAsthmaClinic.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		EncounterIndicator patientVisitsToAsthmaClinicMonthIndicator = new EncounterIndicator();
		patientVisitsToAsthmaClinicMonthIndicator.setName("patientVisitsToAsthmaClinicQuarterlyIndicator");
		patientVisitsToAsthmaClinicMonthIndicator.setEncounterQuery(new Mapped<EncounterQuery>(patientVisitsToAsthmaClinic,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		dsd.addColumn(patientVisitsToAsthmaClinicMonthIndicator);
		
	}
	
	// create monthly cohort Data set
	
	private CohortIndicatorDataSetDefinition createMonthlyBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Monthly Cohort Data Set");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		createMonthlyIndicators(dsd);
		return dsd;
	}
	
	// create quarterly cohort Data set
	private CohortIndicatorDataSetDefinition createQuarterlyBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Quarterly Cohort Data Set");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		createQuarterlyIndicators(dsd);
		return dsd;
	}
	
	private void createQuarterlyIndicators(CohortIndicatorDataSetDefinition dsd) {
		
		/*//=======================================================================
		//  A2: Total # of patients seen in the last month/quarter
		//==================================================================
		
		EncounterCohortDefinition patientSeen = Cohorts.createEncounterParameterizedByDate("Patients seen",
		    onOrAfterOnOrBefore, patientsSeenEncounterTypes);
		
		EncounterCohortDefinition patientWithDDB = Cohorts.createEncounterBasedOnForms("patientWithDDB",
		    onOrAfterOnOrBefore, DDBforms);
		
		CompositionCohortDefinition patientsSeenComposition = new CompositionCohortDefinition();
		patientsSeenComposition.setName("patientsSeenComposition");
		patientsSeenComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenComposition.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientWithDDB, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenComposition.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenComposition.setCompositionString("1 OR 2");
		
		CohortIndicator patientsSeenQuarterIndicator = Indicators.newCountIndicator("patientsSeenMonthThreeIndicator",
		    patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
		CohortIndicator patientsSeenMonthOneIndicator = Indicators.newCountIndicator("patientsSeenMonthOneIndicator",
		    patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m+1d},onOrBefore=${endDate}"));
		CohortIndicator patientsSeenMonthTwoIndicator = Indicators.newCountIndicator("patientsSeenMonthTwoIndicator",
		    patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-2m+1d},onOrBefore=${endDate-1m+1d}"));
		CohortIndicator patientsSeenMonthThreeIndicator = Indicators.newCountIndicator("patientsSeenMonthThreeIndicator",
		    patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate-2m+1d}"));
		
		
		
		
		//=================================================
		//     Adding columns to data set definition     //
		//=================================================
		
		dsd.addColumn("A2Q", "Total # of patients seen in the last quarter", new Mapped(patientsSeenQuarterIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A2QM1", "Total # of patients seen in the last month one", new Mapped(patientsSeenMonthOneIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A2QM2", "Total # of patients seen in the last month two", new Mapped(patientsSeenMonthTwoIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A2QM3", "Total # of patients seen in the last month three", new Mapped(
		        patientsSeenMonthThreeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");*/
		
	}
	
	private void createMonthlyIndicators(CohortIndicatorDataSetDefinition dsd) {
		//=======================================================
		// B3: Of the new patients enrolled in the last month, % with documented peak flow taken both before and after salbutamol at intake
		//=======================================================
		
		ProgramEnrollmentCohortDefinition enrolledInAthmaProgram=Cohorts.createProgramEnrollmentParameterizedByStartEndDate("enrolledInAthmaProgram", asthmaProgram);
		
		
		//SqlCohortDefinition patientsWithBothPeakFlowInSameDDBForm=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("patientsWithBothPeakFlowInSameDDBForm", DDBforms, peacFlowConcepts);
		
		SqlCohortDefinition patientsWithBothPeakFlowInSameDDBForm=Cohorts.getPatientsWithTwoObservationsBothInFormBetweenStartAndEndDate("patientsWithBothPeakFlowInSameDDBForm", DDBform, peakFlowAfterSalbutamol,peakFlowBeforeSalbutamol);
		
		
		
		
		CohortIndicator enrolledInAthmaProgramIndicator = Indicators.newCountIndicator("enrolledInAthmaProgramIndicator",
			enrolledInAthmaProgram,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));
		
		CohortIndicator patientsWithBothPeakFlowInSameDDBFormIndicator = Indicators.newCountIndicator("patientsWithBothPeakFlowInSameDDBFormIndicator",
			patientsWithBothPeakFlowInSameDDBForm,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		
		
		//=======================================================
		// B4: Of the new patients enrolled in the last month, % with smoking status documented at intake
		//=======================================================
		
		SqlCohortDefinition patientsWithSmokingHistory=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("patientsWithSmokingHistory",DDBform,smokingHistory);
		
		CohortIndicator patientsWithSmokingHistoryIndicator = Indicators.newCountIndicator("patientsWithSmokingHistoryIndicator",
			patientsWithSmokingHistory,
		ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		
		//=======================================================
		// D1: Of total patients seen in the last month, % with no asthma/COPD-related regimen documented ever (asthma meds: salbutamol, beclomethasone, prednisolone, aminophyilline)
		//=======================================================
	
		
		
		SqlCohortDefinition patientsWithAsthmaVisit = new SqlCohortDefinition();				
		patientsWithAsthmaVisit.setQuery("select distinct patient_id from encounter where encounter_type="
				                + asthmaEncounterType.getId()
				                + " and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0");
		patientsWithAsthmaVisit.setName("patientsWithAsthmaVisit");
		patientsWithAsthmaVisit.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithAsthmaVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		
		SqlCohortDefinition patientsHaveDrugOrdersInAsthmaMedication=Cohorts.getPatientsEverNotOnRegimen("patientsHaveDrugOrdersInAsthmaMedication", asthmasMedications);
		
		
		CompositionCohortDefinition patientsWithAsthmaVisitAndEverNotOnRegimen=new CompositionCohortDefinition();
		patientsWithAsthmaVisitAndEverNotOnRegimen.setName("patientsWithAsthmaVisitAndEverNotOnRegimen");
		patientsWithAsthmaVisitAndEverNotOnRegimen.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithAsthmaVisitAndEverNotOnRegimen.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithAsthmaVisitAndEverNotOnRegimen.addSearch("1", patientsWithAsthmaVisit, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		patientsWithAsthmaVisitAndEverNotOnRegimen.addSearch("2", patientsHaveDrugOrdersInAsthmaMedication, null);
		patientsWithAsthmaVisitAndEverNotOnRegimen.setCompositionString("1 AND (NOT 2)");
		
		
		CohortIndicator patientsWithAsthmaVisitIndicator = Indicators.newCountIndicator("patientsWithAsthmaVisitIndicator",
			patientsWithAsthmaVisit,
		ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		
		
		CohortIndicator patientsWithAsthmaVisitAndEverNotOnRegimenIndicator = Indicators.newCountIndicator("patientsEverNotOnRegimenIndicator",
		patientsWithAsthmaVisitAndEverNotOnRegimen,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
			
		
		

		
		
		
		//========================================================
		//        Adding columns to data set definition         //
		//========================================================
		
		
		dsd.addColumn("B3N", "patients with documented peak flow taken both before and after salbutamol at intake", new Mapped(patientsWithBothPeakFlowInSameDDBFormIndicator,
	        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		dsd.addColumn("B3D", "new patients enrolled in report period", new Mapped(enrolledInAthmaProgramIndicator,
	        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		
		dsd.addColumn("B4N", "patients with smoking status documented at intake", new Mapped(patientsWithSmokingHistoryIndicator,
	        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		
		dsd.addColumn("D1N", "patients with no asthma/COPD-related regimen documented ever (asthma meds: salbutamol, beclomethasone, prednisolone, aminophyilline)", new Mapped(patientsWithAsthmaVisitAndEverNotOnRegimenIndicator,
	        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		dsd.addColumn("D1D", "Of total patients seen in report period", new Mapped(patientsWithAsthmaVisitIndicator,
	        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		
		
		
	}
	
	private void setUpProperties() {
		asthmaProgram = gp.getProgram(GlobalPropertiesManagement.CHRONIC_RESPIRATORY_PROGRAM);
		asthmaPrograms.add(asthmaProgram);
		asthmaEncounterType = gp.getEncounterType(GlobalPropertiesManagement.ASTHMA_VISIT);
		DDBform = gp.getForm(GlobalPropertiesManagement.ASTHMA_DDB);
		rendevousForm=gp.getForm(GlobalPropertiesManagement.ASTHMA_RENDEVOUS_VISIT_FORM);
		
		DDBforms.add(DDBform);

		
		onOrAfterOnOrBefore.add("onOrAfter");
		onOrAfterOnOrBefore.add("onOrBefore");
		
		enrolledOnOrAfterOnOrBefore.add("enrolledOnOrAfter");
		enrolledOnOrAfterOnOrBefore.add("enrolledOnOrBefore");
		
		
		peakFlowAfterSalbutamol=gp.getConcept(GlobalPropertiesManagement.PEAK_FLOW_AFTER_SALBUTAMOL);
		peakFlowBeforeSalbutamol=gp.getConcept(GlobalPropertiesManagement.PEAK_FLOW_BEFORE_SALBUTAMOL);
		
		smokingHistory=gp.getConcept(GlobalPropertiesManagement.SMOKING_HISTORY);
		
		asthmasMedications=gp.getConceptsByConceptSet(GlobalPropertiesManagement.CHRONIC_RESPIRATORY_DISEASE_TREATMENT_DRUGS);
		
		
	}
}
