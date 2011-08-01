package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CurrentOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstRecordedObservationWithCodedConceptAnswer;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObservationInMostRecentEncounterOfType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rwandareports.customcalculators.Alerts;
import org.openmrs.module.rwandareports.customcalculators.BreastFeedingOrFormula;
import org.openmrs.module.rwandareports.customcalculators.DDR;
import org.openmrs.module.rwandareports.customcalculators.DPA;
import org.openmrs.module.rwandareports.customcalculators.DecisionDate;
import org.openmrs.module.rwandareports.customcalculators.GestationalAge;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.comparator.PMTCTDataSetRowComparator;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.filter.DrugNameFilter;
import org.openmrs.module.rwandareports.filter.LastThreeObsFilter;
import org.openmrs.module.rwandareports.filter.ObservationFilter;
import org.openmrs.module.rwandareports.filter.RemoveDecimalFilter;

public class SetupPMTCTPregnancyConsultationReport {
	
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupPMTCTPregnancyConsultationReport(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		setUpGlobalProperties();
		
		createCohortDefinitions();
		ReportDefinition rd = createReportDefinition();
//		h.createRowPerPatientXlsOverview(rd, "PMTCTPregnancyConsultationSheet.xls", "PMTCTPregnancyConsultationSheet.xls_", null);
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "PMTCTPregnancyConsultationSheetV2.xls", "PMTCTPregnancyConsultationSheet.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:6,dataset:dataSet");
	
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("PMTCTPregnancyConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(ReportDefinition.class, "PMTCT Pregnancy consultation");
		
		h.purgeDefinition(HIVARTRegisterDataSetDefinition.class, "PMTCT Pregnancy consultation Data Set");
		
		h.purgeDefinition(CohortDefinition.class, "PMTCTPregLocation: Patients at location");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("PMTCT Pregnancy consultation");
		
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		reportDefinition.addParameter(new Parameter("date", "Week starting on", Date.class));
		reportDefinition.setBaseCohortDefinition(h.cohortDefinition("PMTCTPregLocation: Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.replaceReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition)
	{
		// Create new dataset definition 
		PatientDataSetDefinition dataSetDefinition = new PatientDataSetDefinition();
		dataSetDefinition.addParameter(new Parameter("date", "Date", Date.class));
		dataSetDefinition.setName(reportDefinition.getName() + " Data Set");
		dataSetDefinition.setComparator(new PMTCTDataSetRowComparator());
		
		InProgramCohortDefinition inPMTCTProgram = new InProgramCohortDefinition();
		inPMTCTProgram.setName("pmtct: In Program");
		List<Program> programs = new ArrayList<Program>();
		Program pmtct = Context.getProgramWorkflowService().getProgramByName(properties.get("PMTCT_PROGRAM"));
		inPMTCTProgram.setOnDate(Calendar.getInstance().getTime());
		if(pmtct != null)
		{
			programs.add(pmtct);
		}
		inPMTCTProgram.setPrograms(programs);
		dataSetDefinition.addFilter(inPMTCTProgram, new HashMap<String,Object>());
		
		Concept nextVisitConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("PMTCT_NEXT_VISIT_CONCEPT_ID")));
		
		DateObsCohortDefinition dueThatWeek = new DateObsCohortDefinition();
		dueThatWeek.setOperator1(RangeComparator.GREATER_EQUAL);
		dueThatWeek.setOperator2(RangeComparator.LESS_EQUAL);
		dueThatWeek.setTimeModifier(TimeModifier.ANY);
		dueThatWeek.addParameter(new Parameter("value1", "value1", Date.class));
		dueThatWeek.addParameter(new Parameter("value2", "value2", Date.class));
		dueThatWeek.setName("patients due that week");
		dueThatWeek.setQuestion(nextVisitConcept);
		dataSetDefinition.addFilter(dueThatWeek, ParameterizableUtil.createParameterMappings("value1=${date},value2=${date+7d}"));
		
		PatientProperty givenName = new PatientProperty("givenName");
		dataSetDefinition.addColumn(givenName, new HashMap<String,Object>());
		
		PatientProperty familyName = new PatientProperty("familyName");
		dataSetDefinition.addColumn(familyName, new HashMap<String,Object>());
		
		PatientIdentifierType imbType = Context.getPatientService().getPatientIdentifierTypeByName(properties.get("IMB_IDENTIFIER_TYPE"));
		PatientIdentifier imbId = new PatientIdentifier(imbType);
		imbType.setPatientIdentifierTypeId(imbId.getId());
		
		PatientIdentifierType pcType = Context.getPatientService().getPatientIdentifierTypeByName(properties.get("PRIMARY_CARE_IDENTIFIER_TYPE"));
		PatientIdentifier pcId = new PatientIdentifier(pcType);
		pcType.setPatientIdentifierTypeId(pcId.getId());
		
		MultiplePatientDataDefinitions infantId = new MultiplePatientDataDefinitions();
		infantId.setName("Id");
		infantId.addPatientDataDefinition(imbId, new HashMap<String,Object>());
		infantId.addPatientDataDefinition(pcId, new HashMap<String,Object>());
		dataSetDefinition.addColumn(infantId, new HashMap<String,Object>());
		
		DateFormatFilter dateFilter = new DateFormatFilter();
		dateFilter.setFinalDateFormat("dd-MMM-yyyy");
		
		FirstRecordedObservationWithCodedConceptAnswer diagnosisDate = new FirstRecordedObservationWithCodedConceptAnswer();
		diagnosisDate.setName("hivDiagnosis");
		diagnosisDate.setDescription("hivDiagnosis");
		Concept question = Context.getConceptService().getConcept(Integer.valueOf(properties.get("HIV_TEST_CONCEPT")));
		Concept answer = Context.getConceptService().getConcept(Integer.valueOf(properties.get("POSITIVE_HIV_RESULT_CONCEPT")));
		diagnosisDate.setAnswerRequired(answer);
		diagnosisDate.setQuestion(question);
		diagnosisDate.setDateFormat("dd-MMM-yyyy");
		dataSetDefinition.addColumn(diagnosisDate, new HashMap<String, Object>());
		
		MostRecentObservation ddr = new MostRecentObservation();
		Concept ddrConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("DDR_CONCEPT")));
		ddr.setConcept(ddrConcept);
		ddr.setName("ddr");
		ddr.setDescription("ddr");
		ddr.setFilter(dateFilter);
		
