package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

public class SetupPMTCTFormCompletionSheet {
	
	Helper h = new Helper();
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	private List<String> onOrAfterOnOrBefore = new ArrayList<String>();
	
	private Program pmtctCombinedInfantProgram;
	
	//properties retrieved from global variables
	
	private Form pmtctDDB;
	
	private Form pmtctRDV;
	
	
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
		
		rd.setBaseCohortDefinition(Cohorts.createInProgramParameterizableByDate("InPMTCT", pmtctCombinedInfantProgram, onOrAfterOnOrBefore), ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}"));
		
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
		
		SqlEncounterQuery formsCompleted = new SqlEncounterQuery();
		formsCompleted.setQuery("select encounter_id from encounter where voided = 0 and form_id = " + pmtctDDB.getFormId() + " and encounter_datetime >= :startDate and encounter_datetime <= :endDate");
		formsCompleted.setName("PMTCT Encounter");
		formsCompleted.addParameter(new Parameter("startDate", "startDate", Date.class));
		formsCompleted.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		EncounterIndicator ddbCompleted = new EncounterIndicator();
		ddbCompleted.setName("1");
		ddbCompleted.setEncounterQuery(new Mapped<EncounterQuery>(formsCompleted,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		dsd.addColumn(ddbCompleted);
		
		SqlEncounterQuery formsCompletedRDV = new SqlEncounterQuery();
		formsCompletedRDV.setQuery("select encounter_id from encounter where voided = 0 and form_id = " + pmtctRDV.getFormId() + " and encounter_datetime >= :startDate and encounter_datetime <= :endDate");
		formsCompletedRDV.setName("PMTCT Encounter");
		formsCompletedRDV.addParameter(new Parameter("startDate", "startDate", Date.class));
		formsCompletedRDV.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		EncounterIndicator rdvCompleted = new EncounterIndicator();
		rdvCompleted.setName("2");
		rdvCompleted.setEncounterQuery(new Mapped<EncounterQuery>(formsCompletedRDV,ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		dsd.addColumn(rdvCompleted);
	}
	
	private void setUpProperties() {
		pmtctDDB = gp.getForm(GlobalPropertiesManagement.PMTCT_DDB);
		
		pmtctRDV = gp.getForm(GlobalPropertiesManagement.PMTCT_RDV);
		
		pmtctCombinedInfantProgram = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM);
		
		onOrAfterOnOrBefore.add("onOrAfter");
		onOrAfterOnOrBefore.add("onOrBefore");
		
		
		
	}
}
