package org.openmrs.module.rwandareports.web.controller;

import org.openmrs.module.rwandareports.reporting.SetupAdultHIVConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupAdultLateVisitAndCD4Report;
import org.openmrs.module.rwandareports.reporting.SetupAsthmaConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupAsthmaLateVisit;
import org.openmrs.module.rwandareports.reporting.SetupAsthmaQuarterlyAndMonthReport;
import org.openmrs.module.rwandareports.reporting.SetupCombinedHFCSPConsultationReport;
import org.openmrs.module.rwandareports.reporting.SetupDataQualityIndicatorReport;
import org.openmrs.module.rwandareports.reporting.SetupDiabetesQuarterlyAndMonthReport;
import org.openmrs.module.rwandareports.reporting.SetupExposedClinicInfantMonthly;
import org.openmrs.module.rwandareports.reporting.SetupHIVResearchDataQualitySheet;
import org.openmrs.module.rwandareports.reporting.SetupHIVResearchExtractionSheet;
import org.openmrs.module.rwandareports.reporting.SetupHeartFailurereport;
import org.openmrs.module.rwandareports.reporting.SetupMissingCD4Report;
import org.openmrs.module.rwandareports.reporting.SetupDiabetesConsultAndLTFU;
import org.openmrs.module.rwandareports.reporting.SetupMonthlyCD4DeclineReport;
import org.openmrs.module.rwandareports.reporting.SetupNCDLateVisitandLTFUReport;
import org.openmrs.module.rwandareports.reporting.SetupNCDConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTCombinedClinicMotherMonthlyReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTFoodDistributionReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTFormCompletionSheet;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTFormulaDistributionReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTPregnancyConsultationReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTPregnancyMonthlyReport;
import org.openmrs.module.rwandareports.reporting.SetupPediHIVConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupPediatricLateVisitAndCD4Report;
import org.openmrs.module.rwandareports.reporting.SetupQuarterlyCrossSiteIndicatorByDistrictReport;
import org.openmrs.module.rwandareports.reporting.SetupQuarterlyViralLoadReport;
import org.openmrs.module.rwandareports.reporting.SetupRwandaPrimaryCareReport;
import org.openmrs.module.rwandareports.reporting.SetupTBConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupTracNetRwandaReportBySite;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;


