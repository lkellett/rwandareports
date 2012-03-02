package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.Form;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.dataset.EncounterIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.indicator.EncounterIndicator;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.EncounterQuerys;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

public class SetupPMTCTFormCompletionSheet {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	private List<String> onOrAfterOnOrBefore = new ArrayList<String>();
	
	private Program pmtctCombinedInfantProgram;
	
	private Program pmtctPregnancyProgram;
	
	private Program pmtctCombinedMotherProgram;
	
	private List<Program> programs = new ArrayList<Program>();
	
	//properties retrieved from global variables
	
	private Form pmtctDDB;
	
	private Form pmtctRDV;
	
	private Form adultFlowVisit;
	
	private Form transferToPMTCT;
	
	private Form transferToCC;
	
	private Concept pregnancyStatus;
	
	private Concept yes;
	
	private Concept familyPlanning;
	
	private Concept birthWeightConcept;
	
	private Concept tripleDuringPregnancy;
	
	private Concept monoDuringPregnancy;
	
	private Concept noneDuringPregnancy;
	
	private Concept therapyDuringPregnancy;
	
	private List<Concept> therapyAnswers = new ArrayList<Concept>();
	
	private Concept motherCD4Concept;
	
	private Concept changeToArtificial;
	
	private Concept nextVisitConcept;
	
	private Concept ddrConcept;
	
	private Concept dpaConcept;
	
	private Concept pregnancyTestConcept;
	
