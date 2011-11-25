package org.openmrs.module.rwandareports.reporting;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.util.IOUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.definition.service.SerializedDefinitionService;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.ExcelTemplateRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reportingobjectgroup.report.renderer.ExcelCalendarTemplateRenderer;
import org.openmrs.util.OpenmrsClassLoader;

public class Helper {
	
	public void purgeReportDefinition(String name) {
		ReportDefinitionService rds = Context.getService(ReportDefinitionService.class);
		try {
			ReportDefinition findDefinition = findReportDefinition(name);
			if (findDefinition != null) {
				rds.purgeDefinition(findDefinition);
			}
		}
		catch (RuntimeException e) {
			// intentional empty as the author is too long out of business...
		}
	}
	
	public ReportDefinition findReportDefinition(String name) {
		ReportDefinitionService s = (ReportDefinitionService) Context.getService(ReportDefinitionService.class);
		List<ReportDefinition> defs = s.getDefinitions(name, true);
		for (ReportDefinition def : defs) {
			return def;
		}
		throw new RuntimeException("Couldn't find Definition " + name);
	}
	
	public void saveReportDefinition(ReportDefinition rd) {
		ReportDefinitionService rds = (ReportDefinitionService) Context.getService(ReportDefinitionService.class);
		
		//try to find existing report definitions to replace
		List<ReportDefinition> definitions = rds.getDefinitions(rd.getName(), true);
		if (definitions.size() > 0) {
			ReportDefinition existingDef = definitions.get(0);
			rd.setId(existingDef.getId());
			rd.setUuid(existingDef.getUuid());
		}
		try {
			rds.saveDefinition(rd);
		}
		catch (Exception e) {
			SerializedDefinitionService s = (SerializedDefinitionService) Context
			        .getService(SerializedDefinitionService.class);
			s.saveDefinition(rd);
		}
	}
	
	public void createXlsOverview(ReportDefinition rd, String resourceName, String name,
	                              Map<? extends Object, ? extends Object> properties) throws IOException {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rdd : rs.getAllReportDesigns(false)) {
			if (name.equals(rdd.getName())) {
				rs.purgeReportDesign(rdd);
			}
		}
		
		ReportDesignResource resource = new ReportDesignResource();
		resource.setName(resourceName);
		resource.setExtension("xls");
		InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream(resourceName);
		resource.setContents(IOUtils.toByteArray(is));
		final ReportDesign design = new ReportDesign();
		design.setName(name);
		design.setReportDefinition(rd);
		design.setRendererType(ExcelTemplateRenderer.class);
		design.addResource(resource);
		if (properties != null) {
			design.getProperties().putAll(properties);
		}
		resource.setReportDesign(design);
		
		rs.saveReportDesign(design);
	}
	
	public void createXlsCalendarOverview(ReportDefinition rd, String resourceName, String name,
	                                      Map<? extends Object, ? extends Object> properties) throws IOException {
		
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rdd : rs.getAllReportDesigns(false)) {
			if (name.equals(rdd.getName())) {
				rs.purgeReportDesign(rdd);
			}
		}
		
		ReportDesignResource resource = new ReportDesignResource();
		resource.setName(resourceName);
		resource.setExtension("xls");
		InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream(resourceName);
		resource.setContents(IOUtils.toByteArray(is));
		final ReportDesign design = new ReportDesign();
		design.setName(name);
		design.setReportDefinition(rd);
		design.setRendererType(ExcelCalendarTemplateRenderer.class);
		design.addResource(resource);
		if (properties != null) {
			design.getProperties().putAll(properties);
		}
		resource.setReportDesign(design);
	
		rs.saveReportDesign(design);
	}
	
	public ReportDesign createRowPerPatientXlsOverviewReportDesign(ReportDefinition rd, String resourceName, String name,
	                                                               Map<? extends Object, ? extends Object> properties)
	    throws IOException {
		
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rdd : rs.getAllReportDesigns(false)) {
			if (name.equals(rdd.getName())) {
				rs.purgeReportDesign(rdd);
			}
		}
		
		ReportDesignResource resource = new ReportDesignResource();
		resource.setName(resourceName);
		resource.setExtension("xls");
		InputStream is = OpenmrsClassLoader.getInstance().getResourceAsStream(resourceName);
		resource.setContents(IOUtils.toByteArray(is));
		final ReportDesign design = new ReportDesign();
		design.setName(name);
		design.setReportDefinition(rd);
		design.setRendererType(ExcelTemplateRenderer.class);
		design.addResource(resource);
		if (properties != null) {
			design.getProperties().putAll(properties);
		}
		resource.setReportDesign(design);
		
		return design;
	}
	
	public void saveReportDesign(ReportDesign design) {
		ReportService rs = Context.getService(ReportService.class);
		rs.saveReportDesign(design);
	}
	
}
