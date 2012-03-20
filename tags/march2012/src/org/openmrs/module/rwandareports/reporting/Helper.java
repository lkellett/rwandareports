package org.openmrs.module.rwandareports.reporting;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.util.IOUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition.CohortIndicatorAndDimensionColumn;
import org.openmrs.module.reporting.definition.service.SerializedDefinitionService;
import org.openmrs.module.reporting.evaluation.Definition;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.ExcelTemplateRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reportingobjectgroup.dataset.RollingDailyIndicatorDataSetDefinition;
import org.openmrs.module.reportingobjectgroup.dataset.RollingDailyIndicatorDataSetDefinition.RwandaReportsIndicatorAndDimensionColumn;
import org.openmrs.module.reportingobjectgroup.report.definition.RollingDailyPeriodIndicatorReportDefinition;
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
	
	public void purgeReportDefinition(ReportDefinition rd, Boolean saveIndependently) {
		ReportDefinitionService rds = (ReportDefinitionService) Context.getService(ReportDefinitionService.class);
		
		if (saveIndependently && rd !=null && rd instanceof PeriodIndicatorReportDefinition) {
			PeriodIndicatorReportDefinition pird = (PeriodIndicatorReportDefinition)rd;
			CohortIndicatorDataSetDefinition dataSetDefinition = pird.getIndicatorDataSetDefinition();
			
			for (Iterator<CohortIndicatorAndDimensionColumn> iterator = dataSetDefinition.getColumns().iterator(); iterator.hasNext();) {
				CohortIndicatorAndDimensionColumn column = iterator.next();
				purgeDefinition(column.getIndicator().getParameterizable().getClass(), column.getIndicator().getParameterizable().getName());
	        }
		}
		
		if (saveIndependently && rd !=null && rd instanceof RollingDailyPeriodIndicatorReportDefinition) {
			RollingDailyPeriodIndicatorReportDefinition pird = (RollingDailyPeriodIndicatorReportDefinition)rd;
			RollingDailyIndicatorDataSetDefinition dataSetDefinition = pird.getIndicatorDataSetDefinition();
			
			for (Iterator<RwandaReportsIndicatorAndDimensionColumn> iterator = dataSetDefinition.getColumns().iterator(); iterator.hasNext();) {
				RwandaReportsIndicatorAndDimensionColumn column = iterator.next();
				purgeDefinition(column.getIndicator().getParameterizable().getClass(), column.getIndicator().getParameterizable().getName());
	        }
		}

		purgeReportDefinition(rd.getName());
    }
	
	public Definition findDefinition(Class clazz, String name) {
		SerializedDefinitionService s = (SerializedDefinitionService) Context.getService(SerializedDefinitionService.class);
		List<Definition> defs = s.getDefinitions(clazz, name, true);
		for (Definition def : defs) {
			return def;
		}
		throw new RuntimeException("Couldn't find Definition " + name);
	}
	public void purgeDefinition(Class clazz, String name) {
		SerializedDefinitionService s = (SerializedDefinitionService) Context.getService(SerializedDefinitionService.class);
		try {
			if (findDefinition(clazz, name) != null) {
				s.purgeDefinition(findDefinition(clazz, name));
			}
		}
		catch (RuntimeException e) {
		//	log.warn("Could not delete definition", e);
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
	
	public void replaceCohortDefinition(CohortDefinition def) {
		CohortDefinitionService cds = Context.getService(CohortDefinitionService.class);
		purgeDefinition(def.getClass(), def.getName());
		cds.saveDefinition(def);
	}
	
	public void replaceDefinition(Definition def) {
		SerializedDefinitionService s = (SerializedDefinitionService) Context.getService(SerializedDefinitionService.class);
		purgeDefinition(def.getClass(), def.getName());
		s.saveDefinition(def);
	}
	
    public void saveReportDefinition(ReportDefinition rd, Boolean saveIndependently) {
		ReportDefinitionService rds = (ReportDefinitionService) Context.getService(ReportDefinitionService.class);
		
		if (saveIndependently && rd instanceof PeriodIndicatorReportDefinition) {
			PeriodIndicatorReportDefinition pird = (PeriodIndicatorReportDefinition)rd;
			CohortIndicatorDataSetDefinition dataSetDefinition = pird.getIndicatorDataSetDefinition();
			
			for (Iterator<CohortIndicatorAndDimensionColumn> iterator = dataSetDefinition.getColumns().iterator(); iterator.hasNext();) {
				CohortIndicatorAndDimensionColumn column = iterator.next();
				replaceDefinition(column.getIndicator().getParameterizable());
	        }
		}
		
		if (saveIndependently && rd instanceof RollingDailyPeriodIndicatorReportDefinition) {
			RollingDailyPeriodIndicatorReportDefinition pird = (RollingDailyPeriodIndicatorReportDefinition)rd;
			RollingDailyIndicatorDataSetDefinition dataSetDefinition = pird.getIndicatorDataSetDefinition();
			
			for (Iterator<RwandaReportsIndicatorAndDimensionColumn> iterator = dataSetDefinition.getColumns().iterator(); iterator.hasNext();) {
				RwandaReportsIndicatorAndDimensionColumn column = iterator.next();
				replaceDefinition(column.getIndicator().getParameterizable());
	        }
		}
		
		
		rds.saveDefinition(rd);
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
	
	
	public void createXlsOverviewBySavingIt(ReportDefinition rd, String resourceName, String name, Map<? extends Object, ? extends Object> properties) throws IOException {
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
    	
    	ReportService rs = Context.getService(ReportService.class);
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