	private Concept dateDelivery;
	
	
    public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = createCrossSiteReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd,
		    "PMTCTFormCompletion.xls", "PMTCT Form Completion Excel", null);
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,dataset:DataSet");
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PMTCT Form Completion Excel".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("PMTCT Form Completion");
	}
	
	private ReportDefinition createCrossSiteReportDefinition() {
		
		ReportDefinition rd = new ReportDefinition();
		rd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		rd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");
		rd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		
		rd.setName("PMTCT Form Completion");
		
		rd.addDataSetDefinition(createDataSet(),
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));
		
		rd.setBaseCohortDefinition(Cohorts.createInProgramParameterizableByDate("InPMTCT", programs, onOrAfterOnOrBefore), ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}"));
		
		h.saveReportDefinition(rd);
		
		return rd;
	}
	
	private LocationHierachyIndicatorDataSetDefinition createDataSet() {
		
		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(createBaseDataSet());
		ldsd.setName("DataSet");
		ldsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		return ldsd;
	}
	
	private EncounterIndicatorDataSetDefinition createBaseDataSet() {
		
		EncounterIndicatorDataSetDefinition eidsd = new EncounterIndicatorDataSetDefinition();
	
		eidsd.setName("DataSet");
		eidsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		eidsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		createIndicators(eidsd);
		return eidsd;
	}
	
	private void createIndicators(EncounterIndicatorDataSetDefinition dsd) {
		
		SqlEncounterQuery patientsWithVisit = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentGroupedByPatient("patientsWithVisit", adultFlowVisit, pmtctPregnancyProgram);
		SqlEncounterQuery visitForms = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentGroupedByPatient("visitForms", adultFlowVisit, pmtctPregnancyProgram);
		SqlEncounterQuery pregnantYes = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentContainingCodedObservationValue("pregnantYes", adultFlowVisit, pregnancyStatus, yes, pmtctPregnancyProgram);
		
		SqlEncounterQuery patientsCCWithVisit = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentGroupedByPatient("patientsCCWithVisit", adultFlowVisit, pmtctCombinedMotherProgram);
		SqlEncounterQuery visitCCForms = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentGroupedByPatient("visitCCForms", adultFlowVisit, pmtctCombinedMotherProgram);
		SqlEncounterQuery contraception = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentContainingObservation("contraception", adultFlowVisit, familyPlanning, pmtctCombinedMotherProgram);
		
		SqlEncounterQuery expDDB = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentGroupedByPatient("expDDB", pmtctDDB, pmtctCombinedInfantProgram);
		SqlEncounterQuery patientsExpDDB = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentGroupedByPatient("patientsExpDDB", pmtctDDB, pmtctCombinedInfantProgram);
		
		SqlEncounterQuery birthWeight = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentContainingObservation("birthWeight", pmtctDDB, birthWeightConcept, pmtctCombinedInfantProgram);
		SqlEncounterQuery therapyWhilePregnant = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentContainingCodedObservationValue("therapyWhilePregnant", pmtctDDB, therapyDuringPregnancy, therapyAnswers, pmtctCombinedInfantProgram);
		SqlEncounterQuery motherCD4 = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentContainingObservation("motherCD4", pmtctDDB, motherCD4Concept, pmtctCombinedInfantProgram);
		
		SqlEncounterQuery expRDV = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentGroupedByPatient("expRDV", pmtctRDV, pmtctCombinedInfantProgram);
		SqlEncounterQuery patientsExpRDV = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentGroupedByPatient("patientsExpRDV", pmtctRDV, pmtctCombinedInfantProgram);
		
		SqlEncounterQuery artificial = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentContainingObservation("artificial", pmtctRDV, changeToArtificial, pmtctCombinedInfantProgram);
		SqlEncounterQuery nextVisit = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentContainingObservation("nextVisit", pmtctRDV, nextVisitConcept, pmtctCombinedInfantProgram);
		
		SqlEncounterQuery pregTrans = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentGroupedByPatient("pregTrans", transferToPMTCT, pmtctPregnancyProgram);
		SqlEncounterQuery patientsPregTrans = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentGroupedByPatient("patientsPregTrans", transferToPMTCT, pmtctPregnancyProgram);
		
		SqlEncounterQuery dpa = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentContainingObservation("dpa", transferToPMTCT, dpaConcept, pmtctPregnancyProgram);
		SqlEncounterQuery ddr = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentContainingObservation("ddr", transferToPMTCT, ddrConcept, pmtctPregnancyProgram);
		SqlEncounterQuery pregnancyTest = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentContainingObservation("pregnancyTest", transferToPMTCT, pregnancyTestConcept, pmtctPregnancyProgram);
		
		SqlEncounterQuery ccTrans = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentGroupedByPatient("ccTrans", transferToCC, pmtctCombinedMotherProgram);
		SqlEncounterQuery patientsCCTrans = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentGroupedByPatient("patientsCCTrans", transferToCC, pmtctCombinedMotherProgram);
		
		SqlEncounterQuery delivery = EncounterQuerys.getFormsBetweenStartEndDatesForAProgramEnrollmentContainingObservation("delivery", transferToPMTCT, dateDelivery, pmtctCombinedMotherProgram);
		
		EncounterIndicator one = new EncounterIndicator();
		one.setName("1");
		one.setEncounterQuery(new Mapped<EncounterQuery>(patientsWithVisit,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator two = new EncounterIndicator();
		two.setName("2");
		two.setEncounterQuery(new Mapped<EncounterQuery>(visitForms,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator three = new EncounterIndicator();
		three.setName("3");
		three.setEncounterQuery(new Mapped<EncounterQuery>(pregnantYes,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator four = new EncounterIndicator();
		four.setName("4");
		four.setEncounterQuery(new Mapped<EncounterQuery>(patientsCCWithVisit,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator five = new EncounterIndicator();
		five.setName("5");
		five.setEncounterQuery(new Mapped<EncounterQuery>(visitCCForms,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator six = new EncounterIndicator();
		six.setName("6");
		six.setEncounterQuery(new Mapped<EncounterQuery>(contraception,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator seven = new EncounterIndicator();
		seven.setName("7");
		seven.setEncounterQuery(new Mapped<EncounterQuery>(expDDB,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator eight = new EncounterIndicator();
		eight.setName("8");
		eight.setEncounterQuery(new Mapped<EncounterQuery>(patientsExpDDB,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator nine = new EncounterIndicator();
		nine.setName("9");
		nine.setEncounterQuery(new Mapped<EncounterQuery>(birthWeight,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator ten = new EncounterIndicator();
		ten.setName("10");
		ten.setEncounterQuery(new Mapped<EncounterQuery>(therapyWhilePregnant,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator eleven = new EncounterIndicator();
		eleven.setName("11");
		eleven.setEncounterQuery(new Mapped<EncounterQuery>(motherCD4,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator twelve = new EncounterIndicator();
		twelve.setName("12");
		twelve.setEncounterQuery(new Mapped<EncounterQuery>(expRDV,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator thirteen = new EncounterIndicator();
		thirteen.setName("13");
		thirteen.setEncounterQuery(new Mapped<EncounterQuery>(patientsExpRDV,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator fourteen = new EncounterIndicator();
		fourteen.setName("14");
		fourteen.setEncounterQuery(new Mapped<EncounterQuery>(artificial,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator fifteen = new EncounterIndicator();
		fifteen.setName("15");
		fifteen.setEncounterQuery(new Mapped<EncounterQuery>(nextVisit,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator sixteen = new EncounterIndicator();
		sixteen.setName("16");
		sixteen.setEncounterQuery(new Mapped<EncounterQuery>(pregTrans,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator seventeen = new EncounterIndicator();
		seventeen.setName("17");
		seventeen.setEncounterQuery(new Mapped<EncounterQuery>(patientsPregTrans,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator eighteen = new EncounterIndicator();
		eighteen.setName("18");
		eighteen.setEncounterQuery(new Mapped<EncounterQuery>(dpa,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator nineteen = new EncounterIndicator();
		nineteen.setName("19");
		nineteen.setEncounterQuery(new Mapped<EncounterQuery>(ddr,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator twenty = new EncounterIndicator();
		twenty.setName("20");
		twenty.setEncounterQuery(new Mapped<EncounterQuery>(pregnancyTest,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator twentyone = new EncounterIndicator();
		twentyone.setName("21");
		twentyone.setEncounterQuery(new Mapped<EncounterQuery>(ccTrans,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator twentytwo = new EncounterIndicator();
		twentytwo.setName("22");
		twentytwo.setEncounterQuery(new Mapped<EncounterQuery>(patientsCCTrans,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		EncounterIndicator twentythree = new EncounterIndicator();
		twentythree.setName("23");
		twentythree.setEncounterQuery(new Mapped<EncounterQuery>(delivery,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		dsd.addColumn(one);
		dsd.addColumn(two);
		dsd.addColumn(three);
		dsd.addColumn(four);
		dsd.addColumn(five);
		dsd.addColumn(six);
		dsd.addColumn(seven);
		dsd.addColumn(eight);
		dsd.addColumn(nine);
		dsd.addColumn(ten);
		dsd.addColumn(eleven);
		dsd.addColumn(twelve);
		dsd.addColumn(thirteen);
		dsd.addColumn(fourteen);
		dsd.addColumn(fifteen);
		dsd.addColumn(sixteen);
		dsd.addColumn(seventeen);
		dsd.addColumn(eighteen);
		dsd.addColumn(nineteen);
		dsd.addColumn(twenty);
		dsd.addColumn(twentyone);
		dsd.addColumn(twentytwo);
		dsd.addColumn(twentythree);
	}
	
	private void setUpProperties() {
		pmtctDDB = gp.getForm(GlobalPropertiesManagement.PMTCT_DDB);
		
		pmtctRDV = gp.getForm(GlobalPropertiesManagement.PMTCT_RDV);
		
		pmtctCombinedInfantProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM);
		pmtctCombinedMotherProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_MOTHER_PROGRAM);
		pmtctPregnancyProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
		
		programs.add(pmtctCombinedInfantProgram);
		programs.add(pmtctCombinedMotherProgram);
		programs.add(pmtctPregnancyProgram);
		
		adultFlowVisit = gp.getForm(GlobalPropertiesManagement.ADULT_FLOW_VISIT);
		
		transferToPMTCT = gp.getForm(GlobalPropertiesManagement.TRANSFER_TO_PMTCT);
		
		transferToCC = gp.getForm(GlobalPropertiesManagement.TRANSFER_TO_CC);
		
		pregnancyStatus = gp.getConcept(GlobalPropertiesManagement.PREGNANCY_STATUS);
		
		yes = gp.getConcept(GlobalPropertiesManagement.YES);
		
		familyPlanning = gp.getConcept(GlobalPropertiesManagement.METHOD_OF_FAMILY_PLANNING);
		
		birthWeightConcept = gp.getConcept(GlobalPropertiesManagement.BIRTH_WEIGHT);
		
		tripleDuringPregnancy = gp.getConcept(GlobalPropertiesManagement.TRIPLE_THERAPY_DURING_PREGNANCY);
		
		monoDuringPregnancy = gp.getConcept(GlobalPropertiesManagement.MONO_THERAPY_DURING_PREGNANCY);
		
		noneDuringPregnancy = gp.getConcept(GlobalPropertiesManagement.NO_THERAPY_DURING_PREGNANCY);
		
		therapyDuringPregnancy = gp.getConcept(GlobalPropertiesManagement.PROPHYLAXIS_FOR_MOTHER_IN_PMTCT);
		
		motherCD4Concept = gp.getConcept(GlobalPropertiesManagement.MOTHER_CD4);
		
		changeToArtificial = gp.getConcept(GlobalPropertiesManagement.CHANGE_TO_ARTIFICIAL_MILK);
		
		nextVisitConcept = gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE);
		
		dpaConcept = gp.getConcept(GlobalPropertiesManagement.DPA);
		
		ddrConcept = gp.getConcept(GlobalPropertiesManagement.DDR);
		
		pregnancyTestConcept = gp.getConcept(GlobalPropertiesManagement.PREGNANCY_TEST_DATE);
		
		dateDelivery = gp.getConcept(GlobalPropertiesManagement.PREGNANCY_DELIVERY_DATE);
		
		therapyAnswers.add(tripleDuringPregnancy);
		therapyAnswers.add(monoDuringPregnancy);
		therapyAnswers.add(noneDuringPregnancy);
		
		onOrAfterOnOrBefore.add("onOrAfter");
		onOrAfterOnOrBefore.add("onOrBefore");
	}
}
