package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.comparator.PMTCTDataSetRowComparator;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupNCDConsultationSheet {
	protected final static Log log = LogFactory.getLog(SetupNCDConsultationSheet.class);

	Helper h = new Helper();
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties retrieved from global variables
	private List<Program> diseases;
		
	public void setup() throws Exception {
		setupPrograms();
		ReportDefinition rd = createReportDefinition();	
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "NCDConsultationSheet.xls","NCDConsultationSheet.xls_", null);	
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:9,dataset:dataset0|sheet:2,row:9,dataset:dataset1|sheet:3,row:9,dataset:dataset2|sheet:4,row:9,dataset:dataset3");
		design.setProperties(props);
		h.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("NCDConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("NCD Consult Sheet");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("NCD Consult Sheet");	
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));	
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),
			    ParameterizableUtil.createParameterMappings("location=${location}"));
		
		for (Program program : diseases) {
			createDataSetDefinition(reportDefinition,program,diseases.indexOf(program));			
		}

		h.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition,Program program,int datasetIndex) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName(program.getName() + " Data Set");
		dataSetDefinition.setComparator(new PMTCTDataSetRowComparator());
		dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition.addParameter(new Parameter("endDate", "enDate", Date.class));
		
		//Add Filters	
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate(program.getName()+"Cohort", program), ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
        dataSetDefinition.addFilter(Cohorts.createDateObsCohortDefinition(gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE), RangeComparator.GREATER_EQUAL,RangeComparator.LESS_EQUAL, TimeModifier.ANY), ParameterizableUtil.createParameterMappings("value1=${endDate},value2=${endDate+7d}"));
         
     	DateFormatFilter dateFilter = new DateFormatFilter();
		dateFilter.setFinalDateFormat("dd-MMM-yyyy");
		
		//Add Columns
         
     	dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
     	
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentWeight("RecentWeight", "@ddMMMyy"),new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("AccompName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", null, dateFilter),new HashMap<String, Object>());
						
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("dataset"+datasetIndex, dataSetDefinition, mappings);
		
	}
	
	private List<Program> setupPrograms() {
		 diseases = new ArrayList<Program>();
		 diseases.add(gp.getProgram(GlobalPropertiesManagement.CRD_PROGRAM));
		 diseases.add(gp.getProgram(GlobalPropertiesManagement.HEART_FAILURE_PROGRAM_NAME));
		 diseases.add(gp.getProgram(GlobalPropertiesManagement.HYPERTENSION_PROGRAM));
		 diseases.add(gp.getProgram(GlobalPropertiesManagement.EPILEPSY_PROGRAM));
		return diseases;
	}
}
