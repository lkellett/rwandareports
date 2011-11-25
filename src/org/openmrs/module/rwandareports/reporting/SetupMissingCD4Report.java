package org.openmrs.module.rwandareports.reporting;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.OrderType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.InverseCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.filter.LastEncounterFilter;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

public class SetupMissingCD4Report {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties
	private Concept patientDied;
	
	private Concept labConceptCD4;
	
	private Concept cd4;
	
	private OrderType labOrder;
	
	private Program hivProgram;
	
	private Program pediHivProgram;
	
	private ProgramWorkflow treatmentGroup;
	
	private ProgramWorkflow treatmentStatus;
	
	private List<EncounterType> clinicalEncoutersExcLab;
	
	public void setup() throws Exception {
		
		setUpProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "MissingCD4ReportTemplate.xls",
		    "XlsMissingCD4ReportTemplate", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,dataset:NotCompleted|sheet:1,row:9,dataset:NotCompletedPatientDataSet|sheet:2,dataset:NoResult|sheet:2,row:9,dataset:NoResultPatientDataSet");
		
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsMissingCD4ReportTemplate".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Missing CD4 Report");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Missing CD4 Report");
		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");
		reportDefinition.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		reportDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		createDataSetDefinition(reportDefinition);
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		//====================================================================
		//           Patients Dataset definition
		//====================================================================
		
		PatientDataSetDefinition notCompletedDataSet = new PatientDataSetDefinition();
		notCompletedDataSet.addParameter(new Parameter("location", "Location", Location.class));
		notCompletedDataSet.addParameter(new Parameter("endDate", "End Date", Date.class));
		notCompletedDataSet.addParameter(new Parameter("startDate", "Start Date", Date.class));
		notCompletedDataSet.setName("NotCompleted");
		
		PatientDataSetDefinition noResultDataSet = new PatientDataSetDefinition();
		noResultDataSet.setName("NoResult");
		
		SqlCohortDefinition patientDead = new SqlCohortDefinition(
		        "SELECT DISTINCT person_id FROM obs o WHERE o.concept_id='" + patientDied.getId() + "'");
		InverseCohortDefinition patientAlive = new InverseCohortDefinition(patientDead);
		noResultDataSet.addFilter(patientAlive, new HashMap<String, Object>());
		
		SqlCohortDefinition notCompleted = new SqlCohortDefinition();
		notCompleted
		        .setQuery("select e.patient_id from encounter e, orders o where e.encounter_id = o.encounter_id and "
		                + "o.concept_id = "
		                + labConceptCD4.getId()
		                + " and o.order_type_id = "
		                + labOrder.getId()
		                + " and o.voided = 0 and e.voided = 0 and "
		                + "e.encounter_id not in (select encounter_id from obs where voided = 0 and encounter_id is not null and concept_id = "
		                + cd4.getId() + ") and o.date_created > :startDate and o.date_created < :endDate");
		
		notCompleted.addParameter(new Parameter("endDate", "endDate", Date.class));
		notCompleted.addParameter(new Parameter("startDate", "startDate", Date.class));
		notCompletedDataSet.addFilter(notCompleted,
		    ParameterizableUtil.createParameterMappings("endDate=${endDate-1w},startDate=${startDate}"));
		
		SqlCohortDefinition noResult = new SqlCohortDefinition();
		noResult.setQuery("select person_id from obs where comments in ('Re-order', 'Closed', 'Failed') and obs_datetime < :endDate "
		        + "and concept_id ="
		        + cd4.getId()
		        + " and obs_id in (select o.obs_id from (select person_id as pi, max(obs_datetime) od from obs where concept_id="
		        + cd4.getId()
		        + " and voided = 0 group by person_id)RecentObs inner join obs o on o.obs_datetime = RecentObs.od and o.person_id = RecentObs.pi)");
		noResult.addParameter(new Parameter("endDate", "endDate", Date.class));
		noResultDataSet.addFilter(noResult, ParameterizableUtil.createParameterMappings("endDate=${endDate-1w}"));
		
		//==================================================================
		//                 Columns of report settings
		//==================================================================
		
