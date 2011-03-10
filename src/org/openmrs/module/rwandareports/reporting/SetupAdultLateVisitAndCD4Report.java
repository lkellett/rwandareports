package org.openmrs.module.rwandareports.reporting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.record.formula.functions.Setname;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.context.Context;
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientDateOfBirth;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentEncounterDate;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentEncounterType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentObservationDate;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ReturnVisitDate;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InProgramCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InStateCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InverseCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PersonAttributeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AgeAtDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConcept;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllDrugOrdersRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfWorkflowStateChange;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedAfterDateRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.FirstDrugOrderStartedRestrictedByConceptSet;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObsValueAfterDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObsValueBeforeDateOfOtherDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rwandareports.dataset.HIVARTRegisterDataSetDefinition;
import org.openmrs.module.rwandareports.filter.GroupStateFilter;

public class SetupAdultLateVisitAndCD4Report {
	
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupAdultLateVisitAndCD4Report(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		setUpGlobalProperties();
		
		createCohortDefinitions();
		ReportDefinition rd = createReportDefinition();
		h.createRowPerPatientXlsOverview(rd, "AdultLateVisitAndCD4Template.xls", "XlsAdultLateVisitAndCD4Template", null);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsAdultLateVisitAndCD4Template".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(ReportDefinition.class, "Adult Late Visit And CD4");
		
		h.purgeDefinition(PatientDataSetDefinition.class, "Adult Late Visit And CD4 Data Set");
		
		h.purgeDefinition(CohortDefinition.class, "location: Patients at location");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Adult Late Visit And CD4");
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		reportDefinition.setBaseCohortDefinition(h.cohortDefinition("location: Patients at location"), ParameterizableUtil.createParameterMappings("location=${location}"));
		
		createDataSetDefinition(reportDefinition);
		
		h.replaceReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition)
	{
		//====================================================================
		//           Patients Dataset definition
		//====================================================================
		
		
		// Create Adult ART late visit dataset definition 
		PatientDataSetDefinition dataSetDefinition1 = new PatientDataSetDefinition();
		dataSetDefinition1.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition1.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition1.setName(reportDefinition.getName() + " Data Set");
		
		// Create Adult Pre-ART late visit dataset definition 
		PatientDataSetDefinition dataSetDefinition2 = new PatientDataSetDefinition();
		dataSetDefinition2.setName(reportDefinition.getName() + " Data Set");
		
		//Create Adult HIV late CD4 count dataset definition
		PatientDataSetDefinition dataSetDefinition3 = new PatientDataSetDefinition();
		dataSetDefinition3.setName(reportDefinition.getName() + " Data Set");
		
		//Create HIV lost to follow-up dataset definition
		PatientDataSetDefinition dataSetDefinition4 = new PatientDataSetDefinition();
		dataSetDefinition4.setName(reportDefinition.getName() + " Data Set");
		
		
		//Adult HIV program Cohort definition
		InProgramCohortDefinition adultHivProgramCohort = new InProgramCohortDefinition();
		/*adultHivProgramCohort.addParameter(new Parameter("onDate","On Date",Date.class));
		adultHivProgramCohort.addParameter(new Parameter("endDate","End Date",Date.class));
		*/
		List<Program> programs = new ArrayList<Program>();
		Program hadultHivProgram = Context.getProgramWorkflowService().getProgram(Integer.parseInt(Context.getAdministrationService().getGlobalProperty("hiv.programid.adult")));
		adultHivProgramCohort.addParameter(new Parameter("onDate","On Date",Date.class));
		programs.add(hadultHivProgram);
		adultHivProgramCohort.setPrograms(programs);
		dataSetDefinition1.addFilter(adultHivProgramCohort,	ParameterizableUtil.createParameterMappings("onDate=${endDate-9y}"));
		dataSetDefinition2.addFilter(adultHivProgramCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate-9y}"));
		dataSetDefinition3.addFilter(adultHivProgramCohort, new HashMap<String,Object>());
		dataSetDefinition4.addFilter(adultHivProgramCohort, new HashMap<String,Object>());
		
		
		/*Map<String,Object> hivProgramMapping = new HashMap<String,Object>();
		reportDefinition.setBaseCohortDefinition(adultHivProgramCohort, hivProgramMapping);
		*/		
		// Patients not Died Cohort definition.
		
		SqlCohortDefinition patientDied=new SqlCohortDefinition("SELECT DISTINCT person_id FROM obs o WHERE o.concept_id='1811'");
		InverseCohortDefinition patientAlive=new InverseCohortDefinition(patientDied);
		dataSetDefinition1.addFilter(patientAlive, new HashMap<String,Object>());
		dataSetDefinition2.addFilter(patientAlive, new HashMap<String,Object>());
		dataSetDefinition3.addFilter(patientAlive, new HashMap<String,Object>());
		dataSetDefinition4.addFilter(patientAlive, new HashMap<String,Object>());
		
		/*PersonAttributeCohortDefinition loc = new PersonAttributeCohortDefinition();			
		List<Location> locations=new ArrayList<Location>();
		locations.add(location);
		loc.setValueLocations(locations);
		dataSetDefinition1.addFilter(loc);
		dataSetDefinition2.addFilter(loc);
		dataSetDefinition3.addFilter(loc);
		dataSetDefinition4.addFilter(loc);
		*/
		//==================================================================
		//                 1. Adult ART late visit
		//==================================================================
		
		
		
		// ON ANTIRETROVIRALS state cohort definition.
		
		
		InStateCohortDefinition onARTStatusCohort = new InStateCohortDefinition();
		//onARTStatusCohort.addParameter(new Parameter("onDate","On Date",Date.class));
		List<ProgramWorkflowState> states = new ArrayList<ProgramWorkflowState>();
		ProgramWorkflow txStatus = hadultHivProgram.getWorkflowByName("TREATMENT STATUS");
		/*ProgramWorkflowState onART=txStatus.getState("ON ANTIRETROVIRALS");
		states.add(onART);
		onARTStatusCohort.setStates(states);
		dataSetDefinition1.addFilter(onARTStatusCohort);*/
		
		ProgramWorkflowState onART = null;
		if(txStatus != null)
		{
			onART = txStatus.getState(Context.getConceptService().getConceptByName("ON ANTIRETROVIRALS"));
			//onART = txStatus.getState("ON ANTIRETROVIRALS");
			if(onART != null)
			{
				states.add(onART);
				onARTStatusCohort.setOnDate(new Date());
				onARTStatusCohort.setStates(states);
				/*CompositionCohortDefinition onARTStatusCompositionCohort=new CompositionCohortDefinition();
				onARTStatusCompositionCohort.addParameter(new Parameter("endDate","End Date",Date.class));
				onARTStatusCompositionCohort.addParameter(new Parameter("onDate","On Date",Date.class));
				onARTStatusCompositionCohort.getSearches().put("onARTStatusCohort", new Mapped<CohortDefinition>(onARTStatusCohort, ParameterizableUtil.createParameterMappings("onDate=${endDate}")));
				onARTStatusCompositionCohort.setCompositionString("onARTStatusCohort");
				*/dataSetDefinition1.addFilter(onARTStatusCohort, new HashMap<String,Object>());
				
				
				/*dataSetDefinition1.addFilter(onARTStatusCohort);*/
			}
		}
		
		
		
		// Patients With Any Encounter in last Year
		SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd");
		//Date date = AdultLateVisitAndCD4ReportUtil.getEndDate();
		Calendar cal1 = Calendar.getInstance();
		//cal1.setTime(endDate);
		cal1.setTime(new Date());
		cal1.add(Calendar.MONTH, -12);
		Date dateOneYearAgo = cal1.getTime();
		
				
		SqlCohortDefinition patientsWithAnyEncounterInOneYear = new SqlCohortDefinition("select distinct e.patient_id from encounter e where e.voided=0 and e.encounter_datetime>='"+sdf1.format(dateOneYearAgo)+"' and e.encounter_datetime<='"+sdf1.format(new Date())+"'");
		dataSetDefinition1.addFilter(patientsWithAnyEncounterInOneYear, new HashMap<String,Object>());
		dataSetDefinition2.addFilter(patientsWithAnyEncounterInOneYear, new HashMap<String,Object>());
		dataSetDefinition3.addFilter(patientsWithAnyEncounterInOneYear, new HashMap<String,Object>());
	
		
		// Patients without Any clinical Encounter(Test lab excluded) in last three months.
		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(new Date());
		cal2.add(Calendar.MONTH, -3);
		Date dateThreeMountsAgo = cal2.getTime();
		
		String encTypesIds=Context.getAdministrationService().getGlobalProperty("ClinicalencounterTypeIds.labTestExcl");
		//SqlCohortDefinition patientsWithouthClinicalEncounterInThreeMonths=new SqlCohortDefinition("select distinct p.patient_id from patient p, encounter e1 where p.patient_id = e1.patient_id and e1.voided = 0 and p.voided = 0 and p.patient_id not in (select patient_id from encounter where encounter_type in ("+encTypesIds+") and encounter_datetime >= '"+sdf1.format(dateThreeMountsAgo)+"'and voided = 0)");
		SqlCohortDefinition patientsWithClinicalEncounterInThreeMonths=new SqlCohortDefinition("select distinct p.patient_id from patient p, encounter e1 where p.patient_id = e1.patient_id and e1.voided = 0 and p.voided = 0 and e1.encounter_type in ("+encTypesIds+") and e1.encounter_datetime >= '"+sdf1.format(dateThreeMountsAgo)+"'");
		InverseCohortDefinition patientsWithouthClinicalEncounterInThreeMonths=new InverseCohortDefinition(patientsWithClinicalEncounterInThreeMonths);
		dataSetDefinition1.addFilter(patientsWithouthClinicalEncounterInThreeMonths, new HashMap<String,Object>());
		
		
		//==================================================================
		//                 2. Adult Pre-ART late visit
		//==================================================================
		
		
		
		// Following state cohort definition.
		
		
		InStateCohortDefinition followingStatusCohort = new InStateCohortDefinition();
		List<ProgramWorkflowState> followingstates = new ArrayList<ProgramWorkflowState>();
		ProgramWorkflow followingtxStatus = hadultHivProgram.getWorkflowByName("TREATMENT STATUS");
		//followingStatusCohort.addParameter(new Parameter("onOrBefore","onOrBefore",Date.class));
		/*ProgramWorkflowState following=followingtxStatus.getState("FOLLOWING");
		followingstates.add(following);
		followingStatusCohort.setStates(followingstates);
		dataSetDefinition2.addFilter(followingStatusCohort);*/
		
		ProgramWorkflowState following = null;
		if(txStatus != null)
		{
			following = followingtxStatus.getState(Context.getConceptService().getConceptByName("FOLLOWING"));
			//getState("FOLLOWING");
			if(following != null)
			{
				followingstates.add(following);
				followingStatusCohort.setOnDate(new Date());
				followingStatusCohort.setStates(followingstates);
				
				/*CompositionCohortDefinition followingStatusCompositionCohort=new CompositionCohortDefinition();
				followingStatusCompositionCohort.addParameter(new Parameter("endDate","End Date",Date.class));
				followingStatusCompositionCohort.getSearches().put("followingStatusCohort", new Mapped<CohortDefinition>(followingStatusCohort, ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}")));
				followingStatusCompositionCohort.setCompositionString("followingStatusCohort");
				dataSetDefinition2.addFilter(followingStatusCompositionCohort);
		       */ 
				dataSetDefinition2.addFilter(followingStatusCohort, new HashMap<String,Object>());
			}
		}	
		
		
		
		
		// Patients without Any clinical Encounter(Test lab excluded) in last six months.
		Calendar cal3 = Calendar.getInstance();
		cal3.setTime(new Date());
		cal3.add(Calendar.MONTH, -6);
		Date dateSixMountsAgo = cal3.getTime();
		
		//SqlCohortDefinition patientsWithouthClinicalEncounterInSixMonths=new SqlCohortDefinition("select distinct p.patient_id from patient p, encounter e1 where p.patient_id = e1.patient_id and e1.voided = 0 and p.voided = 0 and p.patient_id not in (select patient_id from encounter where encounter_type in ("+encTypesIds+") and encounter_datetime >= '"+sdf1.format(dateSixMountsAgo)+"'and voided = 0)");
		SqlCohortDefinition patientsWithClinicalEncounterInSixMonths=new SqlCohortDefinition("select distinct p.patient_id from patient p, encounter e1 where p.patient_id = e1.patient_id and e1.voided = 0 and p.voided = 0 and e1.encounter_type in ("+encTypesIds+") and e1.encounter_datetime >= '"+sdf1.format(dateSixMountsAgo)+"'");
		InverseCohortDefinition patientsWithouthClinicalEncounterInSixMonths=new InverseCohortDefinition(patientsWithClinicalEncounterInSixMonths);
		dataSetDefinition2.addFilter(patientsWithouthClinicalEncounterInSixMonths, new HashMap<String,Object>());
		
			
			

		//==================================================================
		//                 3. Adult HIV late CD4 count
		//==================================================================
		
		//SqlCohortDefinition patientsWithouthCD4RecordInSixMonths=new SqlCohortDefinition("select distinct p.patient_id from patient p, obs o where p.patient_id = o.person_id and o.voided = 0 and p.voided = 0 and p.patient_id not in (select person_id from obs where concept_id='5497' and obs_datetime >= '"+sdf1.format(dateSixMountsAgo)+"' and voided = 0)");
		SqlCohortDefinition patientsWitCD4RecordInSixMonths=new SqlCohortDefinition("select distinct p.patient_id from patient p, obs o where p.patient_id = o.person_id and o.voided = 0 and p.voided = 0 and o.concept_id='5497' and o.obs_datetime >= '"+sdf1.format(dateSixMountsAgo)+"'");
		InverseCohortDefinition patientsWithouthCD4RecordInSixMonths=new InverseCohortDefinition(patientsWitCD4RecordInSixMonths);
		dataSetDefinition3.addFilter(patientsWithouthCD4RecordInSixMonths, new HashMap<String,Object>());
		

		//==================================================================
		//                 4. HIV lost to follow-up
		//==================================================================
		
		//Patients with no encounters of any kind in the past year
		
		
		//SqlCohortDefinition patientsWithoutEncountersInPastYear = new SqlCohortDefinition("select distinct p.patient_id from patient p, encounter e1 where p.patient_id = e1.patient_id and e1.voided = 0 and p.voided = 0 and e1.encounter_datetime < '"+sdf1.format(dateOneYearAgo)+"' and p.patient_id not in (select patient_id from encounter where encounter_datetime >= '"+sdf1.format(dateOneYearAgo)+"' and voided = 0)");
		SqlCohortDefinition patientsWithEncountersInPastYear = new SqlCohortDefinition("select distinct p.patient_id from patient p, encounter e1 where p.patient_id = e1.patient_id and e1.voided = 0 and p.voided = 0 and e1.encounter_datetime >= '"+sdf1.format(dateOneYearAgo)+"'");
		InverseCohortDefinition patientsWithoutEncountersInPastYear=new InverseCohortDefinition(patientsWithEncountersInPastYear);
		dataSetDefinition4.addFilter(patientsWithoutEncountersInPastYear, new HashMap<String,Object>());		
		
		
		
		
		//==================================================================
		//                 Columns of report settings
		//==================================================================
		
		
		
		PatientIdentifierType imbType = Context.getPatientService().getPatientIdentifierTypeByName("IMB ID");
		PatientIdentifier imbId = new PatientIdentifier(imbType);
		imbId.setName("IMB ID");
		imbId.setDescription("IMB ID");
		dataSetDefinition1.addColumn(imbId, new HashMap<String,Object>());
		dataSetDefinition2.addColumn(imbId, new HashMap<String,Object>());
		dataSetDefinition3.addColumn(imbId, new HashMap<String,Object>());
		dataSetDefinition4.addColumn(imbId, new HashMap<String,Object>());
		
		PatientProperty givenName = new PatientProperty("givenName");
		givenName.setName("First Name");
		givenName.setDescription("First Name");
		dataSetDefinition1.addColumn(givenName, new HashMap<String,Object>());
		dataSetDefinition2.addColumn(givenName, new HashMap<String,Object>());
		dataSetDefinition3.addColumn(givenName, new HashMap<String,Object>());
		dataSetDefinition4.addColumn(givenName, new HashMap<String,Object>());
		
		PatientProperty familyName = new PatientProperty("familyName");
		familyName.setName("Last Name");
		familyName.setDescription("Last Name");
		dataSetDefinition1.addColumn(familyName, new HashMap<String,Object>());
		dataSetDefinition2.addColumn(familyName, new HashMap<String,Object>());
		dataSetDefinition3.addColumn(familyName, new HashMap<String,Object>());
		dataSetDefinition4.addColumn(familyName, new HashMap<String,Object>());
		
		PatientProperty gender = new PatientProperty("gender");
		gender.setName("Sex");
		gender.setDescription("Sex");
		dataSetDefinition1.addColumn(gender, new HashMap<String,Object>());
		dataSetDefinition2.addColumn(gender, new HashMap<String,Object>());
		dataSetDefinition3.addColumn(gender, new HashMap<String,Object>());
		dataSetDefinition4.addColumn(gender, new HashMap<String,Object>());
		
		PatientDateOfBirth birthdate = new PatientDateOfBirth();
		birthdate.setName("Date of Birth");
		birthdate.setDescription("Date of Birth");
		dataSetDefinition1.addColumn(birthdate, new HashMap<String,Object>());
		dataSetDefinition2.addColumn(birthdate, new HashMap<String,Object>());
		dataSetDefinition3.addColumn(birthdate, new HashMap<String,Object>());
		dataSetDefinition4.addColumn(birthdate, new HashMap<String,Object>());
		
		
		StateOfPatient txGroup=new StateOfPatient();
		txGroup.setPatientProgram(hadultHivProgram);
		txGroup.setPatienProgramWorkflow(hadultHivProgram.getWorkflowByName("TREATMENT GROUP"));
		txGroup.setName("Group");
		txGroup.setDescription("Group");	
		txGroup.setFilter(new GroupStateFilter());
		dataSetDefinition1.addColumn(txGroup, new HashMap<String,Object>());
		dataSetDefinition2.addColumn(txGroup, new HashMap<String,Object>());
		dataSetDefinition3.addColumn(txGroup, new HashMap<String,Object>());
		dataSetDefinition4.addColumn(txGroup, new HashMap<String,Object>());
				
		StateOfPatient stOfPatient=new StateOfPatient();
		stOfPatient.setPatientProgram(hadultHivProgram);
		stOfPatient.setPatienProgramWorkflow(hadultHivProgram.getWorkflowByName("TREATMENT STATUS"));
		stOfPatient.setName("Treatment");
		stOfPatient.setDescription("Treatment");
		dataSetDefinition1.addColumn(stOfPatient, new HashMap<String,Object>());
		dataSetDefinition2.addColumn(stOfPatient, new HashMap<String,Object>());
		dataSetDefinition3.addColumn(stOfPatient, new HashMap<String,Object>());
		dataSetDefinition4.addColumn(stOfPatient, new HashMap<String,Object>());
		
		
		RecentEncounterDate lastVisitEncounterDate=new  RecentEncounterDate();
		lastVisitEncounterDate.setName("Last visit date");
		lastVisitEncounterDate.setDescription("Last visit date");
		dataSetDefinition1.addColumn(lastVisitEncounterDate, new HashMap<String,Object>());
		dataSetDefinition2.addColumn(lastVisitEncounterDate, new HashMap<String,Object>());
		dataSetDefinition3.addColumn(lastVisitEncounterDate, new HashMap<String,Object>());
		dataSetDefinition4.addColumn(lastVisitEncounterDate, new HashMap<String,Object>());
		
		RecentEncounterType lastEncounterType=new RecentEncounterType();
		lastEncounterType.setName("Last visit type");
		lastEncounterType.setDescription("Last visit type");
		dataSetDefinition1.addColumn(lastEncounterType, new HashMap<String,Object>());
		dataSetDefinition2.addColumn(lastEncounterType, new HashMap<String,Object>());
		dataSetDefinition3.addColumn(lastEncounterType, new HashMap<String,Object>());
		dataSetDefinition4.addColumn(lastEncounterType, new HashMap<String,Object>());
		
		
		ReturnVisitDate returnVisitDate=new ReturnVisitDate();
		returnVisitDate.setConcept(Context.getConceptService().getConceptByName("RETURN VISIT DATE"));
		returnVisitDate.setName("Date of missed appointment");
		returnVisitDate.setDescription("Date of missed appointment");
		dataSetDefinition1.addColumn(returnVisitDate, new HashMap<String,Object>());
		dataSetDefinition2.addColumn(returnVisitDate, new HashMap<String,Object>());
		
		
		
		/*dataSetDefinition3.addColumn(returnVisitDate);
		dataSetDefinition4.addColumn(returnVisitDate);
		*/
		
		RecentObservation cd4Count=new RecentObservation();
		cd4Count.setConcept(Context.getConceptService().getConceptByName("CD4 COUNT"));
		cd4Count.setName("Most recent CD4");
		cd4Count.setDescription("Most recent CD4");
		dataSetDefinition1.addColumn(cd4Count, new HashMap<String,Object>());
		dataSetDefinition2.addColumn(cd4Count, new HashMap<String,Object>());
		dataSetDefinition3.addColumn(cd4Count, new HashMap<String,Object>());
		dataSetDefinition4.addColumn(cd4Count, new HashMap<String,Object>());
		
				
		RecentObservationDate dateOfCD4Count=new RecentObservationDate();
		dateOfCD4Count.setConcept(Context.getConceptService().getConceptByName("CD4 COUNT"));
		dateOfCD4Count.setName("Most recent CD4 date");
		dateOfCD4Count.setDescription("Most recent CD4 date");
		dataSetDefinition1.addColumn(dateOfCD4Count, new HashMap<String,Object>());
		dataSetDefinition2.addColumn(dateOfCD4Count, new HashMap<String,Object>());
		dataSetDefinition3.addColumn(dateOfCD4Count, new HashMap<String,Object>());
		dataSetDefinition4.addColumn(dateOfCD4Count, new HashMap<String,Object>());
		
		PatientRelationship accompagnateur=new PatientRelationship();
		accompagnateur.setRelationshipTypeId(1);
		accompagnateur.setName("Accompagnateur");
		accompagnateur.setDescription("Accompagnateur");
		dataSetDefinition1.addColumn(accompagnateur, new HashMap<String,Object>());
		dataSetDefinition2.addColumn(accompagnateur, new HashMap<String,Object>());
		dataSetDefinition3.addColumn(accompagnateur, new HashMap<String,Object>());
		dataSetDefinition4.addColumn(accompagnateur, new HashMap<String,Object>());
		
				
		dataSetDefinition1.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition2.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition3.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition4.addParameter(new Parameter("location", "Location", Location.class));
		
		dataSetDefinition1.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition2.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition3.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataSetDefinition4.addParameter(new Parameter("endDate", "End Date", Date.class));

		
		Map<String, Object> mappings1 = new HashMap<String, Object>();
		mappings1.put("location", "${location}");
		mappings1.put("endDate", "${endDate}");
		

		Map<String, Object> mappings2 = new HashMap<String, Object>();
		mappings2.put("location", "${location}");
		mappings2.put("endDate", "${endDate}");
		
		Map<String, Object> mappings3 = new HashMap<String, Object>();
		mappings3.put("location", "${location}");
		mappings3.put("endDate", "${endDate}");
		
		
		Map<String, Object> mappings4 = new HashMap<String, Object>();
		mappings4.put("location", "${location}");
		mappings4.put("endDate", "${endDate}");
		
		
		reportDefinition.addDataSetDefinition("AdultARTLateVisit", dataSetDefinition1, mappings1);
		reportDefinition.addDataSetDefinition("AdultPreARTLateVisit", dataSetDefinition2, mappings2);
		reportDefinition.addDataSetDefinition("AdultHIVLateCD4Count", dataSetDefinition3, mappings3);
		reportDefinition.addDataSetDefinition("HIVLostToFollowup", dataSetDefinition4, mappings4);
		
	}
	
	
	
