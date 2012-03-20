/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.rwandareports.renderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.web.renderers.IndicatorReportWebRenderer;
import org.openmrs.module.reporting.web.renderers.WebReportRenderer;

/**
 * Renderer for CD4 Count Report
 *
 */
public class TracNetCustomRenderer extends IndicatorReportWebRenderer implements WebReportRenderer {
	
	
	/**
     * @see org.openmrs.report.ReportRenderer#getLabel()
     */
	@Override
    public String getLabel() {
    	return "TracNet Web Report";
    }
	
	
	/**
	 * @see org.openmrs.module.reporting.web.renderers.WebReportRenderer#getLinkUrl(org.openmrs.module.reporting.report.definition.ReportDefinition)
	 */
	@Override
	public String getLinkUrl(ReportDefinition arg0) {
		return "module/rwandareports/renderTracNetDataSet.form";
	}
	
	/**
	 * @see org.openmrs.report.ReportRenderer#getRenderingModes(org.openmrs.report.ReportDefinition)
	 */
	@Override
	public Collection<RenderingMode> getRenderingModes(ReportDefinition definition) {
		List<RenderingMode> ret = new ArrayList<RenderingMode>();
		for (Map.Entry<String, Mapped<? extends DataSetDefinition>> e : definition.getDataSetDefinitions().entrySet()) {
			String name = e.getKey();
			DataSetDefinition def = e.getValue().getParameterizable();
	    	if ("TracNet Report Data Set".equals(def.getName())) {
				ret.add(new RenderingMode(this, this.getLabel() , name, Integer.MAX_VALUE - 5));
	    	}
		}
		return ret;
	}
	
}