		MostRecentObservation dpa = new MostRecentObservation();
		Concept dpaConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("DPA_CONCEPT")));
		dpa.setConcept(dpaConcept);
		dpa.setName("dpa");
		dpa.setDescription("dpa");
		dpa.setFilter(dateFilter);
		
		MostRecentObservation cd4Test = new MostRecentObservation();
		Concept cd4Concept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("CD4_CONCEPT")));
		cd4Test.setConcept(cd4Concept);
		cd4Test.setName("CD4Test");
		cd4Test.setFilter(new RemoveDecimalFilter());
		cd4Test.setDateFormat("dd-MMM-yyyy");
		dataSetDefinition.addColumn(cd4Test, new HashMap<String, Object>());
		
		PatientRelationship accomp = new PatientRelationship();
		accomp.setName("AccompName");
		accomp.setRelationshipTypeId(Integer.valueOf(properties.get("ACCOMPAGNATUER_RELATIONSHIP_ID")));
		accomp.setRetrievePersonAorB("A");
		dataSetDefinition.addColumn(accomp, new HashMap<String,Object>());
		
		PatientAddress address = new PatientAddress();
		address.setName("Sector");
		address.setIncludeCountry(false);
		address.setIncludeProvince(false);
		address.setIncludeDistrict(false);
		address.setIncludeCell(false);
		address.setIncludeUmudugudu(false);
		dataSetDefinition.addColumn(address, new HashMap<String,Object>());
		
		PatientAddress address2 = new PatientAddress();
		address2.setName("Cell");
		address2.setIncludeCountry(false);
		address2.setIncludeProvince(false);
		address2.setIncludeDistrict(false);
		address2.setIncludeSector(false);
		address2.setIncludeUmudugudu(false);
		dataSetDefinition.addColumn(address2, new HashMap<String,Object>());
		
		PatientAddress address3 = new PatientAddress();
		address3.setName("Umudugudu");
		address3.setIncludeCountry(false);
		address3.setIncludeProvince(false);
		address3.setIncludeDistrict(false);
		address3.setIncludeSector(false);
		address3.setIncludeCell(false);
		dataSetDefinition.addColumn(address3, new HashMap<String,Object>());
		
		PatientAddress address4 = new PatientAddress();
		address4.setName("District");
		address4.setIncludeCountry(false);
		address4.setIncludeProvince(false);
		address4.setIncludeSector(false);
		address4.setIncludeCell(false);
		address4.setIncludeUmudugudu(false);
		dataSetDefinition.addColumn(address4, new HashMap<String,Object>());
		
		Concept artDrugsSet = Context.getConceptService().getConcept(new Integer(properties.get("ALL_ART_DRUGS_CONCEPT")));
		
		CurrentOrdersRestrictedByConceptSet artDrugs = new CurrentOrdersRestrictedByConceptSet();
		artDrugs.addParameter(new Parameter("onDate", "OnDate", ProgramWorkflowState.class));
		artDrugs.setName("Regimen");
		artDrugs.setDescription("Regimen");
		artDrugs.setDrugConceptSetConcept(artDrugsSet);
		artDrugs.setDateFormat("dd-MMM-yyyy");
		artDrugs.setDrugFilter(new DrugNameFilter());
		dataSetDefinition.addColumn(artDrugs, new HashMap<String,Object>());
		
		MostRecentObservation nextVisit = new MostRecentObservation();
		nextVisit.setConcept(nextVisitConcept);
		nextVisit.setName("nextVisit");
		nextVisit.setDescription("nextVisit");
		nextVisit.setFilter(dateFilter);
		dataSetDefinition.addColumn(nextVisit, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions gestationalAge = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		gestationalAge.addPatientDataToBeEvaluated(ddr, new HashMap<String, Object>());
		gestationalAge.setName("gestationalAge");
		gestationalAge.setDescription("gestationalAge");
		gestationalAge.setCalculator(new GestationalAge());
		dataSetDefinition.addColumn(gestationalAge, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions decisionDate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		decisionDate.addPatientDataToBeEvaluated(artDrugs, new HashMap<String, Object>());
		decisionDate.setName("decisionDate");
		decisionDate.setDescription("decisionDate");
		decisionDate.setCalculator(new DecisionDate());
		dataSetDefinition.addColumn(decisionDate, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions ddrDate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		ddrDate.addPatientDataToBeEvaluated(ddr, new HashMap<String, Object>());
		ddrDate.addPatientDataToBeEvaluated(dpa, new HashMap<String, Object>());
		ddrDate.setName("ddrCalc");
		ddrDate.setDescription("ddrCalc");
		ddrDate.setCalculator(new DDR());
		dataSetDefinition.addColumn(ddrDate, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions dpaDate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		dpaDate.addPatientDataToBeEvaluated(ddr, new HashMap<String, Object>());
		dpaDate.addPatientDataToBeEvaluated(dpa, new HashMap<String, Object>());
		dpaDate.setName("dpaCalc");
		dpaDate.setDescription("dpaCalc");
		dpaDate.setCalculator(new DPA());
		dataSetDefinition.addColumn(dpaDate, new HashMap<String, Object>());
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions bOrF = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		bOrF.setName("bOrF");
		bOrF.addPatientDataToBeEvaluated(decisionDate, new HashMap<String, Object>());
		bOrF.addPatientDataToBeEvaluated(cd4Test, new HashMap<String, Object>());
		bOrF.setCalculator(new BreastFeedingOrFormula());
		dataSetDefinition.addColumn(bOrF, new HashMap<String, Object>());
		
		AllObservationValues weight = new AllObservationValues();
		Concept weightConcept = Context.getConceptService().getConcept(new Integer(properties.get("WEIGHT_CONCEPT")));
		weight.setConcept(weightConcept);
		weight.setName("weightObs");
		weight.setFilter(new LastThreeObsFilter());
		weight.setDateFormat("ddMMMyy");
		weight.setOutputFilter(new ObservationFilter());
		
		ObservationInMostRecentEncounterOfType io = new ObservationInMostRecentEncounterOfType();
		Concept ioConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("IO_CONCEPT")));
		io.setName("IO");
		io.setObservationConcept(ioConcept);
		EncounterType flowsheetEncounter = Context.getEncounterService().getEncounterType(Integer.valueOf(properties.get("FLOWSHEET_ENCOUNTER")));
		List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
		encounterTypes.add(flowsheetEncounter);
		io.setEncounterTypes(encounterTypes);
		
		ObservationInMostRecentEncounterOfType sideEffect = new ObservationInMostRecentEncounterOfType();
		Concept sideEffectConcept = Context.getConceptService().getConcept(Integer.valueOf(properties.get("SIDE_EFFECT_CONCEPT")));
		sideEffect.setName("SideEffects");
		sideEffect.setObservationConcept(sideEffectConcept);
		encounterTypes.add(flowsheetEncounter);
		sideEffect.setEncounterTypes(encounterTypes);
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(cd4Test, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(gestationalAge, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(weight, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(sideEffect, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(io, new HashMap<String, Object>());
		alert.setCalculator(new Alerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("date", "${date}");
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	
	
	private void createCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pa.voided = 0 and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
		location.setName("PMTCTPregLocation: Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
		
	}
	
	
	
	private void setUpGlobalProperties()
	{
		properties = new HashMap<String, String>();
		
		String pmtctProgram = Context.getAdministrationService().getGlobalProperty("reports.pmtctprogramname");
		properties.put("PMTCT_PROGRAM", pmtctProgram);
		
		String identifierType = Context.getAdministrationService().getGlobalProperty("reports.imbIdIdentifier");
		properties.put("IMB_IDENTIFIER_TYPE", identifierType);
		
		String pcIdentifierType = Context.getAdministrationService().getGlobalProperty("reports.primaryCareIdIdentifier");
		properties.put("PRIMARY_CARE_IDENTIFIER_TYPE", pcIdentifierType);
		
		String hivConcept = Context.getAdministrationService().getGlobalProperty("reports.hivTestConcept");
		properties.put("HIV_TEST_CONCEPT", hivConcept);
		
		String positiveHivConcept = Context.getAdministrationService().getGlobalProperty("reports.positiveHivTestConcept");
		properties.put("POSITIVE_HIV_RESULT_CONCEPT", positiveHivConcept);
		
		String ddrConcept = Context.getAdministrationService().getGlobalProperty("reports.ddrConcept");
		properties.put("DDR_CONCEPT", ddrConcept);
		
		String dpaConcept = Context.getAdministrationService().getGlobalProperty("reports.dpaConcept");
		properties.put("DPA_CONCEPT", dpaConcept);
		
		String allARTDrugsConcept = Context.getAdministrationService().getGlobalProperty("reports.allArtDrugsConceptSet");
		properties.put("ALL_ART_DRUGS_CONCEPT", allARTDrugsConcept);
		
		String cd4Concept = Context.getAdministrationService().getGlobalProperty("reports.cd4Concept");
		properties.put("CD4_CONCEPT", cd4Concept);
		
		String nextVisitConcept = Context.getAdministrationService().getGlobalProperty("reports.pmtctNextVisitConcept");
		properties.put("PMTCT_NEXT_VISIT_CONCEPT_ID", nextVisitConcept);
		
		String accompType = Context.getAdministrationService().getGlobalProperty("reports.accompagnatuerRelationship");
		properties.put("ACCOMPAGNATUER_RELATIONSHIP_ID", accompType);
		
		String ioConcept = Context.getAdministrationService().getGlobalProperty("reports.ioConcept");
		properties.put("IO_CONCEPT", ioConcept);
		
		String flowsheetEncounter = Context.getAdministrationService().getGlobalProperty("reports.adultflowsheetencounter");
		properties.put("FLOWSHEET_ENCOUNTER", flowsheetEncounter);
		
		String sideEffectConcept = Context.getAdministrationService().getGlobalProperty("reports.sideEffectConcept");
		properties.put("SIDE_EFFECT_CONCEPT", sideEffectConcept);
		
		String weightConcept = Context.getAdministrationService().getGlobalProperty("reports.weightConcept");
		properties.put("WEIGHT_CONCEPT", weightConcept);
	}	
	
}
