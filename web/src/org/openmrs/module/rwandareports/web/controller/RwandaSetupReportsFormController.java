package org.openmrs.module.rwandareports.web.controller;

import org.openmrs.module.rwandareports.reporting.Helper;
import org.openmrs.module.rwandareports.reporting.SetupAdultHIVConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupAdultLateVisitAndCD4Report;
import org.openmrs.module.rwandareports.reporting.SetupCombinedHFCSPConsultationReport;
import org.openmrs.module.rwandareports.reporting.SetupDataQualityIndicatorBySiteReport;
import org.openmrs.module.rwandareports.reporting.SetupHeartFailurereportAllSites;
import org.openmrs.module.rwandareports.reporting.SetupHeartFailurereportBySite;
import org.openmrs.module.rwandareports.reporting.SetupHivArtRegisterReport;
import org.openmrs.module.rwandareports.reporting.SetupMissingCD4AllSiteReport;
import org.openmrs.module.rwandareports.reporting.SetupMissingCD4Report;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTFoodDistributionReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTFormulaDistributionReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTPregnancyConsultationReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTRegisterReport;
import org.openmrs.module.rwandareports.reporting.SetupPediHIVConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupPediatricLateVisitAndCD4Report;
import org.openmrs.module.rwandareports.reporting.SetupQuarterlyCrossSiteIndicatorByDistrictReport;
import org.openmrs.module.rwandareports.reporting.SetupQuarterlyCrossSiteIndicatorBySiteReport;
import org.openmrs.module.rwandareports.reporting.SetupQuarterlyCrossSiteIndicatorReport;
import org.openmrs.module.rwandareports.reporting.SetupRwandaPrimaryCareReport;
import org.openmrs.module.rwandareports.reporting.SetupTBConsultationSheet;
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
	
	@RequestMapping("/module/rwandareports/remove_quarterlyCrossDistrictIndicator")
	public ModelAndView removeQuarterlyCrossDistrictIndicator() throws Exception {
		new SetupQuarterlyCrossSiteIndicatorByDistrictReport(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_quarterlyCrossDistrictIndicator")
	public ModelAndView registerQuarterlyCrossDistrictIndicator() throws Exception {
		new SetupQuarterlyCrossSiteIndicatorByDistrictReport(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}

	@RequestMapping("/module/rwandareports/register_adulthivartregister")
	public ModelAndView registerAdultHivArtRegiser() throws Exception {
		new SetupHivArtRegisterReport(new Helper(), false).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pmtctregister")
	public ModelAndView removePMTCTRegister() throws Exception {
		new SetupPMTCTRegisterReport(new Helper(), false).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_pmtctregister")
	public ModelAndView registerPMTCTRegiser() throws Exception {
		new SetupPMTCTRegisterReport(new Helper(), false).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_adulthivartregister")
	public ModelAndView removeAdultHivArtRegister() throws Exception {
		new SetupHivArtRegisterReport(new Helper(), false).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_pedihivartregister")
	public ModelAndView registerPediHivArtRegiser() throws Exception {
		new SetupHivArtRegisterReport(new Helper(), true).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pedihivartregister")
	public ModelAndView removePediHivArtRegister() throws Exception {
		new SetupHivArtRegisterReport(new Helper(), true).delete();
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
					
	@RequestMapping("/module/rwandareports/register_pmtctFoodDistributionSheet")
	public ModelAndView registerPmtctFoodDistribution() throws Exception {
		new SetupPMTCTFoodDistributionReport(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pmtctFoodDistributionSheet")
	public ModelAndView removePmtctFoodDistribution() throws Exception {
		new SetupPMTCTFoodDistributionReport(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_pmtctFormulaDistributionSheet")
	public ModelAndView registerPmtctFormulaDistribution() throws Exception {
		new SetupPMTCTFormulaDistributionReport(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pmtctFormulaDistributionSheet")
	public ModelAndView removePmtctFormulaDistribution() throws Exception {
		new SetupPMTCTFormulaDistributionReport(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_pmtctPregnancyConsultationSheet")
	public ModelAndView registerPmtctPregnancyConsultation() throws Exception {
		new SetupPMTCTPregnancyConsultationReport(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pmtctPregnancyConsultationSheet")
	public ModelAndView removePmtctPregnanacyConsultation() throws Exception {
		new SetupPMTCTPregnancyConsultationReport(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_pediHIVConsultationSheet")
	public ModelAndView registerPediHIVConsultationSheet() throws Exception {
		new SetupPediHIVConsultationSheet(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pediHIVConsultationSheet")
	public ModelAndView removePediHIVConsultationSheet() throws Exception {
		new SetupPediHIVConsultationSheet(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_adultHIVConsultationSheet")
	public ModelAndView registerAdultHIVConsultationSheet() throws Exception {
		new SetupAdultHIVConsultationSheet(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_adultHIVConsultationSheet")
	public ModelAndView removeAdultHIVConsultationSheet() throws Exception {
		new SetupAdultHIVConsultationSheet(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_tbConsultationSheet")
	public ModelAndView registerTbConsultationSheet() throws Exception {
		new SetupTBConsultationSheet(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_tbConsultationSheet")
	public ModelAndView removeTbConsultationSheet() throws Exception {
		new SetupTBConsultationSheet(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
//Remove/Register Adult Late visit And CD4
	
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
	//Remove/Register Pediatric Late visit And CD4
	@RequestMapping("/module/rwandareports/register_pediatricLatevisitAndCD4")
	public ModelAndView registerPediatricLatevisitAndCD4() throws Exception {
		new SetupPediatricLateVisitAndCD4Report(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pediatricLatevisitAndCD4")
	public ModelAndView removePediatricLatevisitAndCD4() throws Exception {
		new SetupPediatricLateVisitAndCD4Report(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	//Remove/Register Rwanda primary care report
	@RequestMapping("/module/rwandareports/remove_rwandaPrimaryCareReport")
	public ModelAndView removeRwandaPrimaryCareIndicator() throws Exception {
		new SetupRwandaPrimaryCareReport(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_rwandaPrimaryCareReport")
	public ModelAndView registerRwandaPrimaryCareIndicator() throws Exception {
		new SetupRwandaPrimaryCareReport(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	//Remove/Register Heart Failure report by site and all
	@RequestMapping("/module/rwandareports/remove_heartFailureReport")
	public ModelAndView removeHeartFailureIndicator() throws Exception {
		new SetupHeartFailurereportBySite(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_heartFailureReport")
	public ModelAndView registerHeartFailureIndicatorIndicator() throws Exception {
		new SetupHeartFailurereportBySite(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	@RequestMapping("/module/rwandareports/remove_heartFailureReportAllSites")
	public ModelAndView removeHeartFailureAllSitesIndicator() throws Exception {
		new SetupHeartFailurereportAllSites(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_heartFailureReportAllSites")
	public ModelAndView registerHeartFailureAllSitesIndicatorIndicator() throws Exception {
		new SetupHeartFailurereportAllSites(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	// end of Heart failure report
	
	
	@RequestMapping("/module/rwandareports/register_missingCD4Report")
	public ModelAndView registerMissingCD4Report() throws Exception {
		new SetupMissingCD4Report(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_missingCD4Report")
	public ModelAndView removeMissingCD4Report() throws Exception {
		new SetupMissingCD4Report(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_missingCD4AllSiteReport")
	public ModelAndView registerMissingCD4AllSiteReport() throws Exception {
		new SetupMissingCD4AllSiteReport(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_missingCD4AllSiteReport")
	public ModelAndView removeMissingCD4AllSiteReport() throws Exception {
		new SetupMissingCD4AllSiteReport(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_dataQualityReport")
	public ModelAndView registerDataQualityReport() throws Exception {
		new SetupDataQualityIndicatorBySiteReport(new Helper()).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_dataQualityReport")
	public ModelAndView removeDataQualityReport() throws Exception {
		new SetupDataQualityIndicatorBySiteReport(new Helper()).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
}
