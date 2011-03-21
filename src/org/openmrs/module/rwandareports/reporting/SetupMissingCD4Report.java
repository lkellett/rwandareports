package org.openmrs.module.rwandareports.reporting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InverseCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientIdentifier;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientRelationship;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentEncounterDate;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentEncounterType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RecentObservationDate;
import org.openmrs.module.rowperpatientreports.patientdata.definition.StateOfPatient;

public class SetupMissingCD4Report {
	
	Helper h = new Helper();
	
	private HashMap<String, String> properties;
	
	public SetupMissingCD4Report(Helper helper) {
		h = helper;
	}
	
	public void setup() throws Exception {
		
		delete();
		
		setUpGlobalProperties();
		
		createCohortDefinitions();
		ReportDefinition rd = createReportDefinition();
		h.createRowPerPatientXlsOverview(rd, "MissingCD4ReportTemplate.xls", "XlsMissingCD4ReportTemplate", null);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsMissingCD4ReportTemplate".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeDefinition(ReportDefinition.class, "Missing CD4 Report");
		
		h.purgeDefinition(PatientDataSetDefinition.class, "Not completed Data Set");
		h.purgeDefinition(PatientDataSetDefinition.class, "No Result Data Set");
		
		h.purgeDefinition(CohortDefinition.class, "location: Patients at location");
	}
	
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Missing CD4 Report");
		reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
		reportDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
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
		
		PatientDataSetDefinition notCompletedDataSet = new PatientDataSetDefinition();
		notCompletedDataSet.addParameter(new Parameter("location", "Location", Location.class));
		notCompletedDataSet.addParameter(new Parameter("endDate", "End Date", Date.class));
		notCompletedDataSet.addParameter(new Parameter("startDate", "Start Date", Date.class));
		notCompletedDataSet.setName("Not completed Data Set");
		
		PatientDataSetDefinition noResultDataSet = new PatientDataSetDefinition();
		noResultDataSet.setName("No Result Data Set");
		
		SqlCohortDefinition patientDied=new SqlCohortDefinition("SELECT DISTINCT person_id FROM obs o WHERE o.concept_id='" + properties.get("PATIENT_DIED_CONCEPT") + "'");
		InverseCohortDefinition patientAlive=new InverseCohortDefinition(patientDied);
		noResultDataSet.addFilter(patientAlive, new HashMap<String,Object>());
		
		SqlCohortDefinition notCompleted = new SqlCohortDefinition();
		notCompleted
		        .setQuery("select e.patient_id from encounter e, orders o where e.encounter_id = o.encounter_id and " +
		        		  "o.concept_id = " + 
		        		  properties.get("CD4_LAB_CONCEPT") + 
		        		  " and o.order_type_id = " + 
		        		  properties.get("LAB_ORDER_TYPE") + 
		        		  " and o.voided = 0 and e.voided = 0 and " +
		        		  "e.encounter_id not in (select encounter_id from obs where voided = 0 and encounter_id is not null and concept_id = " + 
		        		  properties.get("CD4_CONCEPT") +
		        		  ") and o.date_created > :startDate and o.date_created < :endDate");	
		
		notCompleted.addParameter(new Parameter("endDate", "endDate", Date.class));
		notCompleted.addParameter(new Parameter("startDate", "startDate", Date.class));
		notCompletedDataSet.addFilter(notCompleted, ParameterizableUtil.createParameterMappings("endDate=${endDate-1w},startDate=${startDate}"));
		
		SqlCohortDefinition noResult = new SqlCohortDefinition();
		noResult
		.setQuery("select person_id from obs where comments in ('Re-order', 'Closed', 'Failed') and obs_datetime < :endDate " +
			"and concept_id =" +
			properties.get("CD4_CONCEPT") + 
			" and obs_id in (select o.obs_id from (select person_id as pi, max(obs_datetime) od from obs where concept_id=" +
			 properties.get("CD4_CONCEPT") + 
			" and voided = 0 group by person_id)RecentObs inner join obs o on o.obs_datetime = RecentObs.od and o.person_id = RecentObs.pi)");
		noResult.addParameter(new Parameter("endDate", "endDate", Date.class));
		noResultDataSet.addFilter(noResult, ParameterizableUtil.createParameterMappings("endDate=${endDate-1w}"));
		
		//==================================================================
		//                 Columns of report settings
		//==================================================================
		
		PatientIdentifierType imbType = Context.getPatientService().getPatientIdentifierTypeByName("IMB ID");
		PatientIdentifier imbId = new PatientIdentifier(imbType);
		imbId.setName("IMB ID");
		imbId.setDescription("IMB ID");
		notCompletedDataSet.addColumn(imbId, new HashMap<String,Object>());
		noResultDataSet.addColumn(imbId, new HashMap<String,Object>());
		
