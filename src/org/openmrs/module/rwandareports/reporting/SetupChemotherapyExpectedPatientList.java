package org.openmrs.module.rwandareports.reporting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.common.SortCriteria.SortDirection;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.comparator.ChemotherapyDataSetRowComparator;
import org.openmrs.module.rwandareports.definition.UpcomingChemotherapyCohortDefinition;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupChemotherapyExpectedPatientList {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties retrieved from global variables
	private Program oncologyProgram;

	private ProgramWorkflow diagnosis;
	
	private Concept chemotherapy;
	
	private Concept telephone;
	
	
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "ChemotherapyExpectedPatientList.xls",
		    "ChemotherapyPatientList.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:7,dataset:dataSet");
		design.setProperties(props);
		
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("ChemotherapyPatientList.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Chemotherapy Expected Patient List");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Chemotherapy Expected Patient List");
				
		UpcomingChemotherapyCohortDefinition baseCohort = new UpcomingChemotherapyCohortDefinition();
		baseCohort.setChemotherapyIndication(chemotherapy);
		baseCohort.addParameter(new Parameter("asOfDate", "asOfDate", Date.class));
		baseCohort.addParameter(new Parameter("untilDate", "untilDate", Date.class));
		
		reportDefinition.setBaseCohortDefinition(baseCohort,ParameterizableUtil.createParameterMappings("asOfDate=${endDate},untilDate=${endDate+6d}"));
		reportDefinition.addParameter(new Parameter("endDate", "Week of (select Monday)", Date.class));
		createDataSetDefinition(reportDefinition);
		
		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName("Chemotherapy Patient List");
		
		SortCriteria sortCriteria = new SortCriteria();
		sortCriteria.addSortElement("regimenDate", SortDirection.ASC);
		dataSetDefinition.setSortCriteria(sortCriteria);
		dataSetDefinition.addParameter(new Parameter("endDate", "Monday", Date.class));
		
		
		//Add Columns
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("id"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());		
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getGender("sex"), new HashMap<String, Object>());		
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDrugRegimenInformationParameterized("regimen", false), ParameterizableUtil.createParameterMappings("asOfDate=${endDate},untilDate=${endDate+6d}"));
		dataSetDefinition.addColumn(RowPerPatientColumns.getRegimenDateInformationParameterized("regimenDate", "dd/MM/yyyy"), ParameterizableUtil.createParameterMappings("asOfDate=${endDate},untilDate=${endDate+6d}"));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getStateOfPatient("diagnosis", oncologyProgram, diagnosis, null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecent("telephone", telephone, null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("address", true, true, true, true),
		    new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("accompagnateur"), new HashMap<String, Object>());
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	private void setupProperties() {
		
		oncologyProgram = gp.getProgram(GlobalPropertiesManagement.ONCOLOGY_PROGRAM);
		
		diagnosis = gp.getProgramWorkflow(GlobalPropertiesManagement.DIAGNOSIS_WORKFLOW, GlobalPropertiesManagement.ONCOLOGY_PROGRAM);
		
		chemotherapy = gp.getConcept(GlobalPropertiesManagement.CHEMOTHERAPY);
		
		telephone = gp.getConcept(GlobalPropertiesManagement.TELEPHONE_NUMBER_CONCEPT);
	}
}