		notCompletedDataSet.addColumn(RowPerPatientColumns.getIMBId("IMB ID"), new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getIMBId("IMB ID"), new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(RowPerPatientColumns.getFirstNameColumn("First Name"), new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getFirstNameColumn("First Name"), new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(RowPerPatientColumns.getFamilyNameColumn("Last Name"), new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getFamilyNameColumn("Last Name"), new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(RowPerPatientColumns.getGender("Sex"), new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getGender("Sex"), new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(RowPerPatientColumns.getStateOfPatient("Group", hivProgram, treatmentGroup, null),
		    new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getStateOfPatient("Group", hivProgram, treatmentGroup, null),
		    new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(
		    RowPerPatientColumns.getStateOfPatient("Treatment", hivProgram, treatmentStatus, null),
		    new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getStateOfPatient("Treatment", hivProgram, treatmentStatus, null),
		    new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(
		    RowPerPatientColumns.getStateOfPatient("PediGroup", pediHivProgram, treatmentGroup, null),
		    new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getStateOfPatient("PediGroup", pediHivProgram, treatmentGroup, null),
		    new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(
		    RowPerPatientColumns.getStateOfPatient("Treatment", pediHivProgram, treatmentStatus, null),
		    new HashMap<String, Object>());
		noResultDataSet.addColumn(
		    RowPerPatientColumns.getStateOfPatient("Treatment", pediHivProgram, treatmentStatus, null),
		    new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(RowPerPatientColumns.getRecentEncounterType("Last visit type",
		    clinicalEncoutersExcLab, new LastEncounterFilter()), new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getRecentEncounterType("Last visit type", clinicalEncoutersExcLab,
		    new LastEncounterFilter()), new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(RowPerPatientColumns.getMostRecentCD4("Most recent CD4", null),
		    new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getMostRecentCD4("Most recent CD4", null),
		    new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(RowPerPatientColumns.getAccompRelationship("Accompagnateur"),
		    new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getAccompRelationship("Accompagnateur"),
		    new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(RowPerPatientColumns.getPatientAddress("district", true, false, false, false),
		    new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getPatientAddress("district", true, false, false, false),
		    new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(RowPerPatientColumns.getPatientAddress("sector", false, true, false, false),
		    new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getPatientAddress("sector", false, true, false, false),
		    new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(RowPerPatientColumns.getPatientAddress("cell", false, false, true, false),
		    new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getPatientAddress("cell", false, false, true, false),
		    new HashMap<String, Object>());
		
		notCompletedDataSet.addColumn(RowPerPatientColumns.getPatientAddress("umudugudu", false, false, false, true),
		    new HashMap<String, Object>());
		noResultDataSet.addColumn(RowPerPatientColumns.getPatientAddress("umudugudu", false, false, false, true),
		    new HashMap<String, Object>());
		
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
		
		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(notCompletedDataSet);
		ldsd.setName("NotCompletedInd");
		ldsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		LocationHierachyIndicatorDataSetDefinition ldsd2 = new LocationHierachyIndicatorDataSetDefinition(
		        notCompletedDataSet);
		ldsd2.setName("NoResultInd");
		ldsd2.addParameter(new Parameter("startDate", "Start Date", Date.class));
		ldsd2.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd2.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		reportDefinition.addDataSetDefinition("NotCompleted", ldsd, mappings1);
		reportDefinition.addDataSetDefinition("NoResult", noResultDataSet, mappings1);
	}
	
	private void setUpProperties() {
		patientDied = gp.getConcept(GlobalPropertiesManagement.PATIENT_DIED);
		labConceptCD4 = gp.getConcept(GlobalPropertiesManagement.CD4_PANEL_LAB_CONCEPT);
		cd4 = gp.getConcept(GlobalPropertiesManagement.CD4_TEST);
		
		labOrder = gp.getOrderType(GlobalPropertiesManagement.LAB_ORDER_TYPE);
		
		treatmentGroup = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_GROUP_WORKFLOW,
		    GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		treatmentStatus = gp.getProgramWorkflow(GlobalPropertiesManagement.TREATMENT_STATUS_WORKFLOW,
		    GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		hivProgram = gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
		
		pediHivProgram = gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
		
		clinicalEncoutersExcLab = gp.getEncounterTypeList(GlobalPropertiesManagement.CLINICAL_ENCOUNTER_TYPES_EXC_LAB_TEST);
	}
	
}
