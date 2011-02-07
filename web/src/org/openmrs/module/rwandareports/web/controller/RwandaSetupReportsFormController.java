package org.openmrs.module.rwandareports.web.controller;

import org.openmrs.module.rwandareports.reporting.Helper;
import org.openmrs.module.rwandareports.reporting.SetupQuarterlyCrossSiteIndicatorReport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;


@Controller
public class RwandaSetupReportsFormController {
                      
	@RequestMapping("/module/rwandareports/remove_quarterlyCrossSiteIndicator")
	public ModelAndView removeQuarterlyCrossSiteIndicator() throws Exception {
		new SetupQuarterlyCrossSiteIndicatorReport(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_quarterlyCrossSiteIndicator")
	public ModelAndView registerQuarterlyCrossSiteIndicator() throws Exception {
		new SetupQuarterlyCrossSiteIndicatorReport(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
}