		PatientProperty givenName = new PatientProperty("givenName");
		givenName.setName("First Name");
		givenName.setDescription("First Name");
		notCompletedDataSet.addColumn(givenName, new HashMap<String,Object>());
		noResultDataSet.addColumn(givenName, new HashMap<String,Object>());
		
		PatientProperty familyName = new PatientProperty("familyName");
		familyName.setName("Last Name");
		familyName.setDescription("Last Name");
		notCompletedDataSet.addColumn(familyName, new HashMap<String,Object>());
		noResultDataSet.addColumn(familyName, new HashMap<String,Object>());
		
		PatientProperty gender = new PatientProperty("gender");
		gender.setName("Sex");
		gender.setDescription("Sex");
		notCompletedDataSet.addColumn(gender, new HashMap<String,Object>());
		noResultDataSet.addColumn(gender, new HashMap<String,Object>());
		
		PatientProperty age = new PatientProperty("age");
		notCompletedDataSet.addColumn(gender, new HashMap<String,Object>());
		noResultDataSet.addColumn(gender, new HashMap<String,Object>());
		notCompletedDataSet.addColumn(age, new HashMap<String,Object>());
		noResultDataSet.addColumn(age, new HashMap<String,Object>());
		
		Program hadultHivProgram = Context.getProgramWorkflowService().getProgramByName(properties.get("HIV_PROGRAM"));
		
		StateOfPatient txGroup=new StateOfPatient();
		txGroup.setPatientProgram(hadultHivProgram);
		txGroup.setPatienProgramWorkflow(hadultHivProgram.getWorkflowByName(properties.get("HIV_TREATMENT_GROUP_STATUS")));
		txGroup.setName("Group");
		txGroup.setDescription("Group");		
		notCompletedDataSet.addColumn(txGroup, new HashMap<String,Object>());
		noResultDataSet.addColumn(txGroup, new HashMap<String,Object>());
		
		StateOfPatient stOfPatient=new StateOfPatient();
		stOfPatient.setPatientProgram(hadultHivProgram);
		stOfPatient.setPatienProgramWorkflow(hadultHivProgram.getWorkflowByName(properties.get("HIV_WORKFLOW_STATUS")));
		stOfPatient.setName("Treatment");
		stOfPatient.setDescription("Treatment");
		notCompletedDataSet.addColumn(stOfPatient, new HashMap<String,Object>());
		noResultDataSet.addColumn(stOfPatient, new HashMap<String,Object>());
		
		Program pediAdultHivProgram = Context.getProgramWorkflowService().getProgramByName(properties.get("PEDI_HIV_PROGRAM"));
		
		StateOfPatient pediTxGroup = new StateOfPatient();
		pediTxGroup.setPatientProgram(hadultHivProgram);
		pediTxGroup.setPatienProgramWorkflow(pediAdultHivProgram.getWorkflowByName(properties.get("HIV_TREATMENT_GROUP_STATUS")));
		pediTxGroup.setName("PediGroup");
		pediTxGroup.setDescription("PediGroup");		
		notCompletedDataSet.addColumn(pediTxGroup, new HashMap<String,Object>());
		noResultDataSet.addColumn(pediTxGroup, new HashMap<String,Object>());
		
		StateOfPatient pediStOfPatient=new StateOfPatient();
		pediStOfPatient.setPatientProgram(hadultHivProgram);
		pediStOfPatient.setPatienProgramWorkflow(hadultHivProgram.getWorkflowByName(properties.get("HIV_WORKFLOW_STATUS")));
		pediStOfPatient.setName("PediTreatment");
		pediStOfPatient.setDescription("PediTreatment");
		notCompletedDataSet.addColumn(pediStOfPatient, new HashMap<String,Object>());
		noResultDataSet.addColumn(pediStOfPatient, new HashMap<String,Object>());
		
		RecentEncounterType lastEncounterType=new RecentEncounterType();
		lastEncounterType.setName("Last visit type");
		lastEncounterType.setDescription("Last visit type");
		notCompletedDataSet.addColumn(lastEncounterType, new HashMap<String,Object>());
		noResultDataSet.addColumn(lastEncounterType, new HashMap<String,Object>());
		
		RecentEncounterDate lastVisitEncounterDate=new  RecentEncounterDate();
		lastVisitEncounterDate.setName("Last visit date");
		lastVisitEncounterDate.setDescription("Last visit date");
		notCompletedDataSet.addColumn(lastVisitEncounterDate, new HashMap<String,Object>());
		noResultDataSet.addColumn(lastVisitEncounterDate, new HashMap<String,Object>());
		
		RecentObservation cd4Count=new RecentObservation();
		cd4Count.setConcept(Context.getConceptService().getConceptByName("CD4 COUNT"));
		cd4Count.setName("Most recent CD4");
		cd4Count.setDescription("Most recent CD4");
		notCompletedDataSet.addColumn(cd4Count, new HashMap<String,Object>());
		noResultDataSet.addColumn(cd4Count, new HashMap<String,Object>());
		
