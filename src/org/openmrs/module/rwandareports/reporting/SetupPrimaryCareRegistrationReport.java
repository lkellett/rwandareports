package org.openmrs.module.rwandareports.reporting;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.dataset.PrimaryCareDataSetDefinition;

public class SetupPrimaryCareRegistrationReport {
	
	protected final static Log log = LogFactory.getLog(SetupPrimaryCareRegistrationReport.class);
	
	Helper h = new Helper();
	
	public void setup() throws Exception {
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = h.createRowPerPatientXlsOverviewReportDesign(rd, "PrimaryCareRegistrationData.xls", "XlsPrimaryCareRegistrationData",
		    null);
		
		createDataSetDefinition(rd);
		
		h.saveReportDefinition(rd);
		
		Properties props = new Properties();
		props.put(
		    "repeatingSections",
		    "sheet:1,row:2,dataset:dataSet");
		
		design.setProperties(props);
		h.saveReportDesign(design);
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsPrimaryCareRegistrationData".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		h.purgeReportDefinition("Research-Primary Care Registration Data");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Research-Primary Care Registration Data");
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition rd) {
		
		PrimaryCareDataSetDefinition dataSet = new PrimaryCareDataSetDefinition();
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		
		rd.addDataSetDefinition("dataSet", dataSet, mappings);
	}
}
