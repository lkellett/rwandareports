package org.openmrs.module.rwandareports.web.renderer;

import java.util.Collection;
import java.util.Collections;

import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.web.renderers.AbstractWebReportRenderer;
import org.openmrs.module.rwandareports.dataset.RollingDailyIndicatorDataSetDefinition;

@Handler
public class RollingDailyCalendarWebReportRenderer extends AbstractWebReportRenderer {
	
	/**
     * @see org.openmrs.module.reporting.report.renderer.ReportRenderer#canRender(org.openmrs.module.reporting.report.definition.ReportDefinition)
     */
    public boolean canRender(ReportDefinition reportDefinition) {
    	for (Mapped<? extends DataSetDefinition> def : reportDefinition.getDataSetDefinitions().values()) {
	    	if (def.getParameterizable() instanceof RollingDailyIndicatorDataSetDefinition) {
	    		return true;
	    	}    	
	    }
	    return false;
    }

	/**
     * @see org.openmrs.report.ReportRenderer#getLabel()
     */
    public String getLabel() {
    	return "Rwanda Reports Custom Web Indicator Renderer";
    }

	/**
	 * @see org.openmrs.report.ReportRenderer#getLinkUrl(org.openmrs.report.ReportDefinition)
	 */
	public String getLinkUrl(ReportDefinition reportDefinition) {
		return "module/reporting/reports/renderIndicatorReportData.form";
	}
		
	/**
	 * @see org.openmrs.report.ReportRenderer#getRenderingModes(org.openmrs.report.ReportDefinition)
	 */
	public Collection<RenderingMode> getRenderingModes(ReportDefinition schema) {
		return Collections.singleton(new RenderingMode(this, this.getLabel(), null, Integer.MAX_VALUE - 10));
	}
	
}
