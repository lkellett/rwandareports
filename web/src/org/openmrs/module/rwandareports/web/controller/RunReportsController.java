package org.openmrs.module.rwandareports.web.controller;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.Report;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.ReportRequest.Priority;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.reporting.Helper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RunReportsController {
    
	private Helper h = new Helper();
	
    @RequestMapping("/module/rwandaReports/printReport.form") 
    public void viewIndex(ModelMap model, 
                            @RequestParam(value="report", required=true) String report,
                            @RequestParam(value="parameters", required=true) String parameters,
                            HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException {
    	
    	ReportService rs = Context.getService(ReportService.class);
		
		ReportRequest rr = new ReportRequest();
		
		ReportDefinition reportDef = h.findReportDefinition(report);
		
		String[] paramNames = parameters.split(",");
		
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		for (String param: paramNames) {
			params.put(param, request.getParameter(param));			
		}
		
		rr.setReportDefinition(new Mapped<ReportDefinition>(reportDef, params));
		
		List<RenderingMode> modes = rs.getRenderingModes(reportDef);
		RenderingMode rm = modes.get(0);
		
	    rr.setRenderingMode(rm);
	    rr.setPriority(Priority.HIGHEST);
		
		Report rep = rs.runReport(rr);
		
		String filename = rm.getRenderer().getFilename(rep.getRequest().getReportDefinition().getParameterizable(), rm.getArgument()).replace(" ", "_");
		response.setContentType(rm.getRenderer().getRenderedContentType(rep.getRequest().getReportDefinition().getParameterizable(), rm.getArgument()));
		byte[] data = rep.getRenderedOutput();
		
		if (data != null) {
			response.setHeader("Content-Disposition", "attachment; filename=" + filename);
			response.setHeader("Pragma", "no-cache");
			IOUtils.write(data, response.getOutputStream());
		}
		else {
			response.getWriter().write("There was an error retrieving the report");
		}
    }
}
