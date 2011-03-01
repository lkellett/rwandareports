package org.openmrs.module.rwandareports.web.controller;

import org.openmrs.module.rwandareports.reporting.Helper;
import org.openmrs.module.rwandareports.reporting.SetupAdultLateVisitAndCD4Report;
import org.openmrs.module.rwandareports.reporting.SetupCombinedHFCSPConsultationReport;
import org.openmrs.module.rwandareports.reporting.SetupHivArtRegisterReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTFoodDistributionReport;
import org.openmrs.module.rwandareports.reporting.SetupQuarterlyCrossSiteIndicatorBySiteReport;
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
	
	@RequestMapping("/module/rwandareports/remove_quarterlyCrossRegionIndicator")
	public ModelAndView removeQuarterlyCrossRegionIndicator() throws Exception {
		new SetupQuarterlyCrossSiteIndicatorBySiteReport(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_quarterlyCrossRegionIndicator")
	public ModelAndView registerQuarterlyCrossRegionIndicator() throws Exception {
		new SetupQuarterlyCrossSiteIndicatorBySiteReport(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}

	@RequestMapping("/module/rwandareports/register_hivartregister")
	public ModelAndView registerHivArtRegiser() throws Exception {
		new SetupHivArtRegisterReport(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_hivartregister")
	public ModelAndView removeHivArtRegister() throws Exception {
		new SetupHivArtRegisterReport(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_combinedHSCSPConsultation")
	public ModelAndView registerCombinedHSCSPConsultation() throws Exception {
		new SetupCombinedHFCSPConsultationReport(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_combinedHSCSPConsultation")
	public ModelAndView removeCombinedHSCSPConsultation() throws Exception {
		new SetupCombinedHFCSPConsultationReport(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_pmtctFoodDistribution")
	public ModelAndView registerPmtctFoodDistribution() throws Exception {
		new SetupPMTCTFoodDistributionReport(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pmtctFoodDistribution")
	public ModelAndView removePmtctFoodDistribution() throws Exception {
		new SetupPMTCTFoodDistributionReport(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	//Remove Register Late visit And CD4
	
	@RequestMapping("/module/rwandareports/register_adultLatevisitAndCD4")
	public ModelAndView registerAdultLatevisitAndCD4() throws Exception {
		new SetupAdultLateVisitAndCD4Report(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_adultLatevisitAndCD4")
	public ModelAndView removeAdultLatevisitAndCD4() throws Exception {
		new SetupAdultLateVisitAndCD4Report(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	
	
	
	
}