		RecentObservationDate dateOfCD4Count=new RecentObservationDate();
		dateOfCD4Count.setConcept(Context.getConceptService().getConceptByName("CD4 COUNT"));
		dateOfCD4Count.setName("Most recent CD4 date");
		dateOfCD4Count.setDescription("Most recent CD4 date");
		notCompletedDataSet.addColumn(dateOfCD4Count, new HashMap<String,Object>());
		noResultDataSet.addColumn(dateOfCD4Count, new HashMap<String,Object>());
		
		PatientRelationship accompagnateur=new PatientRelationship();
		accompagnateur.setRelationshipTypeId(1);
		accompagnateur.setName("Accompagnateur");
		accompagnateur.setDescription("Accompagnateur");
		notCompletedDataSet.addColumn(accompagnateur, new HashMap<String,Object>());
		noResultDataSet.addColumn(accompagnateur, new HashMap<String,Object>());
		
		PatientAddress district = new PatientAddress();
		district.setName("district");
		district.setIncludeCountry(false);
		district.setIncludeProvince(false);
		district.setIncludeCell(false);
		district.setIncludeSector(false);
		district.setIncludeUmudugudu(false);
		notCompletedDataSet.addColumn(district, new HashMap<String,Object>());
		noResultDataSet.addColumn(district, new HashMap<String,Object>());
		
		PatientAddress sector = new PatientAddress();
		sector.setName("sector");
		sector.setIncludeCountry(false);
		sector.setIncludeProvince(false);
		sector.setIncludeCell(false);
		sector.setIncludeDistrict(false);
		sector.setIncludeUmudugudu(false);
		notCompletedDataSet.addColumn(sector, new HashMap<String,Object>());
		noResultDataSet.addColumn(sector, new HashMap<String,Object>());
		
		PatientAddress cell = new PatientAddress();
		cell.setName("cell");
		cell.setIncludeCountry(false);
		cell.setIncludeProvince(false);
		cell.setIncludeSector(false);
		cell.setIncludeDistrict(false);
		cell.setIncludeUmudugudu(false);
		notCompletedDataSet.addColumn(cell, new HashMap<String,Object>());
		noResultDataSet.addColumn(cell, new HashMap<String,Object>());
		
		PatientAddress umudugudu = new PatientAddress();
		umudugudu.setName("umudugudu");
		umudugudu.setIncludeCountry(false);
		umudugudu.setIncludeProvince(false);
		umudugudu.setIncludeSector(false);
		umudugudu.setIncludeDistrict(false);
		umudugudu.setIncludeCell(false);
		notCompletedDataSet.addColumn(umudugudu, new HashMap<String,Object>());
		noResultDataSet.addColumn(umudugudu, new HashMap<String,Object>());
		
		notCompletedDataSet.addParameter(new Parameter("location", "Location", Location.class));
		noResultDataSet.addParameter(new Parameter("location", "Location", Location.class));
		
		notCompletedDataSet.addParameter(new Parameter("endDate", "End Date", Date.class));
		noResultDataSet.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		notCompletedDataSet.addParameter(new Parameter("startDate", "Start Date", Date.class));
		noResultDataSet.addParameter(new Parameter("startDate", "Start Date", Date.class));

		Map<String, Object> mappings1 = new HashMap<String, Object>();
		mappings1.put("location", "${location}");
		mappings1.put("endDate", "${endDate}");
		mappings1.put("startDate", "${startDate}");

		reportDefinition.addDataSetDefinition("NotCompleted", notCompletedDataSet, mappings1);
		reportDefinition.addDataSetDefinition("NoResult", noResultDataSet, mappings1);
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
		
		String pediHivProgram = Context.getAdministrationService().getGlobalProperty("reports.pedihivprogramname");
		properties.put("PEDI_HIV_PROGRAM", pediHivProgram);
		
		String workflowStatus = Context.getAdministrationService().getGlobalProperty("reports.hivworkflowstatus");
		properties.put("HIV_WORKFLOW_STATUS", workflowStatus);
		
		String groupStatus = Context.getAdministrationService().getGlobalProperty("reports.hivtreatmentstatus");
		properties.put("HIV_TREATMENT_GROUP_STATUS", groupStatus);
		
		String patientDiedConcept = Context.getAdministrationService().getGlobalProperty("reports.patientDiedConcept");
		properties.put("PATIENT_DIED_CONCEPT", patientDiedConcept);
		
		String cd4Concept = Context.getAdministrationService().getGlobalProperty("reports.cd4Concept");
		properties.put("CD4_CONCEPT", cd4Concept);
		
		String cd4LabConcept = Context.getAdministrationService().getGlobalProperty("reports.cd4LabConcept");
		properties.put("CD4_LAB_CONCEPT", cd4LabConcept);
		
		String labOrderType = Context.getAdministrationService().getGlobalProperty("reports.labOrderType");
		properties.put("LAB_ORDER_TYPE", labOrderType);
		
	}
	
}