@Controller
public class RwandaSetupReportsFormController {
                  
	
	@RequestMapping("/module/rwandareports/remove_quarterlyCrossDistrictIndicator")
	public ModelAndView removeQuarterlyCrossDistrictIndicator() throws Exception {
		new SetupQuarterlyCrossSiteIndicatorByDistrictReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_quarterlyCrossDistrictIndicator")
	public ModelAndView registerQuarterlyCrossDistrictIndicator() throws Exception {
		new SetupQuarterlyCrossSiteIndicatorByDistrictReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}

	@RequestMapping("/module/rwandareports/register_adulthivartregister")
	public ModelAndView registerAdultHivArtRegiser() throws Exception {
		//new SetupHivArtRegisterReport(false).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_adulthivartregister")
	public ModelAndView removeAdultHivArtRegister() throws Exception {
		//new SetupHivArtRegisterReport(false).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_pedihivartregister")
	public ModelAndView registerPediHivArtRegiser() throws Exception {
	//	new SetupHivArtRegisterReport(true).setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pedihivartregister")
	public ModelAndView removePediHivArtRegister() throws Exception {
		//new SetupHivArtRegisterReport(true).delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_combinedHSCSPConsultation")
	public ModelAndView registerCombinedHSCSPConsultation() throws Exception {
		new SetupCombinedHFCSPConsultationReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_combinedHSCSPConsultation")
	public ModelAndView removeCombinedHSCSPConsultation() throws Exception {
		new SetupCombinedHFCSPConsultationReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
					
	@RequestMapping("/module/rwandareports/register_pmtctFoodDistributionSheet")
	public ModelAndView registerPmtctFoodDistribution() throws Exception {
		new SetupPMTCTFoodDistributionReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pmtctFoodDistributionSheet")
	public ModelAndView removePmtctFoodDistribution() throws Exception {
		new SetupPMTCTFoodDistributionReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_pmtctFormulaDistributionSheet")
	public ModelAndView registerPmtctFormulaDistribution() throws Exception {
		new SetupPMTCTFormulaDistributionReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pmtctFormulaDistributionSheet")
	public ModelAndView removePmtctFormulaDistribution() throws Exception {
		new SetupPMTCTFormulaDistributionReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_pmtctPregnancyConsultationSheet")
	public ModelAndView registerPmtctPregnancyConsultation() throws Exception {
		new SetupPMTCTPregnancyConsultationReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pmtctPregnancyConsultationSheet")
	public ModelAndView removePmtctPregnanacyConsultation() throws Exception {
		new SetupPMTCTPregnancyConsultationReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	

	@RequestMapping("/module/rwandareports/register_pmtctFormCompletionSheet")
	public ModelAndView registerPmtctFormCompletionSheet() throws Exception {
		new SetupPMTCTFormCompletionSheet().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pmtctFormCompletionSheet")
	public ModelAndView removePmtctFormCompletionSheet() throws Exception {
		new SetupPMTCTFormCompletionSheet().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	//Consult sheets
	@RequestMapping("/module/rwandareports/register_pediHIVConsultationSheet")
	public ModelAndView registerPediHIVConsultationSheet() throws Exception {
		new SetupPediHIVConsultationSheet().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pediHIVConsultationSheet")
	public ModelAndView removePediHIVConsultationSheet() throws Exception {
		new SetupPediHIVConsultationSheet().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_adultHIVConsultationSheet")
	public ModelAndView registerAdultHIVConsultationSheet() throws Exception {
		new SetupAdultHIVConsultationSheet().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_adultHIVConsultationSheet")
	public ModelAndView removeAdultHIVConsultationSheet() throws Exception {
		new SetupAdultHIVConsultationSheet().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_tbConsultationSheet")
	public ModelAndView registerTbConsultationSheet() throws Exception {
		new SetupTBConsultationSheet().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_tbConsultationSheet")
	public ModelAndView removeTbConsultationSheet() throws Exception {
		new SetupTBConsultationSheet().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}

	@RequestMapping("/module/rwandareports/register_asthmaConsultationSheet")
	public ModelAndView registerAsthmaConsultationSheet() throws Exception {
		new SetupAsthmaConsultationSheet().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_asthmaConsultationSheet")
	public ModelAndView removeAsthmaConsultationSheet() throws Exception {
		new SetupAsthmaConsultationSheet().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
//Remove/Register Adult Late visit And CD4
	
	@RequestMapping("/module/rwandareports/register_adultLatevisitAndCD4")
	public ModelAndView registerAdultLatevisitAndCD4() throws Exception {
		new SetupAdultLateVisitAndCD4Report().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_adultLatevisitAndCD4")
	public ModelAndView removeAdultLatevisitAndCD4() throws Exception {
		new SetupAdultLateVisitAndCD4Report().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	//Remove/Register Pediatric Late visit And CD4
	@RequestMapping("/module/rwandareports/register_pediatricLatevisitAndCD4")
	public ModelAndView registerPediatricLatevisitAndCD4() throws Exception {
		new SetupPediatricLateVisitAndCD4Report().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_pediatricLatevisitAndCD4")
	public ModelAndView removePediatricLatevisitAndCD4() throws Exception {
		new SetupPediatricLateVisitAndCD4Report().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	//Remove/Register Rwanda primary care report
	@RequestMapping("/module/rwandareports/remove_rwandaPrimaryCareReport")
	public ModelAndView removeRwandaPrimaryCareIndicator() throws Exception {
		new SetupRwandaPrimaryCareReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_rwandaPrimaryCareReport")
	public ModelAndView registerRwandaPrimaryCareIndicator() throws Exception {
		new SetupRwandaPrimaryCareReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	//Remove/Register Heart Failure report by site and all
	@RequestMapping("/module/rwandareports/remove_heartFailureReport")
	public ModelAndView removeHeartFailureIndicator() throws Exception {
		new SetupHeartFailurereport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_heartFailureReport")
	public ModelAndView registerHeartFailureIndicatorIndicator() throws Exception {
		new SetupHeartFailurereport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	// end of Heart failure report
	
	
	@RequestMapping("/module/rwandareports/register_missingCD4Report")
	public ModelAndView registerMissingCD4Report() throws Exception {
		new SetupMissingCD4Report().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_missingCD4Report")
	public ModelAndView removeMissingCD4Report() throws Exception {
		new SetupMissingCD4Report().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_dataQualityReport")
	public ModelAndView registerDataQualityReport() throws Exception {
		new SetupDataQualityIndicatorReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_dataQualityReport")
	public ModelAndView removeDataQualityReport() throws Exception {
		new SetupDataQualityIndicatorReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
		
	//Research Links
	@RequestMapping("/module/rwandareports/register_hivResearchDataQuality")
	public ModelAndView registerHivResearchDataQualityReport() throws Exception {
		new SetupHIVResearchDataQualitySheet().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_hivResearchDataQuality")
	public ModelAndView removeHivResearchDataQualityReport() throws Exception {
		new SetupHIVResearchDataQualitySheet().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	//Diabetes Consult/LTFU
	@RequestMapping("/module/rwandareports/register_DiabetesConsultAndLTFU")
	public ModelAndView registerDiabetesConsultAndLTFU() throws Exception {
		new SetupDiabetesConsultAndLTFU().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_DiabetesConsultAndLTFU")
	public ModelAndView removeDiabetesConsultAndLTFU() throws Exception {
		new SetupDiabetesConsultAndLTFU().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	//NCD late visit and LTFU
	@RequestMapping("/module/rwandareports/register_NCDlatevistAndLTFU")
	public ModelAndView registerNCDlatevistAndLTFUReport() throws Exception {
		new SetupNCDLateVisitandLTFUReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_NCDlatevistAndLTFU")
	public ModelAndView removeNCDlatevistAndLTFUReport() throws Exception {
		new SetupNCDLateVisitandLTFUReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}

	
	//Remove/Register Pediatric Late visit And CD4
	@RequestMapping("/module/rwandareports/register_NCDConsult")
	public ModelAndView registerNCDConsult() throws Exception {
		new SetupNCDConsultationSheet().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_NCDConsult")
	public ModelAndView removeNCDConsult() throws Exception {
		new SetupNCDConsultationSheet().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}

	
	@RequestMapping("/module/rwandareports/register_hivResearchDataExtraction")
	public ModelAndView registerHivResearchDataExtractionReport() throws Exception {
		new SetupHIVResearchExtractionSheet().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_hivResearchDataExtraction")
	public ModelAndView removeHivResearchDataExtractionReport() throws Exception {
		new SetupHIVResearchExtractionSheet().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_viralLoad")
	public ModelAndView registerViralLoadReport() throws Exception {
		new SetupQuarterlyViralLoadReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_viralLoad")
	public ModelAndView removeViralLoadReport() throws Exception {
		new SetupQuarterlyViralLoadReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	@RequestMapping("/module/rwandareports/register_DiabetesQuarterlyAndMonthReport")
	public ModelAndView registerDiabetesQuarterlyAndMonthReport() throws Exception {
		new SetupDiabetesQuarterlyAndMonthReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/remove_DiabetesQuarterlyAndMonthReport")
	public ModelAndView removeDiabetesQuarterlyAndMonthReport() throws Exception {
		new SetupDiabetesQuarterlyAndMonthReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	//Remove/Register TracNet report
	@RequestMapping("/module/rwandareports/remove_tracNetReport")
	public ModelAndView removeTracNetIndicator() throws Exception {
		new SetupTracNetRwandaReportBySite().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_tracNetReport")
	public ModelAndView registerTracNetIndicator() throws Exception {
		new SetupTracNetRwandaReportBySite().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	//Remove/Register PMTCT Combined Clinic Mother Monthly Report report
	
	@RequestMapping("/module/rwandareports/register_pmtctCombinedClinicMotherMonthlyReport")
	public ModelAndView registerPMTCTCombinedClinicMotherMonthly() throws Exception {
		new SetupPMTCTCombinedClinicMotherMonthlyReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	@RequestMapping("/module/rwandareports/remove_pmtctCombinedClinicMotherMonthlyReport")
	public ModelAndView removePMTCTCombinedClinicMotherMonthly() throws Exception {
		new SetupPMTCTCombinedClinicMotherMonthlyReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	
//Remove/Register Asthma Quarterly And Monthly Report
	
	@RequestMapping("/module/rwandareports/register_asthmaQuarterlyAndMonthReport")
	public ModelAndView registerAsthmaQuarterlyAndMonthReport() throws Exception {
		new SetupAsthmaQuarterlyAndMonthReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	@RequestMapping("/module/rwandareports/remove_asthmaQuarterlyAndMonthReport")
	public ModelAndView removeAsthmaQuarterlyAndMonthReport() throws Exception {
		new SetupAsthmaQuarterlyAndMonthReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
//Remove/Register Asthma Late Visit Report
	
	@RequestMapping("/module/rwandareports/register_asthmaLateVisitReport")
	public ModelAndView registerAsthmaLateVisit() throws Exception {
		new SetupAsthmaLateVisit().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	@RequestMapping("/module/rwandareports/remove_asthmaLateVisitReport")
	public ModelAndView removeregisterAsthmaLateVisit() throws Exception {
		new SetupAsthmaLateVisit().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
//Remove/Register PMTCT Pregnancy Monthly Report
	
	@RequestMapping("/module/rwandareports/register_pmtctPregMonthlyReport")
	public ModelAndView registerPMTCTPregMonthlyVisit() throws Exception {
		new SetupPMTCTPregnancyMonthlyReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	@RequestMapping("/module/rwandareports/remove_pmtctPregMonthlyReport")
	public ModelAndView removePMTCTPregMonthlyVisit() throws Exception {
		new SetupPMTCTPregnancyMonthlyReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
   //Remove/Register Combined Infant Monthly Report
	
	@RequestMapping("/module/rwandareports/register_pmtctCombinedClinicInfantReport")
	public ModelAndView registerPMTCTCombinedInfantReport() throws Exception {
		new SetupExposedClinicInfantMonthly().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	@RequestMapping("/module/rwandareports/remove_pmtctCombinedClinicInfantReport")
	public ModelAndView removePMTCTCombinedInfantReport() throws Exception {
		new SetupExposedClinicInfantMonthly().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
	@RequestMapping("/module/rwandareports/register_monthlyCD4Decline")
	public ModelAndView registerMonthlyCD4Decline() throws Exception {
		new SetupMonthlyCD4DeclineReport().setup();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	@RequestMapping("/module/rwandareports/remove_monthlyCD4Decline")
	public ModelAndView removeMonthlyCD4Decline() throws Exception {
		new SetupMonthlyCD4DeclineReport().delete();
		return new ModelAndView(new RedirectView("rwandareports.form"));
	}
	
}
