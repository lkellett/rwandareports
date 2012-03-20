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
package org.openmrs.module.rwandareports.web.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.report.ReportData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller prepares result page for Quarterly HIV Care and ART Reporting
 */
@Controller
public class RenderTracNetController {
	
	@RequestMapping("/module/rwandareports/renderTracNetDataSet.form")
	public String showReport(Model model, HttpSession session) {
		String renderArg = (String) session.getAttribute(ReportingConstants.OPENMRS_REPORT_ARGUMENT);
		ReportData data = null;
		try {
			data = (ReportData) session.getAttribute(ReportingConstants.OPENMRS_REPORT_DATA);
		}
		catch (ClassCastException ex) {
			// pass
		}
		if (data == null)
			return "redirect:../reporting/dashboard/index.form";
		
		MapDataSet dataSet = (MapDataSet) data.getDataSets().get(renderArg);
		model.addAttribute("columns", dataSet.getMetaData());
		//List<Regimen> regimens = RwandaReportsUtil.createRegimenList(Context.getService(Cd4CountReportingService.class).getAllRegimens());  
	//	model.addAttribute("regimens", regimens);
		return null;
	}
	
}