	private void createCohortDefinitions() {
		
		SqlCohortDefinition location = new SqlCohortDefinition();
		location
		        .setQuery("select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
		location.setName("location: Patients at location");
		location.addParameter(new Parameter("location", "location", Location.class));
		h.replaceCohortDefinition(location);
		
	}
	
	
	
	private void setUpGlobalProperties()
	{
		properties = new HashMap<String, String>();
		
		String hivProgram = Context.getAdministrationService().getGlobalProperty("reports.hivprogramname");
		properties.put("HIV_PROGRAM", hivProgram);
		
		String currentLocation = Context.getAdministrationService().getGlobalProperty("reports.currentlocation");
		properties.put("CURRENT_LOCATION", currentLocation);
		
		String workflowStatus = Context.getAdministrationService().getGlobalProperty("reports.hivworkflowstatus");
		properties.put("HIV_WORKFLOW_STATUS", workflowStatus);
		
		String onARTState = Context.getAdministrationService().getGlobalProperty("reports.hivonartstate");
		properties.put("HIV_ON_ART_STATE", onARTState);
		
		String allARTDrugsConcept = Context.getAdministrationService().getGlobalProperty("registers.allArtDrugsConceptSet");
		properties.put("ALL_ART_DRUGS_CONCEPT", allARTDrugsConcept);
		
		String weightConcept = Context.getAdministrationService().getGlobalProperty("reports.weightConcept");
		properties.put("WEIGHT_CONCEPT", weightConcept);
		
		String stageConcept = Context.getAdministrationService().getGlobalProperty("registers.stageConcept");
		properties.put("STAGE_CONCEPT", stageConcept);
		
		String cd4Concept = Context.getAdministrationService().getGlobalProperty("reports.cd4Concept");
		properties.put("CD4_CONCEPT", cd4Concept);
		
		String cd4PercentageConcept = Context.getAdministrationService().getGlobalProperty("registers.cd4PercentageConcept");
		properties.put("CD4_PERCENTAGE_CONCEPT", cd4PercentageConcept);
		
		String ctxTreatmentConcept = Context.getAdministrationService().getGlobalProperty("registers.ctxTreatmentConcept");
		properties.put("CTX_TREATMENT_CONCEPT", ctxTreatmentConcept);
		
		String tbTreatmentConcept = Context.getAdministrationService().getGlobalProperty("registers.tbTreatmentConcept");
		properties.put("TB_TREATMENT_CONCEPT", tbTreatmentConcept);
		
		String pregnancyDeliveryDateConcept = Context.getAdministrationService().getGlobalProperty("registers.pregnancyDeliveryDateConcept");
		properties.put("PREGNANCY_DELIVERY_DATE_CONCEPT", pregnancyDeliveryDateConcept);
		
		String allFirstLineARTDrugsConcept = Context.getAdministrationService().getGlobalProperty("registers.allFirstLineArtDrugsConceptSet");
		properties.put("ALL_FIRST_LINE_ART_DRUGS_CONCEPT", allFirstLineARTDrugsConcept);
		
		String allSecondLineARTDrugsConcept = Context.getAdministrationService().getGlobalProperty("registers.allSecondLineArtDrugsConceptSet");
		properties.put("ALL_SECOND_LINE_ART_DRUGS_CONCEPT", allSecondLineARTDrugsConcept);
		
		String tbTestConcept = Context.getAdministrationService().getGlobalProperty("registers.tbTestConcept");
		properties.put("TB_TEST_CONCEPT", tbTestConcept);
	}
	
}
